package controller;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import model.Utilisateur;
import util.FXMLUtils;

/**
 * Contrôleur pour le dashboard administrateur
 */
public class AdminDashboardController {
    
    @FXML
    private Label welcomeLabel;
    
    @FXML
    private Button gestionStockButton;
    
    @FXML
    private Button gestionUtilisateursButton;
    
    @FXML
    private Button deconnexionButton;
    
    private Utilisateur utilisateur;
    
    @FXML
    private void initialize() {
        utilisateur = ConnexionController.getUtilisateurConnecte();
        if (utilisateur != null) {
            welcomeLabel.setText("Bienvenue, " + utilisateur.getUsername() + " (Admin)");
        }
    }
    
    @FXML
    private void handleGestionStock() {
        try {
            Stage stage = (Stage) gestionStockButton.getScene().getWindow();
            FXMLUtils.changeScene(stage, "/view/GestionStock.fxml", "Gestion de Stock");
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", 
                     "Erreur lors du chargement de la gestion de stock: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleGestionUtilisateurs() {
        try {
            Stage stage = (Stage) gestionUtilisateursButton.getScene().getWindow();
            FXMLUtils.changeScene(stage, "/view/GestionUtilisateurs.fxml", "Gestion des Utilisateurs");
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", 
                     "Erreur lors du chargement de la gestion des utilisateurs: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleDeconnexion() {
        ConnexionController.deconnecter();
        try {
            Stage stage = (Stage) deconnexionButton.getScene().getWindow();
            FXMLUtils.changeScene(stage, "/view/Connexion.fxml", "Connexion");
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", 
                     "Erreur lors de la déconnexion: " + e.getMessage());
        }
    }
    
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

