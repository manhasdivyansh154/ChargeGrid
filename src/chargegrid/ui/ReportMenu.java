package chargegrid.ui;

import chargegrid.service.AppContext;
import chargegrid.util.ConsoleUtil;
import chargegrid.util.InputHelper;

/** Analytics. stationFilter == null for admins (whole network). */
public class ReportMenu extends BaseMenu {
    private final Integer stationFilter;

    public ReportMenu(AppContext ctx, Integer stationFilter) {
        super(ctx);
        this.stationFilter = stationFilter;
    }

    public void show() {
        boolean back = false;
        while (!back) {
            ConsoleUtil.header(stationFilter == null ? "NETWORK REPORTS" : "STATION REPORTS");
            if (stationFilter == null) System.out.println("1. System summary");
            System.out.println("2. Revenue by station");
            System.out.println("3. Charger usage");
            System.out.println("4. Peak hours");
            System.out.println("5. Top customers");
            System.out.println("0. Back");
            int choice = InputHelper.readInt("Choice: ", 0, 5);
            switch (choice) {
                case 1:
                    if (stationFilter != null) { ConsoleUtil.error("Not available for managers."); break; }
                    safely(() -> ConsoleUtil.printTable(new String[]{"Metric", "Value"}, ctx.reportService.systemSummary()));
                    break;
                case 2:
                    safely(() -> ConsoleUtil.printTable(new String[]{"Station", "Sessions", "Energy kWh", "Billed Rs", "Collected Rs"},
                            ctx.reportService.revenueByStation(stationFilter)));
                    break;
                case 3:
                    safely(() -> ConsoleUtil.printTable(new String[]{"Charger", "Station", "Type", "Sessions", "Energy kWh"},
                            ctx.reportService.chargerUsage(stationFilter)));
                    break;
                case 4:
                    safely(() -> ConsoleUtil.printTable(new String[]{"Hour of day", "Sessions", "Energy kWh"},
                            ctx.reportService.peakHours(stationFilter)));
                    break;
                case 5:
                    safely(() -> ConsoleUtil.printTable(new String[]{"Customer", "Sessions", "Energy kWh", "Spend Rs"},
                            ctx.reportService.topCustomers(stationFilter)));
                    break;
                default:
                    back = true;
            }
        }
    }
}
