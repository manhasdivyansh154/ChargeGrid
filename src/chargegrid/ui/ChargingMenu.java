package chargegrid.ui;

import chargegrid.model.*;
import chargegrid.service.AppContext;
import chargegrid.util.ConsoleUtil;
import chargegrid.util.DateTimeUtil;
import chargegrid.util.InputHelper;

import java.util.List;

public class ChargingMenu extends BaseMenu {
    private final Customer customer;

    public ChargingMenu(AppContext ctx, Customer customer) {
        super(ctx);
        this.customer = customer;
    }

    public void show() {
        boolean back = false;
        while (!back) {
            ConsoleUtil.header("CHARGING SESSIONS");
            System.out.println("1. Start charging");
            System.out.println("2. Stop charging");
            System.out.println("3. My active sessions");
            System.out.println("4. Charging history");
            System.out.println("0. Back");
            switch (InputHelper.readInt("Choice: ", 0, 4)) {
                case 1:
                    safely(this::start);
                    break;
                case 2:
                    safely(this::stop);
                    break;
                case 3:
                    safely(() -> printSessions(ctx.chargingService.activeSessions(customer.getId())));
                    break;
                case 4:
                    safely(() -> printSessions(ctx.chargingService.history(customer.getId())));
                    break;
                default:
                    back = true;
            }
        }
    }

    private void start() throws Exception {
        Vehicle v = pickVehicle(customer);
        int chargerId;
        int bookingId = 0;
        if (InputHelper.readYesNo("Do you have a booking for this session?")) {
            List<Booking> mine = ctx.bookingService.listForUser(customer.getId());
            printBookings(mine);
            bookingId = readId("Booking ID: ");
            Booking found = null;
            for (Booking b : mine) if (b.getId() == bookingId) found = b;
            if (found == null) throw new chargegrid.exception.ChargeGridException("Booking " + bookingId + " not found.");
            chargerId = found.getChargerId();
        } else {
            chargerId = pickCharger().getId();
        }
        ChargingSession s = ctx.chargingService.startSession(customer.getId(), v.getId(), chargerId, bookingId);
        ConsoleUtil.success("Charging started. Session #" + s.getId() + " on charger " + chargerId
                + " at " + DateTimeUtil.format(s.getStartTime()));
        ConsoleUtil.info("Choose 'Stop charging' when you are done.");
    }

    private void stop() throws Exception {
        List<ChargingSession> active = ctx.chargingService.activeSessions(customer.getId());
        if (active.isEmpty()) {
            ConsoleUtil.info("You have no active sessions.");
            return;
        }
        printSessions(active);
        int sessionId = readId("Session ID to stop: ");
        int minutes = InputHelper.readInt("How many minutes did you charge? (simulation, 1-600): ", 1, 600);
        Invoice inv = ctx.chargingService.stopSession(customer.getId(), sessionId, minutes);

        ConsoleUtil.header("CHARGING COMPLETE - INVOICE #" + inv.getId());
        for (ChargingSession s : ctx.chargingService.history(customer.getId())) {
            if (s.getId() == sessionId) System.out.println("Energy delivered : " + ConsoleUtil.num(s.getEnergyKwh()) + " kWh");
        }
        System.out.println("Charging cost    : " + ConsoleUtil.money(inv.getAmount()));
        System.out.println("GST (5%)         : " + ConsoleUtil.money(inv.getTax()));
        System.out.println("TOTAL            : " + ConsoleUtil.money(inv.getTotal()));
        if (inv.getStatus() == PaymentStatus.PAID) {
            ConsoleUtil.success("Paid automatically from your wallet.");
        } else {
            ConsoleUtil.info("Wallet balance was too low, so the invoice is UNPAID. Pay it from 'Wallet & Invoices'.");
        }
    }
}
