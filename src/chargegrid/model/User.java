package chargegrid.model;

public abstract class User {
    private int id;
    private final String username;
    private String passwordHash;
    private final String fullName;
    private final String email;
    private final String phone;

    protected User(int id, String username, String passwordHash, String fullName, String email, String phone) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
    }

    public abstract UserRole getRole();

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String h) { this.passwordHash = h; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }

    /** Factory used by the DAO layer to build the right subclass for a role. */
    public static User create(UserRole role, int id, String username, String hash,
                              String fullName, String email, String phone, int stationId) {
        switch (role) {
            case ADMIN:
                return new Admin(id, username, hash, fullName, email, phone);
            case MANAGER:
                return new StationManager(id, username, hash, fullName, email, phone, stationId);
            default:
                return new Customer(id, username, hash, fullName, email, phone);
        }
    }
}
