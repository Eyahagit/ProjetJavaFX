package Services;

import Models.SanteBienEtre;
import Models.SleepTracking;

import java.util.*;

/**
 * Service IA de Prédiction de Santé Préventive
 * Analyse les tendances et prédit les risques de santé
 */
public class PreventiveHealthAI {
    
    // Base de données des facteurs de risque
    private final Map<String, Double> riskFactors = new HashMap<>();
    private final List<String> warningSymptoms = Arrays.asList(
        "fatigue chronique", "irritabilité", "troubles du sommeil",
        "perte d'appétit", "isolement social", "anxiété persistante"
    );
    
    public PreventiveHealthAI() {
        initializeRiskFactors();
    }
    
    /**
     * Initialise les facteurs de risque
     */
    private void initializeRiskFactors() {
        // Facteurs de stress
        riskFactors.put("stress_élevé", 0.8);
        riskFactors.put("stress_modéré", 0.5);
        riskFactors.put("stress_faible", 0.2);
        
        // Facteurs de sommeil
        riskFactors.put("sommeil_mauvais", 0.7);
        riskFactors.put("sommeil_moyen", 0.4);
        riskFactors.put("sommeil_bon", 0.1);
        
        // Facteurs de santé mentale
        riskFactors.put("mental_préoccupant", 0.9);
        riskFactors.put("mental_moyen", 0.5);
        riskFactors.put("mental_bon", 0.2);
        
        // Facteurs de style de vie
        riskFactors.put("sédentarité", 0.6);
        riskFactors.put("alcool_excès", 0.7);
        riskFactors.put("tabac", 0.8);
        riskFactors.put("alimentation_déséquilibrée", 0.5);
    }
    
    /**
     * Prédit les risques de santé pour les 30 prochains jours
     */
    public HealthRiskPrediction predictHealthRisks(Map<String, Object> userData) {
        
        // Calculer le score de risque global
        double overallRisk = calculateOverallRisk(userData);
        
        // Identifier les risques spécifiques
        List<String> specificRisks = identifySpecificRisks(userData, overallRisk);
        
        // Générer les recommandations préventives
        List<String> recommendations = generatePreventiveRecommendations(userData, specificRisks);
        
        // Calculer le niveau de confiance
        double confidence = calculatePredictionConfidence(userData);
        
        return HealthRiskPrediction.builder()
            .overallRiskScore(overallRisk)
            .riskLevel(categorizeRiskLevel(overallRisk))
            .specificRisks(specificRisks)
            .recommendations(recommendations)
            .confidenceLevel(confidence)
            .predictionDate(new Date())
            .timeframe("30 jours")
            .build();
    }
    
    /**
     * Calcule le score de risque global
     */
    private double calculateOverallRisk(Map<String, Object> userData) {
        double totalRisk = 0.0;
        int factorCount = 0;
        
        // Analyser les scores de bien-être si disponibles
        if (userData.containsKey("stress_level")) {
            double stressLevel = (Double) userData.get("stress_level");
            totalRisk += stressLevel / 100.0 * 0.3; // Pondération 30%
            factorCount++;
        }
        
        if (userData.containsKey("sleep_quality")) {
            double sleepQuality = (Double) userData.get("sleep_quality");
            totalRisk += (100 - sleepQuality) / 100.0 * 0.25; // Pondération 25% (inversé)
            factorCount++;
        }
        
        if (userData.containsKey("mental_state")) {
            double mentalState = (Double) userData.get("mental_state");
            totalRisk += (100 - mentalState) / 100.0 * 0.35; // Pondération 35% (inversé)
            factorCount++;
        }
        
        // Ajouter les facteurs de style de vie
        if (userData.containsKey("lifestyle_factors")) {
            @SuppressWarnings("unchecked")
            List<String> lifestyleFactors = (List<String>) userData.get("lifestyle_factors");
            for (String factor : lifestyleFactors) {
                if (riskFactors.containsKey(factor)) {
                    totalRisk += riskFactors.get(factor) * 0.1; // Pondération 10% par facteur
                    factorCount++;
                }
            }
        }
        
        return factorCount > 0 ? Math.min(totalRisk, 1.0) : 0.0;
    }
    
