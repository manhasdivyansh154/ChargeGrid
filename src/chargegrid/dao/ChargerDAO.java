package chargegrid.dao;

import chargegrid.model.Charger;
import chargegrid.model.ChargerStatus;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface ChargerDAO {
    int addCharger(Charger c) throws SQLException;
    Charger findById(int id) throws SQLException;
    List<Charger> findAll() throws SQLException;
    List<Charger> findByStation(int stationId) throws SQLException;
    boolean updateStatus(int id, ChargerStatus status) throws SQLException;
    /** Transaction-aware version (uses the caller's connection). */
    boolean updateStatus(Connection con, int id, ChargerStatus status) throws SQLException;
    boolean updateRate(int id, double rate) throws SQLException;
    boolean deleteCharger(int id) throws SQLException;
}
