package chargegrid.ui;

import chargegrid.model.Customer;
import chargegrid.model.Invoice;
import chargegrid.model.Payment;
import chargegrid.model.PaymentMode;
import chargegrid.service.AppContext;
import chargegrid.util.ConsoleUtil;
import chargegrid.util.DateTimeUtil;
import chargegrid.util.InputHelper;

import java.util.ArrayList;
import java.util.List;

public class BillingMenu extends BaseMenu {
    private final Customer customer;

    public BillingMenu(AppContext ctx, Customer customer) {
        super(ctx);
        this.customer = customer;
    }

    public void show() {
        boolean back = false;
        while (!back) {
            ConsoleUtil.header("WALLET & INVOICES");
            System.out.println("1. Wallet balance");
            System.out.println("2. Top up wallet");
            System.out.println("3. My invoices");
            System.out.println("4. Pay an unpaid invoice");
            System.out.println("5. Payment history");
            System.out.println("0. Back");
            switch (InputHelper.readInt("Choice: ", 0, 5)) {
                case 1:
                    safely(() -> ConsoleUtil.info("Wallet balance: " + ConsoleUtil.money(ctx.billingService.getBalance(customer.getId()))));
                    break;
                case 2:
                    safely(() -> {
                        double amount = InputHelper.readDouble("Amount to add (Rs.10 - Rs.50,000): ", 1, 1000000);
                        double bal = ctx.billingService.topUp(customer.getId(), amount);
                        ConsoleUtil.success("Wallet topped up. New balance: " + ConsoleUtil.money(bal));
                    });
                    break;
                case 3:
                    safely(() -> printInvoices(ctx.billingService.listInvoices(customer.getId())));
                    break;
                case 4:
                    safely(this::payInvoice);
                    break;
                case 5:
                    safely(() -> {
                        List<String[]> rows = new ArrayList<>();
                        for (Payment p : ctx.billingService.listPayments(customer.getId())) {
                            rows.add(new String[]{String.valueOf(p.getId()), String.valueOf(p.getInvoiceId()),
                                    ConsoleUtil.num(p.getAmount()), p.getMode().name(), DateTimeUtil.format(p.getPaidAt())});
                        }
                        ConsoleUtil.printTable(new String[]{"Payment", "Invoice", "Amount Rs", "Mode", "Paid at"}, rows);
                    });
                    break;
                default:
                    back = true;
            }
        }
    }

    private void payInvoice() throws Exception {
        List<Invoice> unpaid = ctx.billingService.listUnpaid(customer.getId());
        if (unpaid.isEmpty()) {
            ConsoleUtil.info("You have no unpaid invoices.");
            return;
        }
        printInvoices(unpaid);
        int id = readId("Invoice ID to pay: ");
        System.out.println("Payment mode: 1) Wallet  2) UPI  3) Card  4) Cash");
        PaymentMode mode = PaymentMode.values()[InputHelper.readInt("Mode: ", 1, 4) - 1];
        Payment p = ctx.billingService.payInvoice(customer.getId(), id, mode);
        ConsoleUtil.success("Paid " + ConsoleUtil.money(p.getAmount()) + " via " + mode + " (payment #" + p.getId() + ").");
    }
}
