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
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
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
     * Crée une carte produit avec effets 3D et hover - IMPROVED LAYOUT
     */
    private VBox creerCarteProduit(Produit produit) {
        VBox card = new VBox(12);
        card.getStyleClass().add("product-card");
        card.setAlignment(Pos.TOP_LEFT);
        card.setPadding(new Insets(18));

        // ============================================
        // HEADER: Category Badge (Small, Top Right)
        // ============================================
        HBox headerBox = new HBox();
        headerBox.setAlignment(Pos.CENTER_RIGHT);
        headerBox.getStyleClass().add("product-card-header");
        headerBox.setPadding(new Insets(0, 0, 8, 0));
        
        Label catLabel = new Label(produit.getCategorie() != null && !produit.getCategorie().isEmpty() 
            ? produit.getCategorie() : "Non catégorisé");
        catLabel.getStyleClass().add("product-category");
        headerBox.getChildren().add(catLabel);

        // ============================================
        // CONTENT: Main Product Information
        // ============================================
        VBox contentBox = new VBox(10);
        contentBox.getStyleClass().add("product-card-content");
        contentBox.setAlignment(Pos.TOP_LEFT);
        contentBox.setPadding(new Insets(8, 0, 0, 0));

        // PRODUCT NAME - BIG & CLEAR (Primary Focus)
        Label nomLabel = new Label(produit.getNom());
        nomLabel.getStyleClass().add("product-name");
        nomLabel.setWrapText(true);
        nomLabel.setMaxWidth(Double.MAX_VALUE);
        nomLabel.setAlignment(Pos.TOP_LEFT);

        // PRICE BOX - LARGE & PROMINENT with both prices
        VBox priceBox = new VBox(5);
        priceBox.setAlignment(Pos.CENTER_LEFT);
        priceBox.setPadding(new Insets(8, 0, 8, 0));
        priceBox.setStyle("-fx-background-color: rgba(76, 175, 80, 0.1); -fx-background-radius: 8; -fx-padding: 10;");
        
        HBox prixVenteBox = new HBox(8);
        prixVenteBox.setAlignment(Pos.CENTER_LEFT);
        Label prixVenteIcon = new Label("💰");
        prixVenteIcon.setStyle("-fx-font-size: 18px;");
        Label prixVenteLabel = new Label(String.format("Prix Vente: %.2f DT", produit.getPrixVenteDefaut()));
        prixVenteLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2E7D32;");
        prixVenteBox.getChildren().addAll(prixVenteIcon, prixVenteLabel);
        
        HBox prixAchatBox = new HBox(8);
        prixAchatBox.setAlignment(Pos.CENTER_LEFT);
        Label prixAchatIcon = new Label("🏷️");
        prixAchatIcon.setStyle("-fx-font-size: 14px;");
        Label prixAchatLabel = new Label(String.format("Prix Achat: %.2f DT", produit.getPrixAchatActuel()));
        prixAchatLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;");
        prixAchatBox.getChildren().addAll(prixAchatIcon, prixAchatLabel);
        
        priceBox.getChildren().addAll(prixVenteBox, prixAchatBox);

        // DETAILS CONTAINER - Clear and comprehensive info
        VBox detailsContainer = new VBox(8);
        detailsContainer.setAlignment(Pos.TOP_LEFT);
        detailsContainer.setPadding(new Insets(8, 0, 8, 0));

        // Code barre
        if (produit.getCodeBarre() != null && !produit.getCodeBarre().isEmpty()) {
            HBox codeBox = new HBox(8);
            codeBox.setAlignment(Pos.CENTER_LEFT);
            Label codeIcon = new Label("📋");
            codeIcon.setStyle("-fx-font-size: 16px;");
            Label codeLabel = new Label("Code: " + produit.getCodeBarre());
            codeLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #333;");
            codeBox.getChildren().addAll(codeIcon, codeLabel);
            detailsContainer.getChildren().add(codeBox);
        }

        // Unité
        if (produit.getUnite() != null && !produit.getUnite().isEmpty()) {
            HBox uniteBox = new HBox(8);
            uniteBox.setAlignment(Pos.CENTER_LEFT);
            Label uniteIcon = new Label("📦");
            uniteIcon.setStyle("-fx-font-size: 16px;");
            Label uniteLabel = new Label("Unité: " + produit.getUnite());
            uniteLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #555;");
            uniteBox.getChildren().addAll(uniteIcon, uniteLabel);
            detailsContainer.getChildren().add(uniteBox);
        }
        
        // Seuil d'alerte
        HBox seuilBox = new HBox(8);
        seuilBox.setAlignment(Pos.CENTER_LEFT);
        Label seuilIcon = new Label("⚠️");
        seuilIcon.setStyle("-fx-font-size: 16px;");
        Label seuilLabel = new Label("Seuil alerte: " + produit.getSeuilAlerte());
        seuilLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #FF9800; -fx-font-weight: bold;");
        seuilBox.getChildren().addAll(seuilIcon, seuilLabel);
        detailsContainer.getChildren().add(seuilBox);

        contentBox.getChildren().addAll(nomLabel, priceBox, detailsContainer);

        VBox spacer = new VBox();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        HBox footerBox = new HBox(12);
        footerBox.getStyleClass().add("product-card-footer");
        footerBox.setAlignment(Pos.CENTER_LEFT);
        footerBox.setPadding(new Insets(8, 0, 0, 0));

        // Stock Container - Large and prominent with clear status
        VBox stockContainerBox = new VBox(5);
        stockContainerBox.setAlignment(Pos.CENTER);
        stockContainerBox.setPadding(new Insets(10));
        stockContainerBox.setStyle("-fx-background-radius: 8;");
        
        // Determine stock color and message
        String stockColor;
        String stockIcon;
        String stockMessage;
        
        if (produit.getQuantiteStock() == 0) {
            stockColor = "-fx-background-color: rgba(244, 67, 54, 0.15);";
            stockIcon = "❌";
            stockMessage = "RUPTURE";
        } else if (produit.isStockFaible() || produit.getQuantiteStock() <= produit.getSeuilAlerte()) {
            stockColor = "-fx-background-color: rgba(255, 152, 0, 0.15);";
            stockIcon = "⚠️";
            stockMessage = "FAIBLE";
        } else if (produit.getQuantiteStock() > 50) {
            stockColor = "-fx-background-color: rgba(76, 175, 80, 0.15);";
            stockIcon = "✅";
            stockMessage = "BON";
        } else {
            stockColor = "-fx-background-color: rgba(33, 150, 243, 0.15);";
            stockIcon = "📦";
            stockMessage = "MOYEN";
        }
        
        stockContainerBox.setStyle(stockColor + " -fx-background-radius: 8;");
        
        HBox stockQtyBox = new HBox(8);
        stockQtyBox.setAlignment(Pos.CENTER_LEFT);
        Label stockIconLabel = new Label(stockIcon);
        stockIconLabel.setStyle("-fx-font-size: 22px;");
        Label stockQtyLabel = new Label("Stock: " + produit.getQuantiteStock());
        stockQtyLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #333;");
        stockQtyBox.getChildren().addAll(stockIconLabel, stockQtyLabel);
        Label stockStatusLabel = new Label(stockMessage);
        stockStatusLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #666;");
        stockContainerBox.getChildren().addAll(stockQtyBox, stockStatusLabel);

        Button actionButton = new Button("➕ Ajouter");
        actionButton.getStyleClass().addAll("btn", "btn-primary");
        actionButton.setMinHeight(45);
        actionButton.setAlignment(Pos.CENTER);
        actionButton.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        actionButton.setOnAction(e -> ouvrirAjoutStock(produit));
        HBox.setHgrow(actionButton, Priority.ALWAYS);

        footerBox.getChildren().addAll(stockContainerBox, actionButton);

        // ============================================
        // ASSEMBLE CARD
        // ============================================
        card.getChildren().addAll(
            headerBox,
            contentBox,
            spacer,
            footerBox
        );

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

