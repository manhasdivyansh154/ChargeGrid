package chargegrid.service;

import chargegrid.dao.StationDAO;
import chargegrid.exception.ChargeGridException;
import chargegrid.exception.InvalidInputException;
import chargegrid.model.Station;
import chargegrid.util.Validator;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;

public class StationService {
    private final StationDAO stationDAO;

    public StationService(StationDAO stationDAO) {
        this.stationDAO = stationDAO;
    }

    public Station addStation(String name, String city, String address) throws SQLException, ChargeGridException {
        if (Validator.isBlank(name) || Validator.isBlank(city) || Validator.isBlank(address))
            throw new InvalidInputException("Name, city and address are all required.");
        Station s = new Station(0, name, city, address);
        return new Station(stationDAO.addStation(s), name, city, address);
    }

    public Station getStation(int id) throws SQLException, ChargeGridException {
        Station s = stationDAO.findById(id);
        if (s == null) throw new ChargeGridException("Station " + id + " not found.");
        return s;
    }

    public void updateStation(int id, String name, String city, String address) throws SQLException, ChargeGridException {
        Station s = getStation(id);
        if (!Validator.isBlank(name)) s.setName(name);
        if (!Validator.isBlank(city)) s.setCity(city);
        if (!Validator.isBlank(address)) s.setAddress(address);
        stationDAO.updateStation(s);
    }

    public void deleteStation(int id) throws SQLException, ChargeGridException {
        getStation(id);
        try {
            stationDAO.deleteStation(id);
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new ChargeGridException("Cannot delete: this station still has chargers. Delete its chargers first.");
        }
    }

    public List<Station> listStations() throws SQLException {
        return stationDAO.findAll();
    }

    public List<Station> searchByCity(String city) throws SQLException {
        return stationDAO.findByCity(city);
    }
}
