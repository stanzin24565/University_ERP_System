package edu.univ.erp.ui;

import edu.univ.erp.domain.Enrollment;
import edu.univ.erp.domain.Section;
import edu.univ.erp.domain.User;
import edu.univ.erp.service.InstructorService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * InstructorSectionsPanel - shows instructor's sections and provides quick links to Manage Grades.
 * When Manage Grades clicked, opens GradeManagementPanel and pre-selects the section.
 */
public class InstructorSectionsPanel extends JPanel {
    private final User currentUser;
    private final InstructorService instructorService;
    private JTable sectionsTable;
    private DefaultTableModel tableModel;
    private JLabel statusLabel;
    private GradeManagementPanel gradePanel; // we will create when needed

    public InstructorSectionsPanel(User user) {
        this.currentUser = user;
        this.instructorService = new InstructorService();
        initializeUI();
        loadInstructorSections();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(8,8));
        setBorder(BorderFactory.createEmptyBorder(8,8,8,8));

        JLabel title = new JLabel("My Teaching Sections", JLabel.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 16));
        add(title, BorderLayout.NORTH);

        String[] cols = {"Section ID","Course","Schedule","Room","Capacity","Enrolled","Available","Semester","Year","Status"};
        tableModel = new DefaultTableModel(cols,0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex==0 || columnIndex==4 || columnIndex==5 || columnIndex==6 || columnIndex==8) return Integer.class;
                return String.class;
            }
        };

        sectionsTable = new JTable(tableModel);
        sectionsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane sp = new JScrollPane(sectionsTable);
        add(sp, BorderLayout.CENTER);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton refresh = new JButton("Refresh");
        JButton viewEnroll = new JButton("View Enrollments");
        JButton manageGrades = new JButton("Manage Grades");
        JButton details = new JButton("Section Details");

        btns.add(refresh);
        btns.add(viewEnroll);
        btns.add(manageGrades);
        btns.add(details);

        add(btns, BorderLayout.SOUTH);

        statusLabel = new JLabel("Loading...");
        add(statusLabel, BorderLayout.PAGE_END);

        // listeners
        refresh.addActionListener(e -> loadInstructorSections());
        viewEnroll.addActionListener(e -> viewEnrollments());
        manageGrades.addActionListener(e -> openManageGrades());
        details.addActionListener(e -> viewSectionDetails());

        sectionsTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) viewEnrollments();
            }
        });
    }

    private void loadInstructorSections() {
        tableModel.setRowCount(0);
        List<Section> secs = instructorService.getInstructorSections((int) currentUser.getUserId());
        for (Section s : secs) {
            int available = s.getCapacity() - s.getEnrolledCount();
            tableModel.addRow(new Object[]{
                    s.getSectionId(),
                    getCourseName(s.getCourseId()),
                    s.getDayTime(),
                    s.getRoom(),
                    s.getCapacity(),
                    s.getEnrolledCount(),
                    available,
                    s.getSemester(),
                    s.getYear(),
                    s.getStatus()
            });
        }
        statusLabel.setText("Found " + secs.size() + " section(s).");
    }

    private void viewEnrollments() {
        int r = sectionsTable.getSelectedRow();
        if (r == -1) { JOptionPane.showMessageDialog(this, "Select a section first.", "Warning", JOptionPane.WARNING_MESSAGE); return; }
        int sectionId = (Integer) tableModel.getValueAt(r,0);
        String course = (String) tableModel.getValueAt(r,1);
        List<Enrollment> enrolls = instructorService.getSectionEnrollments(sectionId);
        EnrollmentDialog dlg = new EnrollmentDialog((JFrame) SwingUtilities.getWindowAncestor(this),
                "Enrollments - " + course, enrolls);
        dlg.setVisible(true);
    }

    private void openManageGrades() {
        int r = sectionsTable.getSelectedRow();
        if (r == -1) { JOptionPane.showMessageDialog(this, "Select a section first.", "Warning", JOptionPane.WARNING_MESSAGE); return; }
        int sectionId = (Integer) tableModel.getValueAt(r,0);
        Section s = instructorService.getSectionById(sectionId);
        if (s == null) { JOptionPane.showMessageDialog(this, "Section not found.", "Error", JOptionPane.ERROR_MESSAGE); return; }
        if (!instructorService.isInstructorOfSection((int) currentUser.getUserId(), sectionId)) {
            JOptionPane.showMessageDialog(this, "Not your section.", "Access Denied", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // find or create GradeManagementPanel in a dialog
        JFrame parent = (JFrame) SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(parent, "Manage Grades - Section " + sectionId, true);
        GradeManagementPanel gPanel = new GradeManagementPanel(currentUser);
        gPanel.openForSection(s);
        dialog.getContentPane().add(gPanel);
        dialog.setSize(1000, 700);
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }

    private void viewSectionDetails() {
        int r = sectionsTable.getSelectedRow();
        if (r == -1) { JOptionPane.showMessageDialog(this, "Select a section first.", "Warning", JOptionPane.WARNING_MESSAGE); return; }
        int sectionId = (Integer) tableModel.getValueAt(r,0);
        Section s = instructorService.getSectionById(sectionId);
        if (s == null) { JOptionPane.showMessageDialog(this, "Section not found.", "Error", JOptionPane.ERROR_MESSAGE); return; }
        StringBuilder sb = new StringBuilder();
        sb.append("Section ID: ").append(s.getSectionId()).append("\n");
        sb.append("Course: ").append(getCourseName(s.getCourseId())).append("\n");
        sb.append("Schedule: ").append(s.getDayTime()).append("\n");
        sb.append("Room: ").append(s.getRoom()).append("\n");
        sb.append("Capacity: ").append(s.getCapacity()).append("\n");
        sb.append("Enrolled: ").append(s.getEnrolledCount()).append("\n");
        sb.append("Semester: ").append(s.getSemester()).append("\n");
        sb.append("Year: ").append(s.getYear()).append("\n");
        sb.append("Status: ").append(s.getStatus()).append("\n");
        JOptionPane.showMessageDialog(this, new JScrollPane(new JTextArea(sb.toString())), "Section Details", JOptionPane.INFORMATION_MESSAGE);
    }

    // Placeholder - replace with CourseService lookup if available
    private String getCourseName(int courseId) {
        return "Course " + courseId;
    }

    // Enrollment dialog inner class (same as before; reused)
    private static class EnrollmentDialog extends JDialog {
        public EnrollmentDialog(JFrame parent, String title, List<Enrollment> enrollments) {
            super(parent, title, true);
            setSize(700, 400);
            setLocationRelativeTo(parent);
            setLayout(new BorderLayout());
            String[] cols = {"Enrollment ID","Student ID","Student Name","Status","Enrolled At"};
            DefaultTableModel tm = new DefaultTableModel(cols,0) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
            };
            JTable t = new JTable(tm);
            for (Enrollment en : enrollments) {
                tm.addRow(new Object[]{en.getEnrollmentId(), en.getStudentId(), "Student "+en.getStudentId(), en.getStatus(), en.getEnrolledAt()});
            }
            add(new JScrollPane(t), BorderLayout.CENTER);
            JButton close = new JButton("Close");
            close.addActionListener(e -> dispose());
            JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            p.add(close);
            add(p, BorderLayout.SOUTH);
        }
    }
}
