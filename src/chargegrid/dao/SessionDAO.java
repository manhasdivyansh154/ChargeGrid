package chargegrid.dao;

import chargegrid.model.ChargingSession;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public interface SessionDAO {
    int startSession(Connection con, ChargingSession s) throws SQLException;
    boolean completeSession(Connection con, int sessionId, LocalDateTime end, double energyKwh, double cost) throws SQLException;
    ChargingSession findById(int id) throws SQLException;
    List<ChargingSession> findAll() throws SQLException;
    List<ChargingSession> findByUser(int userId) throws SQLException;
    List<ChargingSession> findActiveByUser(int userId) throws SQLException;
    List<ChargingSession> findByStation(int stationId) throws SQLException;
}
