package edu.univ.erp.service;

import edu.univ.erp.domain.Enrollment;
import edu.univ.erp.data.EnrollmentDAO;
import java.util.List;

public class EnrollmentService {
    private EnrollmentDAO enrollmentDAO;

    public EnrollmentService() {
        this.enrollmentDAO = new EnrollmentDAO();
    }

    public List<Enrollment> getEnrollmentsBySection(int sectionId) {
        return enrollmentDAO.findBySectionId(sectionId);
    }

    public List<Enrollment> getActiveEnrollmentsBySection(int sectionId) {
        return enrollmentDAO.findActiveBySectionId(sectionId);
    }

    public int getEnrollmentCount(int sectionId) {
        return enrollmentDAO.countBySectionId(sectionId);
    }

    public boolean isStudentEnrolled(int studentId, int sectionId) {
        return enrollmentDAO.hasActiveEnrollment(studentId, sectionId);
    }
}