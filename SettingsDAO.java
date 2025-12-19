package edu.univ.erp.data;

import edu.univ.erp.domain.Settings;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SettingsDAO {

    public Settings findByKey(String keyName) {
        String sql = "SELECT * FROM settings WHERE key_name = ?";

        try (Connection conn = DatabaseConnection.getERPConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, keyName);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToSettings(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Settings> findAll() {
        String sql = "SELECT * FROM settings ORDER BY key_name";
        List<Settings> settingsList = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getERPConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                settingsList.add(mapResultSetToSettings(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return settingsList;
    }

    public boolean save(Settings settings) {
        String sql = "INSERT INTO settings (key_name, value, description) VALUES (?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE value = ?, description = ?, updated_at = CURRENT_TIMESTAMP";

        try (Connection conn = DatabaseConnection.getERPConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, settings.getKeyName());
            stmt.setString(2, settings.getValue());
            stmt.setString(3, settings.getDescription());
            stmt.setString(4, settings.getValue());
            stmt.setString(5, settings.getDescription());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateValue(String keyName, String value) {
        String sql = "UPDATE settings SET value = ?, updated_at = CURRENT_TIMESTAMP WHERE key_name = ?";

        try (Connection conn = DatabaseConnection.getERPConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, value);
            stmt.setString(2, keyName);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private Settings mapResultSetToSettings(ResultSet rs) throws SQLException {
        Settings settings = new Settings(
                rs.getString("key_name"),
                rs.getString("value"),
                rs.getString("description")
        );

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            settings.setUpdatedAt(updatedAt.toLocalDateTime());
        }

        return settings;
    }
}