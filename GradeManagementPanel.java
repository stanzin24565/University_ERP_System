package edu.univ.erp.ui;

import edu.univ.erp.domain.Enrollment;
import edu.univ.erp.domain.Grade;
import edu.univ.erp.domain.Section;
import edu.univ.erp.domain.User;
import edu.univ.erp.service.InstructorService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.*;
import java.util.List;

/**
 * GradeManagementPanel - cleaned and aligned with InstructorService signatures.
 * Uses Option A weights (internal to InstructorService): Assignment1 15%, Assignment2 15%,
 * Midterm 25%, Final 30%, Project 15%.
 */
public class GradeManagementPanel extends JPanel {
    private final User currentUser;
    private final InstructorService instructorService;
    private Section currentSection;

    private JComboBox<Section> sectionComboBox;
    private JTable gradesTable;
    private DefaultTableModel tableModel;
    private JLabel statusLabel;
    private JButton loadGradesButton;
    private JButton saveGradesButton;
    private JButton calculateFinalButton;
    private JButton statsButton;
    private JButton exportGradesButton;

    // components used in this UI (keeps order stable)
    private static final List<String> COMPONENTS = List.of(
            "Assignment 1", "Assignment 2", "Midterm", "Final Exam", "Project"
    );

    public GradeManagementPanel(User user) {
        this.currentUser = user;
        this.instructorService = new InstructorService();
        initializeUI();
        loadInstructorSections();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(10,10));
        setBorder(BorderFactory.createEmptyBorder(8,8,8,8));

