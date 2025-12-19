package edu.univ.erp.data;

import edu.univ.erp.domain.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    // ------------------------------
    // Find user by ID
    // ------------------------------
    public User findById(int userId) {
        String sql = "SELECT * FROM users_auth WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getAuthConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToUser(rs);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // ------------------------------
    // Find user by Username
    // ------------------------------
    public User findByUsername(String username) {
        String sql = "SELECT * FROM users_auth WHERE username = ?";

        try (Connection conn = DatabaseConnection.getAuthConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                System.out.println(rs.getString("role"));
                return mapResultSetToUser(rs);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // ------------------------------
    // Get all users
    // ------------------------------
    public List<User> findAll() {
        String sql = "SELECT * FROM users_auth ORDER BY user_id";
        List<User> users = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getAuthConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    // ------------------------------
    // Get users by role
    // ------------------------------
    public List<User> findByRole(String role) {
        String sql = "SELECT * FROM users_auth WHERE role = ? ORDER BY user_id";
        List<User> users = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getAuthConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, role);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    // ------------------------------
    // Save = insert or update
    // ------------------------------
    public boolean save(User user) {
        if (user.getUserId() > 0) {
            return update(user);
        } else {
            return insert(user);
        }
    }

    // ------------------------------
    // Insert user
    // ------------------------------
    // ------------------------------
// Insert user - FIXED VERSION
// ------------------------------
    private boolean insert(User user) {
        String sql = "INSERT INTO users_auth (username, password_hash, role, status) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getAuthConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPassword()); // ✅ FIXED: Use actual password from user
            stmt.setString(3, user.getRole());
            stmt.setString(4, user.getStatus());

            System.out.println("DEBUG INSERT: Attempting to insert user: " + user.getUsername() + ", role: " + user.getRole() + ", status: " + user.getStatus());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    user.setUserId(generatedKeys.getInt(1));
                    System.out.println("DEBUG INSERT: User inserted successfully with ID: " + user.getUserId());
                    return true;
                }
            }

        } catch (SQLException e) {
            System.err.println("ERROR inserting user: " + e.getMessage());

            // Check for specific error types
            if (e.getMessage().toLowerCase().contains("unique") || e.getMessage().toLowerCase().contains("duplicate")) {
                System.err.println("Username already exists: " + user.getUsername());
            } else if (e.getMessage().toLowerCase().contains("foreign key") || e.getMessage().toLowerCase().contains("constraint")) {
                System.err.println("Constraint violation for role: " + user.getRole());
            }

            e.printStackTrace();
        }
        return false;
    }

    // ------------------------------
// Update user - FIXED VERSION
// ------------------------------
    private boolean update(User user) {
        // Check if password is being updated
        boolean updatePassword = (user.getPassword() != null && !user.getPassword().isEmpty());

        String sql;
        
        // Try to update with new lockout fields first
        if (updatePassword) {
            sql = "UPDATE users_auth SET username = ?, password_hash = ?, role = ?, status = ?, failed_attempts = ?, locked_until = ? WHERE user_id = ?";
        } else {
            sql = "UPDATE users_auth SET username = ?, role = ?, status = ?, failed_attempts = ?, locked_until = ? WHERE user_id = ?";
        }

        try (Connection conn = DatabaseConnection.getAuthConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            int paramIndex = 1;
            stmt.setString(paramIndex++, user.getUsername());
            if (updatePassword) {
                stmt.setString(paramIndex++, user.getPassword());
            }
            stmt.setString(paramIndex++, user.getRole());
            stmt.setString(paramIndex++, user.getStatus());
            stmt.setInt(paramIndex++, user.getFailedAttempts() != null ? user.getFailedAttempts() : 0);
            stmt.setTimestamp(paramIndex++, user.getLockedUntil());
            stmt.setInt(paramIndex, user.getUserId());

            System.out.println("DEBUG UPDATE: Updating user ID: " + user.getUserId() + ", password update: " + updatePassword);

            int rowsAffected = stmt.executeUpdate();
            System.out.println("DEBUG UPDATE: Rows affected: " + rowsAffected);
            return rowsAffected > 0;

        } catch (SQLException e) {
            // If update fails due to missing columns, try old schema
            if (e.getMessage().toLowerCase().contains("unknown column")) {
                System.out.println("DEBUG: New columns not found, trying legacy update...");
                return updateLegacy(user, updatePassword);
            }
            
            System.err.println("ERROR updating user: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    // Legacy update for databases without lockout columns
    private boolean updateLegacy(User user, boolean updatePassword) {
        String sql;
        if (updatePassword) {
            sql = "UPDATE users_auth SET username = ?, password_hash = ?, role = ?, status = ? WHERE user_id = ?";
        } else {
            sql = "UPDATE users_auth SET username = ?, role = ?, status = ? WHERE user_id = ?";
        }

        try (Connection conn = DatabaseConnection.getAuthConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            int paramIndex = 1;
            stmt.setString(paramIndex++, user.getUsername());
            if (updatePassword) {
                stmt.setString(paramIndex++, user.getPassword());
            }
            stmt.setString(paramIndex++, user.getRole());
            stmt.setString(paramIndex++, user.getStatus());
            stmt.setInt(paramIndex, user.getUserId());

            System.out.println("DEBUG UPDATE (LEGACY): Updating user ID: " + user.getUserId());
            
            int rowsAffected = stmt.executeUpdate();
            System.out.println("DEBUG UPDATE (LEGACY): Rows affected: " + rowsAffected);
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("ERROR updating user (legacy): " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    // ------------------------------
// Delete user - ENHANCED VERSION
// ------------------------------
    public boolean delete(int userId) {
        String sql = "DELETE FROM users_auth WHERE user_id = ?";

        System.out.println("DEBUG DELETE: Attempting to delete user with ID: " + userId);

        try (Connection conn = DatabaseConnection.getAuthConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            int rowsAffected = stmt.executeUpdate();

            System.out.println("DEBUG DELETE: SQL executed, rows affected: " + rowsAffected);

            if (rowsAffected > 0) {
                System.out.println("SUCCESS: User with ID " + userId + " deleted successfully");
                return true;
            } else {
                System.out.println("FAILED: No user found with ID: " + userId + " or delete was blocked");

                // Let's verify if the user exists
                String checkSql = "SELECT COUNT(*) FROM users_auth WHERE user_id = ?";
                try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                    checkStmt.setInt(1, userId);
                    ResultSet rs = checkStmt.executeQuery();
                    if (rs.next()) {
                        int count = rs.getInt(1);
                        System.out.println("DEBUG: User existence check - COUNT: " + count);
                        if (count == 0) {
                            System.out.println("CONFIRMED: User with ID " + userId + " does not exist in database");
                        } else {
                            System.out.println("MYSTERY: User exists but delete failed - likely foreign key constraint");
                        }
                    }
                }
                return false;
            }

        } catch (SQLException e) {
            System.err.println("SQL ERROR deleting user ID " + userId + ": " + e.getMessage());
            System.err.println("SQL State: " + e.getSQLState());
            System.err.println("Error Code: " + e.getErrorCode());

            // Check for specific constraint violations
            String errorMsg = e.getMessage().toLowerCase();
            if (errorMsg.contains("foreign key") || errorMsg.contains("constraint")) {
                System.err.println("DELETE BLOCKED: Foreign key constraint violation");
                System.err.println("User has related records in other tables that must be deleted first");
            }
            if (errorMsg.contains("cannot delete") || errorMsg.contains("restrict")) {
                System.err.println("DELETE BLOCKED: Delete restriction in place");
            }

            e.printStackTrace();
            return false;
        }
    }
    // Authenticate user login
    public boolean authenticate(String username, String password) {
        String sql = "SELECT * FROM users_auth WHERE username = ? AND password_hash = ? AND status = 'ACTIVE'";

        try (Connection conn = DatabaseConnection.getAuthConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, password); // Should use hash
            ResultSet rs = stmt.executeQuery();

            return rs.next();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ------------------------------
    // Convert SQL row to User object
    // ------------------------------
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User(
                rs.getInt("user_id"),
                rs.getString("username"),
                rs.getString("password_hash"),
                rs.getString("role")
        );
        
        // Set additional fields (handle columns that might not exist yet)
        try {
            user.setFailedAttempts(rs.getInt("failed_attempts"));
        } catch (SQLException e) {
            user.setFailedAttempts(0); // Default if column doesn't exist
        }
        
        try {
            user.setLockedUntil(rs.getTimestamp("locked_until"));
        } catch (SQLException e) {
            user.setLockedUntil(null); // Default if column doesn't exist
        }
        
        return user;
    }
}
