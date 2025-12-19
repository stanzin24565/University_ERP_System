package edu.univ.erp.ui;
import edu.univ.erp.access.SessionManager;
import edu.univ.erp.auth.AuthenticationService;
import edu.univ.erp.domain.User;
import edu.univ.erp.data.UserDAO;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JLabel statusLabel;
    private AuthenticationService authService;
    private UserDAO userDAO;
    
    public LoginFrame() {
        this.authService = new AuthenticationService();
        this.userDAO = new UserDAO();
        initializeUI();
    }

    private void initializeUI() {
        setTitle("ERP System - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 350);
        setLocationRelativeTo(null);
        setResizable(false);
        setIconImage(new ImageIcon("ERP_LOGO.png").getImage());


        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header
        JLabel headerLabel = new JLabel("ERP", JLabel.CENTER);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 30));
        headerLabel.setForeground(Color.decode("#05014A"));

        mainPanel.add(headerLabel, BorderLayout.NORTH);

        // Login form
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Username:"), gbc);

        gbc.gridx = 1;
        usernameField = new JTextField(15);
        formPanel.add(usernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("Password:"), gbc);

        gbc.gridx = 1;
        passwordField = new JPasswordField(15);
        formPanel.add(passwordField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        loginButton = new JButton("LOGIN");
        headerLabel.setForeground(Color.decode("#05014A"));

        loginButton.setForeground(Color.BLUE);
        loginButton.setFocusPainted(false);
        formPanel.add(loginButton, gbc);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // Status label
        statusLabel = new JLabel(" ", JLabel.CENTER);
        statusLabel.setForeground(Color.RED);
        
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.add(statusLabel, BorderLayout.NORTH);
        
        // Info label
        JLabel infoLabel = new JLabel(" admin1 | inst1 | student1 | student2", JLabel.CENTER);
        infoLabel.setFont(new Font("Arial", Font.ITALIC, 10));
        infoLabel.setForeground(new Color(100, 100, 100));
        statusPanel.add(infoLabel, BorderLayout.SOUTH);
        
        mainPanel.add(statusPanel, BorderLayout.SOUTH);

        add(mainPanel);

        // Event listeners
        LoginAction action = new LoginAction();
        loginButton.addActionListener(action);
        passwordField.addActionListener(action); // Press Enter = login
    }

    private class LoginAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());

            if (username.isEmpty() || password.isEmpty()) {
                statusLabel.setText("Please enter username and password.");
                return;
            }

            // Check if user exists and is locked
            User userCheck = userDAO.findByUsername(username);
            
            if (userCheck != null && userCheck.isLocked()) {
                int secondsLeft = authService.getLockoutTimeRemaining(userCheck);
                int minutesLeft = (secondsLeft + 59) / 60; // Round up
                statusLabel.setText("Account locked. Try again in " + minutesLeft + " minute(s).");
                return;
            }

            // Authenticate using AuthenticationService (handles lockout)
            User user = authService.authenticate(username, password);

            if (user != null) {
                // Step 3: check status
                if (!user.getStatus().equalsIgnoreCase("ACTIVE")) {
                    statusLabel.setText("Your account is not active.");
                    return;
                }

                // Step 4: login success
                SessionManager sessionManager = SessionManager.getInstance();
                sessionManager.login(user);

                dispose(); // close login window

                // Step 5: open dashboard
                switch (user.getRole().toUpperCase()) {
                    case "ADMIN":
                        new AdminDashboard(user).setVisible(true);
                        break;
                    case "INSTRUCTOR":
                        new InstructorDashboard(user).setVisible(true);
                        break;
                    case "STUDENT":
                        new StudentDashboard(user).setVisible(true);
                        break;
                    default:
                        JOptionPane.showMessageDialog(LoginFrame.this,
                                "Unknown user role: " + user.getRole(),
                                "Error", JOptionPane.ERROR_MESSAGE);
                }

            } else {
                // Check if account just got locked
                User lockedUser = userDAO.findByUsername(username);
                if (lockedUser != null && lockedUser.isLocked()) {
                    int secondsLeft = authService.getLockoutTimeRemaining(lockedUser);
                    int minutesLeft = (secondsLeft + 59) / 60;
                    statusLabel.setText("Too many failed attempts. Account locked for " + minutesLeft + " min.");
                } else {
                    statusLabel.setText("Incorrect username or password.");
                }
            }
        }


    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Launch the Login window
            new LoginFrame().setVisible(true);
        });
    }
}