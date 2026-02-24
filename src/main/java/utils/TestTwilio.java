package utils;

public class TestTwilio {
    public static void main(String[] args) {
        System.out.println("=== TEST TWILIO ===");
        System.out.println("Account SID: " + TwilioUtil.ACCOUNT_SID);
        System.out.println("Auth Token: " + TwilioUtil.AUTH_TOKEN.substring(0, 5) + "...");
        System.out.println("Numéro Twilio: " + TwilioUtil.NUMERO_TWILIO);
        System.out.println("Destinataire: " + TwilioUtil.NUMERO_ALERTE);
        System.out.println();

        try {
            TwilioUtil.envoyerAlerteSMS("Test", "Message de test", "Localisation test");
            System.out.println("✅ SMS envoyé avec succès !");
        } catch (Exception e) {
            System.err.println("❌ Échec: " + e.getMessage());
        }
    }
}