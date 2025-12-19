package edu.univ.erp.ui;

import edu.univ.erp.domain.User;
import edu.univ.erp.data.UserDAO;
import edu.univ.erp.util.Logger;

import javax.swing.*;
import java.awt.*;

/**
 * Dialog for users to change their password
 * Validates password requirements and updates Auth DB
 */
public class ChangePasswordDialog extends JDialog {
    private User currentUser;
    private UserDAO userDAO;
    private Logger logger;
    
    private JPasswordField currentPasswordField;
    private JPasswordField newPasswordField;
    private JPasswordField confirmPasswordField;
    private JLabel strengthLabel;
    private JButton changeButton;
    private JButton cancelButton;
    
    private boolean passwordChanged = false;

    public ChangePasswordDialog(Frame owner, User user) {
        super(owner, "Change Password", true);
        this.currentUser = user;
        this.userDAO = new UserDAO();
        this.logger = Logger.getInstance();
        
        initializeUI();
        setLocationRelativeTo(owner);
    }

    private void initializeUI() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(400, 300);
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Title
        JLabel titleLabel = new JLabel("Change Your Password", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(new Color(0, 70, 130));
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        
        // Form panel
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBorder(BorderFactory.createTitledBorder("Password Change"));
        
        // Current password field
        JPanel currentPanel = new JPanel(new BorderLayout(5, 0));
        currentPanel.add(new JLabel("Current Password:"), BorderLayout.WEST);
        currentPasswordField = new JPasswordField(20);
        currentPanel.add(currentPasswordField, BorderLayout.CENTER);
        formPanel.add(currentPanel);
        formPanel.add(Box.createVerticalStrut(10));
        
        // New password field
        JPanel newPanel = new JPanel(new BorderLayout(5, 0));
        newPanel.add(new JLabel("New Password:"), BorderLayout.WEST);
        newPasswordField = new JPasswordField(20);
        newPasswordField.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent evt) {
                updatePasswordStrength();
            }
        });
        newPanel.add(newPasswordField, BorderLayout.CENTER);
        formPanel.add(newPanel);
        formPanel.add(Box.createVerticalStrut(5));
        
        // Password strength indicator
        strengthLabel = new JLabel("Strength: ---");
        strengthLabel.setFont(new Font("Arial", Font.ITALIC, 10));
        strengthLabel.setForeground(Color.GRAY);
        formPanel.add(strengthLabel);
        formPanel.add(Box.createVerticalStrut(10));
        
        // Confirm password field
        JPanel confirmPanel = new JPanel(new BorderLayout(5, 0));
        confirmPanel.add(new JLabel("Confirm Password:"), BorderLayout.WEST);
        confirmPasswordField = new JPasswordField(20);
        confirmPanel.add(confirmPasswordField, BorderLayout.CENTER);
        formPanel.add(confirmPanel);
        formPanel.add(Box.createVerticalStrut(10));
        
        // Requirements
        JLabel reqLabel = new JLabel("Requirements: At least 8 characters, 1 uppercase, 1 digit");
        reqLabel.setFont(new Font("Arial", Font.PLAIN, 9));
        reqLabel.setForeground(new Color(100, 100, 100));
        formPanel.add(reqLabel);
        
        mainPanel.add(formPanel, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        changeButton = new JButton("Change Password");
        cancelButton = new JButton("Cancel");
        
        changeButton.setBackground(new Color(0, 120, 215));
        changeButton.setForeground(Color.BLACK);
        cancelButton.setBackground(new Color(200, 200, 200));
        cancelButton.setForeground(Color.BLACK);
        
        changeButton.addActionListener(e -> handlePasswordChange());
        cancelButton.addActionListener(e -> dispose());
        
        buttonPanel.add(changeButton);
        buttonPanel.add(cancelButton);
        
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        setContentPane(mainPanel);
    }

    private void updatePasswordStrength() {
        String password = new String(newPasswordField.getPassword());
        
        if (password.isEmpty()) {
            strengthLabel.setText("Strength: ---");
            strengthLabel.setForeground(Color.GRAY);
            return;
        }
        
        int strength = 0;
        StringBuilder info = new StringBuilder();
        
        if (password.length() >= 8) strength++;
        if (password.matches(".*[A-Z].*")) strength++;
        if (password.matches(".*[0-9].*")) strength++;
        if (password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};:'\",.<>?/\\\\|`~].*")) strength++;
        
        switch (strength) {
            case 0:
            case 1:
                info.append("Weak");
                strengthLabel.setForeground(new Color(200, 0, 0));
                break;
            case 2:
                info.append("Fair");
                strengthLabel.setForeground(new Color(255, 140, 0));
                break;
            case 3:
                info.append("Good");
                strengthLabel.setForeground(new Color(0, 150, 0));
                break;
            case 4:
                info.append("Strong");
                strengthLabel.setForeground(new Color(0, 100, 0));
                break;
        }
        
        strengthLabel.setText("Strength: " + info.toString());
    }

    private void handlePasswordChange() {
        String currentPassword = new String(currentPasswordField.getPassword());
        String newPassword = new String(newPasswordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());
        
        // Validate current password
        if (!currentPassword.equals(currentUser.getPassword())) {
            JOptionPane.showMessageDialog(this,
                    "Current password is incorrect.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            logger.warn("Failed password change attempt for user: " + currentUser.getUsername());
            return;
        }
        
        // Validate new password not empty
        if (newPassword.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "New password cannot be empty.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Validate new password length
        if (newPassword.length() < 8) {
            JOptionPane.showMessageDialog(this,
                    "New password must be at least 8 characters long.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Validate new password has uppercase
        if (!newPassword.matches(".*[A-Z].*")) {
            JOptionPane.showMessageDialog(this,
                    "New password must contain at least one uppercase letter.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Validate new password has digit
        if (!newPassword.matches(".*[0-9].*")) {
            JOptionPane.showMessageDialog(this,
                    "New password must contain at least one digit.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Validate passwords match
        if (!newPassword.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this,
                    "New password and confirmation do not match.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Validate new password is different from current
        if (newPassword.equals(currentPassword)) {
            JOptionPane.showMessageDialog(this,
                    "New password must be different from current password.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Update password in database
        currentUser.setPassword(newPassword);
        if (userDAO.save(currentUser)) {
            JOptionPane.showMessageDialog(this,
                    "Password changed successfully!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            logger.info("Password changed successfully for user: " + currentUser.getUsername());
            passwordChanged = true;
            dispose();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Failed to update password. Please try again.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            logger.error("Failed to update password for user: " + currentUser.getUsername());
        }
    }

    public boolean isPasswordChanged() {
        return passwordChanged;
    }
}
