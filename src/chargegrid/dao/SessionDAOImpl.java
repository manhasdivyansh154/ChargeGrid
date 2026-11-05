package chargegrid.dao;

import chargegrid.model.ChargingSession;
import chargegrid.model.SessionStatus;
import chargegrid.util.DBConnection;
import chargegrid.util.DateTimeUtil;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SessionDAOImpl implements SessionDAO {

    private ChargingSession map(ResultSet rs) throws SQLException {
        return new ChargingSession(rs.getInt("session_id"), rs.getInt("user_id"), rs.getInt("vehicle_id"),
                rs.getInt("charger_id"), rs.getInt("booking_id"),
                DateTimeUtil.fromTimestamp(rs.getTimestamp("start_time")),
                DateTimeUtil.fromTimestamp(rs.getTimestamp("end_time")),
                rs.getDouble("energy_kwh"), rs.getDouble("cost"), SessionStatus.valueOf(rs.getString("status")));
    }

    private List<ChargingSession> query(String sql, Integer param) throws SQLException {
        List<ChargingSession> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            if (param != null) ps.setInt(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    @Override
    public int startSession(Connection con, ChargingSession s) throws SQLException {
        String sql = "INSERT INTO charging_sessions (user_id, vehicle_id, charger_id, booking_id, start_time, status) VALUES (?,?,?,?,?,'ACTIVE')";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, s.getUserId());
            ps.setInt(2, s.getVehicleId());
            ps.setInt(3, s.getChargerId());
            if (s.getBookingId() > 0) ps.setInt(4, s.getBookingId()); else ps.setNull(4, Types.INTEGER);
            ps.setTimestamp(5, DateTimeUtil.toTimestamp(s.getStartTime()));
            ps.executeUpdate();
            try (ResultSet k = ps.getGeneratedKeys()) {
                return k.next() ? k.getInt(1) : -1;
            }
        }
    }

    @Override
    public boolean completeSession(Connection con, int sessionId, LocalDateTime end, double energyKwh, double cost) throws SQLException {
        String sql = "UPDATE charging_sessions SET end_time = ?, energy_kwh = ?, cost = ?, status = 'COMPLETED' WHERE session_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setTimestamp(1, DateTimeUtil.toTimestamp(end));
            ps.setDouble(2, energyKwh);
            ps.setDouble(3, cost);
            ps.setInt(4, sessionId);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public ChargingSession findById(int id) throws SQLException {
        List<ChargingSession> l = query("SELECT * FROM charging_sessions WHERE session_id = ?", id);
        return l.isEmpty() ? null : l.get(0);
    }

    @Override
    public List<ChargingSession> findAll() throws SQLException {
        return query("SELECT * FROM charging_sessions ORDER BY start_time DESC", null);
    }

    @Override
    public List<ChargingSession> findByUser(int userId) throws SQLException {
        return query("SELECT * FROM charging_sessions WHERE user_id = ? ORDER BY start_time DESC", userId);
    }

    @Override
    public List<ChargingSession> findActiveByUser(int userId) throws SQLException {
        return query("SELECT * FROM charging_sessions WHERE user_id = ? AND status = 'ACTIVE' ORDER BY start_time", userId);
    }

    @Override
    public List<ChargingSession> findByStation(int stationId) throws SQLException {
        return query("SELECT s.* FROM charging_sessions s JOIN chargers c ON c.charger_id = s.charger_id "
                + "WHERE c.station_id = ? ORDER BY s.start_time DESC", stationId);
    }
}
