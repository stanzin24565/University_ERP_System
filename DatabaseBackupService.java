package edu.univ.erp.service;

import edu.univ.erp.data.DatabaseConnection;
import edu.univ.erp.util.FileUtil;
import java.io.File;
import java.sql.Connection;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;


public class DatabaseBackupService {
    private static final String BACKUP_DIR = "backups/";
    private static final String MYSQL_PATH = "mysql";
    private static final String MYSQLDUMP_PATH = "mysqldump";

    public DatabaseBackupService() {
        // Create backup directory if it doesn't exist
        File backupDir = new File(BACKUP_DIR);
        if (!backupDir.exists()) {
            backupDir.mkdirs();
        }
    }

    /**
     * Perform database backup for both Auth and ERP databases
     */
    public BackupResult performBackup() {
        try {
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String authBackupFile = BACKUP_DIR + "auth_backup_" + timestamp + ".sql";
            String erpBackupFile = BACKUP_DIR + "erp_backup_" + timestamp + ".sql";

            boolean authSuccess = backupDatabase("university_auth_db", authBackupFile);
            boolean erpSuccess = backupDatabase("university_erp_db", erpBackupFile);

            if (authSuccess && erpSuccess) {
                return new BackupResult(true, "Backup completed successfully",
                        List.of(authBackupFile, erpBackupFile));
            } else {
                return new BackupResult(false,
                        "Backup partially failed. Auth: " + authSuccess + ", ERP: " + erpSuccess,
                        new ArrayList<>());
            }
        } catch (Exception e) {
            return new BackupResult(false, "Backup failed: " + e.getMessage(), new ArrayList<>());
        }
    }

    /**
     * Restore database from backup files
     */
    public boolean performRestore(String authBackupFile, String erpBackupFile) {
        try {
            boolean authRestored = restoreDatabase("university_auth_db", authBackupFile);
            boolean erpRestored = restoreDatabase("university_erp_db", erpBackupFile);

            return authRestored && erpRestored;
        } catch (Exception e) {
            System.err.println("Restore failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get list of available backup files
     */
    public List<BackupFileInfo> getAvailableBackups() {
        List<BackupFileInfo> backups = new ArrayList<>();
        File backupDir = new File(BACKUP_DIR);

        if (backupDir.exists() && backupDir.isDirectory()) {
            File[] files = backupDir.listFiles((dir, name) -> name.endsWith(".sql"));
            if (files != null) {
                for (File file : files) {
                    backups.add(new BackupFileInfo(
                            file.getName(),
                            file.length(),
                            new Date(file.lastModified()),
                            file.getAbsolutePath()
                    ));
                }
            }
        }

        return backups;
    }

    private boolean backupDatabase(String databaseName, String outputFile) {
        try {
            String command = String.format(
                    "%s -u %s -p%s %s > %s",
                    MYSQLDUMP_PATH,
                    "root", // Replace with your DB username
                    "stanzin1809", // Replace with your DB password
                    databaseName,
                    outputFile
            );

            Process process = Runtime.getRuntime().exec(new String[]{"bash", "-c", command});
            int exitCode = process.waitFor();

            return exitCode == 0;
        } catch (Exception e) {
            System.err.println("Backup failed for " + databaseName + ": " + e.getMessage());
            return false;
        }
    }

    private boolean restoreDatabase(String databaseName, String backupFile) {
        try {
            // First, drop and recreate the database
            try (Connection conn = DatabaseConnection.getERPConnection();
                 Statement stmt = conn.createStatement()) {

                stmt.execute("DROP DATABASE IF EXISTS " + databaseName);
                stmt.execute("CREATE DATABASE " + databaseName);
            }

            // Restore from backup file
            String command = String.format(
                    "%s -u %s -p%s %s < %s",
                    MYSQL_PATH,
                    "root", // Replace with your DB username
                    "stanzin1809", // Replace with your DB password
                    databaseName,
                    backupFile
            );

            Process process = Runtime.getRuntime().exec(new String[]{"bash", "-c", command});
            int exitCode = process.waitFor();

            return exitCode == 0;
        } catch (Exception e) {
            System.err.println("Restore failed for " + databaseName + ": " + e.getMessage());
            return false;
        }
    }

    // Inner classes for backup results and file info
    public static class BackupResult {
        private final boolean success;
        private final String message;
        private final List<String> backupFiles;

        public BackupResult(boolean success, String message, List<String> backupFiles) {
            this.success = success;
            this.message = message;
            this.backupFiles = backupFiles;
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public List<String> getBackupFiles() { return backupFiles; }
    }

    public static class BackupFileInfo {
        private final String filename;
        private final long size;
        private final Date createdDate;
        private final String filePath;

        public BackupFileInfo(String filename, long size, Date createdDate, String filePath) {
            this.filename = filename;
            this.size = size;
            this.createdDate = createdDate;
            this.filePath = filePath;
        }

        public String getFilename() { return filename; }
        public long getSize() { return size; }
        public Date getCreatedDate() { return createdDate; }
        public String getFilePath() { return filePath; }

        public String getFormattedSize() {
            if (size < 1024) return size + " B";
            else if (size < 1024 * 1024) return String.format("%.1f KB", size / 1024.0);
            else return String.format("%.1f MB", size / (1024.0 * 1024.0));
        }
    }
}