package utils;

public class ConfigLoader {

    // ===== VOS IDENTIFIANTS TWILIO =====
    // REMPLACEZ CES VALEURS AVEC LES VÔTRES
    private static final String TWILIO_ACCOUNT_SID = "***";
    private static final String TWILIO_AUTH_TOKEN = "***";
    private static final String TWILIO_VERIFY_SERVICE_SID = "***";
    private static final String TWILIO_PHONE_NUMBER = "***";

    private static final String ALERT_PHONE_NUMBER = "***";

    // Configuration application
    private static final String APP_NAME = "GrowMind";
    private static final String DEFAULT_COUNTRY_CODE = "+216";
    private static final int SMS_TIMEOUT = 300;

    /**
     * Récupère l'Account SID Twilio
     */
    public static String getAccountSid() {
        return TWILIO_ACCOUNT_SID;
    }

    /**
     * Récupère l'Auth Token Twilio
     */
    public static String getAuthToken() {
        return TWILIO_AUTH_TOKEN;
    }

    /**
     * Récupère le Service SID Twilio Verify
     */
    public static String getVerifyServiceSid() {
        return TWILIO_VERIFY_SERVICE_SID;
    }

    /**
     * Récupère le numéro de téléphone Twilio
     */
    public static String getTwilioPhoneNumber() {
        return TWILIO_PHONE_NUMBER;
    }

    /**
     * Récupère le nom de l'application
     */
    public static String getAppName() {
        return APP_NAME;
    }

    /**
     * Récupère le code pays par défaut
     */
    public static String getDefaultCountryCode() {
        return DEFAULT_COUNTRY_CODE;
    }

    /**
     * Récupère le timeout SMS (en secondes)
     */
    public static int getSmsTimeout() {
        return SMS_TIMEOUT;
    }

    /**
     * Vérifie si la configuration Twilio est complète
     */
    public static boolean isTwilioConfigured() {
        return !TWILIO_ACCOUNT_SID.startsWith("ACxxxx") &&
                !TWILIO_AUTH_TOKEN.startsWith("xxxx") &&
                !TWILIO_VERIFY_SERVICE_SID.startsWith("VAxxxx");
    }

    /**
     * Affiche la configuration (pour debug)
     */
    public static void printConfig() {
        System.out.println("\n=== Configuration Twilio ===");
        System.out.println("Account SID: " + maskString(TWILIO_ACCOUNT_SID));
        System.out.println("Auth Token: " + maskString(TWILIO_AUTH_TOKEN));
        System.out.println("Verify SID: " + maskString(TWILIO_VERIFY_SERVICE_SID));
        System.out.println("Phone: " + TWILIO_PHONE_NUMBER);
        System.out.println("App: " + APP_NAME);
        System.out.println("Country Code: " + DEFAULT_COUNTRY_CODE);
        System.out.println("Timeout: " + SMS_TIMEOUT + "s");
        System.out.println("Configuré: " + (isTwilioConfigured() ? "✅" : "❌"));
        System.out.println("============================\n");
    }

    /**
     * Masque une chaîne pour l'affichage (sécurité)
     */
    public static String maskString(String str) {
        if (str == null || str.length() < 8) return "****";
        return str.substring(0, 4) + "****" + str.substring(str.length() - 4);
    }
}