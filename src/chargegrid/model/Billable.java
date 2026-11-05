package chargegrid.model;

import java.time.LocalDateTime;

/** Anything that can price an amount of energy at a given moment. */
public interface Billable {
    double calculateCost(double energyKwh, LocalDateTime when);
}
