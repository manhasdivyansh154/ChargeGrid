package chargegrid.model;

public class Customer extends User {
    public Customer(int id, String username, String hash, String fullName, String email, String phone) {
        super(id, username, hash, fullName, email, phone);
    }
    @Override public UserRole getRole() { return UserRole.CUSTOMER; }
}
