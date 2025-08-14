package sn.ouleymatou.hotelmanagement.utils;

import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import sn.ouleymatou.hotelmanagement.entities.Chambre;

import java.io.FileWriter;
import java.io.IOException;

public class ExportUtil {

    public static <T> void exportTableViewToCSV(TableView<T> tableView, String filePath) {
        try (FileWriter writer = new FileWriter(filePath)) {
            for (TableColumn<T, ?> column : tableView.getColumns()) {
                writer.write(column.getText() + ",");
            }
            writer.write("\n");

            ObservableList<T> items = tableView.getItems();
            for (T item : items) {
                for (TableColumn<T, ?> column : tableView.getColumns()) {
                    Object cellData = column.getCellData(item);
                    writer.write((cellData != null ? cellData.toString() : "") + ",");
                }
                writer.write("\n");
            }

            showAlert("Export réussi", "Les données ont été exportées avec succès au format CSV.");
        } catch (IOException e) {
            showAlert("Erreur d'export", "Impossible d'exporter les données : " + e.getMessage());
        }
    }

    private static void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void exportToExcel(ObservableList<Chambre> items, String listeDesChambres) {
    }

    public static void exportToPDF(ObservableList<Chambre> items, String listeDesChambres) {
    }
}
