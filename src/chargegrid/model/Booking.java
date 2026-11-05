package chargegrid.model;

import java.time.LocalDateTime;

public class Booking {
    private final int id;
    private final int userId;
    private final int vehicleId;
    private final int chargerId;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private BookingStatus status;

    public Booking(int id, int userId, int vehicleId, int chargerId,
                   LocalDateTime startTime, LocalDateTime endTime, BookingStatus status) {
        this.id = id;
        this.userId = userId;
        this.vehicleId = vehicleId;
        this.chargerId = chargerId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
    }
    public int getId() { return id; }
    public int getUserId() { return userId; }
    public int getVehicleId() { return vehicleId; }
    public int getChargerId() { return chargerId; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public BookingStatus getStatus() { return status; }
    public void setStatus(BookingStatus status) { this.status = status; }
}
