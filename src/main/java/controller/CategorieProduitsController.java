package controller;

import dao.ProduitDAO;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.DetailVente;
import model.Produit;

/**
 * Contrôleur pour afficher les produits d'une catégorie
 */
public class CategorieProduitsController {
    
    @FXML
    private Label categorieLabel;
    
    @FXML
    private FlowPane produitsContainer;
    
    @FXML
    private Button retourButton;
    
    @FXML
    private Label panierCountLabel;
    
    private ProduitDAO produitDAO;
    private String categorie;
    private static javafx.collections.ObservableList<DetailVente> panierGlobal;
    
    @FXML
    private void initialize() {
        produitDAO = new ProduitDAO();
        if (panierGlobal == null) {
            panierGlobal = javafx.collections.FXCollections.observableArrayList();
        }
        updatePanierCount();
    }
    
    public void setCategorie(String categorie) {
        this.categorie = categorie;
        categorieLabel.setText("Catégorie: " + categorie);
        chargerProduits();
    }
    
    private void chargerProduits() {
        produitsContainer.getChildren().clear();
        produitsContainer.setHgap(15);
        produitsContainer.setVgap(15);
        produitsContainer.setPadding(new Insets(20));
        
        java.util.List<Produit> produits = produitDAO.findByCategorie(categorie);
        
        if (produits.isEmpty()) {
            Label noProductsLabel = new Label("Aucun produit disponible dans cette catégorie");
            noProductsLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #666;");
            produitsContainer.getChildren().add(noProductsLabel);
            return;
        }
        
        for (Produit produit : produits) {
            VBox card = createProductCard(produit);
            produitsContainer.getChildren().add(card);
        }
    }
    
    private VBox createProductCard(Produit produit) {
        VBox card = new VBox(10);
        card.setPrefSize(200, 250);
        card.setPadding(new Insets(15));
        card.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 10; " +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 8, 0, 0, 3); " +
            "-fx-border-color: #e0e0e0; " +
            "-fx-border-width: 1; " +
            "-fx-border-radius: 10;"
        );
        
        // Nom du produit
        Label nomLabel = new Label(produit.getNom());
        nomLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #333;");
        nomLabel.setWrapText(true);
        nomLabel.setMaxWidth(170);
        
        // Prix
        Label prixLabel = new Label(String.format("%.2f €", produit.getPrixVenteDefaut()));
        prixLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #4CAF50;");
        
        // Stock
        Label stockLabel = new Label("Stock: " + produit.getQuantiteStock());
        stockLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");
        
        // Bouton Ajouter
        Button ajouterButton = new Button("Ajouter");
        ajouterButton.setPrefWidth(170);
        ajouterButton.setPrefHeight(35);
        ajouterButton.setStyle(
            "-fx-background-color: #4CAF50; " +
            "-fx-text-fill: white; " +
            "-fx-font-size: 14px; " +
            "-fx-font-weight: bold; " +
            "-fx-background-radius: 5; " +
            "-fx-cursor: hand;"
        );
        
        // Effet hover sur le bouton
        ajouterButton.setOnMouseEntered(e -> {
            ajouterButton.setStyle(
                "-fx-background-color: #45a049; " +
                "-fx-text-fill: white; " +
                "-fx-font-size: 14px; " +
                "-fx-font-weight: bold; " +
                "-fx-background-radius: 5; " +
                "-fx-cursor: hand; " +
                "-fx-scale-x: 1.05; " +
                "-fx-scale-y: 1.05;"
            );
        });
        
        ajouterButton.setOnMouseExited(e -> {
            ajouterButton.setStyle(
                "-fx-background-color: #4CAF50; " +
                "-fx-text-fill: white; " +
                "-fx-font-size: 14px; " +
                "-fx-font-weight: bold; " +
                "-fx-background-radius: 5; " +
                "-fx-cursor: hand; " +
                "-fx-scale-x: 1.0; " +
                "-fx-scale-y: 1.0;"
            );
        });
        
        ajouterButton.setOnAction(e -> ajouterAuPanier(produit));
        
        // Effet hover sur la carte
        card.setOnMouseEntered(e -> {
            card.setStyle(
                "-fx-background-color: #f5f5f5; " +
                "-fx-background-radius: 10; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 12, 0, 0, 5); " +
                "-fx-border-color: #4CAF50; " +
                "-fx-border-width: 2; " +
                "-fx-border-radius: 10; " +
                "-fx-scale-x: 1.02; " +
                "-fx-scale-y: 1.02;"
            );
        });
        
        card.setOnMouseExited(e -> {
            card.setStyle(
                "-fx-background-color: white; " +
                "-fx-background-radius: 10; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 8, 0, 0, 3); " +
                "-fx-border-color: #e0e0e0; " +
                "-fx-border-width: 1; " +
                "-fx-border-radius: 10; " +
                "-fx-scale-x: 1.0; " +
                "-fx-scale-y: 1.0;"
            );
        });
        
        card.getChildren().addAll(nomLabel, prixLabel, stockLabel, ajouterButton);
        
        return card;
    }
    
    private void ajouterAuPanier(Produit produit) {
        // Vérifier si le produit est déjà dans le panier
        DetailVente detailExistant = panierGlobal.stream()
            .filter(d -> d.getProduitId() == produit.getId())
            .findFirst()
            .orElse(null);
        
        if (detailExistant != null) {
            int nouvelleQuantite = detailExistant.getQuantite() + 1;
            if (nouvelleQuantite > produit.getQuantiteStock()) {
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.WARNING
                );
                alert.setTitle("Stock insuffisant");
                alert.setHeaderText(null);
                alert.setContentText("Stock disponible: " + produit.getQuantiteStock());
                alert.showAndWait();
                return;
            }
            detailExistant.setQuantite(nouvelleQuantite);
        } else {
            DetailVente detail = new DetailVente();
            detail.setProduitId(produit.getId());
            detail.setQuantite(1);
            detail.setPrixVenteUnitaire(produit.getPrixVenteDefaut());
            detail.setPrixAchatUnitaire(produit.getPrixAchatActuel());
            detail.setProduit(produit);
            panierGlobal.add(detail);
        }
        
        updatePanierCount();
    }
    
    private void updatePanierCount() {
        int count = panierGlobal != null ? panierGlobal.size() : 0;
        panierCountLabel.setText("Panier: " + count + " article(s)");
    }
    
    public static javafx.collections.ObservableList<DetailVente> getPanierGlobal() {
        if (panierGlobal == null) {
            panierGlobal = javafx.collections.FXCollections.observableArrayList();
        }
        return panierGlobal;
    }
    
    @FXML
    private void handleRetour() {
        try {
            Stage stage = (Stage) retourButton.getScene().getWindow();
            util.FXMLUtils.changeScene(stage, "/view/CaisseCategories.fxml", "Catégories");
        } catch (Exception e) {
            System.err.println("Erreur lors du retour: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleVoirPanier() {
        try {
            Stage stage = (Stage) retourButton.getScene().getWindow();
            util.FXMLUtils.changeScene(stage, "/view/Caisse.fxml", "Caisse - Point de Vente");
        } catch (Exception e) {
            System.err.println("Erreur lors de l'ouverture du panier: " + e.getMessage());
        }
    }
}

