package sn.ouleymatou.hotelmanagement.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import sn.ouleymatou.hotelmanagement.entities.Chambre;
import sn.ouleymatou.hotelmanagement.entities.Reservation;
import sn.ouleymatou.hotelmanagement.services.ChambreService;
import sn.ouleymatou.hotelmanagement.services.ReservationService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class AddReservationDialogController {

    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField telephoneField;
    @FXML private TextField emailField;
    @FXML private DatePicker dateArriveePicker;
    @FXML private DatePicker dateDepartPicker;
    @FXML private Spinner<Integer> nbPersonnesSpinner;
    @FXML private ComboBox<Chambre> chambreComboBox;
    @FXML private Label labelNuits;
    @FXML private Label labelMontant;
    @FXML private TextField acompteField;
    @FXML private Button btnConfirmer;
    @FXML private Button btnSuivant;
    @FXML private Button btnPrecedent;
    @FXML private VBox etape1, etape2, etape3, etape4;

    // Nouveaux Spinner pour les heures et minutes
    @FXML private Spinner<Integer> heureArriveeSpinner;
    @FXML private Spinner<Integer> minuteArriveeSpinner;
    @FXML private Spinner<Integer> heureDepartSpinner;
    @FXML private Spinner<Integer> minuteDepartSpinner;

    private double montantTotal = 0.0;
    private long nbNuits = 0;

    private final ReservationService reservationService = new ReservationService();
    private final ChambreService chambreService = new ChambreService();

    private int currentStep = 1;

    @FXML
    public void initialize() {
        // Spinner nombre personnes 1-10
        nbPersonnesSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 1));

        // Spinner heures (0-23)
        heureArriveeSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 14));
        heureDepartSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 12));

        // Spinner minutes (0-59)
        minuteArriveeSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0));
        minuteDepartSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0));

        // Écouteurs pour DatePicker et Spinner pour mises à jour chambres
        dateArriveePicker.valueProperty().addListener((obs, oldVal, newVal) -> updateChambresDisponibles());
        dateDepartPicker.valueProperty().addListener((obs, oldVal, newVal) -> updateChambresDisponibles());
        nbPersonnesSpinner.valueProperty().addListener((obs, oldVal, newVal) -> updateChambresDisponibles());
        heureArriveeSpinner.valueProperty().addListener((obs, oldVal, newVal) -> updateChambresDisponibles());
        minuteArriveeSpinner.valueProperty().addListener((obs, oldVal, newVal) -> updateChambresDisponibles());
        heureDepartSpinner.valueProperty().addListener((obs, oldVal, newVal) -> updateChambresDisponibles());
        minuteDepartSpinner.valueProperty().addListener((obs, oldVal, newVal) -> updateChambresDisponibles());

        // Écouteur chambre sélectionnée pour calcul montant
        chambreComboBox.valueProperty().addListener((obs, oldVal, newVal) -> calculer());

        // Afficher étape 1 au démarrage
        showEtape(1);

        // Bouton précédent désactivé au départ
        btnPrecedent.setDisable(true);

        // Masquer bouton confirmer au départ (sera visible en dernière étape)
        btnConfirmer.setVisible(false);
    }

    private void updateChambresDisponibles() {
        try {
            LocalDate arriveeDate = dateArriveePicker.getValue();
            LocalDate departDate = dateDepartPicker.getValue();
            int nbPersonnes = nbPersonnesSpinner.getValue();

            if (arriveeDate != null && departDate != null && departDate.isAfter(arriveeDate)) {
                LocalDateTime arrivee = arriveeDate.atTime(heureArriveeSpinner.getValue(), minuteArriveeSpinner.getValue());
                LocalDateTime depart = departDate.atTime(heureDepartSpinner.getValue(), minuteDepartSpinner.getValue());

                List<Chambre> disponibles = chambreService.getChambresDisponibles(arrivee, depart);
                List<Chambre> filtrees = disponibles.stream()
                        .filter(c -> c.getCapacite() >= nbPersonnes)
                        .toList();

                chambreComboBox.getItems().setAll(filtrees);
                chambreComboBox.getSelectionModel().selectFirst();
                calculer();

                if (filtrees.isEmpty()) {
                    showAlert("Aucune chambre disponible",
                            "Aucune chambre n’est disponible pour cette période et ce nombre de personnes.",
                            Alert.AlertType.WARNING);
                }
            } else {
                chambreComboBox.getItems().clear();
                labelMontant.setText("");
                labelNuits.setText("");
            }
        } catch (Exception e) {
            chambreComboBox.getItems().clear();
            labelMontant.setText("");
            labelNuits.setText("");
        }
    }

    private void calculer() {
        try {
            LocalDate arriveeDate = dateArriveePicker.getValue();
            LocalDate departDate = dateDepartPicker.getValue();
            Chambre chambre = chambreComboBox.getValue();

            if (arriveeDate != null && departDate != null && chambre != null && departDate.isAfter(arriveeDate)) {
                nbNuits = ChronoUnit.DAYS.between(arriveeDate, departDate);
                montantTotal = nbNuits * chambre.getTarifParNuit();
                labelNuits.setText(nbNuits + " nuit(s)");
                labelMontant.setText(String.format("%.2f FCFA", montantTotal));
            } else {
                labelNuits.setText("");
                labelMontant.setText("");
            }
        } catch (Exception e) {
            labelNuits.setText("");
            labelMontant.setText("");
        }
    }

    @FXML
    private void handleSuivant() {
        boolean valide;
        switch (currentStep) {
            case 1 -> valide = validerEtape1();
            case 2 -> valide = validerEtape2();
            case 3 -> valide = validerEtape3();
            default -> valide = true;
        }
        if (!valide) return;

        if (currentStep < 4) {
            showEtape(currentStep + 1);
        }
    }

    @FXML
    private void handlePrecedent() {
        if (currentStep > 1) {
            showEtape(currentStep - 1);
        }
    }

    private void showEtape(int etape) {
        etape1.setVisible(etape == 1);
        etape2.setVisible(etape == 2);
        etape3.setVisible(etape == 3);
        etape4.setVisible(etape == 4);
        currentStep = etape;

        btnPrecedent.setDisable(currentStep == 1);
        btnSuivant.setVisible(currentStep != 4);
        btnConfirmer.setVisible(currentStep == 4);
    }

    private boolean validerEtape1() {
        if (nomField.getText().trim().length() < 2) {
            showAlert("Nom invalide", "Le nom doit contenir au moins 2 caractères.", Alert.AlertType.ERROR);
            return false;
        }
        if (prenomField.getText().trim().length() < 2) {
            showAlert("Prénom invalide", "Le prénom doit contenir au moins 2 caractères.", Alert.AlertType.ERROR);
            return false;
        }
        if (!telephoneField.getText().trim().matches("^(77|78|75|76|70)\\d{7}$")) {
            showAlert("Téléphone invalide", "Le téléphone doit commencer par 77,78,75,76 ou 70 et contenir 9 chiffres.", Alert.AlertType.ERROR);
            return false;
        }
        if (!emailField.getText().trim().isEmpty() && !emailField.getText().trim().matches("^.+@.+\\..+$")) {
            showAlert("Email invalide", "Veuillez entrer un email valide.", Alert.AlertType.ERROR);
            return false;
        }
        return true;
    }

    private boolean validerEtape2() {
        LocalDate arriveeDate = dateArriveePicker.getValue();
        LocalDate departDate = dateDepartPicker.getValue();
        if (arriveeDate == null || departDate == null) {
            showAlert("Date invalide", "Veuillez sélectionner les dates d'arrivée et de départ.", Alert.AlertType.ERROR);
            return false;
        }
        if (arriveeDate.isBefore(LocalDate.now())) {
            showAlert("Date invalide", "La date d'arrivée doit être dans le futur.", Alert.AlertType.ERROR);
            return false;
        }
        if (!departDate.isAfter(arriveeDate)) {
            showAlert("Date invalide", "La date de départ doit être après la date d'arrivée.", Alert.AlertType.ERROR);
            return false;
        }
        return true;
    }

    private boolean validerEtape3() {
        if (chambreComboBox.getValue() == null) {
            showAlert("Chambre non sélectionnée", "Veuillez sélectionner une chambre.", Alert.AlertType.ERROR);
            return false;
        }
        if (nbPersonnesSpinner.getValue() > chambreComboBox.getValue().getCapacite()) {
            showAlert("Capacité dépassée", "La chambre ne peut pas accueillir autant de personnes.", Alert.AlertType.ERROR);
            return false;
        }
        return true;
    }

    private boolean validerEtape4() {
        double acompte;
        try {
            acompte = Double.parseDouble(acompteField.getText().trim());
        } catch (NumberFormatException e) {
            showAlert("Acompte invalide", "Veuillez entrer un montant d'acompte valide.", Alert.AlertType.ERROR);
            return false;
        }
        if (acompte < montantTotal * 0.3) {
            showAlert("Acompte insuffisant", "L'acompte doit être au moins 30% du montant total.", Alert.AlertType.ERROR);
            return false;
        }
        return true;
    }

    @FXML
    public void handleReserver() {
        if (!validerEtape4()) return;

        try {
            Reservation reservation = new Reservation();
            reservation.setNumero(reservationService.genererNumeroReservation());
            reservation.setDateReservation(LocalDateTime.now());
            reservation.setNomClient(nomField.getText().trim());
            reservation.setPrenomClient(prenomField.getText().trim());
            reservation.setTelephone(telephoneField.getText().trim());
            reservation.setEmail(emailField.getText().trim());
            reservation.setDateArrivee(dateArriveePicker.getValue().atTime(
                    heureArriveeSpinner.getValue(), minuteArriveeSpinner.getValue()));
            reservation.setDateDepart(dateDepartPicker.getValue().atTime(
                    heureDepartSpinner.getValue(), minuteDepartSpinner.getValue()));
            reservation.setNombreNuits((int) nbNuits);
            reservation.setMontantTotal(montantTotal);
            reservation.setNombrePersonnes(nbPersonnesSpinner.getValue());
            reservation.setAcompte(Double.parseDouble(acompteField.getText().trim()));
            reservation.setStatut(Reservation.StatutReservation.CONFIRMEE);

            Chambre chambre = chambreComboBox.getValue();
            reservation.setChambre(chambre);

            if (reservation.getDateArrivee().toLocalDate().equals(LocalDate.now())) {
                chambre.setStatut(Chambre.StatutChambre.OCCUPEE);
                chambreService.save(chambre);
            }

            reservationService.enregistrerReservation(reservation);

            showAlert("Succès", "Réservation enregistrée avec succès !", Alert.AlertType.INFORMATION);
            Stage stage = (Stage) btnConfirmer.getScene().getWindow();
            stage.close();
        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors de l'enregistrement : " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String titre, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
