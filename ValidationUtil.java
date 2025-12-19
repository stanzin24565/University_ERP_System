package edu.univ.erp.util;
import java.util.regex.Pattern;

public class ValidationUtil {

    // Email validation
    public static boolean isValidEmail(String email) {
        if (email == null) return false;
        String emailRegex = "^[a-zA-Z0-9_+&-]+(?:\\.[a-zA-Z0-9_+&-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return Pattern.compile(emailRegex).matcher(email).matches();
    }

    // Phone number validation
    public static boolean isValidPhone(String phone) {
        if (phone == null) return false;
        return phone.matches("^[0-9]{10,15}$");
    }

    // Name validation (letters and spaces only)
    public static boolean isValidName(String name) {
        if (name == null) return false;
        return name.matches("^[a-zA-Z\\s]{2,50}$");
    }

    // Roll number validation
    public static boolean isValidRollNumber(String rollNo) {
        if (rollNo == null) return false;
        return rollNo.matches("^[A-Za-z0-9-]{3,20}$");
    }

    // Course code validation
    public static boolean isValidCourseCode(String code) {
        if (code == null) return false;
        return code.matches("^[A-Z]{2,4}[0-9]{3,4}$");
    }

    // Year validation
    public static boolean isValidYear(int year) {
        return year >= 1 && year <= 5;
    }

    // Credit validation
    public static boolean isValidCredits(int credits) {
        return credits >= 1 && credits <= 6;
    }

    // Grade validation
    public static boolean isValidGrade(double grade) {
        return grade >= 0 && grade <= 100;
    }

    // Username validation
    public static boolean isValidUsername(String username) {
        if (username == null) return false;
        return username.matches("^[a-zA-Z0-9_]{3,20}$");
    }

    // Date validation (YYYY-MM-DD format)
    public static boolean isValidDate(String date) {
        if (date == null) return false;
        return date.matches("^\\d{4}-\\d{2}-\\d{2}$");
    }
}