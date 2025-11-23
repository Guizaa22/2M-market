package controller;

import java.io.IOException;
import java.util.List;

import dao.ProduitDAO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.DetailVente;
import model.Produit;

/**
 * Contrôleur pour l'interface de sélection de catégories
 */
public class CaisseCategoriesController {
    
    @FXML
    private FlowPane categoriesContainer;
    
    @FXML
    private Button panierButton;
    
    @FXML
    private Button deconnexionButton;
    
    @FXML
    private Label panierCountLabel;
    
    @FXML
    private TextField rechercheField;
    
    @FXML
    private TextField quantiteField;
    
    @FXML
    private Button ajouterRapideButton;
    
    @FXML
    private Button plusButton;
    
    @FXML
    private Button moinsButton;
    
    @FXML
    private Label produitInfoLabel;
    
    private ProduitDAO produitDAO;
    
    @FXML
    private void initialize() {
        produitDAO = new ProduitDAO();
        quantiteField.setText("1");
        rechercheField.requestFocus();
        
        // Écouter les changements dans le champ de recherche pour auto-remplissage
        rechercheField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.trim().isEmpty()) {
                rechercherProduit(newVal.trim());
            } else {
                produitInfoLabel.setText("");
            }
        });
        
        // Ajouter le CSS après que la scène soit chargée
        javafx.application.Platform.runLater(() -> {
            if (panierButton != null && panierButton.getScene() != null) {
                javafx.scene.Parent root = panierButton.getScene().getRoot();
                if (root != null) {
                    String cssUrl = getClass().getResource("/styles/caisse.css").toExternalForm();
                    if (!root.getStylesheets().contains(cssUrl)) {
                        root.getStylesheets().add(cssUrl);
                    }
                }
            }
        });
        
        // Mettre à jour le compteur de panier
        updatePanierCount();
        
        // Écouter les changements du panier global
        if (CategorieProduitsController.getPanierGlobal() != null) {
            CategorieProduitsController.getPanierGlobal().addListener(
                (javafx.collections.ListChangeListener.Change<? extends DetailVente> c) -> {
                    updatePanierCount();
                }
            );
        }
        
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
        // Créer un VBox pour contenir l'icône et le texte
        VBox content = new VBox(8);
        content.setAlignment(javafx.geometry.Pos.CENTER);
        
        // Icône selon la catégorie
        String icon = getCategoryIcon(categorie);
        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 48px;");
        
        Label textLabel = new Label(categorie);
        textLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");
        textLabel.setWrapText(true);
        textLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        
        content.getChildren().addAll(iconLabel, textLabel);
        
        Button button = new Button();
        button.setGraphic(content);
        button.setPrefSize(220, 180);
        button.setContentDisplay(javafx.scene.control.ContentDisplay.CENTER);
        button.setStyle(
            "-fx-font-size: 18px; " +
            "-fx-font-weight: bold; " +
            "-fx-background-color: linear-gradient(to bottom, #4CAF50, #2E7D32); " +
            "-fx-text-fill: white; " +
            "-fx-background-radius: 20; " +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 12, 0, 0, 6); " +
            "-fx-cursor: hand; " +
            "-fx-padding: 15;"
        );
        
        // Effet hover amélioré
        button.setOnMouseEntered(e -> {
            button.setStyle(
                "-fx-font-size: 20px; " +
                "-fx-font-weight: bold; " +
                "-fx-background-color: linear-gradient(to bottom, #66BB6A, #4CAF50); " +
                "-fx-text-fill: white; " +
                "-fx-background-radius: 20; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.5), 18, 0, 0, 10); " +
                "-fx-scale-x: 1.08; " +
                "-fx-scale-y: 1.08; " +
                "-fx-cursor: hand; " +
                "-fx-padding: 15;"
            );
        });
        
        button.setOnMouseExited(e -> {
            button.setStyle(
                "-fx-font-size: 18px; " +
                "-fx-font-weight: bold; " +
                "-fx-background-color: linear-gradient(to bottom, #4CAF50, #2E7D32); " +
                "-fx-text-fill: white; " +
                "-fx-background-radius: 20; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 12, 0, 0, 6); " +
                "-fx-scale-x: 1.0; " +
                "-fx-scale-y: 1.0; " +
                "-fx-cursor: hand; " +
                "-fx-padding: 15;"
            );
        });
        
        button.setOnAction(e -> ouvrirCategorie(categorie));
        
        return button;
    }
    
    private String getCategoryIcon(String categorie) {
        String cat = categorie.toLowerCase();
        if (cat.contains("aliment") || cat.contains("food")) {
            return "🍞";
        } else if (cat.contains("boisson") || cat.contains("drink")) {
            return "🥤";
        } else if (cat.contains("tabac") || cat.contains("tobacco")) {
            return "🚬";
        } else if (cat.contains("hygiene") || cat.contains("hygiène")) {
            return "🧴";
        } else if (cat.contains("divers") || cat.contains("other")) {
            return "📦";
        }
        return "🏷️";
    }
    
    private void ouvrirCategorie(String categorie) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/CategorieProduits.fxml"));
            VBox root = loader.load();
            
            CategorieProduitsController controller = loader.getController();
            controller.setCategorie(categorie);
            
            Stage stage = (Stage) panierButton.getScene().getWindow();
            stage.getScene().setRoot(root);
            stage.setTitle("Produits - " + categorie);
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement de la page catégorie: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleVoirPanier() {
        try {
            Stage stage = (Stage) panierButton.getScene().getWindow();
            util.FXMLUtils.changeScene(stage, "/view/Caisse.fxml", "Caisse - Point de Vente");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", 
                     "Erreur lors de l'ouverture du panier: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleDeconnexion() {
        ConnexionController.deconnecter();
        try {
            Stage stage = (Stage) deconnexionButton.getScene().getWindow();
            util.FXMLUtils.changeScene(stage, "/view/Connexion.fxml", "Connexion");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", 
                     "Erreur lors de la déconnexion: " + e.getMessage());
        }
    }
    
    private Produit produitTrouve = null; // Produit actuellement trouvé pour auto-ajout
    
    @FXML
    private void handleRecherche() {
        handleAjouterRapide();
    }
    
    @FXML
    private void handlePlusQuantite() {
        try {
            int quantite = Integer.parseInt(quantiteField.getText().trim());
            quantite++;
            quantiteField.setText(String.valueOf(quantite));
        } catch (NumberFormatException e) {
            quantiteField.setText("1");
        }
    }
    
    @FXML
    private void handleMoinsQuantite() {
        try {
            int quantite = Integer.parseInt(quantiteField.getText().trim());
            if (quantite > 1) {
                quantite--;
                quantiteField.setText(String.valueOf(quantite));
            }
        } catch (NumberFormatException e) {
            quantiteField.setText("1");
        }
    }
    
    private void rechercherProduit(String recherche) {
        produitTrouve = produitDAO.rechercherProduit(recherche);
        
        if (produitTrouve != null) {
            produitInfoLabel.setText("✓ " + produitTrouve.getNom() + " - " + 
                                   String.format("%.2f €", produitTrouve.getPrixVenteDefaut()) + 
                                   " (Stock: " + produitTrouve.getQuantiteStock() + ")");
            produitInfoLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold; -fx-font-size: 13px;");
            
            // Si c'est un code-barres (généralement numérique et long), ajouter automatiquement
            if (recherche.matches("\\d+") && recherche.length() >= 8) {
                // Code-barres détecté, ajouter automatiquement après un court délai
                javafx.application.Platform.runLater(() -> {
                    try {
                        Thread.sleep(300); // Petit délai pour laisser le temps de voir l'info
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    handleAjouterRapide();
                });
            }
        } else {
            produitInfoLabel.setText("❌ Produit introuvable");
            produitInfoLabel.setStyle("-fx-text-fill: #f44336; -fx-font-weight: bold; -fx-font-size: 13px;");
        }
    }
    
    @FXML
    private void handleAjouterRapide() {
        String recherche = rechercheField.getText().trim();
        
        if (recherche.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Recherche vide", 
                     "Veuillez entrer un nom de produit ou un code-barres.");
            return;
        }
        
        // Si produit déjà trouvé, utiliser celui-ci, sinon rechercher
        if (produitTrouve == null) {
            produitTrouve = produitDAO.rechercherProduit(recherche);
        }
        
        if (produitTrouve == null) {
            showAlert(Alert.AlertType.WARNING, "Produit introuvable", 
                     "Aucun produit trouvé avec ce nom ou code-barres.");
            rechercheField.clear();
            produitInfoLabel.setText("");
            return;
        }
        
        if (produitTrouve.getQuantiteStock() <= 0) {
            showAlert(Alert.AlertType.WARNING, "Stock insuffisant", 
                     "Ce produit n'est plus en stock.");
            rechercheField.clear();
            produitInfoLabel.setText("");
            produitTrouve = null;
            return;
        }
        
        int quantite;
        try {
            quantite = Integer.parseInt(quantiteField.getText().trim());
            if (quantite <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Quantité invalide", 
                     "Veuillez entrer une quantité valide.");
            return;
        }
        
        if (quantite > produitTrouve.getQuantiteStock()) {
            showAlert(Alert.AlertType.WARNING, "Stock insuffisant", 
                     "Stock disponible: " + produitTrouve.getQuantiteStock());
            return;
        }
        
        // Ajouter au panier global
        javafx.collections.ObservableList<DetailVente> panier = CategorieProduitsController.getPanierGlobal();
        DetailVente detailExistant = panier.stream()
            .filter(d -> d.getProduitId() == produitTrouve.getId())
            .findFirst()
            .orElse(null);
        
        if (detailExistant != null) {
            int nouvelleQuantite = detailExistant.getQuantite() + quantite;
            if (nouvelleQuantite > produitTrouve.getQuantiteStock()) {
                showAlert(Alert.AlertType.WARNING, "Stock insuffisant", 
                         "Quantité totale demandée dépasse le stock disponible.");
                return;
            }
            detailExistant.setQuantite(nouvelleQuantite);
        } else {
            DetailVente detail = new DetailVente();
            detail.setProduitId(produitTrouve.getId());
            detail.setQuantite(quantite);
            detail.setPrixVenteUnitaire(produitTrouve.getPrixVenteDefaut());
            detail.setPrixAchatUnitaire(produitTrouve.getPrixAchatActuel());
            detail.setProduit(produitTrouve);
            panier.add(detail);
        }
        
        String nomProduit = produitTrouve.getNom();
        
        updatePanierCount();
        rechercheField.clear();
        quantiteField.setText("1");
        produitInfoLabel.setText("");
        produitTrouve = null;
        rechercheField.requestFocus();
        
        showAlert(Alert.AlertType.INFORMATION, "Produit ajouté", 
                 nomProduit + " (" + quantite + "x) ajouté au panier !");
    }
    
    private void updatePanierCount() {
        int count = 0;
        if (CategorieProduitsController.getPanierGlobal() != null) {
            count = CategorieProduitsController.getPanierGlobal().size();
        }
        if (panierCountLabel != null) {
            panierCountLabel.setText("Panier: " + count);
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

