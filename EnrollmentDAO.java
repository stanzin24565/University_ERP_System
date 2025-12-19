package edu.univ.erp.data;

import edu.univ.erp.domain.Enrollment;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class EnrollmentDAO {

    public boolean createEnrollment(int studentId, int sectionId) {
        String sql = "INSERT INTO enrollments (student_id, section_id, status, enrolled_at) VALUES (?, ?, 'REGISTERED', NOW())";

        try (Connection conn = DatabaseConnection.getERPConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, studentId);
            stmt.setInt(2, sectionId);

            int result = stmt.executeUpdate();
            System.out.println("DEBUG: INSERT RESULT = " + result);

            return result > 0;

        } catch (SQLException e) {
            System.err.println("\n====== SQL ERROR WHILE INSERTING ENROLLMENT ======");
            e.printStackTrace();
            System.err.println("===================================================\n");
        }

        return false;
    }


    public boolean dropEnrollment(int enrollmentId) {
        String sql = "UPDATE enrollments SET status = 'DROPPED' WHERE enrollment_id = ? AND status = 'REGISTERED'";

        try (Connection conn = DatabaseConnection.getERPConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, enrollmentId);
            int result = stmt.executeUpdate();
            System.out.println("DEBUG: DROP enrollment result = " + result);
            return result > 0;
        } catch (SQLException e) {
            System.err.println("ERROR dropping enrollment: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public boolean hasActiveEnrollment(int studentId, int sectionId) {
        String sql = "SELECT COUNT(*) FROM enrollments WHERE student_id = ? AND section_id = ? AND status = 'REGISTERED'";

        try (Connection conn = DatabaseConnection.getERPConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, studentId);
            stmt.setInt(2, sectionId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<Enrollment> findByStudentId(int studentId) {
        // FIX: Only return ACTIVE enrollments (status = 'REGISTERED')
        String sql = "SELECT * FROM enrollments WHERE student_id = ? AND status = 'REGISTERED' ORDER BY enrolled_at DESC";
        List<Enrollment> enrollments = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getERPConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, studentId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                enrollments.add(mapResultSetToEnrollment(rs));
            }

            System.out.println("DEBUG: Found " + enrollments.size() + " ACTIVE enrollments for student " + studentId);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return enrollments;
    }

    public List<Enrollment> findBySectionId(int sectionId) {
        String sql = "SELECT * FROM enrollments WHERE section_id = ? ORDER BY enrolled_at DESC";
        List<Enrollment> enrollments = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getERPConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, sectionId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                enrollments.add(mapResultSetToEnrollment(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return enrollments;
    }

    public List<Enrollment> findActiveBySectionId(int sectionId) {
        String sql = "SELECT * FROM enrollments WHERE section_id = ? AND status = 'REGISTERED' ORDER BY enrolled_at DESC";
        List<Enrollment> enrollments = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getERPConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, sectionId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                enrollments.add(mapResultSetToEnrollment(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return enrollments;
    }

    public Enrollment findByStudentAndSection(int studentId, int sectionId) {
        String sql = "SELECT * FROM enrollments WHERE student_id = ? AND section_id = ? AND status = 'REGISTERED'";

        try (Connection conn = DatabaseConnection.getERPConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, studentId);
            stmt.setInt(2, sectionId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToEnrollment(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Enrollment findById(int enrollmentId) {
        String sql = "SELECT * FROM enrollments WHERE enrollment_id = ?";

        try (Connection conn = DatabaseConnection.getERPConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, enrollmentId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToEnrollment(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int countBySectionId(int sectionId) {
        String sql = "SELECT COUNT(*) FROM enrollments WHERE section_id = ? AND status = 'REGISTERED'";

        try (Connection conn = DatabaseConnection.getERPConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, sectionId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private Enrollment mapResultSetToEnrollment(ResultSet rs) throws SQLException {
        Timestamp enrolledAt = rs.getTimestamp("enrolled_at");

        Enrollment enrollment = new Enrollment(
                rs.getInt("enrollment_id"),
                rs.getInt("student_id"),
                rs.getInt("section_id"),
                rs.getString("status"),
                enrolledAt != null ? enrolledAt.toLocalDateTime() : null
        );

        // Note: dropped_at column doesn't exist in the database
        // The droppedAt field in the domain object is not populated from DB

        return enrollment;
    }
    // Add this method for debugging - shows ALL enrollments including dropped ones
    public List<Enrollment> findAllByStudentId(int studentId) {
        String sql = "SELECT * FROM enrollments WHERE student_id = ? ORDER BY enrolled_at DESC";
        List<Enrollment> enrollments = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getERPConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, studentId);
            ResultSet rs = stmt.executeQuery();

            int totalCount = 0;
            int activeCount = 0;
            while (rs.next()) {
                totalCount++;
                Enrollment enrollment = mapResultSetToEnrollment(rs);
                enrollments.add(enrollment);
                if ("REGISTERED".equals(enrollment.getStatus())) {
                    activeCount++;
                }
            }

            System.out.println("DEBUG: Student " + studentId + " has " + totalCount + " total enrollments, " + activeCount + " active");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return enrollments;
    }
    public List<String[]> getSectionEnrollmentDetails(int sectionId) {
        String sql = "SELECT e.enrollment_id, s.roll_no, u.username, e.status, e.enrolled_at " +
                "FROM enrollments e " +
                "JOIN students s ON e.student_id = s.user_id " +
                "JOIN users u ON s.user_id = u.user_id " +
                "WHERE e.section_id = ? " +
                "ORDER BY e.enrolled_at DESC";

        List<String[]> list = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getERPConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, sectionId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                list.add(new String[]{
                        String.valueOf(rs.getInt("enrollment_id")),
                        rs.getString("roll_no"),
                        rs.getString("username"),
                        rs.getString("status"),
                        String.valueOf(rs.getTimestamp("enrolled_at"))
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }


    // NEW: return enriched enrollment view for UI
    public List<String[]> findEnrollmentDetailsByStudentId(int studentId) {
        String sql =
                "SELECT e.enrollment_id, c.code AS course_code, c.title AS course_title, " +
                        "s.section_id, e.status, e.enrolled_at " +
                        "FROM enrollments e " +
                        "JOIN sections s ON e.section_id = s.section_id " +
                        "JOIN courses c ON s.course_id = c.course_id " +
                        "WHERE e.student_id = ? AND e.status = 'REGISTERED' " +
                        "ORDER BY e.enrolled_at DESC";

        List<String[]> list = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getERPConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, studentId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                list.add(new String[]{
                        String.valueOf(rs.getInt("enrollment_id")),
                        rs.getString("course_code"),
                        rs.getString("course_title"),
                        String.valueOf(rs.getInt("section_id")),
                        rs.getString("status"),
                        String.valueOf(rs.getTimestamp("enrolled_at"))
                });
            }

            System.out.println("DEBUG: Loaded " + list.size() +
                    " enriched enrollments for student " + studentId);

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }



}