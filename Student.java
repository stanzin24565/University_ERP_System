package edu.univ.erp.domain;

public class Student {
    private int userId;
    private String rollNo;
    private String program;
    private int year;
    private String email;
    private String phone;

    private int studentId;

    private String firstName;
    private String lastName;




    // Constructors, getters, and setters
    public Student() {}

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }








    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }



    public Student(int userId, String rollNo, String program, int year) {
        this.userId = userId;
        this.rollNo = rollNo;
        this.program = program;
        this.year = year;
    }

    // Getters and Setters
    public int getUserId() { return userId; }
    public String getRollNo() { return rollNo; }
    public String getProgram() { return program; }
    public int getYear() { return year; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }

    public void setEmail(String email) { this.email = email; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setProgram(String program) { this.program = program; }
    public void setYear(int year) { this.year = year; }

    @Override
    public String toString() {
        return String.format("Student{userId=%d, rollNo='%s', program='%s', year=%d}",
                userId, rollNo, program, year);
    }
}