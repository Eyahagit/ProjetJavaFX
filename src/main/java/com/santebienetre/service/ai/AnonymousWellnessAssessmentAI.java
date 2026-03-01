package com.santebienetre.service.ai;

import com.santebienetre.model.AnonymousWellnessAssessment;
import com.santebienetre.repository.AnonymousWellnessAssessmentRepository;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Service IA d'évaluation de bien-être pour utilisateurs anonymes
 */
public class AnonymousWellnessAssessmentAI {
    
    private AnonymousWellnessAssessmentRepository assessmentRepository;
    
    private AnonymousSessionService sessionService;
    
    public AnonymousWellnessAssessmentAI() {
        // Initialisation sans Spring pour compatibilité avec l'architecture existante
        this.sessionService = new AnonymousSessionService();
    }
    
    /**
     * Évalue le bien-être basé sur les réponses au quiz
     */
    public AnonymousWellnessAssessment assessAnonymousWellness(String sessionId, Map<String, Integer> responses) {
        
        // Analyse des réponses
        double stressLevel = analyzeStressResponses(responses);
        double sleepQuality = analyzeSleepResponses(responses);
        double mentalState = analyzeMentalResponses(responses);
        
        // Calcul du score global
        double overallScore = calculateOverallScore(stressLevel, sleepQuality, mentalState);
        
        // Génération des recommandations
        List<String> recommendations = generateRecommendations(stressLevel, sleepQuality, mentalState);
        
        // Création de l'assessment
        AnonymousWellnessAssessment assessment = new AnonymousWellnessAssessment();
        assessment.setSessionId(sessionId);
        assessment.setOverallWellnessScore(overallScore);
        assessment.setStressLevel(stressLevel);
        assessment.setSleepQuality(sleepQuality);
        assessment.setMentalState(mentalState);
        assessment.setStressCategory(categorizeStress(stressLevel));
        assessment.setSleepCategory(categorizeSleep(sleepQuality));
        assessment.setMentalCategory(categorizeMental(mentalState));
        assessment.setRecommendations(recommendations);
        assessment.setAssessmentDate(LocalDateTime.now());
            
        // Pour l'instant, on ne sauvegarde pas en base
        // TODO: Sauvegarder en base de données quand Spring sera configuré
        
        // Stockage des données de comportement
        sessionService.storeBehaviorData(sessionId, "wellness_assessment", assessment);
        
        return assessment;
    }
    
    /**
     * Analyse des réponses sur le stress
     */
    private double analyzeStressResponses(Map<String, Integer> responses) {
        double stressScore = 0;
        int count = 0;
        
        // Questions sur le stress (échelle 1-5)
        String[] stressQuestions = {
            "stress_frequency", "stress_intensity", "work_stress", 
            "personal_stress", "anxiety_level"
        };
        
        for (String question : stressQuestions) {
            if (responses.containsKey(question)) {
                stressScore += responses.get(question);
                count++;
            }
        }
        
        return count > 0 ? (stressScore / count) * 20 : 0; // Normalisation sur 100
    }
    
    /**
     * Analyse des réponses sur le sommeil
     */
    private double analyzeSleepResponses(Map<String, Integer> responses) {
        double sleepScore = 0;
        int count = 0;
        
        String[] sleepQuestions = {
            "sleep_duration", "sleep_quality", "sleep_consistency",
            "fall_asleep_time", "night_awakenings"
        };
        
        for (String question : sleepQuestions) {
            if (responses.containsKey(question)) {
                // Inverser pour certaines questions (plus c'est élevé, meilleur c'est)
                int value = responses.get(question);
                if (question.equals("sleep_duration") || question.equals("sleep_quality")) {
                    sleepScore += value;
                } else {
                    // Pour les problèmes de sommeil, inverser le score
                    sleepScore += (6 - value);
                }
                count++;
            }
        }
        
        return count > 0 ? (sleepScore / count) * 20 : 0;
    }
    
    /**
     * Analyse des réponses sur la santé mentale
     */
    private double analyzeMentalResponses(Map<String, Integer> responses) {
        double mentalScore = 0;
        int count = 0;
        
        String[] mentalQuestions = {
            "mood_level", "energy_level", "concentration", 
            "social_interaction", "life_satisfaction"
        };
        
        for (String question : mentalQuestions) {
            if (responses.containsKey(question)) {
                mentalScore += responses.get(question);
                count++;
            }
        }
        
        return count > 0 ? (mentalScore / count) * 20 : 0;
    }
    
    /**
     * Calcule le score global de bien-être
     */
    private double calculateOverallScore(double stress, double sleep, double mental) {
        // Pondération : 30% stress, 35% sommeil, 35% mental
        return (stress * 0.3) + (sleep * 0.35) + (mental * 0.35);
    }
    
    /**
     * Catégorise le niveau de stress
     */
    private String categorizeStress(double stressLevel) {
        if (stressLevel >= 80) return "TRÈS ÉLEVÉ";
        if (stressLevel >= 60) return "ÉLEVÉ";
        if (stressLevel >= 40) return "MODÉRÉ";
        if (stressLevel >= 20) return "FAIBLE";
        return "TRÈS FAIBLE";
    }
    
    /**
     * Catégorise la qualité du sommeil
     */
    private String categorizeSleep(double sleepQuality) {
        if (sleepQuality >= 80) return "EXCELLENTE";
        if (sleepQuality >= 60) return "BONNE";
        if (sleepQuality >= 40) return "MOYENNE";
        if (sleepQuality >= 20) return "MAUVAISE";
        return "TRÈS MAUVAISE";
    }
    
    /**
     * Catégorise l'état mental
     */
    private String categorizeMental(double mentalState) {
        if (mentalState >= 80) return "EXCELLENT";
        if (mentalState >= 60) return "BON";
        if (mentalState >= 40) return "MOYEN";
        if (mentalState >= 20) return "PRÉOCCUPANT";
        return "CRITIQUE";
    }
    
    /**
     * Génère des recommandations personnalisées
     */
    private List<String> generateRecommendations(double stress, double sleep, double mental) {
        List<String> recommendations = new ArrayList<>();
        
        // Recommandations stress
        if (stress > 60) {
            recommendations.add("💡 Essayez des techniques de relaxation comme la méditation ou la respiration profonde");
            recommendations.add("🧘‍♀️ Considérez des séances de yoga ou de stretching régulier");
        }
        
        // Recommandations sommeil
        if (sleep < 60) {
            recommendations.add("😴 Établissez une routine de coucher régionale");
            recommendations.add("📱 Évitez les écrans 1h avant de dormir");
            recommendations.add("☕ Limitez la caféine après 14h");
        }
        
        // Recommandations mental
        if (mental < 60) {
            recommendations.add("🏃‍♀️ L'exercice physique peut améliorer votre humeur");
            recommendations.add("👥 Passez du temps avec vos proches");
            recommendations.add("📝 Tenez un journal pour exprimer vos émotions");
        }
        
        // Recommandations générales
        if (stress < 40 && sleep > 60 && mental > 60) {
            recommendations.add("🌟 Continuez vos bonnes habitudes !");
            recommendations.add("🎯 Fixez-vous de nouveaux objectifs de bien-être");
        }
        
        return recommendations;
    }
}
