package utils;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import com.twilio.exception.ApiException;

public class TwilioUtil {

    // ✅ COORDONNÉES CORRECTES
    public static final String ACCOUNT_SID = "ACdef405787678a6091868d4011b0c76cc";
    public static final String AUTH_TOKEN = "7b88991aa906ff577fe23d1a55cf006d";
    public static final String NUMERO_TWILIO = "+15026102896";
    public static final String NUMERO_ALERTE = "+21652551135";

    static {
        try {
            Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
            System.out.println("✅ Twilio initialisé avec succès !");
            System.out.println("   Account SID: " + ACCOUNT_SID);
            System.out.println("   Numéro Twilio: " + NUMERO_TWILIO);
        } catch (Exception e) {
            System.err.println("❌ Erreur initialisation Twilio: " + e.getMessage());
        }
    }

    public static void envoyerAlerteSMS(String nom, String message, String localisation) {
        try {
            String texte = "🆘 URGENCE GrowMind\n\n";
            texte += "👤 Personne: " + nom + "\n";
            texte += "📝 Message: " + message + "\n";
            if (localisation != null && !localisation.isEmpty()) {
                texte += "📍 Localisation: " + localisation;
            }

            System.out.println("📤 Envoi SMS vers " + NUMERO_ALERTE + "...");

            Message sms = Message.creator(
                    new PhoneNumber(NUMERO_ALERTE),
                    new PhoneNumber(NUMERO_TWILIO),
                    texte
            ).create();

            System.out.println("✅ SMS envoyé ! SID: " + sms.getSid());

        } catch (ApiException e) {
            System.err.println("❌ Erreur Twilio: " + e.getMessage());
            System.err.println("   Code: " + e.getCode());
            throw e;
        }
    }
}