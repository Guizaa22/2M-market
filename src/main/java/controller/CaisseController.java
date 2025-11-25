package controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import dao.ProduitDAO;
import dao.VenteDAO;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import model.DetailVente;
import model.Produit;
import model.Utilisateur;
import model.Vente;

/**
 * Contrôleur pour l'interface de caisse (point de vente)
 */
public class CaisseController {
    
    @FXML
    private VBox panierListContainer;
    
    @FXML
    private Label totalLabel;
    
    @FXML
    private Label tvaLabel;
    
    @FXML
    private Label userLabel;
    
    @FXML
    private Button scannerButton;
    
    @FXML
    private Button codeBarreButton;
    
    @FXML
    private Button especesButton;
    
    @FXML
    private Button carteButton;
    
    @FXML
    private Button autreButton;
    
    @FXML
    private Button validerButton;
    
    @FXML
    private Button annulerButton;
    
    @FXML
    private Button rechercheButton;
    
    @FXML
    private Button modifierQuantiteButton;
    
    @FXML
    private Button categoriesButton;
    
    private String modePaiement = "Espèces";
    
    private ProduitDAO produitDAO;
    private VenteDAO venteDAO;
    private ObservableList<DetailVente> panierList;
    private Utilisateur utilisateur;
    
    @FXML
    private void initialize() {
        produitDAO = new ProduitDAO();
        venteDAO = new VenteDAO();
        utilisateur = ConnexionController.getUtilisateurConnecte();
        
        // Utiliser le panier global (getPanierGlobal() l'initialise automatiquement si null)
        panierList = CategorieProduitsController.getPanierGlobal();
        
        // Mettre à jour le panier quand il change
        panierList.addListener((javafx.collections.ListChangeListener.Change<? extends DetailVente> c) -> {
            javafx.application.Platform.runLater(() -> {
                rafraichirPanier();
                calculerTotal();
            });
        });
        
        // Charger les produits pour chaque détail
        for (DetailVente detail : panierList) {
            if (detail.getProduit() == null) {
                Produit produit = produitDAO.findById(detail.getProduitId());
                detail.setProduit(produit);
            }
        }
        
        rafraichirPanier();
        calculerTotal();
        
        // Afficher le nom de l'utilisateur
        if (utilisateur != null && userLabel != null) {
            userLabel.setText("👤 " + utilisateur.getUsername());
        }
        
        // Ajouter le CSS
        javafx.application.Platform.runLater(() -> {
            if (panierListContainer != null && panierListContainer.getScene() != null) {
                javafx.scene.Parent root = panierListContainer.getScene().getRoot();
                if (root != null) {
                    String cssUrl = getClass().getResource("/styles/caisse.css").toExternalForm();
                    if (!root.getStylesheets().contains(cssUrl)) {
                        root.getStylesheets().add(cssUrl);
                    }
                }
            }
        });
    }
    
    
    private void rafraichirPanier() {
        panierListContainer.getChildren().clear();
        
        if (panierList.isEmpty()) {
            Label emptyLabel = new Label("🛒 Panier vide\nAjoutez des produits pour commencer");
            emptyLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #999; -fx-alignment: center;");
            panierListContainer.getChildren().add(emptyLabel);
            return;
        }
        
        for (DetailVente detail : panierList) {
            HBox itemRow = createPanierItemRow(detail);
            panierListContainer.getChildren().add(itemRow);
        }
    }
    
