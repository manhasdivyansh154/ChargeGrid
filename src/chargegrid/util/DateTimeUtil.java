package chargegrid.util;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class DateTimeUtil {
    public static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private DateTimeUtil() { }

    public static LocalDateTime parse(String text) {
        return LocalDateTime.parse(text.trim(), FORMAT);
    }

    public static String format(LocalDateTime t) {
        return t == null ? "-" : t.format(FORMAT);
    }

    public static Timestamp toTimestamp(LocalDateTime t) {
        return t == null ? null : Timestamp.valueOf(t);
    }

    public static LocalDateTime fromTimestamp(Timestamp ts) {
        return ts == null ? null : ts.toLocalDateTime();
    }

    public static LocalDateTime nowMinute() {
        return LocalDateTime.now().withSecond(0).withNano(0);
    }
}
