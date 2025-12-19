package edu.univ.erp.util;
import edu.univ.erp.domain.*;
import edu.univ.erp.service.*;
import java.util.List;

public class ReportGenerator {

    // Generate student enrollment report
    public static String generateStudentEnrollmentReport(int studentId) {
        StudentService studentService = new StudentService();
        List<Enrollment> enrollments = studentService.getStudentEnrollments(studentId);

        StringBuilder report = new StringBuilder();
        report.append("STUDENT ENROLLMENT REPORT\n");
        report.append("==========================\n\n");

        for (Enrollment enrollment : enrollments) {
            report.append(String.format("Enrollment ID: %d\n", enrollment.getEnrollmentId()));
            report.append(String.format("Section ID: %d\n", enrollment.getSectionId()));
            report.append(String.format("Status: %s\n", enrollment.getStatus()));
            report.append(String.format("Enrolled Date: %s\n", enrollment.getEnrolledAt()));
            report.append("--------------------------\n");
        }

        return report.toString();
    }

    // Generate course catalog report
    public static String generateCourseCatalogReport() {
        StudentService studentService = new StudentService();
        List<Course> courses = studentService.getCourseCatalog();

        StringBuilder report = new StringBuilder();
        report.append("COURSE CATALOG REPORT\n");
        report.append("=====================\n\n");

        for (Course course : courses) {
            report.append(String.format("Course Code: %s\n", course.getCode()));
            report.append(String.format("Title: %s\n", course.getTitle()));
            report.append(String.format("Credits: %d\n", course.getCredits()));
            report.append(String.format("Status: %s\n", course.getStatus()));
            report.append("--------------------------\n");
        }

        return report.toString();
    }

    // Generate system usage report
    public static String generateSystemUsageReport() {
        StringBuilder report = new StringBuilder();
        report.append("SYSTEM USAGE REPORT\n");
        report.append("===================\n\n");
        report.append(String.format("Report Generated: %s\n", DateUtil.getCurrentDateTimeString()));
        report.append(String.format("System: %s\n", ConfigManager.getInstance().getSystemName()));
        report.append("This is a sample system usage report.\n");

        return report.toString();
    }
}