package chargegrid.service;

import chargegrid.dao.ChargerDAO;
import chargegrid.dao.StationDAO;
import chargegrid.exception.ChargeGridException;
import chargegrid.exception.ChargerUnavailableException;
import chargegrid.exception.InvalidInputException;
import chargegrid.model.Charger;
import chargegrid.model.ChargerStatus;
import chargegrid.model.ChargerType;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;

public class ChargerService {
    private final ChargerDAO chargerDAO;
    private final StationDAO stationDAO;

    public ChargerService(ChargerDAO chargerDAO, StationDAO stationDAO) {
        this.chargerDAO = chargerDAO;
        this.stationDAO = stationDAO;
    }

    public Charger addCharger(int stationId, ChargerType type, double powerKw, double rate)
            throws SQLException, ChargeGridException {
        if (stationDAO.findById(stationId) == null) throw new InvalidInputException("Station " + stationId + " does not exist.");
        if (powerKw < 1 || powerKw > 400) throw new InvalidInputException("Power must be between 1 and 400 kW.");
        if (rate < 1 || rate > 200) throw new InvalidInputException("Rate must be between Rs.1 and Rs.200 per kWh.");
        Charger c = Charger.create(type, 0, stationId, powerKw, rate, ChargerStatus.AVAILABLE);
        int id = chargerDAO.addCharger(c);
        return Charger.create(type, id, stationId, powerKw, rate, ChargerStatus.AVAILABLE);
    }

    public Charger getCharger(int id) throws SQLException, ChargeGridException {
        Charger c = chargerDAO.findById(id);
        if (c == null) throw new ChargeGridException("Charger " + id + " not found.");
        return c;
    }

    /** Used by managers: the charger must belong to their own station. */
    public Charger getChargerInStation(int id, int stationId) throws SQLException, ChargeGridException {
        Charger c = getCharger(id);
        if (c.getStationId() != stationId) throw new ChargeGridException("Charger " + id + " is not in your station.");
        return c;
    }

    public List<Charger> listAll() throws SQLException {
        return chargerDAO.findAll();
    }

    public List<Charger> listByStation(int stationId) throws SQLException {
        return chargerDAO.findByStation(stationId);
    }

    public void setStatus(int chargerId, ChargerStatus status) throws SQLException, ChargeGridException {
        Charger c = getCharger(chargerId);
        if (status == ChargerStatus.OCCUPIED)
            throw new InvalidInputException("OCCUPIED is set automatically when a charging session starts.");
        if (c.getStatus() == ChargerStatus.OCCUPIED)
            throw new ChargerUnavailableException("Charger " + chargerId + " is in use right now.");
        chargerDAO.updateStatus(chargerId, status);
    }

    public void updateRate(int chargerId, double rate) throws SQLException, ChargeGridException {
        getCharger(chargerId);
        if (rate < 1 || rate > 200) throw new InvalidInputException("Rate must be between Rs.1 and Rs.200 per kWh.");
        chargerDAO.updateRate(chargerId, rate);
    }

    public void deleteCharger(int chargerId) throws SQLException, ChargeGridException {
        Charger c = getCharger(chargerId);
        if (c.getStatus() == ChargerStatus.OCCUPIED) throw new ChargerUnavailableException("Charger is in use right now.");
        try {
            chargerDAO.deleteCharger(chargerId);
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new ChargeGridException("Cannot delete: this charger has bookings, sessions or tickets. Mark it FAULTY instead.");
        }
    }
}
