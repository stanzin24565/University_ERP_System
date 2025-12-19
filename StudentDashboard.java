package edu.univ.erp.ui;

import edu.univ.erp.domain.User;
import edu.univ.erp.access.SessionManager;
import edu.univ.erp.service.MaintenanceService;  // ← ADD THIS IMPORT
import javax.swing.*;
import java.awt.*;

public class StudentDashboard extends JFrame {
    private User currentUser;
    private SessionManager sessionManager;
    private JTabbedPane tabbedPane;
    private MaintenanceService maintenanceService;  // ← ADD THIS FIELD
    private JLabel statusLabel;  // ← ADD THIS FIELD
    private JPanel bannerPanel;  // ← ADD BANNER PANEL REFERENCE

    public StudentDashboard(User user) {
        this.currentUser = user;
        this.sessionManager = SessionManager.getInstance();
        this.maintenanceService = new MaintenanceService();  // ← INITIALIZE SERVICE
        initializeUI();
        checkMaintenanceMode();  // ← CHECK MAINTENANCE MODE
    }

    private void initializeUI() {
        setTitle("Student Dashboard - University ERP");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);

        // Create menu bar
        JMenuBar menuBar = new JMenuBar();
        JMenu academicMenu = new JMenu("Academic");
        JMenu settingsMenu = new JMenu("Settings");
        JMenu helpMenu = new JMenu("More");

        JMenuItem registrationItem = new JMenuItem("Course Registration");
        JMenuItem gradesItem = new JMenuItem("My Grades");
        
        JMenuItem changePasswordItem = new JMenuItem("Change Password");
        
        JMenuItem logoutItem = new JMenuItem("Logout");

        academicMenu.add(registrationItem);
        academicMenu.add(gradesItem);
        settingsMenu.add(changePasswordItem);
        helpMenu.add(logoutItem);

        menuBar.add(academicMenu);
        menuBar.add(settingsMenu);
        menuBar.add(helpMenu);
        setJMenuBar(menuBar);

        // Main content panel with banner and tabs
        JPanel mainContentPanel = new JPanel(new BorderLayout());

        // Maintenance banner (initially hidden)
        bannerPanel = new JPanel(new BorderLayout());
        bannerPanel.setBackground(new Color(220, 20, 60)); // Crimson red
        bannerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        JLabel bannerLabel = new JLabel("⚠️  MAINTENANCE MODE - Read Only", JLabel.CENTER);
        bannerLabel.setFont(new Font("Arial", Font.BOLD, 16));
        bannerLabel.setForeground(Color.WHITE);
        bannerPanel.add(bannerLabel, BorderLayout.CENTER);
        bannerPanel.setVisible(false); // Hidden by default

        mainContentPanel.add(bannerPanel, BorderLayout.NORTH);

        // Main tabbed pane
        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("My Registrations", new RegistrationPanel(currentUser));
        tabbedPane.addTab("Course Catalog", new CoursePanel(currentUser));
        tabbedPane.addTab("Grades", new StudentGradesPanel(currentUser));

        mainContentPanel.add(tabbedPane, BorderLayout.CENTER);

        // Status bar - MODIFIED TO INCLUDE MAINTENANCE STATUS
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(BorderFactory.createEtchedBorder());

        String baseStatus = "Welcome, " + currentUser.getUsername() + " | Role: Student";
        statusLabel = new JLabel(baseStatus);
        statusPanel.add(statusLabel, BorderLayout.WEST);

        mainContentPanel.add(statusPanel, BorderLayout.SOUTH);

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

        registrationItem.addActionListener(e -> tabbedPane.setSelectedIndex(0));
        gradesItem.addActionListener(e -> tabbedPane.setSelectedIndex(2));
    }

    // ← ADD THIS NEW METHOD TO CHECK MAINTENANCE MODE
    private void checkMaintenanceMode() {
        if (maintenanceService.isMaintenanceMode()) {
            // Show the maintenance banner
            bannerPanel.setVisible(true);

            // Update status to show maintenance mode
            statusLabel.setText("Welcome, " + currentUser.getUsername() +
                    " | Role: Student | ⚠️ MAINTENANCE MODE - Read Only");
            statusLabel.setForeground(Color.RED);

            // Disable menu items that allow changes
            disableStudentActions();
        }
    }

    // ← ADD THIS METHOD TO DISABLE ACTIONS
    private void disableStudentActions() {
        // Disable academic menu items that allow changes
        Component[] academicComponents = ((JMenu)getJMenuBar().getMenu(0)).getMenuComponents();
        for (Component comp : academicComponents) {
            if (comp instanceof JMenuItem) {
                JMenuItem menuItem = (JMenuItem) comp;
                // Only disable items that perform actions (not view-only)
                if (!menuItem.getText().equals("My Grades")) { // Keep grades viewable
                    menuItem.setEnabled(false);
                }
            }
        }

        // You might also want to disable specific tabs or add maintenance warnings
        // to the individual panels (RegistrationPanel, CoursePanel, etc.)
        addMaintenanceWarningToPanels();
    }

    // ← ADD THIS METHOD TO ADD WARNINGS TO INDIVIDUAL PANELS
    private void addMaintenanceWarningToPanels() {
        // This method would need to be implemented based on your panel classes
        // For now, we'll just log that maintenance mode is active
        System.out.println("MAINTENANCE MODE: Student actions disabled for user: " + currentUser.getUsername());
    }
}