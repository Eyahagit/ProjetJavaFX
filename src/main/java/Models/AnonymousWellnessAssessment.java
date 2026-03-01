package Models;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Entité pour les évaluations de bien-être anonymes (version simplifiée sans JPA)
 */
public class AnonymousWellnessAssessment {
    
    private Long id;
    private String sessionId;
    private Double overallWellnessScore;
    private Double stressLevel;
    private Double sleepQuality;
    private Double mentalState;
    private String stressCategory;
    private String sleepCategory;
    private String mentalCategory;
    private List<String> recommendations;
    private LocalDateTime assessmentDate;
    
    // Constructeurs
    public AnonymousWellnessAssessment() {}
    
    public AnonymousWellnessAssessment(String sessionId, Double overallWellnessScore, LocalDateTime assessmentDate) {
        this.sessionId = sessionId;
        this.overallWellnessScore = overallWellnessScore;
        this.assessmentDate = assessmentDate;
    }
    
    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    
    public Double getOverallWellnessScore() { return overallWellnessScore; }
    public void setOverallWellnessScore(Double overallWellnessScore) { this.overallWellnessScore = overallWellnessScore; }
    
    public Double getStressLevel() { return stressLevel; }
    public void setStressLevel(Double stressLevel) { this.stressLevel = stressLevel; }
    
    public Double getSleepQuality() { return sleepQuality; }
    public void setSleepQuality(Double sleepQuality) { this.sleepQuality = sleepQuality; }
    
    public Double getMentalState() { return mentalState; }
    public void setMentalState(Double mentalState) { this.mentalState = mentalState; }
    
    public String getStressCategory() { return stressCategory; }
    public void setStressCategory(String stressCategory) { this.stressCategory = stressCategory; }
    
    public String getSleepCategory() { return sleepCategory; }
    public void setSleepCategory(String sleepCategory) { this.sleepCategory = sleepCategory; }
    
    public String getMentalCategory() { return mentalCategory; }
    public void setMentalCategory(String mentalCategory) { this.mentalCategory = mentalCategory; }
    
    public List<String> getRecommendations() { return recommendations; }
    public void setRecommendations(List<String> recommendations) { this.recommendations = recommendations; }
    
    public LocalDateTime getAssessmentDate() { return assessmentDate; }
    public void setAssessmentDate(LocalDateTime assessmentDate) { this.assessmentDate = assessmentDate; }
    
    @Override
    public String toString() {
        return "AnonymousWellnessAssessment{" +
                "sessionId='" + sessionId + '\'' +
                ", overallWellnessScore=" + overallWellnessScore +
                ", stressLevel=" + stressLevel +
                ", sleepQuality=" + sleepQuality +
                ", mentalState=" + mentalState +
                ", assessmentDate=" + assessmentDate +
                '}';
    }
}
