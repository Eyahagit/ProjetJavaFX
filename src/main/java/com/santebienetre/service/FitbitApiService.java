package com.santebienetre.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.santebienetre.config.ApiConfig;
import okhttp3.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.concurrent.TimeUnit;

/**
 * Service for integrating with Fitbit Web API
 * Requires OAuth 2.0 authentication
 */
public class FitbitApiService {
    
    private static final String FITBIT_API_BASE = "https://api.fitbit.com/1/user";
    private static final String AUTH_URL = "https://www.fitbit.com/oauth2/authorize";
    private static final String TOKEN_URL = "https://api.fitbit.com/oauth2/token";
    
    // Configuration OAuth 2.0 (chargée depuis la configuration)
    private String clientId = ApiConfig.getFitbitClientId();
    private String clientSecret = ApiConfig.getFitbitClientSecret();
    private String redirectUri = ApiConfig.getFitbitRedirectUri();
    
    private final OkHttpClient client;
    private final Gson gson;
    private String accessToken;
    private String refreshToken;
    
    public FitbitApiService() {
        this.client = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build();
        this.gson = new Gson();
        
        System.out.println("[FITBIT] Service initialized with Client ID: " + 
            (clientId.equals("VOTRE_FITBIT_CLIENT_ID") ? "Not configured" : "Configured"));
    }
    
    /**
     * Génère l'URL d'autorisation OAuth 2.0
     * @return URL à laquelle rediriger l'utilisateur pour l'autorisation
     */
    public String getAuthorizationUrl() {
        return AUTH_URL + "?" +
            "response_type=code&" +
            "client_id=" + clientId + "&" +
            "redirect_uri=" + redirectUri + "&" +
            "scope=activity%20nutrition%20profile%20settings%20sleep%20social%20weight&" +
            "expires_in=604800";
    }
    
    /**
     * Échange le code d'autorisation contre des tokens d'accès
     * @param authorizationCode code reçu de Fitbit après autorisation
     * @return true si l'échange a réussi
     */
    public boolean exchangeCodeForTokens(String authorizationCode) throws IOException {
        String requestBody = "client_id=" + clientId + 
            "&grant_type=authorization_code" +
            "&redirect_uri=" + redirectUri +
            "&code=" + authorizationCode;
        
        RequestBody body = RequestBody.create(
            requestBody,
            MediaType.get("application/x-www-form-urlencoded")
        );
        
        Request request = new Request.Builder()
            .url(TOKEN_URL)
            .header("Authorization", "Basic " + java.util.Base64.getEncoder()
                .encodeToString((clientId + ":" + clientSecret).getBytes()))
            .header("Content-Type", "application/x-www-form-urlencoded")
            .post(body)
            .build();
        
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                return false;
            }
            
            String responseBody = response.body().string();
            JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();
            
            this.accessToken = json.get("access_token").getAsString();
            this.refreshToken = json.get("refresh_token").getAsString();
            