    /**
     * Identifie les risques spécifiques
     */
    private List<String> identifySpecificRisks(Map<String, Object> userData, double overallRisk) {
        List<String> risks = new ArrayList<>();
        
        if (overallRisk > 0.7) {
            risks.add("🚨 Risque élevé de burnout");
            risks.add("⚠️ Risque de dépression");
        }
        
        if (userData.containsKey("stress_level") && (Double) userData.get("stress_level") > 70) {
            risks.add("😰 Stress chronique");
            risks.add("💗 Risque cardiovasculaire");
        }
        
        if (userData.containsKey("sleep_quality") && (Double) userData.get("sleep_quality") < 50) {
            risks.add("😴 Troubles du sommeil chroniques");
            risks.add("🧠 Risque cognitif");
        }
        
        if (userData.containsKey("mental_state") && (Double) userData.get("mental_state") < 40) {
            risks.add("🧠 Dépression potentielle");
            risks.add("👥 Isolement social");
        }
        
        // Ajouter les symptômes d'avertissement
        if (userData.containsKey("symptoms")) {
            @SuppressWarnings("unchecked")
            List<String> symptoms = (List<String>) userData.get("symptoms");
            for (String symptom : symptoms) {
                if (warningSymptoms.contains(symptom.toLowerCase())) {
                    risks.add("⚠️ Symptôme d'avertissement: " + symptom);
                }
            }
        }
        
        return risks;
    }
    
    /**
     * Génère des recommandations préventives
     */
    private List<String> generatePreventiveRecommendations(Map<String, Object> userData, List<String> specificRisks) {
        List<String> recommendations = new ArrayList<>();
        
        // Recommandations basées sur les risques identifiés
        for (String risk : specificRisks) {
            if (risk.contains("burnout") || risk.contains("stress")) {
                recommendations.add("🧘‍♀️ Programmez 10 minutes de méditation quotidienne");
                recommendations.add("📅 Établissez des limites claires travail-vie personnelle");
                recommendations.add("🏃‍♀️ Faites 30 minutes d'exercice modéré par jour");
            }
            
            if (risk.contains("sommeil")) {
                recommendations.add("😴 Maintenez une routine de coucher stricte");
                recommendations.add("📱 Éliminez les écrans 1h avant de dormir");
                recommendations.add("🌿 Essayez des tisanes relaxantes (camomille, verveine)");
            }
            
            if (risk.contains("dépression") || risk.contains("mental")) {
                recommendations.add("👥 Contactez un ami ou un proche quotidiennement");
                recommendations.add("🌞 Exposez-vous à la lumière naturelle 30 minutes/jour");
                recommendations.add("📝 Tenez un journal de gratitude");
                recommendations.add("🏥 Consultez un professionnel de santé");
            }
        }
        
        // Recommandations générales de prévention
        recommendations.add("🥗 Adoptez une alimentation riche en oméga-3 et vitamines");
        recommendations.add("💧 Buvez 1.5L d'eau par jour");
        recommendations.add("📊 Suivez régulièrement vos indicateurs de bien-être");
        recommendations.add("🎯 Fixez-vous des objectifs réalistes et atteignables");
        
        return recommendations;
    }
    
    /**
     * Calcule le niveau de confiance de la prédiction
     */
    private double calculatePredictionConfidence(Map<String, Object> userData) {
        int dataPoints = 0;
        
        if (userData.containsKey("stress_level")) dataPoints++;
        if (userData.containsKey("sleep_quality")) dataPoints++;
        if (userData.containsKey("mental_state")) dataPoints++;
        if (userData.containsKey("lifestyle_factors")) dataPoints++;
        if (userData.containsKey("symptoms")) dataPoints++;
        
        // Plus il y a de données, plus la confiance est élevée
        return Math.min(dataPoints / 5.0, 1.0);
    }
    
    /**
     * Catégorise le niveau de risque
     */
    private String categorizeRiskLevel(double riskScore) {
        if (riskScore >= 0.8) return "TRÈS ÉLEVÉ";
        if (riskScore >= 0.6) return "ÉLEVÉ";
        if (riskScore >= 0.4) return "MODÉRÉ";
        if (riskScore >= 0.2) return "FAIBLE";
        return "TRÈS FAIBLE";
    }
    
    /**
     * Génère une alerte préventive
     */
    public PreventiveAlert generatePreventiveAlert(HealthRiskPrediction prediction) {
        if (prediction.getOverallRiskScore() > 0.7) {
            return PreventiveAlert.builder()
                .alertType("URGENT")
                .title("🚨 Alertes Santé Préventive")
                .message("Des risques importants ont été détectés. Une action immédiate est recommandée.")
                .actions(Arrays.asList(
                    "📞 Contacter un professionnel",
                    "📋 Planifier une consultation",
                    "📊 Analyser en détail"
                ))
                .severity("HIGH")
                .build();
        } else if (prediction.getOverallRiskScore() > 0.4) {
            return PreventiveAlert.builder()
                .alertType("WARNING")
                .title("⚠️ Recommandations de Bien-être")
                .message("Des améliorations sont possibles pour votre bien-être.")
                .actions(Arrays.asList(
                    "📖 Consulter les recommandations",
                    "🎯 Suivre un programme préventif",
                    "📊 Surveiller vos indicateurs"
                ))
                .severity("MEDIUM")
                .build();
        }
        
        return null;
    }
    
