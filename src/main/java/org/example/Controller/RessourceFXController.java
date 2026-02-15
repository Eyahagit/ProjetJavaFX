package org.example.Controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;

import javafx.stage.Stage;
import org.example.Models.Ressource;
import org.example.Services.ressourceService;

import javafx.scene.control.cell.PropertyValueFactory;
import java.time.LocalDate;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class RessourceFXController {

    @FXML
    private TextField txtTitre; // Should be read-only or removed if we strictly follow "Ajouter opens new
                                // interface"
    @FXML
    private TextArea txtDescription;
    @FXML
    private TextField txtLocalisation; // Mapping to content
    @FXML
    private DatePicker datePicker; // Mapping to dateCreation (display only)

    @FXML
    private Button btnAjouter;
    @FXML
    private Button btnModifier;
    @FXML
    private Button btnSupprimer;

    @FXML
    private TableView<Ressource> listEvenements; // Mapped to the TableView in FXML

    @FXML
    private TableColumn<Ressource, String> colTitre;

    @FXML
    private TableColumn<Ressource, String> colDescription;

    @FXML
    private TableColumn<Ressource, String> colType;

    @FXML
    private TableColumn<Ressource, String> colCategory;

    @FXML
    private TableColumn<Ressource, LocalDate> colDate;

    @FXML
    private TableColumn<Ressource, String> colAuthor;

    @FXML
    private TextField searchField;

    @FXML
    private Label lblCount;

    private final ressourceService service = new ressourceService();
    private ObservableList<Ressource> ressourceList = FXCollections.observableArrayList();
    private ObservableList<Ressource> filteredList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Initialize Table Columns
        colTitre.setCellValueFactory(new PropertyValueFactory<>("title"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateCreation"));
        colAuthor.setCellValueFactory(new PropertyValueFactory<>("author"));

        refreshList();

        // Add selection listener
        listEvenements.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                showDetails(newValue);
            }
        });
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText().toLowerCase();

        if (query.isEmpty()) {
            listEvenements.setItems(ressourceList);
            lblCount.setText(ressourceList.size() + " ressource(s)");
            return;
        }

        List<Ressource> filtered = ressourceList.stream()
                .filter(r -> r.getTitle().toLowerCase().contains(query) ||
                        r.getDescription().toLowerCase().contains(query) ||
                        r.getType().toLowerCase().contains(query) ||
                        r.getCategory().toLowerCase().contains(query))
                .toList();

        filteredList.setAll(filtered);
        listEvenements.setItems(filteredList);
        lblCount.setText(filteredList.size() + " ressource(s)");
    }

    public void refreshList() {
        List<Ressource> list = service.findAll();
        ressourceList.setAll(list);
        listEvenements.setItems(ressourceList);
        lblCount.setText(list.size() + " ressource(s)");
    }

    private void showDetails(Ressource r) {
        if (txtTitre != null)
            txtTitre.setText(r.getTitle());
        if (txtDescription != null)
            txtDescription.setText(r.getDescription());
        if (txtLocalisation != null)
            txtLocalisation.setText(r.getContent());
        if (datePicker != null)
            datePicker.setValue(r.getDateCreation());
    }

    @FXML
    private void ajouterEvenement() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/addRessource.fxml"));
            Parent root = loader.load();

            AddRessourceController controller = loader.getController();
            controller.setMainController(this);

            Stage stage = new Stage();
            stage.setTitle("Ajouter Ressource");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir la fenêtre d'ajout.");
        }
    }

    @FXML
    private void modifierEvenement() {
        Ressource selected = listEvenements.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Sélection requise", "Veuillez sélectionner une ressource à modifier.");
            return;
        }

        // We can update directly from the fields on the left (if we keep them editable)
        // Or open a dialogue. The user said "l'admin peut modifier...".
        // Use the fields on the left for modification to keep "meme design"

        selected.setTitle(txtTitre.getText());
        selected.setDescription(txtDescription.getText());
        selected.setContent(txtLocalisation.getText());
        if (datePicker.getValue() != null) {
            selected.setDateCreation(datePicker.getValue());
        }

        service.update(selected);
        refreshList();
        showAlert(Alert.AlertType.INFORMATION, "Succès", "Ressource modifiée avec succès !");
    }

    @FXML
    private void supprimerEvenement() {
        Ressource selected = listEvenements.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Sélection requise", "Veuillez sélectionner une ressource à supprimer.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer la ressource ?");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer : " + selected.getTitle() + " ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            service.delete(selected.getId());
            refreshList();
        }
    }

    // Hover effects (can be empty if handled by CSS or generic handlers,
    // but FXML refs them so they need to exist)
    @FXML
    private void hoverAjouter() {
    }

    @FXML
    private void exitAjouter() {
    }

    @FXML
    private void hoverModifier() {
    }

    @FXML
    private void exitModifier() {
    }

    @FXML
    private void hoverSupprimer() {
    }

    @FXML
    private void exitSupprimer() {
    }

    @FXML
    private void handleListClick() {
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
