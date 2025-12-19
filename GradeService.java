package edu.univ.erp.service;

import edu.univ.erp.domain.Grade;
import edu.univ.erp.domain.Enrollment;
import edu.univ.erp.data.GradeDAO;
import edu.univ.erp.data.EnrollmentDAO;
import java.util.List;

public class GradeService {
    private GradeDAO gradeDAO;
    private EnrollmentDAO enrollmentDAO;

    public GradeService() {
        this.gradeDAO = new GradeDAO();
        this.enrollmentDAO = new EnrollmentDAO();
    }

    public boolean enterGrade(int enrollmentId, String component, double score,
                              double maxScore, double weightage, int instructorId) {
        Grade grade = new Grade(0, enrollmentId, component, maxScore, weightage);
        grade.setScore(score);
        grade.setEnteredBy(instructorId);

        return gradeDAO.save(grade);
    }

    public boolean calculateFinalGrade(int enrollmentId) {
        List<Grade> grades = gradeDAO.findByEnrollmentId(enrollmentId);

        double totalWeightedScore = 0;
        double totalWeightage = 0;
        boolean allGraded = true;

        for (Grade grade : grades) {
            if (grade.getScore() == null) {
                allGraded = false;
                break;
            }
            totalWeightedScore += grade.getWeightedScore();
            totalWeightage += grade.getWeightage();
        }

        if (!allGraded || totalWeightage == 0) {
            return false;
        }

        double finalGrade = (totalWeightedScore / totalWeightage);

        // Update all grades with final grade
        for (Grade grade : grades) {
            grade.setFinalGrade(finalGrade);
            gradeDAO.save(grade);
        }

        return true;
    }

    public List<Grade> getGradesForEnrollment(int enrollmentId) {
        return gradeDAO.findByEnrollmentId(enrollmentId);
    }

    public List<Grade> getGradesForStudentSection(int studentId, int sectionId) {
        Enrollment enrollment = enrollmentDAO.findByStudentAndSection(studentId, sectionId);
        if (enrollment != null) {
            return gradeDAO.findByEnrollmentId(enrollment.getEnrollmentId());
        }
        return List.of();
    }
}