    /**
     * Classes internes pour les réponses
     */
    public static class HealthRiskPrediction {
        private double overallRiskScore;
        private String riskLevel;
        private List<String> specificRisks;
        private List<String> recommendations;
        private double confidenceLevel;
        private Date predictionDate;
        private String timeframe;
        
        public static Builder builder() { return new Builder(); }
        
        public static class Builder {
            private HealthRiskPrediction prediction = new HealthRiskPrediction();
            
            public Builder overallRiskScore(double score) { prediction.overallRiskScore = score; return this; }
            public Builder riskLevel(String level) { prediction.riskLevel = level; return this; }
            public Builder specificRisks(List<String> risks) { prediction.specificRisks = risks; return this; }
            public Builder recommendations(List<String> recs) { prediction.recommendations = recs; return this; }
            public Builder confidenceLevel(double confidence) { prediction.confidenceLevel = confidence; return this; }
            public Builder predictionDate(Date date) { prediction.predictionDate = date; return this; }
            public Builder timeframe(String timeframe) { prediction.timeframe = timeframe; return this; }
            
            public HealthRiskPrediction build() { return prediction; }
        }
        
        // Getters
        public double getOverallRiskScore() { return overallRiskScore; }
        public String getRiskLevel() { return riskLevel; }
        public List<String> getSpecificRisks() { return specificRisks; }
        public List<String> getRecommendations() { return recommendations; }
        public double getConfidenceLevel() { return confidenceLevel; }
        public Date getPredictionDate() { return predictionDate; }
        public String getTimeframe() { return timeframe; }
    }
    
    public static class PreventiveAlert {
        private String alertType;
        private String title;
        private String message;
        private List<String> actions;
        private String severity;
        
        public static Builder builder() { return new Builder(); }
        
        public static class Builder {
            private PreventiveAlert alert = new PreventiveAlert();
            
            public Builder alertType(String type) { alert.alertType = type; return this; }
            public Builder title(String title) { alert.title = title; return this; }
            public Builder message(String message) { alert.message = message; return this; }
            public Builder actions(List<String> actions) { alert.actions = actions; return this; }
            public Builder severity(String severity) { alert.severity = severity; return this; }
            
            public PreventiveAlert build() { return alert; }
        }
        
        // Getters
        public String getAlertType() { return alertType; }
        public String getTitle() { return title; }
        public String getMessage() { return message; }
        public List<String> getActions() { return actions; }
        public String getSeverity() { return severity; }
    }

    /**
     * Generates health insights based on wellness data
     */
    public String generateHealthInsights(List<SanteBienEtre> healthData) {
        if (healthData.isEmpty()) {
            return "Données insuffisantes pour générer des insights santé.";
        }

        StringBuilder insights = new StringBuilder();
        
        // Calculate averages
        double avgStress = healthData.stream()
            .mapToInt(SanteBienEtre::getNiveauStress)
            .average()
            .orElse(0.0);

        // Mood analysis
        Map<String, Long> moodCounts = healthData.stream()
            .collect(java.util.stream.Collectors.groupingBy(
                SanteBienEtre::getHumeur,
                java.util.stream.Collectors.counting()
            ));

        String mostCommonMood = moodCounts.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("Inconnu");

        insights.append(String.format("• Niveau de stress moyen : %.1f/10\n", avgStress));
        insights.append(String.format("• Humeur la plus fréquente : %s\n", mostCommonMood));

        if (avgStress > 7) {
            insights.append("⚠️ Stress élevé détecté - recommandé de pratiquer la relaxation\n");
        } else if (avgStress < 4) {
            insights.append("✅ Bon niveau de stress - continuez ainsi !\n");
        }

        // Trend analysis
        if (healthData.size() >= 2) {
            healthData.sort((a, b) -> a.getDateSuivi().compareTo(b.getDateSuivi()));
            int recentStress = healthData.get(healthData.size() - 1).getNiveauStress();
            int previousStress = healthData.get(healthData.size() - 2).getNiveauStress();
            
            if (recentStress > previousStress + 2) {
                insights.append("📈 Augmentation récente du stress - surveillez cette tendance\n");
            } else if (recentStress < previousStress - 2) {
                insights.append("📉 Amélioration récente du stress - excellent progrès !\n");
            }
        }

        return insights.toString();
    }

