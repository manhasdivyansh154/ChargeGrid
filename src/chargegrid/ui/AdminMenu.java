package chargegrid.ui;

import chargegrid.model.Admin;
import chargegrid.model.StationManager;
import chargegrid.model.User;
import chargegrid.service.AppContext;
import chargegrid.util.ConsoleUtil;
import chargegrid.util.InputHelper;

import java.util.ArrayList;
import java.util.List;

public class AdminMenu extends BaseMenu {
    private final Admin admin;

    public AdminMenu(AppContext ctx, Admin admin) {
        super(ctx);
        this.admin = admin;
    }

    public void show() {
        boolean logout = false;
        while (!logout) {
            ConsoleUtil.header("ADMIN MENU - " + admin.getFullName());
            System.out.println("1. Station management");
            System.out.println("2. Charger management");
            System.out.println("3. Users (list / create station manager)");
            System.out.println("4. Maintenance & technicians");
            System.out.println("5. All bookings");
            System.out.println("6. All charging sessions");
            System.out.println("7. Reports");
            System.out.println("8. Change password");
            System.out.println("0. Logout");
            switch (InputHelper.readInt("Choice: ", 0, 8)) {
                case 1:
                    new StationMenu(ctx).show();
                    break;
                case 2:
                    new ChargerMenu(ctx, null).show();
                    break;
                case 3:
                    userMenu();
                    break;
                case 4:
                    new MaintenanceMenu(ctx, admin, null).show();
                    break;
                case 5:
                    safely(() -> printBookings(ctx.bookingService.listAll()));
                    break;
                case 6:
                    safely(() -> printSessions(ctx.chargingService.listAll()));
                    break;
                case 7:
                    new ReportMenu(ctx, null).show();
                    break;
                case 8:
                    changePassword(admin);
                    break;
                default:
                    logout = true;
            }
        }
    }

    private void userMenu() {
        boolean back = false;
        while (!back) {
            ConsoleUtil.header("USER MANAGEMENT");
            System.out.println("1. List all users");
            System.out.println("2. Create station manager");
            System.out.println("0. Back");
            switch (InputHelper.readInt("Choice: ", 0, 2)) {
                case 1:
                    safely(() -> {
                        List<String[]> rows = new ArrayList<>();
                        for (User u : ctx.authService.listUsers()) {
                            String station = (u instanceof StationManager && ((StationManager) u).getStationId() > 0)
                                    ? String.valueOf(((StationManager) u).getStationId()) : "-";
                            rows.add(new String[]{String.valueOf(u.getId()), u.getUsername(), u.getFullName(),
                                    u.getRole().name(), u.getEmail(), u.getPhone(), station});
                        }
                        ConsoleUtil.printTable(new String[]{"ID", "Username", "Name", "Role", "Email", "Phone", "Station"}, rows);
                    });
                    break;
                case 2:
                    safely(() -> {
                        printStations(ctx.stationService.listStations());
                        int stationId = readId("Station ID to manage: ");
                        String username = InputHelper.readString("Username: ");
                        String password = InputHelper.readString("Password (min 6 chars): ");
                        String name = InputHelper.readString("Full name: ");
                        String email = InputHelper.readString("Email: ");
                        String phone = InputHelper.readString("Phone (10 digits): ");
                        StationManager m = ctx.authService.createManager(username, password, name, email, phone, stationId);
                        ConsoleUtil.success("Manager created with ID " + m.getId());
                    });
                    break;
                default:
                    back = true;
            }
        }
    }
}
