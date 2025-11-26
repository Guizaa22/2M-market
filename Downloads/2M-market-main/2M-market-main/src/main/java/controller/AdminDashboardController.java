package controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import model.Utilisateur;
import model.Produit;
import dao.ProduitDAO;
import dao.VenteDAO;
import util.FXMLUtils;

/**
 * Contrôleur pour le dashboard administrateur
 */
public class AdminDashboardController {
    
    @FXML
    private Label welcomeLabel;

    @FXML
    private Button gestionVentesButton;


    @FXML
    private Button gestionStockButton;
    
    @FXML
    private Button gestionUtilisateursButton;
    
    @FXML
    private Button gestionTabacButton;
    
    @FXML
    private Button visualisationProduitsButton;
    
    @FXML
    private Button deconnexionButton;
    
    @FXML
    private Label totalProduitsStatLabel;
    
    @FXML
    private Label rupturesStatLabel;
    
    @FXML
    private Label ventesJourStatLabel;
    
    private Utilisateur utilisateur;
    private ProduitDAO produitDAO;
    private VenteDAO venteDAO;
    
    @FXML
    private void initialize() {
        utilisateur = ConnexionController.getUtilisateurConnecte();
        if (utilisateur != null) {
            welcomeLabel.setText("Bienvenue, " + utilisateur.getUsername() + " (Admin)");
        }
        produitDAO = new ProduitDAO();
        venteDAO = new VenteDAO();
        rafraichirStatistiques();
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
    @FXML
    private void handleGestionVentes() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/GestionVentes.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) gestionVentesButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Gestion des Ventes");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleGestionTabac() {
        try {
            Stage stage = (Stage) gestionTabacButton.getScene().getWindow();
            FXMLUtils.changeScene(stage, "/view/GestionTabac.fxml", "Caisse Tabac - Ventes Tabac/Puff/Terrea");
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", 
                     "Erreur lors du chargement de la gestion des ventes de tabac: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleVisualisationProduits() {
        try {
            Stage stage = (Stage) visualisationProduitsButton.getScene().getWindow();
            FXMLUtils.changeScene(stage, "/view/VisualisationProduits.fxml", "Visualisation des Produits");
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", 
                     "Erreur lors du chargement de la visualisation des produits: " + e.getMessage());
        }
    }

    private void rafraichirStatistiques() {
        List<Produit> produits = produitDAO.findAll();
        long produitsDisponibles = produits.stream()
                .filter(p -> p.getQuantiteStock() > 0)
                .count();
        long ruptures = produits.stream()
                .filter(p -> p.getQuantiteStock() == 0)
                .count();
        totalProduitsStatLabel.setText(String.valueOf(produitsDisponibles));
        rupturesStatLabel.setText(String.valueOf(ruptures));
        
        LocalDateTime debutJour = LocalDateTime.now()
                .with(LocalTime.MIN);
        LocalDateTime finJour = LocalDateTime.now()
                .with(LocalTime.MAX);
        BigDecimal ventesJour = venteDAO.getTotalRecettes(debutJour, finJour);
        ventesJourStatLabel.setText(String.format("%.2f DT", ventesJour));
    }
}

