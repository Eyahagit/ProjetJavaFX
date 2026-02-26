package org.example.utils;

/**
 * Helper to display star ratings (1–5) as ★/☆ strings.
 */
public final class StarRatingHelper {

    private static final String FILLED = "★";
    private static final String EMPTY = "☆";
    private static final int MAX_STARS = 5;

    private StarRatingHelper() {}

    /**
     * Returns a string of 5 stars for the given note (1–5).
     * e.g. note 3 → "★★★☆☆"
     */
    public static String toStarString(int note) {
        if (note < 1) return EMPTY.repeat(MAX_STARS);
        if (note > MAX_STARS) note = MAX_STARS;
        return FILLED.repeat(note) + EMPTY.repeat(MAX_STARS - note);
    }

    /**
     * Returns star string for an average rating (rounded).
     * e.g. 3.6 → "★★★★☆"
     */
    public static String toStarStringFromAverage(double average) {
        return toStarString((int) Math.round(average));
    }

    public static String getFilledStar() { return FILLED; }
    public static String getEmptyStar() { return EMPTY; }
    public static int getMaxStars() { return MAX_STARS; }
}
