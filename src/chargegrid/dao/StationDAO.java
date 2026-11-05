package chargegrid.dao;

import chargegrid.model.Station;
import java.sql.SQLException;
import java.util.List;

public interface StationDAO {
    int addStation(Station s) throws SQLException;
    boolean updateStation(Station s) throws SQLException;
    boolean deleteStation(int id) throws SQLException;
    Station findById(int id) throws SQLException;
    List<Station> findAll() throws SQLException;
    List<Station> findByCity(String city) throws SQLException;
}
