package edu.univ.erp.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class DateUtil {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Format LocalDate to string
    public static String formatDate(LocalDate date) {
        return date != null ? date.format(DATE_FORMATTER) : "";
    }

    // Format LocalDateTime to string
    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATETIME_FORMATTER) : "";
    }

    // Parse string to LocalDate
    public static LocalDate parseDate(String dateString) {
        try {
            return LocalDate.parse(dateString, DATE_FORMATTER);
        } catch (Exception e) {
            return null;
        }
    }

    // Parse string to LocalDateTime
    public static LocalDateTime parseDateTime(String dateTimeString) {
        try {
            return LocalDateTime.parse(dateTimeString, DATETIME_FORMATTER);
        } catch (Exception e) {
            return null;
        }
    }

    // Get current date as string
    public static String getCurrentDateString() {
        return LocalDate.now().format(DATE_FORMATTER);
    }

    // Get current datetime as string
    public static String getCurrentDateTimeString() {
        return LocalDateTime.now().format(DATETIME_FORMATTER);
    }

    // Calculate age from birth date
    public static int calculateAge(LocalDate birthDate) {
        return (int) ChronoUnit.YEARS.between(birthDate, LocalDate.now());
    }

    // Check if date is in the past
    public static boolean isPastDate(LocalDate date) {
        return date.isBefore(LocalDate.now());
    }

    // Check if date is in the future
    public static boolean isFutureDate(LocalDate date) {
        return date.isAfter(LocalDate.now());
    }

    // Get semester based on current month
    public static String getCurrentSemester() {
        int month = LocalDate.now().getMonthValue();
        if (month >= 1 && month <= 5) {
            return "SPRING";
        } else if (month >= 6 && month <= 7) {
            return "SUMMER";
        } else {
            return "FALL";
        }
    }

    // Get academic year
    public static int getCurrentAcademicYear() {
        return LocalDate.now().getYear();
    }
}
