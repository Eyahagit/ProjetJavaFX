package Controllers;

import entities.ForumPost;
import Models.users;
import Services.ServiceForumPost;
import utils.*;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.time.LocalDateTime;

public class PostFormController {

    public enum Mode { AJOUT, MODIFICATION }

    @FXML private Label lblTitre;
    @FXML private TextField txtNom;
    @FXML private ComboBox<String> cmbRole;
    @FXML private ComboBox<String> cmbCategorie;
    @FXML private TextArea txtContenu;
    @FXML private CheckBox chkArchive;
    @FXML private Button btnSauvegarder;
    @FXML private Button btnAnnuler;
    @FXML private Label lblUserInfo;

    private ServiceForumPost service;
    private PostListController postListController;
    private MainForumController mainController;
    private ForumPost postCourant;
    private Mode modeCourant;
    private boolean isFrench = true;
    private users currentUser;

    @FXML
    public void initialize() {
        // Initialiser les ComboBox
        cmbRole.setItems(FXCollections.observableArrayList(
                "🧑 Patient", "👨‍⚕️ Médecin", "🧘 Thérapeute"
        ));

        cmbCategorie.setItems(FXCollections.observableArrayList(
                "😟 Anxiété", "🧠 Stress", "😢 Dépression", "🧘 Méditation", "💬 Général"
        ));
        cmbCategorie.setValue("💬 Général");

        txtContenu.setPromptText("Écrivez votre message ici...");

        // Rendre le champ nom non éditable car il sera pré-rempli
        txtNom.setEditable(false);
        txtNom.setStyle("-fx-background-color: #f0f0f0;");

        // Rendre le champ rôle non éditable
        cmbRole.setDisable(true);
        cmbRole.setStyle("-fx-opacity: 1;");
    }

    public void setCurrentUser(users user) {
        this.currentUser = user;
        if (user != null) {
            // Pré-remplir le nom avec celui de l'utilisateur connecté
            String fullName = user.getName() + " " + user.getSecond_name();
            txtNom.setText(fullName.trim());

            // Définir le rôle en fonction du rôle de l'utilisateur
            String role = user.getRole();
            if (role != null) {
                switch(role.toLowerCase()) {
                    case "patient":
                        cmbRole.setValue("🧑 Patient");
                        break;
                    case "doctor":
                        cmbRole.setValue("👨‍⚕️ Médecin");
                        break;
                    case "admin":
                        cmbRole.setValue("👑 Administrateur");
                        break;
                    default:
                        cmbRole.setValue("🧑 Patient");
                }
            }

            // Afficher l'information utilisateur
            if (lblUserInfo != null) {
                lblUserInfo.setText("Connecté en tant que: " + fullName.trim() + " (" + getRoleDisplay(role) + ")");
            }

            System.out.println("✅ Formulaire pré-rempli pour: " + user.getEmail());
        }
    }

    private String getRoleDisplay(String role) {
        if (role == null) return "Invité";
        switch(role.toLowerCase()) {
            case "patient": return "Patient";
            case "doctor": return "Médecin";
            case "admin": return "Administrateur";
            default: return role;
        }
    }

    public void setService(ServiceForumPost service) {
        this.service = service;
    }

    public void setPostListController(PostListController controller) {
        this.postListController = controller;
        if (controller != null) {
            this.isFrench = controller.isFrench();
        }
    }

    public void setMainController(MainForumController controller) {
        this.mainController = controller;
        if (controller != null && controller.getCurrentUser() != null) {
            setCurrentUser(controller.getCurrentUser());
        }
    }

    public void setMode(Mode mode) {
        this.modeCourant = mode;
        if (mode == Mode.AJOUT) {
            lblTitre.setText(isFrench ? "➕ Nouvelle publication" : "➕ New post");
            btnSauvegarder.setText(isFrench ? "Publier" : "Publish");
            chkArchive.setVisible(false);
            chkArchive.setSelected(false);
            txtContenu.clear();
        } else {
            lblTitre.setText(isFrench ? "✏️ Modifier la publication" : "✏️ Edit post");
            btnSauvegarder.setText(isFrench ? "Mettre à jour" : "Update");
            chkArchive.setVisible(true);
        }
    }

