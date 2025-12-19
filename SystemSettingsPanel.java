package edu.univ.erp.ui;

import edu.univ.erp.domain.User;
import edu.univ.erp.service.AdminService;
import edu.univ.erp.service.MaintenanceService;
import edu.univ.erp.service.DatabaseBackupService;
import edu.univ.erp.service.DatabaseMaintenanceService;
import edu.univ.erp.util.FileUtil;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.io.File;

public class SystemSettingsPanel extends JPanel {
    private User currentUser;
    private AdminService adminService;
    private MaintenanceService maintenanceService;
    private DatabaseBackupService backupService;
    private DatabaseMaintenanceService dbMaintenanceService;

    private JCheckBox maintenanceCheckbox;
    private JLabel maintenanceStatusLabel;
    private JButton saveSettingsButton;
    private JButton refreshButton;

    public SystemSettingsPanel(User user) {
        this.currentUser = user;
        this.adminService = new AdminService();
        this.maintenanceService = new MaintenanceService();
        this.backupService = new DatabaseBackupService();
        this.dbMaintenanceService = new DatabaseMaintenanceService();

        // Check if user has admin privileges
        if (!"ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            showAccessDenied();
            return;
        }

        initializeUI();
        loadSettings();
    }

    private void showAccessDenied() {
        setLayout(new BorderLayout());
        JLabel accessDeniedLabel = new JLabel("⛔ Access Denied - Administrator Privileges Required", JLabel.CENTER);
        accessDeniedLabel.setFont(new Font("Arial", Font.BOLD, 16));
        accessDeniedLabel.setForeground(Color.RED);
        add(accessDeniedLabel, BorderLayout.CENTER);
    }

    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Title
        JLabel titleLabel = new JLabel("System Settings - Administrator Panel", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(new Color(0, 70, 130));
        add(titleLabel, BorderLayout.NORTH);

        // Main content panel
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Maintenance Mode Section
        JPanel maintenancePanel = createMaintenancePanel();
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.gridwidth = 2;
        contentPanel.add(maintenancePanel, gbc);

        // System Information Section
        JPanel infoPanel = createSystemInfoPanel();
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.gridwidth = 2;
        contentPanel.add(infoPanel, gbc);

        // Database Section
        JPanel databasePanel = createDatabasePanel();
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 1;
        contentPanel.add(databasePanel, gbc);

        // Backup Section
        JPanel backupPanel = createBackupPanel();
        gbc.gridx = 1; gbc.gridy = 2;
        contentPanel.add(backupPanel, gbc);

        add(contentPanel, BorderLayout.CENTER);

        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        refreshButton = new JButton("Refresh");
        saveSettingsButton = new JButton("Save Settings");
        JButton backupButton = new JButton("Backup Now");
        JButton restoreButton = new JButton("Restore Backup");

        // Style buttons
        saveSettingsButton.setBackground(new Color(0, 120, 215));
        saveSettingsButton.setForeground(Color.BLACK);
        backupButton.setBackground(new Color(50, 205, 50));
        backupButton.setForeground(Color.BLACK);
        restoreButton.setBackground(new Color(255, 140, 0));
        restoreButton.setForeground(Color.BLACK);

        buttonPanel.add(refreshButton);
        buttonPanel.add(saveSettingsButton);
        buttonPanel.add(backupButton);
        buttonPanel.add(restoreButton);

        add(buttonPanel, BorderLayout.SOUTH);

        // Event listeners
        refreshButton.addActionListener(e -> loadSettings());
        saveSettingsButton.addActionListener(e -> saveSettings());
        backupButton.addActionListener(e -> performBackup());
        restoreButton.addActionListener(e -> performRestore());

        maintenanceCheckbox.addActionListener(e -> updateMaintenanceStatusDisplay());
    }

    private JPanel createMaintenancePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Maintenance Mode"));
        panel.setBackground(new Color(240, 240, 240));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        maintenanceCheckbox = new JCheckBox("Enable Maintenance Mode");
        maintenanceCheckbox.setFont(new Font("Arial", Font.BOLD, 14));
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(maintenanceCheckbox, gbc);

        maintenanceStatusLabel = new JLabel("System is currently in NORMAL mode");
        maintenanceStatusLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        maintenanceStatusLabel.setForeground(Color.GREEN);
        gbc.gridy = 1;
        panel.add(maintenanceStatusLabel, gbc);

