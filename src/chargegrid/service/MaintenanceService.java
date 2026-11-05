package chargegrid.service;

import chargegrid.dao.ChargerDAO;
import chargegrid.dao.MaintenanceDAO;
import chargegrid.exception.ChargeGridException;
import chargegrid.exception.InvalidInputException;
import chargegrid.model.*;
import chargegrid.util.DBConnection;
import chargegrid.util.Validator;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class MaintenanceService {
    private final MaintenanceDAO maintenanceDAO;
    private final ChargerDAO chargerDAO;

    public MaintenanceService(MaintenanceDAO maintenanceDAO, ChargerDAO chargerDAO) {
        this.maintenanceDAO = maintenanceDAO;
        this.chargerDAO = chargerDAO;
    }

    /** Raises a ticket; an AVAILABLE charger is marked FAULTY so nobody books it. */
    public MaintenanceTicket reportFault(int userId, int chargerId, String description) throws SQLException, ChargeGridException {
        Charger c = chargerDAO.findById(chargerId);
        if (c == null) throw new ChargeGridException("Charger " + chargerId + " not found.");
        if (Validator.isBlank(description) || description.trim().length() < 5)
            throw new InvalidInputException("Please describe the problem (at least 5 characters).");
        LocalDateTime now = LocalDateTime.now();
        int id = maintenanceDAO.addTicket(new MaintenanceTicket(0, chargerId, userId, description.trim(), TicketStatus.OPEN, 0, now, null));
        if (c.getStatus() == ChargerStatus.AVAILABLE) chargerDAO.updateStatus(chargerId, ChargerStatus.FAULTY);
        return new MaintenanceTicket(id, chargerId, userId, description.trim(), TicketStatus.OPEN, 0, now, null);
    }

    public void assignTechnician(int ticketId, int technicianId) throws SQLException, ChargeGridException {
        if (maintenanceDAO.findTicket(ticketId) == null) throw new ChargeGridException("Ticket " + ticketId + " not found.");
        if (maintenanceDAO.findTechnician(technicianId) == null) throw new ChargeGridException("Technician " + technicianId + " not found.");
        if (!maintenanceDAO.assignTechnician(ticketId, technicianId)) throw new ChargeGridException("Ticket " + ticketId + " is already resolved.");
    }

    /** Resolves the ticket and brings a FAULTY charger back to AVAILABLE (one transaction). */
    public void resolveTicket(int ticketId) throws SQLException, ChargeGridException {
        MaintenanceTicket t = maintenanceDAO.findTicket(ticketId);
        if (t == null) throw new ChargeGridException("Ticket " + ticketId + " not found.");
        if (t.getStatus() == TicketStatus.RESOLVED) throw new ChargeGridException("Ticket " + ticketId + " is already resolved.");
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                maintenanceDAO.resolveTicket(con, ticketId);
                Charger c = chargerDAO.findById(t.getChargerId());
                if (c != null && c.getStatus() == ChargerStatus.FAULTY) chargerDAO.updateStatus(con, c.getId(), ChargerStatus.AVAILABLE);
                con.commit();
            } catch (SQLException | RuntimeException e) {
                con.rollback();
                throw e;
            }
        }
    }

    /** Manager guard: the ticket's charger must be in the manager's station. */
    public MaintenanceTicket getTicketInStation(int ticketId, int stationId) throws SQLException, ChargeGridException {
        MaintenanceTicket t = maintenanceDAO.findTicket(ticketId);
        if (t == null) throw new ChargeGridException("Ticket " + ticketId + " not found.");
        Charger c = chargerDAO.findById(t.getChargerId());
        if (c == null || c.getStationId() != stationId) throw new ChargeGridException("Ticket " + ticketId + " is not in your station.");
        return t;
    }

    public Technician addTechnician(String name, String phone, String speciality) throws SQLException, ChargeGridException {
        if (Validator.isBlank(name) || Validator.isBlank(speciality)) throw new InvalidInputException("Name and speciality are required.");
        if (!Validator.isValidPhone(phone)) throw new InvalidInputException("Phone must be a 10-digit mobile number.");
        int id = maintenanceDAO.addTechnician(new Technician(0, name, phone, speciality));
        return new Technician(id, name, phone, speciality);
    }

    public List<Technician> listTechnicians() throws SQLException {
        return maintenanceDAO.findAllTechnicians();
    }

    public List<MaintenanceTicket> listAllTickets() throws SQLException {
        return maintenanceDAO.findAllTickets();
    }

    public List<MaintenanceTicket> listTicketsByStation(int stationId) throws SQLException {
        return maintenanceDAO.findTicketsByStation(stationId);
    }
}
