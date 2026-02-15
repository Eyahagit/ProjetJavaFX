package org.example.utils;

import org.example.Models.User;

/**
 * Holds the current static user used for the session (e.g. evaluations, favoris).
 * Set once at startup or login and used by services that need a user context.
 */
public final class StaticUser {

    private static User currentUser;

    private StaticUser() {}

    public static User get() {
        return currentUser;
    }

    public static void set(User user) {
        currentUser = user;
    }

    public static int getId() {
        return currentUser != null ? currentUser.getId() : 0;
    }

    public static boolean isSet() {
        return currentUser != null;
    }
}
