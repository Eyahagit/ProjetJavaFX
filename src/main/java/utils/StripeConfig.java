package utils;

import com.stripe.Stripe;

public class StripeConfig {

    // ========== TES CLÉS STRIPE ICI ==========
    // Remplace par TES vraies clés que tu as récupérées du dashboard Stripe
    private static final String STRIPE_SECRET_KEY = "sk_test_51T4l4yDtVJGz9Rf826HKq5jPdCIFWPasYXF9wpg1L51cGZZAV1MEr6GIxMndWzD1nHm19RqEyBKO8uTKjRwg9Ipv00AeRZKGUB";
    private static final String STRIPE_PUBLIC_KEY = "pk_test_51T4l4yDtVJGz9Rf89ofO4JAtGVeYflqA40etcLq3EEPT0chE9w6Tkuw0cGTKATrvRW1I5weuWVOTLImRAftrGXN700HtEwOdHV";

    // Bloc static - s'exécute automatiquement au chargement de la classe
    static {
        Stripe.apiKey = STRIPE_SECRET_KEY;
        System.out.println("✅ Stripe configuré avec succès (mode test)");
        System.out.println("🔑 Clé publique: " + STRIPE_PUBLIC_KEY.substring(0, 15) + "...");
    }

    /**
     * Récupérer la clé publique (pour affichage si besoin)
     */
    public static String getPublicKey() {
        return STRIPE_PUBLIC_KEY;
    }

    /**
     * Vérifier que Stripe est configuré correctement
     */
    public static boolean isConfigured() {
        return Stripe.apiKey != null && Stripe.apiKey.startsWith("sk_test_");
    }
}