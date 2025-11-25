package controller;

import java.util.List;

import dao.ProduitDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import model.Produit;

/**
 * Contrôleur pour la visualisation des produits avec filtrage et recherche
 * Interface améliorée avec effets 3D et hover
 */
public class VisualisationProduitsController {
    
    @FXML
    private FlowPane produitsContainer;
    
    @FXML
    private TextField rechercheField;
    
    @FXML
    private ComboBox<String> categorieComboBox;
    
    @FXML
    private TextField codeBarreRechercheField;
    
    @FXML
    private Label totalProduitsLabel;
    
    private ProduitDAO produitDAO;
    private ObservableList<Produit> tousProduits;
    private FilteredList<Produit> produitsFiltres;
    
    @FXML
    private void initialize() {
        produitDAO = new ProduitDAO();
        tousProduits = FXCollections.observableArrayList();
        produitsFiltres = new FilteredList<>(tousProduits, p -> true);
        
        // Configuration du conteneur
        produitsContainer.setHgap(20);
        produitsContainer.setVgap(20);
        produitsContainer.setPadding(new Insets(20));
        
        // Charger les catégories
        chargerCategories();
        
        // Charger tous les produits
        chargerProduits();
        
        // Écouter les changements de recherche
        rechercheField.textProperty().addListener((obs, oldVal, newVal) -> filtrerProduits());
        
        // Écouter les changements de catégorie
        categorieComboBox.valueProperty().addListener((obs, oldVal, newVal) -> filtrerProduits());
        
        // Recherche par code-barres avec auto-ajout
        codeBarreRechercheField.setOnAction(e -> rechercherParCodeBarre());
    }
    
    /**
     * Charge toutes les catégories disponibles
     */
    private void chargerCategories() {
        List<String> categories = produitDAO.findAllCategories();
        categorieComboBox.getItems().clear();
        categorieComboBox.getItems().add("Toutes les catégories");
        categorieComboBox.getItems().addAll(categories);
        categorieComboBox.setValue("Toutes les catégories");
    }
    
    /**
     * Charge tous les produits depuis la base de données
     */
    private void chargerProduits() {
        tousProduits.clear();
        List<Produit> produits = produitDAO.findAll();
        tousProduits.addAll(produits);
        afficherProduits();
        mettreAJourTotal();
    }
    
    /**
     * Affiche les produits filtrés dans le conteneur
     */
    private void afficherProduits() {
        produitsContainer.getChildren().clear();
        
        for (Produit produit : produitsFiltres) {
            VBox card = creerCarteProduit(produit);
            produitsContainer.getChildren().add(card);
        }
    }
    
