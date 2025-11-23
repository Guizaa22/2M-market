package controller;

import dao.ProduitDAO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

/**
 * Contrôleur pour l'interface de sélection de catégories
 */
public class CaisseCategoriesController {
    
    @FXML
    private FlowPane categoriesContainer;
    
    @FXML
    private Button retourButton;
    
    private ProduitDAO produitDAO;
    
    @FXML
    private void initialize() {
        produitDAO = new ProduitDAO();
        // Ajouter le CSS après que la scène soit chargée
        javafx.application.Platform.runLater(() -> {
            if (retourButton != null && retourButton.getScene() != null) {
                javafx.scene.Parent root = retourButton.getScene().getRoot();
                if (root != null) {
                    String cssUrl = getClass().getResource("/styles/caisse.css").toExternalForm();
                    if (!root.getStylesheets().contains(cssUrl)) {
                        root.getStylesheets().add(cssUrl);
                    }
                }
            }
        });
        chargerCategories();
    }
    
    private void chargerCategories() {
        categoriesContainer.getChildren().clear();
        categoriesContainer.setHgap(20);
        categoriesContainer.setVgap(20);
        categoriesContainer.setPadding(new Insets(20));
        
        List<String> categories = produitDAO.findAllCategories();
        
        // Si aucune catégorie, ajouter des catégories par défaut
        if (categories.isEmpty()) {
            categories.add("Alimentaire");
            categories.add("Boissons");
            categories.add("Tabac");
            categories.add("Hygiène");
            categories.add("Divers");
        }
        
        for (String categorie : categories) {
            Button categoryButton = createCategoryButton(categorie);
            categoriesContainer.getChildren().add(categoryButton);
        }
    }
    
    private Button createCategoryButton(String categorie) {
        Button button = new Button(categorie);
        button.setPrefSize(200, 150);
        button.setStyle(
            "-fx-font-size: 18px; " +
            "-fx-font-weight: bold; " +
            "-fx-background-color: linear-gradient(to bottom, #4CAF50, #45a049); " +
            "-fx-text-fill: white; " +
            "-fx-background-radius: 15; " +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 10, 0, 0, 5); " +
            "-fx-cursor: hand;"
        );
        
        // Effet hover
        button.setOnMouseEntered(e -> {
            button.setStyle(
                "-fx-font-size: 20px; " +
                "-fx-font-weight: bold; " +
                "-fx-background-color: linear-gradient(to bottom, #66BB6A, #4CAF50); " +
                "-fx-text-fill: white; " +
                "-fx-background-radius: 15; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.5), 15, 0, 0, 8); " +
                "-fx-scale-x: 1.05; " +
                "-fx-scale-y: 1.05; " +
                "-fx-cursor: hand;"
            );
        });
        
        button.setOnMouseExited(e -> {
            button.setStyle(
                "-fx-font-size: 18px; " +
                "-fx-font-weight: bold; " +
                "-fx-background-color: linear-gradient(to bottom, #4CAF50, #45a049); " +
                "-fx-text-fill: white; " +
                "-fx-background-radius: 15; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 10, 0, 0, 5); " +
                "-fx-scale-x: 1.0; " +
                "-fx-scale-y: 1.0; " +
                "-fx-cursor: hand;"
            );
        });
        
        button.setOnAction(e -> ouvrirCategorie(categorie));
        
        return button;
    }
    
    private void ouvrirCategorie(String categorie) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/CategorieProduits.fxml"));
            VBox root = loader.load();
            
            CategorieProduitsController controller = loader.getController();
            controller.setCategorie(categorie);
            
            Stage stage = (Stage) retourButton.getScene().getWindow();
            stage.getScene().setRoot(root);
            stage.setTitle("Produits - " + categorie);
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement de la page catégorie: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleRetour() {
        try {
            Stage stage = (Stage) retourButton.getScene().getWindow();
            util.FXMLUtils.changeScene(stage, "/view/Caisse.fxml", "Caisse - Point de Vente");
        } catch (Exception e) {
            System.err.println("Erreur lors du retour: " + e.getMessage());
        }
    }
}

