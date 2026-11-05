package chargegrid.dao;

import chargegrid.model.StationManager;
import chargegrid.model.User;
import chargegrid.model.UserRole;
import chargegrid.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAOImpl implements UserDAO {

    private User map(ResultSet rs) throws SQLException {
        return User.create(UserRole.valueOf(rs.getString("role")), rs.getInt("user_id"),
                rs.getString("username"), rs.getString("password_hash"), rs.getString("full_name"),
                rs.getString("email"), rs.getString("phone"), rs.getInt("station_id"));
    }

    @Override
    public int addUser(User u) throws SQLException {
        String sql = "INSERT INTO users (username, password_hash, full_name, email, phone, role, station_id) VALUES (?,?,?,?,?,?,?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, u.getUsername());
            ps.setString(2, u.getPasswordHash());
            ps.setString(3, u.getFullName());
            ps.setString(4, u.getEmail());
            ps.setString(5, u.getPhone());
            ps.setString(6, u.getRole().name());
            if (u instanceof StationManager && ((StationManager) u).getStationId() > 0) {
                ps.setInt(7, ((StationManager) u).getStationId());
            } else {
                ps.setNull(7, Types.INTEGER);
            }
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : -1;
            }
        }
    }

    @Override
    public User findByUsername(String username) throws SQLException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT * FROM users WHERE username = ?")) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    @Override
    public User findById(int id) throws SQLException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT * FROM users WHERE user_id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    @Override
    public List<User> findAll() throws SQLException {
        List<User> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM users ORDER BY user_id")) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    @Override
    public List<User> findByRole(UserRole role) throws SQLException {
        List<User> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT * FROM users WHERE role = ? ORDER BY user_id")) {
            ps.setString(1, role.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    @Override
    public boolean usernameExists(String username) throws SQLException {
        return findByUsername(username) != null;
    }

    @Override
    public boolean updatePassword(int userId, String newHash) throws SQLException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("UPDATE users SET password_hash = ? WHERE user_id = ?")) {
            ps.setString(1, newHash);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        }
    }
}
