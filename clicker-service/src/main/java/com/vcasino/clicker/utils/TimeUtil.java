package com.vcasino.clicker.utils;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class TimeUtil {

    private final static ZoneOffset zoneOffset = ZoneOffset.of("+00:00");
    private final static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    public static Instant getCurrentInstant() {
        return Instant.now();
    }

    public static Instant toInstant(LocalDateTime localDateTime) {
        return localDateTime.toInstant(zoneOffset);
    }

    public static LocalDate getCurrentDate() {
        return LocalDate.now();
    }

    public static boolean isToday(LocalDate date) {
        return date.isEqual(getCurrentDate());
    }

    public static boolean isYesterday(LocalDate date) {
        return date.isEqual(getCurrentDate().minusDays(1));
    }

    public static Timestamp toTimestamp(LocalDateTime localDateTime) {
        return Timestamp.valueOf(localDateTime);
    }

    public static Timestamp toTimestamp(Instant instant) {
        return Timestamp.from(instant);
    }

    public static Timestamp toTimestamp(Long unixTime) {
        return new Timestamp(unixTime * 1000);
    }

    public static Timestamp getCurrentTimestamp() {
        return Timestamp.from(getCurrentInstant());
    }

    public static LocalDateTime getCurrentDateTime() {
        return LocalDateTime.now();
    }

    public static LocalDateTime parseDateTime(String date) {
        return LocalDateTime.parse(date, formatter);
    }

    public static LocalDateTime toLocalDateTime(Instant instant) {
        return LocalDateTime.ofInstant(instant, zoneOffset);
    }

    public static LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp.toLocalDateTime();
    }

    public static String format(LocalDateTime localDateTime) {
        return localDateTime.format(formatter);
    }

    public static Long toUnixTime(Timestamp timestamp) {
        return timestamp.getTime() / 1000;
    }

    public static Long getCurrentUnixTime() {
        return getCurrentInstant().getEpochSecond();
    }

    public static Long getDifferenceInSeconds(LocalDateTime start, LocalDateTime end) {
        return getDuration(start, end).getSeconds();
    }

    public static Long getDifferenceInSeconds(Long unixStartTime, Long unixEndTime) {
        return unixEndTime - unixStartTime;
    }

    public static Long getDifferenceInMinutes(Long unixStartTime, Long unixEndTime) {
        return (unixEndTime - unixStartTime) / 60;
    }

    public static Long getDifferenceInHours(Long unixStartTime, Long unixEndTime) {
        return (unixEndTime - unixStartTime) / 3600;
    }

    public static Long getDifferenceInSeconds(Timestamp start, Timestamp end) {
        return getDuration(start, end).getSeconds();
    }

    public static Long getDifferenceInMinutes(Timestamp start, Timestamp end) {
        return getDuration(start, end).toMinutes();
    }

    public static Long getDifferenceInHours(Timestamp start, Timestamp end) {
        return getDuration(start, end).toHours();
    }

    public static Duration getDuration(Timestamp start, Timestamp end) {
        return getDuration(start.toLocalDateTime(), end.toLocalDateTime());
    }

    public static Duration getDuration(LocalDateTime start, LocalDateTime end) {
        return Duration.between(start.withNano(0), end.withNano(0));
    }
}
