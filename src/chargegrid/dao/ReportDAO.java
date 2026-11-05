package chargegrid.dao;

import java.sql.SQLException;
import java.util.List;

/** Read-only analytic queries. A null stationId means "all stations". */
public interface ReportDAO {
    List<String[]> systemSummary() throws SQLException;
    List<String[]> revenueByStation(Integer stationId) throws SQLException;
    List<String[]> chargerUsage(Integer stationId) throws SQLException;
    List<String[]> peakHours(Integer stationId) throws SQLException;
    List<String[]> topCustomers(Integer stationId) throws SQLException;
}
