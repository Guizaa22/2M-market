package controller;

import dao.ProduitDAO;
import javafx.animation.ScaleTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import model.Produit;
import util.FXMLUtils;
import util.PopupManager;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Contrôleur pour l'ajout de stock par les employés
 * Permet uniquement de voir et ajouter du stock (pas de modification/suppression)
 */
public class AjoutStockEmployeController {

    @FXML
    private StackPane rootPane;

    @FXML
    private TextField rechercheField;
    
    @FXML
    private Button rechercherButton;
    
    @FXML
    private VBox produitInfoBox;
    
    @FXML
    private Label nomLabel;
    
    @FXML
    private Label codeBarreLabel;
    
    @FXML
    private Label stockActuelLabel;
    
    @FXML
    private Label prixVenteLabel;
    
    @FXML
    private TextField quantiteField;
    
    @FXML
    private Button moinsButton;
    
    @FXML
    private Button plusButton;
    
    @FXML
    private Button ajouterStockButton;
    
    @FXML
    private Label messageLabel;
    
    @FXML
    private FlowPane produitsContainer;
    
    @FXML
    private TextField filtreField;
    
    @FXML
    private Button retourButton;
    
    private ProduitDAO produitDAO;
    private Produit produitSelectionne;
    private ObservableList<Produit> tousProduits;
    private ObservableList<Produit> produitsFiltres;

    @FXML
    private void initialize() {
        produitDAO = new ProduitDAO();
        tousProduits = FXCollections.observableArrayList();
        produitsFiltres = FXCollections.observableArrayList();
        
        chargerTousProduits();
        afficherProduits();
        
        // ✨ LIVE SEARCH: Recherche automatique pendant que vous tapez
        rechercheField.textProperty().addListener((observable, oldValue, newValue) -> {
            rechercheAutomatique(newValue);
        });
    }

    /**
     * Charge tous les produits depuis la base de données
     */
    private void chargerTousProduits() {
        tousProduits.clear();
        List<Produit> produits = produitDAO.findAll();
        produits.stream()
                .filter(p -> p.getQuantiteStock() > 0)
                .forEach(tousProduits::add);
        produitsFiltres.setAll(tousProduits);
    }

    /**
     * Affiche les produits dans le FlowPane avec animation
     */
    private void afficherProduits() {
        produitsContainer.getChildren().clear();
        
        if (produitsFiltres.isEmpty()) {
            // Message si aucun produit
            VBox emptyState = new VBox(20);
            emptyState.setAlignment(Pos.CENTER);
            emptyState.setPadding(new Insets(60));
            emptyState.getStyleClass().add("empty-state");
            
            Label emptyIcon = new Label("📦");
            emptyIcon.getStyleClass().add("empty-state-icon");
            emptyIcon.setStyle("-fx-font-size: 64px;");
            
            Label emptyTitle = new Label("Aucun produit trouvé");
            emptyTitle.getStyleClass().add("empty-state-title");
            emptyTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
            
            Label emptyDesc = new Label("Essayez de modifier votre recherche ou ajoutez des produits à la base de données.");
            emptyDesc.getStyleClass().add("empty-state-description");
            emptyDesc.setStyle("-fx-font-size: 14px; -fx-wrap-text: true; -fx-text-alignment: center;");
            emptyDesc.setWrapText(true);
            emptyDesc.setMaxWidth(400);
            
            emptyState.getChildren().addAll(emptyIcon, emptyTitle, emptyDesc);
            produitsContainer.getChildren().add(emptyState);
        } else {
            // Afficher les cartes produit
            for (Produit produit : produitsFiltres) {
                VBox card = creerCarteProduit(produit);
                produitsContainer.getChildren().add(card);
            }
        }
    }

