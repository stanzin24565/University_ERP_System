package edu.univ.erp.ui;



import edu.univ.erp.domain.Course;
import edu.univ.erp.domain.User;
import edu.univ.erp.service.AdminService;
import edu.univ.erp.util.ValidationUtil;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CourseManagementPanel extends JPanel {
    private User currentUser;
    private AdminService adminService;
    private JTable courseTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JButton searchButton;

    public CourseManagementPanel(User user) {
        this.currentUser = user;
        this.adminService = new AdminService();
        initializeUI();
        loadCourseData();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Title
        JLabel titleLabel = new JLabel("Course Management", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(new Color(0, 70, 130));
        add(titleLabel, BorderLayout.NORTH);

        // Search panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBorder(BorderFactory.createTitledBorder("Search Courses"));

        searchField = new JTextField(20);
        searchButton = new JButton("Search");
        JButton clearButton = new JButton("Clear");

        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(clearButton);

        add(searchPanel, BorderLayout.NORTH);

        // Table
        String[] columns = {"Course ID", "Code", "Title", "Credits", "Status", "Description"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0 || columnIndex == 3) return Integer.class;
                return String.class;
            }
        };

        courseTable = new JTable(tableModel);
        courseTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        courseTable.setRowHeight(25);
        courseTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));

        // Set column widths
        courseTable.getColumnModel().getColumn(0).setPreferredWidth(80);  // ID
        courseTable.getColumnModel().getColumn(1).setPreferredWidth(100); // Code
        courseTable.getColumnModel().getColumn(2).setPreferredWidth(200); // Title
        courseTable.getColumnModel().getColumn(3).setPreferredWidth(70);  // Credits
        courseTable.getColumnModel().getColumn(4).setPreferredWidth(80);  // Status
        courseTable.getColumnModel().getColumn(5).setPreferredWidth(300); // Description

        JScrollPane scrollPane = new JScrollPane(courseTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Courses"));
        add(scrollPane, BorderLayout.CENTER);

        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JButton refreshButton = new JButton("Refresh");
        JButton addButton = new JButton("Add Course");
        JButton editButton = new JButton("Edit Course");
        JButton deleteButton = new JButton("Delete Course");
        JButton activateButton = new JButton("Activate");
        JButton deactivateButton = new JButton("Deactivate");

        // Style buttons
        addButton.setBackground(new Color(0, 120, 215));
        addButton.setForeground(Color.BLACK);
        editButton.setBackground(new Color(255, 140, 0));
        editButton.setForeground(Color.BLACK);
        deleteButton.setBackground(new Color(220, 20, 60));
        deleteButton.setForeground(Color.BLACK);
        activateButton.setBackground(new Color(50, 205, 50));
        activateButton.setForeground(Color.BLACK);
        deactivateButton.setBackground(new Color(255, 69, 0));
        deactivateButton.setForeground(Color.BLACK);

        buttonPanel.add(refreshButton);
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(activateButton);
        buttonPanel.add(deactivateButton);

        add(buttonPanel, BorderLayout.SOUTH);

        // Event listeners
        refreshButton.addActionListener(e -> loadCourseData());
        addButton.addActionListener(e -> addCourse());
        editButton.addActionListener(e -> editCourse());
        deleteButton.addActionListener(e -> deleteCourse());
        activateButton.addActionListener(e -> activateCourse());
        deactivateButton.addActionListener(e -> deactivateCourse());
        searchButton.addActionListener(e -> searchCourses());
        clearButton.addActionListener(e -> {
            searchField.setText("");
            loadCourseData();
        });

        // Double click to edit
        courseTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    editCourse();
                }
            }
        });
    }

    private void loadCourseData() {
        tableModel.setRowCount(0);
        java.util.List<Course> courses = adminService.getAllCourses();

        for (Course course : courses) {
            tableModel.addRow(new Object[]{
                    course.getCourseId(),
                    course.getCode(),
                    course.getTitle(),
                    course.getCredits(),
                    course.getStatus(),
                    course.getDescription() != null ? course.getDescription() : ""
            });
        }

        updateStatusBar();
    }

    private void searchCourses() {
        String searchTerm = searchField.getText().trim();
        if (searchTerm.isEmpty()) {
            loadCourseData();
            return;
        }

        tableModel.setRowCount(0);
        java.util.List<Course> courses = adminService.getAllCourses();

        for (Course course : courses) {
            if (course.getCode().toLowerCase().contains(searchTerm.toLowerCase()) ||
                    course.getTitle().toLowerCase().contains(searchTerm.toLowerCase()) ||
                    (course.getDescription() != null && course.getDescription().toLowerCase().contains(searchTerm.toLowerCase()))) {

                tableModel.addRow(new Object[]{
                        course.getCourseId(),
                        course.getCode(),
                        course.getTitle(),
                        course.getCredits(),
                        course.getStatus(),
                        course.getDescription() != null ? course.getDescription() : ""
                });
            }
        }

        updateStatusBar();
    }

    private void addCourse() {
        CourseDialog dialog = new CourseDialog((JFrame) SwingUtilities.getWindowAncestor(this), "Add New Course", null);
        dialog.setVisible(true);

        if (dialog.isSuccess()) {
            loadCourseData();
        }
    }

    private void editCourse() {
        int selectedRow = courseTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a course to edit.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int courseId = (Integer) tableModel.getValueAt(selectedRow, 0);
        Course course = adminService.getAllCourses().stream()
                .filter(c -> c.getCourseId() == courseId)
                .findFirst()
                .orElse(null);

        if (course != null) {
            CourseDialog dialog = new CourseDialog((JFrame) SwingUtilities.getWindowAncestor(this), "Edit Course", course);
            dialog.setVisible(true);

            if (dialog.isSuccess()) {
                loadCourseData();
            }
        }
    }

    private void deleteCourse() {
        int selectedRow = courseTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a course to delete.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int courseId = (Integer) tableModel.getValueAt(selectedRow, 0);
        String courseCode = (String) tableModel.getValueAt(selectedRow, 1);
        String courseTitle = (String) tableModel.getValueAt(selectedRow, 2);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete course:\n" +
                        "Code: " + courseCode + "\n" +
                        "Title: " + courseTitle + "?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = adminService.deleteCourse(courseId);
            if (success) {
                JOptionPane.showMessageDialog(this, "Course deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadCourseData();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete course.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void activateCourse() {
        int selectedRow = courseTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a course to activate.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int courseId = (Integer) tableModel.getValueAt(selectedRow, 0);
        boolean success = adminService.activateCourse(courseId);

        if (success) {
            JOptionPane.showMessageDialog(this, "Course activated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadCourseData();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to activate course.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deactivateCourse() {
        int selectedRow = courseTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a course to deactivate.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int courseId = (Integer) tableModel.getValueAt(selectedRow, 0);
        boolean success = adminService.deactivateCourse(courseId);

        if (success) {
            JOptionPane.showMessageDialog(this, "Course deactivated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadCourseData();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to deactivate course.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateStatusBar() {
        int totalCourses = tableModel.getRowCount();
        int activeCourses = 0;

        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if ("ACTIVE".equals(tableModel.getValueAt(i, 4))) {
                activeCourses++;
            }
        }

        // You can update a status label here if you add one to the panel
        System.out.println("Courses: " + activeCourses + " active / " + totalCourses + " total");
    }

    // Inner class for Course Dialog
    private class CourseDialog extends JDialog {
        private JTextField codeField, titleField, creditsField;
        private JTextArea descriptionArea;
        private JComboBox<String> statusCombo;
        private boolean success = false;
        private Course existingCourse;

        public CourseDialog(JFrame parent, String title, Course course) {
            super(parent, title, true);
            this.existingCourse = course;
            initializeDialog();
            if (course != null) {
                populateFields(course);
            }
        }

        private void initializeDialog() {
            setLayout(new BorderLayout(10, 10));
            setSize(500, 400);
            setLocationRelativeTo(getParent());
            setResizable(false);

            // Form panel
            JPanel formPanel = new JPanel(new GridBagLayout());
            formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.anchor = GridBagConstraints.WEST;

            // Code
            gbc.gridx = 0; gbc.gridy = 0;
            formPanel.add(new JLabel("Course Code:*"), gbc);
            gbc.gridx = 1;
            codeField = new JTextField(20);
            formPanel.add(codeField, gbc);

            // Title
            gbc.gridx = 0; gbc.gridy = 1;
            formPanel.add(new JLabel("Course Title:*"), gbc);
            gbc.gridx = 1;
            titleField = new JTextField(20);
            formPanel.add(titleField, gbc);

            // Credits
            gbc.gridx = 0; gbc.gridy = 2;
            formPanel.add(new JLabel("Credits:*"), gbc);
            gbc.gridx = 1;
            creditsField = new JTextField(20);
            formPanel.add(creditsField, gbc);

            // Status
            gbc.gridx = 0; gbc.gridy = 3;
            formPanel.add(new JLabel("Status:"), gbc);
            gbc.gridx = 1;
            statusCombo = new JComboBox<>(new String[]{"ACTIVE", "INACTIVE", "ARCHIVED"});
            formPanel.add(statusCombo, gbc);

            // Description
            gbc.gridx = 0; gbc.gridy = 4;
            formPanel.add(new JLabel("Description:"), gbc);
            gbc.gridx = 1;
            descriptionArea = new JTextArea(5, 20);
            descriptionArea.setLineWrap(true);
            descriptionArea.setWrapStyleWord(true);
            JScrollPane scrollPane = new JScrollPane(descriptionArea);
            formPanel.add(scrollPane, gbc);

            add(formPanel, BorderLayout.CENTER);

            // Buttons panel
            JPanel buttonPanel = new JPanel(new FlowLayout());
            JButton saveButton = new JButton("Save");
            JButton cancelButton = new JButton("Cancel");

            saveButton.setBackground(new Color(0, 120, 215));
            saveButton.setForeground(Color.WHITE);
            cancelButton.setBackground(new Color(200, 200, 200));

            buttonPanel.add(saveButton);
            buttonPanel.add(cancelButton);
            add(buttonPanel, BorderLayout.SOUTH);

            // Event listeners
            saveButton.addActionListener(e -> saveCourse());
            cancelButton.addActionListener(e -> dispose());

            // Enter key support
            getRootPane().setDefaultButton(saveButton);
        }

        private void populateFields(Course course) {
            codeField.setText(course.getCode());
            titleField.setText(course.getTitle());
            creditsField.setText(String.valueOf(course.getCredits()));
            descriptionArea.setText(course.getDescription() != null ? course.getDescription() : "");
            statusCombo.setSelectedItem(course.getStatus());
        }

        private void saveCourse() {
            try {
                String code = codeField.getText().trim();
                String title = titleField.getText().trim();
                String creditsText = creditsField.getText().trim();
                String description = descriptionArea.getText().trim();
                String status = (String) statusCombo.getSelectedItem();

                // Validation
                if (code.isEmpty() || title.isEmpty() || creditsText.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Please fill in all required fields.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (!ValidationUtil.isValidCourseCode(code)) {
                    JOptionPane.showMessageDialog(this, "Invalid course code format. Use format like: CS101, MATH201", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                int credits;
                try {
                    credits = Integer.parseInt(creditsText);
                    if (!ValidationUtil.isValidCredits(credits)) {
                        JOptionPane.showMessageDialog(this, "Credits must be between 1 and 6.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, "Credits must be a valid number.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Check for duplicate course code
                if (existingCourse == null || !existingCourse.getCode().equals(code)) {
                    if (adminService.courseCodeExists(code)) {
                        JOptionPane.showMessageDialog(this, "Course code already exists. Please use a different code.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }

                // Save course
                Course course = existingCourse != null ? existingCourse : new Course();
                course.setCourse(code, title, credits, description, status);

                boolean saveSuccess;
                if (existingCourse != null) {
                    saveSuccess = adminService.updateCourse(course);
                } else {
                    saveSuccess = adminService.createCourse(course);
                }

                if (saveSuccess) {
                    success = true;
                    JOptionPane.showMessageDialog(this, "Course saved successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to save course.", "Error", JOptionPane.ERROR_MESSAGE);
                }

            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error saving course: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        public boolean isSuccess() {
            return success;
        }
    }
}