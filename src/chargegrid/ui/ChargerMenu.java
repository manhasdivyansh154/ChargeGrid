package chargegrid.ui;

import chargegrid.model.Charger;
import chargegrid.model.ChargerStatus;
import chargegrid.model.ChargerType;
import chargegrid.service.AppContext;
import chargegrid.util.ConsoleUtil;
import chargegrid.util.InputHelper;

/** Charger management. stationFilter == null for admins (all stations), or the manager's own station id. */
public class ChargerMenu extends BaseMenu {
    private final Integer stationFilter;

    public ChargerMenu(AppContext ctx, Integer stationFilter) {
        super(ctx);
        this.stationFilter = stationFilter;
    }

    private Charger resolve(int id) throws Exception {
        return stationFilter == null ? ctx.chargerService.getCharger(id) : ctx.chargerService.getChargerInStation(id, stationFilter);
    }

    private void listChargers() throws Exception {
        if (stationFilter != null) {
            printChargers(ctx.chargerService.listByStation(stationFilter));
        } else {
            printChargers(ctx.chargerService.listAll());
        }
    }

    public void show() {
        boolean back = false;
        while (!back) {
            ConsoleUtil.header("CHARGER MANAGEMENT");
            System.out.println("1. View chargers");
            System.out.println("2. Add charger");
            System.out.println("3. Set charger status (Available / Faulty)");
            System.out.println("4. Update rate per kWh");
            System.out.println("5. Delete charger");
            System.out.println("0. Back");
            switch (InputHelper.readInt("Choice: ", 0, 5)) {
                case 1:
                    safely(this::listChargers);
                    break;
                case 2:
                    safely(this::addCharger);
                    break;
                case 3:
                    safely(() -> {
                        listChargers();
                        Charger c = resolve(readId("Charger ID: "));
                        int pick = InputHelper.readInt("New status: 1) AVAILABLE  2) FAULTY : ", 1, 2);
                        ctx.chargerService.setStatus(c.getId(), pick == 1 ? ChargerStatus.AVAILABLE : ChargerStatus.FAULTY);
                        ConsoleUtil.success("Status updated.");
                    });
                    break;
                case 4:
                    safely(() -> {
                        listChargers();
                        Charger c = resolve(readId("Charger ID: "));
                        double rate = InputHelper.readDouble("New rate (Rs per kWh): ", 1, 200);
                        ctx.chargerService.updateRate(c.getId(), rate);
                        ConsoleUtil.success("Rate updated.");
                    });
                    break;
                case 5:
                    safely(() -> {
                        listChargers();
                        Charger c = resolve(readId("Charger ID to delete: "));
                        if (InputHelper.readYesNo("Delete charger " + c.getId() + "?")) {
                            ctx.chargerService.deleteCharger(c.getId());
                            ConsoleUtil.success("Charger deleted.");
                        }
                    });
                    break;
                default:
                    back = true;
            }
        }
    }

    private void addCharger() throws Exception {
        int stationId;
        if (stationFilter != null) {
            stationId = stationFilter;
        } else {
            printStations(ctx.stationService.listStations());
            stationId = readId("Station ID: ");
        }
        System.out.println("Types: 1) AC Slow  2) DC Fast  3) Ultra Fast");
        ChargerType type = ChargerType.values()[InputHelper.readInt("Type: ", 1, 3) - 1];
        double power = InputHelper.readDouble("Power in kW (0 = default " + type.getDefaultPowerKw() + "): ", 0, 400);
        if (power == 0) power = type.getDefaultPowerKw();
        double rate = InputHelper.readDouble("Rate Rs/kWh (0 = default " + type.getDefaultRate() + "): ", 0, 200);
        if (rate == 0) rate = type.getDefaultRate();
        Charger c = ctx.chargerService.addCharger(stationId, type, power, rate);
        ConsoleUtil.success("Charger added with ID " + c.getId());
    }
}
