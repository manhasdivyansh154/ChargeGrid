package chargegrid.dao;

import chargegrid.model.Charger;
import chargegrid.model.ChargerStatus;
import chargegrid.model.ChargerType;
import chargegrid.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ChargerDAOImpl implements ChargerDAO {

    private Charger map(ResultSet rs) throws SQLException {
        return Charger.create(ChargerType.valueOf(rs.getString("charger_type")), rs.getInt("charger_id"),
                rs.getInt("station_id"), rs.getDouble("power_kw"), rs.getDouble("rate_per_kwh"),
                ChargerStatus.valueOf(rs.getString("status")));
    }

    @Override
    public int addCharger(Charger c) throws SQLException {
        String sql = "INSERT INTO chargers (station_id, charger_type, power_kw, rate_per_kwh, status) VALUES (?,?,?,?,?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, c.getStationId());
            ps.setString(2, c.getType().name());
            ps.setDouble(3, c.getPowerKw());
            ps.setDouble(4, c.getRatePerKwh());
            ps.setString(5, c.getStatus().name());
            ps.executeUpdate();
            try (ResultSet k = ps.getGeneratedKeys()) {
                return k.next() ? k.getInt(1) : -1;
            }
        }
    }

    @Override
    public Charger findById(int id) throws SQLException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT * FROM chargers WHERE charger_id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    @Override
    public List<Charger> findAll() throws SQLException {
        List<Charger> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM chargers ORDER BY station_id, charger_id")) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    @Override
    public List<Charger> findByStation(int stationId) throws SQLException {
        List<Charger> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT * FROM chargers WHERE station_id = ? ORDER BY charger_id")) {
            ps.setInt(1, stationId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    @Override
    public boolean updateStatus(int id, ChargerStatus status) throws SQLException {
        try (Connection con = DBConnection.getConnection()) {
            return updateStatus(con, id, status);
        }
    }

    @Override
    public boolean updateStatus(Connection con, int id, ChargerStatus status) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("UPDATE chargers SET status = ? WHERE charger_id = ?")) {
            ps.setString(1, status.name());
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean updateRate(int id, double rate) throws SQLException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("UPDATE chargers SET rate_per_kwh = ? WHERE charger_id = ?")) {
            ps.setDouble(1, rate);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean deleteCharger(int id) throws SQLException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM chargers WHERE charger_id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }
}
