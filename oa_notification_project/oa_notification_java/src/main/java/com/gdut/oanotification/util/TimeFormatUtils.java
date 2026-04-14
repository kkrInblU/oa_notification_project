package com.gdut.oanotification.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class TimeFormatUtils {

    private static final DateTimeFormatter MINUTE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter HOUR_MINUTE_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private TimeFormatUtils() {
    }

    public static String formatMinute(LocalDateTime value) {
        if (value == null) {
            return "";
        }
        return value.format(MINUTE_FORMATTER);
    }

    public static String nowHourMinute() {
        return LocalDateTime.now().format(HOUR_MINUTE_FORMATTER);
    }

    public static String formatSecond(LocalDateTime value) {
        if (value == null) {
            return "";
        }
        return value.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public static String buildSummary(String contentText, int limit) {
        if (contentText == null || contentText.isBlank()) {
            return "";
        }
        String compact = contentText.trim().replaceAll("\\s+", " ");
        if (compact.length() <= limit) {
            return compact;
        }
        return compact.substring(0, limit) + "...";
    }
}
