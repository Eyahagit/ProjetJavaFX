package Controllers;

import Models.SleepTracking;
import Services.SleepTrackingService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import com.lowagie.text.Document;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

public class GestionSommeilController implements Initializable {

    @FXML private TextField tfUserId;
    @FXML private DatePicker dpDateSommeil;
    @FXML private TextField tfHeureCoucher;
    @FXML private TextField tfHeureReveil;
    @FXML private Spinner<Integer> spQualiteSommeil;
    @FXML private TextArea taCommentaire;
    @FXML private TextField tfDuree;

    @FXML private DatePicker dpDateDebut;
    @FXML private DatePicker dpDateFin;

    @FXML private TableView<SleepTracking> tableView;
    @FXML private TableColumn<SleepTracking, Integer> colId;
    @FXML private TableColumn<SleepTracking, Integer> colUserId;
    @FXML private TableColumn<SleepTracking, LocalDate> colDate;
    @FXML private TableColumn<SleepTracking, LocalTime> colCoucher;
    @FXML private TableColumn<SleepTracking, LocalTime> colReveil;
    @FXML private TableColumn<SleepTracking, Integer> colDuree;
    @FXML private TableColumn<SleepTracking, Integer> colQualite;
    @FXML private TableColumn<SleepTracking, String> colCommentaire;
    @FXML private ImageView logoImageView;

    private final SleepTrackingService service = new SleepTrackingService();
    private final ObservableList<SleepTracking> dataList = FXCollections.observableArrayList();

    private static final int DEFAULT_USER_ID = 1;

    private boolean userEditedCommentaire = false;
    private boolean internalCommentUpdate = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Pattern digitsOnly = Pattern.compile("\\d*");
        UnaryOperator<TextFormatter.Change> digitsFilter = change -> {
            String text = change.getControlNewText();
            return digitsOnly.matcher(text).matches() ? change : null;
        };
        tfUserId.setTextFormatter(new TextFormatter<>(digitsFilter));

        Pattern timeAllowed = Pattern.compile("[0-9:]*");
        UnaryOperator<TextFormatter.Change> timeFilter = change -> {
            String text = change.getControlNewText();
            if (!timeAllowed.matcher(text).matches()) return null;
            return text.length() <= 5 ? change : null;
        };
        tfHeureCoucher.setTextFormatter(new TextFormatter<>(timeFilter));
        tfHeureReveil.setTextFormatter(new TextFormatter<>(timeFilter));

        taCommentaire.setTextFormatter(new TextFormatter<>(change -> {
            return change.getControlNewText().length() <= 1000 ? change : null;
        }));

