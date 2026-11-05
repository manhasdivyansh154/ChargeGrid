package chargegrid.dao;

import chargegrid.model.Wallet;
import chargegrid.util.DBConnection;

import java.sql.*;

public class WalletDAOImpl implements WalletDAO {

    @Override
    public void createWallet(int userId) throws SQLException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("INSERT INTO wallets (user_id, balance) VALUES (?, 0)")) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        }
    }

    @Override
    public Wallet findByUser(int userId) throws SQLException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT * FROM wallets WHERE user_id = ?")) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? new Wallet(rs.getInt("wallet_id"), rs.getInt("user_id"), rs.getDouble("balance")) : null;
            }
        }
    }

    @Override
    public boolean topUp(int userId, double amount) throws SQLException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("UPDATE wallets SET balance = balance + ? WHERE user_id = ?")) {
            ps.setDouble(1, amount);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean deduct(Connection con, int userId, double amount) throws SQLException {
        String sql = "UPDATE wallets SET balance = balance - ? WHERE user_id = ? AND balance >= ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDouble(1, amount);
            ps.setInt(2, userId);
            ps.setDouble(3, amount);
            return ps.executeUpdate() == 1;
        }
    }
}
