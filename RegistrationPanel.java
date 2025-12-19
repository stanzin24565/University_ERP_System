package edu.univ.erp.ui;

import edu.univ.erp.domain.User;
import edu.univ.erp.service.StudentService;
import edu.univ.erp.service.MaintenanceService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class RegistrationPanel extends JPanel {
    private User currentUser;
    private StudentService studentService;
    private MaintenanceService maintenanceService;

    private JTable enrollmentTable;
    private DefaultTableModel tableModel;

    private JButton dropButton;
    private JButton registerButton;

    public RegistrationPanel(User user) {
        this.currentUser = user;
        this.studentService = new StudentService();
        this.maintenanceService = new MaintenanceService();

        initializeUI();
        loadEnrollmentData();
        checkMaintenanceMode();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("My Course Registrations", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        add(titleLabel, BorderLayout.NORTH);

        // Table Columns
        String[] columns = {
                "Enrollment ID",
                "Course Code",
                "Course Title",
                "Section",
                "Status",
                "Enrolled Date"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };

        enrollmentTable = new JTable(tableModel);
        enrollmentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        add(new JScrollPane(enrollmentTable), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton refreshButton = new JButton("Refresh");
        dropButton = new JButton("Drop Course");
        registerButton = new JButton("Register New Course");

        refreshButton.addActionListener(e -> loadEnrollmentData());
        dropButton.addActionListener(e -> dropCourse());
        registerButton.addActionListener(e -> registerNewCourse());

        buttonPanel.add(refreshButton);
        buttonPanel.add(dropButton);
        buttonPanel.add(registerButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadEnrollmentData() {
        try {
            tableModel.setRowCount(0);

            // ⬇️ FIXED: load enriched enrollment rows
            List<String[]> rows =
                    studentService.getStudentRegistrationDetails(currentUser.getUserId());

            for (String[] r : rows) {
                tableModel.addRow(r);
            }

            if (rows.isEmpty()) {
                System.out.println("DEBUG: No enrollments found.");
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error loading enrollments: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void checkMaintenanceMode() {
        if (maintenanceService.isMaintenanceMode()) {
            dropButton.setEnabled(false);
            registerButton.setEnabled(false);

            dropButton.setText("Drop Course (Disabled - Maintenance)");
            registerButton.setText("Register (Disabled - Maintenance)");

            JLabel warning = new JLabel(
                    "⚠️ MAINTENANCE MODE: Course registration/drop disabled",
                    JLabel.CENTER);
            warning.setForeground(Color.RED);
            warning.setFont(new Font("Arial", Font.BOLD, 12));

            add(warning, BorderLayout.NORTH);
        }
    }

    private void dropCourse() {
        if (maintenanceService.isMaintenanceMode()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Cannot drop courses during maintenance mode.",
                    "Maintenance Mode",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Check drop deadline
        if (!isBeforeDropDeadline()) {
            JOptionPane.showMessageDialog(
                    this,
                    "The drop deadline has passed. You can no longer drop courses.",
                    "Drop Deadline Exceeded",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int row = enrollmentTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this,
                    "Select a course to drop.",
                    "Warning",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int enrollmentId = Integer.parseInt(tableModel.getValueAt(row, 0).toString());

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure?",
                "Drop Course",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean ok = studentService.dropSection(
                    enrollmentId,
                    currentUser.getUserId());

            if (ok) {
                JOptionPane.showMessageDialog(this, "Course dropped.");
                loadEnrollmentData();
            } else {
                JOptionPane.showMessageDialog(this, "Drop failed.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private boolean isBeforeDropDeadline() {
        try {
            // Get drop deadline from settings
            edu.univ.erp.domain.Settings setting = new edu.univ.erp.data.SettingsDAO().findByKey("drop_deadline");
            if (setting == null) {
                return true; // Allow drop if no deadline is set
            }

            String deadlineStr = setting.getValue(); // Expected format: YYYY-MM-DD
            java.time.LocalDate deadline = java.time.LocalDate.parse(deadlineStr);
            java.time.LocalDate today = java.time.LocalDate.now();

            return today.isBefore(deadline);
        } catch (Exception e) {
            System.err.println("Error checking drop deadline: " + e.getMessage());
            return true; // Allow drop if there's an error
        }
    }

    private void registerNewCourse() {
        if (maintenanceService.isMaintenanceMode()) {
            JOptionPane.showMessageDialog(this,
                    "Registration disabled during maintenance.",
                    "Maintenance Mode",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Show a dialog with the course catalog
        JDialog catalogDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "Course Catalog - Register New Course",
                true);
        catalogDialog.setLayout(new BorderLayout());
        catalogDialog.setSize(700, 500);
        catalogDialog.setLocationRelativeTo(this);

        // Create table for courses
        String[] columns = {"Course Code", "Title", "Credits"};
        DefaultTableModel catalogModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        try {
            List<edu.univ.erp.domain.Course> courses = studentService.getCourseCatalog();
            for (edu.univ.erp.domain.Course course : courses) {
                catalogModel.addRow(new Object[]{
                        course.getCode(),
                        course.getTitle(),
                        course.getCredits()
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(catalogDialog,
                    "Error loading courses: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }

        JTable catalogTable = new JTable(catalogModel);
        catalogTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(catalogTable);

        // Buttons panel for the dialog
        JPanel dialogButtonPanel = new JPanel(new FlowLayout());
        JButton viewSectionsButton = new JButton("View Sections");
        JButton closeButton = new JButton("Close");

        viewSectionsButton.addActionListener(e -> {
            int selectedRow = catalogTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(catalogDialog,
                        "Please select a course to view sections.",
                        "Warning",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            String courseCode = (String) catalogModel.getValueAt(selectedRow, 0);
            String courseTitle = (String) catalogModel.getValueAt(selectedRow, 1);

            try {
                List<edu.univ.erp.domain.Section> sections = studentService.getSectionsByCourse(courseCode);

                if (sections.isEmpty()) {
                    JOptionPane.showMessageDialog(catalogDialog,
                            "No sections available for course: " + courseCode,
                            "No Sections",
                            JOptionPane.INFORMATION_MESSAGE);
                    return;
                }

                // Show sections dialog
                showSectionsDialogForRegistration(courseCode, courseTitle, sections, catalogDialog);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(catalogDialog,
                        "Error loading sections: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        closeButton.addActionListener(e -> catalogDialog.dispose());

        dialogButtonPanel.add(viewSectionsButton);
        dialogButtonPanel.add(closeButton);

        catalogDialog.add(scrollPane, BorderLayout.CENTER);
        catalogDialog.add(dialogButtonPanel, BorderLayout.SOUTH);
        catalogDialog.setVisible(true);
    }

    private void showSectionsDialogForRegistration(String courseCode, String courseTitle, 
                                                   List<edu.univ.erp.domain.Section> sections, 
                                                   JDialog parentDialog) {
        JDialog sectionsDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "Sections for: " + courseTitle,
                true);
        sectionsDialog.setLayout(new BorderLayout());
        sectionsDialog.setSize(700, 420);
        sectionsDialog.setLocationRelativeTo(parentDialog);

        // Create table for sections
        String[] columns = {"Section ID", "Instructor", "Day/Time", "Room", "Capacity", "Available Seats", "Semester"};
        DefaultTableModel sectionsModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        for (edu.univ.erp.domain.Section section : sections) {
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

        registerSectionButton.addActionListener(e -> {
            int selectedSectionRow = sectionsTable.getSelectedRow();
            if (selectedSectionRow == -1) {
                JOptionPane.showMessageDialog(sectionsDialog,
                        "Please select a section to register.",
                        "Warning",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            int sectionId = (Integer) sectionsModel.getValueAt(selectedSectionRow, 0);

            try {
                boolean success = studentService.registerForSection(currentUser.getUserId(), sectionId);

                if (success) {
                    JOptionPane.showMessageDialog(sectionsDialog,
                            "Successfully registered for section: " + sectionId,
                            "Registration Successful",
                            JOptionPane.INFORMATION_MESSAGE);
                    sectionsDialog.dispose();
                    parentDialog.dispose();
                    loadEnrollmentData();  // Refresh the enrollment table
                } else {
                    JOptionPane.showMessageDialog(sectionsDialog,
                            "Failed to register for section. It may be full or you may already be registered.",
                            "Registration Failed",
                            JOptionPane.WARNING_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(sectionsDialog,
                        "Error during registration: " + ex.getMessage(),
                        "Registration Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        closeButton.addActionListener(e -> sectionsDialog.dispose());

        dialogButtonPanel.add(registerSectionButton);
        dialogButtonPanel.add(closeButton);

        sectionsDialog.add(scrollPane, BorderLayout.CENTER);
        sectionsDialog.add(dialogButtonPanel, BorderLayout.SOUTH);
        sectionsDialog.setVisible(true);
    }
}
