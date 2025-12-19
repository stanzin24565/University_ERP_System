package edu.univ.erp.domain;

public class Section {
    private int sectionId;
    private int courseId;
    private int instructorId;
    private String dayTime;
    private String room;
    private int capacity;
    private int enrolledCount;
    private String semester;
    private int year;
    private String status;
    private String instructorName; // ADD THIS FIELD

    // FIXED CONSTRUCTOR - changed instructorId from String to int
    public Section(int sectionId, int courseId, int instructorId, String dayTime,
                   String room, int capacity, String semester, int year) {
        this.sectionId = sectionId;
        this.courseId = courseId;
        this.instructorId = instructorId;
        this.dayTime = dayTime;
        this.room = room;
        this.capacity = capacity;
        this.enrolledCount = 0;
        this.semester = semester;
        this.year = year;
        this.status = "ACTIVE";
        this.instructorName = "Instructor"; // Default value
    }

    // Getters
    public int getSectionId() { return sectionId; }
    public int getCourseId() { return courseId; }
    public int getInstructorId() { return instructorId; }
    public String getDayTime() { return dayTime; }
    public String getRoom() { return room; }
    public int getCapacity() { return capacity; }
    public int getEnrolledCount() { return enrolledCount; }
    public String getSemester() { return semester; }
    public int getYear() { return year; }
    public String getStatus() { return status; }

    // ADD THESE MISSING GETTERS FOR COURSE PANEL
    public String getInstructorName() {
        return instructorName != null ? instructorName : "Instructor " + instructorId;
    }

    public int getAvailableSeats() {
        return capacity - enrolledCount;
    }

    // Setters
    public void setSectionId(int sectionId) { this.sectionId = sectionId; }
    public void setCourseId(int courseId) { this.courseId = courseId; }
    public void setInstructorId(int instructorId) { this.instructorId = instructorId; }
    public void setDayTime(String dayTime) { this.dayTime = dayTime; }
    public void setRoom(String room) { this.room = room; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public void setEnrolledCount(int enrolledCount) { this.enrolledCount = enrolledCount; }
    public void setSemester(String semester) { this.semester = semester; }
    public void setYear(int year) { this.year = year; }
    public void setStatus(String status) { this.status = status; }
    public void setInstructorName(String instructorName) { this.instructorName = instructorName; }

    public boolean hasAvailableSeats() {
        return enrolledCount < capacity;
    }

    @Override
    public String toString() {
        return String.format("Section{id=%d, courseId=%d, instructorId=%d, capacity=%d/%d}",
                sectionId, courseId, instructorId, enrolledCount, capacity);
    }
}