package Services;

import com.twilio.Twilio;
import com.twilio.rest.verify.v2.service.Verification;
import com.twilio.rest.verify.v2.service.VerificationCheck;
import utils.ConfigLoader;

public class TwilioVerifyService {

    private static TwilioVerifyService instance;
    private final String accountSid;
    private final String authToken;
    private final String verifyServiceSid;
    private boolean initialized = false;

    private TwilioVerifyService() {
        // CORRECTION: Utiliser les méthodes getters de ConfigLoader
        this.accountSid = ConfigLoader.getAccountSid();              // ✅ CORRECT
        this.authToken = ConfigLoader.getAuthToken();                // ✅ CORRECT
        this.verifyServiceSid = ConfigLoader.getVerifyServiceSid();  // ✅ CORRECT

        // Initialiser Twilio si les identifiants sont présents
        if (accountSid != null && authToken != null &&
                !accountSid.isEmpty() && !authToken.isEmpty() &&
                !accountSid.startsWith("ACxxxx") && !authToken.startsWith("xxxx")) {

            try {
                Twilio.init(accountSid, authToken);
                initialized = true;
                System.out.println("✅ Twilio Verify Service initialisé avec succès");
                System.out.println("   Account SID: " + ConfigLoader.maskString(accountSid));
                System.out.println("   Verify SID: " + ConfigLoader.maskString(verifyServiceSid));
            } catch (Exception e) {
                System.err.println("❌ Erreur lors de l'initialisation de Twilio: " + e.getMessage());
            }
        } else {
            System.err.println("⚠️ Attention: Identifiants Twilio manquants ou invalides");
            System.err.println("   Account SID: " + (accountSid != null ? "présent" : "manquant"));
            System.err.println("   Auth Token: " + (authToken != null ? "présent" : "manquant"));
            System.err.println("   Verify SID: " + (verifyServiceSid != null ? "présent" : "manquant"));
        }
    }

    public static TwilioVerifyService getInstance() {
        if (instance == null) {
            instance = new TwilioVerifyService();
        }
        return instance;
    }

    /**
     * Envoie un code de vérification par SMS
     * @param phoneNumber Numéro de téléphone au format international (+216XXXXXXXX)
     * @return true si le SMS a été envoyé avec succès
     */
    public boolean sendVerificationCode(String phoneNumber) {
        if (!initialized) {
            System.err.println("❌ Twilio non initialisé - Vérifiez votre configuration");
            return false;
        }

        if (verifyServiceSid == null || verifyServiceSid.isEmpty() || verifyServiceSid.startsWith("VAxxxx")) {
            System.err.println("❌ Verify Service SID manquant ou invalide");
            return false;
        }

        try {
            System.out.println("📤 Envoi du code à " + phoneNumber + "...");

            Verification verification = Verification.creator(
                    verifyServiceSid,   // Service SID
                    phoneNumber,        // Numéro de téléphone
                    "sms"               // Canal (sms)
            ).create();

            System.out.println("✅ Code envoyé avec succès - Status: " + verification.getStatus());
            System.out.println("📱 SID: " + verification.getSid());
            return true;

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'envoi du SMS: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Vérifie le code saisi par l'utilisateur
     * @param phoneNumber Numéro de téléphone
     * @param code Code à 6 chiffres
     * @return true si le code est valide
     */
    public boolean verifyCode(String phoneNumber, String code) {
        if (!initialized) {
            System.err.println("❌ Twilio non initialisé");
            return false;
        }

        try {
            System.out.println("🔐 Vérification du code pour " + phoneNumber + "...");

            VerificationCheck verificationCheck = VerificationCheck.creator(
                            verifyServiceSid    // Service SID
                    ).setTo(phoneNumber)        // Numéro de téléphone
                    .setCode(code)             // Code à vérifier
                    .create();

            String status = verificationCheck.getStatus();
            boolean approved = "approved".equals(status);

            if (approved) {
                System.out.println("✅ Code vérifié avec succès !");
            } else {
                System.out.println("❌ Code invalide - Status: " + status);
            }

            return approved;

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la vérification: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Vérifie si le service est initialisé
     */
    public boolean isInitialized() {
        return initialized;
    }

    /**
     * Teste la connexion à Twilio
     */
    public boolean testConnection() {
        if (!initialized) {
            System.err.println("❌ Service non initialisé");
            return false;
        }

        try {
            System.out.println("🔍 Test de connexion à Twilio...");

            // Test avec un numéro de test
            Verification verification = Verification.creator(
                    verifyServiceSid,
                    "+15017122661", // Numéro de test Twilio
                    "sms"
            ).create();

            System.out.println("✅ Connexion à Twilio réussie !");
            return true;

        } catch (Exception e) {
            System.err.println("❌ Échec de la connexion à Twilio: " + e.getMessage());
            return false;
        }
    }

    /**
     * Obtient le SID du service
     */
    public String getVerifyServiceSid() {
        return verifyServiceSid;
    }
}