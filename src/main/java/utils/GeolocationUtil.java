package utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class GeolocationUtil {

    /**
     * Récupère la localisation approximative par IP
     */
    public static String getLocalisationParIP() {
        try {
            // Utiliser une API gratuite de géolocalisation
            URL url = new URL("http://ip-api.com/json/?fields=city,regionName,country");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            // Parser le JSON (simplifié)
            String reponse = response.toString();
            String ville = extraireValeur(reponse, "city");
            String region = extraireValeur(reponse, "regionName");
            String pays = extraireValeur(reponse, "country");

            if (ville != null && !ville.isEmpty() && !ville.equals("null") && !ville.equals("")) {
                if (region != null && !region.isEmpty() && !region.equals("null")) {
                    return ville + ", " + region + ", " + pays;
                } else {
                    return ville + ", " + pays;
                }
            }

        } catch (Exception e) {
            System.err.println("❌ Impossible d'obtenir la localisation: " + e.getMessage());
        }

        return "Localisation non disponible";
    }

    private static String extraireValeur(String json, String cle) {
        try {
            String recherche = "\"" + cle + "\":\"";
            int debut = json.indexOf(recherche);
            if (debut > 0) {
                debut += recherche.length();
                int fin = json.indexOf("\"", debut);
                if (fin > debut) {
                    return json.substring(debut, fin);
                }
            }
        } catch (Exception e) {
            // Ignorer
        }
        return "";
    }
}