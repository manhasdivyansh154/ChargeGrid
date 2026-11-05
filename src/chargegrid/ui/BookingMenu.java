package chargegrid.ui;

import chargegrid.model.Booking;
import chargegrid.model.Charger;
import chargegrid.model.Customer;
import chargegrid.model.Vehicle;
import chargegrid.service.AppContext;
import chargegrid.service.BookingService;
import chargegrid.util.ConsoleUtil;
import chargegrid.util.DateTimeUtil;
import chargegrid.util.InputHelper;

import java.time.LocalDateTime;

public class BookingMenu extends BaseMenu {
    private final Customer customer;

    public BookingMenu(AppContext ctx, Customer customer) {
        super(ctx);
        this.customer = customer;
    }

    public void show() {
        boolean back = false;
        while (!back) {
            ConsoleUtil.header("SLOT BOOKINGS");
            System.out.println("1. Book a charging slot");
            System.out.println("2. My bookings");
            System.out.println("3. Cancel a booking");
            System.out.println("0. Back");
            switch (InputHelper.readInt("Choice: ", 0, 3)) {
                case 1:
                    safely(this::book);
                    break;
                case 2:
                    safely(() -> printBookings(ctx.bookingService.listForUser(customer.getId())));
                    break;
                case 3:
                    safely(() -> {
                        printBookings(ctx.bookingService.listForUser(customer.getId()));
                        int id = readId("Booking ID to cancel: ");
                        ctx.bookingService.cancelBooking(customer.getId(), id);
                        ConsoleUtil.success("Booking cancelled.");
                    });
                    break;
                default:
                    back = true;
            }
        }
    }

    private void book() throws Exception {
        Vehicle v = pickVehicle(customer);
        Charger c = pickCharger();
        LocalDateTime start = InputHelper.readDateTime("Slot start");
        int minutes = InputHelper.readInt("Duration in minutes (" + BookingService.MIN_MINUTES + "-" + BookingService.MAX_MINUTES + "): ",
                1, 100000);

        double kwh = ctx.chargingService.estimateEnergy(v, c, minutes);
        double cost = ctx.chargingService.estimateCost(v, c, minutes, start);
        System.out.println();
        System.out.println("Estimated energy : " + ConsoleUtil.num(kwh) + " kWh");
        System.out.println("Estimated cost   : " + ConsoleUtil.money(cost) + " (+5% GST)"
                + (c.isPeak(start) ? "   [peak-hour rate applies]" : ""));
        if (!InputHelper.readYesNo("Confirm booking?")) {
            ConsoleUtil.info("Booking not made.");
            return;
        }
        Booking b = ctx.bookingService.createBooking(customer.getId(), v.getId(), c.getId(), start, minutes);
        ConsoleUtil.success("Booking #" + b.getId() + " confirmed: " + DateTimeUtil.format(b.getStartTime())
                + " to " + DateTimeUtil.format(b.getEndTime()));
    }
}
