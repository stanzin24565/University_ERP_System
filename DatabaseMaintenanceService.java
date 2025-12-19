package edu.univ.erp.service;





import edu.univ.erp.data.DatabaseConnection;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;


public class DatabaseMaintenanceService {

    /**
     * Optimize all database tables
     */
    public MaintenanceResult optimizeTables() {
        List<String> results = new ArrayList<>();
        boolean success = true;

        try (Connection conn = DatabaseConnection.getERPConnection();
             Statement stmt = conn.createStatement()) {

            // Get all tables
            List<String> tables = getDatabaseTables(conn);

            for (String table : tables) {
                try {
                    stmt.execute("OPTIMIZE TABLE " + table);
                    results.add("✓ Optimized: " + table);
                } catch (Exception e) {
                    results.add("✗ Failed to optimize: " + table + " - " + e.getMessage());
                    success = false;
                }
            }

            return new MaintenanceResult(success, "Table optimization completed", results);

        } catch (Exception e) {
            return new MaintenanceResult(false, "Optimization failed: " + e.getMessage(), new ArrayList<>());
        }
    }

    /**
     * Check database integrity and foreign key relationships
     */
    public MaintenanceResult checkDatabaseIntegrity() {
        List<String> results = new ArrayList<>();
        boolean success = true;

        try (Connection conn = DatabaseConnection.getERPConnection();
             Statement stmt = conn.createStatement()) {

            // Check table structures
            results.add("=== Table Structure Checks ===");
            List<String> tables = getDatabaseTables(conn);
            for (String table : tables) {
                try {
                    ResultSet rs = stmt.executeQuery("CHECK TABLE " + table);
                    if (rs.next()) {
                        String msgType = rs.getString("Msg_type");
                        String msgText = rs.getString("Msg_text");
                        if ("status".equals(msgType)) {
                            results.add("✓ " + table + ": " + msgText);
                        } else {
                            results.add("✗ " + table + ": " + msgText);
                            success = false;
                        }
                    }
                } catch (Exception e) {
                    results.add("✗ " + table + ": Check failed - " + e.getMessage());
                    success = false;
                }
            }

            // Check foreign key relationships
            results.add("\n=== Foreign Key Checks ===");
            checkForeignKeyRelationships(conn, results);

            return new MaintenanceResult(success, "Database integrity check completed", results);

        } catch (Exception e) {
            return new MaintenanceResult(false, "Integrity check failed: " + e.getMessage(), new ArrayList<>());
        }
    }

    /**
     * Get database size information
     */
    public DatabaseSizeInfo getDatabaseSize() {
        try (Connection conn = DatabaseConnection.getERPConnection();
             Statement stmt = conn.createStatement()) {

            String query = "SELECT " +
                    "table_schema as 'Database', " +
                    "SUM(data_length + index_length) as 'Size' " +
                    "FROM information_schema.TABLES " +
                    "WHERE table_schema IN ('university_auth_db', 'university_erp_db') " +
                    "GROUP BY table_schema";

            ResultSet rs = stmt.executeQuery(query);
            long totalSize = 0;
            List<DatabaseSize> databaseSizes = new ArrayList<>();

            while (rs.next()) {
                String dbName = rs.getString("Database");
                long size = rs.getLong("Size");
                totalSize += size;
                databaseSizes.add(new DatabaseSize(dbName, size));
            }

            return new DatabaseSizeInfo(totalSize, databaseSizes);

        } catch (Exception e) {
            System.err.println("Error getting database size: " + e.getMessage());
            return new DatabaseSizeInfo(0, new ArrayList<>());
        }
    }

    private List<String> getDatabaseTables(Connection conn) throws Exception {
        List<String> tables = new ArrayList<>();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SHOW TABLES")) {

            while (rs.next()) {
                tables.add(rs.getString(1));
            }
        }
        return tables;
    }

    private void checkForeignKeyRelationships(Connection conn, List<String> results) throws Exception {
        String query = "SELECT " +
                "TABLE_NAME, COLUMN_NAME, CONSTRAINT_NAME, REFERENCED_TABLE_NAME, REFERENCED_COLUMN_NAME " +
                "FROM information_schema.KEY_COLUMN_USAGE " +
                "WHERE REFERENCED_TABLE_SCHEMA = 'university_erp_db' " +
                "AND REFERENCED_TABLE_NAME IS NOT NULL";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            boolean hasForeignKeys = false;
            while (rs.next()) {
                hasForeignKeys = true;
                String table = rs.getString("TABLE_NAME");
                String column = rs.getString("COLUMN_NAME");
                String referencedTable = rs.getString("REFERENCED_TABLE_NAME");
                String referencedColumn = rs.getString("REFERENCED_COLUMN_NAME");

                results.add("✓ FK: " + table + "." + column + " → " +
                        referencedTable + "." + referencedColumn);
            }

            if (!hasForeignKeys) {
                results.add("No foreign key relationships found");
            }
        }
    }

    // Inner classes for maintenance results
    public static class MaintenanceResult {
        private final boolean success;
        private final String summary;
        private final List<String> details;

        public MaintenanceResult(boolean success, String summary, List<String> details) {
            this.success = success;
            this.summary = summary;
            this.details = details;
        }

        public boolean isSuccess() { return success; }
        public String getSummary() { return summary; }
        public List<String> getDetails() { return details; }
    }

    public static class DatabaseSizeInfo {
        private final long totalSize;
        private final List<DatabaseSize> databaseSizes;

        public DatabaseSizeInfo(long totalSize, List<DatabaseSize> databaseSizes) {
            this.totalSize = totalSize;
            this.databaseSizes = databaseSizes;
        }

        public long getTotalSize() { return totalSize; }
        public List<DatabaseSize> getDatabaseSizes() { return databaseSizes; }

        public String getFormattedTotalSize() {
            return formatSize(totalSize);
        }
    }

    public static class DatabaseSize {
        private final String databaseName;
        private final long size;

        public DatabaseSize(String databaseName, long size) {
            this.databaseName = databaseName;
            this.size = size;
        }

        public String getDatabaseName() { return databaseName; }
        public long getSize() { return size; }

        public String getFormattedSize() {
            return formatSize(size);
        }
    }

    private static String formatSize(long size) {
        if (size < 1024) return size + " B";
        else if (size < 1024 * 1024) return String.format("%.1f KB", size / 1024.0);
        else return String.format("%.1f MB", size / (1024.0 * 1024.0));
    }
}