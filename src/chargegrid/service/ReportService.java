package chargegrid.service;

import chargegrid.dao.ReportDAO;

import java.sql.SQLException;
import java.util.List;

/** Thin wrapper over ReportDAO. stationId == null means "all stations" (admin view). */
public class ReportService {
    private final ReportDAO reportDAO;

    public ReportService(ReportDAO reportDAO) {
        this.reportDAO = reportDAO;
    }

    public List<String[]> systemSummary() throws SQLException { return reportDAO.systemSummary(); }
    public List<String[]> revenueByStation(Integer stationId) throws SQLException { return reportDAO.revenueByStation(stationId); }
    public List<String[]> chargerUsage(Integer stationId) throws SQLException { return reportDAO.chargerUsage(stationId); }
    public List<String[]> peakHours(Integer stationId) throws SQLException { return reportDAO.peakHours(stationId); }
    public List<String[]> topCustomers(Integer stationId) throws SQLException { return reportDAO.topCustomers(stationId); }
}