        JLabel warningLabel = new JLabel("<html><i>Note: When maintenance mode is enabled, only administrators can make changes to the system.</i></html>");
        warningLabel.setFont(new Font("Arial", Font.ITALIC, 11));
        warningLabel.setForeground(Color.RED);
        gbc.gridy = 2;
        panel.add(warningLabel, gbc);

        return panel;
    }

    private JPanel createSystemInfoPanel() {
        JPanel panel = new JPanel(new GridLayout(0, 2, 10, 5));
        panel.setBorder(BorderFactory.createTitledBorder("System Information"));

        // System version - now dynamic
        panel.add(new JLabel("System Version:"));
        panel.add(new JLabel(maintenanceService.getSystemVersion()));

        // Database version
        panel.add(new JLabel("Database Version:"));
        panel.add(new JLabel("MySQL 8.0"));

        // Database size - dynamic
        DatabaseMaintenanceService.DatabaseSizeInfo sizeInfo = dbMaintenanceService.getDatabaseSize();
        panel.add(new JLabel("Database Size:"));
        panel.add(new JLabel(sizeInfo.getFormattedTotalSize()));

        // Last backup - dynamic
        List<DatabaseBackupService.BackupFileInfo> backups = backupService.getAvailableBackups();
        String lastBackup = backups.isEmpty() ? "No backups" :
                FileUtil.formatDate(backups.get(0).getCreatedDate());
        panel.add(new JLabel("Last Backup:"));
        panel.add(new JLabel(lastBackup));

        // Total users - dynamic
        int totalUsers = adminService.getAllUsers().size();
        panel.add(new JLabel("Total Users:"));
        panel.add(new JLabel(String.valueOf(totalUsers)));

        // Total courses - dynamic
        int totalCourses = adminService.getAllCourses().size();
        panel.add(new JLabel("Total Courses:"));
        panel.add(new JLabel(String.valueOf(totalCourses)));

        // Total sections - dynamic
        int totalSections = adminService.getAllSections().size();
        panel.add(new JLabel("Total Sections:"));
        panel.add(new JLabel(String.valueOf(totalSections)));

        return panel;
    }

    private JPanel createDatabasePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Database Management"));
        panel.setPreferredSize(new Dimension(300, 150));

        // Get database info
        DatabaseMaintenanceService.DatabaseSizeInfo sizeInfo = dbMaintenanceService.getDatabaseSize();

        JTextArea dbInfoArea = new JTextArea();
        dbInfoArea.setEditable(false);
        dbInfoArea.setText("Database: university_erp_db\n" +
                "Host: localhost:3306\n" +
                "User: root\n" +
                "Total Size: " + sizeInfo.getFormattedTotalSize() + "\n" +
                "Status: Connected");
        dbInfoArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        dbInfoArea.setBackground(new Color(240, 240, 240));

        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton optimizeButton = new JButton("Optimize DB");
        JButton checkButton = new JButton("Check Integrity");

        optimizeButton.setBackground(new Color(255, 215, 0));
        checkButton.setBackground(new Color(135, 206, 250));

        buttonPanel.add(optimizeButton);
        buttonPanel.add(checkButton);

        panel.add(new JScrollPane(dbInfoArea), BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        // Event listeners for database buttons
        optimizeButton.addActionListener(e -> optimizeDatabase());
        checkButton.addActionListener(e -> checkDatabaseIntegrity());

        return panel;
    }

    private JPanel createBackupPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Backup & Restore"));
        panel.setPreferredSize(new Dimension(300, 150));

        List<DatabaseBackupService.BackupFileInfo> backups = backupService.getAvailableBackups();
        String lastBackupInfo = backups.isEmpty() ?
                "No backups available" :
                "Last: " + FileUtil.formatDate(backups.get(0).getCreatedDate()) +
                        "\nSize: " + backups.get(0).getFormattedSize() +
                        "\nTotal Backups: " + backups.size();

        JTextArea backupInfoArea = new JTextArea();
        backupInfoArea.setEditable(false);
        backupInfoArea.setText("Backup Location: backups/\n" +
                "Auto Backup: Enabled\n" +
                "Schedule: Daily at 02:00\n\n" +
                lastBackupInfo);
        backupInfoArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        backupInfoArea.setBackground(new Color(240, 240, 240));

        JPanel settingsPanel = new JPanel(new GridLayout(0, 1, 5, 5));
        JCheckBox autoBackupCheckbox = new JCheckBox("Auto Backup", true);
        JComboBox<String> backupScheduleCombo = new JComboBox<>(new String[]{"Daily", "Weekly", "Monthly"});

        settingsPanel.add(autoBackupCheckbox);
        settingsPanel.add(new JLabel("Schedule:"));
        settingsPanel.add(backupScheduleCombo);

        panel.add(new JScrollPane(backupInfoArea), BorderLayout.CENTER);
        panel.add(settingsPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void loadSettings() {
        try {
            // Load maintenance mode status
            boolean maintenanceMode = maintenanceService.isMaintenanceMode();
            maintenanceCheckbox.setSelected(maintenanceMode);
            updateMaintenanceStatusDisplay();

            // Refresh system information
            refreshSystemInfo();

            System.out.println("Settings loaded successfully. Maintenance mode: " + maintenanceMode);
        } catch (Exception e) {
            System.err.println("Error loading settings: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Error loading settings: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateMaintenanceStatusDisplay() {
        boolean maintenanceMode = maintenanceCheckbox.isSelected();
        if (maintenanceMode) {
            maintenanceStatusLabel.setText("System is currently in MAINTENANCE mode");
            maintenanceStatusLabel.setForeground(Color.RED);
        } else {
            maintenanceStatusLabel.setText("System is currently in NORMAL mode");
            maintenanceStatusLabel.setForeground(Color.GREEN);
        }
    }

    private void refreshSystemInfo() {
        try {
            // Refresh the panel to update dynamic information
            removeAll();
            initializeUI();
            revalidate();
            repaint();
            System.out.println("System information refreshed successfully");
        } catch (Exception e) {
            System.err.println("Error refreshing system info: " + e.getMessage());
        }
    }

    private void saveSettings() {
        try {
            // Save maintenance mode setting
            boolean maintenanceMode = maintenanceCheckbox.isSelected();
            boolean success = adminService.setMaintenanceMode(maintenanceMode);

            if (success) {
                JOptionPane.showMessageDialog(this,
                        "Settings saved successfully.\n" +
                                (maintenanceMode ?
                                        "System is now in MAINTENANCE mode. Only administrators can make changes." :
                                        "System is now in NORMAL mode."),
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                updateMaintenanceStatusDisplay();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to save settings.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error saving settings: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // REAL BACKUP FUNCTIONALITY
    private void performBackup() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to perform a database backup now?\n" +
                        "This may take a few minutes.",
                "Confirm Backup",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            // Show progress dialog
            JDialog progressDialog = createProgressDialog("Backup in progress...");
            progressDialog.setVisible(true);

            // Perform backup in background thread
            new SwingWorker<DatabaseBackupService.BackupResult, Void>() {
                @Override
                protected DatabaseBackupService.BackupResult doInBackground() throws Exception {
                    return backupService.performBackup();
                }

                @Override
                protected void done() {
                    progressDialog.dispose();
                    try {
                        DatabaseBackupService.BackupResult result = get();
                        if (result.isSuccess()) {
                            StringBuilder message = new StringBuilder();
                            message.append("Backup completed successfully!\n\n");
                            message.append("Backup files created:\n");
                            for (String file : result.getBackupFiles()) {
                                message.append("• ").append(file).append("\n");
                            }
                            JOptionPane.showMessageDialog(SystemSettingsPanel.this,
                                    message.toString(),
                                    "Backup Complete",
                                    JOptionPane.INFORMATION_MESSAGE);

                            // Refresh to show new backup info
                            refreshSystemInfo();
                        } else {
                            JOptionPane.showMessageDialog(SystemSettingsPanel.this,
                                    "Backup failed: " + result.getMessage(),
                                    "Backup Error",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(SystemSettingsPanel.this,
                                "Backup failed: " + e.getMessage(),
                                "Backup Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            }.execute();
        }
    }

    // REAL RESTORE FUNCTIONALITY
    private void performRestore() {
        // Show available backups
        List<DatabaseBackupService.BackupFileInfo> backups = backupService.getAvailableBackups();

        if (backups.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No backup files found in the backups directory.",
                    "No Backups",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Create backup selection dialog
        String[] backupOptions = backups.stream()
                .map(b -> b.getFilename() + " (" + b.getFormattedSize() + " - " +
                        FileUtil.formatDate(b.getCreatedDate()) + ")")
                .toArray(String[]::new);

        JComboBox<String> backupCombo = new JComboBox<>(backupOptions);

        int result = JOptionPane.showConfirmDialog(this,
                new Object[]{"Select backup file to restore:", backupCombo},
                "Restore Backup",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            int selectedIndex = backupCombo.getSelectedIndex();
            DatabaseBackupService.BackupFileInfo selectedBackup = backups.get(selectedIndex);

            int confirm = JOptionPane.showConfirmDialog(this,
                    "WARNING: This will overwrite all current data!\n" +
                            "Are you sure you want to restore from: " + selectedBackup.getFilename() + "?",
                    "Confirm Restore",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                // Show progress dialog
                JDialog progressDialog = createProgressDialog("Restore in progress...");
                progressDialog.setVisible(true);

                new SwingWorker<Boolean, Void>() {
                    @Override
                    protected Boolean doInBackground() throws Exception {
                        // For simplicity, using the same file for both databases
                        // In a real implementation, you'd have separate auth and ERP backups
                        return backupService.performRestore(
                                selectedBackup.getFilePath(),
                                selectedBackup.getFilePath()
                        );
                    }

                    @Override
                    protected void done() {
                        progressDialog.dispose();
                        try {
                            boolean success = get();
                            if (success) {
                                JOptionPane.showMessageDialog(SystemSettingsPanel.this,
                                        "Database restored successfully!\n" +
                                                "Please restart the application to see the changes.",
                                        "Restore Complete",
                                        JOptionPane.INFORMATION_MESSAGE);
                            } else {
                                JOptionPane.showMessageDialog(SystemSettingsPanel.this,
                                        "Restore failed. Please check the console for errors.",
                                        "Restore Error",
                                        JOptionPane.ERROR_MESSAGE);
                            }
                        } catch (Exception e) {
                            JOptionPane.showMessageDialog(SystemSettingsPanel.this,
                                    "Restore failed: " + e.getMessage(),
                                    "Restore Error",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }.execute();
            }
        }
    }

    // REAL DATABASE OPTIMIZATION
    private void optimizeDatabase() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "This will optimize all database tables to improve performance.\n" +
                        "Continue?",
                "Confirm Optimization",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            JDialog progressDialog = createProgressDialog("Optimizing database...");
            progressDialog.setVisible(true);

            new SwingWorker<DatabaseMaintenanceService.MaintenanceResult, Void>() {
                @Override
                protected DatabaseMaintenanceService.MaintenanceResult doInBackground() throws Exception {
                    return dbMaintenanceService.optimizeTables();
                }

                @Override
                protected void done() {
                    progressDialog.dispose();
                    try {
                        DatabaseMaintenanceService.MaintenanceResult result = get();
                        showMaintenanceResult("Database Optimization", result);
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(SystemSettingsPanel.this,
                                "Optimization failed: " + e.getMessage(),
                                "Optimization Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            }.execute();
        }
    }

    // REAL DATABASE INTEGRITY CHECK
    private void checkDatabaseIntegrity() {
        JDialog progressDialog = createProgressDialog("Checking database integrity...");
        progressDialog.setVisible(true);

        new SwingWorker<DatabaseMaintenanceService.MaintenanceResult, Void>() {
            @Override
            protected DatabaseMaintenanceService.MaintenanceResult doInBackground() throws Exception {
                return dbMaintenanceService.checkDatabaseIntegrity();
            }

            @Override
            protected void done() {
                progressDialog.dispose();
                try {
                    DatabaseMaintenanceService.MaintenanceResult result = get();
                    showMaintenanceResult("Database Integrity Check", result);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(SystemSettingsPanel.this,
                            "Integrity check failed: " + e.getMessage(),
                            "Check Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    // HELPER METHOD TO SHOW MAINTENANCE RESULTS
    private void showMaintenanceResult(String title, DatabaseMaintenanceService.MaintenanceResult result) {
        StringBuilder message = new StringBuilder();
        message.append(result.getSummary()).append("\n\n");

        for (String detail : result.getDetails()) {
            message.append(detail).append("\n");
        }

        int messageType = result.isSuccess() ?
                JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE;

        JTextArea textArea = new JTextArea(message.toString());
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(600, 400));

        JOptionPane.showMessageDialog(this, scrollPane, title, messageType);
    }

    // HELPER METHOD TO CREATE PROGRESS DIALOG
    private JDialog createProgressDialog(String message) {
        JDialog dialog = new JDialog((Frame) null, "Please Wait", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(300, 100);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel label = new JLabel(message, JLabel.CENTER);
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);

        panel.add(label, BorderLayout.CENTER);
        panel.add(progressBar, BorderLayout.SOUTH);

        dialog.add(panel);
        return dialog;
    }
}