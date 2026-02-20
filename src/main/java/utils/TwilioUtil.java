package utils;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

public class TwilioUtil {

    // TES INFOS CORRECTES
    public static final String ACCOUNT_SID = "ACdef405787678a6091868d4011b0c76cc";
    public static final String AUTH_TOKEN = "e5b768670b77716a5860eba4ea09e584";
    public static final String NUMERO_TWILIO = "+15026102896";  // ✅ TON VRAI NUMÉRO
    public static final String NUMERO_ALERTE = "+21652551135";

    static {
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
        System.out.println("✅ Twilio initialisé");
        System.out.println("📞 Numéro Twilio: " + NUMERO_TWILIO);
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

        System.out.println("📤 Envoi SMS: " + texte);

        Message sms = Message.creator(
                new PhoneNumber(NUMERO_ALERTE),
                new PhoneNumber(NUMERO_TWILIO),
                texte
        ).create();

        System.out.println("✅ SMS envoyé ! SID: " + sms.getSid());
    }
}