package chargegrid.dao;

import chargegrid.model.Station;
import chargegrid.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StationDAOImpl implements StationDAO {

    private Station map(ResultSet rs) throws SQLException {
        return new Station(rs.getInt("station_id"), rs.getString("name"), rs.getString("city"), rs.getString("address"));
    }

    @Override
    public int addStation(Station s) throws SQLException {
        String sql = "INSERT INTO stations (name, city, address) VALUES (?,?,?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, s.getName());
            ps.setString(2, s.getCity());
            ps.setString(3, s.getAddress());
            ps.executeUpdate();
            try (ResultSet k = ps.getGeneratedKeys()) {
                return k.next() ? k.getInt(1) : -1;
            }
        }
    }

    @Override
    public boolean updateStation(Station s) throws SQLException {
        String sql = "UPDATE stations SET name = ?, city = ?, address = ? WHERE station_id = ?";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, s.getName());
            ps.setString(2, s.getCity());
            ps.setString(3, s.getAddress());
            ps.setInt(4, s.getId());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean deleteStation(int id) throws SQLException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM stations WHERE station_id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public Station findById(int id) throws SQLException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT * FROM stations WHERE station_id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    @Override
    public List<Station> findAll() throws SQLException {
        List<Station> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM stations ORDER BY station_id")) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    @Override
    public List<Station> findByCity(String city) throws SQLException {
        List<Station> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT * FROM stations WHERE city LIKE ? ORDER BY station_id")) {
            ps.setString(1, "%" + city + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }
}
