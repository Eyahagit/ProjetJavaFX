package services;

import java.time.LocalDate;

public class DateAPI {
    public enum StatutEvenement { PASSE, AUJOURDHUI, FUTUR }
    public static StatutEvenement getStatut(LocalDate d) {
        LocalDate now = LocalDate.now();
        if (d.isBefore(now)) return StatutEvenement.PASSE;
        if (d.isEqual(now)) return StatutEvenement.AUJOURDHUI;
        return StatutEvenement.FUTUR;
    }
    public static String getCouleurPourStatut(StatutEvenement s) {
        return s == StatutEvenement.PASSE ? "#e74c3c" : s == StatutEvenement.AUJOURDHUI ? "#27ae60" : "#1565c0";
    }
    public static String getEmojiPourStatut(StatutEvenement s) {
        return s == StatutEvenement.PASSE ? "⌛" : s == StatutEvenement.AUJOURDHUI ? "🔴 LIVE" : "📅";
    }
}