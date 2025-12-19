package edu.univ.erp.ui;
import edu.univ.erp.domain.User;
import edu.univ.erp.access.SessionManager;
import edu.univ.erp.service.MaintenanceService;
import javax.swing.*;
import java.awt.*;

public class AdminDashboard extends JFrame {
    private User currentUser;
    private SessionManager sessionManager;
    private JTabbedPane tabbedPane;
    private MaintenanceService maintenanceService;
    private JLabel statusLabel;
    private JPanel bannerPanel;

    public AdminDashboard(User user) {
        this.currentUser = user;
        this.sessionManager = SessionManager.getInstance();
        this.maintenanceService = new MaintenanceService();
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Admin Dashboard - University ERP");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);

        // Create menu bar
        JMenuBar menuBar = new JMenuBar();
        JMenu systemMenu = new JMenu("System");
        JMenu settingsMenu = new JMenu("Settings");
        JMenu helpMenu = new JMenu("More");

        JMenuItem changePasswordItem = new JMenuItem("Change Password");
        JMenuItem logoutItem = new JMenuItem("Logout");
        JMenuItem exitItem = new JMenuItem("Exit");
        JMenuItem aboutItem = new JMenuItem("About");

        systemMenu.add(logoutItem);
        systemMenu.add(exitItem);
        settingsMenu.add(changePasswordItem);
        helpMenu.add(aboutItem);

        menuBar.add(systemMenu);
        menuBar.add(settingsMenu);
        menuBar.add(helpMenu);
        setJMenuBar(menuBar);

        // Main content panel with banner and tabs
        JPanel mainContentPanel = new JPanel(new BorderLayout());

        // Maintenance banner (initially hidden)
        bannerPanel = new JPanel(new BorderLayout());
        bannerPanel.setBackground(new Color(220, 20, 60)); // Crimson red
        bannerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        JLabel bannerLabel = new JLabel(" MAINTENANCE MODE - Read Only", JLabel.CENTER);
        bannerLabel.setFont(new Font("Arial", Font.BOLD, 16));
        bannerLabel.setForeground(Color.WHITE);
        bannerPanel.add(bannerLabel, BorderLayout.CENTER);
        bannerPanel.setVisible(false); // Hidden by default (admin doesn't see maintenance restrictions)

        mainContentPanel.add(bannerPanel, BorderLayout.NORTH);

        // Main tabbed pane
        tabbedPane = new JTabbedPane();

        // Add different management panels
        tabbedPane.addTab("User Management", new UserManagementPanel(currentUser));
        tabbedPane.addTab("Course Management", new CourseManagementPanel(currentUser));
        tabbedPane.addTab("Section Management", new SectionManagementPanel(currentUser));
        tabbedPane.addTab("System Settings", new SystemSettingsPanel(currentUser));

        mainContentPanel.add(tabbedPane, BorderLayout.CENTER);

        // Status bar
        JPanel statusPanelBottom = new JPanel(new BorderLayout());
        statusPanelBottom.setBorder(BorderFactory.createEtchedBorder());
        statusLabel = new JLabel("Welcome, " + currentUser.getUsername() + " | Role: " + currentUser.getRole());
        statusPanelBottom.add(statusLabel, BorderLayout.WEST);

        mainContentPanel.add(statusPanelBottom, BorderLayout.SOUTH);

        add(mainContentPanel);

        // Event listeners
        logoutItem.addActionListener(e -> {
            sessionManager.logout();
            dispose();
            new LoginFrame().setVisible(true);
        });
        
        changePasswordItem.addActionListener(e -> {
            ChangePasswordDialog dialog = new ChangePasswordDialog(this, currentUser);
            dialog.setVisible(true);
        });

        exitItem.addActionListener(e -> System.exit(0));
        aboutItem.addActionListener(e -> showAboutDialog());
    }

    private void showAboutDialog() {
        JOptionPane.showMessageDialog(this,
                "University ERP System\nVersion 1.0\n\nDeveloped for Academic Project",
                "About",
                JOptionPane.INFORMATION_MESSAGE);
    }
}
