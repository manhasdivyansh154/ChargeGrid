package chargegrid.model;

import java.time.LocalDateTime;

public class Invoice {
    private final int id;
    private final int sessionId;
    private final int userId;
    private final double amount;
    private final double tax;
    private final double total;
    private final PaymentStatus status;
    private final LocalDateTime issuedAt;

    public Invoice(int id, int sessionId, int userId, double amount, double tax, double total,
                   PaymentStatus status, LocalDateTime issuedAt) {
        this.id = id;
        this.sessionId = sessionId;
        this.userId = userId;
        this.amount = amount;
        this.tax = tax;
        this.total = total;
        this.status = status;
        this.issuedAt = issuedAt;
    }
    public int getId() { return id; }
    public int getSessionId() { return sessionId; }
    public int getUserId() { return userId; }
    public double getAmount() { return amount; }
    public double getTax() { return tax; }
    public double getTotal() { return total; }
    public PaymentStatus getStatus() { return status; }
    public LocalDateTime getIssuedAt() { return issuedAt; }
}
