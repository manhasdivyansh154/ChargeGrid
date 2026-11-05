package chargegrid.dao;

import chargegrid.model.Vehicle;
import java.sql.SQLException;
import java.util.List;

public interface VehicleDAO {
    int addVehicle(Vehicle v) throws SQLException;
    Vehicle findById(int id) throws SQLException;
    List<Vehicle> findByOwner(int ownerId) throws SQLException;
    boolean regNoExists(String regNo) throws SQLException;
    boolean deleteVehicle(int id, int ownerId) throws SQLException;
}
