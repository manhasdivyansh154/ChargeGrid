package chargegrid.ui;

import chargegrid.model.Charger;
import chargegrid.model.Customer;
import chargegrid.model.MaintenanceTicket;
import chargegrid.model.Station;
import chargegrid.service.AppContext;
import chargegrid.util.ConsoleUtil;
import chargegrid.util.InputHelper;

import java.util.List;

public class CustomerMenu extends BaseMenu {
    private final Customer customer;

    public CustomerMenu(AppContext ctx, Customer customer) {
        super(ctx);
        this.customer = customer;
    }

    public void show() {
        boolean logout = false;
        while (!logout) {
            ConsoleUtil.header("CUSTOMER MENU - " + customer.getFullName());
            System.out.println("1. My vehicles");
            System.out.println("2. Find stations & chargers");
            System.out.println("3. Book a slot");
            System.out.println("4. Charging sessions (start / stop / history)");
            System.out.println("5. Wallet & invoices");
            System.out.println("6. Report a faulty charger");
            System.out.println("7. Change password");
            System.out.println("0. Logout");
            switch (InputHelper.readInt("Choice: ", 0, 7)) {
                case 1:
                    new VehicleMenu(ctx, customer).show();
                    break;
                case 2:
                    safely(this::findStations);
                    break;
                case 3:
                    new BookingMenu(ctx, customer).show();
                    break;
                case 4:
                    new ChargingMenu(ctx, customer).show();
                    break;
                case 5:
                    new BillingMenu(ctx, customer).show();
                    break;
                case 6:
                    safely(() -> {
                        Charger c = pickCharger();
                        String desc = InputHelper.readString("Describe the problem: ");
                        MaintenanceTicket t = ctx.maintenanceService.reportFault(customer.getId(), c.getId(), desc);
                        ConsoleUtil.success("Thank you. Ticket " + t.getId() + " has been raised.");
                    });
                    break;
                case 7:
                    changePassword(customer);
                    break;
                default:
                    logout = true;
            }
        }
    }

    private void findStations() throws Exception {
        String city = InputHelper.readLine("City to search (blank = all): ");
        List<Station> stations = city.isEmpty() ? ctx.stationService.listStations() : ctx.stationService.searchByCity(city);
        printStations(stations);
        if (stations.isEmpty()) return;
        String pick = InputHelper.readLine("Station ID to see its chargers (blank to skip): ");
        if (!pick.isEmpty()) {
            try {
                Station s = ctx.stationService.getStation(Integer.parseInt(pick));
                printChargers(ctx.chargerService.listByStation(s.getId()));
            } catch (NumberFormatException e) {
                ConsoleUtil.error("Please enter a number.");
            }
        }
    }
}
