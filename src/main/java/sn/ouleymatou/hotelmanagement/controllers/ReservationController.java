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
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;
import sn.ouleymatou.hotelmanagement.entities.Reservation;
import sn.ouleymatou.hotelmanagement.services.ReservationService;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

public class ReservationController implements Initializable {

    @FXML private TableView<Reservation> tableAllReservations;
    @FXML private TableView<Reservation> tableArriveesDuJour;
    @FXML private TableView<Reservation> tableDepartsDuJour;

    @FXML private TabPane tabPaneReservations;

    @FXML private DatePicker dateDebut;
    @FXML private DatePicker dateFin;
    @FXML private ComboBox<String> statutFilter;
    @FXML private TextField recherche;

    @FXML private Label lblTotalReservations;
    @FXML private Label lblArriveesAujourdHui;
    @FXML private Label lblDepartsAujourdHui;
    @FXML private Label lblChiffreAffaires;

    @FXML private Button btnNouvelleReservation;

    private final ReservationService reservationService = new ReservationService();

    private final ObservableList<Reservation> allReservations = FXCollections.observableArrayList();
    private final ObservableList<Reservation> arriveesDuJour = FXCollections.observableArrayList();
    private final ObservableList<Reservation> departsDuJour = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialiser colonnes pour toutes les tables
        initialiserColonnes(tableAllReservations, true, "All");
        initialiserColonnes(tableArriveesDuJour, false, "Arrivees");
        initialiserColonnes(tableDepartsDuJour, false, "Departs");

        tableAllReservations.setItems(allReservations);
        tableArriveesDuJour.setItems(arriveesDuJour);
        tableDepartsDuJour.setItems(departsDuJour);

        statutFilter.setItems(FXCollections.observableArrayList("Tous", "Confirmée", "Annulée", "Check-in", "Check-out"));
        statutFilter.setValue("Tous");

        // Charger les données
        chargerToutesReservations();
        chargerArriveesDuJour();
        chargerDepartsDuJour();
        chargerStatistiques();
    }

    private void initialiserColonnes(TableView<Reservation> table, boolean avecActions, String suffixe) {
        TableColumn<Reservation, String> colNum = new TableColumn<>("N° Reservation");
        colNum.setCellValueFactory(new PropertyValueFactory<>("numero"));
        colNum.setId("colNumReservation" + suffixe);

        TableColumn<Reservation, String> colClient = new TableColumn<>("Client");
        colClient.setCellValueFactory(new PropertyValueFactory<>("nomCompletClient"));
        colClient.setId("colClient" + suffixe);

        TableColumn<Reservation, String> colChambre = new TableColumn<>("Chambre");
        colChambre.setCellValueFactory(new PropertyValueFactory<>("numeroChambre"));
        colChambre.setId("colChambre" + suffixe);

        TableColumn<Reservation, String> colArrivee = new TableColumn<>("Arrivée");
        colArrivee.setCellValueFactory(new PropertyValueFactory<>("dateArriveeString"));
        colArrivee.setId("colArrivee" + suffixe);

        TableColumn<Reservation, String> colDepart = new TableColumn<>("Départ");
        colDepart.setCellValueFactory(new PropertyValueFactory<>("dateDepartString"));
        colDepart.setId("colDepart" + suffixe);

        TableColumn<Reservation, Integer> colNuits = new TableColumn<>("Nuits");
        colNuits.setCellValueFactory(new PropertyValueFactory<>("nombreNuits"));
        colNuits.setId("colNuits" + suffixe);

        TableColumn<Reservation, Double> colMontant = new TableColumn<>("Montant");
        colMontant.setCellValueFactory(new PropertyValueFactory<>("montantTotal"));
        colMontant.setId("colMontant" + suffixe);

        table.getColumns().setAll(colNum, colClient, colChambre, colArrivee, colDepart, colNuits, colMontant);

        if (avecActions) {
            TableColumn<Reservation, Void> colActions = new TableColumn<>("Actions");
            colActions.setCellFactory(creerCellFactoryActions());
            colActions.setId("colActions" + suffixe);
            table.getColumns().add(colActions);
        }
    }

    private Callback<TableColumn<Reservation, Void>, TableCell<Reservation, Void>> creerCellFactoryActions() {
        return param -> new TableCell<>() {
            private final Button btnCheckIn = new Button("Check-in");
            private final Button btnCheckOut = new Button("Check-out");

            {
                btnCheckIn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");
                btnCheckOut.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white;");

                btnCheckIn.setOnAction(event -> {
                    Reservation data = getTableView().getItems().get(getIndex());
                    try {
                        reservationService.checkIn(data);
                        chargerToutesReservations();
                        chargerArriveesDuJour();
                        chargerDepartsDuJour();
                        chargerStatistiques();
                    } catch (Exception e) {
                        new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait();
                    }
                });

                btnCheckOut.setOnAction(event -> {
                    Reservation data = getTableView().getItems().get(getIndex());
                    try {
                        reservationService.checkOut(data);
                        chargerToutesReservations();
                        chargerArriveesDuJour();
                        chargerDepartsDuJour();
                        chargerStatistiques();
                    } catch (Exception e) {
                        new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait();
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox hbox = new HBox(5);
                    Reservation res = getTableView().getItems().get(getIndex());
                    LocalDate today = LocalDate.now();

                    if (res.getStatut() == Reservation.StatutReservation.CONFIRMEE &&
                            res.getDateArrivee().toLocalDate().equals(today)) {
                        btnCheckIn.setDisable(false);
                        hbox.getChildren().add(btnCheckIn);
                    } else if (res.getStatut() == Reservation.StatutReservation.EN_COURS &&
                            res.getDateDepart().toLocalDate().equals(today)) {
                        btnCheckOut.setDisable(false);
                        hbox.getChildren().add(btnCheckOut);
                    }

                    setGraphic(hbox);
                }
            }
        };
    }

    private void chargerToutesReservations() {
        List<Reservation> reservations = reservationService.getAllReservations();
        allReservations.setAll(reservations);
    }

    private void chargerArriveesDuJour() {
        List<Reservation> reservations = reservationService.getArriveesDuJour();
        arriveesDuJour.setAll(reservations);
    }

    private void chargerDepartsDuJour() {
        List<Reservation> reservations = reservationService.getDepartsDuJour();
        departsDuJour.setAll(reservations);
    }

    private void chargerStatistiques() {
        lblTotalReservations.setText(reservationService.getTotalReservations() + " Total Réservations");
        lblArriveesAujourdHui.setText(reservationService.getArriveesDuJour().size() + " Arrivées Aujourd'hui");
        lblDepartsAujourdHui.setText(reservationService.getDepartsDuJour().size() + " Départs Aujourd'hui");
        lblChiffreAffaires.setText(String.format("%,.0f FCFA CA Aujourd'hui", reservationService.getChiffreAffairesDuJour()));
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

            chargerToutesReservations();
            chargerArriveesDuJour();
            chargerDepartsDuJour();
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
