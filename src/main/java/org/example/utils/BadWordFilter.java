package org.example.utils;

import javafx.geometry.Pos;
import org.controlsfx.control.Notifications;

public class BadWordFilter {

    private static final String[] BAD_WORDS = { "bad1", "bad2", "bad3" };
    private static int offenseCount = 0;

    public static boolean containsBadWord(String text) {
        if (text == null || text.isBlank())
            return false;
        String lower = text.toLowerCase();
        for (String bad : BAD_WORDS) {
            if (lower.contains(bad.toLowerCase()))
                return true;
        }
        return false;
    }

    public static void showBadWordNotifications() {
        offenseCount++;
        // Geometric progression: 3, 6, 12, 24...
        // Formula: 3 * 2^(offenseCount - 1)
        int countToShow = 3 * (int) Math.pow(2, offenseCount - 1);

        String title = "⚠️ Alerte Sécurité - Tentative #" + offenseCount;
        String message = "Contenu inapproprié détecté ! " + countToShow + " avertissements envoyés.";

        for (int i = 0; i < countToShow; i++) {
            Notifications.create()
                    .title(title)
                    .text(message + " (Avertissement " + (i + 1) + "/" + countToShow + ")")
                    .position(Pos.TOP_RIGHT)
                    .showWarning();
        }

        if (offenseCount >= 3) {
            Notifications.create()
                    .title("❌ BANISSEMENT IMMINENT")
                    .text("Ceci est votre " + offenseCount + "ème récidive. L'administrateur a été notifié pour ban.")
                    .position(Pos.CENTER)
                    .showError();
        }
    }
}