    /**
     * Crée une carte produit pour l'affichage
     */
    private VBox creerCarteProduit(Produit produit) {
        VBox card = new VBox(12);
        card.getStyleClass().add("product-card");
        card.setAlignment(Pos.TOP_LEFT);
        card.setPadding(new Insets(18));

        // ============ NOM du produit (GRAND et CLAIR) ============
        Label nomLabel = new Label(produit.getNom());
        nomLabel.getStyleClass().add("product-name");
        nomLabel.setWrapText(true);
        nomLabel.setMaxWidth(Double.MAX_VALUE);
        nomLabel.setAlignment(Pos.TOP_LEFT);
        nomLabel.setStyle("-fx-padding: 0 0 8 0;");

        // ============ PRICE: Prix de vente (TRÈS VISIBLE) ============
        HBox priceContainer = new HBox();
        priceContainer.setAlignment(Pos.CENTER_LEFT);
        priceContainer.setStyle("-fx-padding: 5 0;");
        
        Label prixLabel = new Label(String.format("%.2f DT", produit.getPrixVenteDefaut()));
        prixLabel.getStyleClass().add("product-price");
        priceContainer.getChildren().add(prixLabel);

        // ============ DETAILS: Informations ============
        VBox detailsBox = new VBox(5);
        detailsBox.setAlignment(Pos.TOP_LEFT);
        detailsBox.setStyle("-fx-padding: 8 0;");
        
        // Code-barres
        Label codeLabel = new Label("📋 " + produit.getCodeBarre());
        codeLabel.getStyleClass().add("product-code");
        codeLabel.setAlignment(Pos.CENTER_LEFT);
        detailsBox.getChildren().add(codeLabel);
        
        // Unité (si disponible)
        if (produit.getUnite() != null && !produit.getUnite().isEmpty()) {
            Label uniteLabel = new Label("📦 " + produit.getUnite());
            uniteLabel.getStyleClass().add("product-unite");
            uniteLabel.setAlignment(Pos.CENTER_LEFT);
            detailsBox.getChildren().add(uniteLabel);
        }

        // ============ Spacer pour pousser le footer en bas ============
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // ============ FOOTER: Stock + Button ============
        VBox footerBox = new VBox(10);
        footerBox.setAlignment(Pos.BOTTOM_CENTER);
        footerBox.setStyle("-fx-padding: 10 0 0 0;");

        // STOCK: Badge coloré
        HBox stockContainer = new HBox();
        stockContainer.setAlignment(Pos.CENTER);
        stockContainer.setStyle("-fx-padding: 5 0;");
        
        Label stockLabel = new Label("📊 Stock: " + produit.getQuantiteStock());
        stockLabel.getStyleClass().add("product-stock");
        
        // Couleur selon le niveau de stock
        if (produit.getQuantiteStock() == 0) {
            stockLabel.getStyleClass().add("stock-critical");
        } else if (produit.isStockFaible() || produit.getQuantiteStock() <= produit.getSeuilAlerte()) {
            stockLabel.getStyleClass().add("stock-low");
        } else if (produit.getQuantiteStock() > 50) {
            stockLabel.getStyleClass().add("stock-high");
        } else {
            stockLabel.getStyleClass().add("stock-medium");
        }
        
        stockContainer.getChildren().add(stockLabel);

        // BUTTON: Sélectionner (fixé en bas)
        Button selectButton = new Button("✅ Sélectionner");
        selectButton.getStyleClass().addAll("btn", "btn-primary");
        selectButton.setMaxWidth(Double.MAX_VALUE);
        selectButton.setMinHeight(40);
        selectButton.setAlignment(Pos.CENTER);
        selectButton.setOnAction(e -> selectionnerProduit(produit));

        footerBox.getChildren().addAll(stockContainer, selectButton);

        // Assembler la carte dans l'ordre
        card.getChildren().addAll(
            nomLabel,
            priceContainer,
            detailsBox,
            spacer,
            footerBox
        );

        // Effet hover smooth
        configurerEffetHover(card);

        return card;
    }

    /**
     * Configure l'effet hover sur la carte
     */
    private void configurerEffetHover(VBox card) {
        ScaleTransition scaleIn = new ScaleTransition(Duration.millis(200), card);
        scaleIn.setToX(1.05);
        scaleIn.setToY(1.05);

        ScaleTransition scaleOut = new ScaleTransition(Duration.millis(200), card);
        scaleOut.setToX(1.0);
        scaleOut.setToY(1.0);

        card.setOnMouseEntered(e -> scaleIn.playFromStart());
        card.setOnMouseExited(e -> scaleOut.playFromStart());
    }

    /**
     * Sélectionne un produit pour ajouter du stock
     */
    private void selectionnerProduit(Produit produit) {
        produitSelectionne = produit;
        afficherInfoProduit(produit);
        quantiteField.setText("1");
        produitInfoBox.setVisible(true);
        messageLabel.setVisible(false);
    }

    /**
     * Affiche les informations du produit sélectionné
     */
    private void afficherInfoProduit(Produit produit) {
        nomLabel.setText(produit.getNom());
        codeBarreLabel.setText(produit.getCodeBarre());
        stockActuelLabel.setText(String.valueOf(produit.getQuantiteStock()));
        prixVenteLabel.setText(String.format("%.2f €", produit.getPrixVenteDefaut()));
    }

    /**
     * ✨ LIVE SEARCH: Recherche automatique pendant que vous tapez
     */
    private void rechercheAutomatique(String texte) {
        String recherche = texte.trim().toLowerCase();
        
        if (recherche.isEmpty()) {
            // Si le champ est vide, réinitialiser
            produitsFiltres.setAll(tousProduits);
            produitInfoBox.setVisible(false);
            messageLabel.setVisible(false);
            afficherProduits();
            return;
        }
        
        // Filtrer les produits en temps réel
        List<Produit> resultats = tousProduits.stream()
            .filter(p -> p.getNom().toLowerCase().contains(recherche) ||
                        p.getCodeBarre().toLowerCase().contains(recherche))
            .collect(Collectors.toList());
        
        produitsFiltres.setAll(resultats);
        afficherProduits();
        
        // Si recherche exacte (code-barres ou nom complet), sélectionner automatiquement
        if (resultats.size() == 1) {
            selectionnerProduit(resultats.get(0));
        } else if (recherche.length() >= 8 && recherche.matches("\\d+")) {
            // Si c'est un code-barres complet, chercher exact match
            Produit exact = produitDAO.rechercherProduit(recherche);
            if (exact != null) {
                selectionnerProduit(exact);
            }
        }
    }
    
