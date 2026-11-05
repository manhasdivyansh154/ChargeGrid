package chargegrid.model;

public class StationManager extends User {
    private final int stationId;

    public StationManager(int id, String username, String hash, String fullName, String email, String phone, int stationId) {
        super(id, username, hash, fullName, email, phone);
        this.stationId = stationId;
    }
    public int getStationId() { return stationId; }
    @Override public UserRole getRole() { return UserRole.MANAGER; }
}
