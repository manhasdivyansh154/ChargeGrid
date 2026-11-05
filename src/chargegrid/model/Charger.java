package chargegrid.model;

import java.time.LocalDateTime;

/** Abstract charger. Each concrete type decides its own peak-hour surcharge (polymorphism). */
public abstract class Charger implements Billable {
    private final int id;
    private final int stationId;
    private double powerKw;
    private double ratePerKwh;
    private ChargerStatus status;

    protected Charger(int id, int stationId, double powerKw, double ratePerKwh, ChargerStatus status) {
        this.id = id;
        this.stationId = stationId;
        this.powerKw = powerKw;
        this.ratePerKwh = ratePerKwh;
        this.status = status;
    }

    public abstract ChargerType getType();
    /** Multiplier applied to the rate between 18:00 and 22:00. */
    public abstract double getPeakMultiplier();

    public boolean isPeak(LocalDateTime t) {
        int h = t.getHour();
        return h >= 18 && h < 22;
    }

    @Override
    public double calculateCost(double energyKwh, LocalDateTime when) {
        double multiplier = isPeak(when) ? getPeakMultiplier() : 1.0;
        return Math.round(energyKwh * ratePerKwh * multiplier * 100.0) / 100.0;
    }

    public int getId() { return id; }
    public int getStationId() { return stationId; }
    public double getPowerKw() { return powerKw; }
    public double getRatePerKwh() { return ratePerKwh; }
    public ChargerStatus getStatus() { return status; }
    public void setPowerKw(double powerKw) { this.powerKw = powerKw; }
    public void setRatePerKwh(double r) { this.ratePerKwh = r; }
    public void setStatus(ChargerStatus s) { this.status = s; }

    public static Charger create(ChargerType type, int id, int stationId, double powerKw,
                                 double rate, ChargerStatus status) {
        switch (type) {
            case AC:
                return new ACCharger(id, stationId, powerKw, rate, status);
            case DC_FAST:
                return new DCFastCharger(id, stationId, powerKw, rate, status);
            default:
                return new UltraFastCharger(id, stationId, powerKw, rate, status);
        }
    }
}
