package utils;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import java.io.InputStream;
import java.util.Properties;

public class TwilioUtil {

    public static String ACCOUNT_SID;
    public static String AUTH_TOKEN;
    public static String NUMERO_TWILIO;
    public static String NUMERO_ALERTE;

    static {
        loadConfig();
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
        System.out.println("✅ Twilio initialisé");
    }

    private static void loadConfig() {
        try (InputStream input = TwilioUtil.class.getClassLoader().getResourceAsStream("config.properties")) {
            Properties prop = new Properties();
            prop.load(input);

            ACCOUNT_SID = prop.getProperty("twilio.account.sid");
            AUTH_TOKEN = prop.getProperty("twilio.auth.token");
            NUMERO_TWILIO = prop.getProperty("twilio.phone.number");
            NUMERO_ALERTE = prop.getProperty("twilio.alert.number");

            System.out.println("📖 Configuration chargée");

        } catch (Exception e) {
            System.err.println("❌ Erreur chargement config: " + e.getMessage());
            // Valeurs par défaut pour le développement
            ACCOUNT_SID = "ACdef405787678a6091868d4011b0c76cc";
            AUTH_TOKEN = "e5b768670b77716a5860eba4ea09e584";
            NUMERO_TWILIO = "+15026102896";
            NUMERO_ALERTE = "+21652551135";
        }
    }

    public static void envoyerAlerteSMS(String nom, String message, String localisation) {
        String texte = "🆘 URGENCE GrowMind\n\n";
        texte += "👤 Personne: " + nom + "\n";
        texte += "📝 Message: " + message + "\n";

        if (localisation != null && !localisation.isEmpty() && !localisation.equals("Localisation non disponible")) {
            texte += "📍 Localisation: " + localisation;
        } else {
            texte += "📍 Localisation: Non partagée";
        }

        System.out.println("📤 Envoi SMS...");

        Message sms = Message.creator(
                new PhoneNumber(NUMERO_ALERTE),
                new PhoneNumber(NUMERO_TWILIO),
                texte
        ).create();

        System.out.println("✅ SMS envoyé ! SID: " + sms.getSid());
    }
}