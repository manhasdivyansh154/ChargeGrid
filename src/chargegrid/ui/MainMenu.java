package chargegrid.ui;

import chargegrid.model.Admin;
import chargegrid.model.Customer;
import chargegrid.model.StationManager;
import chargegrid.model.User;
import chargegrid.service.AppContext;
import chargegrid.util.ConsoleUtil;
import chargegrid.util.InputHelper;

/** Entry menu: login, self-registration for customers, exit. Routes each role to its own menu. */
public class MainMenu extends BaseMenu {

    public MainMenu(AppContext ctx) {
        super(ctx);
    }

    public void show() {
        boolean exit = false;
        while (!exit) {
            ConsoleUtil.header("CHARGEGRID - EV Charging Network");
            System.out.println("1. Login");
            System.out.println("2. Register as customer");
            System.out.println("0. Exit");
            switch (InputHelper.readInt("Choice: ", 0, 2)) {
                case 1:
                    safely(this::login);
                    break;
                case 2:
                    safely(this::register);
                    break;
                default:
                    exit = true;
            }
        }
        System.out.println("\nThank you for using ChargeGrid. Goodbye!");
    }

    private void login() throws Exception {
        String username = InputHelper.readString("Username: ");
        String password = InputHelper.readString("Password: ");
        User user = ctx.authService.login(username, password);
        ConsoleUtil.success("Welcome, " + user.getFullName() + " (" + user.getRole() + ")");
        if (user instanceof Admin) {
            new AdminMenu(ctx, (Admin) user).show();
        } else if (user instanceof StationManager) {
            new ManagerMenu(ctx, (StationManager) user).show();
        } else {
            new CustomerMenu(ctx, (Customer) user).show();
        }
    }

    private void register() throws Exception {
        ConsoleUtil.header("CUSTOMER REGISTRATION");
        String username = InputHelper.readString("Choose a username (4-20 letters/digits/_): ");
        String password = InputHelper.readString("Choose a password (min 6 chars): ");
        String name = InputHelper.readString("Full name: ");
        String email = InputHelper.readString("Email: ");
        String phone = InputHelper.readString("Mobile (10 digits): ");
        ctx.authService.registerCustomer(username, password, name, email, phone);
        ConsoleUtil.success("Registration complete. You can log in now.");
    }
}
