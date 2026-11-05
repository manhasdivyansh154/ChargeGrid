package chargegrid.model;

public class Wallet {
    private final int id;
    private final int userId;
    private final double balance;

    public Wallet(int id, int userId, double balance) {
        this.id = id;
        this.userId = userId;
        this.balance = balance;
    }
    public int getId() { return id; }
    public int getUserId() { return userId; }
    public double getBalance() { return balance; }
}
