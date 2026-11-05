package chargegrid.ui;

import chargegrid.model.Station;
import chargegrid.model.StationManager;
import chargegrid.service.AppContext;
import chargegrid.util.ConsoleUtil;
import chargegrid.util.InputHelper;

public class ManagerMenu extends BaseMenu {
    private final StationManager manager;

    public ManagerMenu(AppContext ctx, StationManager manager) {
        super(ctx);
        this.manager = manager;
    }

    public void show() {
        int stationId = manager.getStationId();
        if (stationId == 0) {
            ConsoleUtil.error("No station is assigned to your account yet. Ask an admin to assign one.");
            return;
        }
        String stationName = "station " + stationId;
        try {
            Station s = ctx.stationService.getStation(stationId);
            stationName = s.getName() + " (" + s.getCity() + ")";
        } catch (Exception e) {
            // keep the generic name
        }

        boolean logout = false;
        while (!logout) {
            ConsoleUtil.header("MANAGER - " + stationName);
            System.out.println("1. Chargers (view / add / status / rate)");
            System.out.println("2. Station bookings");
            System.out.println("3. Station charging sessions");
            System.out.println("4. Maintenance tickets");
            System.out.println("5. Station reports");
            System.out.println("6. Change password");
            System.out.println("0. Logout");
            switch (InputHelper.readInt("Choice: ", 0, 6)) {
                case 1:
                    new ChargerMenu(ctx, stationId).show();
                    break;
                case 2:
                    safely(() -> printBookings(ctx.bookingService.listForStation(stationId)));
                    break;
                case 3:
                    safely(() -> printSessions(ctx.chargingService.listForStation(stationId)));
                    break;
                case 4:
                    new MaintenanceMenu(ctx, manager, stationId).show();
                    break;
                case 5:
                    new ReportMenu(ctx, stationId).show();
                    break;
                case 6:
                    changePassword(manager);
                    break;
                default:
                    logout = true;
            }
        }
    }
}
