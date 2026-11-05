package chargegrid.dao;

import chargegrid.model.Invoice;
import chargegrid.model.Payment;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface InvoiceDAO {
    int createInvoice(Connection con, Invoice inv) throws SQLException;
    boolean markPaid(Connection con, int invoiceId) throws SQLException;
    int addPayment(Connection con, Payment p) throws SQLException;
    Invoice findById(int id) throws SQLException;
    List<Invoice> findByUser(int userId) throws SQLException;
    List<Invoice> findUnpaidByUser(int userId) throws SQLException;
    List<Payment> findPaymentsByUser(int userId) throws SQLException;
}
