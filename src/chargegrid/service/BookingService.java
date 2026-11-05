package chargegrid.service;

import chargegrid.dao.BookingDAO;
import chargegrid.dao.ChargerDAO;
import chargegrid.exception.BookingConflictException;
import chargegrid.exception.ChargeGridException;
import chargegrid.exception.ChargerUnavailableException;
import chargegrid.exception.InvalidInputException;
import chargegrid.model.*;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class BookingService {
    public static final int MIN_MINUTES = 15;
    public static final int MAX_MINUTES = 240;

    private final BookingDAO bookingDAO;
    private final VehicleService vehicleService;
    private final ChargerDAO chargerDAO;

    public BookingService(BookingDAO bookingDAO, VehicleService vehicleService, ChargerDAO chargerDAO) {
        this.bookingDAO = bookingDAO;
        this.vehicleService = vehicleService;
        this.chargerDAO = chargerDAO;
    }

    public Booking createBooking(int userId, int vehicleId, int chargerId, LocalDateTime start, int minutes)
            throws SQLException, ChargeGridException {
        vehicleService.getOwnedVehicle(vehicleId, userId);
        Charger c = chargerDAO.findById(chargerId);
        if (c == null) throw new ChargeGridException("Charger " + chargerId + " not found.");
        if (c.getStatus() == ChargerStatus.FAULTY) throw new ChargerUnavailableException("Charger " + chargerId + " is faulty and cannot be booked.");
        if (minutes < MIN_MINUTES || minutes > MAX_MINUTES)
            throw new InvalidInputException("Duration must be " + MIN_MINUTES + "-" + MAX_MINUTES + " minutes.");
        if (start.isBefore(LocalDateTime.now().minusMinutes(1)))
            throw new InvalidInputException("Start time cannot be in the past.");
        if (start.isAfter(LocalDateTime.now().plusDays(30)))
            throw new InvalidInputException("Bookings can only be made up to 30 days ahead.");

        LocalDateTime end = start.plusMinutes(minutes);
        if (bookingDAO.hasConflict(chargerId, start, end))
            throw new BookingConflictException("That slot overlaps another booking on this charger. Try a different time or charger.");

        Booking b = new Booking(0, userId, vehicleId, chargerId, start, end, BookingStatus.CONFIRMED);
        int id = bookingDAO.addBooking(b);
        return new Booking(id, userId, vehicleId, chargerId, start, end, BookingStatus.CONFIRMED);
    }

    public void cancelBooking(int userId, int bookingId) throws SQLException, ChargeGridException {
        Booking b = bookingDAO.findById(bookingId);
        if (b == null || b.getUserId() != userId) throw new ChargeGridException("Booking " + bookingId + " not found.");
        if (b.getStatus() != BookingStatus.CONFIRMED) throw new ChargeGridException("Only CONFIRMED bookings can be cancelled.");
        if (!b.getStartTime().isAfter(LocalDateTime.now())) throw new ChargeGridException("This slot has already started and cannot be cancelled.");
        bookingDAO.updateStatus(bookingId, BookingStatus.CANCELLED);
    }

    public List<Booking> listForUser(int userId) throws SQLException {
        return bookingDAO.findByUser(userId);
    }

    public List<Booking> listForStation(int stationId) throws SQLException {
        return bookingDAO.findByStation(stationId);
    }

    public List<Booking> listAll() throws SQLException {
        return bookingDAO.findAll();
    }
}
