package edu.univ.erp.ui;

import edu.univ.erp.domain.Enrollment;
import edu.univ.erp.domain.Grade;
import edu.univ.erp.domain.User;
import edu.univ.erp.service.StudentService;
import edu.univ.erp.service.MaintenanceService;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class StudentGradesPanel extends JPanel {
    private User currentUser;
    private StudentService studentService;
    private MaintenanceService maintenanceService;
    private JTable gradesTable;
    private DefaultTableModel tableModel;
    private JLabel statusLabel;
    private JComboBox<String> semesterComboBox;
    private JButton refreshButton;
    private JButton exportButton;
    private JButton calculateGPAButton;
    private JButton requestTranscriptButton;

    public StudentGradesPanel(User user) {
        this.currentUser = user;
        this.studentService = new StudentService();
        this.maintenanceService = new MaintenanceService();
        initializeUI();
        loadStudentGrades();
        checkMaintenanceMode();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Title
        JLabel titleLabel = new JLabel("My Grades & Academic Record", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(new Color(0, 70, 130));
        add(titleLabel, BorderLayout.NORTH);

        // Control panel
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controlPanel.setBorder(BorderFactory.createTitledBorder("Filter Options"));

        controlPanel.add(new JLabel("Semester:"));
        semesterComboBox = new JComboBox<>(new String[]{"All Semesters", "Fall 2024", "Spring 2024", "Fall 2023", "Spring 2023"});
        controlPanel.add(semesterComboBox);

        refreshButton = new JButton("Refresh Grades");
        refreshButton.setBackground(new Color(0, 120, 215));
        refreshButton.setForeground(Color.BLACK);
        controlPanel.add(refreshButton);

        add(controlPanel, BorderLayout.NORTH);

        // Grades table
        String[] columns = {"Course Code", "Course Title", "Credits", "Assignment 1", "Assignment 2", "Midterm", "Final Exam", "Project", "Total Grade", "Letter Grade", "Grade Points", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Read-only for students
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 2) return Integer.class; // Credits
                if (columnIndex >= 3 && columnIndex <= 8) return Double.class; // Grades
                if (columnIndex == 10) return Double.class; // Grade Points
                return String.class;
            }
        };

        gradesTable = new JTable(tableModel);
        gradesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        gradesTable.setRowHeight(25);
        gradesTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));

        // Set column widths
        gradesTable.getColumnModel().getColumn(0).setPreferredWidth(100);  // Course Code
        gradesTable.getColumnModel().getColumn(1).setPreferredWidth(200);  // Course Title
        gradesTable.getColumnModel().getColumn(2).setPreferredWidth(60);   // Credits
        gradesTable.getColumnModel().getColumn(3).setPreferredWidth(90);   // Assignment 1
        gradesTable.getColumnModel().getColumn(4).setPreferredWidth(90);   // Assignment 2
        gradesTable.getColumnModel().getColumn(5).setPreferredWidth(70);   // Midterm
        gradesTable.getColumnModel().getColumn(6).setPreferredWidth(80);   // Final Exam
        gradesTable.getColumnModel().getColumn(7).setPreferredWidth(70);   // Project
        gradesTable.getColumnModel().getColumn(8).setPreferredWidth(80);   // Total Grade
        gradesTable.getColumnModel().getColumn(9).setPreferredWidth(80);   // Letter Grade
        gradesTable.getColumnModel().getColumn(10).setPreferredWidth(80);  // Grade Points
        gradesTable.getColumnModel().getColumn(11).setPreferredWidth(80);  // Status

        JScrollPane scrollPane = new JScrollPane(gradesTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Course Grades"));
        add(scrollPane, BorderLayout.CENTER);

        // Status and summary panel
        JPanel summaryPanel = createSummaryPanel();
        add(summaryPanel, BorderLayout.SOUTH);

        // Event listeners
        refreshButton.addActionListener(e -> loadStudentGrades());
        semesterComboBox.addActionListener(e -> filterBySemester());

        // Double click to view grade details
        gradesTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    viewGradeDetails();
                }
            }
        });
    }

    private void checkMaintenanceMode() {
        if (maintenanceService.isMaintenanceMode()) {
            // Disable action buttons (export, GPA calculation, transcript requests)
            exportButton.setEnabled(false);
            calculateGPAButton.setEnabled(false);
            requestTranscriptButton.setEnabled(false);

            // Change button text to show they're disabled
            exportButton.setText("Export (Disabled - Maintenance)");
            calculateGPAButton.setText("Calculate GPA (Disabled - Maintenance)");
            requestTranscriptButton.setText("Request Transcript (Disabled - Maintenance)");

            // Change button colors to indicate disabled state
            exportButton.setBackground(Color.LIGHT_GRAY);
            calculateGPAButton.setBackground(Color.LIGHT_GRAY);
            requestTranscriptButton.setBackground(Color.LIGHT_GRAY);

            // Add maintenance warning label
            JLabel maintenanceLabel = new JLabel("⚠️ MAINTENANCE MODE: Grade-related actions disabled", JLabel.CENTER);
            maintenanceLabel.setForeground(Color.RED);
            maintenanceLabel.setFont(new Font("Arial", Font.BOLD, 12));
            maintenanceLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

            // Add the label above the table
            add(maintenanceLabel, BorderLayout.NORTH);

            System.out.println("MAINTENANCE MODE: Grade actions disabled for student: " + currentUser.getUsername());
        }
    }

    private JPanel createSummaryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        // Status label
        statusLabel = new JLabel("Loading your grades...", JLabel.CENTER);
        statusLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        statusLabel.setForeground(Color.BLACK);

        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout());

        exportButton = new JButton("Export Grade Report");
        calculateGPAButton = new JButton("Calculate GPA");
        requestTranscriptButton = new JButton("Request Official Transcript");

        // Style buttons
        exportButton.setBackground(new Color(50, 205, 50));
        exportButton.setForeground(Color.BLACK);
        calculateGPAButton.setBackground(new Color(0, 120, 215));
        calculateGPAButton.setForeground(Color.BLACK);
        requestTranscriptButton.setBackground(new Color(75, 0, 130));
        requestTranscriptButton.setForeground(Color.BLACK);

        buttonPanel.add(exportButton);
        buttonPanel.add(calculateGPAButton);
        buttonPanel.add(requestTranscriptButton);

        // GPA summary panel
        JPanel gpaPanel = new JPanel(new GridLayout(1, 4, 10, 5));
        gpaPanel.setBorder(BorderFactory.createTitledBorder("Academic Summary"));

        JLabel currentGPALabel = new JLabel("Current GPA: ---", JLabel.CENTER);
        JLabel totalCreditsLabel = new JLabel("Total Credits: ---", JLabel.CENTER);
        JLabel completedCoursesLabel = new JLabel("Completed: ---", JLabel.CENTER);
        JLabel inProgressLabel = new JLabel("In Progress: ---", JLabel.CENTER);

        currentGPALabel.setFont(new Font("Arial", Font.BOLD, 12));
        totalCreditsLabel.setFont(new Font("Arial", Font.BOLD, 12));
        completedCoursesLabel.setFont(new Font("Arial", Font.BOLD, 12));
        inProgressLabel.setFont(new Font("Arial", Font.BOLD, 12));

        currentGPALabel.setForeground(new Color(0, 100, 0));
        totalCreditsLabel.setForeground(Color.BLUE);
        completedCoursesLabel.setForeground(new Color(139, 0, 0));
        inProgressLabel.setForeground(new Color(255, 140, 0));

        gpaPanel.add(currentGPALabel);
        gpaPanel.add(totalCreditsLabel);
        gpaPanel.add(completedCoursesLabel);
        gpaPanel.add(inProgressLabel);

        panel.add(buttonPanel, BorderLayout.NORTH);
        panel.add(gpaPanel, BorderLayout.CENTER);
        panel.add(statusLabel, BorderLayout.SOUTH);

        // Event listeners for buttons
        exportButton.addActionListener(e -> exportGradeReport());
        calculateGPAButton.addActionListener(e -> calculateGPA());
        requestTranscriptButton.addActionListener(e -> requestTranscript());

        return panel;
    }

    private void loadStudentGrades() {
        tableModel.setRowCount(0);
        statusLabel.setText("Loading your grades...");

        try {
            // Get student's enrollments
            List<Enrollment> enrollments = studentService.getStudentEnrollments(currentUser.getUserId());

            int completedCourses = 0;
            int inProgressCourses = 0;
            double totalGradePoints = 0;
            int totalCredits = 0;

            for (Enrollment enrollment : enrollments) {
                if ("REGISTERED".equals(enrollment.getStatus()) || "COMPLETED".equals(enrollment.getStatus())) {
                    // Get grades for this enrollment
                    List<Grade> grades = studentService.getGradesForEnrollment(enrollment.getEnrollmentId());

                    // Get course details (you would need to implement this in your service)
                    String courseCode = getCourseCode(enrollment.getSectionId());
                    String courseTitle = getCourseTitle(enrollment.getSectionId());
                    int credits = getCourseCredits(enrollment.getSectionId());

                    // Calculate grades
                    Double assignment1 = getGradeValue(grades, "Assignment 1");
                    Double assignment2 = getGradeValue(grades, "Assignment 2");
                    Double midterm = getGradeValue(grades, "Midterm");
                    Double finalExam = getGradeValue(grades, "Final Exam");
                    Double project = getGradeValue(grades, "Project");

                    Double totalGrade = calculateTotalGrade(assignment1, assignment2, midterm, finalExam, project);
                    String letterGrade = calculateLetterGrade(totalGrade);
                    Double gradePoints = calculateGradePoints(letterGrade, credits);

                    String status = "REGISTERED".equals(enrollment.getStatus()) ? "In Progress" : "Completed";

                    if ("Completed".equals(status)) {
                        completedCourses++;
                        if (gradePoints != null) {
                            totalGradePoints += gradePoints;
                            totalCredits += credits;
                        }
                    } else {
                        inProgressCourses++;
                    }

                    tableModel.addRow(new Object[]{
                            courseCode,
                            courseTitle,
                            credits,
                            assignment1,
                            assignment2,
                            midterm,
                            finalExam,
                            project,
                            totalGrade,
                            letterGrade,
                            gradePoints,
                            status
                    });
                }
            }

            // Update summary
            updateGPASummary(totalGradePoints, totalCredits, completedCourses, inProgressCourses);
            statusLabel.setText("Successfully loaded " + enrollments.size() + " course records.");

        } catch (Exception e) {
            statusLabel.setText("Error loading grades: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Error loading your grades: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void filterBySemester() {
        String selectedSemester = (String) semesterComboBox.getSelectedItem();
        if ("All Semesters".equals(selectedSemester)) {
            loadStudentGrades();
        } else {
            // Filter logic would be implemented here
            statusLabel.setText("Filtered by: " + selectedSemester);
            // You would need to modify your service to filter by semester
        }
    }

    private void viewGradeDetails() {
        // Maintenance check - viewing details is read-only, so we allow it
        // But we can add a note if needed
        if (maintenanceService.isMaintenanceMode()) {
            // Just log it, but allow viewing since it's read-only
            System.out.println("MAINTENANCE MODE: Student viewing grade details (read-only)");
        }

        int selectedRow = gradesTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a course to view grade details.",
                    "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String courseCode = (String) tableModel.getValueAt(selectedRow, 0);
        String courseTitle = (String) tableModel.getValueAt(selectedRow, 1);
        Double assignment1 = (Double) tableModel.getValueAt(selectedRow, 3);
        Double assignment2 = (Double) tableModel.getValueAt(selectedRow, 4);
        Double midterm = (Double) tableModel.getValueAt(selectedRow, 5);
        Double finalExam = (Double) tableModel.getValueAt(selectedRow, 6);
        Double project = (Double) tableModel.getValueAt(selectedRow, 7);
        Double totalGrade = (Double) tableModel.getValueAt(selectedRow, 8);
        String letterGrade = (String) tableModel.getValueAt(selectedRow, 9);
        String status = (String) tableModel.getValueAt(selectedRow, 11);

        // Create detailed grade breakdown
        StringBuilder details = new StringBuilder();
        details.append("Grade Details for ").append(courseCode).append("\n");
        details.append("================================\n\n");
        details.append("Course: ").append(courseTitle).append("\n");
        details.append("Status: ").append(status).append("\n\n");

        details.append("Grade Breakdown:\n");
        details.append("----------------\n");

        if (assignment1 != null) {
            details.append(String.format("Assignment 1: %6.2f/100\n", assignment1));
        }
        if (assignment2 != null) {
            details.append(String.format("Assignment 2: %6.2f/100\n", assignment2));
        }
        if (midterm != null) {
            details.append(String.format("Midterm:      %6.2f/100\n", midterm));
        }
        if (finalExam != null) {
            details.append(String.format("Final Exam:   %6.2f/100\n", finalExam));
        }
        if (project != null) {
            details.append(String.format("Project:      %6.2f/100\n", project));
        }

        details.append("\n");
        details.append(String.format("Total Grade:  %6.2f/100\n", totalGrade != null ? totalGrade : 0.0));
        details.append(String.format("Letter Grade: %s\n", letterGrade));

        // Add grade interpretation
        details.append("\nGrade Interpretation:\n");
        details.append("---------------------\n");
        details.append(getGradeInterpretation(letterGrade));

        JTextArea textArea = new JTextArea(details.toString());
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        textArea.setBackground(new Color(240, 240, 240));

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(400, 350));

        JOptionPane.showMessageDialog(this, scrollPane,
                "Grade Details - " + courseCode,
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void exportGradeReport() {
        // Maintenance check at method start
        if (maintenanceService.isMaintenanceMode()) {
            JOptionPane.showMessageDialog(this,
                    "Cannot export grade reports during maintenance mode.\n" +
                            "Please try again after maintenance is complete.",
                    "Maintenance Mode",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No grade data to export.",
                    "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int choice = JOptionPane.showOptionDialog(this,
                "Choose export format:",
                "Export Grade Report",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                new String[]{"PDF Report", "Excel Spreadsheet", "CSV File", "Cancel"},
                "PDF Report");

        if (choice != 3) { // Not Cancel
            String format = "";
            switch (choice) {
                case 0: format = "PDF"; break;
                case 1: format = "Excel"; break;
                case 2: format = "CSV"; break;
            }

            JOptionPane.showMessageDialog(this,
                    "Exporting grade report as " + format + " format...\n\n" +
                            "The report will include:\n" +
                            "- All your course grades\n" +
                            "- Grade breakdown by component\n" +
                            "- GPA calculation\n" +
                            "- Academic summary\n" +
                            "- Official university header\n" +
                            "- Export timestamp: " + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                    "Export Grade Report",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void calculateGPA() {
        // Maintenance check at method start
        if (maintenanceService.isMaintenanceMode()) {
            JOptionPane.showMessageDialog(this,
                    "Cannot calculate GPA during maintenance mode.\n" +
                            "Please try again after maintenance is complete.",
                    "Maintenance Mode",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        double totalGradePoints = 0;
        int totalCredits = 0;
        int calculatedCourses = 0;

        for (int row = 0; row < tableModel.getRowCount(); row++) {
            String status = (String) tableModel.getValueAt(row, 11);
            if ("Completed".equals(status)) {
                Double gradePoints = (Double) tableModel.getValueAt(row, 10);
                Integer credits = (Integer) tableModel.getValueAt(row, 2);

                if (gradePoints != null && credits != null) {
                    totalGradePoints += gradePoints;
                    totalCredits += credits;
                    calculatedCourses++;
                }
            }
        }

        if (totalCredits > 0) {
            double gpa = totalGradePoints / totalCredits;
            String gpaText = String.format("%.2f", gpa);

            // Determine GPA standing
            String standing = getGPAStanding(gpa);

            JOptionPane.showMessageDialog(this,
                    "GPA Calculation Results:\n\n" +
                            "Total Grade Points: " + String.format("%.2f", totalGradePoints) + "\n" +
                            "Total Credits: " + totalCredits + "\n" +
                            "Courses Calculated: " + calculatedCourses + "\n\n" +
                            "Cumulative GPA: " + gpaText + "\n" +
                            "Academic Standing: " + standing + "\n\n" +
                            getGPAInterpretation(gpa),
                    "GPA Calculation",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                    "No completed courses found for GPA calculation.\n" +
                            "GPA is calculated only for courses with 'Completed' status.",
                    "GPA Calculation",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void requestTranscript() {
        // Maintenance check at method start
        if (maintenanceService.isMaintenanceMode()) {
            JOptionPane.showMessageDialog(this,
                    "Cannot request transcripts during maintenance mode.\n" +
                            "Please try again after maintenance is complete.",
                    "Maintenance Mode",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        JOptionPane.showMessageDialog(this,
                "Official Transcript Request\n\n" +
                        "This will request an official transcript from the Registrar's Office.\n\n" +
                        "Options:\n" +
                        "• Electronic Transcript (PDF) - Free\n" +
                        "• Printed Transcript - $5.00 per copy\n" +
                        "• Rush Processing - Additional $10.00\n\n" +
                        "Delivery Methods:\n" +
                        "• Email (instant)\n" +
                        "• Mail (5-7 business days)\n" +
                        "• Pickup at Registrar's Office\n\n" +
                        "Would you like to proceed with the transcript request?",
                "Request Official Transcript",
                JOptionPane.QUESTION_MESSAGE);
    }

    // Helper methods
    private Double getGradeValue(List<Grade> grades, String component) {
        return grades.stream()
                .filter(g -> component.equals(g.getComponent()))
                .findFirst()
                .map(Grade::getScore)
                .orElse(null);
    }

    private Double calculateTotalGrade(Double a1, Double a2, Double midterm, Double finalExam, Double project) {
        if (a1 == null && a2 == null && midterm == null && finalExam == null && project == null) {
            return null;
        }

        double total = 0;
        double weightSum = 0;

        if (a1 != null) { total += a1 * 0.15; weightSum += 0.15; }
        if (a2 != null) { total += a2 * 0.15; weightSum += 0.15; }
        if (midterm != null) { total += midterm * 0.25; weightSum += 0.25; }
        if (finalExam != null) { total += finalExam * 0.30; weightSum += 0.30; }
        if (project != null) { total += project * 0.15; weightSum += 0.15; }

        return weightSum > 0 ? Math.round(total * 100.0) / 100.0 : null;
    }

    private String calculateLetterGrade(Double totalGrade) {
        if (totalGrade == null) return "N/A";

        if (totalGrade >= 93) return "A";
        if (totalGrade >= 90) return "A-";
        if (totalGrade >= 87) return "B+";
        if (totalGrade >= 83) return "B";
        if (totalGrade >= 80) return "B-";
        if (totalGrade >= 77) return "C+";
        if (totalGrade >= 73) return "C";
        if (totalGrade >= 70) return "C-";
        if (totalGrade >= 67) return "D+";
        if (totalGrade >= 63) return "D";
        if (totalGrade >= 60) return "D-";
        return "F";
    }

    private Double calculateGradePoints(String letterGrade, int credits) {
        if (letterGrade == null || "N/A".equals(letterGrade)) return null;

        double points = 0;
        switch (letterGrade) {
            case "A": points = 4.0; break;
            case "A-": points = 3.7; break;
            case "B+": points = 3.3; break;
            case "B": points = 3.0; break;
            case "B-": points = 2.7; break;
            case "C+": points = 2.3; break;
            case "C": points = 2.0; break;
            case "C-": points = 1.7; break;
            case "D+": points = 1.3; break;
            case "D": points = 1.0; break;
            case "D-": points = 0.7; break;
            case "F": points = 0.0; break;
            default: return null;
        }

        return points * credits;
    }

    private void updateGPASummary(double totalGradePoints, int totalCredits, int completedCourses, int inProgressCourses) {
        // This would update the summary labels in the UI
        double gpa = totalCredits > 0 ? totalGradePoints / totalCredits : 0.0;

        // You would update the labels here with the calculated values
        System.out.println(String.format("GPA: %.2f, Credits: %d, Completed: %d, In Progress: %d",
                gpa, totalCredits, completedCourses, inProgressCourses));
    }

    private String getGradeInterpretation(String letterGrade) {
        switch (letterGrade) {
            case "A": return "Excellent - Outstanding achievement";
            case "A-": return "Excellent - High level of achievement";
            case "B+": return "Good - Very good performance";
            case "B": return "Good - Good performance";
            case "B-": return "Good - Satisfactory performance";
            case "C+": return "Satisfactory - Acceptable performance";
            case "C": return "Satisfactory - Average performance";
            case "C-": return "Satisfactory - Minimum passing grade for major requirements";
            case "D+": return "Passing - Below average performance";
            case "D": return "Passing - Poor performance";
            case "D-": return "Passing - Minimum passing grade";
            case "F": return "Failing - Unsatisfactory performance";
            default: return "Grade not available or course in progress";
        }
    }

    private String getGPAStanding(double gpa) {
        if (gpa >= 3.7) return "Dean's List - Highest Honors";
        if (gpa >= 3.5) return "Dean's List - High Honors";
        if (gpa >= 3.0) return "Dean's List - Honors";
        if (gpa >= 2.0) return "Good Standing";
        if (gpa >= 1.7) return "Academic Warning";
        return "Academic Probation";
    }

    private String getGPAInterpretation(double gpa) {
        if (gpa >= 3.7) return "Outstanding academic performance! Keep up the excellent work!";
        if (gpa >= 3.0) return "Strong academic performance. You're doing great!";
        if (gpa >= 2.5) return "Good academic standing. Continue your efforts!";
        if (gpa >= 2.0) return "Satisfactory performance. Consider seeking academic support if needed.";
        return "Your GPA indicates academic difficulty. Please meet with your academic advisor.";
    }

    // Placeholder methods - you would implement these with actual service calls
    private String getCourseCode(int sectionId) {
        // Implementation would call service to get course code
        return "CS" + (100 + sectionId);
    }

    private String getCourseTitle(int sectionId) {
        // Implementation would call service to get course title
        return "Computer Science " + (100 + sectionId);
    }

    private int getCourseCredits(int sectionId) {
        // Implementation would call service to get course credits
        return 3; // Most courses are 3 credits
    }
}