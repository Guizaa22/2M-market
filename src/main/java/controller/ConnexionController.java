package controller;

import java.io.IOException;

import dao.UtilisateurDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.Utilisateur;
import util.FXMLUtils;

/**
 * Contrôleur pour l'interface de connexion
 */
public class ConnexionController {
    
    @FXML
    private TextField usernameField;
    
    @FXML
    private PasswordField passwordField;
    
    private final UtilisateurDAO utilisateurDAO;
    private static Utilisateur utilisateurConnecte;
    
    public ConnexionController() {
        utilisateurDAO = new UtilisateurDAO();
    }
    
    @FXML
    private void initialize() {
        // Focus sur le champ username au démarrage
        usernameField.requestFocus();
    }
    
    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        
        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Champs vides", 
                     "Veuillez remplir tous les champs.");
            return;
        }
        
        Utilisateur utilisateur = utilisateurDAO.authenticate(username, password);
        
        if (utilisateur != null) {
            utilisateurConnecte = utilisateur;
            
            try {
                Stage stage = (Stage) usernameField.getScene().getWindow();
                
                if (utilisateur.isAdmin()) {
                    // Rediriger vers le dashboard admin
                    FXMLUtils.changeScene(stage, "/view/AdminDashboard.fxml", "Dashboard Administrateur");
                } else {
                    // Rediriger vers la caisse pour les employés
                    FXMLUtils.changeScene(stage, "/view/Caisse.fxml", "Caisse - Point de Vente");
                }
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", 
                         "Erreur lors du chargement de l'interface: " + e.getMessage());
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "Échec de connexion", 
                     "Nom d'utilisateur ou mot de passe incorrect.");
            passwordField.clear();
        }
    }
    
    @FXML
    private void handleEnterKey() {
        handleLogin();
    }
    
    public static Utilisateur getUtilisateurConnecte() {
        return utilisateurConnecte;
    }
    
    public static void deconnecter() {
        utilisateurConnecte = null;
    }
    
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

