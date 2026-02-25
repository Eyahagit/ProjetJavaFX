package Service;

import Models.RendezVous;
import utils.StripeConfig;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;

public class StripePaymentService {

    public StripePaymentService() {
        // Vérifier que Stripe est bien configuré
        if (!StripeConfig.isConfigured()) {
            System.err.println("⚠️ Attention: Stripe n'est pas correctement configuré!");
            System.err.println("Vérifie tes clés API dans utils/StripeConfig.java");
        }
    }

    /**
     * Créer une session de paiement Stripe
     * @param rdv Le rendez-vous concerné
     * @param montant Le montant à payer en DT (sera converti en USD pour Stripe)
     * @return L'URL de paiement Stripe à ouvrir dans le navigateur
     */
    public String creerSessionPaiement(RendezVous rdv, double montant) {
        try {
            // Stripe utilise les centimes (montant × 100)
            Long montantCentimes = (long)(montant * 100);

            // Construire la session de paiement
            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl("https://growmind.tn/success?session_id={CHECKOUT_SESSION_ID}")
                    .setCancelUrl("https://growmind.tn/cancel")
                    .addLineItem(
                            SessionCreateParams.LineItem.builder()
                                    .setQuantity(1L)
                                    .setPriceData(
                                            SessionCreateParams.LineItem.PriceData.builder()
                                                    .setCurrency("usd")
                                                    .setUnitAmount(montantCentimes)
                                                    .setProductData(
                                                            SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                    .setName("Rendez-vous " + rdv.getTypeCons())
                                                                    .setDescription(
                                                                            "Patient: " + rdv.getNomCompletPatient() + "\n" +
                                                                                    "Date: " + rdv.getDateRdv() + " à " + rdv.getHeure() + "\n" +
                                                                                    "Psychologue: Dr. " + rdv.getNomPsychologue()
                                                                    )
                                                                    .build()
                                                    )
                                                    .build()
                                    )
                                    .build()
                    )
                    .putMetadata("rendezVousId", String.valueOf(rdv.getIdRdv()))
                    .putMetadata("patientNom", rdv.getNomCompletPatient())
                    .putMetadata("patientTel", rdv.getTelephonePatient())
                    .putMetadata("psychologue", rdv.getNomPsychologue() + " " + rdv.getPrenomPsychologue())
                    .build();

            // Créer la session Stripe
            Session session = Session.create(params);

            System.out.println("✅ Session Stripe créée: " + session.getId());
            System.out.println("💰 Montant: " + montant + " USD");
            System.out.println("🔗 URL: " + session.getUrl());
            System.out.println("👤 Patient: " + rdv.getNomCompletPatient());

            return session.getUrl(); // L'URL de paiement

        } catch (StripeException e) {
            System.err.println("❌ Erreur Stripe: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Vérifier le statut d'un paiement
     * @param sessionId L'ID de la session Stripe
     * @return true si le paiement est réussi, false sinon
     */
    public boolean verifierPaiement(String sessionId) {
        try {
            Session session = Session.retrieve(sessionId);
            String paymentStatus = session.getPaymentStatus();
            boolean estPaye = "paid".equals(paymentStatus);

            System.out.println("📊 Statut du paiement " + sessionId + ": " +
                    (estPaye ? "✅ RÉUSSI" : "⏳ " + paymentStatus));

            return estPaye;
        } catch (StripeException e) {
            System.err.println("❌ Erreur vérification: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Version simplifiée pour tester rapidement
     */
    public String testPaiement(RendezVous rdv) {
        return creerSessionPaiement(rdv, 50.0);
    }
}