        taCommentaire.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!internalCommentUpdate) {
                userEditedCommentaire = newVal != null && !newVal.trim().isEmpty();
            }
        });

        tfUserId.setText(String.valueOf(DEFAULT_USER_ID));
        dpDateSommeil.setValue(LocalDate.now());

        spQualiteSommeil.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 5, 3));
        spQualiteSommeil.setEditable(true);
        tfHeureCoucher.setText("22:30");
        tfHeureReveil.setText("06:30");
        tfDuree.setEditable(false);

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colUserId.setCellValueFactory(new PropertyValueFactory<>("userId"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateSommeil"));
        colCoucher.setCellValueFactory(new PropertyValueFactory<>("heureCoucher"));
        colReveil.setCellValueFactory(new PropertyValueFactory<>("heureReveil"));
        colDuree.setCellValueFactory(new PropertyValueFactory<>("dureeMinutes"));
        colQualite.setCellValueFactory(new PropertyValueFactory<>("qualiteSommeil"));
        colCommentaire.setCellValueFactory(new PropertyValueFactory<>("commentaire"));

        tableView.setItems(dataList);

        tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) fillForm(newVal);
        });

        dpDateSommeil.valueProperty().addListener((o, a, b) -> refreshDurationPreview());
        tfHeureCoucher.textProperty().addListener((o, a, b) -> refreshDurationPreview());
        tfHeureReveil.textProperty().addListener((o, a, b) -> refreshDurationPreview());

        spQualiteSommeil.valueProperty().addListener((o, a, b) -> refreshConseilsPreview());
        dpDateSommeil.valueProperty().addListener((o, a, b) -> refreshConseilsPreview());
        tfHeureCoucher.textProperty().addListener((o, a, b) -> refreshConseilsPreview());
        tfHeureReveil.textProperty().addListener((o, a, b) -> refreshConseilsPreview());

        loadAll();
        refreshDurationPreview();
        refreshConseilsPreview();
        
        // Load GrowMind logo
        loadLogo();
    }

    private String validateFormForSave() {
        StringBuilder errors = new StringBuilder();

        String rawUserId = tfUserId.getText() != null ? tfUserId.getText().trim() : "";
        if (rawUserId.isEmpty()) {
            errors.append("- ID Utilisateur est obligatoire\n");
        } else {
            try {
                int userId = Integer.parseInt(rawUserId);
                if (userId <= 0) errors.append("- ID Utilisateur doit être > 0\n");
            } catch (NumberFormatException e) {
                errors.append("- ID Utilisateur doit être un nombre\n");
            }
        }

        LocalDate date = dpDateSommeil.getValue();
        if (date == null) {
            errors.append("- Date est obligatoire\n");
        }

        String rawCoucher = tfHeureCoucher.getText() != null ? tfHeureCoucher.getText().trim() : "";
        if (rawCoucher.isEmpty()) {
            errors.append("- Heure coucher est obligatoire\n");
        } else if (parseTime(rawCoucher) == null) {
            errors.append("- Heure coucher invalide (format HH:mm)\n");
        }

        String rawReveil = tfHeureReveil.getText() != null ? tfHeureReveil.getText().trim() : "";
        if (rawReveil.isEmpty()) {
            errors.append("- Heure réveil est obligatoire\n");
        } else if (parseTime(rawReveil) == null) {
            errors.append("- Heure réveil invalide (format HH:mm)\n");
        }

        Integer qualite = spQualiteSommeil.getValue();
        if (qualite == null || qualite < 1 || qualite > 5) {
            errors.append("- Qualité sommeil doit être entre 1 et 5\n");
        }

        String commentaire = taCommentaire.getText() != null ? taCommentaire.getText().trim() : "";
        if (commentaire.isEmpty()) {
            errors.append("- Commentaire est obligatoire\n");
        }
        if (commentaire.length() > 1000) {
            errors.append("- Commentaire dépasse 1000 caractères\n");
        }

        return errors.length() == 0 ? null : errors.toString();
    }

    private void loadAll() {
        dataList.clear();
        List<SleepTracking> list = service.getAll();
        dataList.addAll(list);
    }

    private void loadFiltered(LocalDate start, LocalDate end) {
        int userId = getUserId();
        dataList.clear();
        List<SleepTracking> list = service.getByDateRange(userId, start, end);
        dataList.addAll(list);
    }

    private int getUserId() {
        try {
            return Integer.parseInt(tfUserId.getText().trim());
        } catch (NumberFormatException e) {
            return DEFAULT_USER_ID;
        }
    }

    private LocalTime parseTime(String raw) {
        if (raw == null) return null;
        String t = raw.trim();
        if (t.isEmpty()) return null;

        // Accept inputs like: 7, 07, 7:5, 7:05, 07:5, 07:05
        if (t.matches("^\\d{1,2}$")) {
            int h = Integer.parseInt(t);
            if (h < 0 || h > 23) return null;
            return LocalTime.of(h, 0);
        }
        if (t.matches("^\\d{1,2}:\\d{1,2}$")) {
            String[] parts = t.split(":");
            int h = Integer.parseInt(parts[0]);
            int m = Integer.parseInt(parts[1]);
            if (h < 0 || h > 23) return null;
            if (m < 0 || m > 59) return null;
            return LocalTime.of(h, m);
        }
        try {
            return LocalTime.parse(t, DateTimeFormatter.ofPattern("H:mm"));
        } catch (DateTimeParseException e) {
            try {
                return LocalTime.parse(t);
            } catch (DateTimeParseException ignored) {
                return null;
            }
        }
    }

    private void refreshDurationPreview() {
        LocalDate date = dpDateSommeil.getValue();
        LocalTime coucher = parseTime(tfHeureCoucher.getText());
        LocalTime reveil = parseTime(tfHeureReveil.getText());
        if (date == null || coucher == null || reveil == null) {
            tfDuree.setText("");
            return;
        }
        int minutes = service.computeDurationMinutes(date, coucher, reveil);
        tfDuree.setText(formatDuration(minutes));
    }

    private void refreshConseilsPreview() {
        if (userEditedCommentaire) return;

        LocalDate date = dpDateSommeil.getValue();
        LocalTime coucher = parseTime(tfHeureCoucher.getText());
        LocalTime reveil = parseTime(tfHeureReveil.getText());
        Integer qualite = spQualiteSommeil.getValue();

        int minutes = -1;
        if (date != null && coucher != null && reveil != null) {
            minutes = service.computeDurationMinutes(date, coucher, reveil);
        }

        String conseils = buildConseils(qualite != null ? qualite : 0, minutes);
        if (conseils == null || conseils.trim().isEmpty()) return;

        internalCommentUpdate = true;
        try {
            taCommentaire.setText(conseils);
        } finally {
            internalCommentUpdate = false;
        }
    }

    private String buildConseils(int qualite, int minutes) {
        StringBuilder sb = new StringBuilder();

        if (qualite >= 1 && qualite <= 5) {
            if (qualite <= 2) {
                sb.append("Qualité faible (").append(qualite).append("/5). ");
                sb.append("Essaie de réduire les écrans 60 min avant le coucher, et adopte une routine calme (lecture, respiration).\n");
            } else if (qualite == 3) {
                sb.append("Qualité moyenne (3/5). ");
                sb.append("Une routine régulière et une chambre fraîche/sombre peuvent améliorer ton sommeil.\n");
            } else {
                sb.append("Bonne qualité (").append(qualite).append("/5). ");
                sb.append("Continue avec tes bonnes habitudes (horaires réguliers, activité légère).\n");
            }
        }

        if (minutes > 0) {
            double hours = minutes / 60.0;
            sb.append("Durée estimée: ").append(formatDuration(minutes)).append(". ");
            if (hours < 6) {
                sb.append("Sommeil un peu court : vise 7–9h, évite la caféine après 16h, et essaie de te coucher plus tôt.\n");
            } else if (hours <= 9.5) {
                sb.append("Durée dans une bonne fourchette : maintiens des horaires constants.\n");
            } else {
                sb.append("Sommeil long : si tu te sens fatigué malgré tout, pense à la qualité (stress, bruit, réveils nocturnes).\n");
            }
        } else {
            sb.append("Renseigne l'heure de coucher et de réveil pour calculer la durée automatiquement.\n");
        }

        return sb.toString().trim();
    }

    private String formatDuration(int minutes) {
        int h = minutes / 60;
        int m = minutes % 60;
        return h + "h" + (m < 10 ? "0" + m : String.valueOf(m));
    }

    private SleepTracking buildFromForm() {
        LocalDate date = dpDateSommeil.getValue();
        LocalTime coucher = parseTime(tfHeureCoucher.getText());
        LocalTime reveil = parseTime(tfHeureReveil.getText());
        if (date == null || coucher == null || reveil == null) return null;

        int userId = getUserId();
        int qualite = spQualiteSommeil.getValue();
        int minutes = service.computeDurationMinutes(date, coucher, reveil);

        return new SleepTracking(userId, date, coucher, reveil, minutes, qualite, taCommentaire.getText());
    }

    private void fillForm(SleepTracking s) {
        tfUserId.setText(String.valueOf(s.getUserId()));
        dpDateSommeil.setValue(s.getDateSommeil());
        tfHeureCoucher.setText(s.getHeureCoucher() != null ? s.getHeureCoucher().toString() : "");
        tfHeureReveil.setText(s.getHeureReveil() != null ? s.getHeureReveil().toString() : "");
        spQualiteSommeil.getValueFactory().setValue(s.getQualiteSommeil());
        userEditedCommentaire = true;
        taCommentaire.setText(s.getCommentaire());
        tfDuree.setText(formatDuration(s.getDureeMinutes()));
    }

    @FXML
    private void handleAdd() {
        String validation = validateFormForSave();
        if (validation != null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", validation, null);
            return;
        }
        SleepTracking s = buildFromForm();
        if (s == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez remplir les champs (date + heures au format HH:mm).", null);
            return;
        }
        int id = service.add(s);
        if (id > 0) {
            s.setId(id);
            dataList.add(0, s);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Sommeil ajouté avec succès.", null);
            handleClear();
        } else {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ajouter l'enregistrement.", null);
        }
    }

    @FXML
    private void handleUpdate() {
        SleepTracking selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Sélectionnez un enregistrement à modifier.", null);
            return;
        }
        String validation = validateFormForSave();
        if (validation != null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", validation, null);
            return;
        }
        SleepTracking s = buildFromForm();
        if (s == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez remplir les champs (date + heures au format HH:mm).", null);
            return;
        }
        s.setId(selected.getId());
        if (service.update(s)) {
            int idx = dataList.indexOf(selected);
            dataList.set(idx, s);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Sommeil modifié avec succès.", null);
            handleClear();
        } else {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de modifier l'enregistrement.", null);
        }
    }

    @FXML
    private void handleDelete() {
        SleepTracking selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Sélectionnez un enregistrement à supprimer.", null);
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText(null);
        confirm.setContentText("Supprimer cet enregistrement ?");
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;

        if (service.delete(selected.getId())) {
            dataList.remove(selected);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Sommeil supprimé.", null);
            handleClear();
        } else {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de supprimer l'enregistrement.", null);
        }
    }

    @FXML
    private void handleClear() {
        tfUserId.setText(String.valueOf(DEFAULT_USER_ID));
        dpDateSommeil.setValue(LocalDate.now());
        tfHeureCoucher.setText("22:30");
        tfHeureReveil.setText("06:30");
        spQualiteSommeil.getValueFactory().setValue(3);
        userEditedCommentaire = false;
        taCommentaire.clear();
        tableView.getSelectionModel().clearSelection();
        refreshDurationPreview();
        refreshConseilsPreview();
    }

    @FXML
    private void handleFilter() {
        String rawUserId = tfUserId.getText() != null ? tfUserId.getText().trim() : "";
        if (rawUserId.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Attention", "ID Utilisateur est obligatoire pour filtrer.", null);
            return;
        }
        try {
            int userId = Integer.parseInt(rawUserId);
            if (userId <= 0) {
                showAlert(Alert.AlertType.WARNING, "Attention", "ID Utilisateur doit être > 0.", null);
                return;
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Attention", "ID Utilisateur doit être un nombre.", null);
            return;
        }
        LocalDate start = dpDateDebut.getValue();
        LocalDate end = dpDateFin.getValue();
        if (start == null || end == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Sélectionnez les dates de début et fin.", null);
            return;
        }
        if (start.isAfter(end)) {
            showAlert(Alert.AlertType.WARNING, "Attention", "La date de début doit être avant la date de fin.", null);
            return;
        }
        loadFiltered(start, end);
    }

    @FXML
    private void handleShowAll() {
        loadAll();
        dpDateDebut.setValue(null);
        dpDateFin.setValue(null);
    }

    private void showAlert(Alert.AlertType type, String title, String message, String header) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleBack() {
        Stage stage = (Stage) tableView.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleExportPdf() {
        try {
            if (tableView.getItems() == null || tableView.getItems().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Attention", "Aucune donnée à exporter.", null);
                return;
            }

            FileChooser chooser = new FileChooser();
            chooser.setTitle("Exporter l'historique Sommeil en PDF");
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
            chooser.setInitialFileName("GrowMind_Sommeil_User" + getUserId() + ".pdf");

            Stage stage = (Stage) tableView.getScene().getWindow();
            File file = chooser.showSaveDialog(stage);
            if (file == null) return;

            exportSleepPdf(file);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "PDF exporté avec succès :\n" + file.getAbsolutePath(), null);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'exporter en PDF.\n" + e.getMessage(), null);
        }
    }

    private void exportSleepPdf(File file) throws Exception {
        List<SleepTracking> rows = tableView.getItems();
        int userId = getUserId();
        LocalDate start = dpDateDebut.getValue();
        LocalDate end = dpDateFin.getValue();

        List<SleepTracking> sorted = rows.stream()
                .sorted(Comparator.comparing(SleepTracking::getDateSommeil))
                .toList();

        double avgQuality = sorted.stream().mapToInt(SleepTracking::getQualiteSommeil).average().orElse(0.0);
        double avgMinutes = sorted.stream().mapToInt(SleepTracking::getDureeMinutes).average().orElse(0.0);
        int totalMinutes = sorted.stream().mapToInt(SleepTracking::getDureeMinutes).sum();

        Document doc = new Document(PageSize.A4, 36, 36, 36, 36);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            PdfWriter.getInstance(doc, fos);
            doc.open();

            addPdfHeader(doc);

            Font titleFont = new Font(Font.HELVETICA, 16, Font.BOLD, new java.awt.Color(74, 111, 165));
            Paragraph title = new Paragraph("Historique Sommeil", titleFont);
            title.setSpacingBefore(8);
            title.setSpacingAfter(8);
            doc.add(title);

            Font metaFont = new Font(Font.HELVETICA, 10, Font.NORMAL, java.awt.Color.DARK_GRAY);
            Paragraph meta = new Paragraph();
            meta.setFont(metaFont);
            meta.add("Utilisateur: " + userId + "\n");
            meta.add("Généré le: " + java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + "\n");
            if (start != null && end != null) {
                meta.add("Période: " + start.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " → " + end.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + "\n");
            } else {
                meta.add("Période: Toutes les dates\n");
            }
            meta.setSpacingAfter(10);
            doc.add(meta);

            addSummaryCards(doc, sorted.size(), avgQuality, avgMinutes, totalMinutes);
            doc.add(Chunk.NEWLINE);
            addSleepTable(doc, sorted);
        } finally {
            if (doc.isOpen()) doc.close();
        }
    }

    private void addPdfHeader(Document doc) {
        try {
            InputStream is = getClass().getResourceAsStream("/images/logo.jpeg");
            if (is != null) {
                byte[] bytes = is.readAllBytes();
                Image logo = Image.getInstance(bytes);
                logo.scaleToFit(240, 70);
                logo.setAlignment(Image.ALIGN_LEFT);
                doc.add(logo);
            }
        } catch (Exception ignored) {
        }
    }

    private void addSummaryCards(Document doc, int count, double avgQuality, double avgMinutes, int totalMinutes) throws DocumentException {
        PdfPTable t = new PdfPTable(4);
        t.setWidthPercentage(100);
        t.setSpacingBefore(6);
        t.setSpacingAfter(6);
        t.setWidths(new float[]{1.1f, 1.1f, 1.1f, 1.1f});

        t.addCell(summaryCell("Nuits", String.valueOf(count)));
        t.addCell(summaryCell("Qualité moyenne", String.format("%.1f/5", avgQuality)));
        t.addCell(summaryCell("Durée moyenne", formatMinutes((int) Math.round(avgMinutes))));
        t.addCell(summaryCell("Total sommeil", formatMinutes(totalMinutes)));
        doc.add(t);
    }

    private PdfPCell summaryCell(String label, String value) {
        Font labelFont = new Font(Font.HELVETICA, 9, Font.NORMAL, java.awt.Color.GRAY);
        Font valueFont = new Font(Font.HELVETICA, 12, Font.BOLD, new java.awt.Color(74, 111, 165));

        Paragraph p = new Paragraph();
        p.add(new Phrase(label + "\n", labelFont));
        p.add(new Phrase(value, valueFont));

        PdfPCell cell = new PdfPCell(p);
        cell.setPadding(10);
        cell.setBorderColor(new java.awt.Color(209, 231, 243));
        cell.setBackgroundColor(new java.awt.Color(248, 253, 255));
        return cell;
    }

    private void addSleepTable(Document doc, List<SleepTracking> rows) throws DocumentException {
        PdfPTable table = new PdfPTable(7);
        table.setWidthPercentage(100);
        table.setHeaderRows(1);
        table.setWidths(new float[]{1.0f, 1.0f, 0.9f, 0.9f, 0.9f, 0.8f, 2.2f});

        addHeaderCell(table, "Date");
        addHeaderCell(table, "User");
        addHeaderCell(table, "Coucher");
        addHeaderCell(table, "Réveil");
        addHeaderCell(table, "Durée");
        addHeaderCell(table, "Qualité");
        addHeaderCell(table, "Commentaire");

        DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        for (SleepTracking r : rows) {
            table.addCell(bodyCell(r.getDateSommeil() != null ? r.getDateSommeil().format(df) : ""));
            table.addCell(bodyCell(String.valueOf(r.getUserId())));
            table.addCell(bodyCell(r.getHeureCoucher() != null ? r.getHeureCoucher().toString() : ""));
            table.addCell(bodyCell(r.getHeureReveil() != null ? r.getHeureReveil().toString() : ""));
            table.addCell(bodyCell(formatMinutes(r.getDureeMinutes())));
            table.addCell(bodyCell(String.valueOf(r.getQualiteSommeil())));
            table.addCell(bodyCell(r.getCommentaire() != null ? r.getCommentaire() : ""));
        }
        doc.add(table);
    }

    private void addHeaderCell(PdfPTable table, String text) {
        Font f = new Font(Font.HELVETICA, 10, Font.BOLD, java.awt.Color.WHITE);
        PdfPCell c = new PdfPCell(new Phrase(text, f));
        c.setBackgroundColor(new java.awt.Color(74, 111, 165));
        c.setPadding(6);
        c.setBorderColor(new java.awt.Color(74, 111, 165));
        table.addCell(c);
    }

    private PdfPCell bodyCell(String text) {
        Font f = new Font(Font.HELVETICA, 9, Font.NORMAL, java.awt.Color.DARK_GRAY);
        PdfPCell c = new PdfPCell(new Phrase(text, f));
        c.setPadding(5);
        c.setBorderColor(new java.awt.Color(224, 224, 224));
        return c;
    }

    private String formatMinutes(int minutes) {
        if (minutes <= 0) return "0h00";
        int h = minutes / 60;
        int m = minutes % 60;
        return h + "h" + (m < 10 ? "0" + m : String.valueOf(m));
    }
    
    /** Load GrowMind logo */
    private void loadLogo() {
        try {
            javafx.scene.image.Image logo = new javafx.scene.image.Image(getClass().getResourceAsStream("/images/logo.jpeg"));
            if (logo.isError()) {
                System.err.println("[DEBUG] Failed to load logo image in Sommeil");
            } else {
                logoImageView.setImage(logo);
                logoImageView.setFitWidth(600);
                logoImageView.setFitHeight(140);
                logoImageView.setPreserveRatio(true);
                System.err.println("[DEBUG] GrowMind logo loaded successfully in Sommeil with size 600x140");
            }
        } catch (Exception e) {
            System.err.println("[DEBUG] Exception loading logo in Sommeil: " + e.getMessage());
        }
    }
}
