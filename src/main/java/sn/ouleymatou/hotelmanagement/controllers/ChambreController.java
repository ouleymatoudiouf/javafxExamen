package sn.ouleymatou.hotelmanagement.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import sn.ouleymatou.hotelmanagement.entities.Chambre;
import sn.ouleymatou.hotelmanagement.entities.Chambre.StatutChambre;
import sn.ouleymatou.hotelmanagement.entities.TypeChambre;
import sn.ouleymatou.hotelmanagement.services.ChambreService;
import sn.ouleymatou.hotelmanagement.services.TypeChambreService;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ChambreController implements Initializable {

    @FXML private TableView<Chambre> chambreTable;
    @FXML private TableColumn<Chambre, String> numeroColumn;
    @FXML private TableColumn<Chambre, String> typeColumn;
    @FXML private TableColumn<Chambre, String> statutColumn;
    @FXML private TableColumn<Chambre, Void> actionColumn;

    @FXML private ComboBox<String> typeComboBox;
    @FXML private ComboBox<String> statutComboBox;
    @FXML private Label totalLabel;

    private final ChambreService chambreService = new ChambreService();
    private final TypeChambreService typeChambreService = new TypeChambreService();
    private ObservableList<Chambre> chambreList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        configurerColonnes();
        chargerChambres();
        chargerFiltres();
    }

    // --------------------- Configuration des colonnes ---------------------
    private void configurerColonnes() {
        numeroColumn.setCellValueFactory(new PropertyValueFactory<>("numero"));
        typeColumn.setCellValueFactory(cellData -> {
            TypeChambre type = cellData.getValue().getTypeChambre();
            return new javafx.beans.property.SimpleStringProperty(type != null ? type.getLibelle() : "N/A");
        });
        statutColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getStatut().name()));

        actionColumn.setCellFactory(col -> new TableCell<>() {
            private final Button btnModifier = new Button("Modifier");
            private final Button btnSupprimer = new Button("❌");
            private final HBox hbox = new HBox(5, btnModifier, btnSupprimer);

            {
                btnModifier.setOnAction(e -> modifierChambre(getTableView().getItems().get(getIndex())));
                btnSupprimer.setOnAction(e -> supprimerChambre(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : hbox);
            }
        });
    }

    // --------------------- Chargement des chambres ---------------------
    private void chargerChambres() {
        chambreList.setAll(chambreService.lister());
        chambreTable.setItems(chambreList);
        totalLabel.setText("Total : " + chambreList.size());
    }

    // --------------------- Chargement des filtres ---------------------
    private void chargerFiltres() {
        typeComboBox.getItems().clear();
        typeComboBox.getItems().add("Tous");
        typeChambreService.getAll().forEach(type -> typeComboBox.getItems().add(type.getLibelle()));
        typeComboBox.getSelectionModel().selectFirst();

        statutComboBox.getItems().clear();
        statutComboBox.getItems().add("Tous");
        for (StatutChambre statut : StatutChambre.values()) {
            statutComboBox.getItems().add(statut.name());
        }
        statutComboBox.getSelectionModel().selectFirst();
    }

    // --------------------- Gestion du retour ---------------------
    @FXML
    public void handleRetour(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/dashboard-view.fxml"));
            Stage stage = (Stage) chambreTable.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", null, "Impossible de revenir au tableau de bord.");
            e.printStackTrace();
        }
    }

    // --------------------- Ajout / Modification ---------------------
    @FXML
    public void handleNouvelleChambre(ActionEvent event) {
        ouvrirDialogChambre(null, "Ajouter une chambre");
    }

    private void modifierChambre(Chambre chambre) {
        ouvrirDialogChambre(chambre, "Modifier la chambre");
    }

    private void ouvrirDialogChambre(Chambre chambre, String titre) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AddChambreDialog.fxml"));
            DialogPane dialogPane = loader.load();

            Dialog<Chambre> dialog = new Dialog<>();
            dialog.setTitle(titre);
            dialog.setDialogPane(dialogPane);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            AddChambreDialogController controller = loader.getController();
            controller.setDialog(dialog);
            if (chambre != null) controller.setChambre(chambre);

            dialog.showAndWait().ifPresent(result -> {
                if (result != null) {
                    if (chambre == null) {
                        chambreList.add(result);
                    } else {
                        int index = chambreList.indexOf(chambre);
                        if (index >= 0) chambreList.set(index, result);
                    }
                    totalLabel.setText("Total : " + chambreList.size());
                }
            });
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", null, "Impossible d'ouvrir le formulaire de chambre.");
            e.printStackTrace();
        }
    }

    // --------------------- Suppression ---------------------
    private void supprimerChambre(Chambre chambre) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION,
                "Voulez-vous vraiment supprimer cette chambre ?", ButtonType.YES, ButtonType.NO);
        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    chambreService.supprimer(chambre);
                    chambreList.remove(chambre);
                    totalLabel.setText("Total : " + chambreList.size());
                    showAlert(Alert.AlertType.INFORMATION, "Succès", null, "Chambre supprimée.");
                } catch (IllegalStateException ex) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", null, ex.getMessage());
                }
            }
        });
    }

    // --------------------- Filtrage ---------------------
    @FXML
    public void handleFiltrer(ActionEvent event) {
        String selectedType = typeComboBox.getValue();
        String selectedStatut = statutComboBox.getValue();
        List<Chambre> result = chambreService.filtrer(selectedType, selectedStatut);
        chambreList.setAll(result);
        totalLabel.setText("Total : " + chambreList.size());
    }

    // --------------------- Exports ---------------------
    @FXML public void handleExportExcel(ActionEvent event) { /* TODO */ }
    @FXML public void handleExportPDF(ActionEvent event) { /* TODO */ }

    // --------------------- Alert utilitaire ---------------------
    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
