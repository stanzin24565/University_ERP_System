package edu.univ.erp.ui;
import edu.univ.erp.domain.User;
import edu.univ.erp.service.AdminService;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class UserManagementPanel extends JPanel {
    private User currentUser;
    private AdminService adminService;
    private JTable userTable;
    private DefaultTableModel tableModel;

    public UserManagementPanel(User user) {
        this.currentUser = user;
        this.adminService = new AdminService();
        initializeUI();
        loadUserData();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());

        // Title
        JLabel titleLabel = new JLabel("User Management", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        add(titleLabel, BorderLayout.NORTH);

        // Table
        String[] columns = {"User ID", "Username", "Role", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        userTable = new JTable(tableModel);
        userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(userTable);
        add(scrollPane, BorderLayout.CENTER);

        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton refreshButton = new JButton("Refresh");
        JButton addButton = new JButton("Add User");
        JButton editButton = new JButton("Edit User");
        JButton deleteButton = new JButton("Delete User");

        refreshButton.addActionListener(e -> loadUserData());
        addButton.addActionListener(e -> addUser());
        editButton.addActionListener(e -> editUser());
        deleteButton.addActionListener(e -> deleteUser());

        buttonPanel.add(refreshButton);
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadUserData() {
        tableModel.setRowCount(0);
        List<User> users = adminService.getAllUsers();

        for (User user : users) {
            tableModel.addRow(new Object[]{
                    user.getUserId(),
                    user.getUsername(),
                    user.getRole(),
                    user.getStatus()
            });
        }
    }

    private void addUser() {
        // Create a dialog for adding new user
        JDialog addDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Add New User", true);
        addDialog.setLayout(new BorderLayout());
        addDialog.setSize(400, 300);
        addDialog.setLocationRelativeTo(this);

        // Form panel
        JPanel formPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JComboBox<String> roleComboBox = new JComboBox<>(new String[]{"ADMIN","INSTRUCTOR", "STUDENT"});
        JComboBox<String> statusComboBox = new JComboBox<>(new String[]{"ACTIVE", "INACTIVE"});

        formPanel.add(new JLabel("Username:"));
        formPanel.add(usernameField);
        formPanel.add(new JLabel("Password:"));
        formPanel.add(passwordField);
        formPanel.add(new JLabel("Role:"));
        formPanel.add(roleComboBox);
        formPanel.add(new JLabel("Status:"));
        formPanel.add(statusComboBox);

        addDialog.add(formPanel, BorderLayout.CENTER);

        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");

        saveButton.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());
            String role = (String) roleComboBox.getSelectedItem();
            String status = (String) statusComboBox.getSelectedItem();

            System.out.println("DEBUG ADD: Form data - Username: '" + username + "', Password: '" +
                    (password.isEmpty() ? "EMPTY" : "PROVIDED") + "', Role: " + role + ", Status: " + status);

            // Validation
            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(addDialog, "Username and password are required!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (username.length() < 3) {
                JOptionPane.showMessageDialog(addDialog, "Username must be at least 3 characters long!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Create new user
            User newUser = new User();
            newUser.setUsername(username);
            newUser.setPassword(password); // Make sure this is set
            newUser.setRole(role);
            newUser.setActive("ACTIVE".equals(status));

            System.out.println("DEBUG ADD: User object created - Username: " + newUser.getUsername() +
                    ", Password: " + (newUser.getPassword() != null ? "SET" : "NULL") +
                    ", Role: " + newUser.getRole() +
                    ", Active: " + newUser.isActive());

            boolean success = adminService.createUser(newUser);
            System.out.println("DEBUG ADD: Create user result: " + success);

            if (success) {
                JOptionPane.showMessageDialog(addDialog, "User '" + username + "' added successfully!");
                addDialog.dispose();
                loadUserData();
            } else {
                JOptionPane.showMessageDialog(addDialog,
                        "Failed to add user. Username '" + username + "' might already exist.\nCheck console for details.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> addDialog.dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        addDialog.add(buttonPanel, BorderLayout.SOUTH);

        addDialog.setVisible(true);
    }
    private void editUser() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a user to edit.");
            return;
        }

        int userId = (Integer) tableModel.getValueAt(selectedRow, 0);

        // Use the UserDAO's findById method through AdminService
        final User userToEdit = adminService.getUserById(userId);

        if (userToEdit == null) {
            JOptionPane.showMessageDialog(this, "User not found!");
            return;
        }

        // Create a dialog for editing user
        JDialog editDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Edit User", true);
        editDialog.setLayout(new BorderLayout());
        editDialog.setSize(400, 300);
        editDialog.setLocationRelativeTo(this);

        // Form panel
        JPanel formPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JTextField usernameField = new JTextField(userToEdit.getUsername());
        JPasswordField passwordField = new JPasswordField();
        JComboBox<String> roleComboBox = new JComboBox<>(new String[]{"ADMIN", "STUDENT", "INSTRUCTOR"});
        JComboBox<String> statusComboBox = new JComboBox<>(new String[]{"ACTIVE", "INACTIVE"});

        roleComboBox.setSelectedItem(userToEdit.getRole());
        statusComboBox.setSelectedItem(userToEdit.getStatus());

        formPanel.add(new JLabel("Username:"));
        formPanel.add(usernameField);
        formPanel.add(new JLabel("Password")); // LEAVE BLANK TO SAVE THE CURRENT PASSWORD
        formPanel.add(passwordField);
        formPanel.add(new JLabel("Role:"));
        formPanel.add(roleComboBox);
        formPanel.add(new JLabel("Status:"));
        formPanel.add(statusComboBox);

        editDialog.add(formPanel, BorderLayout.CENTER);

        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");

        saveButton.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());
            String role = (String) roleComboBox.getSelectedItem();
            String status = (String) statusComboBox.getSelectedItem();

            // Validation
            if (username.isEmpty()) {
                JOptionPane.showMessageDialog(editDialog, "Username is required!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (username.length() < 3) {
                JOptionPane.showMessageDialog(editDialog, "Username must be at least 3 characters long!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Update user
            userToEdit.setUsername(username);
            userToEdit.setRole(role);
            userToEdit.setActive("ACTIVE".equals(status));

            // Update password only if provided
            if (!password.isEmpty()) {
                userToEdit.setPassword(password);
            }

            boolean success = adminService.updateUser(userToEdit);
            if (success) {
                JOptionPane.showMessageDialog(editDialog, "User updated successfully!");
                editDialog.dispose();
                loadUserData();
            } else {
                JOptionPane.showMessageDialog(editDialog, "Failed to update user.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> editDialog.dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        editDialog.add(buttonPanel, BorderLayout.SOUTH);

        editDialog.setVisible(true);
    }

    private void deleteUser() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a user to delete.");
            return;
        }

        int userId = (Integer) tableModel.getValueAt(selectedRow, 0);
        String username = (String) tableModel.getValueAt(selectedRow, 1);

        System.out.println("DELETE ATTEMPT: User ID=" + userId + ", Username=" + username);

        // First, verify the user exists
        User userToDelete = adminService.getUserById(userId);
        if (userToDelete == null) {
            JOptionPane.showMessageDialog(this,
                    "User not found in database! ID: " + userId + "\nThe user may have been already deleted.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        System.out.println("User verified: " + userToDelete.getUsername());

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to DELETE user:\n" +
                        "Username: " + username + "\n" +
                        "User ID: " + userId + "\n" +
                        "Role: " + userToDelete.getRole() + "\n\n" +
                        "This action cannot be undone!",
                "CONFIRM DELETE",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                System.out.println("User confirmed deletion, calling delete service...");
                boolean success = adminService.deleteUser(userId);
                System.out.println("Delete service returned: " + success);

                if (success) {
                    JOptionPane.showMessageDialog(this,
                            "User '" + username + "' has been successfully deleted!",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                    loadUserData(); // Refresh the table
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Delete operation failed!\n\n" +
                                    "Possible reasons:\n" +
                                    "• User has related records in other tables\n" +
                                    "• Database constraints prevent deletion\n" +
                                    "• User does not exist\n\n" +
                                    "Check console for detailed error information.",
                            "Delete Failed",
                            JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "Unexpected error during deletion:\n" + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }

}