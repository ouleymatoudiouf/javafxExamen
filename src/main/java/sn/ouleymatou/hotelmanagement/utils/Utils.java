package sn.ouleymatou.hotelmanagement.utils;

import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Optional;

public class Utils {

    // ✅ Ancienne méthode conservée (alias de showInfoAlert)
    public static void showAlert(String title, String message) {
        showInfoAlert(title, message);
    }

    // ✅ Alerte d'information
    public static void showInfoAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // ❌ Alerte d’erreur
    public static void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText("Une erreur est survenue");
        alert.setContentText(message);
        alert.showAndWait();
    }

    // ⚠️ Alerte d’avertissement
    public static void showWarningAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText("Attention");
        alert.setContentText(message);
        alert.showAndWait();
    }

    // ✅ Confirmation avec retour booléen
    public static boolean showConfirmation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText("Confirmation");
        alert.setContentText(message);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    // ✅ Export TableView vers CSV
    public static <T> void exportTableViewToCSV(TableView<T> tableView, String filePath) {
        try (FileWriter writer = new FileWriter(filePath)) {
            // En-têtes
            for (TableColumn<T, ?> column : tableView.getColumns()) {
                writer.write(column.getText() + ",");
            }
            writer.write("\n");

            // Données
            ObservableList<T> items = tableView.getItems();
            for (T item : items) {
                for (TableColumn<T, ?> column : tableView.getColumns()) {
                    Object cellData = column.getCellData(item);
                    writer.write((cellData != null ? cellData.toString() : "") + ",");
                }
                writer.write("\n");
            }

            showInfoAlert("Export réussi", "Les données ont été exportées avec succès au format CSV.");
        } catch (IOException e) {
            showErrorAlert("Erreur d'export", "Impossible d'exporter les données : " + e.getMessage());
        }
    }
}
