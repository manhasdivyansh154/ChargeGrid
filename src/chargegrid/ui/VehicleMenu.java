package chargegrid.ui;

import chargegrid.model.Customer;
import chargegrid.model.Vehicle;
import chargegrid.service.AppContext;
import chargegrid.util.ConsoleUtil;
import chargegrid.util.InputHelper;

public class VehicleMenu extends BaseMenu {
    private final Customer customer;

    public VehicleMenu(AppContext ctx, Customer customer) {
        super(ctx);
        this.customer = customer;
    }

    public void show() {
        boolean back = false;
        while (!back) {
            ConsoleUtil.header("MY VEHICLES");
            System.out.println("1. Add vehicle");
            System.out.println("2. View my vehicles");
            System.out.println("3. Remove vehicle");
            System.out.println("0. Back");
            switch (InputHelper.readInt("Choice: ", 0, 3)) {
                case 1:
                    safely(() -> {
                        String reg = InputHelper.readString("Registration no (e.g. HR26AB1234): ");
                        String make = InputHelper.readString("Make (e.g. Tata): ");
                        String model = InputHelper.readString("Model (e.g. Nexon EV): ");
                        double battery = InputHelper.readDouble("Battery capacity (kWh): ", 5, 200);
                        double maxKw = InputHelper.readDouble("Max charging rate the car accepts (kW): ", 3, 350);
                        Vehicle v = ctx.vehicleService.addVehicle(customer.getId(), reg, make, model, battery, maxKw);
                        ConsoleUtil.success("Vehicle added with ID " + v.getId());
                    });
                    break;
                case 2:
                    safely(() -> printVehicles(ctx.vehicleService.listVehicles(customer.getId())));
                    break;
                case 3:
                    safely(() -> {
                        printVehicles(ctx.vehicleService.listVehicles(customer.getId()));
                        int id = readId("Vehicle ID to remove: ");
                        if (InputHelper.readYesNo("Remove vehicle " + id + "?")) {
                            ctx.vehicleService.deleteVehicle(id, customer.getId());
                            ConsoleUtil.success("Vehicle removed.");
                        }
                    });
                    break;
                default:
                    back = true;
            }
        }
    }
}
