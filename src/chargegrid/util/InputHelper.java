package chargegrid.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

/** All console input goes through here so validation/looping is written only once. */
public final class InputHelper {
    private static final Scanner SC = new Scanner(System.in);

    private InputHelper() { }

    public static String readLine(String prompt) {
        System.out.print(prompt);
        if (!SC.hasNextLine()) {
            System.out.println("\nInput closed. Exiting.");
            System.exit(0);
        }
        return SC.nextLine().trim();
    }

    /** Re-asks until a non-empty value is typed. */
    public static String readString(String prompt) {
        while (true) {
            String s = readLine(prompt);
            if (!s.isEmpty()) return s;
            System.out.println("  Value cannot be empty.");
        }
    }

    public static int readInt(String prompt, int min, int max) {
        while (true) {
            String s = readLine(prompt);
            try {
                int v = Integer.parseInt(s);
                if (v >= min && v <= max) return v;
                System.out.println("  Enter a number between " + min + " and " + max + ".");
            } catch (NumberFormatException e) {
                System.out.println("  Please enter a valid whole number.");
            }
        }
    }

    public static double readDouble(String prompt, double min, double max) {
        while (true) {
            String s = readLine(prompt);
            try {
                double v = Double.parseDouble(s);
                if (v >= min && v <= max) return v;
                System.out.println("  Enter a value between " + min + " and " + max + ".");
            } catch (NumberFormatException e) {
                System.out.println("  Please enter a valid number.");
            }
        }
    }

    public static boolean readYesNo(String prompt) {
        while (true) {
            String s = readLine(prompt + " (y/n): ").toLowerCase();
            if (s.equals("y") || s.equals("yes")) return true;
            if (s.equals("n") || s.equals("no")) return false;
            System.out.println("  Please type y or n.");
        }
    }

    /** Accepts "yyyy-MM-dd HH:mm" or the word "now". */
    public static LocalDateTime readDateTime(String prompt) {
        while (true) {
            String s = readLine(prompt + " [yyyy-MM-dd HH:mm or 'now']: ");
            if (s.equalsIgnoreCase("now")) return DateTimeUtil.nowMinute();
            try {
                return DateTimeUtil.parse(s);
            } catch (DateTimeParseException e) {
                System.out.println("  Invalid format. Example: 2026-11-05 18:30");
            }
        }
    }
}
