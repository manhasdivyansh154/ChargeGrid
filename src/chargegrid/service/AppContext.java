package chargegrid.service;

import chargegrid.dao.*;

/** Wires DAOs into services once and shares them with every menu (simple manual dependency injection). */
public class AppContext {
    public final AuthService authService;
    public final StationService stationService;
    public final ChargerService chargerService;
    public final VehicleService vehicleService;
    public final BookingService bookingService;
    public final ChargingService chargingService;
    public final BillingService billingService;
    public final MaintenanceService maintenanceService;
    public final ReportService reportService;

    public AppContext() {
        UserDAO userDAO = new UserDAOImpl();
        StationDAO stationDAO = new StationDAOImpl();
        ChargerDAO chargerDAO = new ChargerDAOImpl();
        VehicleDAO vehicleDAO = new VehicleDAOImpl();
        BookingDAO bookingDAO = new BookingDAOImpl();
        SessionDAO sessionDAO = new SessionDAOImpl();
        WalletDAO walletDAO = new WalletDAOImpl();
        InvoiceDAO invoiceDAO = new InvoiceDAOImpl();
        MaintenanceDAO maintenanceDAO = new MaintenanceDAOImpl();
        ReportDAO reportDAO = new ReportDAOImpl();

        authService = new AuthService(userDAO, walletDAO, stationDAO);
        stationService = new StationService(stationDAO);
        chargerService = new ChargerService(chargerDAO, stationDAO);
        vehicleService = new VehicleService(vehicleDAO);
        bookingService = new BookingService(bookingDAO, vehicleService, chargerDAO);
        billingService = new BillingService(walletDAO, invoiceDAO);
        chargingService = new ChargingService(sessionDAO, chargerDAO, vehicleDAO, bookingDAO, billingService);
        maintenanceService = new MaintenanceService(maintenanceDAO, chargerDAO);
        reportService = new ReportService(reportDAO);
    }
}
