package chargegrid.ui;

import chargegrid.model.Station;
import chargegrid.service.AppContext;
import chargegrid.util.ConsoleUtil;
import chargegrid.util.InputHelper;

/** Admin only: add / list / update / delete stations. */
public class StationMenu extends BaseMenu {

    public StationMenu(AppContext ctx) {
        super(ctx);
    }

    public void show() {
        boolean back = false;
        while (!back) {
            ConsoleUtil.header("STATION MANAGEMENT");
            System.out.println("1. Add station");
            System.out.println("2. List stations");
            System.out.println("3. Update station");
            System.out.println("4. Delete station");
            System.out.println("0. Back");
            switch (InputHelper.readInt("Choice: ", 0, 4)) {
                case 1:
                    safely(() -> {
                        String name = InputHelper.readString("Station name: ");
                        String city = InputHelper.readString("City: ");
                        String address = InputHelper.readString("Address: ");
                        Station s = ctx.stationService.addStation(name, city, address);
                        ConsoleUtil.success("Station added with ID " + s.getId());
                    });
                    break;
                case 2:
                    safely(() -> printStations(ctx.stationService.listStations()));
                    break;
                case 3:
                    safely(() -> {
                        printStations(ctx.stationService.listStations());
                        int id = readId("Station ID to update: ");
                        ctx.stationService.getStation(id);
                        System.out.println("(leave a field blank to keep its current value)");
                        String name = InputHelper.readLine("New name: ");
                        String city = InputHelper.readLine("New city: ");
                        String address = InputHelper.readLine("New address: ");
                        ctx.stationService.updateStation(id, name, city, address);
                        ConsoleUtil.success("Station updated.");
                    });
                    break;
                case 4:
                    safely(() -> {
                        printStations(ctx.stationService.listStations());
                        int id = readId("Station ID to delete: ");
                        if (InputHelper.readYesNo("Delete station " + id + "?")) {
                            ctx.stationService.deleteStation(id);
                            ConsoleUtil.success("Station deleted.");
                        }
                    });
                    break;
                default:
                    back = true;
            }
        }
    }
}
