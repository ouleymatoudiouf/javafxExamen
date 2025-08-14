package sn.ouleymatou.hotelmanagement.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.scene.control.cell.PropertyValueFactory;
import sn.ouleymatou.hotelmanagement.entities.Reservation;
import sn.ouleymatou.hotelmanagement.services.ReservationService;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

public class ReservationController implements Initializable {

    // Table et colonnes
    @FXML private TableView<Reservation> tableReservations;
    @FXML private TableColumn<Reservation, String> colNumReservation;
    @FXML private TableColumn<Reservation, String> colClient;
    @FXML private TableColumn<Reservation, String> colChambre;
    @FXML private TableColumn<Reservation, String> colArrivee;
    @FXML private TableColumn<Reservation, String> colDepart;
    @FXML private TableColumn<Reservation, Integer> colNuits;
    @FXML private TableColumn<Reservation, String> colStatut;
    @FXML private TableColumn<Reservation, Double> colMontant;
    @FXML private TableColumn<Reservation, Void> colActions;

    // Filtres (non utilisés ici mais définis)
    @FXML private DatePicker dateDebut;
    @FXML private DatePicker dateFin;
    @FXML private ComboBox<String> statutFilter;
    @FXML private TextField recherche;

    // Onglets
    @FXML private TabPane tabPaneReservations;

    // Labels statistiques
    @FXML private Label lblTotalReservations;
    @FXML private Label lblArriveesAujourdHui;
    @FXML private Label lblDepartsAujourdHui;
    @FXML private Label lblChiffreAffaires;

    @FXML private Button btnNouvelleReservation;

    private final ObservableList<Reservation> reservationsObservable = FXCollections.observableArrayList();
    private final ReservationService reservationService = new ReservationService();

    // Variable pour garder l'onglet actif
    private String ongletActuel = "Toutes";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Configuration colonnes
        colNumReservation.setCellValueFactory(new PropertyValueFactory<>("numero"));
        colClient.setCellValueFactory(new PropertyValueFactory<>("nomCompletClient"));
        colChambre.setCellValueFactory(new PropertyValueFactory<>("numeroChambre"));
        colArrivee.setCellValueFactory(new PropertyValueFactory<>("dateArriveeString"));
        colDepart.setCellValueFactory(new PropertyValueFactory<>("dateDepartString"));
        colNuits.setCellValueFactory(new PropertyValueFactory<>("nombreNuits"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statutString"));
        colMontant.setCellValueFactory(new PropertyValueFactory<>("montantTotal"));

        tableReservations.setItems(reservationsObservable);

        statutFilter.setItems(FXCollections.observableArrayList("Tous", "Confirmée", "Annulée", "Check-in", "Check-out"));
        statutFilter.setValue("Tous");

        ajouterBoutonsActions();
        filtrerParOnglet("Toutes"); // charge toutes les réservations au départ
        chargerStatistiques();

        // Gestion changement d'onglet
        tabPaneReservations.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab != null) {
                ongletActuel = newTab.getText();
                filtrerParOnglet(ongletActuel);
                chargerStatistiques();
            }
        });
    }

    private void filtrerParOnglet(String onglet) {
        System.out.println("Filtrage onglet: " + onglet);
        this.ongletActuel = onglet;
        List<Reservation> reservations;

        switch (onglet) {
            case "Toutes":
                reservations = reservationService.getAllReservations();
                break;
            case "Arrivees":
                reservations = reservationService.getArriveesDuJour();
                break;
            case "Departs":
                reservations = reservationService.getDepartsDuJour();
                break;
            default:
                reservations = reservationService.getAllReservations();
        }

        System.out.println("Nombre de réservations récupérées: " + reservations.size());

        reservationsObservable.setAll(reservations);
    }


    // Rafraîchir avec l'onglet courant
    private void filtrerParOnglet() {
        filtrerParOnglet(this.ongletActuel);
    }

    // Ajouter boutons Check-in et Check-out dans la colonne Actions
    private void ajouterBoutonsActions() {
        Callback<TableColumn<Reservation, Void>, TableCell<Reservation, Void>> cellFactory = param -> new TableCell<>() {

            private final Button btnCheckIn = new Button("Check-in");
            private final Button btnCheckOut = new Button("Check-out");

            {
                btnCheckIn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");
                btnCheckOut.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white;");

                btnCheckIn.setOnAction(event -> {
                    Reservation data = getTableView().getItems().get(getIndex());
                    try {
                        reservationService.checkIn(data);
                        new Alert(Alert.AlertType.INFORMATION, "Check-in effectué avec succès.").showAndWait();
                        filtrerParOnglet();
                        chargerStatistiques();
                    } catch (Exception e) {
                        new Alert(Alert.AlertType.ERROR, "Erreur lors du check-in : " + e.getMessage()).showAndWait();
                    }
                });

                btnCheckOut.setOnAction(event -> {
                    Reservation data = getTableView().getItems().get(getIndex());
                    try {
                        reservationService.checkOut(data);
                        new Alert(Alert.AlertType.INFORMATION, "Check-out effectué avec succès.").showAndWait();
                        filtrerParOnglet();
                        chargerStatistiques();
                    } catch (Exception e) {
                        new Alert(Alert.AlertType.ERROR, "Erreur lors du check-out : " + e.getMessage()).showAndWait();
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                } else {
                    HBox hbox = new HBox(10);
                    Reservation res = getTableView().getItems().get(getIndex());

                    LocalDate today = LocalDate.now();

                    // Bouton Check-in activé uniquement si statut = CONFIRMEE et dateArrivee = aujourd'hui
                    if (res.getStatut() == Reservation.StatutReservation.CONFIRMEE) {
                        btnCheckIn.setDisable(!res.getDateArrivee().equals(today));
                        hbox.getChildren().add(btnCheckIn);
                    }
                    // Bouton Check-out activé uniquement si statut = EN_COURS et dateDepart = aujourd'hui
                    else if (res.getStatut() == Reservation.StatutReservation.EN_COURS) {
                        btnCheckOut.setDisable(!res.getDateDepart().equals(today));
                        hbox.getChildren().add(btnCheckOut);
                    }
                    setGraphic(hbox);
                }
            }
        };

        colActions.setCellFactory(cellFactory);
    }


    private void chargerStatistiques() {
        long totalReservations = reservationService.getTotalReservations();
        int arriveesAujourdHui = reservationService.getArriveesDuJour().size();
        int departsAujourdHui = reservationService.getDepartsDuJour().size();
        double chiffreAffaires = reservationService.getChiffreAffairesDuJour();

        lblTotalReservations.setText(totalReservations + " Total Réservations");
        lblArriveesAujourdHui.setText(arriveesAujourdHui + " Arrivées Aujourd'hui");
        lblDepartsAujourdHui.setText(departsAujourdHui + " Départs Aujourd'hui");
        lblChiffreAffaires.setText(String.format("%,.0f FCFA CA Aujourd'hui", chiffreAffaires));
    }

    @FXML
    private void handleNouvelleReservation(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AddReservationDialog.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Nouvelle Réservation");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            filtrerParOnglet();
            chargerStatistiques();
        } catch (IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Impossible de charger le formulaire de réservation.").showAndWait();
        }
    }

    @FXML
    private void handleFiltrer() {
        System.out.println("Filtrer avec : " + dateDebut.getValue() + " - " + dateFin.getValue() +
                " - " + statutFilter.getValue() + " - " + recherche.getText());
    }

    @FXML
    public void handleRetour(ActionEvent actionEvent) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/Dashboard.fxml"));
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Impossible de charger l'écran précédent.").showAndWait();
        }
    }
}