    private HBox createPanierItemRow(DetailVente detail) {
        Produit produit = detail.getProduit();
        if (produit == null) {
            produit = produitDAO.findById(detail.getProduitId());
            detail.setProduit(produit);
        }
        
        // Variables finales pour les lambdas
        final Produit finalProduit = produit;
        final DetailVente finalDetail = detail;
        
        HBox row = new HBox(15);
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        row.setPadding(new Insets(15));
        row.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.15), 10, 0, 0, 4); -fx-border-color: #e0e0e0; -fx-border-width: 1; -fx-border-radius: 12;");
        row.setPrefHeight(120);
        row.setMinHeight(120);
        
        // Effet hover pour tactile
        row.setOnMouseEntered(e -> {
            row.setStyle("-fx-background-color: linear-gradient(to right, #f9f9f9, #f5f5f5); -fx-background-radius: 12; -fx-effect: dropshadow(three-pass-box, rgba(76, 175, 80, 0.3), 15, 0, 0, 6); -fx-border-color: #4CAF50; -fx-border-width: 2; -fx-border-radius: 12;");
        });
        row.setOnMouseExited(e -> {
            row.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.15), 10, 0, 0, 4); -fx-border-color: #e0e0e0; -fx-border-width: 1; -fx-border-radius: 12;");
        });
        
        // Icône produit (placeholder)
        Label iconLabel = new Label("📦");
        iconLabel.setStyle("-fx-font-size: 48px;");
        
        // Informations produit
        VBox infoBox = new VBox(8);
        infoBox.setPrefWidth(300);
        infoBox.setMinWidth(300);
        
        Label nomLabel = new Label(finalProduit != null ? finalProduit.getNom() : "Produit ID: " + finalDetail.getProduitId());
        nomLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #333;");
        nomLabel.setWrapText(true);
        
        Label descLabel = new Label("📋 " + (finalProduit != null ? finalProduit.getCodeBarre() : "N/A"));
        descLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;");
        
        String unite = finalProduit != null ? finalProduit.getUnite() : "unité";
        Label prixUnitaireLabel = new Label("Prix: € " + String.format("%.2f", finalDetail.getPrixVenteUnitaire()) + " / " + unite);
        prixUnitaireLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #999;");
        
        infoBox.getChildren().addAll(nomLabel, descLabel, prixUnitaireLabel);
        
        // Contrôles de quantité (plus grands pour tactile)
        VBox quantiteContainer = new VBox(5);
        quantiteContainer.setAlignment(javafx.geometry.Pos.CENTER);
        
        Label quantiteTitleLabel = new Label("Quantité (" + unite + ")");
        quantiteTitleLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666; -fx-alignment: center;");
        
        HBox quantiteBox = new HBox(8);
        quantiteBox.setAlignment(javafx.geometry.Pos.CENTER);
        
        // Déclarer quantiteLabel avant les boutons pour l'utiliser dans les lambdas
        Label quantiteLabel = new Label(String.valueOf(finalDetail.getQuantite()));
        quantiteLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-pref-width: 60; -fx-alignment: center; -fx-text-fill: #333;");
        
        Button moinsButton = new Button("➖");
        moinsButton.setPrefSize(50, 50);
        moinsButton.setMinSize(50, 50);
        moinsButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 20px; -fx-background-radius: 10; -fx-cursor: hand;");
        moinsButton.setOnMouseEntered(e -> {
            moinsButton.setStyle("-fx-background-color: #d32f2f; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 20px; -fx-background-radius: 10; -fx-cursor: hand; -fx-scale-x: 1.1; -fx-scale-y: 1.1;");
        });
        moinsButton.setOnMouseExited(e -> {
            moinsButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 20px; -fx-background-radius: 10; -fx-cursor: hand; -fx-scale-x: 1.0; -fx-scale-y: 1.0;");
        });
        moinsButton.setOnAction(e -> {
            if (finalDetail.getQuantite() > 1) {
                finalDetail.setQuantite(finalDetail.getQuantite() - 1);
                quantiteLabel.setText(String.valueOf(finalDetail.getQuantite()));
                rafraichirPanier();
                calculerTotal();
            } else {
                // Si quantité = 1, proposer de retirer
                Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
                confirmAlert.setTitle("Retirer le produit");
                confirmAlert.setHeaderText(null);
                confirmAlert.setContentText("Voulez-vous retirer ce produit du panier ?");
                if (confirmAlert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                    panierList.remove(finalDetail);
                    rafraichirPanier();
                    calculerTotal();
                }
            }
        });
        
        Button plusButton = new Button("➕");
        plusButton.setPrefSize(50, 50);
        plusButton.setMinSize(50, 50);
        plusButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 20px; -fx-background-radius: 10; -fx-cursor: hand;");
        plusButton.setOnMouseEntered(e -> {
            plusButton.setStyle("-fx-background-color: #388E3C; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 20px; -fx-background-radius: 10; -fx-cursor: hand; -fx-scale-x: 1.1; -fx-scale-y: 1.1;");
        });
        plusButton.setOnMouseExited(e -> {
            plusButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 20px; -fx-background-radius: 10; -fx-cursor: hand; -fx-scale-x: 1.0; -fx-scale-y: 1.0;");
        });
        plusButton.setOnAction(e -> {
            if (finalProduit != null && finalDetail.getQuantite() < finalProduit.getQuantiteStock()) {
                finalDetail.setQuantite(finalDetail.getQuantite() + 1);
                quantiteLabel.setText(String.valueOf(finalDetail.getQuantite()));
                rafraichirPanier();
                calculerTotal();
            } else {
                showAlert(Alert.AlertType.WARNING, "Stock insuffisant", 
                         "Stock disponible: " + (finalProduit != null ? finalProduit.getQuantiteStock() : 0));
            }
        });
        
        quantiteBox.getChildren().addAll(moinsButton, quantiteLabel, plusButton);
        quantiteContainer.getChildren().addAll(quantiteTitleLabel, quantiteBox);
        
        // Prix total
        VBox prixContainer = new VBox(5);
        prixContainer.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        prixContainer.setPrefWidth(150);
        
        Label prixTitleLabel = new Label("Total");
        prixTitleLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");
        
        Label prixLabel = new Label("€ " + String.format("%.2f", finalDetail.getSousTotal()));
        prixLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #4CAF50; -fx-alignment: center-right;");
        
        prixContainer.getChildren().addAll(prixTitleLabel, prixLabel);
        
        // Boutons d'action (Retirer et Modifier)
        VBox actionsBox = new VBox(10);
        actionsBox.setAlignment(javafx.geometry.Pos.CENTER);
        actionsBox.setSpacing(10);
        
        Button retirerButton = new Button("🗑️ Retirer");
        retirerButton.setPrefSize(120, 45);
        retirerButton.setMinSize(120, 45);
        retirerButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-background-radius: 8; -fx-cursor: hand;");
        retirerButton.setOnMouseEntered(e -> {
            retirerButton.setStyle("-fx-background-color: #d32f2f; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-background-radius: 8; -fx-cursor: hand; -fx-scale-x: 1.05; -fx-scale-y: 1.05;");
        });
        retirerButton.setOnMouseExited(e -> {
            retirerButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-background-radius: 8; -fx-cursor: hand; -fx-scale-x: 1.0; -fx-scale-y: 1.0;");
        });
        retirerButton.setOnAction(e -> {
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Retirer le produit");
            confirmAlert.setHeaderText(null);
            confirmAlert.setContentText("Voulez-vous retirer \"" + (finalProduit != null ? finalProduit.getNom() : "ce produit") + "\" du panier ?");
            if (confirmAlert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                panierList.remove(finalDetail);
                rafraichirPanier();
                calculerTotal();
            }
        });
        
        Button modifierButton = new Button("✏️ Modifier");
        modifierButton.setPrefSize(120, 45);
        modifierButton.setMinSize(120, 45);
        modifierButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-background-radius: 8; -fx-cursor: hand;");
        modifierButton.setOnMouseEntered(e -> {
            modifierButton.setStyle("-fx-background-color: #1976D2; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-background-radius: 8; -fx-cursor: hand; -fx-scale-x: 1.05; -fx-scale-y: 1.05;");
        });
        modifierButton.setOnMouseExited(e -> {
            modifierButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-background-radius: 8; -fx-cursor: hand; -fx-scale-x: 1.0; -fx-scale-y: 1.0;");
        });
        modifierButton.setOnAction(e -> {
            // Ouvrir la recherche pour modifier
            handleRechercheProduit();
        });
        
        actionsBox.getChildren().addAll(retirerButton, modifierButton);
        
        row.getChildren().addAll(iconLabel, infoBox, quantiteContainer, prixContainer, actionsBox);
        
        return row;
    }
    
    private VBox createPanierCard(DetailVente detail) {
        VBox card = new VBox(10);
        card.setPrefSize(280, 200);
        card.setPadding(new Insets(15));
        card.getStyleClass().add("panier-card");
        card.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 12; " +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.15), 10, 0, 0, 4); " +
            "-fx-border-color: #e0e0e0; " +
            "-fx-border-width: 1; " +
            "-fx-border-radius: 12;"
        );
        
        Produit produit = detail.getProduit();
        if (produit == null) {
            produit = produitDAO.findById(detail.getProduitId());
            detail.setProduit(produit);
        }
        
        // Nom du produit
        Label nomLabel = new Label(produit != null ? produit.getNom() : "Produit ID: " + detail.getProduitId());
        nomLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2E7D32;");
        nomLabel.setWrapText(true);
        nomLabel.setMaxWidth(250);
        
        // Code-barres
        Label codeBarreLabel = new Label("📋 " + (produit != null ? produit.getCodeBarre() : "N/A"));
        codeBarreLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");
        
        // Quantité et prix
        HBox infoBox = new HBox(15);
        Label quantiteLabel = new Label("Quantité: " + detail.getQuantite());
        quantiteLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #333;");
        
        Label prixLabel = new Label(String.format("%.2f €", detail.getPrixVenteUnitaire()) + " / unité");
        prixLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #666;");
        
        infoBox.getChildren().addAll(quantiteLabel, prixLabel);
        
        // Sous-total
        Label sousTotalLabel = new Label("Sous-total: " + String.format("%.2f €", detail.getSousTotal()));
        sousTotalLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #4CAF50;");
        
        // Indication que la carte est cliquable
        Label clickHint = new Label("💡 Double-cliquez pour modifier");
        clickHint.setStyle("-fx-font-size: 11px; -fx-text-fill: #999; -fx-font-style: italic;");
        
        // Bouton retirer
        Button retirerButton = new Button("🗑️ Retirer");
        retirerButton.setPrefWidth(250);
        retirerButton.setPrefHeight(35);
        retirerButton.setStyle(
            "-fx-background-color: #f44336; " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-background-radius: 8; " +
            "-fx-cursor: hand; " +
            "-fx-font-size: 13px;"
        );
        
        retirerButton.setOnMouseEntered(e -> {
            retirerButton.setStyle(
                "-fx-background-color: #d32f2f; " +
                "-fx-text-fill: white; " +
                "-fx-font-weight: bold; " +
                "-fx-background-radius: 8; " +
                "-fx-cursor: hand; " +
                "-fx-font-size: 13px; " +
                "-fx-scale-x: 1.05; " +
                "-fx-scale-y: 1.05;"
            );
        });
        
        retirerButton.setOnMouseExited(e -> {
            retirerButton.setStyle(
                "-fx-background-color: #f44336; " +
                "-fx-text-fill: white; " +
                "-fx-font-weight: bold; " +
                "-fx-background-radius: 8; " +
                "-fx-cursor: hand; " +
                "-fx-font-size: 13px; " +
                "-fx-scale-x: 1.0; " +
                "-fx-scale-y: 1.0;"
            );
        });
        
        retirerButton.setOnAction(e -> {
            panierList.remove(detail);
            rafraichirPanier();
            calculerTotal();
        });
        
        // Bouton modifier
        Button modifierButton = new Button("✏️ Modifier");
        modifierButton.setPrefWidth(250);
        modifierButton.setPrefHeight(35);
        modifierButton.setStyle(
            "-fx-background-color: #2196F3; " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-background-radius: 8; " +
            "-fx-cursor: hand; " +
            "-fx-font-size: 13px;"
        );
        
        modifierButton.setOnMouseEntered(e -> {
            modifierButton.setStyle(
                "-fx-background-color: #1976D2; " +
                "-fx-text-fill: white; " +
                "-fx-font-weight: bold; " +
                "-fx-background-radius: 8; " +
                "-fx-cursor: hand; " +
                "-fx-font-size: 13px; " +
                "-fx-scale-x: 1.05; " +
                "-fx-scale-y: 1.05;"
            );
        });
        
        modifierButton.setOnMouseExited(e -> {
            modifierButton.setStyle(
                "-fx-background-color: #2196F3; " +
                "-fx-text-fill: white; " +
                "-fx-font-weight: bold; " +
                "-fx-background-radius: 8; " +
                "-fx-cursor: hand; " +
                "-fx-font-size: 13px; " +
                "-fx-scale-x: 1.0; " +
                "-fx-scale-y: 1.0;"
            );
        });
        
        modifierButton.setOnAction(e -> {
            // Modifier la quantité directement dans le panier
            // La quantité peut être modifiée avec les boutons +/-
        });
        
        // HBox pour les boutons
        HBox buttonsBox = new HBox(10);
        buttonsBox.getChildren().addAll(modifierButton, retirerButton);
        
        // VBox pour organiser les éléments
        VBox contentBox = new VBox(8);
        contentBox.getChildren().addAll(nomLabel, codeBarreLabel, infoBox, sousTotalLabel, clickHint, buttonsBox);
        
        // Effet hover sur la carte avec indication cliquable
        card.setOnMouseEntered(e -> {
            card.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #f9f9f9, #f5f5f5); " +
                "-fx-background-radius: 12; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(76, 175, 80, 0.3), 15, 0, 0, 6); " +
                "-fx-border-color: #4CAF50; " +
                "-fx-border-width: 2; " +
                "-fx-border-radius: 12;"
            );
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
        });
        
        card.getChildren().add(contentBox);
        
        // Double-clic sur la carte pour modifier (alternative au bouton)
        final Produit finalProduitCard = produit; // Final pour lambda
        card.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2 && finalProduitCard != null) {
                // La quantité peut être modifiée avec les boutons +/-
            }
        });
        
        return card;
    }
    
    @FXML
    private void handleValider() {
        if (panierList.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Panier vide", 
                     "Le panier est vide. Ajoutez des produits avant de valider.");
            return;
        }
        
        if (utilisateur == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", 
                     "Utilisateur non connecté.");
            return;
        }
        
        BigDecimal total = calculerTotal();
        
        // Créer la vente
        Vente vente = new Vente(LocalDateTime.now(), total, utilisateur.getId());
        
        // Ajouter les détails
        for (DetailVente detail : panierList) {
            vente.addDetail(detail);
        }
        
        // Enregistrer la vente
        if (venteDAO.create(vente)) {
            showAlert(Alert.AlertType.INFORMATION, "Vente validée", 
                     "La vente a été enregistrée avec succès.\n" +
                     "Mode de paiement: " + modePaiement + "\n" +
                     "Total: " + String.format("%.2f €", total));
            
            // Vider le panier
            panierList.clear();
            calculerTotal();
            rafraichirPanier();
        } else {
            showAlert(Alert.AlertType.ERROR, "Erreur", 
                     "Erreur lors de l'enregistrement de la vente.");
        }
    }
    
    @FXML
    private void handleAnnuler() {
        if (!panierList.isEmpty()) {
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Annuler la vente");
            confirmAlert.setHeaderText(null);
            confirmAlert.setContentText("Êtes-vous sûr de vouloir annuler cette vente ?");
            
            if (confirmAlert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                panierList.clear();
                calculerTotal();
                rafraichirPanier();
            }
        }
    }
    
    @FXML
    private void handleCategories() {
        try {
            javafx.stage.Stage stage = (javafx.stage.Stage) categoriesButton.getScene().getWindow();
            util.FXMLUtils.changeScene(stage, "/view/CaisseCategories.fxml", "Catégories");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", 
                     "Erreur lors de l'ouverture des catégories: " + e.getMessage());
        }
    }
    
    
    
    private BigDecimal calculerTotal() {
        BigDecimal total = panierList.stream()
            .map(DetailVente::getSousTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Calculer TVA (20%)
        BigDecimal tva = total.multiply(new BigDecimal("0.20"));
        
        if (totalLabel != null) {
            totalLabel.setText("€ " + String.format("%.2f", total));
        }
        if (tvaLabel != null) {
            tvaLabel.setText("TVA: € " + String.format("%.2f", tva));
        }
        
        return total;
    }
    
    @FXML
    private void handleScanner() {
        // Ouvrir la recherche de produits
        handleRechercheProduit();
    }
    
    @FXML
    private void handleCodeBarre() {
        // Ouvrir la recherche de produits
        handleRechercheProduit();
    }
    
    @FXML
    private void handlePaiementEspeces() {
        modePaiement = "Espèces";
        especesButton.setStyle("-fx-background-color: linear-gradient(to bottom, #2E7D32, #1B5E20); -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px; -fx-background-radius: 10; -fx-pref-height: 70; -fx-pref-width: 180; -fx-cursor: hand; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 8, 0, 0, 3);");
        carteButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px; -fx-background-radius: 10; -fx-pref-height: 70; -fx-pref-width: 180; -fx-cursor: hand;");
        autreButton.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px; -fx-background-radius: 10; -fx-pref-height: 70; -fx-pref-width: 180; -fx-cursor: hand;");
    }
    
    @FXML
    private void handlePaiementCarte() {
        modePaiement = "Carte Bancaire";
        especesButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px; -fx-background-radius: 10; -fx-pref-height: 70; -fx-pref-width: 180; -fx-cursor: hand;");
        carteButton.setStyle("-fx-background-color: linear-gradient(to bottom, #1565C0, #0D47A1); -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px; -fx-background-radius: 10; -fx-pref-height: 70; -fx-pref-width: 180; -fx-cursor: hand; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 8, 0, 0, 3);");
        autreButton.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px; -fx-background-radius: 10; -fx-pref-height: 70; -fx-pref-width: 180; -fx-cursor: hand;");
    }
    
    @FXML
    private void handlePaiementAutre() {
        modePaiement = "Autre";
        especesButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px; -fx-background-radius: 10; -fx-pref-height: 70; -fx-pref-width: 180; -fx-cursor: hand;");
        carteButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px; -fx-background-radius: 10; -fx-pref-height: 70; -fx-pref-width: 180; -fx-cursor: hand;");
        autreButton.setStyle("-fx-background-color: linear-gradient(to bottom, #F57C00, #E65100); -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px; -fx-background-radius: 10; -fx-pref-height: 70; -fx-pref-width: 180; -fx-cursor: hand; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 8, 0, 0, 3);");
    }
    
    @FXML
    private void handleModifierQuantite() {
        // Ouvrir la recherche pour modifier une quantité
        handleRechercheProduit();
    }
    
    @FXML
    private void handleRechercheProduit() {
        try {
            javafx.stage.Stage stage = (javafx.stage.Stage) rechercheButton.getScene().getWindow();
            util.FXMLUtils.changeScene(stage, "/view/CaisseCategories.fxml", "Catégories");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", 
                     "Erreur lors de l'ouverture de la recherche: " + e.getMessage());
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