        JLabel title = new JLabel("Grade Management", JLabel.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 18));
        add(title, BorderLayout.NORTH);

        // top control
        JPanel ctl = new JPanel(new FlowLayout(FlowLayout.LEFT));
        ctl.add(new JLabel("Section:"));
        sectionComboBox = new JComboBox<>();
        sectionComboBox.setPreferredSize(new Dimension(400, 26));
        ctl.add(sectionComboBox);

        loadGradesButton = new JButton("Load Students");
        ctl.add(loadGradesButton);

        add(ctl, BorderLayout.PAGE_START);

        // table columns
        List<String> cols = new ArrayList<>();
        cols.add("Enrollment ID");
        cols.add("Student ID");
        cols.add("Student Name");
        cols.addAll(COMPONENTS);
        cols.add("Total");
        cols.add("Letter");

        tableModel = new DefaultTableModel(cols.toArray(), 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // editable only component columns
                int compStart = 3;
                int compEnd = compStart + COMPONENTS.size() - 1;
                return column >= compStart && column <= compEnd;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0 || columnIndex == 1) return Integer.class;
                if (columnIndex >= 3 && columnIndex <= 3 + COMPONENTS.size() - 1) return Double.class;
                if (columnIndex == 3 + COMPONENTS.size()) return Double.class; // total
                return String.class;
            }
        };

        gradesTable = new JTable(tableModel);
        
        // Custom cell editor for Double values with listener for updates
        DefaultCellEditor doubleCellEditor = new DefaultCellEditor(new JTextField()) {
            @Override
            public boolean stopCellEditing() {
                try {
                    String value = (String) getCellEditorValue();
                    if (value != null && !value.isEmpty()) {
                        Double.parseDouble(value);
                    }
                    return super.stopCellEditing();
                } catch (NumberFormatException e) {
                    return false;
                }
            }
        };
        
        // Set the editor for all Double columns and add listener
        gradesTable.setDefaultEditor(Double.class, doubleCellEditor);
        
        // Add a key listener to update totals and letters after editing
        gradesTable.addPropertyChangeListener("tableCellEditor", e -> {
            if (e.getOldValue() == null && e.getNewValue() != null) {
                // Editor started
                return;
            }
            if (e.getOldValue() != null && e.getNewValue() == null) {
                // Editor stopped - update the calculated columns
                int row = gradesTable.getSelectedRow();
                if (row >= 0) {
                    updateRowTotalsAndLetter(row);
                }
            }
        });
        
        JScrollPane sp = new JScrollPane(gradesTable);
        add(sp, BorderLayout.CENTER);

        // bottom controls
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
        saveGradesButton = new JButton("Save Grades");
        calculateFinalButton = new JButton("Compute Final Grades");
        statsButton = new JButton("Show Stats");
        exportGradesButton = new JButton("Export CSV");

        bottom.add(saveGradesButton);
        bottom.add(calculateFinalButton);
        bottom.add(statsButton);
        bottom.add(exportGradesButton);

        statusLabel = new JLabel("Select a section and Load Students.");
        bottom.add(statusLabel);

        add(bottom, BorderLayout.SOUTH);

        // listeners
        loadGradesButton.addActionListener(e -> loadStudentGradesForSelectedSection());
        saveGradesButton.addActionListener(e -> saveGrades());
        calculateFinalButton.addActionListener(e -> computeFinalsForSection());
        statsButton.addActionListener(e -> showStats());
        exportGradesButton.addActionListener(e -> exportCsv());

        // disable until loaded
        saveGradesButton.setEnabled(false);
        calculateFinalButton.setEnabled(false);
        statsButton.setEnabled(false);
        exportGradesButton.setEnabled(false);
    }

    private void loadInstructorSections() {
        sectionComboBox.removeAllItems();
        List<Section> secs = instructorService.getInstructorSections((int) currentUser.getUserId());
        for (Section s : secs) {
            if ("ACTIVE".equalsIgnoreCase(s.getStatus()))
                sectionComboBox.addItem(s);
        }
        if (sectionComboBox.getItemCount() == 0) {
            statusLabel.setText("No active sections assigned to you.");
        } else {
            statusLabel.setText("Choose a section and click Load Students.");
        }
    }

    private void loadStudentGradesForSelectedSection() {
        Section sel = (Section) sectionComboBox.getSelectedItem();
        if (sel == null) {
            JOptionPane.showMessageDialog(this, "Please select a section.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        // ensure instructor owns this section
        if (!instructorService.isInstructorOfSection((int) currentUser.getUserId(), sel.getSectionId())) {
            JOptionPane.showMessageDialog(this, "Not your section.", "Access Denied", JOptionPane.ERROR_MESSAGE);
            return;
        }
        currentSection = sel;
        tableModel.setRowCount(0);

        System.out.println("[GradeManagementPanel] Loading students for section " + sel.getSectionId());
        
        List<Enrollment> enrollments = instructorService.getSectionEnrollments(sel.getSectionId());
        System.out.println("[GradeManagementPanel] Found " + enrollments.size() + " enrollments");
        
        int loadedCount = 0;
        for (Enrollment en : enrollments) {
            // only active/ENROLLED rows
            if (!"ENROLLED".equalsIgnoreCase(en.getStatus()) && !"REGISTERED".equalsIgnoreCase(en.getStatus())) {
                System.out.println("[GradeManagementPanel] Skipping enrollment " + en.getEnrollmentId() + " with status " + en.getStatus());
                continue;
            }

            // fetch existing grades
            List<Grade> grades = instructorService.getGradesForEnrollment(en.getEnrollmentId());
            Map<String, Double> gradeMap = new HashMap<>();
            for (Grade g : grades) gradeMap.put(g.getComponent(), g.getScore());

            Object[] row = new Object[3 + COMPONENTS.size() + 2]; // +2 for Total, Letter (no Final)
            row[0] = (int) en.getEnrollmentId();
            row[1] = (int) en.getStudentId();
            row[2] = getStudentName(en.getStudentId());
            int idx = 3;
            for (String comp : COMPONENTS) {
                row[idx++] = gradeMap.getOrDefault(comp, null);
            }
            // compute total and letter
            Double total = computeTotalForRow(row);
            row[idx++] = total;  // Total column
            row[idx] = total == null ? "" : numericToLetter(total);  // Letter column

            tableModel.addRow(row);
            loadedCount++;
        }

        // enable actions
        saveGradesButton.setEnabled(true);
        calculateFinalButton.setEnabled(true);
        statsButton.setEnabled(true);
        exportGradesButton.setEnabled(true);

        statusLabel.setText("Loaded " + loadedCount + " students for section " + sel.getSectionId());
        System.out.println("[GradeManagementPanel] Successfully loaded " + loadedCount + " students");
    }

    // helper to compute total using fixed weights (match InstructorService weights)
    private Double computeTotalForRow(Object[] row) {
        double total = 0.0;
        double weightSum = 0.0;
        // weights mirrored from InstructorService
        Map<String, Double> weights = Map.of(
                "Assignment 1", 0.15,
                "Assignment 2", 0.15,
                "Midterm", 0.25,
                "Final Exam", 0.30,
                "Project", 0.15
        );

        // enrollment_id at index 0, student_id at 1, name at 2, components start at 3
        StringBuilder debug = new StringBuilder("[GradeManagementPanel.computeTotalForRow] ");
        for (int i = 0; i < COMPONENTS.size(); i++) {
            Object val = row[3 + i];
            Double sc = null;
            if (val instanceof Number) sc = ((Number) val).doubleValue();
            else if (val instanceof String) {
                try { sc = Double.parseDouble((String) val); } catch (Exception ignored) {}
            }
            if (sc != null && sc > 0) {
                String comp = COMPONENTS.get(i);
                double w = weights.getOrDefault(comp, 0.0);
                double weighted = sc * w;
                total += weighted;
                weightSum += w;
                debug.append(comp).append("=").append(sc).append("*").append(w).append("=").append(weighted).append(", ");
            }
        }
        if (weightSum == 0.0) {
            System.out.println(debug.toString() + " -> total=null (no grades)");
            return null;
        }
        double rounded = Math.round(total * 100.0) / 100.0;
        System.out.println(debug.toString() + " -> total=" + rounded + " (weightSum=" + weightSum + ")");
        return rounded;
    }

    private String numericToLetter(Double total) {
        if (total == null) return "";
        if (total >= 90) return "A";
        if (total >= 80) return "B";
        if (total >= 70) return "C";
        if (total >= 60) return "D";
        return "F";
    }

    private void updateRowTotalsAndLetter(int row) {
        if (row < 0 || row >= tableModel.getRowCount()) return;
        
        // Recalculate total for this row
        Object[] rowData = new Object[tableModel.getColumnCount()];
        for (int c = 0; c < tableModel.getColumnCount(); c++) {
            rowData[c] = tableModel.getValueAt(row, c);
        }
        
        Double total = computeTotalForRow(rowData);
        int totalColIdx = 3 + COMPONENTS.size();
        int finalColIdx = totalColIdx + 1;
        int letterColIdx = finalColIdx + 1;
        
        // Update Total column
        tableModel.setValueAt(total, row, totalColIdx);
        
        // Update Letter column
        String letter = total == null ? "" : numericToLetter(total);
        tableModel.setValueAt(letter, row, letterColIdx);
        
        System.out.println("[GradeManagementPanel] Updated row " + row + " - Total: " + total + ", Letter: " + letter);
    }

    private void saveGrades() {
        if (currentSection == null) {
            JOptionPane.showMessageDialog(this, "Please load a section first.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!instructorService.isInstructorOfSection((int) currentUser.getUserId(), currentSection.getSectionId())) {
            JOptionPane.showMessageDialog(this, "Not your section.", "Access Denied", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No students loaded. Load students first.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // build payload (use Integer keys to match InstructorService)
        Map<Integer, Map<String, Double>> payload = new HashMap<>();
        boolean hasGrades = false;
        
        for (int r = 0; r < tableModel.getRowCount(); r++) {
            Integer enrollmentId = (Integer) tableModel.getValueAt(r, 0);
            if (enrollmentId == null) continue;
            Map<String, Double> comps = new HashMap<>();
            for (int i = 0; i < COMPONENTS.size(); i++) {
                Object val = tableModel.getValueAt(r, 3 + i);
                Double sc = null;
                if (val instanceof Number) sc = ((Number) val).doubleValue();
                else if (val instanceof String) {
                    try { sc = Double.parseDouble((String) val); } catch (Exception ignored) {}
                }
                if (sc != null) {
                    // validate range
                    if (sc < 0 || sc > 100) {
                        JOptionPane.showMessageDialog(this, "Grades must be 0-100. Check row " + (r+1),
                                "Invalid Input", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    comps.put(COMPONENTS.get(i), sc);
                    hasGrades = true;
                }
            }
            if (!comps.isEmpty()) {
                payload.put(enrollmentId, comps);
            }
        }

        if (!hasGrades) {
            JOptionPane.showMessageDialog(this, "No grades entered. Please enter at least one grade.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        System.out.println("[GradeManagementPanel.saveGrades] Saving grades for " + payload.size() + " students in section " + currentSection.getSectionId());
        
        try {
            int saved = instructorService.saveGradesForSection(currentSection.getSectionId(), payload);
            String message;
            if (saved == payload.size()) {
                message = "Successfully saved grades for " + saved + " students.";
            } else {
                message = "Saved grades for " + saved + " out of " + payload.size() + " students.\n" + (payload.size() - saved) + " failed.";
            }
            statusLabel.setText(message);
            JOptionPane.showMessageDialog(this, message, "Saved", JOptionPane.INFORMATION_MESSAGE);
            // reload to ensure latest computed values appear
            loadStudentGradesForSelectedSection();
        } catch (Exception e) {
            System.err.println("[GradeManagementPanel.saveGrades] ERROR: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error saving grades: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void computeFinalsForSection() {
        if (currentSection == null) {
            JOptionPane.showMessageDialog(this, "Load a section first.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!instructorService.isInstructorOfSection((int) currentUser.getUserId(), currentSection.getSectionId())) {
            JOptionPane.showMessageDialog(this, "Not your section.", "Access Denied", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No students loaded. Load students first.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        System.out.println("[GradeManagementPanel.computeFinalsForSection] Starting computation for section " + currentSection.getSectionId());
        
        // InstructorService.computeFinalsForSection(sectionId) returns Map<Integer,Boolean>
        Map<Integer, Boolean> results = instructorService.computeFinalsForSection(currentSection.getSectionId());
        long success = results.values().stream().filter(b -> b).count();
        long failed = results.size() - success;
        
        System.out.println("[GradeManagementPanel.computeFinalsForSection] Results: " + success + " successful, " + failed + " failed");
        
        String message;
        if (failed > 0) {
            message = "Computed final grades for " + success + " enrollments.\n" + failed + " enrollments failed (may not have all components entered).";
        } else {
            message = "Computed final grades for " + success + " enrollments.";
        }
        
        statusLabel.setText("Computed final grades for " + success + " out of " + results.size() + " enrollments.");
        JOptionPane.showMessageDialog(this, message, "Done", JOptionPane.INFORMATION_MESSAGE);
        loadStudentGradesForSelectedSection();
    }

    private void showStats() {
        if (currentSection == null) {
            JOptionPane.showMessageDialog(this, "Load a section first.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        // InstructorService.computeStats(sectionId) returns Map<String,double[]>
        Map<String, double[]> stats = instructorService.computeStats(currentSection.getSectionId());
        StringBuilder sb = new StringBuilder();
        sb.append("Statistics for section ").append(currentSection.getSectionId()).append("\n\n");
        for (String comp : COMPONENTS) {
            double[] arr = stats.get(comp);
            sb.append(comp).append(": ");
            if (arr == null || Double.isNaN(arr[0])) {
                sb.append("No data\n");
            } else {
                sb.append(String.format("Avg=%.2f Min=%.2f Max=%.2f\n", arr[0], arr[1], arr[2]));
            }
        }
        double[] overall = stats.get("OVERALL");
        sb.append("\nOverall: ");
        if (overall == null || Double.isNaN(overall[0])) sb.append("No data\n");
        else sb.append(String.format("Avg=%.2f Min=%.2f Max=%.2f\n", overall[0], overall[1], overall[2]));

        JTextArea ta = new JTextArea(sb.toString());
        ta.setEditable(false);
        ta.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JOptionPane.showMessageDialog(this, new JScrollPane(ta), "Section Stats", JOptionPane.INFORMATION_MESSAGE);
    }

    private void exportCsv() {
        if (currentSection == null) {
            JOptionPane.showMessageDialog(this, "Load a section first.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No data to export.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save grades CSV");
        int rc = chooser.showSaveDialog(this);
        if (rc != JFileChooser.APPROVE_OPTION) return;
        try (PrintWriter pw = new PrintWriter(new FileWriter(chooser.getSelectedFile()))) {
            // header
            List<String> headers = new ArrayList<>();
            for (int c=0;c<tableModel.getColumnCount();c++) headers.add(tableModel.getColumnName(c));
            pw.println(String.join(",", headers));
            for (int r=0;r<tableModel.getRowCount();r++) {
                List<String> cells = new ArrayList<>();
                for (int c=0;c<tableModel.getColumnCount();c++) {
                    Object v = tableModel.getValueAt(r,c);
                    cells.add(v == null ? "" : v.toString());
                }
                pw.println(String.join(",", cells));
            }
            JOptionPane.showMessageDialog(this, "CSV exported successfully.", "Export", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error exporting CSV: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Helper - placeholder for student name fetch. Replace with a StudentService call if present.
    private String getStudentName(int studentId) {
        return "Student " + studentId;
    }

    // expose a method to programmatically select a section (used by InstructorSectionsPanel)
    public void openForSection(Section s) {

        boolean found = false;

        for (int i = 0; i < sectionComboBox.getItemCount(); i++) {
            Section item = sectionComboBox.getItemAt(i);
            if (item.getSectionId() == s.getSectionId()) {
                sectionComboBox.setSelectedIndex(i);
                found = true;
                break;
            }
        }

        if (!found) {
            sectionComboBox.addItem(s);
            sectionComboBox.setSelectedItem(s);
        }

        loadStudentGradesForSelectedSection();
    }
}
