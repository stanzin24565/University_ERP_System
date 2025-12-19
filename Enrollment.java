package edu.univ.erp.domain;

import java.time.LocalDateTime;

public class Enrollment {
    private int enrollmentId;
    private int studentId;
    private int sectionId;
    private String status;
    private LocalDateTime enrolledAt;
    private LocalDateTime droppedAt;

    public Enrollment(int enrollmentId, int studentId, int sectionId) {
        this.enrollmentId = enrollmentId;
        this.studentId = studentId;
        this.sectionId = sectionId;
        this.status = "REGISTERED";
        this.enrolledAt = LocalDateTime.now();
    }

    public Enrollment(int enrollmentId, int studentId, int sectionId, String status,
                      LocalDateTime enrolledAt) {
        this.enrollmentId = enrollmentId;
        this.studentId = studentId;
        this.sectionId = sectionId;
        this.status = status;
        this.enrolledAt = enrolledAt;
    }

    // Getters and Setters
    public int getEnrollmentId() { return enrollmentId; }
    public int getStudentId() { return studentId; }
    public int getSectionId() { return sectionId; }
    public String getStatus() { return status; }
    public LocalDateTime getEnrolledAt() { return enrolledAt; }
    public LocalDateTime getDroppedAt() { return droppedAt; }

    public void setStatus(String status) { this.status = status; }
    public void setDroppedAt(LocalDateTime droppedAt) { this.droppedAt = droppedAt; }

    public boolean isActive() {
        return "REGISTERED".equals(status);
    }

    @Override
    public String toString() {
        return String.format("Enrollment{id=%d, studentId=%d, sectionId=%d, status='%s'}",
                enrollmentId, studentId, sectionId, status);
    }
}