    /**
     * Generates sleep insights based on sleep tracking data
     */
    public String generateSleepInsights(List<SleepTracking> sleepData) {
        if (sleepData.isEmpty()) {
            return "Données insuffisantes pour générer des insights sommeil.";
        }

        StringBuilder insights = new StringBuilder();
        
        double avgQuality = sleepData.stream()
            .mapToInt(SleepTracking::getQualiteSommeil)
            .average()
            .orElse(0.0);

        insights.append(String.format("• Qualité moyenne du sommeil : %.1f/10\n", avgQuality));

        if (avgQuality < 5) {
            insights.append("⚠️ Qualité de sommeil faible - envisagez une meilleure hygiène de sommeil\n");
        } else if (avgQuality > 7) {
            insights.append("✅ Bonne qualité de sommeil - maintenez vos habitudes !\n");
        }

        // Sleep duration analysis
        List<Integer> durations = sleepData.stream()
            .map(SleepTracking::getDureeMinutes)
            .filter(d -> d > 0)
            .collect(java.util.stream.Collectors.toList());

        if (!durations.isEmpty()) {
            double avgDuration = durations.stream()
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0.0);
            
            insights.append(String.format("• Durée moyenne : %.1f heures\n", avgDuration / 60.0));
            
            if (avgDuration < 420) { // Less than 7 hours
                insights.append("⚠️ Durée de sommeil insuffisante - visez 7-9 heures par nuit\n");
            }
        }

        return insights.toString();
    }

    /**
     * Generates personalized recommendations based on health and sleep data
     */
    public String generatePersonalizedRecommendations(List<SanteBienEtre> healthData, List<SleepTracking> sleepData) {
        StringBuilder recommendations = new StringBuilder();
        
        if (!healthData.isEmpty()) {
            double avgStress = healthData.stream()
                .mapToInt(SanteBienEtre::getNiveauStress)
                .average()
                .orElse(0.0);

            if (avgStress > 7) {
                recommendations.append("• Pratiquez 10 minutes de méditation quotidienne\n");
                recommendations.append("• Essayez des exercices de respiration profonde\n");
            }
            
            // Check mood patterns
            long stressCount = healthData.stream()
                .filter(s -> "Stressé".equals(s.getHumeur()))
                .count();
            
            if (stressCount > healthData.size() * 0.5) {
                recommendations.append("• Envisagez des activités de détente régulières\n");
                recommendations.append("• Consultez un professionnel si le stress persiste\n");
            }
        }

        if (!sleepData.isEmpty()) {
            double avgSleepQuality = sleepData.stream()
                .mapToInt(SleepTracking::getQualiteSommeil)
                .average()
                .orElse(0.0);

            if (avgSleepQuality < 6) {
                recommendations.append("• Évitez les écrans 1 heure avant de dormir\n");
                recommendations.append("• Maintenez une température fraîche dans la chambre\n");
                recommendations.append("• Établissez une routine de coucher régulière\n");
            }
        }

        if (recommendations.length() == 0) {
            recommendations.append("• Continuez vos bonnes habitudes actuelles !\n");
            recommendations.append("• Maintenez un équilibre entre travail et repos\n");
        }

        return recommendations.toString();
    }

    /**
     * Generates predictive alerts based on health and sleep trends
     */
    public List<String> generatePredictiveAlerts(List<SanteBienEtre> healthData, List<SleepTracking> sleepData) {
        List<String> alerts = new java.util.ArrayList<>();

        // Stress trend analysis
        if (healthData.size() >= 3) {
            healthData.sort((a, b) -> a.getDateSuivi().compareTo(b.getDateSuivi()));
            List<Integer> recentStress = healthData.subList(healthData.size() - 3, healthData.size())
                .stream()
                .map(SanteBienEtre::getNiveauStress)
                .collect(java.util.stream.Collectors.toList());

            boolean increasingStress = recentStress.get(0) < recentStress.get(1) && recentStress.get(1) < recentStress.get(2);
            if (increasingStress && recentStress.get(2) > 6) {
                alerts.add("Tendance au stress à la hausse détectée - intervention recommandée");
            }
        }

        // Sleep quality trend analysis
        if (sleepData.size() >= 3) {
            sleepData.sort((a, b) -> a.getDateSommeil().compareTo(b.getDateSommeil()));
            List<Integer> recentSleep = sleepData.subList(sleepData.size() - 3, sleepData.size())
                .stream()
                .map(SleepTracking::getQualiteSommeil)
                .collect(java.util.stream.Collectors.toList());

            boolean decreasingSleep = recentSleep.get(0) > recentSleep.get(1) && recentSleep.get(1) > recentSleep.get(2);
            if (decreasingSleep && recentSleep.get(2) < 5) {
                alerts.add("Dégradation de la qualité du sommeil - attention à l'hygiène de sommeil");
            }
        }

        // Combined health-sleep analysis
        if (!healthData.isEmpty() && !sleepData.isEmpty()) {
            double avgStress = healthData.stream()
                .mapToInt(SanteBienEtre::getNiveauStress)
                .average()
                .orElse(0.0);

            double avgSleep = sleepData.stream()
                .mapToInt(SleepTracking::getQualiteSommeil)
                .average()
                .orElse(0.0);

            if (avgStress > 7 && avgSleep < 5) {
                alerts.add("Risque élevé : stress élevé combiné à mauvais sommeil - consultation recommandée");
            }
        }

        return alerts;
    }
}
