package chargegrid.dao;

import chargegrid.model.Booking;
import chargegrid.model.BookingStatus;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public interface BookingDAO {
    int addBooking(Booking b) throws SQLException;
    Booking findById(int id) throws SQLException;
    List<Booking> findAll() throws SQLException;
    List<Booking> findByUser(int userId) throws SQLException;
    List<Booking> findByStation(int stationId) throws SQLException;
    /** True if a CONFIRMED booking on this charger overlaps [start, end). */
    boolean hasConflict(int chargerId, LocalDateTime start, LocalDateTime end) throws SQLException;
    boolean updateStatus(int id, BookingStatus status) throws SQLException;
    boolean updateStatus(Connection con, int id, BookingStatus status) throws SQLException;
}
