package chargegrid.service;

import chargegrid.dao.VehicleDAO;
import chargegrid.exception.ChargeGridException;
import chargegrid.exception.InvalidInputException;
import chargegrid.model.Vehicle;
import chargegrid.util.Validator;

import java.sql.SQLException;
import java.util.List;

public class VehicleService {
    private final VehicleDAO vehicleDAO;

    public VehicleService(VehicleDAO vehicleDAO) {
        this.vehicleDAO = vehicleDAO;
    }

    public Vehicle addVehicle(int ownerId, String regNo, String make, String model, double batteryKwh, double maxChargeKw)
            throws SQLException, ChargeGridException {
        String reg = regNo.replace(" ", "").replace("-", "").toUpperCase();
        if (!Validator.isValidRegNo(reg)) throw new InvalidInputException("Invalid registration number (example: HR26AB1234).");
        if (Validator.isBlank(make) || Validator.isBlank(model)) throw new InvalidInputException("Make and model are required.");
        if (batteryKwh < 5 || batteryKwh > 200) throw new InvalidInputException("Battery capacity must be 5-200 kWh.");
        if (maxChargeKw < 3 || maxChargeKw > 350) throw new InvalidInputException("Max charge rate must be 3-350 kW.");
        if (vehicleDAO.regNoExists(reg)) throw new InvalidInputException("A vehicle with this registration is already registered.");
        int id = vehicleDAO.addVehicle(new Vehicle(0, ownerId, reg, make, model, batteryKwh, maxChargeKw));
        return new Vehicle(id, ownerId, reg, make, model, batteryKwh, maxChargeKw);
    }

    public List<Vehicle> listVehicles(int ownerId) throws SQLException {
        return vehicleDAO.findByOwner(ownerId);
    }

    public Vehicle getOwnedVehicle(int vehicleId, int ownerId) throws SQLException, ChargeGridException {
        Vehicle v = vehicleDAO.findById(vehicleId);
        if (v == null || v.getOwnerId() != ownerId) throw new ChargeGridException("Vehicle " + vehicleId + " not found in your garage.");
        return v;
    }

    public void deleteVehicle(int vehicleId, int ownerId) throws SQLException, ChargeGridException {
        try {
            if (!vehicleDAO.deleteVehicle(vehicleId, ownerId)) throw new ChargeGridException("Vehicle not found in your garage.");
        } catch (java.sql.SQLIntegrityConstraintViolationException e) {
            throw new ChargeGridException("Cannot remove: this vehicle has booking or charging history.");
        }
    }
}
