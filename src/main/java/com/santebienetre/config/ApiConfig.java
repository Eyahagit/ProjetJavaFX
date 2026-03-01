package com.santebienetre.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Gestionnaire de configuration pour les clés API
 */
public class ApiConfig {
    private static Properties properties;
    
    static {
        loadProperties();
    }
    
    private static void loadProperties() {
        properties = new Properties();
        try (InputStream input = ApiConfig.class.getClassLoader().getResourceAsStream("api-config.properties")) {
            if (input != null) {
                properties.load(input);
                System.out.println("[CONFIG] API configuration loaded successfully");
            } else {
                System.out.println("[CONFIG] No api-config.properties found, using default values");
            }
        } catch (IOException e) {
            System.err.println("[CONFIG] Error loading API configuration: " + e.getMessage());
        }
    }
    
    public static String getEdamamAppId() {
        return properties.getProperty("EDAMAM_APP_ID", "VOTRE_EDAMAM_APP_ID");
    }
    
    public static String getEdamamAppKey() {
        return properties.getProperty("EDAMAM_APP_KEY", "VOTRE_EDAMAM_APP_KEY");
    }
    
    public static String getStravaClientId() {
        return properties.getProperty("STRAVA_CLIENT_ID", "VOTRE_STRAVA_CLIENT_ID");
    }
    
    public static String getStravaClientSecret() {
        return properties.getProperty("STRAVA_CLIENT_SECRET", "VOTRE_STRAVA_CLIENT_SECRET");
    }
    
    public static String getUsdaApiKey() {
        return properties.getProperty("USDA_API_KEY", "VOTRE_USDA_API_KEY");
    }
    
    public static String getFitbitClientId() {
        return properties.getProperty("FITBIT_CLIENT_ID", "VOTRE_FITBIT_CLIENT_ID");
    }
    
    public static String getFitbitClientSecret() {
        return properties.getProperty("FITBIT_CLIENT_SECRET", "VOTRE_FITBIT_CLIENT_SECRET");
    }
    
    public static String getFitbitRedirectUri() {
        return properties.getProperty("FITBIT_REDIRECT_URI", "http://localhost:8080/callback");
    }
    
    public static boolean isConfigured() {
        return !getEdamamAppId().equals("VOTRE_EDAMAM_APP_ID") || 
               !getFitbitClientId().equals("VOTRE_FITBIT_CLIENT_ID");
    }
    
    public static void printConfiguration() {
        System.out.println("[CONFIG] Configuration actuelle:");
        System.out.println("  Edamam App ID: " + (getEdamamAppId().equals("VOTRE_EDAMAM_APP_ID") ? "❌ Non configuré" : "✅ Configuré"));
        System.out.println("  Fitbit Client ID: " + (getFitbitClientId().equals("VOTRE_FITBIT_CLIENT_ID") ? "❌ Non configuré" : "✅ Configuré"));
    }
}
