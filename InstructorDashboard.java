package edu.univ.erp.ui;
import edu.univ.erp.domain.User;
import edu.univ.erp.access.SessionManager;
import edu.univ.erp.service.MaintenanceService;
import javax.swing.*;
import java.awt.*;

public class InstructorDashboard extends JFrame {
    private User currentUser;
    private SessionManager sessionManager;
    private JTabbedPane tabbedPane;
    private MaintenanceService maintenanceService;
    private JLabel statusLabel;
    private JPanel bannerPanel;

    public InstructorDashboard(User user) {
        this.currentUser = user;
        this.sessionManager = SessionManager.getInstance();
        this.maintenanceService = new MaintenanceService();
        initializeUI();
        checkMaintenanceMode();
    }

    private void initializeUI() {
        setTitle("Instructor Dashboard - University ERP");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 750);
        setLocationRelativeTo(null);

        // Create menu bar
        JMenuBar menuBar = new JMenuBar();
        JMenu coursesMenu = new JMenu("Courses");
        JMenu gradesMenu = new JMenu("Grades");
        JMenu settingsMenu = new JMenu("Settings");
        JMenu helpMenu = new JMenu("More");

        JMenuItem mySectionsItem = new JMenuItem("My Sections");
        JMenuItem gradeManagementItem = new JMenuItem("Grade Management");
        JMenuItem changePasswordItem = new JMenuItem("Change Password");
        JMenuItem logoutItem = new JMenuItem("Logout");

        coursesMenu.add(mySectionsItem);
        gradesMenu.add(gradeManagementItem);
        settingsMenu.add(changePasswordItem);
        helpMenu.add(logoutItem);

        menuBar.add(coursesMenu);
        menuBar.add(gradesMenu);
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
        tabbedPane.addTab("My Sections", new InstructorSectionsPanel(currentUser));
        tabbedPane.addTab("Grade Management", new GradeManagementPanel(currentUser));

        mainContentPanel.add(tabbedPane, BorderLayout.CENTER);

        // Status bar
        JPanel statusPanelBottom = new JPanel(new BorderLayout());
        statusPanelBottom.setBorder(BorderFactory.createEtchedBorder());
        statusLabel = new JLabel("Welcome, " + currentUser.getUsername() + " | Role: Instructor");
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

        mySectionsItem.addActionListener(e -> tabbedPane.setSelectedIndex(0));
        gradeManagementItem.addActionListener(e -> tabbedPane.setSelectedIndex(1));
    }

    private void checkMaintenanceMode() {
        if (maintenanceService.isMaintenanceMode()) {
            // Show the maintenance banner
            bannerPanel.setVisible(true);

            // Update status to show maintenance mode
            statusLabel.setText("Welcome, " + currentUser.getUsername() +
                    " | Role: Instructor | ⚠️ MAINTENANCE MODE - Read Only");
            statusLabel.setForeground(Color.RED);

            // Disable menu items that allow changes
            JMenu gradesMenu = getJMenuBar().getMenu(1);
            for (Component comp : gradesMenu.getMenuComponents()) {
                if (comp instanceof JMenuItem) {
                    ((JMenuItem) comp).setEnabled(false);
                }
            }
        }
    }
}