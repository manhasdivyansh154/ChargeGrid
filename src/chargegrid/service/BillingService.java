package chargegrid.service;

import chargegrid.dao.InvoiceDAO;
import chargegrid.dao.WalletDAO;
import chargegrid.exception.ChargeGridException;
import chargegrid.exception.InsufficientBalanceException;
import chargegrid.exception.InvalidInputException;
import chargegrid.model.*;
import chargegrid.util.DBConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class BillingService {
    public static final double TAX_RATE = 0.05;   // 5% GST

    private final WalletDAO walletDAO;
    private final InvoiceDAO invoiceDAO;

    public BillingService(WalletDAO walletDAO, InvoiceDAO invoiceDAO) {
        this.walletDAO = walletDAO;
        this.invoiceDAO = invoiceDAO;
    }

    private static double round2(double v) { return Math.round(v * 100.0) / 100.0; }

    /** Creates an UNPAID invoice inside the caller's transaction. */
    public Invoice createInvoice(Connection con, ChargingSession s, double cost) throws SQLException {
        double tax = round2(cost * TAX_RATE);
        double total = round2(cost + tax);
        Invoice draft = new Invoice(0, s.getId(), s.getUserId(), cost, tax, total, PaymentStatus.UNPAID, LocalDateTime.now());
        int id = invoiceDAO.createInvoice(con, draft);
        return new Invoice(id, s.getId(), s.getUserId(), cost, tax, total, PaymentStatus.UNPAID, draft.getIssuedAt());
    }

    /** Pays from the wallet when the balance is enough. Returns false (and does nothing) otherwise. */
    public boolean tryAutoPay(Connection con, Invoice inv) throws SQLException {
        if (!walletDAO.deduct(con, inv.getUserId(), inv.getTotal())) return false;
        invoiceDAO.markPaid(con, inv.getId());
        invoiceDAO.addPayment(con, new Payment(0, inv.getId(), inv.getUserId(), inv.getTotal(), PaymentMode.WALLET, LocalDateTime.now()));
        return true;
    }

    public Payment payInvoice(int userId, int invoiceId, PaymentMode mode) throws SQLException, ChargeGridException {
        Invoice inv = invoiceDAO.findById(invoiceId);
        if (inv == null || inv.getUserId() != userId) throw new ChargeGridException("Invoice " + invoiceId + " not found.");
        if (inv.getStatus() == PaymentStatus.PAID) throw new ChargeGridException("Invoice " + invoiceId + " is already paid.");

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                if (mode == PaymentMode.WALLET && !walletDAO.deduct(con, userId, inv.getTotal())) {
                    throw new InsufficientBalanceException("Wallet balance is too low. Top up or choose another payment mode.");
                }
                invoiceDAO.markPaid(con, invoiceId);
                Payment p = new Payment(0, invoiceId, userId, inv.getTotal(), mode, LocalDateTime.now());
                int id = invoiceDAO.addPayment(con, p);
                con.commit();
                return new Payment(id, invoiceId, userId, inv.getTotal(), mode, p.getPaidAt());
            } catch (SQLException | ChargeGridException | RuntimeException e) {
                con.rollback();
                throw e;
            }
        }
    }

    public double getBalance(int userId) throws SQLException, ChargeGridException {
        Wallet w = walletDAO.findByUser(userId);
        if (w == null) throw new ChargeGridException("Wallet not found.");
        return w.getBalance();
    }

    public double topUp(int userId, double amount) throws SQLException, ChargeGridException {
        if (amount < 10 || amount > 50000) throw new InvalidInputException("Top-up must be between Rs.10 and Rs.50,000.");
        walletDAO.topUp(userId, round2(amount));
        return getBalance(userId);
    }

    public Invoice getInvoice(int id) throws SQLException {
        return invoiceDAO.findById(id);
    }

    public List<Invoice> listInvoices(int userId) throws SQLException {
        return invoiceDAO.findByUser(userId);
    }

    public List<Invoice> listUnpaid(int userId) throws SQLException {
        return invoiceDAO.findUnpaidByUser(userId);
    }

    public List<Payment> listPayments(int userId) throws SQLException {
        return invoiceDAO.findPaymentsByUser(userId);
    }
}