    /**
     * Crée une carte produit avec effets 3D et hover
     */
    private VBox creerCarteProduit(Produit produit) {
        VBox card = new VBox(10);
        card.setPrefSize(280, 320);
        card.setPadding(new Insets(20));
        card.setAlignment(Pos.TOP_CENTER);
        
        // Style de base avec effet 3D
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 0, 0, 0.3));
        shadow.setRadius(15);
        shadow.setOffsetX(0);
        shadow.setOffsetY(5);
        card.setEffect(shadow);
        
        card.setStyle(
            "-fx-background-color: linear-gradient(to bottom, #ffffff, #f5f5f5); " +
            "-fx-background-radius: 15; " +
            "-fx-border-color: #e0e0e0; " +
            "-fx-border-width: 1; " +
            "-fx-border-radius: 15; " +
            "-fx-cursor: hand;"
        );
        
        // Nom du produit
        Label nomLabel = new Label(produit.getNom());
        nomLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2E7D32; -fx-wrap-text: true;");
        nomLabel.setMaxWidth(240);
        
        // Code-barres
        Label codeBarreLabel = new Label("📋 " + produit.getCodeBarre());
        codeBarreLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #666;");
        
        // Catégorie
        Label categorieLabel = new Label("📂 " + (produit.getCategorie() != null && !produit.getCategorie().isEmpty() ? produit.getCategorie() : "Non catégorisé"));
        categorieLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #999;");
        
        // Unité
        Label uniteLabel = new Label("⚖️ Unité: " + produit.getUnite());
        uniteLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #999;");
        
        // Prix
        HBox prixBox = new HBox(10);
        prixBox.setAlignment(Pos.CENTER);
        
        Label prixAchatLabel = new Label("Achat: €" + String.format("%.2f", produit.getPrixAchatActuel()));
        prixAchatLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #757575;");
        
        Label prixVenteLabel = new Label("Vente: €" + String.format("%.2f", produit.getPrixVenteDefaut()));
        prixVenteLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #4CAF50;");
        
        prixBox.getChildren().addAll(prixAchatLabel, prixVenteLabel);
        
        // Stock avec couleur conditionnelle
        int stock = produit.getQuantiteStock();
        String stockColor = stock > produit.getSeuilAlerte() ? "#4CAF50" : "#f44336";
        Label stockLabel = new Label("📦 Stock: " + stock + " " + produit.getUnite());
        stockLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: " + stockColor + ";");
        
        // Seuil d'alerte
        Label seuilLabel = new Label("⚠️ Seuil: " + produit.getSeuilAlerte());
        seuilLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #FF9800;");
        
        // Bouton d'action
        Button actionButton = new Button("➕ Ajouter au Stock");
        actionButton.setPrefWidth(240);
        actionButton.setPrefHeight(35);
        actionButton.setStyle(
            "-fx-background-color: linear-gradient(to bottom, #4CAF50, #2E7D32); " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-font-size: 13px; " +
            "-fx-background-radius: 8; " +
            "-fx-cursor: hand; " +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 5, 0, 0, 2);"
        );
        
        // Effet hover sur le bouton
        actionButton.setOnMouseEntered(e -> {
            actionButton.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #66BB6A, #4CAF50); " +
                "-fx-text-fill: white; " +
                "-fx-font-weight: bold; " +
                "-fx-font-size: 13px; " +
                "-fx-background-radius: 8; " +
                "-fx-cursor: hand; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 8, 0, 0, 4); " +
                "-fx-scale-x: 1.05; " +
                "-fx-scale-y: 1.05;"
            );
        });
        
        actionButton.setOnMouseExited(e -> {
            actionButton.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #4CAF50, #2E7D32); " +
                "-fx-text-fill: white; " +
                "-fx-font-weight: bold; " +
                "-fx-font-size: 13px; " +
                "-fx-background-radius: 8; " +
                "-fx-cursor: hand; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 5, 0, 0, 2); " +
                "-fx-scale-x: 1.0; " +
                "-fx-scale-y: 1.0;"
            );
        });
        
        actionButton.setOnAction(e -> ouvrirAjoutStock(produit));
        
        // Assembler la carte
        card.getChildren().addAll(nomLabel, codeBarreLabel, categorieLabel, uniteLabel, prixBox, stockLabel, seuilLabel, actionButton);
        
        // Effets hover sur la carte entière (3D)
        card.setOnMouseEntered(e -> {
            DropShadow hoverShadow = new DropShadow();
            hoverShadow.setColor(Color.rgb(76, 175, 80, 0.5));
            hoverShadow.setRadius(20);
            hoverShadow.setOffsetX(0);
            hoverShadow.setOffsetY(8);
            card.setEffect(hoverShadow);
            card.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #f9f9f9, #f0f0f0); " +
                "-fx-background-radius: 15; " +
                "-fx-border-color: #4CAF50; " +
                "-fx-border-width: 2; " +
                "-fx-border-radius: 15; " +
                "-fx-cursor: hand; " +
                "-fx-scale-x: 1.02; " +
                "-fx-scale-y: 1.02; " +
                "-fx-translate-y: -5;"
            );
        });
        
        card.setOnMouseExited(e -> {
            card.setEffect(shadow);
            card.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #ffffff, #f5f5f5); " +
                "-fx-background-radius: 15; " +
                "-fx-border-color: #e0e0e0; " +
                "-fx-border-width: 1; " +
                "-fx-border-radius: 15; " +
                "-fx-cursor: hand; " +
                "-fx-scale-x: 1.0; " +
                "-fx-scale-y: 1.0; " +
                "-fx-translate-y: 0;"
            );
        });
        
        return card;
    }
    
    /**
     * Filtre les produits selon la recherche et la catégorie
     */
    private void filtrerProduits() {
        String recherche = rechercheField.getText().toLowerCase();
        String categorie = categorieComboBox.getValue();
        
        produitsFiltres.setPredicate(produit -> {
            boolean matchRecherche = recherche.isEmpty() || 
                produit.getNom().toLowerCase().contains(recherche) ||
                produit.getCodeBarre().toLowerCase().contains(recherche);
            
            boolean matchCategorie = categorie == null || 
                categorie.equals("Toutes les catégories") ||
                (produit.getCategorie() != null && produit.getCategorie().equals(categorie));
            
            return matchRecherche && matchCategorie;
        });
        
        afficherProduits();
        mettreAJourTotal();
    }
    
    /**
     * Recherche un produit par code-barres et l'ajoute automatiquement s'il n'existe pas
     */
    @FXML
    private void rechercherParCodeBarre() {
        String codeBarre = codeBarreRechercheField.getText().trim();
        
        if (codeBarre.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Code-barres vide", 
                     "Veuillez entrer un code-barres.");
            return;
        }
        
        // Rechercher le produit
        Produit produit = produitDAO.findByCodeBarre(codeBarre);
        
        if (produit != null) {
            // Produit trouvé - ouvrir l'ajout de stock
            ouvrirAjoutStock(produit);
            codeBarreRechercheField.clear();
        } else {
            // Produit non trouvé - proposer de le créer
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Produit introuvable");
            confirmAlert.setHeaderText(null);
            confirmAlert.setContentText("Aucun produit trouvé avec le code-barres: " + codeBarre + 
                                       "\n\nVoulez-vous créer un nouveau produit avec ce code-barres ?");
            
            if (confirmAlert.showAndWait().orElse(javafx.scene.control.ButtonType.CANCEL) == javafx.scene.control.ButtonType.OK) {
                ouvrirCreationProduit(codeBarre);
            }
        }
    }
    
    /**
     * Ouvre une fenêtre pour ajouter du stock à un produit existant
     */
    private void ouvrirAjoutStock(Produit produit) {
        // Créer une boîte de dialogue pour ajouter du stock
        javafx.scene.control.TextInputDialog dialog = new javafx.scene.control.TextInputDialog("0");
        dialog.setTitle("Ajouter au Stock");
        dialog.setHeaderText("Produit: " + produit.getNom());
        dialog.setContentText("Quantité à ajouter (" + produit.getUnite() + "):");
        
        dialog.showAndWait().ifPresent(quantiteStr -> {
            try {
                int quantite = Integer.parseInt(quantiteStr);
                if (quantite > 0) {
                    produit.setQuantiteStock(produit.getQuantiteStock() + quantite);
                    if (produitDAO.update(produit)) {
                        showAlert(Alert.AlertType.INFORMATION, "Succès", 
                                 quantite + " " + produit.getUnite() + " ajouté(s) au stock.");
                        chargerProduits();
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Erreur", 
                                 "Erreur lors de la mise à jour du stock.");
                    }
                } else {
                    showAlert(Alert.AlertType.WARNING, "Quantité invalide", 
                             "La quantité doit être supérieure à 0.");
                }
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", 
                         "Veuillez entrer un nombre valide.");
            }
        });
    }
    
    /**
     * Ouvre une fenêtre pour créer un nouveau produit
     */
    private void ouvrirCreationProduit(String codeBarre) {
        // Rediriger vers la gestion de stock avec le code-barres pré-rempli
        try {
            javafx.stage.Stage stage = (javafx.stage.Stage) produitsContainer.getScene().getWindow();
            util.FXMLUtils.changeScene(stage, "/view/GestionStock.fxml", "Gestion de Stock");
            // Note: Pour pré-remplir le champ, il faudrait passer des paramètres ou utiliser une variable statique
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", 
                     "Erreur lors de l'ouverture de la gestion de stock: " + e.getMessage());
        }
    }
    
    /**
     * Met à jour le label du total de produits
     */
    private void mettreAJourTotal() {
        int total = produitsFiltres.size();
        totalProduitsLabel.setText("Total: " + total + " produit(s)");
    }
    
    @FXML
    private void handleRetour() {
        try {
            javafx.stage.Stage stage = (javafx.stage.Stage) produitsContainer.getScene().getWindow();
            util.FXMLUtils.changeScene(stage, "/view/AdminDashboard.fxml", "Dashboard Administrateur");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", 
                     "Erreur lors du retour: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleRafraichir() {
        chargerCategories();
        chargerProduits();
    }
    
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

