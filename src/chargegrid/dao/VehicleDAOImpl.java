package chargegrid.dao;

import chargegrid.model.Vehicle;
import chargegrid.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VehicleDAOImpl implements VehicleDAO {

    private Vehicle map(ResultSet rs) throws SQLException {
        return new Vehicle(rs.getInt("vehicle_id"), rs.getInt("owner_id"), rs.getString("reg_no"),
                rs.getString("make"), rs.getString("model"), rs.getDouble("battery_kwh"), rs.getDouble("max_charge_kw"));
    }

    @Override
    public int addVehicle(Vehicle v) throws SQLException {
        String sql = "INSERT INTO vehicles (owner_id, reg_no, make, model, battery_kwh, max_charge_kw) VALUES (?,?,?,?,?,?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, v.getOwnerId());
            ps.setString(2, v.getRegNo());
            ps.setString(3, v.getMake());
            ps.setString(4, v.getModel());
            ps.setDouble(5, v.getBatteryKwh());
            ps.setDouble(6, v.getMaxChargeKw());
            ps.executeUpdate();
            try (ResultSet k = ps.getGeneratedKeys()) {
                return k.next() ? k.getInt(1) : -1;
            }
        }
    }

    @Override
    public Vehicle findById(int id) throws SQLException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT * FROM vehicles WHERE vehicle_id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    @Override
    public List<Vehicle> findByOwner(int ownerId) throws SQLException {
        List<Vehicle> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT * FROM vehicles WHERE owner_id = ? ORDER BY vehicle_id")) {
            ps.setInt(1, ownerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    @Override
    public boolean regNoExists(String regNo) throws SQLException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT 1 FROM vehicles WHERE reg_no = ?")) {
            ps.setString(1, regNo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    @Override
    public boolean deleteVehicle(int id, int ownerId) throws SQLException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM vehicles WHERE vehicle_id = ? AND owner_id = ?")) {
            ps.setInt(1, id);
            ps.setInt(2, ownerId);
            return ps.executeUpdate() > 0;
        }
    }
}
