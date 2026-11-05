package chargegrid.model;

public class DCFastCharger extends Charger {
    public DCFastCharger(int id, int stationId, double powerKw, double rate, ChargerStatus status) {
        super(id, stationId, powerKw, rate, status);
    }
    @Override public ChargerType getType() { return ChargerType.DC_FAST; }
    @Override public double getPeakMultiplier() { return 1.20; }
}
