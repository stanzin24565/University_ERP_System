package edu.univ.erp.data;

import edu.univ.erp.domain.Section;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SectionDAO {

    public Section findById(int sectionId) {
        String sql = "SELECT * FROM sections WHERE section_id = ?";

        try (Connection conn = DatabaseConnection.getERPConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, sectionId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToSection(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    // FIXED: removed "AND status='ACTIVE'" because your DB has no status column
    public List<Section> findByInstructor(int instructorId) {
        String sql = "SELECT * FROM sections WHERE instructor_id = ? ORDER BY year DESC, semester";
        List<Section> sections = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getERPConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, instructorId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                sections.add(mapResultSetToSection(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sections;
    }


    // FIXED: removed status filter (status column doesn't exist in sections table)
    public List<Section> findByCourse(int courseId) {
        String sql = "SELECT * FROM sections WHERE course_id = ?";
        List<Section> sections = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getERPConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, courseId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                sections.add(mapResultSetToSection(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sections;
    }


    // FIXED: removed enrolled_count, status checks (columns don't exist in sections table)
    public List<Section> findAvailableSections() {
        String sql = "SELECT * FROM sections ORDER BY course_id, section_id";
        List<Section> sections = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getERPConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                sections.add(mapResultSetToSection(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sections;
    }


    public List<Section> findAllActive() {
        String sql = "SELECT s.*, c.code AS course_code, c.title AS course_title " +
                "FROM sections s JOIN courses c ON s.course_id = c.course_id " +
                "ORDER BY c.code, s.section_id";

        List<Section> sections = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getERPConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                sections.add(mapResultSetToSection(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return sections;
    }


    public boolean save(Section section) {
        if (section.getSectionId() > 0) {
            return update(section);
        } else {
            return insert(section);
        }
    }


    public boolean delete(int sectionId) {
        String query = "DELETE FROM sections WHERE section_id = ?";

        try (Connection conn = DatabaseConnection.getERPConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, sectionId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }



    // FIXED: The original INSERT had 8 columns but used 9 values.
    private boolean insert(Section section) {
        String sql = "INSERT INTO sections (course_id, instructor_id, day_time, room, capacity, semester, year) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getERPConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, section.getCourseId());
            stmt.setInt(2, section.getInstructorId());
            stmt.setString(3, section.getDayTime());
            stmt.setString(4, section.getRoom());
            stmt.setInt(5, section.getCapacity());
            stmt.setString(6, section.getSemester());
            stmt.setInt(7, section.getYear());

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


    // FIXED: removed enrolled_count + status (they do NOT exist in DB)
    private boolean update(Section section) {
        String sql = "UPDATE sections SET course_id = ?, instructor_id = ?, day_time = ?, room = ?, " +
                "capacity = ?, semester = ?, year = ? WHERE section_id = ?";

        try (Connection conn = DatabaseConnection.getERPConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, section.getCourseId());
            stmt.setInt(2, section.getInstructorId());
            stmt.setString(3, section.getDayTime());
            stmt.setString(4, section.getRoom());
            stmt.setInt(5, section.getCapacity());
            stmt.setString(6, section.getSemester());
            stmt.setInt(7, section.getYear());
            stmt.setInt(8, section.getSectionId());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    private Section mapResultSetToSection(ResultSet rs) throws SQLException {
        Section section = new Section(
                rs.getInt("section_id"),
                rs.getInt("course_id"),
                rs.getInt("instructor_id"),
                rs.getString("day_time"),
                rs.getString("room"),
                rs.getInt("capacity"),
                rs.getString("semester"),
                rs.getInt("year")
        );

        // Get the actual enrolled count from the enrollments table
        int sectionId = rs.getInt("section_id");
        int enrolledCount = getEnrolledCount(sectionId);
        section.setEnrolledCount(enrolledCount);

        return section;
    }

    // Helper method to get enrolled count for a section
    private int getEnrolledCount(int sectionId) {
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

    public boolean incrementEnrolledCount(int sectionId) {
        // Enrolled count is calculated dynamically from enrollments table, no-op needed
        return true;
    }

    public boolean decrementEnrolledCount(int sectionId) {
        // Enrolled count is calculated dynamically from enrollments table, no-op needed
        return true;
    }

}
