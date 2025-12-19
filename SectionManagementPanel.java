package edu.univ.erp.ui;

import edu.univ.erp.domain.Section;
import edu.univ.erp.domain.Course;
import edu.univ.erp.domain.User;
import edu.univ.erp.service.AdminService;
import edu.univ.erp.data.EnrollmentDAO;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class SectionManagementPanel extends JPanel {
    private User currentUser;
    private AdminService adminService;
    private JTable sectionTable;
    private DefaultTableModel tableModel;

    public SectionManagementPanel(User user) {
        this.currentUser = user;
        this.adminService = new AdminService();
        initializeUI();
        loadSectionData();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Title
        JLabel titleLabel = new JLabel("Section Management", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(new Color(0, 70, 130));
        add(titleLabel, BorderLayout.NORTH);

        // Table
        String[] columns = {"Section ID", "Course", "Instructor", "Schedule", "Room", "Capacity", "Enrolled", "Status", "Semester", "Year"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0 || columnIndex == 5 || columnIndex == 6 || columnIndex == 9) return Integer.class;
                return String.class;
            }
        };

        sectionTable = new JTable(tableModel);
        sectionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        sectionTable.setRowHeight(25);
        sectionTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));

        // Set column widths
        sectionTable.getColumnModel().getColumn(0).setPreferredWidth(80);   // ID
        sectionTable.getColumnModel().getColumn(1).setPreferredWidth(150);  // Course
        sectionTable.getColumnModel().getColumn(2).setPreferredWidth(120);  // Instructor
        sectionTable.getColumnModel().getColumn(3).setPreferredWidth(150);  // Schedule
        sectionTable.getColumnModel().getColumn(4).setPreferredWidth(80);   // Room
        sectionTable.getColumnModel().getColumn(5).setPreferredWidth(70);   // Capacity
        sectionTable.getColumnModel().getColumn(6).setPreferredWidth(70);   // Enrolled
        sectionTable.getColumnModel().getColumn(7).setPreferredWidth(80);   // Status
        sectionTable.getColumnModel().getColumn(8).setPreferredWidth(100);  // Semester
        sectionTable.getColumnModel().getColumn(9).setPreferredWidth(60);   // Year

        JScrollPane scrollPane = new JScrollPane(sectionTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Sections"));
        add(scrollPane, BorderLayout.CENTER);

        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JButton refreshButton = new JButton("Refresh");
        JButton addButton = new JButton("Add Section");
        JButton editButton = new JButton("Edit Section");
        JButton deleteButton = new JButton("Delete Section");
        JButton viewEnrollmentsButton = new JButton("View Enrollments");

        // Style buttons
        addButton.setBackground(new Color(0, 120, 215));
        addButton.setForeground(Color.BLACK);
        editButton.setBackground(new Color(255, 140, 0));
        editButton.setForeground(Color.BLACK);
        deleteButton.setBackground(new Color(220, 20, 60));
        deleteButton.setForeground(Color.BLACK);
        viewEnrollmentsButton.setBackground(new Color(75, 0, 130));
        viewEnrollmentsButton.setForeground(Color.BLACK);

        buttonPanel.add(refreshButton);
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(viewEnrollmentsButton);

        add(buttonPanel, BorderLayout.SOUTH);

        // Event listeners
        refreshButton.addActionListener(e -> loadSectionData());
        addButton.addActionListener(e -> addSection());
        editButton.addActionListener(e -> editSection());
        deleteButton.addActionListener(e -> deleteSection());
        viewEnrollmentsButton.addActionListener(e -> viewEnrollments());

        // Double click to edit
        sectionTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    editSection();
                }
            }
        });
    }

    private void loadSectionData() {
        tableModel.setRowCount(0);
        List<Section> sections = adminService.getAllSections();

        for (Section section : sections) {
            // Get course name and instructor name using service methods
            Course course = getCourseById(section.getCourseId());
            User instructor = getUserById(section.getInstructorId());

            String courseName = (course != null) ? course.getCode() + " - " + course.getTitle() : "Course " + section.getCourseId();
            String instructorName = (instructor != null && instructor.getFullName() != null) ?
                    instructor.getFullName() : "Instructor " + section.getInstructorId();

            tableModel.addRow(new Object[]{
                    section.getSectionId(),
                    courseName,
                    instructorName,
                    section.getDayTime(),
                    section.getRoom(),
                    section.getCapacity(),
                    section.getEnrolledCount(),
                    section.getStatus(),
                    section.getSemester(),
                    section.getYear()
            });
        }

        updateStatusBar();
    }

    private void addSection() {
        // Create dialog for adding new section
        JDialog addDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Add New Section", true);
        addDialog.setLayout(new BorderLayout());
        addDialog.setSize(500, 450);
        addDialog.setLocationRelativeTo(this);

        // Form panel
        JPanel formPanel = new JPanel(new GridLayout(8, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Get available courses and instructors
        List<Course> courses = adminService.getAllCourses();
        List<User> instructors = getAllInstructors(); // Use our helper method

        JComboBox<Course> courseComboBox = new JComboBox<>();
        for (Course course : courses) {
            courseComboBox.addItem(course);
        }
        courseComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Course) {
                    Course course = (Course) value;
                    setText(course.getCode() + " - " + course.getTitle());
                }
                return this;
            }
        });

        JComboBox<User> instructorComboBox = new JComboBox<>();
        for (User instructor : instructors) {
            instructorComboBox.addItem(instructor);
        }
        instructorComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof User) {
                    User instructor = (User) value;
                    String displayName = instructor.getFullName() != null ?
                            instructor.getFullName() + " (" + instructor.getUsername() + ")" :
                            instructor.getUsername();
                    setText(displayName);
                }
                return this;
            }
        });

        JComboBox<String> semesterComboBox = new JComboBox<>(new String[]{"MONSOON", "SPRING", "SUMMER", "WINTER"});
        JComboBox<String> statusComboBox = new JComboBox<>(new String[]{"ACTIVE", "INACTIVE", "CANCELLED"});

        JTextField yearField = new JTextField(String.valueOf(java.time.Year.now().getValue()));
        JTextField scheduleField = new JTextField(); // e.g., "Mon Wed 10:00-11:30"
        JTextField roomField = new JTextField();
        JSpinner capacitySpinner = new JSpinner(new SpinnerNumberModel(30, 1, 200, 1));

        formPanel.add(new JLabel("Course:"));
        formPanel.add(courseComboBox);
        formPanel.add(new JLabel("Instructor:"));
        formPanel.add(instructorComboBox);
        formPanel.add(new JLabel("Schedule:"));
        formPanel.add(scheduleField);
        formPanel.add(new JLabel("Room:"));
        formPanel.add(roomField);
        formPanel.add(new JLabel("Capacity:"));
        formPanel.add(capacitySpinner);
        formPanel.add(new JLabel("Semester:"));
        formPanel.add(semesterComboBox);
        formPanel.add(new JLabel("Year:"));
        formPanel.add(yearField);
        formPanel.add(new JLabel("Status:"));
        formPanel.add(statusComboBox);

        addDialog.add(formPanel, BorderLayout.CENTER);

        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");

        saveButton.addActionListener(e -> {
            // Validation
            if (scheduleField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(addDialog, "Schedule is required!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (roomField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(addDialog, "Room is required!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                Integer.parseInt(yearField.getText().trim());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(addDialog, "Year must be a valid number!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Create new section using your Section constructor
            Course selectedCourse = (Course) courseComboBox.getSelectedItem();
            User selectedInstructor = (User) instructorComboBox.getSelectedItem();

            if (selectedCourse == null || selectedInstructor == null) {
                JOptionPane.showMessageDialog(addDialog, "Please select both course and instructor!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Create section with 0 as temporary ID (will be set by database)
            Section newSection = new Section(
                    0, // sectionId - will be generated by database
                    selectedCourse.getCourseId(),
                    selectedInstructor.getUserId(),
                    scheduleField.getText().trim(),
                    roomField.getText().trim(),
                    (Integer) capacitySpinner.getValue(),
                    (String) semesterComboBox.getSelectedItem(),
                    Integer.parseInt(yearField.getText().trim())
            );

            // Set status from combo box
            newSection.setStatus((String) statusComboBox.getSelectedItem());

            boolean success = adminService.createSection(newSection);
            if (success) {
                JOptionPane.showMessageDialog(addDialog, "Section created successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                addDialog.dispose();
                loadSectionData();
            } else {
                JOptionPane.showMessageDialog(addDialog, "Failed to create section. Please check the data and try again.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> addDialog.dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        addDialog.add(buttonPanel, BorderLayout.SOUTH);

        addDialog.setVisible(true);
    }

    private void editSection() {
        int selectedRow = sectionTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a section to edit.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int sectionId = (Integer) tableModel.getValueAt(selectedRow, 0);

        // Find the section to edit
        Section sectionToEdit = findSectionById(sectionId);
        if (sectionToEdit == null) {
            JOptionPane.showMessageDialog(this, "Section not found!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Create dialog for editing section
        JDialog editDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Edit Section", true);
        editDialog.setLayout(new BorderLayout());
        editDialog.setSize(500, 450);
        editDialog.setLocationRelativeTo(this);

        // Form panel
        JPanel formPanel = new JPanel(new GridLayout(8, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Get available courses and instructors
        List<Course> courses = adminService.getAllCourses();
        List<User> instructors = getAllInstructors();

        JComboBox<Course> courseComboBox = new JComboBox<>();
        Course currentCourse = getCourseById(sectionToEdit.getCourseId());
        for (Course course : courses) {
            courseComboBox.addItem(course);
            if (course.getCourseId() == sectionToEdit.getCourseId()) {
                courseComboBox.setSelectedItem(course);
            }
        }
        courseComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Course) {
                    Course course = (Course) value;
                    setText(course.getCode() + " - " + course.getTitle());
                }
                return this;
            }
        });

        JComboBox<User> instructorComboBox = new JComboBox<>();
        User currentInstructor = getUserById(sectionToEdit.getInstructorId());
        for (User instructor : instructors) {
            instructorComboBox.addItem(instructor);
            if (instructor.getUserId() == sectionToEdit.getInstructorId()) {
                instructorComboBox.setSelectedItem(instructor);
            }
        }
        instructorComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof User) {
                    User instructor = (User) value;
                    String displayName = instructor.getFullName() != null ?
                            instructor.getFullName() + " (" + instructor.getUsername() + ")" :
                            instructor.getUsername();
                    setText(displayName);
                }
                return this;
            }
        });

        JComboBox<String> semesterComboBox = new JComboBox<>(new String[]{"MONSOON", "SPRING", "SUMMER", "WINTER"});
        semesterComboBox.setSelectedItem(sectionToEdit.getSemester());

        JComboBox<String> statusComboBox = new JComboBox<>(new String[]{"ACTIVE", "INACTIVE", "CANCELLED"});
        statusComboBox.setSelectedItem(sectionToEdit.getStatus());

        JTextField yearField = new JTextField(String.valueOf(sectionToEdit.getYear()));
        JTextField scheduleField = new JTextField(sectionToEdit.getDayTime());
        JTextField roomField = new JTextField(sectionToEdit.getRoom());
        JSpinner capacitySpinner = new JSpinner(new SpinnerNumberModel(sectionToEdit.getCapacity(), 1, 200, 1));

        formPanel.add(new JLabel("Course:"));
        formPanel.add(courseComboBox);
        formPanel.add(new JLabel("Instructor:"));
        formPanel.add(instructorComboBox);
        formPanel.add(new JLabel("Schedule:"));
        formPanel.add(scheduleField);
        formPanel.add(new JLabel("Room:"));
        formPanel.add(roomField);
        formPanel.add(new JLabel("Capacity:"));
        formPanel.add(capacitySpinner);
        formPanel.add(new JLabel("Semester:"));
        formPanel.add(semesterComboBox);
        formPanel.add(new JLabel("Year:"));
        formPanel.add(yearField);
        formPanel.add(new JLabel("Status:"));
        formPanel.add(statusComboBox);

        editDialog.add(formPanel, BorderLayout.CENTER);

        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");

        saveButton.addActionListener(e -> {
            // Validation
            if (scheduleField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(editDialog, "Schedule is required!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (roomField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(editDialog, "Room is required!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                Integer.parseInt(yearField.getText().trim());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(editDialog, "Year must be a valid number!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Update section
            Course selectedCourse = (Course) courseComboBox.getSelectedItem();
            User selectedInstructor = (User) instructorComboBox.getSelectedItem();

            if (selectedCourse == null || selectedInstructor == null) {
                JOptionPane.showMessageDialog(editDialog, "Please select both course and instructor!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            sectionToEdit.setCourseId(selectedCourse.getCourseId());
            sectionToEdit.setInstructorId(selectedInstructor.getUserId());
            sectionToEdit.setDayTime(scheduleField.getText().trim());
            sectionToEdit.setRoom(roomField.getText().trim());
            sectionToEdit.setCapacity((Integer) capacitySpinner.getValue());
            sectionToEdit.setSemester((String) semesterComboBox.getSelectedItem());
            sectionToEdit.setYear(Integer.parseInt(yearField.getText().trim()));
            sectionToEdit.setStatus((String) statusComboBox.getSelectedItem());

            boolean success = adminService.updateSection(sectionToEdit);
            if (success) {
                JOptionPane.showMessageDialog(editDialog, "Section updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                editDialog.dispose();
                loadSectionData();
            } else {
                JOptionPane.showMessageDialog(editDialog, "Failed to update section.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> editDialog.dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        editDialog.add(buttonPanel, BorderLayout.SOUTH);

        editDialog.setVisible(true);
    }

    private void deleteSection() {
        int selectedRow = sectionTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a section to delete.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int sectionId = (Integer) tableModel.getValueAt(selectedRow, 0);
        String courseName = (String) tableModel.getValueAt(selectedRow, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete section for:\n" +
                        "Course: " + courseName + "\n" +
                        "Section ID: " + sectionId + "?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = adminService.deleteSection(sectionId);
            if (success) {
                JOptionPane.showMessageDialog(this, "Section deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadSectionData();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete section. It may have existing enrollments.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void viewEnrollments() {
        int selectedRow = sectionTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a section to view enrollments.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int sectionId = (Integer) tableModel.getValueAt(selectedRow, 0);
        String courseName = (String) tableModel.getValueAt(selectedRow, 1);

        // Show the enrollment list in a proper table
        showEnrollmentsForSection(sectionId, courseName);
    }

    // Helper methods to work with your existing AdminService
    private Course getCourseById(int courseId) {
        // Since AdminService doesn't have getCourseById, we'll search through the list
        List<Course> courses = adminService.getAllCourses();
        for (Course course : courses) {
            if (course.getCourseId() == courseId) {
                return course;
            }
        }
        return null;
    }

    private User getUserById(int userId) {
        return adminService.getUserById(userId);
    }

    private List<User> getAllInstructors() {
        // Get all users and filter for instructors
        List<User> allUsers = adminService.getAllUsers();
        return allUsers.stream()
                .filter(user -> "INSTRUCTOR".equalsIgnoreCase(user.getRole()))
                .toList();
    }

    private Section findSectionById(int sectionId) {
        List<Section> sections = adminService.getAllSections();
        for (Section section : sections) {
            if (section.getSectionId() == sectionId) {
                return section;
            }
        }
        return null;
    }

    private void updateStatusBar() {
        int totalSections = tableModel.getRowCount();
        int activeSections = 0;
        int totalEnrollments = 0;

        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if ("ACTIVE".equals(tableModel.getValueAt(i, 7))) {
                activeSections++;
            }
            totalEnrollments += (Integer) tableModel.getValueAt(i, 6);
        }

        System.out.println("Sections: " + activeSections + " active / " + totalSections + " total | Enrollments: " + totalEnrollments);
    }
    private void showEnrollmentsForSection(int sectionId, String courseName) {
        EnrollmentDAO enrollmentDAO = new EnrollmentDAO();
        List<String[]> enrollments = enrollmentDAO.getSectionEnrollmentDetails(sectionId);

        if (enrollments.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No students enrolled in this section.",
                    "Enrollments - " + courseName,
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Build table
        String[] columns = {"Enrollment ID", "Student ID", "Name", "Enrolled At", "Status"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);

        for (String[] row : enrollments) {
            model.addRow(row);
        }

        JTable table = new JTable(model);
        JScrollPane scroll = new JScrollPane(table);

        // Show inside dialog
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "Enrollments for " + courseName, true);

        dialog.add(scroll);
        dialog.setSize(600, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

}