package cn.laoshini.dk.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * @author fagarine
 */
public class DateUtil {
    public static final DateTimeFormatter YYYY_MM_DD = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter YYYY_MM_DD_HH_MM_SS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final DateTimeFormatter YYYY_MM_DD_HH_MM_SS_SSS = DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    public static final DateTimeFormatter HH_MM_SS = DateTimeFormatter.ofPattern("HH:mm:ss");
    public static final DateTimeFormatter YYYYMMDD = DateTimeFormatter.ofPattern("yyyyMMdd");
    public static final DateTimeFormatter YYYYMMDDHHMMSS = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    public static final DateTimeFormatter YYYYMMDDHHMMSSSSS = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
    public static final DateTimeFormatter HHMMSS = DateTimeFormatter.ofPattern("HHmmss");
    private DateUtil() {
    }

    public static String format(LocalDateTime date, String pattern) {
        return DateTimeFormatter.ofPattern(pattern).format(date);
    }

    public static String format(LocalDate date, String pattern) {
        return DateTimeFormatter.ofPattern(pattern).format(date);
    }

    public static String format(LocalTime time, String pattern) {
        return DateTimeFormatter.ofPattern(pattern).format(time);
    }

    public static String formatToday(String pattern) {
        return DateTimeFormatter.ofPattern(pattern).format(LocalDate.now());
    }

    public static String formatNow(String pattern) {
        return DateTimeFormatter.ofPattern(pattern).format(LocalDateTime.now());
    }

    public static String formatNowTime(String pattern) {
        return DateTimeFormatter.ofPattern(pattern).format(LocalTime.now());
    }

    public static String todayFormat() {
        return YYYY_MM_DD.format(LocalDate.now());
    }

    public static String todayNumberFormat() {
        return YYYYMMDD.format(LocalDate.now());
    }

    public static String nowFormat() {
        return YYYY_MM_DD_HH_MM_SS.format(LocalDate.now());
    }

    public static String nowNumberFormat() {
        return YYYYMMDDHHMMSS.format(LocalDate.now());
    }

    public static String nowMilliFormat() {
        return YYYY_MM_DD_HH_MM_SS_SSS.format(LocalDate.now());
    }

    public static String nowMilliNumberFormat() {
        return YYYYMMDDHHMMSSSSS.format(LocalDate.now());
    }

    public static String nowTimeFormat() {
        return HH_MM_SS.format(LocalDate.now());
    }

    public static String nowTimeNumberFormat() {
        return HHMMSS.format(LocalDate.now());
    }

}
