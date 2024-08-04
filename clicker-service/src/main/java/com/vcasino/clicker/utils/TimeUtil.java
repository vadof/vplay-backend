package com.vcasino.clicker.utils;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class TimeUtil {

    private final static ZoneOffset zoneOffset = ZoneOffset.of("+00:00");
    private final static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static Instant getCurrentInstant() {
        return Instant.now();
    }

    public static Long getCurrentUnixTime() {
        return getCurrentInstant().getEpochSecond();
    }

    public static LocalDateTime toLocalDateTime(Instant instant) {
        return LocalDateTime.ofInstant(instant, zoneOffset);
    }

    public static Instant toInstant(LocalDateTime localDateTime) {
        return localDateTime.toInstant(zoneOffset);
    }

    public static String format(LocalDateTime localDateTime) {
        return localDateTime.format(formatter);
    }

    public static Timestamp toTimestamp(LocalDateTime localDateTime) {
        return Timestamp.valueOf(localDateTime);
    }

    public static Timestamp toTimestamp(Instant instant) {
        return Timestamp.from(instant);
    }

    public static Timestamp getCurrentTimestamp() {
        return Timestamp.from(getCurrentInstant());
    }

}
