package edu.univ.erp.service;

import edu.univ.erp.data.EnrollmentDAO;
import edu.univ.erp.data.GradeDAO;
import edu.univ.erp.data.SectionDAO;
import edu.univ.erp.domain.Enrollment;
import edu.univ.erp.domain.Grade;
import edu.univ.erp.domain.Section;

import java.util.*;
public class InstructorService {

    private final SectionDAO sectionDAO;
    private final EnrollmentDAO enrollmentDAO;
    private final GradeDAO gradeDAO;

    // Grading rule (Option A)
    private final Map<String, Double> WEIGHTS = Map.of(
            "Assignment 1", 0.15,
            "Assignment 2", 0.15,
            "Midterm", 0.25,
            "Final Exam", 0.30,
            "Project", 0.15
    );

    public InstructorService() {
        this.sectionDAO = new SectionDAO();
        this.enrollmentDAO = new EnrollmentDAO();
        this.gradeDAO = new GradeDAO();
    }

    // -----------------------------------------------------
    // SECTION ACCESS
    // -----------------------------------------------------

    public List<Section> getInstructorSections(int instructorId) {
        return sectionDAO.findByInstructor(instructorId);
    }

    public Section getSectionById(int sectionId) {
        return sectionDAO.findById(sectionId);
    }

    /** Permission check */
    public boolean isInstructorOfSection(int instructorId, int sectionId) {
        Section s = getSectionById(sectionId);
        return (s != null && s.getInstructorId() == instructorId);
    }

    // -----------------------------------------------------
    // ENROLLMENTS
    // -----------------------------------------------------

    public List<Enrollment> getSectionEnrollments(int sectionId) {
        return enrollmentDAO.findActiveBySectionId(sectionId);
    }

    // -----------------------------------------------------
    // GRADES
    // -----------------------------------------------------

    /** Return all grade components for one Enrollment */
    public List<Grade> getGradesForEnrollment(int enrollmentId) {
        return gradeDAO.findByEnrollmentId(enrollmentId);
    }

    /** Save an individual component score */
    public boolean saveComponentScore(int enrollmentId, String component, Double score) {
        if (score == null) return true;   // Nothing to save

        try {
            System.out.println("[InstructorService] Saving grade: enrollmentId=" + enrollmentId + ", component=" + component + ", score=" + score);
            
            // Remove existing component
            gradeDAO.deleteComponent(enrollmentId, component);

            // Insert new one
            Grade g = new Grade(
                    0,
                    enrollmentId,
                    component,
                    100,            // max score
                    WEIGHTS.getOrDefault(component, 0.0)
            );

            g.setScore(score);
            boolean saved = gradeDAO.save(g);
            System.out.println("[InstructorService] Grade saved: " + saved);
            return saved;

        } catch (Exception e) {
            System.err.println("[InstructorService] ERROR saving grade for enrollmentId=" + enrollmentId + ", component=" + component);
            e.printStackTrace();
            return false;
        }
    }

    /** Save ALL components for all students in a section */
    public int saveGradesForSection(int sectionId, Map<Integer, Map<String, Double>> gradeMap) {
        System.out.println("[InstructorService] saveGradesForSection called with sectionId=" + sectionId + ", students=" + gradeMap.size());
        
        int saved = 0;

        for (Map.Entry<Integer, Map<String, Double>> entry : gradeMap.entrySet()) {
            int enrollmentId = entry.getKey();
            Map<String, Double> comps = entry.getValue();

            boolean ok = true;

            for (Map.Entry<String, Double> c : comps.entrySet()) {
                if (!saveComponentScore(enrollmentId, c.getKey(), c.getValue())) {
                    ok = false;
                    break;
                }
            }

            if (ok) saved++;
        }
        
        System.out.println("[InstructorService] Completed saving grades for " + saved + " students");
        return saved;
    }

    // -----------------------------------------------------
    // FINAL GRADE COMPUTATION
    // -----------------------------------------------------

