package edu.univ.erp.domain;

import java.time.LocalDateTime;

public class Grade {
    private int gradeId;
    private int enrollmentId;
    private String component;
    private double maxScore;
    private Double score;
    private double weightage;
    private Double finalGrade;
    private Integer enteredBy;
    private LocalDateTime enteredAt;

    public Grade(int gradeId, int enrollmentId, String component, double maxScore, double weightage) {
        this.gradeId = gradeId;
        this.enrollmentId = enrollmentId;
        this.component = component;
        this.maxScore = maxScore;
        this.weightage = weightage;
        this.enteredAt = LocalDateTime.now();
    }

    // Getters and Setters
    public int getGradeId() { return gradeId; }
    public int getEnrollmentId() { return enrollmentId; }
    public String getComponent() { return component; }
    public double getMaxScore() { return maxScore; }
    public Double getScore() { return score; }
    public double getWeightage() { return weightage; }
    public Double getFinalGrade() { return finalGrade; }
    public Integer getEnteredBy() { return enteredBy; }
    public LocalDateTime getEnteredAt() { return enteredAt; }

    public void setGradeId(int gradeId) { this.gradeId = gradeId; }
    public void setScore(Double score) { this.score = score; }
    public void setFinalGrade(Double finalGrade) { this.finalGrade = finalGrade; }
    public void setEnteredBy(Integer enteredBy) { this.enteredBy = enteredBy; }

    public Double getWeightedScore() {
        if (score == null) return null;
        return (score / maxScore) * weightage * 100;
    }

    @Override
    public String toString() {
        return String.format("Grade{id=%d, enrollmentId=%d, component='%s', score=%s/%s}",
                gradeId, enrollmentId, component, score, maxScore);
    }
}
