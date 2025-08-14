package sn.ouleymatou.hotelmanagement.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import sn.ouleymatou.hotelmanagement.HelloApplication;
import sn.ouleymatou.hotelmanagement.entities.User;
import sn.ouleymatou.hotelmanagement.services.UserService;

import java.io.IOException;

public class LoginController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField passwordVisibleField;

    @FXML
    private CheckBox showPasswordCheckBox;

    @FXML
    private Label errorLabel;

    @FXML
    private Button loginBtn;

    private final UserService userService;

    public LoginController() {
        this.userService = new UserService();
    }

    /**
     * Affiche la vue login
     */
    public static void showLoginView(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("/fxml/login-view.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        stage.setTitle("Connexion - Royal Palace Hotel");
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Initialise les composants du formulaire
     */
    @FXML
    private void initialize() {
        // Masquer le champ visible par défaut
        passwordVisibleField.setVisible(false);
        passwordVisibleField.managedProperty().bind(showPasswordCheckBox.selectedProperty());
        passwordField.managedProperty().bind(showPasswordCheckBox.selectedProperty().not());
        passwordVisibleField.visibleProperty().bind(showPasswordCheckBox.selectedProperty());
        passwordField.visibleProperty().bind(showPasswordCheckBox.selectedProperty().not());


        // Lier les champs mot de passe ensemble
        passwordVisibleField.textProperty().bindBidirectional(passwordField.textProperty());

        // Cacher le message d'erreur au début
        errorLabel.setVisible(false);
    }

    /**
     * Gère l'action de connexion
     */
    @FXML
    private void handleLogin(ActionEvent event) {
        UserService userService = new UserService();
        userService.createDefaultAdminIfNotExists();

        errorLabel.setVisible(false);

        String email = emailField.getText().trim();
        String password = passwordField.getText().trim(); // Champ lié avec visible

        if (email.isEmpty() || password.isEmpty()) {
            showError("Veuillez remplir tous les champs.");
            return;
        }

        try {
            User user = userService.authenticate(email, password);
            if (user != null) {
                navigateToDashboard(event, user);
            } else {
                showError("Email ou mot de passe incorrect.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur de connexion. Veuillez réessayer.");
        }
    }

    /**
     * Affiche un message d'erreur
     */
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    /**
     * Redirige vers le dashboard avec l'utilisateur connecté
     */
    private void navigateToDashboard(ActionEvent event, User user) throws IOException {
        FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("/fxml/dashboard-view.fxml"));
        Parent root = loader.load();

        DashboardController controller = loader.getController();
        controller.setCurrentUser(user); // Passage de l'utilisateur

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setTitle("Dashboard - Royal Palace Hotel");
        stage.setScene(new Scene(root));
        stage.show();
    }
}
