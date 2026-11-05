package chargegrid.util;

import java.util.regex.Pattern;

public final class Validator {
    private static final Pattern EMAIL = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE = Pattern.compile("^[6-9][0-9]{9}$");
    private static final Pattern USERNAME = Pattern.compile("^[A-Za-z0-9_]{4,20}$");
    private static final Pattern REG_NO = Pattern.compile("^[A-Z]{2}[0-9]{1,2}[A-Z]{0,3}[0-9]{1,4}$");

    private Validator() { }

    public static boolean isValidEmail(String s) { return s != null && EMAIL.matcher(s).matches(); }
    public static boolean isValidPhone(String s) { return s != null && PHONE.matcher(s).matches(); }
    public static boolean isValidUsername(String s) { return s != null && USERNAME.matcher(s).matches(); }
    public static boolean isValidPassword(String s) { return s != null && s.length() >= 6; }
    public static boolean isValidRegNo(String s) { return s != null && REG_NO.matcher(s).matches(); }
    public static boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }
}
