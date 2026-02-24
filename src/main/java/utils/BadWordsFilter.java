package utils;

import java.util.Arrays;
import java.util.List;

public class BadWordsFilter {

    private static final List<String> BAD_WORDS = Arrays.asList(
            "merde", "putain", "con", "connard", "salope", "enculé",
            "fuck", "shit", "asshole", "bitch", "damn", "saleté",
            "niquer", "baiser", "enfoiré", "batard", "salopard", "ordure"
    );

    private static final String REPLACEMENT = "***";

    public static String filter(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        String filtered = text;
        for (String badWord : BAD_WORDS) {
            filtered = filtered.replaceAll("(?i)" + badWord, REPLACEMENT);
        }
        return filtered;
    }

    public static boolean containsBadWords(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }

        String lowerText = text.toLowerCase();
        for (String badWord : BAD_WORDS) {
            if (lowerText.contains(badWord.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
}