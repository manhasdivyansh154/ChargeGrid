package chargegrid.ui;

import chargegrid.model.Charger;
import chargegrid.model.MaintenanceTicket;
import chargegrid.model.Technician;
import chargegrid.model.User;
import chargegrid.service.AppContext;
import chargegrid.util.ConsoleUtil;
import chargegrid.util.InputHelper;

import java.util.ArrayList;
import java.util.List;

/** Tickets and technicians. stationFilter == null for admins; managers only see their own station. */
public class MaintenanceMenu extends BaseMenu {
    private final User user;
    private final Integer stationFilter;

    public MaintenanceMenu(AppContext ctx, User user, Integer stationFilter) {
        super(ctx);
        this.user = user;
        this.stationFilter = stationFilter;
    }

    private void listTickets() throws Exception {
        if (stationFilter == null) printTickets(ctx.maintenanceService.listAllTickets());
        else printTickets(ctx.maintenanceService.listTicketsByStation(stationFilter));
    }

    private void checkTicket(int id) throws Exception {
        if (stationFilter != null) ctx.maintenanceService.getTicketInStation(id, stationFilter);
    }

    private void printTechnicians() throws Exception {
        List<String[]> rows = new ArrayList<>();
        for (Technician t : ctx.maintenanceService.listTechnicians())
            rows.add(new String[]{String.valueOf(t.getId()), t.getName(), t.getPhone(), t.getSpeciality()});
        ConsoleUtil.printTable(new String[]{"ID", "Name", "Phone", "Speciality"}, rows);
    }

    public void show() {
        boolean back = false;
        while (!back) {
            ConsoleUtil.header("MAINTENANCE");
            System.out.println("1. View tickets");
            System.out.println("2. Report a charger fault");
            System.out.println("3. Assign technician to ticket");
            System.out.println("4. Resolve ticket");
            System.out.println("5. View technicians");
            if (stationFilter == null) System.out.println("6. Add technician");
            System.out.println("0. Back");
            int choice = InputHelper.readInt("Choice: ", 0, stationFilter == null ? 6 : 5);
            switch (choice) {
                case 1:
                    safely(this::listTickets);
                    break;
                case 2:
                    safely(() -> {
                        Charger c = stationFilter == null ? pickCharger() : pickOwnStationCharger();
                        String desc = InputHelper.readString("Describe the problem: ");
                        MaintenanceTicket t = ctx.maintenanceService.reportFault(user.getId(), c.getId(), desc);
                        ConsoleUtil.success("Ticket " + t.getId() + " raised.");
                    });
                    break;
                case 3:
                    safely(() -> {
                        listTickets();
                        int ticketId = readId("Ticket ID: ");
                        checkTicket(ticketId);
                        printTechnicians();
                        int techId = readId("Technician ID: ");
                        ctx.maintenanceService.assignTechnician(ticketId, techId);
                        ConsoleUtil.success("Technician assigned; ticket is now IN_PROGRESS.");
                    });
                    break;
                case 4:
                    safely(() -> {
                        listTickets();
                        int ticketId = readId("Ticket ID to resolve: ");
                        checkTicket(ticketId);
                        ctx.maintenanceService.resolveTicket(ticketId);
                        ConsoleUtil.success("Ticket resolved; faulty charger is AVAILABLE again.");
                    });
                    break;
                case 5:
                    safely(this::printTechnicians);
                    break;
                case 6:
                    safely(() -> {
                        String name = InputHelper.readString("Technician name: ");
                        String phone = InputHelper.readString("Phone (10 digits): ");
                        String spec = InputHelper.readString("Speciality: ");
                        Technician t = ctx.maintenanceService.addTechnician(name, phone, spec);
                        ConsoleUtil.success("Technician added with ID " + t.getId());
                    });
                    break;
                default:
                    back = true;
            }
        }
    }

    private Charger pickOwnStationCharger() throws Exception {
        printChargers(ctx.chargerService.listByStation(stationFilter));
        return ctx.chargerService.getChargerInStation(readId("Charger ID: "), stationFilter);
    }
}
