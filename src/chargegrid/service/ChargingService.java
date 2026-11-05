package chargegrid.service;

import chargegrid.dao.*;
import chargegrid.exception.ChargeGridException;
import chargegrid.exception.ChargerUnavailableException;
import chargegrid.exception.InvalidInputException;
import chargegrid.model.*;
import chargegrid.util.DBConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Charging sessions. Real-time charging cannot be streamed in a console app, so the user
 * tells us how many minutes they charged and we simulate the energy delivered:
 * energy = min(charger kW, vehicle max kW) x hours x efficiency (capped at 90% of the battery).
 */
public class ChargingService {
    private static final double EFFICIENCY = 0.92;
    private static final int WALK_IN_BUFFER_MIN = 30;

    private final SessionDAO sessionDAO;
    private final ChargerDAO chargerDAO;
    private final VehicleDAO vehicleDAO;
    private final BookingDAO bookingDAO;
    private final BillingService billingService;

    public ChargingService(SessionDAO sessionDAO, ChargerDAO chargerDAO, VehicleDAO vehicleDAO,
                           BookingDAO bookingDAO, BillingService billingService) {
        this.sessionDAO = sessionDAO;
        this.chargerDAO = chargerDAO;
        this.vehicleDAO = vehicleDAO;
        this.bookingDAO = bookingDAO;
        this.billingService = billingService;
    }

    public double estimateEnergy(Vehicle v, Charger c, int minutes) {
        double kw = Math.min(c.getPowerKw(), v.getMaxChargeKw());
        double kwh = kw * (minutes / 60.0) * EFFICIENCY;
        kwh = Math.min(kwh, v.getBatteryKwh() * 0.9);
        return Math.round(kwh * 100.0) / 100.0;
    }

    public double estimateCost(Vehicle v, Charger c, int minutes, LocalDateTime when) {
        return c.calculateCost(estimateEnergy(v, c, minutes), when);
    }

    /** bookingId = 0 for a walk-in session. */
    public ChargingSession startSession(int userId, int vehicleId, int chargerId, int bookingId)
            throws SQLException, ChargeGridException {
        Vehicle v = vehicleDAO.findById(vehicleId);
        if (v == null || v.getOwnerId() != userId) throw new ChargeGridException("Vehicle " + vehicleId + " not found in your garage.");
        for (ChargingSession a : sessionDAO.findActiveByUser(userId)) {
            if (a.getVehicleId() == vehicleId) throw new ChargeGridException("This vehicle already has an active session (#" + a.getId() + ").");
        }

        Charger c = chargerDAO.findById(chargerId);
        if (c == null) throw new ChargeGridException("Charger " + chargerId + " not found.");
        if (c.getStatus() == ChargerStatus.FAULTY) throw new ChargerUnavailableException("Charger " + chargerId + " is faulty.");
        if (c.getStatus() == ChargerStatus.OCCUPIED) throw new ChargerUnavailableException("Charger " + chargerId + " is already in use.");

        LocalDateTime now = LocalDateTime.now();
        if (bookingId > 0) {
            Booking b = bookingDAO.findById(bookingId);
            if (b == null || b.getUserId() != userId) throw new ChargeGridException("Booking " + bookingId + " not found.");
            if (b.getStatus() != BookingStatus.CONFIRMED) throw new ChargeGridException("Booking " + bookingId + " is " + b.getStatus() + ".");
            if (b.getChargerId() != chargerId) throw new ChargeGridException("Booking " + bookingId + " is for charger " + b.getChargerId() + ".");
            if (b.getVehicleId() != vehicleId) throw new ChargeGridException("Booking " + bookingId + " is for a different vehicle.");
            if (now.isBefore(b.getStartTime().minusMinutes(15)) || now.isAfter(b.getEndTime()))
                throw new ChargeGridException("You can only start within 15 minutes before the slot until it ends ("
                        + chargegrid.util.DateTimeUtil.format(b.getStartTime()) + " to " + chargegrid.util.DateTimeUtil.format(b.getEndTime()) + ").");
        } else if (bookingDAO.hasConflict(chargerId, now, now.plusMinutes(WALK_IN_BUFFER_MIN))) {
            throw new ChargerUnavailableException("This charger is reserved by another customer within the next "
                    + WALK_IN_BUFFER_MIN + " minutes. Pick another charger.");
        }

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                ChargingSession draft = new ChargingSession(0, userId, vehicleId, chargerId, bookingId, now, null, 0, 0, SessionStatus.ACTIVE);
                int id = sessionDAO.startSession(con, draft);
                chargerDAO.updateStatus(con, chargerId, ChargerStatus.OCCUPIED);
                con.commit();
                return new ChargingSession(id, userId, vehicleId, chargerId, bookingId, now, null, 0, 0, SessionStatus.ACTIVE);
            } catch (SQLException | RuntimeException e) {
                con.rollback();
                throw e;
            }
        }
    }

    /** Completes the session, frees the charger, issues the invoice and auto-pays from the wallet if possible. */
    public Invoice stopSession(int userId, int sessionId, int minutes) throws SQLException, ChargeGridException {
        if (minutes < 1 || minutes > 600) throw new InvalidInputException("Charging time must be 1-600 minutes.");
        ChargingSession s = sessionDAO.findById(sessionId);
        if (s == null || s.getUserId() != userId) throw new ChargeGridException("Session " + sessionId + " not found.");
        if (s.getStatus() != SessionStatus.ACTIVE) throw new ChargeGridException("Session " + sessionId + " is already completed.");

        Vehicle v = vehicleDAO.findById(s.getVehicleId());
        Charger c = chargerDAO.findById(s.getChargerId());
        double energy = estimateEnergy(v, c, minutes);
        double cost = c.calculateCost(energy, s.getStartTime());
        LocalDateTime end = s.getStartTime().plusMinutes(minutes);

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                sessionDAO.completeSession(con, sessionId, end, energy, cost);
                chargerDAO.updateStatus(con, c.getId(), ChargerStatus.AVAILABLE);
                if (s.getBookingId() > 0) bookingDAO.updateStatus(con, s.getBookingId(), BookingStatus.COMPLETED);
                Invoice inv = billingService.createInvoice(con, s, cost);
                billingService.tryAutoPay(con, inv);
                con.commit();
                return billingService.getInvoice(inv.getId());
            } catch (SQLException | RuntimeException e) {
                con.rollback();
                throw e;
            }
        }
    }

    public List<ChargingSession> activeSessions(int userId) throws SQLException {
        return sessionDAO.findActiveByUser(userId);
    }

    public List<ChargingSession> history(int userId) throws SQLException {
        return sessionDAO.findByUser(userId);
    }

    public List<ChargingSession> listForStation(int stationId) throws SQLException {
        return sessionDAO.findByStation(stationId);
    }

    public List<ChargingSession> listAll() throws SQLException {
        return sessionDAO.findAll();
    }
}
