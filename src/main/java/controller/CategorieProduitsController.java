package controller;

import dao.ProduitDAO;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
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
    private Button panierButton;
    
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
        VBox card = new VBox(12);
        card.setPrefSize(220, 280);
        card.setPadding(new Insets(18));
        card.getStyleClass().add("product-card");
        card.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 12; " +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.15), 10, 0, 0, 4); " +
            "-fx-border-color: #e0e0e0; " +
            "-fx-border-width: 1; " +
            "-fx-border-radius: 12;"
        );
        
        // Nom du produit avec style amélioré
        Label nomLabel = new Label(produit.getNom());
        nomLabel.getStyleClass().add("product-name");
        nomLabel.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: #2E7D32;");
        nomLabel.setWrapText(true);
        nomLabel.setMaxWidth(184);
        nomLabel.setMinHeight(40);
        
        // Catégorie du produit
        String categorieProduit = produit.getCategorie();
        if (categorieProduit == null || categorieProduit.isEmpty()) {
            categorieProduit = "Sans catégorie";
        }
        Label categorieLabel = new Label("🏷️ " + categorieProduit);
        categorieLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #666; -fx-font-style: italic;");
        
        // Code-barres
        Label codeBarreLabel = new Label("📋 " + produit.getCodeBarre());
        codeBarreLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #999;");
        
        // Prix avec style amélioré
        Label prixLabel = new Label(String.format("%.2f €", produit.getPrixVenteDefaut()));
        prixLabel.getStyleClass().add("product-price");
        prixLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: linear-gradient(to bottom, #4CAF50, #2E7D32);");
        
        // Stock avec couleur selon disponibilité
        Label stockLabel = new Label("📦 Stock: " + produit.getQuantiteStock());
        if (produit.isStockFaible()) {
            stockLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #f44336; -fx-font-weight: bold;");
        } else if (produit.getQuantiteStock() > 50) {
            stockLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #4CAF50; -fx-font-weight: bold;");
        } else {
            stockLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #FF9800; -fx-font-weight: bold;");
        }
        
        // Bouton Ajouter avec style amélioré
        Button ajouterButton = new Button("➕ Ajouter");
        ajouterButton.setPrefWidth(184);
        ajouterButton.setPrefHeight(40);
        ajouterButton.getStyleClass().add("add-button");
        ajouterButton.setStyle(
            "-fx-background-color: linear-gradient(to bottom, #4CAF50, #2E7D32); " +
            "-fx-text-fill: white; " +
            "-fx-font-size: 15px; " +
            "-fx-font-weight: bold; " +
            "-fx-background-radius: 8; " +
            "-fx-cursor: hand; " +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 5, 0, 0, 2);"
        );
        
        // Effet hover amélioré sur le bouton
        ajouterButton.setOnMouseEntered(e -> {
            ajouterButton.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #66BB6A, #4CAF50); " +
                "-fx-text-fill: white; " +
                "-fx-font-size: 16px; " +
                "-fx-font-weight: bold; " +
                "-fx-background-radius: 8; " +
                "-fx-cursor: hand; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 8, 0, 0, 4); " +
                "-fx-scale-x: 1.08; " +
                "-fx-scale-y: 1.08;"
            );
        });
        
        ajouterButton.setOnMouseExited(e -> {
            ajouterButton.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #4CAF50, #2E7D32); " +
                "-fx-text-fill: white; " +
                "-fx-font-size: 15px; " +
                "-fx-font-weight: bold; " +
                "-fx-background-radius: 8; " +
                "-fx-cursor: hand; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 5, 0, 0, 2); " +
                "-fx-scale-x: 1.0; " +
                "-fx-scale-y: 1.0;"
            );
        });
        
        ajouterButton.setOnAction(e -> ajouterAuPanier(produit));
        
        // Effet hover amélioré sur la carte avec animation
        card.setOnMouseEntered(e -> {
            card.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #f9f9f9, #f5f5f5); " +
                "-fx-background-radius: 12; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(76, 175, 80, 0.4), 18, 0, 0, 8); " +
                "-fx-border-color: #4CAF50; " +
                "-fx-border-width: 2.5; " +
                "-fx-border-radius: 12;"
            );
            // Animation de translation
            TranslateTransition tt = new TranslateTransition(Duration.millis(200), card);
            tt.setToY(-8);
            tt.play();
        });
        
        card.setOnMouseExited(e -> {
            card.setStyle(
                "-fx-background-color: white; " +
                "-fx-background-radius: 12; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.15), 10, 0, 0, 4); " +
                "-fx-border-color: #e0e0e0; " +
                "-fx-border-width: 1; " +
                "-fx-border-radius: 12;"
            );
            // Animation de retour
            TranslateTransition tt = new TranslateTransition(Duration.millis(200), card);
            tt.setToY(0);
            tt.play();
        });
        
        card.getChildren().addAll(nomLabel, categorieLabel, codeBarreLabel, prixLabel, stockLabel, ajouterButton);
        
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
            Stage stage = (Stage) panierButton.getScene().getWindow();
            util.FXMLUtils.changeScene(stage, "/view/Caisse.fxml", "Caisse - Point de Vente");
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText(null);
            alert.setContentText("Erreur lors de l'ouverture du panier: " + e.getMessage());
            alert.showAndWait();
        }
    }
}

