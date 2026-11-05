package chargegrid.util;

import java.util.List;

public final class ConsoleUtil {
    private ConsoleUtil() { }

    public static void header(String title) {
        String bar = repeat('=', Math.max(title.length() + 6, 40));
        System.out.println();
        System.out.println(bar);
        System.out.println("   " + title);
        System.out.println(bar);
    }

    public static void success(String msg) { System.out.println("[OK]    " + msg); }
    public static void error(String msg)   { System.out.println("[ERROR] " + msg); }
    public static void info(String msg)    { System.out.println("[INFO]  " + msg); }

    public static String repeat(char c, int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) sb.append(c);
        return sb.toString();
    }

    public static void printTable(String[] headers, List<String[]> rows) {
        if (rows.isEmpty()) {
            System.out.println("  (no records)");
            return;
        }
        int[] w = new int[headers.length];
        for (int i = 0; i < headers.length; i++) w[i] = headers[i].length();
        for (String[] r : rows)
            for (int i = 0; i < r.length; i++)
                w[i] = Math.max(w[i], r[i] == null ? 1 : r[i].length());

        StringBuilder sep = new StringBuilder("+");
        for (int x : w) sep.append(repeat('-', x + 2)).append('+');

        System.out.println(sep);
        printRow(headers, w);
        System.out.println(sep);
        for (String[] r : rows) printRow(r, w);
        System.out.println(sep);
    }

    private static void printRow(String[] cells, int[] w) {
        StringBuilder sb = new StringBuilder("|");
        for (int i = 0; i < w.length; i++) {
            String c = (i < cells.length && cells[i] != null) ? cells[i] : "-";
            sb.append(' ').append(String.format("%-" + w[i] + "s", c)).append(" |");
        }
        System.out.println(sb);
    }

    public static String money(double v) { return String.format("Rs.%.2f", v); }
    public static String num(double v)   { return String.format("%.2f", v); }
}
