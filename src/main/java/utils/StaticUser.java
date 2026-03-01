package utils;

/**
 * Holds the current static user used for the session (e.g. evaluations,
 * favoris).
 * Set once at startup or login and used by services that need a user context.
 */
public final class StaticUser {

    private static int currentUserId = -1; // -1 means unset

    private StaticUser() {
    }

    public static int getId() {
        return currentUserId;
    }

    public static void setId(int id) {
        currentUserId = id;
    }

    public static boolean isSet() {
        return currentUserId != -1;
    }
}
