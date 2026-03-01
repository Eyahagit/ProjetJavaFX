package Services;

import java.util.Random;

/**
 * Mock Fitness API Service - AUCUNE CLÉ REQUISE !
 * Simule des données fitness réalistes pour tester l'application
 */
public class MockFitnessApiService {
    
    private final Random random = new Random();
    private boolean connected = false;
    
    public MockFitnessApiService() {
        System.out.println("[MOCK FITNESS] Service initialized - NO API KEY REQUIRED!");
    }
    
    /**
     * Simule une connexion (toujours réussie)
     */
    public boolean connect() {
        // Simuler une connexion de 2 secondes
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        this.connected = true;
        System.out.println("[MOCK FITNESS] Connected successfully!");
        return true;
    }
    
    /**
     * Simule la déconnexion
     */
    public void disconnect() {
        this.connected = false;
        System.out.println("[MOCK FITNESS] Disconnected");
    }
    
    /**
     * Génère des données de pas réalistes
     */
    public String getTodaySteps() {
        if (!connected) {
            return "❌ Non connecté - Cliquez sur Connexion";
        }
        
        // Générer un nombre de pas réaliste (2000-15000)
        int steps = 2000 + random.nextInt(13000);
        
        StringBuilder result = new StringBuilder();
        result.append("📊 Données Fitness Simulées:\n");
        result.append("👆 Pas aujourd'hui: ").append(steps).append("\n");
        
        // Ajouter des conseils personnalisés
        if (steps < 5000) {
            result.append("💡 Conseil: Essayez de marcher un peu plus !");
        } else if (steps < 10000) {
            result.append("💡 Conseil: Bon niveau d'activité !");
        } else {
            result.append("💡 Conseil: Excellent niveau d'activité !");
        }
        
        result.append("\n🏃‍♂️ Distance: ").append(String.format("%.1f", steps * 0.0007)).append(" km\n");
        result.append("🔥 Calories: ").append(steps * 0.04).append(" kcal\n");
        result.append("⏱️ Temps actif: ").append(steps / 100).append(" minutes\n");
        result.append("✅ Connecté (Mode Simulation)");
        
        return result.toString();
    }
    
    /**
     * Génère des données de sommeil réalistes
     */
    public String getSleepData() {
        if (!connected) {
            return "❌ Non connecté - Cliquez sur Connexion";
        }
        
        // Générer des heures de sommeil réalistes (5-9 heures)
        int sleepHours = 5 + random.nextInt(5);
        int sleepMinutes = random.nextInt(60);
        
        StringBuilder result = new StringBuilder();
        result.append("😴 Données de Sommeil Simulées:\n");
        result.append("🌙 Durée: ").append(sleepHours).append("h ").append(sleepMinutes).append("min\n");
        
        if (sleepHours < 7) {
            result.append("💡 Conseil: Essayez de dormir plus longtemps !");
        } else {
            result.append("💡 Conseil: Bonne durée de sommeil !");
        }
        
        result.append("📊 Qualité: ").append(getSleepQuality()).append("\n");
        result.append("✅ Connecté (Mode Simulation)");
        
        return result.toString();
    }
    
    /**
     * Génère une qualité de sommeil réaliste
     */
    private String getSleepQuality() {
        int quality = random.nextInt(100);
        if (quality < 30) return "Mauvaise 😞";
        if (quality < 70) return "Correcte 😐";
        return "Excellente 😊";
    }
    
    /**
     * Génère des données de fréquence cardiaque
     */
    public String getHeartRate() {
        if (!connected) {
            return "❌ Non connecté - Cliquez sur Connexion";
        }
        
        // Fréquence cardiaque au repos (60-100 bpm)
        int restingHR = 60 + random.nextInt(40);
        
        StringBuilder result = new StringBuilder();
        result.append("❤️ Fréquence Cardiaque Simulée:\n");
        result.append("📈 Au repos: ").append(restingHR).append(" bpm\n");
        
        if (restingHR < 60) {
            result.append("💡 Conseil: Très bonne forme physique !");
        } else if (restingHR < 80) {
            result.append("💡 Conseil: Bonne santé cardiovasculaire !");
        } else {
            result.append("💡 Conseil: Considérez plus d'exercice !");
        }
        
        result.append("✅ Connecté (Mode Simulation)");
        
        return result.toString();
    }
    
    /**
     * Vérifie si connecté
     */
    public boolean isConnected() {
        return connected;
    }
    
    /**
     * Message d'aide
     */
    public String getHelpMessage() {
        return "🎭 MODE SIMULATION:\n" +
               "✅ Aucune clé API requise\n" +
               "✅ Données réalistes générées\n" +
               "✅ Test immédiat possible\n" +
               "✅ Pour passer à une vraie API: configurez Strava ou Google Fit";
    }
}
