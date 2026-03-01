package utils;

import Models.users;

public class SessionManager {

    private static SessionManager instance;
    private users currentUser;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void setCurrentUser(users user) {
        this.currentUser = user;
        System.out.println("📌 Session: Utilisateur connecté - " + (user != null ? user.getName() : "null"));
    }

    public users getCurrentUser() {
        return currentUser;
    }

    public boolean isLoggedIn() {
        return currentUser != null && !"guest".equals(currentUser.getRole());
    }

    public void logout() {
        this.currentUser = null;
        System.out.println("📌 Session: Déconnexion");
    }
}