package edu.univ.erp.domain;

import edu.univ.erp.util.ValidationUtil;

public class Course {
    private int courseId;
    private String code;
    private String title;
    private int credits;
    private String description;
    private String status;

    // Constructors
    public Course() {
        this.status = "ACTIVE";
    }

    public Course(int courseId, String code, String title, int credits) {
        this.courseId = courseId;
        this.code = code;
        this.title = title;
        this.credits = credits;
        this.status = "ACTIVE";
    }

    public Course(int courseId, String code, String title, int credits, String description) {
        this.courseId = courseId;
        this.code = code;
        this.title = title;
        this.credits = credits;
        this.description = description;
        this.status = "ACTIVE";
    }

    // Main setCourse method - sets all fields at once
    public boolean setCourse(String code, String title, int credits, String description, String status) {
        if (!validateCourseData(code, title, credits)) {
            return false;
        }

        this.code = code;
        this.title = title;
        this.credits = credits;
        this.description = description != null ? description : "";
        this.status = status != null ? status : "ACTIVE";

        return true;
    }

    // Overloaded setCourse without description
    public boolean setCourse(String code, String title, int credits) {
        return setCourse(code, title, credits, "", "ACTIVE");
    }

    // Overloaded setCourse without description and status
    public boolean setCourse(String code, String title, int credits, String description) {
        return setCourse(code, title, credits, description, "ACTIVE");
    }

    // Set course from another course object (copy constructor alternative)
    public boolean setCourse(Course otherCourse) {
        if (otherCourse == null) {
            return false;
        }

        return setCourse(
                otherCourse.getCode(),
                otherCourse.getTitle(),
                otherCourse.getCredits(),
                otherCourse.getDescription(),
                otherCourse.getStatus()
        );
    }

    // Set course with validation and error message
    public boolean setCourseWithValidation(String code, String title, int credits, String description, String status) {
        StringBuilder errorMessage = new StringBuilder();

        if (!ValidationUtil.isValidCourseCode(code)) {
            errorMessage.append("Invalid course code. ");
        }

        if (title == null || title.trim().isEmpty()) {
            errorMessage.append("Course title cannot be empty. ");
        } else if (title.length() > 100) {
            errorMessage.append("Course title too long (max 100 chars). ");
        }

        if (!ValidationUtil.isValidCredits(credits)) {
            errorMessage.append("Credits must be between 1 and 6. ");
        }

        if (description != null && description.length() > 500) {
            errorMessage.append("Description too long (max 500 chars). ");
        }

        if (status != null && !isValidStatus(status)) {
            errorMessage.append("Invalid status. Must be ACTIVE, INACTIVE, or ARCHIVED. ");
        }

        if (errorMessage.length() > 0) {
            throw new IllegalArgumentException("Course validation failed: " + errorMessage.toString());
        }

        return setCourse(code, title, credits, description, status);
    }

    // Set course for update (preserves ID)
    public boolean updateCourse(String code, String title, int credits, String description, String status) {
        if (this.courseId <= 0) {
            throw new IllegalStateException("Cannot update course without valid ID");
        }

        return setCourse(code, title, credits, description, status);
    }

    // Partial update methods
    public void setCourseCode(String code) {
        if (!ValidationUtil.isValidCourseCode(code)) {
            throw new IllegalArgumentException("Invalid course code format");
        }
        this.code = code;
    }

    public void setCourseTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Course title cannot be null or empty");
        }
        if (title.length() > 100) {
            throw new IllegalArgumentException("Course title cannot exceed 100 characters");
        }
        this.title = title;
    }

    public void setCourseCredits(int credits) {
        if (!ValidationUtil.isValidCredits(credits)) {
            throw new IllegalArgumentException("Credits must be between 1 and 6");
        }
        this.credits = credits;
    }

    public void setCourseDescription(String description) {
        if (description != null && description.length() > 500) {
            throw new IllegalArgumentException("Description cannot exceed 500 characters");
        }
        this.description = description;
    }

    public void setCourseStatus(String status) {
        if (!isValidStatus(status)) {
            throw new IllegalArgumentException("Status must be ACTIVE, INACTIVE, or ARCHIVED");
        }
        this.status = status;
    }

    // Bulk setter for multiple fields with validation
    public void setCourseFields(String code, String title, Integer credits, String description, String status) {
        if (code != null) setCourseCode(code);
        if (title != null) setCourseTitle(title);
        if (credits != null) setCourseCredits(credits);
        if (description != null) setCourseDescription(description);
        if (status != null) setCourseStatus(status);
    }

    // Validation helper methods
    private boolean validateCourseData(String code, String title, int credits) {
        if (!ValidationUtil.isValidCourseCode(code)) {
            return false;
        }

        if (title == null || title.trim().isEmpty()) {
            return false;
        }

        if (!ValidationUtil.isValidCredits(credits)) {
            return false;
        }

        return true;
    }

    private boolean isValidStatus(String status) {
        return status != null &&
                (status.equals("ACTIVE") || status.equals("INACTIVE") || status.equals("ARCHIVED"));
    }

    // Getters and Setters
    public int getCourseId() { return courseId; }
    public String getCode() { return code; }
    public String getTitle() { return title; }
    public int getCredits() { return credits; }
    public String getDescription() { return description; }
    public String getStatus() { return status; }

    public void setCourseId(int courseId) {
        if (courseId < 0) {
            throw new IllegalArgumentException("Course ID cannot be negative");
        }
        this.courseId = courseId;
    }

    // Traditional setters with validation
    public void setCode(String code) {
        setCourseCode(code);
    }

    public void setTitle(String title) {
        setCourseTitle(title);
    }

    public void setCredits(int credits) {
        setCourseCredits(credits);
    }

    public void setDescription(String description) {
        setCourseDescription(description);
    }

    public void setStatus(String status) {
        setCourseStatus(status);
    }

    // Business logic methods
    public boolean isActive() {
        return "ACTIVE".equals(status);
    }

    public boolean canBeDeleted() {
        return !"ACTIVE".equals(status);
    }

    public void activate() {
        this.status = "ACTIVE";
    }

    public void deactivate() {
        this.status = "INACTIVE";
    }

    public void archive() {
        this.status = "ARCHIVED";
    }

    // Validation method
    public boolean isValid() {
        return validateCourseData(code, title, credits) && isValidStatus(status);
    }

    // Get validation errors
    public String getValidationErrors() {
        StringBuilder errors = new StringBuilder();

        if (!ValidationUtil.isValidCourseCode(code)) {
            errors.append("Invalid course code. ");
        }

        if (title == null || title.trim().isEmpty()) {
            errors.append("Course title is required. ");
        }

        if (!ValidationUtil.isValidCredits(credits)) {
            errors.append("Invalid credits. ");
        }

        if (!isValidStatus(status)) {
            errors.append("Invalid status. ");
        }

        return errors.toString().trim();
    }

    @Override
    public String toString() {
        return String.format("Course{id=%d, code='%s', title='%s', credits=%d, status='%s'}",
                courseId, code, title, credits, status);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Course course = (Course) o;
        return courseId == course.courseId;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(courseId);
    }
}