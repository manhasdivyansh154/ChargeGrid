package chargegrid.model;

public class Vehicle {
    private final int id;
    private final int ownerId;
    private final String regNo;
    private final String make;
    private final String model;
    private final double batteryKwh;
    private final double maxChargeKw;

    public Vehicle(int id, int ownerId, String regNo, String make, String model, double batteryKwh, double maxChargeKw) {
        this.id = id;
        this.ownerId = ownerId;
        this.regNo = regNo;
        this.make = make;
        this.model = model;
        this.batteryKwh = batteryKwh;
        this.maxChargeKw = maxChargeKw;
    }
    public int getId() { return id; }
    public int getOwnerId() { return ownerId; }
    public String getRegNo() { return regNo; }
    public String getMake() { return make; }
    public String getModel() { return model; }
    public double getBatteryKwh() { return batteryKwh; }
    public double getMaxChargeKw() { return maxChargeKw; }
}
