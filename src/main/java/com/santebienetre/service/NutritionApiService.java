package com.santebienetre.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.santebienetre.config.ApiConfig;
import okhttp3.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Service for integrating with Nutrition APIs to get food information
 * Supports Open Food Facts API (free) as default
 */
public class NutritionApiService {
    
    private static final String OPEN_FOOD_FACTS_URL = "https://world.openfoodfacts.org/api/v2/search";
    private static final String USDA_API_URL = "https://api.nal.usda.gov/fdc/v1/foods/search";
    private static final String EDAMAM_URL = "https://api.edamam.com/api/nutrition-details";
    
    private final OkHttpClient client;
    private final Gson gson;
    
    // API Keys (chargées depuis la configuration)
    private String edamamAppId = ApiConfig.getEdamamAppId();
    private String edamamAppKey = ApiConfig.getEdamamAppKey();
    private String usdaApiKey = ApiConfig.getUsdaApiKey();
    
    public NutritionApiService() {
        this.client = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build();
        this.gson = new Gson();
        
        System.out.println("[NUTRITION] Service initialized with Edamam: " + 
            (edamamAppId.equals("VOTRE_EDAMAM_APP_ID") ? "Not configured" : "Configured"));
    }
    
    /**
     * Recherche des informations nutritionnelles pour un aliment
     * @param foodName nom de l'aliment (ex: "sandwich poulet")
     * @return objet NutritionInfo avec les informations
     */
    public NutritionInfo getFoodNutrition(String foodName) throws IOException {
        System.out.println("[NUTRITION] Searching for: " + foodName);
        
        // Essayer d'abord avec USDA (configuré)
        NutritionInfo result = searchUSDA(foodName);
        if (result != null) {
            System.out.println("[NUTRITION] USDA found result for: " + foodName);
            return result;
        }
        
        // Si USDA ne marche pas, créer des données mock basées sur l'aliment
        System.out.println("[NUTRITION] Using mock data for: " + foodName);
        return createMockNutritionInfo(foodName);
    }
    
    private NutritionInfo searchOpenFoodFacts(String foodName) throws IOException {
        String searchUrl = OPEN_FOOD_FACTS_URL + "?search_terms=" + 
            foodName.replace(" ", "+") + "&page_size=1&fields=product_name,nutriments,image_url";
        
        Request request = new Request.Builder()
            .url(searchUrl)
            .get()
            .build();
        
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                return null;
            }
            
            String responseBody = response.body().string();
            JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();
            
