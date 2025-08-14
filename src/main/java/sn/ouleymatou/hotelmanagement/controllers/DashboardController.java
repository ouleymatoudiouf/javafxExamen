package sn.ouleymatou.hotelmanagement.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import sn.ouleymatou.hotelmanagement.entities.User;
import sn.ouleymatou.hotelmanagement.services.ChambreService;
import sn.ouleymatou.hotelmanagement.services.ReservationService;

import java.io.IOException;

public class DashboardController {

    @FXML private Button logoutBtn;
    @FXML private Label welcomeLabel;

    @FXML private Label labelTotalChambresValue;
    @FXML private Label labelReservationsActivesValue;
    @FXML private Label labelTauxOccupationValue;

    @FXML private VBox boxChambres;
    @FXML private VBox boxReservations;
    @FXML private VBox boxStatistiques;

    private final ChambreService chambreService = new ChambreService();
    private final ReservationService reservationService = new ReservationService();

    private User currentUser;

    @FXML
    private void initialize() {
        try {
            long totalChambres = chambreService.count();
            long total = reservationService.getTotalReservations();

            double tauxOccupation = reservationService.calculerTauxOccupation();

            labelTotalChambresValue.setText(String.valueOf(totalChambres));
            labelReservationsActivesValue.setText(String.valueOf(total));
            labelTauxOccupationValue.setText(String.format("%.0f%%", tauxOccupation));
        } catch (Exception e) {
            e.printStackTrace();
            labelTotalChambresValue.setText("Erreur");
            labelReservationsActivesValue.setText("Erreur");
            labelTauxOccupationValue.setText("Erreur");
        }

        // Ajouter actions sur les boîtes cliquables
        boxChambres.setOnMouseClicked(this::ouvrirGestionChambres);
        boxReservations.setOnMouseClicked(this::ouvrirGestionReservations);
        boxStatistiques.setOnMouseClicked(this::ouvrirStatistiques);

        logoutBtn.setOnAction(event -> deconnexion());
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (user != null && user.getEmail() != null) {
            welcomeLabel.setText("Bienvenue, " + user.getEmail());
        } else {
            welcomeLabel.setText("Bienvenue, Utilisateur");
        }
    }

    private void ouvrirGestionChambres(MouseEvent event) {
        chargerVue("/fxml/chambre-view.fxml", "Gestion des Chambres");
    }

    private void ouvrirGestionReservations(MouseEvent event) {
        chargerVue("/fxml/reservationsView.fxml", "Gestion des Réservations");
    }

    private void ouvrirStatistiques(MouseEvent event) {
        chargerVue("/fxml/statistiques-view.fxml", "Statistiques");
    }

    private void chargerVue(String fxmlPath, String titre) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            // Exemple si tu veux récupérer le contrôleur
            Object controller = loader.getController();
            if(controller instanceof ReservationController rc){
                // tu peux initialiser des données si nécessaire
            }

            Stage stage = new Stage();
            stage.setTitle(titre);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void deconnexion() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/login-view.fxml"));
            Stage loginStage = new Stage();
            loginStage.setTitle("Connexion");
            loginStage.setScene(new Scene(root));
            loginStage.show();

            Stage currentStage = (Stage) logoutBtn.getScene().getWindow();
            currentStage.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Méthodes des boutons (pour d’autres événements)
    @FXML private void handleChambres() { ouvrirGestionChambres(null); }
    @FXML private void handleReservations() { ouvrirGestionReservations(null); }
    @FXML private void handleStatistiques() { ouvrirGestionStatistiques(); }
    @FXML private void handleLogout() { deconnexion(); }

    private void ouvrirGestionStatistiques() {
        chargerVue("/fxml/statistiques-view.fxml", "Statistiques");
    }
}
