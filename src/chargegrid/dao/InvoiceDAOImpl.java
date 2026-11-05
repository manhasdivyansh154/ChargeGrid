package chargegrid.dao;

import chargegrid.model.Invoice;
import chargegrid.model.Payment;
import chargegrid.model.PaymentMode;
import chargegrid.model.PaymentStatus;
import chargegrid.util.DBConnection;
import chargegrid.util.DateTimeUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InvoiceDAOImpl implements InvoiceDAO {

    private Invoice map(ResultSet rs) throws SQLException {
        return new Invoice(rs.getInt("invoice_id"), rs.getInt("session_id"), rs.getInt("user_id"),
                rs.getDouble("amount"), rs.getDouble("tax"), rs.getDouble("total"),
                PaymentStatus.valueOf(rs.getString("status")), DateTimeUtil.fromTimestamp(rs.getTimestamp("issued_at")));
    }

    private List<Invoice> query(String sql, int param) throws SQLException {
        List<Invoice> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    @Override
    public int createInvoice(Connection con, Invoice inv) throws SQLException {
        String sql = "INSERT INTO invoices (session_id, user_id, amount, tax, total, status, issued_at) VALUES (?,?,?,?,?,?,?)";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, inv.getSessionId());
            ps.setInt(2, inv.getUserId());
            ps.setDouble(3, inv.getAmount());
            ps.setDouble(4, inv.getTax());
            ps.setDouble(5, inv.getTotal());
            ps.setString(6, inv.getStatus().name());
            ps.setTimestamp(7, DateTimeUtil.toTimestamp(inv.getIssuedAt()));
            ps.executeUpdate();
            try (ResultSet k = ps.getGeneratedKeys()) {
                return k.next() ? k.getInt(1) : -1;
            }
        }
    }

    @Override
    public boolean markPaid(Connection con, int invoiceId) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("UPDATE invoices SET status = 'PAID' WHERE invoice_id = ? AND status = 'UNPAID'")) {
            ps.setInt(1, invoiceId);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public int addPayment(Connection con, Payment p) throws SQLException {
        String sql = "INSERT INTO payments (invoice_id, user_id, amount, mode, paid_at) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, p.getInvoiceId());
            ps.setInt(2, p.getUserId());
            ps.setDouble(3, p.getAmount());
            ps.setString(4, p.getMode().name());
            ps.setTimestamp(5, DateTimeUtil.toTimestamp(p.getPaidAt()));
            ps.executeUpdate();
            try (ResultSet k = ps.getGeneratedKeys()) {
                return k.next() ? k.getInt(1) : -1;
            }
        }
    }

    @Override
    public Invoice findById(int id) throws SQLException {
        List<Invoice> l = query("SELECT * FROM invoices WHERE invoice_id = ?", id);
        return l.isEmpty() ? null : l.get(0);
    }

    @Override
    public List<Invoice> findByUser(int userId) throws SQLException {
        return query("SELECT * FROM invoices WHERE user_id = ? ORDER BY issued_at DESC", userId);
    }

    @Override
    public List<Invoice> findUnpaidByUser(int userId) throws SQLException {
        return query("SELECT * FROM invoices WHERE user_id = ? AND status = 'UNPAID' ORDER BY issued_at", userId);
    }

    @Override
    public List<Payment> findPaymentsByUser(int userId) throws SQLException {
        List<Payment> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT * FROM payments WHERE user_id = ? ORDER BY paid_at DESC")) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Payment(rs.getInt("payment_id"), rs.getInt("invoice_id"), rs.getInt("user_id"),
                            rs.getDouble("amount"), PaymentMode.valueOf(rs.getString("mode")),
                            DateTimeUtil.fromTimestamp(rs.getTimestamp("paid_at"))));
                }
            }
        }
        return list;
    }
}
