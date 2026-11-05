package chargegrid.model;

public class ACCharger extends Charger {
    public ACCharger(int id, int stationId, double powerKw, double rate, ChargerStatus status) {
        super(id, stationId, powerKw, rate, status);
    }
    @Override public ChargerType getType() { return ChargerType.AC; }
    @Override public double getPeakMultiplier() { return 1.0; }
}
