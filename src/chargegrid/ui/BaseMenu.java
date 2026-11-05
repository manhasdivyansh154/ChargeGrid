package chargegrid.ui;

import chargegrid.exception.ChargeGridException;
import chargegrid.model.*;
import chargegrid.service.AppContext;
import chargegrid.util.ConsoleUtil;
import chargegrid.util.DateTimeUtil;
import chargegrid.util.InputHelper;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Shared behaviour for every menu: safe execution of actions, table printers and pickers. */
public abstract class BaseMenu {
    protected final AppContext ctx;

    protected BaseMenu(AppContext ctx) {
        this.ctx = ctx;
    }

    /** An action that may throw; errors are reported instead of crashing the menu loop. */
    public interface MenuAction {
        void run() throws Exception;
    }

    protected void safely(MenuAction action) {
        try {
            action.run();
        } catch (ChargeGridException e) {
            ConsoleUtil.error(e.getMessage());
        } catch (SQLException e) {
            ConsoleUtil.error("Database error: " + e.getMessage());
        } catch (Exception e) {
            ConsoleUtil.error("Unexpected problem: " + e);
        }
    }

    protected int readId(String prompt) {
        return InputHelper.readInt(prompt, 1, Integer.MAX_VALUE);
    }

    // ---------------------------------------------------------------- printers

    protected void printStations(List<Station> list) {
        List<String[]> rows = new ArrayList<>();
        for (Station s : list) rows.add(new String[]{String.valueOf(s.getId()), s.getName(), s.getCity(), s.getAddress()});
        ConsoleUtil.printTable(new String[]{"ID", "Station", "City", "Address"}, rows);
    }

    protected void printChargers(List<Charger> list) {
        List<String[]> rows = new ArrayList<>();
        for (Charger c : list) {
            rows.add(new String[]{String.valueOf(c.getId()), String.valueOf(c.getStationId()), c.getType().getLabel(),
                    ConsoleUtil.num(c.getPowerKw()), ConsoleUtil.num(c.getRatePerKwh()), c.getStatus().name()});
        }
        ConsoleUtil.printTable(new String[]{"ID", "Station", "Type", "Power kW", "Rs/kWh", "Status"}, rows);
    }

    protected void printVehicles(List<Vehicle> list) {
        List<String[]> rows = new ArrayList<>();
        for (Vehicle v : list) {
            rows.add(new String[]{String.valueOf(v.getId()), v.getRegNo(), v.getMake() + " " + v.getModel(),
                    ConsoleUtil.num(v.getBatteryKwh()), ConsoleUtil.num(v.getMaxChargeKw())});
        }
        ConsoleUtil.printTable(new String[]{"ID", "Reg No", "Vehicle", "Battery kWh", "Max kW"}, rows);
    }

    protected void printBookings(List<Booking> list) {
        List<String[]> rows = new ArrayList<>();
        for (Booking b : list) {
            rows.add(new String[]{String.valueOf(b.getId()), String.valueOf(b.getUserId()), String.valueOf(b.getChargerId()),
                    String.valueOf(b.getVehicleId()), DateTimeUtil.format(b.getStartTime()),
                    DateTimeUtil.format(b.getEndTime()), b.getStatus().name()});
        }
        ConsoleUtil.printTable(new String[]{"ID", "User", "Charger", "Vehicle", "Start", "End", "Status"}, rows);
    }

    protected void printSessions(List<ChargingSession> list) {
        List<String[]> rows = new ArrayList<>();
        for (ChargingSession s : list) {
            rows.add(new String[]{String.valueOf(s.getId()), String.valueOf(s.getUserId()), String.valueOf(s.getVehicleId()),
                    String.valueOf(s.getChargerId()), DateTimeUtil.format(s.getStartTime()), DateTimeUtil.format(s.getEndTime()),
                    ConsoleUtil.num(s.getEnergyKwh()), ConsoleUtil.num(s.getCost()), s.getStatus().name()});
        }
        ConsoleUtil.printTable(new String[]{"ID", "User", "Vehicle", "Charger", "Start", "End", "kWh", "Cost Rs", "Status"}, rows);
    }

    protected void printInvoices(List<Invoice> list) {
        List<String[]> rows = new ArrayList<>();
        for (Invoice i : list) {
            rows.add(new String[]{String.valueOf(i.getId()), String.valueOf(i.getSessionId()), ConsoleUtil.num(i.getAmount()),
                    ConsoleUtil.num(i.getTax()), ConsoleUtil.num(i.getTotal()), i.getStatus().name(), DateTimeUtil.format(i.getIssuedAt())});
        }
        ConsoleUtil.printTable(new String[]{"Invoice", "Session", "Amount", "Tax", "Total", "Status", "Issued"}, rows);
    }

    protected void printTickets(List<MaintenanceTicket> list) {
        List<String[]> rows = new ArrayList<>();
        for (MaintenanceTicket t : list) {
            rows.add(new String[]{String.valueOf(t.getId()), String.valueOf(t.getChargerId()), t.getDescription(), t.getStatus().name(),
                    t.getTechnicianId() == 0 ? "-" : String.valueOf(t.getTechnicianId()), DateTimeUtil.format(t.getCreatedAt())});
        }
        ConsoleUtil.printTable(new String[]{"Ticket", "Charger", "Problem", "Status", "Tech", "Raised"}, rows);
    }

    // ---------------------------------------------------------------- pickers

    /** Shows all stations and asks the user to choose one. */
    protected Station pickStation() throws SQLException, ChargeGridException {
        List<Station> stations = ctx.stationService.listStations();
        if (stations.isEmpty()) throw new ChargeGridException("No stations exist yet.");
        printStations(stations);
        return ctx.stationService.getStation(readId("Station ID: "));
    }

    /** Station, then charger within that station. */
    protected Charger pickCharger() throws SQLException, ChargeGridException {
        Station s = pickStation();
        List<Charger> chargers = ctx.chargerService.listByStation(s.getId());
        if (chargers.isEmpty()) throw new ChargeGridException("This station has no chargers yet.");
        printChargers(chargers);
        return ctx.chargerService.getChargerInStation(readId("Charger ID: "), s.getId());
    }

    protected Vehicle pickVehicle(User owner) throws SQLException, ChargeGridException {
        List<Vehicle> vehicles = ctx.vehicleService.listVehicles(owner.getId());
        if (vehicles.isEmpty()) throw new ChargeGridException("You have no vehicles yet. Add one from 'My Vehicles' first.");
        printVehicles(vehicles);
        return ctx.vehicleService.getOwnedVehicle(readId("Vehicle ID: "), owner.getId());
    }

    protected void changePassword(User user) {
        safely(() -> {
            String oldPw = InputHelper.readString("Current password: ");
            String newPw = InputHelper.readString("New password (min 6 chars): ");
            ctx.authService.changePassword(user, oldPw, newPw);
            ConsoleUtil.success("Password changed.");
        });
    }
}