    /**
     * Compute final grade using your weights (15/15/25/30/15)
     */
    public boolean computeFinalForEnrollment(int enrollmentId) {

        List<Grade> grades = getGradesForEnrollment(enrollmentId);
        System.out.println("[InstructorService.computeFinalForEnrollment] enrollmentId=" + enrollmentId + ", found " + grades.size() + " components");

        double total = 0;
        double wsum = 0;

        for (Grade g : grades) {
            Double w = WEIGHTS.get(g.getComponent());
            if (w != null && g.getScore() != null) {
                System.out.println("[InstructorService.computeFinalForEnrollment] component=" + g.getComponent() + ", score=" + g.getScore() + ", weight=" + w);
                total += g.getScore() * w;
                wsum += w;
            }
        }

        if (wsum == 0) {
            System.err.println("[InstructorService.computeFinalForEnrollment] ERROR: No valid weighted components found for enrollmentId=" + enrollmentId);
            return false;
        }

        double finalScore = Math.round(total * 100.0) / 100.0;
        System.out.println("[InstructorService.computeFinalForEnrollment] Calculated final score=" + finalScore + " (weightSum=" + wsum + ")");

        // Remove old final
        gradeDAO.deleteComponent(enrollmentId, "Final");

        // Insert final as regular Grade
        Grade finalG = new Grade(0, enrollmentId, "Final", 100, 1.0);
        finalG.setScore(finalScore);

        boolean saved = gradeDAO.save(finalG);
        System.out.println("[InstructorService.computeFinalForEnrollment] Final grade saved: " + saved);
        return saved;
    }

    /** Compute final grades for all enrollments in a section */
    public Map<Integer, Boolean> computeFinalsForSection(int sectionId) {
        System.out.println("[InstructorService.computeFinalsForSection] Computing finals for sectionId=" + sectionId);
        
        Map<Integer, Boolean> results = new HashMap<>();
        List<Enrollment> list = getSectionEnrollments(sectionId);
        System.out.println("[InstructorService.computeFinalsForSection] Found " + list.size() + " enrollments");

        int successCount = 0;
        for (Enrollment e : list) {
            boolean ok = computeFinalForEnrollment((int) e.getEnrollmentId());
            results.put((int) e.getEnrollmentId(), ok);
            if (ok) successCount++;
        }
        
        System.out.println("[InstructorService.computeFinalsForSection] Completed: " + successCount + " out of " + list.size() + " finals computed");
        return results;
    }

    // -----------------------------------------------------
    // SIMPLE STATISTICS
    // -----------------------------------------------------

    /**
     * Compute avg/min/max for each component + overall
     */
    public Map<String, double[]> computeStats(int sectionId) {

        List<Enrollment> enrolls = getSectionEnrollments(sectionId);
        Map<String, List<Double>> compMap = new LinkedHashMap<>();

        for (String c : WEIGHTS.keySet()) compMap.put(c, new ArrayList<>());
        List<Double> totals = new ArrayList<>();

        for (Enrollment e : enrolls) {

            List<Grade> grades = getGradesForEnrollment(e.getEnrollmentId());
            Map<String, Double> scoreMap = new HashMap<>();

            for (Grade g : grades) scoreMap.put(g.getComponent(), g.getScore());

            double overall = 0;
            double wsum = 0;

            for (String c : WEIGHTS.keySet()) {
                Double sc = scoreMap.get(c);
                if (sc != null) {
                    compMap.get(c).add(sc);
                    double w = WEIGHTS.get(c);
                    overall += sc * w;
                    wsum += w;
                }
            }

            if (wsum > 0) totals.add(overall);
        }

        Map<String, double[]> stats = new LinkedHashMap<>();

        for (String c : WEIGHTS.keySet()) {
            List<Double> arr = compMap.get(c);
            if (arr.isEmpty()) {
                stats.put(c, new double[]{Double.NaN, Double.NaN, Double.NaN});
                continue;
            }
            stats.put(c, computeArrayStats(arr));
        }

        stats.put("OVERALL", totals.isEmpty()
                ? new double[]{Double.NaN, Double.NaN, Double.NaN}
                : computeArrayStats(totals));

        return stats;
    }

    /** Helper: compute avg/min/max for list */
    private double[] computeArrayStats(List<Double> vals) {
        double sum = 0, min = Double.MAX_VALUE, max = Double.MIN_VALUE;

        for (double v : vals) {
            sum += v;
            min = Math.min(min, v);
            max = Math.max(max, v);
        }
        return new double[]{
                sum / vals.size(),
                min,
                max
        };
    }
}
