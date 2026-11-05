package chargegrid.dao;

import chargegrid.model.Booking;
import chargegrid.model.BookingStatus;
import chargegrid.util.DBConnection;
import chargegrid.util.DateTimeUtil;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BookingDAOImpl implements BookingDAO {

    private Booking map(ResultSet rs) throws SQLException {
        return new Booking(rs.getInt("booking_id"), rs.getInt("user_id"), rs.getInt("vehicle_id"),
                rs.getInt("charger_id"), DateTimeUtil.fromTimestamp(rs.getTimestamp("start_time")),
                DateTimeUtil.fromTimestamp(rs.getTimestamp("end_time")), BookingStatus.valueOf(rs.getString("status")));
    }

    private List<Booking> query(String sql, Integer param) throws SQLException {
        List<Booking> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            if (param != null) ps.setInt(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    @Override
    public int addBooking(Booking b) throws SQLException {
        String sql = "INSERT INTO bookings (user_id, vehicle_id, charger_id, start_time, end_time, status) VALUES (?,?,?,?,?,?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, b.getUserId());
            ps.setInt(2, b.getVehicleId());
            ps.setInt(3, b.getChargerId());
            ps.setTimestamp(4, DateTimeUtil.toTimestamp(b.getStartTime()));
            ps.setTimestamp(5, DateTimeUtil.toTimestamp(b.getEndTime()));
            ps.setString(6, b.getStatus().name());
            ps.executeUpdate();
            try (ResultSet k = ps.getGeneratedKeys()) {
                return k.next() ? k.getInt(1) : -1;
            }
        }
    }

    @Override
    public Booking findById(int id) throws SQLException {
        List<Booking> l = query("SELECT * FROM bookings WHERE booking_id = ?", id);
        return l.isEmpty() ? null : l.get(0);
    }

    @Override
    public List<Booking> findAll() throws SQLException {
        return query("SELECT * FROM bookings ORDER BY start_time DESC", null);
    }

    @Override
    public List<Booking> findByUser(int userId) throws SQLException {
        return query("SELECT * FROM bookings WHERE user_id = ? ORDER BY start_time DESC", userId);
    }

    @Override
    public List<Booking> findByStation(int stationId) throws SQLException {
        return query("SELECT b.* FROM bookings b JOIN chargers c ON c.charger_id = b.charger_id "
                + "WHERE c.station_id = ? ORDER BY b.start_time DESC", stationId);
    }

    @Override
    public boolean hasConflict(int chargerId, LocalDateTime start, LocalDateTime end) throws SQLException {
        String sql = "SELECT COUNT(*) FROM bookings WHERE charger_id = ? AND status = 'CONFIRMED' "
                + "AND start_time < ? AND end_time > ?";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, chargerId);
            ps.setTimestamp(2, DateTimeUtil.toTimestamp(end));
            ps.setTimestamp(3, DateTimeUtil.toTimestamp(start));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    @Override
    public boolean updateStatus(int id, BookingStatus status) throws SQLException {
        try (Connection con = DBConnection.getConnection()) {
            return updateStatus(con, id, status);
        }
    }

    @Override
    public boolean updateStatus(Connection con, int id, BookingStatus status) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("UPDATE bookings SET status = ? WHERE booking_id = ?")) {
            ps.setString(1, status.name());
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        }
    }
}
