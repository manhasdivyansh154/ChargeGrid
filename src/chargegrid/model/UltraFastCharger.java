package chargegrid.model;

public class UltraFastCharger extends Charger {
    public UltraFastCharger(int id, int stationId, double powerKw, double rate, ChargerStatus status) {
        super(id, stationId, powerKw, rate, status);
    }
    @Override public ChargerType getType() { return ChargerType.ULTRA_FAST; }
    @Override public double getPeakMultiplier() { return 1.50; }
}
