package chargegrid.model;

import java.time.LocalDateTime;

public class ChargingSession {
    private final int id;
    private final int userId;
    private final int vehicleId;
    private final int chargerId;
    private final int bookingId;          // 0 = walk-in (no booking)
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;  // null while ACTIVE
    private final double energyKwh;
    private final double cost;
    private final SessionStatus status;

    public ChargingSession(int id, int userId, int vehicleId, int chargerId, int bookingId,
                           LocalDateTime startTime, LocalDateTime endTime,
                           double energyKwh, double cost, SessionStatus status) {
        this.id = id;
        this.userId = userId;
        this.vehicleId = vehicleId;
        this.chargerId = chargerId;
        this.bookingId = bookingId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.energyKwh = energyKwh;
        this.cost = cost;
        this.status = status;
    }
    public int getId() { return id; }
    public int getUserId() { return userId; }
    public int getVehicleId() { return vehicleId; }
    public int getChargerId() { return chargerId; }
    public int getBookingId() { return bookingId; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public double getEnergyKwh() { return energyKwh; }
    public double getCost() { return cost; }
    public SessionStatus getStatus() { return status; }
}
