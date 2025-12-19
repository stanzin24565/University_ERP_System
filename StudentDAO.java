package edu.univ.erp.data;

import edu.univ.erp.domain.Student;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StudentDAO {

    public static Student findById(int userId) {
        String sql = "SELECT * FROM students WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getERPConnection(); // Use ERP database
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToStudent(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Student> findAll() {
        String sql = "SELECT * FROM students";
        List<Student> students = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getERPConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                students.add(mapResultSetToStudent(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return students;
    }

    public boolean save(Student student) {
        String sql = "INSERT INTO students (user_id, roll_no, program, year, email, phone) " +
                "VALUES (?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE program=?, year=?, email=?, phone=?";

        try (Connection conn = DatabaseConnection.getERPConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, student.getUserId());
            stmt.setString(2, student.getRollNo());
            stmt.setString(3, student.getProgram());
            stmt.setInt(4, student.getYear());
            stmt.setString(5, student.getEmail());
            stmt.setString(6, student.getPhone());
            stmt.setString(7, student.getProgram());
            stmt.setInt(8, student.getYear());
            stmt.setString(9, student.getEmail());
            stmt.setString(10, student.getPhone());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static Student mapResultSetToStudent(ResultSet rs) throws SQLException {
        Student student = new Student(
                rs.getInt("user_id"),
                rs.getString("roll_no"),
                rs.getString("program"),
                rs.getInt("year")
        );
        student.setEmail(rs.getString("email"));
        student.setPhone(rs.getString("phone"));
        return student;
    }
}