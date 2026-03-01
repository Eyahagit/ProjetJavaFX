package Services;

import java.util.*;

/**
 * Service IA de Chatbot Bien-être Public avec réponses intelligentes
 */
public class PublicWellnessChatbot {
    
    // Base de connaissances pour les réponses
    private final Map<String, List<String>> knowledgeBase = new HashMap<>();
    private final Map<String, String> emergencyKeywords = new HashMap<>();
    
    public PublicWellnessChatbot() {
        initializeKnowledgeBase();
        initializeEmergencyKeywords();
    }
    
    /**
     * Initialise la base de connaissances du chatbot
     */
    private void initializeKnowledgeBase() {
        // Stress et anxiété
        knowledgeBase.put("stress", Arrays.asList(
            "Le stress est une réaction naturelle. Essayez la respiration profonde : inspirez 4 secondes, retenez 4, expirez 6.",
            "Pour gérer le stress, je vous recommande 5 minutes de méditation quotidienne.",
            "Le stress peut être réduit avec de l'exercice physique modéré comme la marche."
        ));
        
        knowledgeBase.put("anxiété", Arrays.asList(
            "L'anxiété est normale. Concentrez-vous sur le moment présent avec la pleine conscience.",
            "Essayez la technique 5-4-3-2-1 : nommez 5 choses que vous voyez, 4 que vous touchez, 3 que vous entendez...",
            "La respiration carrée aide : 4s inspirez, 4s retenez, 4s expirez, 4s pause."
        ));
        
        // Sommeil
        knowledgeBase.put("sommeil", Arrays.asList(
            "Pour mieux dormir, évitez les écrans 1h avant le coucher et maintenez une température fraîche (18-20°C).",
            "La routine de coucher est clé : mêmes horaires, pas de café après 14h, lecture plutôt que téléphone.",
            "Si vous avez du mal à dormir, essayez la relaxation musculaire progressive."
        ));
        
        knowledgeBase.put("insomnie", Arrays.asList(
            "L'insomnie passe souvent en évitant les siestes longues et en s'exposant à la lumière le matin.",
            "Ne regardez pas l'heure si vous vous réveillez la nuit. Levez-vous si vous ne vous rendezormez pas après 20min.",
            "La thérapie cognitivo-comportementale est très efficace contre l'insomnie chronique."
        ));
        
        // Humeur et dépression
        knowledgeBase.put("humeur", Arrays.asList(
            "L'humeur fluctue normalement. Tenez un journal de gratitude : 3 choses positives chaque jour.",
            "L'exercice libère des endorphines qui améliorent naturellement l'humeur.",
            "La socialisation, même virtuelle, aide maintenir une bonne humeur."
        ));
        
        knowledgeBase.put("dépression", Arrays.asList(
            "Si vous vous sentez déprimé, parlez-en à quelqu'un de confiance. Vous n'êtes pas seul.",
            "Les petites victoires quotidiennes aident : fixez-vous un objectif réalisable chaque jour.",
            "La lumière naturelle et l'exercice sont des antidépresseurs naturels efficaces."
        ));
        
        // Bien-être général
        knowledgeBase.put("énergie", Arrays.asList(
            "Pour plus d'énergie, hydratez-vous bien et mangez des aliments à index glycémique bas.",
            "Le manque de sommeil réduit l'énergie de 30%. Priorisez 7-8h de sommeil.",
            "Les micro-pauses de 5 minutes toutes les heures maintiennent l'énergie."
        ));
        
        knowledgeBase.put("concentration", Arrays.asList(
            "Pour améliorer la concentration, travaillez par blocs de 25min (technique Pomodoro).",
            "La méditation de pleine conscience augmente la capacité de concentration.",
            "Éliminez les distractions : téléphone en mode avion, notifications désactivées."
        ));
        
        // Relations sociales
        knowledgeBase.put("relations", Arrays.asList(
            "Les relations de qualité nécessitent de l'écoute active et de l'empathie.",
            "Exprimez vos besoins clairement mais avec bienveillance dans vos relations.",
            "La vulnérabilité authentique renforce les liens interpersonnels."
        ));
        
        knowledgeBase.put("solitude", Arrays.asList(
            "La solitude est différente de l'isolement. Cultivez vos intérêts pour attirer des personnes similaires.",
            "Les groupes d'intérêt (lecture, sport, bénévolat) sont excellents pour créer des liens.",
            "La qualité des relations prime sur la quantité. Une amie profonde vaut mieux que 50 connaissances."
        ));
    }
    
    /**
     * Initialise les mots-clés d'urgence
     */
    private void initializeEmergencyKeywords() {
        emergencyKeywords.put("suicide", "Si vous avez des pensées suicidaires, appelez le 3114 (24h/24, gratuit). Vous n'êtes pas seul et de l'aide est disponible.");
        emergencyKeywords.put("mort", "Si vous avez des pensées sombres, parlez-en immédiatement à un professionnel ou appelez le 3114.");
        emergencyKeywords.put("crise", "En cas de crise, appelez le 15 (SAMU) ou le 3114 pour un soutien psychologique d'urgence.");
        emergencyKeywords.put("urgence", "Pour toute urgence psychologique, le 3114 est disponible 24h/24.");
    }
    