            return true;
        }
    }
    
    /**
     * Récupère le nombre de pas pour aujourd'hui
     * @return nombre de pas
     */
    public int getTodaySteps() throws IOException {
        return getStepsForDate(LocalDate.now());
    }
    
    /**
     * Récupère le nombre de pas pour une date spécifique
     * @param date date souhaitée
     * @return nombre de pas
     */
    public int getStepsForDate(LocalDate date) throws IOException {
        if (accessToken == null) {
            throw new IllegalStateException("Non authentifié - appelez d'abord exchangeCodeForTokens");
        }
        
        String url = FITBIT_API_BASE + "/-/activities/date/" + 
            date.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE) + ".json";
        
        Request request = new Request.Builder()
            .url(url)
            .header("Authorization", "Bearer " + accessToken)
            .get()
            .build();
        
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                if (response.code() == 401) {
                    // Token expiré, essayer de rafraîchir
                    if (refreshAccessToken()) {
                        return getStepsForDate(date); // Réessayer avec le nouveau token
                    }
                }
                throw new IOException("Erreur API Fitbit: " + response.code());
            }
            
            String responseBody = response.body().string();
            JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();
            
            return json.getAsJsonObject("summary").get("steps").getAsInt();
        }
    }
    
    /**
     * Récupère les données d'activité complètes pour aujourd'hui
     */
    public FitbitActivityData getTodayActivity() throws IOException {
        return getActivityForDate(LocalDate.now());
    }
    
    /**
     * Récupère les données d'activité pour une date spécifique
     */
    public FitbitActivityData getActivityForDate(LocalDate date) throws IOException {
        if (accessToken == null) {
            throw new IllegalStateException("Non authentifié");
        }
        
        String url = FITBIT_API_BASE + "/-/activities/date/" + 
            date.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE) + ".json";
        
        Request request = new Request.Builder()
            .url(url)
            .header("Authorization", "Bearer " + accessToken)
            .get()
            .build();
        
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Erreur API Fitbit: " + response.code());
            }
            
            String responseBody = response.body().string();
            JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();
            
            return parseActivityData(json, date);
        }
    }
    
    /**
     * Récupère les données de sommeil
     */
    public FitbitSleepData getSleepData(LocalDate date) throws IOException {
        if (accessToken == null) {
            throw new IllegalStateException("Non authentifié");
        }
        
        String url = FITBIT_API_BASE + "/-/sleep/date/" + 
            date.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE) + ".json";
        
        Request request = new Request.Builder()
            .url(url)
            .header("Authorization", "Bearer " + accessToken)
            .get()
            .build();
        
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Erreur API Fitbit: " + response.code());
            }
            
            String responseBody = response.body().string();
            JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();
            
            return parseSleepData(json);
        }
    }
    
    /**
     * Rafraîchit le token d'accès
     */
    private boolean refreshAccessToken() throws IOException {
        String requestBody = "grant_type=refresh_token" +
            "&refresh_token=" + refreshToken;
        
        RequestBody body = RequestBody.create(
            requestBody,
            MediaType.get("application/x-www-form-urlencoded")
        );
        
        Request request = new Request.Builder()
            .url(TOKEN_URL)
            .header("Authorization", "Basic " + java.util.Base64.getEncoder()
                .encodeToString((clientId + ":" + clientSecret).getBytes()))
            .header("Content-Type", "application/x-www-form-urlencoded")
            .post(body)
            .build();
        
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                return false;
            }
            
            String responseBody = response.body().string();
            JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();
            
            this.accessToken = json.get("access_token").getAsString();
            if (json.has("refresh_token")) {
                this.refreshToken = json.get("refresh_token").getAsString();
            }
            
            return true;
        }
    }
    
    private FitbitActivityData parseActivityData(JsonObject json, LocalDate date) {
        FitbitActivityData data = new FitbitActivityData();
        data.setDate(date);
        
        JsonObject summary = json.getAsJsonObject("summary");
        data.setSteps(summary.get("steps").getAsInt());
        data.setCaloriesOut(summary.get("caloriesOut").getAsInt());
        data.setCaloriesBMR(summary.get("caloriesBMR").getAsInt());
        data.setActivityCalories(summary.get("activityCalories").getAsInt());
        
        if (summary.has("distances")) {
            JsonObject distances = summary.getAsJsonArray("distances").get(0).getAsJsonObject();
            data.setDistance(distances.get("distance").getAsDouble());
        }
        
        if (summary.has("floors")) {
            data.setFloors(summary.get("floors").getAsInt());
        }
        
        if (summary.has("minutesSedentary")) {
            data.setMinutesSedentary(summary.get("minutesSedentary").getAsInt());
        }
        
        if (summary.has("minutesLightlyActive")) {
            data.setMinutesLightlyActive(summary.get("minutesLightlyActive").getAsInt());
        }
        
        if (summary.has("minutesFairlyActive")) {
            data.setMinutesFairlyActive(summary.get("minutesFairlyActive").getAsInt());
        }
        
        if (summary.has("minutesVeryActive")) {
            data.setMinutesVeryActive(summary.get("minutesVeryActive").getAsInt());
        }
        
        return data;
    }
    
    private FitbitSleepData parseSleepData(JsonObject json) {
        FitbitSleepData data = new FitbitSleepData();
        
        if (json.has("sleep") && json.getAsJsonArray("sleep").size() > 0) {
            JsonObject sleep = json.getAsJsonArray("sleep").get(0).getAsJsonObject();
            data.setMinutesAsleep(sleep.get("minutesAsleep").getAsInt());
            data.setMinutesToFallAsleep(sleep.get("minutesToFallAsleep").getAsInt());
            data.setMinutesAwake(sleep.get("minutesAwake").getAsInt());
            data.setMinutesAfterWakeup(sleep.get("minutesAfterWakeup").getAsInt());
            data.setSleepEfficiency(sleep.get("efficiency").getAsInt());
        }
        
        if (json.has("summary")) {
            JsonObject summary = json.getAsJsonObject("summary");
            data.setTotalMinutesAsleep(summary.get("totalMinutesAsleep").getAsInt());
            data.setTotalSleepRecords(summary.get("totalSleepRecords").getAsInt());
        }
        
        return data;
    }
    
    // Configuration
    public void setCredentials(String clientId, String clientSecret, String redirectUri) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = redirectUri;
    }
    
    public boolean isAuthenticated() {
        return accessToken != null;
    }
    
    // Classes internes pour les données
    public static class FitbitActivityData {
        private LocalDate date;
        private int steps;
        private int caloriesOut;
        private int caloriesBMR;
        private int activityCalories;
        private double distance;
        private int floors;
        private int minutesSedentary;
        private int minutesLightlyActive;
        private int minutesFairlyActive;
        private int minutesVeryActive;
        
        // Getters and Setters
        public LocalDate getDate() { return date; }
        public void setDate(LocalDate date) { this.date = date; }
        
        public int getSteps() { return steps; }
        public void setSteps(int steps) { this.steps = steps; }
        
        public int getCaloriesOut() { return caloriesOut; }
        public void setCaloriesOut(int caloriesOut) { this.caloriesOut = caloriesOut; }
        
        public int getCaloriesBMR() { return caloriesBMR; }
        public void setCaloriesBMR(int caloriesBMR) { this.caloriesBMR = caloriesBMR; }
        
        public int getActivityCalories() { return activityCalories; }
        public void setActivityCalories(int activityCalories) { this.activityCalories = activityCalories; }
        
        public double getDistance() { return distance; }
        public void setDistance(double distance) { this.distance = distance; }
        
        public int getFloors() { return floors; }
        public void setFloors(int floors) { this.floors = floors; }
        
        public int getMinutesSedentary() { return minutesSedentary; }
        public void setMinutesSedentary(int minutesSedentary) { this.minutesSedentary = minutesSedentary; }
        
        public int getMinutesLightlyActive() { return minutesLightlyActive; }
        public void setMinutesLightlyActive(int minutesLightlyActive) { this.minutesLightlyActive = minutesLightlyActive; }
        
        public int getMinutesFairlyActive() { return minutesFairlyActive; }
        public void setMinutesFairlyActive(int minutesFairlyActive) { this.minutesFairlyActive = minutesFairlyActive; }
        
        public int getMinutesVeryActive() { return minutesVeryActive; }
        public void setMinutesVeryActive(int minutesVeryActive) { this.minutesVeryActive = minutesVeryActive; }
    }
    
    public static class FitbitSleepData {
        private int minutesAsleep;
        private int minutesToFallAsleep;
        private int minutesAwake;
        private int minutesAfterWakeup;
        private int sleepEfficiency;
        private int totalMinutesAsleep;
        private int totalSleepRecords;
        
        // Getters and Setters
        public int getMinutesAsleep() { return minutesAsleep; }
        public void setMinutesAsleep(int minutesAsleep) { this.minutesAsleep = minutesAsleep; }
        
        public int getMinutesToFallAsleep() { return minutesToFallAsleep; }
        public void setMinutesToFallAsleep(int minutesToFallAsleep) { this.minutesToFallAsleep = minutesToFallAsleep; }
        
        public int getMinutesAwake() { return minutesAwake; }
        public void setMinutesAwake(int minutesAwake) { this.minutesAwake = minutesAwake; }
        
        public int getMinutesAfterWakeup() { return minutesAfterWakeup; }
        public void setMinutesAfterWakeup(int minutesAfterWakeup) { this.minutesAfterWakeup = minutesAfterWakeup; }
        
        public int getSleepEfficiency() { return sleepEfficiency; }
        public void setSleepEfficiency(int sleepEfficiency) { this.sleepEfficiency = sleepEfficiency; }
        
        public int getTotalMinutesAsleep() { return totalMinutesAsleep; }
        public void setTotalMinutesAsleep(int totalMinutesAsleep) { this.totalMinutesAsleep = totalMinutesAsleep; }
        
        public int getTotalSleepRecords() { return totalSleepRecords; }
        public void setTotalSleepRecords(int totalSleepRecords) { this.totalSleepRecords = totalSleepRecords; }
    }
}
