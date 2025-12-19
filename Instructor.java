package edu.univ.erp.domain;


public class Instructor {
   // private int instructorId;
    private int userId;
    private String department;
    private String designation;
    private String office;
    private String phone;

    public Instructor(int userId, String department) {
        //this.instructorId = instructorId;
        this.userId = userId;
        this.department = department;
    }

    // Getters and Setters
    public int getUserId() { return userId; }
    public String getDepartment() { return department; }
    public String getDesignation() { return designation; }
    public String getOffice() { return office; }
    public String getPhone() { return phone; }

    public void setDesignation(String designation) { this.designation = designation; }
    public void setOffice(String office) { this.office = office; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setDepartment(String department) { this.department = department; }

    @Override
    public String toString() {
        return String.format("Instructor{userId=%d, department='%s', designation='%s'}",
                userId, department, designation);
    }
}