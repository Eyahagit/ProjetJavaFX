package com.santebienetre.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.*;

import java.io.IOException;
import java.time.LocalDate;

/**
 * Service for integrating with Google Fit API (FREE and simple)
 * Uses Google OAuth - very reliable and easy to set up
 */
public class GoogleFitApiService {
    
    private static final String GOOGLE_FIT_BASE = "https://www.googleapis.com/fitness/v1/users/me";
    private static final String GOOGLE_AUTH_URL = "https://accounts.google.com/o/oauth2/auth";
    private static final String GOOGLE_TOKEN_URL = "https://oauth2.googleapis.com/token";
    
    private final OkHttpClient client;
    private final Gson gson;
    
    // Configuration OAuth 2.0
    private String clientId = "YOUR_GOOGLE_CLIENT_ID"; // Simplified - no config file needed
    private String clientSecret = "YOUR_GOOGLE_CLIENT_SECRET";
    private String redirectUri = "http://localhost:8080/callback";
    
    private String accessToken;
    
    public GoogleFitApiService() {
        this.client = new OkHttpClient.Builder()
            .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .build();
        this.gson = new Gson();
        
        System.out.println("[GOOGLE FIT] Service initialized - Google account required");
    }
    
    /**
     * Génère l'URL d'autorisation OAuth 2.0 pour Google
     */
    public String getAuthorizationUrl() {
        return GOOGLE_AUTH_URL + "?" +
            "client_id=" + clientId + "&" +
            "redirect_uri=" + redirectUri + "&" +
            "response_type=code&" +
            "scope=https://www.googleapis.com/auth/fitness.activity.read https://www.googleapis.com/auth/fitness.body.read&" +
            "access_type=offline&" +
            "prompt=consent";
    }
    
    /**
     * Échange le code d'autorisation contre un token d'accès
     */
    public boolean exchangeCodeForToken(String authorizationCode) {
        try {
            RequestBody formBody = new FormBody.Builder()
                .add("client_id", clientId)
                .add("client_secret", clientSecret)
                .add("code", authorizationCode)
                .add("redirect_uri", redirectUri)
                .add("grant_type", "authorization_code")
                .build();
            
            Request request = new Request.Builder()
                .url(GOOGLE_TOKEN_URL)
                .post(formBody)
                .build();
            
            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();
                    
                    if (json.has("access_token")) {
                        this.accessToken = json.get("access_token").getAsString();
                        System.out.println("[GOOGLE FIT] Access token obtained successfully");
                        return true;
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("[GOOGLE FIT] Error exchanging code for token: " + e.getMessage());
        }
        return false;
    }
    
    /**
     * Récupère les données de pas de la journée
     */
    public String getTodaySteps() {
        if (accessToken == null) {
            return "❌ Non connecté à Google Fit";
        }
        
        try {
            // Get today's date in nanoseconds (Google Fit uses nanoseconds)
            long now = System.currentTimeMillis();
            long startOfDay = now - (now % (24 * 60 * 60 * 1000));
            
            String url = GOOGLE_FIT_BASE + "/dataset:aggregate";
            
            // Build the request body for steps
            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("aggregateBy", "[{\"dataTypeName\":\"com.google.step_count.delta\",\"dataSourceId\":\"derived:com.google.step_count.delta:com.google.android.gms:estimated_steps\"}]");
            requestBody.addProperty("bucketByTime", "{\"durationMillis\":86400000}");
            requestBody.addProperty("startTimeMillis", startOfDay);
            requestBody.addProperty("endTimeMillis", now);
            
            RequestBody body = RequestBody.create(
                requestBody.toString(),
                MediaType.get("application/json; charset=utf-8")
            );
            
            Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + accessToken)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();
            
            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    return parseStepsResponse(responseBody);
                } else {
                    return "❌ Erreur de connexion Google Fit";
                }
            }
        } catch (IOException e) {
            System.err.println("[GOOGLE FIT] Error getting steps: " + e.getMessage());
            return "❌ Erreur de connexion";
        }
    }
    
    /**
     * Parse la réponse Google Fit pour extraire les pas
     */
    private String parseStepsResponse(String responseBody) {
        try {
            JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();
            
            if (json.has("bucket") && json.getAsJsonArray("bucket").size() > 0) {
                JsonObject bucket = json.getAsJsonArray("bucket").get(0).getAsJsonObject();
                
                if (bucket.has("dataset") && bucket.getAsJsonArray("dataset").size() > 0) {
                    JsonObject dataset = bucket.getAsJsonArray("dataset").get(0).getAsJsonObject();
                    
                    if (dataset.has("point") && dataset.getAsJsonArray("point").size() > 0) {
                        JsonObject point = dataset.getAsJsonArray("point").get(0).getAsJsonObject();
                        
                        if (point.has("value") && point.getAsJsonArray("value").size() > 0) {
                            JsonObject value = point.getAsJsonArray("value").get(0).getAsJsonObject();
                            int steps = value.has("intVal") ? value.get("intVal").getAsInt() : 0;
                            
                            StringBuilder result = new StringBuilder();
                            result.append("📊 Données Google Fit:\n");
                            result.append("👆 Pas aujourd'hui: ").append(steps).append("\n");
                            
                            // Add health advice based on steps
                            if (steps < 5000) {
                                result.append("💡 Conseil: Essayez de marcher un peu plus !");
                            } else if (steps < 10000) {
                                result.append("💡 Conseil: Bon niveau d'activité !");
                            } else {
                                result.append("💡 Conseil: Excellent niveau d'activité !");
                            }
                            
                            result.append("\n✅ Connecté à Google Fit");
                            
                            return result.toString();
                        }
                    }
                }
            }
            
            return "📊 Aucune donnée de pas aujourd'hui";
            
        } catch (Exception e) {
            System.err.println("[GOOGLE FIT] Error parsing steps response: " + e.getMessage());
            return "❌ Erreur de format des données";
        }
    }
    
    /**
     * Vérifie si l'utilisateur est connecté
     */
    public boolean isConnected() {
        return accessToken != null;
    }
    
    /**
     * Déconnexion
     */
    public void disconnect() {
        this.accessToken = null;
        System.out.println("[GOOGLE FIT] Disconnected");
    }
    
    /**
     * Message d'aide pour la configuration
     */
    public String getHelpMessage() {
        return "🔧 Google Fit nécessite une configuration Google Cloud:\n" +
               "1. Allez sur https://console.cloud.google.com/\n" +
               "2. Créez un nouveau projet\n" +
               "3. Activez Google Fit API\n" +
               "4. Créez des identifiants OAuth 2.0\n" +
               "5. Configurez les clés dans le code";
    }
}