    /**
     * Répond à un message utilisateur avec IA
     */
    public ChatResponse respondToUser(String message, String sessionId) {
        message = message.toLowerCase().trim();
        
        // Vérifier les mots-clés d'urgence
        for (String keyword : emergencyKeywords.keySet()) {
            if (message.contains(keyword)) {
                return ChatResponse.builder()
                    .message(emergencyKeywords.get(keyword))
                    .isCrisis(true)
                    .suggestedActions(Arrays.asList("🚞 Appeler le 3114", "📞 Contacter un professionnel", "👥 Parler à un proche"))
                    .build();
            }
        }
        
        // Analyser l'intention et générer une réponse
        String response = generateIntelligentResponse(message);
        List<String> suggestions = generateSuggestions(message);
        
        return ChatResponse.builder()
            .message(response)
            .isCrisis(false)
            .suggestedActions(suggestions)
            .confidence(calculateConfidence(message))
            .build();
    }
    
    /**
     * Génère une réponse intelligente basée sur le message
     */
    private String generateIntelligentResponse(String message) {
        // Calculer les scores de similarité
        Map<String, Double> scores = new HashMap<>();
        
        for (String topic : knowledgeBase.keySet()) {
            scores.put(topic, calculateSimilarity(message, topic));
        }
        
        // Trouver le meilleur sujet
        String bestTopic = scores.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("général");
        
        Double bestScore = scores.get(bestTopic);
        
        if (bestScore > 0.3) {
            // Réponse spécifique au sujet
            List<String> responses = knowledgeBase.get(bestTopic);
            return responses.get(new Random().nextInt(responses.size()));
        } else {
            // Réponse générale
            return generateGeneralResponse(message);
        }
    }
    
    /**
     * Calcule la similarité entre deux textes
     */
    private double calculateSimilarity(String text1, String text2) {
        String[] words1 = text1.split("\\s+");
        String[] words2 = text2.split("\\s+");
        
        int commonWords = 0;
        for (String word1 : words1) {
            for (String word2 : words2) {
                if (word1.contains(word2) || word2.contains(word1)) {
                    commonWords++;
                    break;
                }
            }
        }
        
        return (double) commonWords / Math.max(words1.length, words2.length);
    }
    
    /**
     * Génère une réponse générale
     */
    private String generateGeneralResponse(String message) {
        List<String> generalResponses = Arrays.asList(
            "Je comprends ce que vous ressentez. Prenez un moment pour respirer profondément.",
            "Chaque jour est une opportunité de prendre soin de soi. Soyez bienveillant avec vous-même.",
            "Le bien-être est un voyage. Célébrez chaque petit progrès.",
            "Vous avez déjà fait le premier pas en cherchant de l'aide. C'est courageux.",
            "La pleine conscience peut aider : concentrez-vous sur votre respiration pendant 2 minutes."
        );
        
        return generalResponses.get(new Random().nextInt(generalResponses.size()));
    }
    
    /**
     * Génère des suggestions d'actions
     */
    private List<String> generateSuggestions(String message) {
        List<String> suggestions = new ArrayList<>();
        
        if (message.contains("stress") || message.contains("anx")) {
            suggestions.add("🧘‍♀️ 5 minutes de respiration");
            suggestions.add("🚶‍♀️ Marche de 10 minutes");
            suggestions.add("📝 Écrire ses pensées");
        }
        
        if (message.contains("sommeil") || message.contains("dormir")) {
            suggestions.add("😴 Routine de coucher");
            suggestions.add("📱 Pas d'écrans 1h avant");
            suggestions.add("🌿 Tisane relaxante");
        }
        
        if (message.contains("humeur") || message.contains("triste")) {
            suggestions.add("🎵 Musique positive");
            suggestions.add("👥 Appeler un ami");
            suggestions.add("🏃‍♀️ Exercice léger");
        }
        
        if (suggestions.isEmpty()) {
            suggestions.add("💬 Continuer la discussion");
            suggestions.add("🧠 Faire un quiz bien-être");
            suggestions.add("📖 Lire des ressources");
        }
        
        return suggestions;
    }
    
    /**
     * Calcule le niveau de confiance de la réponse
     */
    private double calculateConfidence(String message) {
        Map<String, Double> scores = new HashMap<>();
        
        for (String topic : knowledgeBase.keySet()) {
            scores.put(topic, calculateSimilarity(message, topic));
        }
        
        double maxScore = scores.values().stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
        return Math.min(maxScore * 2, 1.0); // Normaliser entre 0 et 1
    }
    
    /**
     * Classe interne pour les réponses du chatbot
     */
    public static class ChatResponse {
        private String message;
        private boolean isCrisis;
        private List<String> suggestedActions;
        private double confidence;
        
        public static Builder builder() { return new Builder(); }
        
        public static class Builder {
            private ChatResponse response = new ChatResponse();
            
            public Builder message(String message) { response.message = message; return this; }
            public Builder isCrisis(boolean isCrisis) { response.isCrisis = isCrisis; return this; }
            public Builder suggestedActions(List<String> actions) { response.suggestedActions = actions; return this; }
            public Builder confidence(double confidence) { response.confidence = confidence; return this; }
            
            public ChatResponse build() { return response; }
        }
        
        // Getters
        public String getMessage() { return message; }
        public boolean isCrisis() { return isCrisis; }
        public List<String> getSuggestedActions() { return suggestedActions; }
        public double getConfidence() { return confidence; }
    }
}
