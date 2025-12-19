package edu.univ.erp.ui;

import edu.univ.erp.domain.User;
import edu.univ.erp.domain.Course;
import edu.univ.erp.domain.Section;
import edu.univ.erp.service.StudentService;
import edu.univ.erp.service.MaintenanceService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class CoursePanel extends JPanel {
    private User currentUser;
    private StudentService studentService;
    private MaintenanceService maintenanceService;
    private JTable courseTable;
    private DefaultTableModel tableModel;
    private JButton viewSectionsButton;
    private JButton registerButton;

    public CoursePanel(User user) {
        this.currentUser = user;
        this.studentService = new StudentService();
        this.maintenanceService = new MaintenanceService();
        initializeUI();
        loadCourseData();
        checkMaintenanceMode();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());

        // Title
        JLabel titleLabel = new JLabel("Course Catalog", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        add(titleLabel, BorderLayout.NORTH);

        // Table
        String[] columns = {"Course Code", "Title", "Credits", "Description", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        courseTable = new JTable(tableModel);
        courseTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        courseTable.getTableHeader().setReorderingAllowed(false);

        JScrollPane scrollPane = new JScrollPane(courseTable);
        add(scrollPane, BorderLayout.CENTER);

        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton refreshButton = new JButton("Refresh");
        viewSectionsButton = new JButton("View Sections");
        registerButton = new JButton("Register for Course");

        refreshButton.addActionListener(e -> loadCourseData());
        viewSectionsButton.addActionListener(e -> viewSections());
        registerButton.addActionListener(e -> registerForCourse());

        buttonPanel.add(refreshButton);
        buttonPanel.add(viewSectionsButton);
        buttonPanel.add(registerButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void checkMaintenanceMode() {
        if (maintenanceService.isMaintenanceMode()) {
            // Disable action buttons
            viewSectionsButton.setEnabled(false);
            registerButton.setEnabled(false);

            // Change button text to show they're disabled
            viewSectionsButton.setText("View Sections (Disabled - Maintenance)");
            registerButton.setText("Register (Disabled - Maintenance)");

            // Change button colors to indicate disabled state
            viewSectionsButton.setBackground(Color.LIGHT_GRAY);
            registerButton.setBackground(Color.LIGHT_GRAY);

            // Add maintenance warning label
            JLabel maintenanceLabel = new JLabel("⚠️ MAINTENANCE MODE: Course enrollment disabled", JLabel.CENTER);
            maintenanceLabel.setForeground(Color.RED);
            maintenanceLabel.setFont(new Font("Arial", Font.BOLD, 12));
            maintenanceLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

            // Add the label above the table
            add(maintenanceLabel, BorderLayout.NORTH);

            System.out.println("MAINTENANCE MODE: Course catalog actions disabled for student: " + currentUser.getUsername());
        }
    }

    private void loadCourseData() {
        try {
            tableModel.setRowCount(0);
            List<Course> courses = studentService.getCourseCatalog();

            for (Course course : courses) {
                tableModel.addRow(new Object[]{
                        course.getCode(),
                        course.getTitle(),
                        course.getCredits(),
                        course.getDescription() != null ? course.getDescription() : "",
                        course.getStatus()
                });
            }

            if (courses.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No courses found.", "Information", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading courses: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void viewSections() {
        // Maintenance check at method start
        if (maintenanceService.isMaintenanceMode()) {
            JOptionPane.showMessageDialog(this,
                    "Cannot view sections during maintenance mode.\n" +
                            "Please try again after maintenance is complete.",
                    "Maintenance Mode",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int selectedRow = courseTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a course to view sections.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            String courseCode = (String) tableModel.getValueAt(selectedRow, 0);
            String courseTitle = (String) tableModel.getValueAt(selectedRow, 1);

            // Get sections for the selected course
            List<Section> sections = studentService.getSectionsByCourse(courseCode);

            if (sections.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "No sections available for course: " + courseCode,
                        "No Sections",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            // Create and show sections dialog
            showSectionsDialog(courseCode, courseTitle, sections);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error loading sections: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showSectionsDialog(String courseCode, String courseTitle, List<Section> sections) {
        JDialog sectionsDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "Sections for: " + courseTitle,
                true);
        sectionsDialog.setLayout(new BorderLayout());
        sectionsDialog.setSize(700, 420);
        sectionsDialog.setLocationRelativeTo(this);

        // Create table for sections
        String[] columns = {"Section ID", "Instructor", "Day/Time", "Room", "Capacity", "Available Seats", "Semester"};
        DefaultTableModel sectionsModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        for (Section section : sections) {
            sectionsModel.addRow(new Object[]{
                    section.getSectionId(),
                    section.getInstructorName(),
                    section.getDayTime(),
                    section.getRoom(),
                    section.getCapacity(),
                    section.getAvailableSeats(),
                    section.getSemester() + " " + section.getYear()
            });
        }

        JTable sectionsTable = new JTable(sectionsModel);
        sectionsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(sectionsTable);

        // Buttons panel for the dialog
        JPanel dialogButtonPanel = new JPanel(new FlowLayout());
        JButton registerSectionButton = new JButton("Register for Selected Section");
        JButton closeButton = new JButton("Close");

        // Disable register button in dialog during maintenance
        if (maintenanceService.isMaintenanceMode()) {
            registerSectionButton.setEnabled(false);
            registerSectionButton.setText("Register (Disabled - Maintenance)");
            registerSectionButton.setBackground(Color.LIGHT_GRAY);
        }

        registerSectionButton.addActionListener(e -> {
            // Maintenance check
            if (maintenanceService.isMaintenanceMode()) {
                JOptionPane.showMessageDialog(sectionsDialog,
                        "Course registration is disabled during maintenance mode.",
                        "Maintenance Mode",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            int selectedSectionRow = sectionsTable.getSelectedRow();
            if (selectedSectionRow == -1) {
                JOptionPane.showMessageDialog(sectionsDialog,
                        "Please select a section to register.",
                        "Warning",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Convert section ID to string for the service method
            int sectionId = (Integer) sectionsModel.getValueAt(selectedSectionRow, 0);

            // IMPORTANT: pass currentUser id as the student id
            String studentIdStr = String.valueOf(currentUser.getUserId());
            String sectionIdStr = String.valueOf(sectionId);

            // Call the centralized register method
            registerForSection(studentIdStr, sectionIdStr, sectionsDialog);
        });

        closeButton.addActionListener(e -> sectionsDialog.dispose());

        dialogButtonPanel.add(registerSectionButton);
        dialogButtonPanel.add(closeButton);

        sectionsDialog.add(scrollPane, BorderLayout.CENTER);
        sectionsDialog.add(dialogButtonPanel, BorderLayout.SOUTH);
        sectionsDialog.setVisible(true);
    }

    /**
     * Wrapper that converts string IDs to ints and calls the service's int-based register method.
     * This matches the StudentService.registerForSection(int studentId, int sectionId) method.
     */
    private void registerForSection(String studentId, String sectionId, JDialog parentDialog) {
        // Maintenance check already handled by caller, but keep defensive check
        if (maintenanceService.isMaintenanceMode()) {
            JOptionPane.showMessageDialog(parentDialog,
                    "Registration is disabled during maintenance mode.",
                    "Maintenance Mode",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int sid = Integer.parseInt(studentId);
            int secId = Integer.parseInt(sectionId);

            boolean success = studentService.registerForSection(sid, secId);

            if (success) {
                JOptionPane.showMessageDialog(parentDialog,
                        "Successfully registered for section: " + sectionId,
                        "Registration Successful",
                        JOptionPane.INFORMATION_MESSAGE);
                parentDialog.dispose();

                // Refresh UI so available seats / catalog reflect new registration
                loadCourseData();
            } else {
                JOptionPane.showMessageDialog(parentDialog,
                        "Failed to register for section. It may be full or you may already be registered.",
                        "Registration Failed",
                        JOptionPane.WARNING_MESSAGE);
            }
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(parentDialog,
                    "Invalid IDs (cannot parse): " + nfe.getMessage(),
                    "Registration Error",
                    JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(parentDialog,
                    "Error during registration: " + e.getMessage(),
                    "Registration Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void registerForCourse() {
        // Maintenance check at method start
        if (maintenanceService.isMaintenanceMode()) {
            JOptionPane.showMessageDialog(this,
                    "Cannot register for courses during maintenance mode.\n" +
                            "Please try again after maintenance is complete.",
                    "Maintenance Mode",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int selectedRow = courseTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a course to register.",
                    "Warning",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // This will automatically show sections and registration options
        viewSections();
    }
}