            if (json.has("products") && json.getAsJsonArray("products").size() > 0) {
                JsonObject product = json.getAsJsonArray("products").get(0).getAsJsonObject();
                return parseOpenFoodFactsProduct(product, foodName);
            }
        }
        
        return null;
    }
    
    private NutritionInfo searchUSDA(String foodName) throws IOException {
        if (usdaApiKey.equals("VOTRE_USDA_API_KEY")) {
            return null; // Pas de clé API configurée
        }
        
        String searchUrl = USDA_API_URL + "?query=" + 
            foodName.replace(" ", "+") + "&pageSize=1&api_key=" + usdaApiKey;
        
        Request request = new Request.Builder()
            .url(searchUrl)
            .get()
            .build();
        
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                return null;
            }
            
            String responseBody = response.body().string();
            JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();
            
            if (json.has("foods") && json.getAsJsonArray("foods").size() > 0) {
                JsonObject food = json.getAsJsonArray("foods").get(0).getAsJsonObject();
                return parseUSDAFood(food, foodName);
            }
        }
        
        return null;
    }
    
    private NutritionInfo parseUSDAFood(JsonObject food, String foodName) {
        NutritionInfo info = new NutritionInfo();
        info.setFoodName(foodName);
        info.setSource("USDA FoodData Central");
        
        if (food.has("description")) {
            info.setFoodName(food.get("description").getAsString());
        }
        
        if (food.has("foodNutrients")) {
            for (var nutrient : food.getAsJsonArray("foodNutrients")) {
                JsonObject nutr = nutrient.getAsJsonObject();
                String nutrientName = nutr.get("nutrientName").getAsString();
                double value = nutr.get("value").getAsDouble();
                
                switch (nutrientName) {
                    case "Energy":
                        info.setCalories((int) value);
                        break;
                    case "Protein":
                        info.setProteins(value);
                        break;
                    case "Carbohydrate, by difference":
                        info.setCarbs(value);
                        break;
                    case "Total lipid (fat)":
                        info.setFats(value);
                        break;
                    case "Fiber, total dietary":
                        info.setFiber(value);
                        break;
                    case "Sugars, total including NLEA":
                        info.setSugar(value);
                        break;
                    case "Sodium, Na":
                        info.setSodium(value);
                        break;
                }
            }
        }
        
        info.generateHealthAdvice();
        return info;
    }
    
    private NutritionInfo searchEdamam(String foodName) throws IOException {
        if (edamamAppId.equals("VOTRE_EDAMAM_APP_ID")) {
            return null; // Pas de clé API configurée
        }
        
        String url = EDAMAM_URL + "?app_id=" + edamamAppId + "&app_key=" + edamamAppKey;
        
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("ingr", foodName);
        
        RequestBody body = RequestBody.create(
            requestBody.toString(),
            MediaType.get("application/json; charset=utf-8")
        );
        
        Request request = new Request.Builder()
            .url(url)
            .post(body)
            .build();
        
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                return null;
            }
            
            String responseBody = response.body().string();
            JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();
            
            if (json.has("ingredients") && json.getAsJsonArray("ingredients").size() > 0) {
                JsonObject ingredient = json.getAsJsonArray("ingredients").get(0).getAsJsonObject();
                return parseEdamamIngredient(ingredient, foodName);
            }
        }
        
        return null;
    }
    
    private NutritionInfo parseOpenFoodFactsProduct(JsonObject product, String foodName) {
        NutritionInfo info = new NutritionInfo();
        info.setFoodName(foodName);
        info.setSource("Open Food Facts");
        
        if (product.has("image_url")) {
            info.setImageUrl(product.get("image_url").getAsString());
        }
        
        JsonObject nutriments = product.getAsJsonObject("nutriments");
        if (nutriments != null) {
            info.setCalories(getSafeInt(nutriments, "energy-kcal_100g"));
            info.setProteins(getSafeDouble(nutriments, "proteins_100g"));
            info.setCarbs(getSafeDouble(nutriments, "carbohydrates_100g"));
            info.setFats(getSafeDouble(nutriments, "fat_100g"));
            info.setFiber(getSafeDouble(nutriments, "fiber_100g"));
            info.setSugar(getSafeDouble(nutriments, "sugars_100g"));
            info.setSodium(getSafeDouble(nutriments, "sodium_100g"));
        }
        
        info.generateHealthAdvice();
        return info;
    }
    
    private NutritionInfo parseEdamamIngredient(JsonObject ingredient, String foodName) {
        NutritionInfo info = new NutritionInfo();
        info.setFoodName(foodName);
        info.setSource("Edamam");
        
        if (ingredient.has("parsed")) {
            JsonObject parsed = ingredient.getAsJsonArray("parsed").get(0).getAsJsonObject();
            info.setFoodName(parsed.get("food").getAsString());
        }
        
        if (ingredient.has("nutrients")) {
            JsonObject nutrients = ingredient.getAsJsonObject("nutrients");
            info.setCalories(getSafeInt(nutrients, "ENERC_KCAL"));
            info.setProteins(getSafeDouble(nutrients, "PROCNT"));
            info.setCarbs(getSafeDouble(nutrients, "CHOCDF"));
            info.setFats(getSafeDouble(nutrients, "FAT"));
            info.setFiber(getSafeDouble(nutrients, "FIBTG"));
            info.setSugar(getSafeDouble(nutrients, "SUGAR"));
            info.setSodium(getSafeDouble(nutrients, "NA"));
        }
        
        info.generateHealthAdvice();
        return info;
    }
    
    private NutritionInfo createDefaultNutritionInfo(String foodName) {
        NutritionInfo info = new NutritionInfo();
        info.setFoodName(foodName);
        info.setSource("Estimation");
        info.setCalories(250); // Valeur par défaut
        info.setProteins(15.0);
        info.setCarbs(30.0);
        info.setFats(8.0);
        info.generateHealthAdvice();
        return info;
    }
    
    private int getSafeInt(JsonObject obj, String key) {
        if (obj.has(key) && !obj.get(key).isJsonNull()) {
            return obj.get(key).getAsInt();
        }
        return 0;
    }
    
    private double getSafeDouble(JsonObject obj, String key) {
        if (obj.has(key) && !obj.get(key).isJsonNull()) {
            return obj.get(key).getAsDouble();
        }
        return 0.0;
    }
    
    /**
     * Configure les clés API pour Edamam
     */
    public void setEdamamCredentials(String appId, String appKey) {
        this.edamamAppId = appId;
        this.edamamAppKey = appKey;
    }
    
    /**
     * Classe interne pour stocker les informations nutritionnelles
     */
    public static class NutritionInfo {
        private String foodName;
        private String source;
        private String imageUrl;
        private int calories;
        private double proteins;
        private double carbs;
        private double fats;
        private double fiber;
        private double sugar;
        private double sodium;
        private String healthAdvice;
        
        // Getters and Setters
        public String getFoodName() { return foodName; }
        public void setFoodName(String foodName) { this.foodName = foodName; }
        
        public String getSource() { return source; }
        public void setSource(String source) { this.source = source; }
        
        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
        
        public int getCalories() { return calories; }
        public void setCalories(int calories) { this.calories = calories; }
        
        public double getProteins() { return proteins; }
        public void setProteins(double proteins) { this.proteins = proteins; }
        
        public double getCarbs() { return carbs; }
        public void setCarbs(double carbs) { this.carbs = carbs; }
        
        public double getFats() { return fats; }
        public void setFats(double fats) { this.fats = fats; }
        
        public double getFiber() { return fiber; }
        public void setFiber(double fiber) { this.fiber = fiber; }
        
        public double getSugar() { return sugar; }
        public void setSugar(double sugar) { this.sugar = sugar; }
        
        public double getSodium() { return sodium; }
        public void setSodium(double sodium) { this.sodium = sodium; }
        
        public String getHealthAdvice() { return healthAdvice; }
        public void setHealthAdvice(String healthAdvice) { this.healthAdvice = healthAdvice; }
        
        /**
         * Génère des conseils santé basés sur les valeurs nutritionnelles
         */
        public void generateHealthAdvice() {
            StringBuilder advice = new StringBuilder();
            
            if (calories > 500) {
                advice.append("⚠️ Plat riche en calories. Consommez avec modération.\n");
            } else if (calories < 200) {
                advice.append("✅ Plat léger, bon pour un contrôle calorique.\n");
            }
            
            if (proteins > 20) {
                advice.append("💪 Riche en protéines, excellent pour les muscles.\n");
            }
            
            if (fiber > 5) {
                advice.append("🌾 Bon apport en fibres, favorable à la digestion.\n");
            }
            
            if (sugar > 15) {
                advice.append("🍬 Teneur élevée en sucres, limitez la consommation.\n");
            }
            
            if (sodium > 600) {
                advice.append("🧂 Salé, privilégiez les aliments moins salés.\n");
            }
            
            if (advice.length() == 0) {
                advice.append("✨ Équilibre nutritionnel correct !");
            }
            
            this.healthAdvice = advice.toString();
        }
        
        @Override
        public String toString() {
            return String.format("%s (%d cal) - %s", foodName, calories, source);
        }
    }
    
    /**
     * Crée des données nutritionnelles mock basées sur le nom de l'aliment
     */
    private NutritionInfo createMockNutritionInfo(String foodName) {
        String lowerName = foodName.toLowerCase();
        
        NutritionInfo info = new NutritionInfo();
        
        // Base nutritionnelle par défaut
        int calories = 100;
        double proteins = 5.0;
        double carbs = 15.0;
        double fats = 3.0;
        double fiber = 2.0;
        double sugar = 5.0;
        double sodium = 100.0;
        String healthAdvice = "";
        
        // Fruits
        if (lowerName.contains("apple") || lowerName.contains("pomme")) {
            calories = 95; proteins = 0.5; carbs = 25.0; fats = 0.3;
            fiber = 4.4; sugar = 19.0; sodium = 1.0;
            healthAdvice = "🍎 Excellente source de fibres et antioxydants. Pauvre en calories, idéale pour la perte de poids.";
        } else if (lowerName.contains("banana") || lowerName.contains("banane")) {
            calories = 105; proteins = 1.3; carbs = 27.0; fats = 0.4;
            fiber = 3.1; sugar = 14.0; sodium = 1.0;
            healthAdvice = "🍌 Riche en potassium et vitamines B. Énergie durable, parfaite avant le sport.";
        } else if (lowerName.contains("orange") || lowerName.contains("orange")) {
            calories = 62; proteins = 1.2; carbs = 15.0; fats = 0.2;
            fiber = 3.1; sugar = 12.0; sodium = 0.0;
            healthAdvice = "🍊 Exceptionnelle source de vitamine C. Renforce le système immunitaire.";
        } else if (lowerName.contains("strawberry") || lowerName.contains("fraise")) {
            calories = 32; proteins = 0.7; carbs = 8.0; fats = 0.3;
            fiber = 2.0; sugar = 4.9; sodium = 1.0;
            healthAdvice = "🍓 Faible en calories, riche en vitamine C et antioxydants. Anti-inflammatoire.";
        }
        
        // Protéines
        else if (lowerName.contains("chicken") || lowerName.contains("poulet")) {
            calories = 165; proteins = 31.0; carbs = 0.0; fats = 3.6;
            fiber = 0.0; sugar = 0.0; sodium = 74.0;
            healthAdvice = "🍗 Excellente source de protéines maigres. Essentiel pour la masse musculaire.";
        } else if (lowerName.contains("beef") || lowerName.contains("bœuf") || lowerName.contains("boeuf")) {
            calories = 250; proteins = 26.0; carbs = 0.0; fats = 15.0;
            fiber = 0.0; sugar = 0.0; sodium = 60.0;
            healthAdvice = "🥩 Riche en fer et vitamine B12. Énergie durable, consommer avec modération.";
        } else if (lowerName.contains("fish") || lowerName.contains("poisson") || lowerName.contains("salmon") || lowerName.contains("saumon")) {
            calories = 208; proteins = 25.0; carbs = 0.0; fats = 12.0;
            fiber = 0.0; sugar = 0.0; sodium = 59.0;
            healthAdvice = "🐟 Riche en oméga-3 et protéines. Excellent pour le cerveau et le cœur.";
        } else if (lowerName.contains("egg") || lowerName.contains("œuf")) {
            calories = 155; proteins = 13.0; carbs = 1.1; fats = 11.0;
            fiber = 0.0; sugar = 1.1; sodium = 124.0;
            healthAdvice = "🥚 Protéines complètes de haute qualité. Riche en vitamines D et B12.";
        }
        
        // Légumes
        else if (lowerName.contains("broccoli") || lowerName.contains("brocoli")) {
            calories = 34; proteins = 2.8; carbs = 7.0; fats = 0.4;
            fiber = 2.6; sugar = 1.5; sodium = 33.0;
            healthAdvice = "🥦 Super-aliment riche en vitamines K et C. Détoxifiant et anticancéreux.";
        } else if (lowerName.contains("carrot") || lowerName.contains("carotte")) {
            calories = 41; proteins = 0.9; carbs = 10.0; fats = 0.2;
            fiber = 2.8; sugar = 4.7; sodium = 69.0;
            healthAdvice = "🥕 Excellente source de bêta-carotène. Bon pour la vision et la peau.";
        } else if (lowerName.contains("tomato") || lowerName.contains("tomate")) {
            calories = 18; proteins = 0.9; carbs = 3.9; fats = 0.2;
            fiber = 1.2; sugar = 2.6; sodium = 5.0;
            healthAdvice = "🍅 Riche en lycopène. Protège contre les maladies cardiaques.";
        }
        
        // Céréales et féculents
        else if (lowerName.contains("rice") || lowerName.contains("riz")) {
            calories = 130; proteins = 2.7; carbs = 28.0; fats = 0.3;
            fiber = 0.4; sugar = 0.1; sodium = 1.0;
            healthAdvice = "🍚 Source d'énergie rapide. Privilégier le riz complet pour plus de fibres.";
        } else if (lowerName.contains("bread") || lowerName.contains("pain")) {
            calories = 265; proteins = 9.0; carbs = 49.0; fats = 3.2;
            fiber = 3.6; sugar = 5.0; sodium = 491.0;
            healthAdvice = "🍞 Source d'énergie. Choisir le pain complet pour un index glycémique plus bas.";
        } else if (lowerName.contains("pasta") || lowerName.contains("pâtes")) {
            calories = 131; proteins = 5.0; carbs = 25.0; fats = 1.1;
            fiber = 1.8; sugar = 0.6; sodium = 6.0;
            healthAdvice = "🍝 Énergie durable. Pâtes complètes recommandées pour plus de nutriments.";
        } else if (lowerName.contains("potato") || lowerName.contains("pomme de terre") || lowerName.contains("patate")) {
            calories = 77; proteins = 2.0; carbs = 17.0; fats = 0.1;
            fiber = 2.2; sugar = 0.8; sodium = 6.0;
            healthAdvice = "🥔 Riche en potassium. Consommer avec la peau pour plus de fibres.";
        }
        
        // Produits laitiers
        else if (lowerName.contains("milk") || lowerName.contains("lait")) {
            calories = 42; proteins = 3.4; carbs = 5.0; fats = 1.0;
            fiber = 0.0; sugar = 5.0; sodium = 44.0;
            healthAdvice = "🥛 Excellente source de calcium. Essentiel pour les os et les dents.";
        } else if (lowerName.contains("cheese") || lowerName.contains("fromage")) {
            calories = 402; proteins = 25.0; carbs = 1.3; fats = 33.0;
            fiber = 0.0; sugar = 0.5; sodium = 621.0;
            healthAdvice = "🧀 Riche en calcium et protéines. Élevé en graisses et sodium, consommer modérément.";
        } else if (lowerName.contains("yogurt") || lowerName.contains("yaourt")) {
            calories = 59; proteins = 10.0; carbs = 3.6; fats = 0.4;
            fiber = 0.0; sugar = 3.6; sodium = 36.0;
            healthAdvice = "🥤 Probiotiques pour la digestion. Protéines de haute qualité.";
        }
        
        // Noix et graines
        else if (lowerName.contains("almond") || lowerName.contains("amande")) {
            calories = 579; proteins = 21.0; carbs = 22.0; fats = 50.0;
            fiber = 12.5; sugar = 4.0; sodium = 1.0;
            healthAdvice = "🌰 Riche en graisses saines et vitamine E. Bon pour le cœur et le cerveau.";
        } else if (lowerName.contains("walnut") || lowerName.contains("noix")) {
            calories = 654; proteins = 15.0; carbs = 14.0; fats = 65.0;
            fiber = 6.7; sugar = 2.6; sodium = 2.0;
            healthAdvice = "🥰 Exceptionnelles source d'oméga-3. Anti-inflammatoires.";
        }
        
        // Boissons
        else if (lowerName.contains("coffee") || lowerName.contains("café")) {
            calories = 2; proteins = 0.1; carbs = 0.0; fats = 0.0;
            fiber = 0.0; sugar = 0.0; sodium = 5.0;
            healthAdvice = "☕ Stimulant doux. Améliore la concentration et le métabolisme.";
        } else if (lowerName.contains("tea") || lowerName.contains("thé")) {
            calories = 2; proteins = 0.0; carbs = 0.0; fats = 0.0;
            fiber = 0.0; sugar = 0.0; sodium = 0.0;
            healthAdvice = "🍵 Riche en antioxydants. Apaisant et bon pour la santé cardiaque.";
        }
        
        // Valeurs par défaut si aucun aliment trouvé
        else {
            healthAdvice = "🔍 Aliment non répertorié. Valeurs nutritionnelles estimées basées sur 100g.";
        }
        
        // Configuration de l'objet NutritionInfo
        info.setFoodName(foodName);
        info.setSource("Smart Mock Data v2.0");
        info.setCalories(calories);
        info.setProteins(proteins);
        info.setCarbs(carbs);
        info.setFats(fats);
        info.setFiber(fiber);
        info.setSugar(sugar);
        info.setSodium(sodium);
        info.setHealthAdvice(healthAdvice);
        
        System.out.println("[NUTRITION] Generated smart mock data for: " + foodName + " (" + calories + " cal)");
        
        return info;
    }
}