    @FXML
    private void handleRecherche() {
        String recherche = rechercheField.getText().trim();
        
        if (recherche.isEmpty()) {
            afficherMessage("Veuillez entrer un code-barres ou un nom de produit.", Alert.AlertType.WARNING);
            return;
        }

        // Chercher le produit exact
        Produit produit = produitDAO.rechercherProduit(recherche);
        
        if (produit != null) {
            selectionnerProduit(produit);
        } else {
            produitInfoBox.setVisible(false);
            afficherMessage("Produit non trouvé. Vérifiez le code-barres ou le nom.", Alert.AlertType.WARNING);
        }
    }

    @FXML
    private void handleMoins() {
        try {
            int quantite = Integer.parseInt(quantiteField.getText());
            if (quantite > 1) {
                quantiteField.setText(String.valueOf(quantite - 1));
            }
        } catch (NumberFormatException e) {
            quantiteField.setText("1");
        }
    }

    @FXML
    private void handlePlus() {
        try {
            int quantite = Integer.parseInt(quantiteField.getText());
            quantiteField.setText(String.valueOf(quantite + 1));
        } catch (NumberFormatException e) {
            quantiteField.setText("1");
        }
    }

    @FXML
    private void handleAjouterStock() {
        if (produitSelectionne == null) {
            afficherMessage("Veuillez sélectionner un produit d'abord.", Alert.AlertType.WARNING);
            return;
        }

        try {
            int quantiteAjouter = Integer.parseInt(quantiteField.getText().trim());
            
            if (quantiteAjouter <= 0) {
                afficherMessage("La quantité doit être supérieure à 0.", Alert.AlertType.WARNING);
                return;
            }

            int nouveauStock = produitSelectionne.getQuantiteStock() + quantiteAjouter;
            
            boolean succes = produitDAO.updateStock(produitSelectionne.getId(), nouveauStock);
            
            if (succes) {
                afficherMessage("Stock mis à jour avec succès! Nouveau stock: " + nouveauStock, Alert.AlertType.INFORMATION);
                produitSelectionne.setQuantiteStock(nouveauStock);
                afficherInfoProduit(produitSelectionne);
                
                // Recharger la liste
                chargerTousProduits();
                afficherProduits();
                
                quantiteField.setText("1");
            } else {
                afficherMessage("Erreur lors de la mise à jour du stock.", Alert.AlertType.ERROR);
            }
        } catch (NumberFormatException e) {
            afficherMessage("Veuillez entrer un nombre valide.", Alert.AlertType.WARNING);
        }
    }

    @FXML
    private void handleFiltre() {
        String filtre = filtreField.getText().toLowerCase().trim();
        
        if (filtre.isEmpty()) {
            produitsFiltres.setAll(tousProduits);
        } else {
            List<Produit> filtres = tousProduits.stream()
                .filter(p -> p.getNom().toLowerCase().contains(filtre) ||
                           p.getCodeBarre().toLowerCase().contains(filtre))
                .collect(Collectors.toList());
            produitsFiltres.setAll(filtres);
        }
        
        afficherProduits();
    }

    @FXML
    private void handleRetour() {
        try {
            javafx.stage.Stage stage = (javafx.stage.Stage) retourButton.getScene().getWindow();
            FXMLUtils.changeScene(stage, "/view/CaisseCategories.fxml", "2M Market - Point de Vente");
        } catch (IOException e) {
            afficherMessage("Erreur lors du retour: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /**
     * Affiche un message moderne avec PopupManager
     */
    private void afficherMessage(String message, Alert.AlertType type) {
        // Utiliser le nouveau système de toast moderne
        if (type == Alert.AlertType.ERROR) {
            PopupManager.showError(message, rootPane);
        } else if (type == Alert.AlertType.WARNING) {
            PopupManager.showWarning(message, rootPane);
        } else if (type == Alert.AlertType.INFORMATION) {
            PopupManager.showSuccess(message, rootPane);
        } else {
            PopupManager.showInfo(message, rootPane);
        }
        
        // Aussi afficher dans le label pour compatibilité
        messageLabel.setText(message);
        messageLabel.setVisible(true);
        
        if (type == Alert.AlertType.ERROR) {
            messageLabel.setStyle("-fx-text-fill: #D32F2F;");
        } else if (type == Alert.AlertType.WARNING) {
            messageLabel.setStyle("-fx-text-fill: #F57C00;");
        } else {
            messageLabel.setStyle("-fx-text-fill: #2E7D32;");
        }
        
        // Masquer le label après 5 secondes
        new Thread(() -> {
            try {
                Thread.sleep(5000);
                javafx.application.Platform.runLater(() -> messageLabel.setVisible(false));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }
}