    public void setPost(ForumPost post) {
        this.postCourant = post;
        txtNom.setText(post.getNom());
        cmbRole.setValue(post.getRole());
        cmbCategorie.setValue(post.getCategorie());
        txtContenu.setText(post.getContenu());
        chkArchive.setSelected(post.isArchive());
    }

    @FXML
    private void handleSauvegarder() {
        String nom = txtNom.getText().trim();
        String role = cmbRole.getValue();
        String categorie = cmbCategorie.getValue();
        String contenu = txtContenu.getText().trim();

        if (nom.isEmpty()) {
            showError(isFrench ? "Le nom est requis" : "Name is required");
            return;
        }

        if (role == null || role.isEmpty()) {
            showError(isFrench ? "Le rôle est requis" : "Role is required");
            return;
        }

        if (categorie == null || categorie.isEmpty()) {
            showError(isFrench ? "La catégorie est requise" : "Category is required");
            return;
        }

        if (contenu.isEmpty()) {
            showError(isFrench ? "Le contenu est requis" : "Content is required");
            txtContenu.requestFocus();
            return;
        }

        if (contenu.length() < 10) {
            showError(isFrench ? "Le contenu doit contenir au moins 10 caractères" : "Content must be at least 10 characters");
            txtContenu.requestFocus();
            return;
        }

        String contenuFiltre = BadWordsFilter.filter(contenu);
        String nomFiltre = BadWordsFilter.filter(nom);

        boolean motsFiltres = !contenu.equals(contenuFiltre) || !nom.equals(nomFiltre);

        if (motsFiltres) {
            showInfo(isFrench ? "Information" : "Information",
                    isFrench ? "Des mots inappropriés ont été automatiquement remplacés par ***"
                            : "Inappropriate words have been automatically replaced with ***");
        }

        try {
            if (modeCourant == Mode.AJOUT) {
                ForumPost nouveauPost = new ForumPost(
                        nomFiltre,
                        role,
                        categorie,
                        contenuFiltre,
                        LocalDateTime.now(),
                        false
                );

                service.ajouter(nouveauPost);
                showSuccess(isFrench ? "✅ Publication publiée avec succès !" : "✅ Post published successfully!");

            } else {
                if (postCourant == null) {
                    showError(isFrench ? "Publication non trouvée" : "Post not found");
                    return;
                }

                postCourant.setNom(nomFiltre);
                postCourant.setRole(role);
                postCourant.setCategorie(categorie);
                postCourant.setContenu(contenuFiltre);
                postCourant.setArchive(chkArchive.isSelected());

                service.modifier(postCourant);
                showSuccess(isFrench ? "✅ Publication modifiée avec succès !" : "✅ Post updated successfully!");
            }

            if (postListController != null) {
                postListController.refreshTable();
            }

            if (mainController != null) {
                mainController.refreshStatistics();
            }

            Thread.sleep(500);
            fermerFenetre();

        } catch (Exception e) {
            showError(isFrench ? "Erreur lors de l'enregistrement: " + e.getMessage()
                    : "Error saving post: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAnnuler() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle(isFrench ? "Confirmation" : "Confirmation");
        confirm.setHeaderText(null);
        confirm.setContentText(isFrench ? "Voulez-vous vraiment annuler ?" : "Do you really want to cancel?");

        if (confirm.showAndWait().get() == ButtonType.OK) {
            fermerFenetre();
        }
    }

    private void fermerFenetre() {
        Stage stage = (Stage) btnSauvegarder.getScene().getWindow();
        if (stage != null) {
            stage.close();
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(isFrench ? "Erreur" : "Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(isFrench ? "Succès" : "Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}