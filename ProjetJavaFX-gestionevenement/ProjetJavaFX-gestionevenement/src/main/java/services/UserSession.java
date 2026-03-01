package services;

import Modele.Utilisateur;

public class UserSession {
    private static Utilisateur currentUser = null;
    // ✅ AJOUTEZ CETTE MÉTHODE
    public static int getId() {
        return currentUser != null ? currentUser.getId() : -1;
    }
    public static void setUser(Utilisateur user) {
        currentUser = user;
    }

    public static Utilisateur getUser() {
        return currentUser;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    public static void logout() {
        currentUser = null;
    }
}