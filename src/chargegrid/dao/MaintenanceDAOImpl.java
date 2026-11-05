package chargegrid.dao;

import chargegrid.model.MaintenanceTicket;
import chargegrid.model.Technician;
import chargegrid.model.TicketStatus;
import chargegrid.util.DBConnection;
import chargegrid.util.DateTimeUtil;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MaintenanceDAOImpl implements MaintenanceDAO {

    private MaintenanceTicket map(ResultSet rs) throws SQLException {
        return new MaintenanceTicket(rs.getInt("ticket_id"), rs.getInt("charger_id"), rs.getInt("reported_by"),
                rs.getString("description"), TicketStatus.valueOf(rs.getString("status")), rs.getInt("technician_id"),
                DateTimeUtil.fromTimestamp(rs.getTimestamp("created_at")),
                DateTimeUtil.fromTimestamp(rs.getTimestamp("resolved_at")));
    }

    private List<MaintenanceTicket> query(String sql, Integer param) throws SQLException {
        List<MaintenanceTicket> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            if (param != null) ps.setInt(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    @Override
    public int addTicket(MaintenanceTicket t) throws SQLException {
        String sql = "INSERT INTO maintenance_tickets (charger_id, reported_by, description, status, created_at) VALUES (?,?,?,'OPEN',?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, t.getChargerId());
            ps.setInt(2, t.getReportedBy());
            ps.setString(3, t.getDescription());
            ps.setTimestamp(4, DateTimeUtil.toTimestamp(t.getCreatedAt()));
            ps.executeUpdate();
            try (ResultSet k = ps.getGeneratedKeys()) {
                return k.next() ? k.getInt(1) : -1;
            }
        }
    }

    @Override
    public MaintenanceTicket findTicket(int id) throws SQLException {
        List<MaintenanceTicket> l = query("SELECT * FROM maintenance_tickets WHERE ticket_id = ?", id);
        return l.isEmpty() ? null : l.get(0);
    }

    @Override
    public List<MaintenanceTicket> findAllTickets() throws SQLException {
        return query("SELECT * FROM maintenance_tickets ORDER BY created_at DESC", null);
    }

    @Override
    public List<MaintenanceTicket> findTicketsByStation(int stationId) throws SQLException {
        return query("SELECT t.* FROM maintenance_tickets t JOIN chargers c ON c.charger_id = t.charger_id "
                + "WHERE c.station_id = ? ORDER BY t.created_at DESC", stationId);
    }

    @Override
    public boolean assignTechnician(int ticketId, int technicianId) throws SQLException {
        String sql = "UPDATE maintenance_tickets SET technician_id = ?, status = 'IN_PROGRESS' WHERE ticket_id = ? AND status <> 'RESOLVED'";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, technicianId);
            ps.setInt(2, ticketId);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean resolveTicket(Connection con, int ticketId) throws SQLException {
        String sql = "UPDATE maintenance_tickets SET status = 'RESOLVED', resolved_at = ? WHERE ticket_id = ? AND status <> 'RESOLVED'";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setTimestamp(1, DateTimeUtil.toTimestamp(LocalDateTime.now()));
            ps.setInt(2, ticketId);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public int addTechnician(Technician t) throws SQLException {
        String sql = "INSERT INTO technicians (name, phone, speciality) VALUES (?,?,?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, t.getName());
            ps.setString(2, t.getPhone());
            ps.setString(3, t.getSpeciality());
            ps.executeUpdate();
            try (ResultSet k = ps.getGeneratedKeys()) {
                return k.next() ? k.getInt(1) : -1;
            }
        }
    }

    private Technician mapTech(ResultSet rs) throws SQLException {
        return new Technician(rs.getInt("technician_id"), rs.getString("name"), rs.getString("phone"), rs.getString("speciality"));
    }

    @Override
    public Technician findTechnician(int id) throws SQLException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT * FROM technicians WHERE technician_id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapTech(rs) : null;
            }
        }
    }

    @Override
    public List<Technician> findAllTechnicians() throws SQLException {
        List<Technician> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM technicians ORDER BY technician_id")) {
            while (rs.next()) list.add(mapTech(rs));
        }
        return list;
    }
}
