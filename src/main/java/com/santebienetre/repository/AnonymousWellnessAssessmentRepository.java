package com.santebienetre.repository;

import com.santebienetre.model.AnonymousWellnessAssessment;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository pour les évaluations de bien-être anonymes
 */
public interface AnonymousWellnessAssessmentRepository {
    
    /**
     * Trouve les évaluations par session
     */
    List<AnonymousWellnessAssessment> findBySessionId(String sessionId);
    
    /**
     * Trouve la dernière évaluation d'une session
     */
    Optional<AnonymousWellnessAssessment> findFirstBySessionIdOrderByAssessmentDateDesc(String sessionId);
    
    /**
     * Statistiques agrégées pour le dashboard
     */
    Double getAverageWellnessScoreSince(LocalDateTime startDate);
    
    Double getAverageStressLevelSince(LocalDateTime startDate);
    
    Double getAverageSleepQualitySince(LocalDateTime startDate);
    
    /**
     * Distribution des catégories
     */
    List<Object[]> getStressCategoryDistributionSince(LocalDateTime startDate);
    
    List<Object[]> getSleepCategoryDistributionSince(LocalDateTime startDate);
    
    /**
     * Tendances temporelles
     */
    List<Object[]> getDailyWellnessTrendsSince(LocalDateTime startDate);
    
    /**
     * Nettoyage des anciennes évaluations
     */
    void deleteOldAssessments(LocalDateTime cutoffDate);
    
    /**
     * Sauvegarde une évaluation
     */
    AnonymousWellnessAssessment save(AnonymousWellnessAssessment assessment);
}
