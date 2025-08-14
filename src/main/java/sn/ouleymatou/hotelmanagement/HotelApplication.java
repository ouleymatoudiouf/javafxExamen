package sn.ouleymatou.hotelmanagement;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import sn.ouleymatou.hotelmanagement.services.UserService;

public class HotelApplication extends Application {

    public static void showLoginHome(Stage stage) {
        // Tu peux ajouter ici plus tard la logique pour revenir à la page login si besoin
    }

    @Override
    public void start(Stage stage) throws Exception {
        // Création de l'utilisateur admin s'il n'existe pas
        UserService userService = new UserService();
        userService.createDefaultAdminIfNotExists();

        // Chargement de la vue de connexion
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/login-view.fxml"));
        stage.setTitle("ROYAL ISI PALACE HOTEL");
        stage.setScene(new Scene(root));
        stage.show();
    }

    public static void main(String[] args) {
        launch(args); // Lance l'application JavaFX
    }
}
