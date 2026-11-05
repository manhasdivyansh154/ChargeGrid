package chargegrid.dao;

import chargegrid.model.MaintenanceTicket;
import chargegrid.model.Technician;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface MaintenanceDAO {
    int addTicket(MaintenanceTicket t) throws SQLException;
    MaintenanceTicket findTicket(int id) throws SQLException;
    List<MaintenanceTicket> findAllTickets() throws SQLException;
    List<MaintenanceTicket> findTicketsByStation(int stationId) throws SQLException;
    boolean assignTechnician(int ticketId, int technicianId) throws SQLException;
    boolean resolveTicket(Connection con, int ticketId) throws SQLException;
    int addTechnician(Technician t) throws SQLException;
    Technician findTechnician(int id) throws SQLException;
    List<Technician> findAllTechnicians() throws SQLException;
}
