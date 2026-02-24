package utils;

public class PhoneFormatter {

    /**
     * Formate un numéro au format international (+216XXXXXXXX)
     */
    public static String formatToInternational(String phone) {
        if (phone == null || phone.isEmpty()) return null;

        // Enlever tous les caractères non numériques
        String cleaned = phone.replaceAll("[^0-9]", "");

        // Si le numéro commence par 00, remplacer par +
        if (cleaned.startsWith("00")) {
            return "+" + cleaned.substring(2);
        }

        // Si le numéro commence par 0, remplacer par +216
        if (cleaned.startsWith("0")) {
            return "+216" + cleaned.substring(1);
        }

        // Si le numéro a 8 chiffres (tunisien sans indicatif)
        if (cleaned.length() == 8) {
            return "+216" + cleaned;
        }

        // Si c'est déjà au format international avec +
        if (phone.startsWith("+")) {
            return phone;
        }

        // Par défaut, ajouter +216
        return "+216" + cleaned;
    }

    /**
     * Valide si le numéro est au bon format international
     */
    public static boolean isValidPhone(String phone) {
        if (phone == null) return false;
        // Format: +216 suivi de 8 chiffres
        String regex = "^\\+216[0-9]{8}$";
        return phone.matches(regex);
    }

    /**
     * Masque le numéro pour l'affichage (ex: +216*****123)
     */
    public static String maskPhone(String phone) {
        if (phone == null || phone.length() < 8) return phone;

        String countryCode = phone.substring(0, 4); // +216
        String lastDigits = phone.substring(phone.length() - 3);

        return countryCode + "*****" + lastDigits;
    }
}