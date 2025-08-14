package sn.ouleymatou.hotelmanagement.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import sn.ouleymatou.hotelmanagement.entities.Chambre;
import sn.ouleymatou.hotelmanagement.services.ChambreService;
import sn.ouleymatou.hotelmanagement.services.ReservationService;

import java.time.LocalDate;
import java.util.Map;

public class StatistiquesController {

    @FXML private DatePicker dateDebutPicker;
    @FXML private DatePicker dateFinPicker;

    @FXML private Label chiffreAffairesLabel;
    @FXML private Label tauxOccupationLabel;
    @FXML private Label chambrePlusReserveeLabel;
    @FXML private Label chambreMoinsReserveeLabel;
    @FXML private Label typeChambrePlusDemandeLabel;
    @FXML private Label clientPlusFideleLabel;
    @FXML private Label dureeMoyenneLabel;
    @FXML private Label annulationsLabel;
    @FXML private Label nbNuitsVenduesLabel;

    @FXML private LineChart<String, Number> lineChartReservations;
    @FXML private PieChart pieChartTypeChambre;
    @FXML private BarChart<String, Number> barChartOccupation;

    private final ReservationService reservationService = new ReservationService();
    private final ChambreService chambreService = new ChambreService();

    @FXML
    public void initialize() {
        LocalDate debut = LocalDate.of(2000, 1, 1);
        LocalDate fin = LocalDate.now();
        dateDebutPicker.setValue(debut);
        dateFinPicker.setValue(fin);
        chargerStatistiques(debut, fin);
    }

    @FXML
    private void appliquerFiltre() {
        LocalDate debut = dateDebutPicker.getValue();
        LocalDate fin = dateFinPicker.getValue();

        if (debut == null || fin == null || fin.isBefore(debut)) {
            showAlert("Erreur", "Veuillez sélectionner une période valide", AlertType.ERROR);
            return;
        }
        chargerStatistiques(debut, fin);
    }

    private void chargerStatistiques(LocalDate debut, LocalDate fin) {
        // Statistiques
        chiffreAffairesLabel.setText(String.format("%.2f FCFA", reservationService.calculChiffreAffaires(debut, fin)));
        tauxOccupationLabel.setText(String.format("%.2f %%", chambreService.calculTauxOccupation(debut, fin)));

        Chambre cMax = chambreService.getChambreLaPlusReservee(debut, fin);
        chambrePlusReserveeLabel.setText(cMax != null ? cMax.getNumero() : "-");

        Chambre cMin = chambreService.getChambreLaMoinsReservee(debut, fin);
        chambreMoinsReserveeLabel.setText(cMin != null ? cMin.getNumero() : "-");

        typeChambrePlusDemandeLabel.setText(chambreService.getTypeChambreLePlusDemande(debut, fin));
        clientPlusFideleLabel.setText(reservationService.getClientLePlusFidele(debut, fin));
        dureeMoyenneLabel.setText(String.format("%.1f nuit(s)", reservationService.getDureeMoyenneSejour(debut, fin)));
        annulationsLabel.setText(String.valueOf(reservationService.getNombreAnnulations(debut, fin)));
        nbNuitsVenduesLabel.setText(String.valueOf(reservationService.getNombreNuitsVendues(debut, fin)));

        // Graphiques
        remplirLineChartReservations(debut, fin);
        remplirPieChartTypeChambre(debut, fin);
        remplirBarChartOccupation(debut, fin);
    }

    private void remplirLineChartReservations(LocalDate debut, LocalDate fin) {
        lineChartReservations.getData().clear();
        XYChart.Series<String, Number> serie = new XYChart.Series<>();
        serie.setName("Réservations");
        Map<String, Integer> data = reservationService.getNombreReservationsParMois(debut, fin);
        data.forEach((mois, nb) -> serie.getData().add(new XYChart.Data<>(mois, nb)));
        lineChartReservations.getData().add(serie);
    }

    private void remplirPieChartTypeChambre(LocalDate debut, LocalDate fin) {
        pieChartTypeChambre.getData().clear();
        Map<String, Integer> parType = chambreService.getReservationsParTypeChambre(debut, fin);
        ObservableList<PieChart.Data> items = FXCollections.observableArrayList();
        parType.forEach((type, nb) -> items.add(new PieChart.Data(type, nb)));
        pieChartTypeChambre.setData(items);
    }

    private void remplirBarChartOccupation(LocalDate debut, LocalDate fin) {
        barChartOccupation.getData().clear();
        XYChart.Series<String, Number> serie = new XYChart.Series<>();
        serie.setName("Taux d'occupation");
        Map<String, Double> tauxParMois = chambreService.getTauxOccupationParMois(debut, fin);
        tauxParMois.forEach((mois, taux) -> serie.getData().add(new XYChart.Data<>(mois, taux)));
        barChartOccupation.getData().add(serie);
    }

    // Bouton "Exporter PDF" présent dans le FXML : on évite une erreur si iText n'est pas configuré
    @FXML
    private void exporterPDF() {
        showAlert("Information",
                "L'export PDF est désactivé pour le moment.\n" +
                        "Réactive-le quand iText sera correctement configuré (modules et dépendances).",
                AlertType.INFORMATION);
    }

    private void showAlert(String titre, String message, AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
