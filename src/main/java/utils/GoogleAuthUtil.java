package utils;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.oauth2.Oauth2;
import com.google.api.services.oauth2.model.Userinfo;

import java.io.*;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;

public class GoogleAuthUtil {

    private static final String APPLICATION_NAME = "GrowMind";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Arrays.asList(
            "https://www.googleapis.com/auth/userinfo.email",
            "https://www.googleapis.com/auth/userinfo.profile"
    );
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    private static GoogleAuthUtil instance;
    private Credential credential;
    private Userinfo userInfo;

    private GoogleAuthUtil() {}

    public static GoogleAuthUtil getInstance() {
        if (instance == null) {
            instance = new GoogleAuthUtil();
        }
        return instance;
    }

    private GoogleClientSecrets getClientSecrets() throws IOException {
        InputStream in = getClass().getResourceAsStream("/client_secret.json");
        if (in == null) {
            throw new FileNotFoundException("❌ client_secret.json non trouvé dans resources");
        }
        return GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
    }

    /**
     * Méthode principale d'authentification
     * @param forceNewAccount true pour forcer le choix du compte à chaque fois
     */
    public boolean authenticate(boolean forceNewAccount) {
        LocalServerReceiver receiver = null;
        try {
            // Si on veut forcer un nouveau compte, on supprime les tokens existants
            if (forceNewAccount) {
                clearTokens();
                System.out.println("🔄 Mode force new account activé - Choix du compte requis");
            }

            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            GoogleClientSecrets clientSecrets = getClientSecrets();

            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                    HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                    .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                    .setAccessType("offline")
                    .build();

            // Utiliser TOUJOURS un port aléatoire pour éviter les conflits
            receiver = new LocalServerReceiver.Builder()
                    .setPort(0)  // 0 = port aléatoire toujours disponible
                    .build();

            // Autorisation standard
            credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");

            System.out.println("✅ Authentification Google réussie sur le port " + receiver.getPort());
            return true;

        } catch (Exception e) {
            System.err.println("❌ Erreur authentification: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            // S'assurer que le serveur est bien fermé
            if (receiver != null) {
                try {
                    receiver.stop();
                    System.out.println("✅ Serveur local arrêté proprement");
                } catch (Exception e) {
                    // Ignorer
                }
            }
        }
    }

    /**
     * Surcharge pour compatibilité avec l'ancien code
     */
    public boolean authenticate() {
        return authenticate(false);
    }

    /**
     * Force le changement de compte à la prochaine connexion
     */
    public void forceAccountChoice() {
        clearTokens();
        System.out.println("✅ Mode changement de compte activé - La prochaine connexion demandera de choisir un compte");
    }

    /**
     * Efface tous les tokens sauvegardés
     */
    public void clearTokens() {
        File tokensDir = new File(TOKENS_DIRECTORY_PATH);
        if (tokensDir.exists()) {
            deleteDirectory(tokensDir);
            System.out.println("🗑️ Tokens supprimés avec succès");
        } else {
            System.out.println("ℹ️ Aucun token à supprimer");
        }
    }

    /**
     * Supprime un dossier récursivement
     */
    private void deleteDirectory(File dir) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    if (file.delete()) {
                        System.out.println("   ✓ Fichier supprimé: " + file.getName());
                    }
                }
            }
        }
        if (dir.delete()) {
            System.out.println("   ✓ Dossier supprimé: " + dir.getName());
        }
    }

    public boolean getUserInfo() {
        if (credential == null) {
            System.err.println("❌ Non authentifié - Appelez authenticate() d'abord");
            return false;
        }

        try {
            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            Oauth2 oauth2 = new Oauth2.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                    .setApplicationName(APPLICATION_NAME)
                    .build();

            userInfo = oauth2.userinfo().get().execute();

            System.out.println("✅ Informations récupérées pour: " + getEmail());
            return true;

        } catch (Exception e) {
            System.err.println("❌ Erreur récupération infos: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public String getEmail() {
        return userInfo != null ? userInfo.getEmail() : null;
    }

    public String getFullName() {
        return userInfo != null ? userInfo.getName() : null;
    }

    /**
     * Vérifie si l'utilisateur est authentifié
     */
    public boolean isAuthenticated() {
        return credential != null;
    }
}