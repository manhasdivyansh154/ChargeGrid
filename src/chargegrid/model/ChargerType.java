package chargegrid.model;

public enum ChargerType {
    AC("AC Slow", 7.4, 12.0),
    DC_FAST("DC Fast", 50.0, 18.0),
    ULTRA_FAST("Ultra Fast", 150.0, 24.0);

    private final String label;
    private final double defaultPowerKw;
    private final double defaultRate;

    ChargerType(String label, double defaultPowerKw, double defaultRate) {
        this.label = label;
        this.defaultPowerKw = defaultPowerKw;
        this.defaultRate = defaultRate;
    }
    public String getLabel() { return label; }
    public double getDefaultPowerKw() { return defaultPowerKw; }
    public double getDefaultRate() { return defaultRate; }
}
