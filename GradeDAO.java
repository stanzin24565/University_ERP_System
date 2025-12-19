package edu.univ.erp.data;

import edu.univ.erp.domain.Grade;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * FINAL, CLEAN, FULLY-CORRECTED GRADE DAO
 * Works with InstructorService and UI panels without errors
 */
public class GradeDAO {

    // ---------------------------------------------------------
    //  FIND BY ID
    // ---------------------------------------------------------
    public Grade findById(int gradeId) {
        String sql = "SELECT * FROM grades WHERE grade_id = ?";

        try (Connection conn = DatabaseConnection.getERPConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, gradeId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToGrade(rs);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // ---------------------------------------------------------
    //  FIND BY ENROLLMENT ID
    // ---------------------------------------------------------
    public List<Grade> findByEnrollmentId(int enrollmentId) {
        String sql = "SELECT * FROM grades WHERE enrollment_id = ? ORDER BY component";
        List<Grade> grades = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getERPConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, enrollmentId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                grades.add(mapResultSetToGrade(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return grades;
    }

    // ---------------------------------------------------------
    //  SAVE (INSERT OR UPDATE)
    // ---------------------------------------------------------
    public boolean save(Grade grade) {
        // Check if this component already exists for this enrollment
        String checkSql = "SELECT grade_id FROM grades WHERE enrollment_id = ? AND component = ?";
        
        try (Connection conn = DatabaseConnection.getERPConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            
            checkStmt.setInt(1, grade.getEnrollmentId());
            checkStmt.setString(2, grade.getComponent());
            ResultSet rs = checkStmt.executeQuery();
            
            if (rs.next()) {
                // Component exists, so update it
                int existingId = rs.getInt("grade_id");
                grade.setGradeId(existingId);
                System.out.println("[GradeDAO.save] Found existing component, updating gradeId=" + existingId);
                return update(grade);
            } else {
                // Component doesn't exist, so insert
                System.out.println("[GradeDAO.save] Component doesn't exist, inserting new");
                return insert(grade);
            }
            
        } catch (SQLException e) {
            System.err.println("[GradeDAO.save] ERROR checking existing component: " + e.getMessage());
            e.printStackTrace();
            // Fall back to insert
            return insert(grade);
        }
    }

    // ---------------------------------------------------------
    //  INSERT (NEW GRADE COMPONENT)
    // ---------------------------------------------------------
    private boolean insert(Grade grade) {

        String sql = "INSERT INTO grades (enrollment_id, component, max_score, score) " +
                "VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getERPConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, grade.getEnrollmentId());
            stmt.setString(2, grade.getComponent());
            stmt.setDouble(3, grade.getMaxScore());
            stmt.setObject(4, grade.getScore());

            int rows = stmt.executeUpdate();
            System.out.println("[GradeDAO.insert] Inserted grade for enrollment " + grade.getEnrollmentId() + ", component=" + grade.getComponent() + ", score=" + grade.getScore() + ", rows=" + rows);
            return rows > 0;

        } catch (SQLException e) {
            System.err.println("[GradeDAO.insert] ERROR inserting grade: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    // ---------------------------------------------------------
    //  UPDATE EXISTING COMPONENT
    // ---------------------------------------------------------
    private boolean update(Grade grade) {

        String sql = "UPDATE grades SET score = ? WHERE grade_id = ?";

        try (Connection conn = DatabaseConnection.getERPConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, grade.getScore());
            stmt.setInt(2, grade.getGradeId());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("[GradeDAO.update] ERROR updating grade: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    // ---------------------------------------------------------
    //  DELETE BY GRADE ID
    // ---------------------------------------------------------
    public boolean delete(int gradeId) {
        String sql = "DELETE FROM grades WHERE grade_id = ?";

        try (Connection conn = DatabaseConnection.getERPConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, gradeId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    // ---------------------------------------------------------
    //  DELETE BY ENROLLMENT + COMPONENT   (REQUIRED BY InstructorService)
    // ---------------------------------------------------------
    public boolean deleteComponent(int enrollmentId, String component) {

        String sql = "DELETE FROM grades WHERE enrollment_id = ? AND component = ?";

        try (Connection conn = DatabaseConnection.getERPConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, enrollmentId);
            stmt.setString(2, component);
            int rows = stmt.executeUpdate();
            System.out.println("[GradeDAO.deleteComponent] Deleted component '" + component + "' for enrollment " + enrollmentId + ", rows=" + rows);
            return true;

        } catch (SQLException e) {
            System.err.println("[GradeDAO.deleteComponent] ERROR: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    // ---------------------------------------------------------
    //  MAPPER
    // ---------------------------------------------------------
    private Grade mapResultSetToGrade(ResultSet rs) throws SQLException {

        Grade grade = new Grade(
                rs.getInt("grade_id"),
                rs.getInt("enrollment_id"),
                rs.getString("component"),
                rs.getDouble("max_score"),
                0.0 // weightage - not stored in DB, default to 0
        );

        Object scoreObj = rs.getObject("score");
        if (scoreObj != null) {
            grade.setScore(((Number) scoreObj).doubleValue());
        }

        return grade;
    }
}
