package edu.univ.erp.data;

import edu.univ.erp.domain.Instructor;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InstructorDAO {

    public Instructor findById(int userId) {
        String sql = "SELECT * FROM instructors WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getERPConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToInstructor(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Instructor> findAll() {
        String sql = "SELECT * FROM instructors";
        List<Instructor> instructors = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getERPConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                instructors.add(mapResultSetToInstructor(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return instructors;
    }

    public boolean save(Instructor instructor) {
        String sql = "INSERT INTO instructors (user_id, department, designation, office, phone) " +
                "VALUES (?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE department=?, designation=?, office=?, phone=?";

        try (Connection conn = DatabaseConnection.getERPConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, instructor.getUserId());
            stmt.setString(2, instructor.getDepartment());
            stmt.setString(3, instructor.getDesignation());
            stmt.setString(4, instructor.getOffice());
            stmt.setString(5, instructor.getPhone());
            stmt.setString(6, instructor.getDepartment());
            stmt.setString(7, instructor.getDesignation());
            stmt.setString(8, instructor.getOffice());
            stmt.setString(9, instructor.getPhone());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private Instructor mapResultSetToInstructor(ResultSet rs) throws SQLException {
        Instructor instructor = new Instructor(
                /// ///////   private int instructorId;
                rs.getInt("user_id"),
                rs.getString("department")
        );
        instructor.setDesignation(rs.getString("designation"));
        instructor.setOffice(rs.getString("office"));
        instructor.setPhone(rs.getString("phone"));
        return instructor;
    }
}