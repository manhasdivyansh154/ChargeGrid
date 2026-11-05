package chargegrid.model;

import java.time.LocalDateTime;

public class MaintenanceTicket {
    private final int id;
    private final int chargerId;
    private final int reportedBy;
    private final String description;
    private final TicketStatus status;
    private final int technicianId;       // 0 = not assigned
    private final LocalDateTime createdAt;
    private final LocalDateTime resolvedAt;

    public MaintenanceTicket(int id, int chargerId, int reportedBy, String description, TicketStatus status,
                             int technicianId, LocalDateTime createdAt, LocalDateTime resolvedAt) {
        this.id = id;
        this.chargerId = chargerId;
        this.reportedBy = reportedBy;
        this.description = description;
        this.status = status;
        this.technicianId = technicianId;
        this.createdAt = createdAt;
        this.resolvedAt = resolvedAt;
    }
    public int getId() { return id; }
    public int getChargerId() { return chargerId; }
    public int getReportedBy() { return reportedBy; }
    public String getDescription() { return description; }
    public TicketStatus getStatus() { return status; }
    public int getTechnicianId() { return technicianId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getResolvedAt() { return resolvedAt; }
}
