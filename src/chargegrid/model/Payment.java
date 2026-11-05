package chargegrid.model;

import java.time.LocalDateTime;

public class Payment {
    private final int id;
    private final int invoiceId;
    private final int userId;
    private final double amount;
    private final PaymentMode mode;
    private final LocalDateTime paidAt;

    public Payment(int id, int invoiceId, int userId, double amount, PaymentMode mode, LocalDateTime paidAt) {
        this.id = id;
        this.invoiceId = invoiceId;
        this.userId = userId;
        this.amount = amount;
        this.mode = mode;
        this.paidAt = paidAt;
    }
    public int getId() { return id; }
    public int getInvoiceId() { return invoiceId; }
    public int getUserId() { return userId; }
    public double getAmount() { return amount; }
    public PaymentMode getMode() { return mode; }
    public LocalDateTime getPaidAt() { return paidAt; }
}
