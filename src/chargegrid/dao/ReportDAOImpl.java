package chargegrid.dao;

import chargegrid.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReportDAOImpl implements ReportDAO {

    /** Runs a query with an optional single station parameter and returns every column as text. */
    private List<String[]> run(String sql, Integer stationId) throws SQLException {
        List<String[]> rows = new ArrayList<>();
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            if (stationId != null) ps.setInt(1, stationId);
            try (ResultSet rs = ps.executeQuery()) {
                int n = rs.getMetaData().getColumnCount();
                while (rs.next()) {
                    String[] row = new String[n];
                    for (int i = 0; i < n; i++) row[i] = rs.getString(i + 1);
                    rows.add(row);
                }
            }
        }
        return rows;
    }

    private static String money(String v) { return v == null ? "0.00" : String.format("%.2f", Double.parseDouble(v)); }

    @Override
    public List<String[]> systemSummary() throws SQLException {
        String sql = "SELECT (SELECT COUNT(*) FROM stations), (SELECT COUNT(*) FROM chargers), "
                + "(SELECT COUNT(*) FROM users WHERE role='CUSTOMER'), (SELECT COUNT(*) FROM charging_sessions WHERE status='ACTIVE'), "
                + "(SELECT COALESCE(SUM(total),0) FROM invoices WHERE status='PAID'), "
                + "(SELECT COALESCE(SUM(total),0) FROM invoices WHERE status='UNPAID'), "
                + "(SELECT COUNT(*) FROM maintenance_tickets WHERE status <> 'RESOLVED')";
        String[] labels = {"Stations", "Chargers", "Customers", "Active sessions", "Revenue collected (Rs.)",
                "Revenue pending (Rs.)", "Open maintenance tickets"};
        List<String[]> one = run(sql, null);
        List<String[]> rows = new ArrayList<>();
        if (!one.isEmpty()) {
            for (int i = 0; i < labels.length; i++) {
                String v = one.get(0)[i];
                rows.add(new String[]{labels[i], (i == 4 || i == 5) ? money(v) : v});
            }
        }
        return rows;
    }

    @Override
    public List<String[]> revenueByStation(Integer stationId) throws SQLException {
        String sql = "SELECT st.name, COUNT(cs.session_id), COALESCE(SUM(cs.energy_kwh),0), "
                + "COALESCE(SUM(i.total),0), COALESCE(SUM(CASE WHEN i.status='PAID' THEN i.total ELSE 0 END),0) "
                + "FROM stations st LEFT JOIN chargers c ON c.station_id = st.station_id "
                + "LEFT JOIN charging_sessions cs ON cs.charger_id = c.charger_id AND cs.status = 'COMPLETED' "
                + "LEFT JOIN invoices i ON i.session_id = cs.session_id "
                + (stationId == null ? "" : "WHERE st.station_id = ? ")
                + "GROUP BY st.station_id, st.name ORDER BY 4 DESC";
        List<String[]> rows = run(sql, stationId);
        for (String[] r : rows) {
            r[2] = money(r[2]);
            r[3] = money(r[3]);
            r[4] = money(r[4]);
        }
        return rows;
    }

    @Override
    public List<String[]> chargerUsage(Integer stationId) throws SQLException {
        String sql = "SELECT c.charger_id, st.name, c.charger_type, COUNT(cs.session_id), COALESCE(SUM(cs.energy_kwh),0) "
                + "FROM chargers c JOIN stations st ON st.station_id = c.station_id "
                + "LEFT JOIN charging_sessions cs ON cs.charger_id = c.charger_id AND cs.status = 'COMPLETED' "
                + (stationId == null ? "" : "WHERE c.station_id = ? ")
                + "GROUP BY c.charger_id, st.name, c.charger_type ORDER BY 4 DESC, c.charger_id";
        List<String[]> rows = run(sql, stationId);
        for (String[] r : rows) r[4] = money(r[4]);
        return rows;
    }

    @Override
    public List<String[]> peakHours(Integer stationId) throws SQLException {
        String sql = "SELECT HOUR(cs.start_time), COUNT(*), COALESCE(SUM(cs.energy_kwh),0) "
                + "FROM charging_sessions cs JOIN chargers c ON c.charger_id = cs.charger_id "
                + "WHERE cs.status = 'COMPLETED' " + (stationId == null ? "" : "AND c.station_id = ? ")
                + "GROUP BY HOUR(cs.start_time) ORDER BY 2 DESC, 1";
        List<String[]> rows = run(sql, stationId);
        for (String[] r : rows) {
            int h = Integer.parseInt(r[0]);
            r[0] = String.format("%02d:00 - %02d:59", h, h);
            r[2] = money(r[2]);
        }
        return rows;
    }

    @Override
    public List<String[]> topCustomers(Integer stationId) throws SQLException {
        String sql = "SELECT u.full_name, COUNT(cs.session_id), COALESCE(SUM(cs.energy_kwh),0), COALESCE(SUM(cs.cost),0) "
                + "FROM charging_sessions cs JOIN users u ON u.user_id = cs.user_id "
                + "JOIN chargers c ON c.charger_id = cs.charger_id "
                + "WHERE cs.status = 'COMPLETED' " + (stationId == null ? "" : "AND c.station_id = ? ")
                + "GROUP BY u.user_id, u.full_name ORDER BY 4 DESC LIMIT 5";
        List<String[]> rows = run(sql, stationId);
        for (String[] r : rows) {
            r[2] = money(r[2]);
            r[3] = money(r[3]);
        }
        return rows;
    }
}
