package Services;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import config.ApiConfig;
import okhttp3.*;

import java.io.IOException;

/**
 * Service for integrating with Strava API (FREE alternative to Fitbit)
 * Much simpler than Fitbit OAuth and completely free
 */
public class StravaApiService {
    
    private static final String STRAVA_API_BASE = "https://www.strava.com/api/v3";
    private static final String STRAVA_AUTH_URL = "https://www.strava.com/oauth/authorize";
    private static final String STRAVA_TOKEN_URL = "https://www.strava.com/oauth/token";
    
    private final OkHttpClient client;
    private final Gson gson;
    
    // Configuration OAuth 2.0
    private String clientId = ApiConfig.getStravaClientId();
    private String clientSecret = ApiConfig.getStravaClientSecret();
    private String redirectUri = "http://localhost:8080/callback";
    
    private String accessToken;
    
    public StravaApiService() {
        this.client = new OkHttpClient.Builder()
            .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .build();
        this.gson = new Gson();
        
        System.out.println("[STRAVA] Service initialized with Client ID: " + 
            (clientId.equals("VOTRE_STRAVA_CLIENT_ID") ? "Not configured" : "Configured"));
    }
    
    /**
     * Génère l'URL d'autorisation OAuth 2.0 pour Strava
     */
    public String getAuthorizationUrl() {
        return STRAVA_AUTH_URL + "?" +
            "client_id=" + clientId + "&" +
            "response_type=code&" +
            "redirect_uri=" + redirectUri + "&" +
            "approval_prompt=auto&" +
            "scope=read,activity:read";
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
                .add("grant_type", "authorization_code")
                .build();
            
            Request request = new Request.Builder()
                .url(STRAVA_TOKEN_URL)
                .post(formBody)
                .build();
            
            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();
                    
                    if (json.has("access_token")) {
                        this.accessToken = json.get("access_token").getAsString();
                        System.out.println("[STRAVA] Access token obtained successfully");
                        return true;
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("[STRAVA] Error exchanging code for token: " + e.getMessage());
        }
        return false;
    }
    
    /**
     * Récupère les activités récentes de l'utilisateur
     */
    public String getRecentActivities() {
        if (accessToken == null) {
            return "❌ Non connecté à Strava";
        }
        
        try {
            String url = STRAVA_API_BASE + "/athlete/activities?per_page=1";
            
            Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + accessToken)
                .get()
                .build();
            
            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    return parseActivities(responseBody);
                } else {
                    return "❌ Erreur de connexion Strava";
                }
            }
        } catch (IOException e) {
            System.err.println("[STRAVA] Error getting activities: " + e.getMessage());
            return "❌ Erreur de connexion";
        }
    }
    
    /**
     * Parse les activités Strava pour extraire les informations pertinentes
     */
    private String parseActivities(String responseBody) {
        try {
            JsonObject[] activities = gson.fromJson(responseBody, JsonObject[].class);
            
            if (activities.length == 0) {
                return "📊 Aucune activité récente";
            }
            
            JsonObject latestActivity = activities[0];
            
            String type = latestActivity.get("type").getAsString();
            String name = latestActivity.get("name").getAsString();
            double distance = latestActivity.has("distance") ? 
                latestActivity.get("distance").getAsDouble() / 1000.0 : 0; // Convert to km
            int movingTime = latestActivity.has("moving_time") ? 
                latestActivity.get("moving_time").getAsInt() : 0; // seconds
            
            // Format the time
            int hours = movingTime / 3600;
            int minutes = (movingTime % 3600) / 60;
            
            StringBuilder result = new StringBuilder();
            result.append("🏃 Dernière activité Strava:\n");
            result.append("📝 ").append(name).append("\n");
            result.append("🏃‍♂️ Type: ").append(type).append("\n");
            
            if (distance > 0) {
                result.append("📏 Distance: ").append(String.format("%.1f", distance)).append(" km\n");
            }
            
            if (movingTime > 0) {
                result.append("⏱️ Durée: ");
                if (hours > 0) {
                    result.append(hours).append("h ");
                }
                result.append(minutes).append(" min\n");
            }
            
            result.append("✅ Connecté à Strava");
            
            return result.toString();
            
        } catch (Exception e) {
            System.err.println("[STRAVA] Error parsing activities: " + e.getMessage());
            return "❌ Erreur de format des données";
        }
    }
    
    /**
     * Vérifie si le service est configuré
     */
    public boolean isConfigured() {
        return !clientId.equals("VOTRE_STRAVA_CLIENT_ID");
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
        System.out.println("[STRAVA] Disconnected");
    }
}
