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

    // ============================================
    // CONSTANTES DE STYLE
    // ============================================
    private static final String STYLE_BUTTON_BASE =
            "-fx-font-size: 18px; " +
                    "-fx-font-weight: bold; " +
                    "-fx-background-color: linear-gradient(to bottom, #4CAF50, #2E7D32); " +
                    "-fx-text-fill: white; " +
                    "-fx-background-radius: 20; " +
                    "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 12, 0, 0, 6); " +
                    "-fx-cursor: hand; " +
                    "-fx-padding: 15;";

    private static final String STYLE_BUTTON_HOVER =
            "-fx-font-size: 20px; " +
                    "-fx-font-weight: bold; " +
                    "-fx-background-color: linear-gradient(to bottom, #66BB6A, #4CAF50); " +
                    "-fx-text-fill: white; " +
                    "-fx-background-radius: 20; " +
                    "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.5), 18, 0, 0, 10); " +
                    "-fx-scale-x: 1.08; " +
                    "-fx-scale-y: 1.08; " +
                    "-fx-cursor: hand; " +
                    "-fx-padding: 15;";

    private static final int BUTTON_WIDTH = 220;
    private static final int BUTTON_HEIGHT = 180;
    private static final int ICON_SIZE = 48;

    // ============================================
    // COMPOSANTS FXML
    // ============================================
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

    // ============================================
    // ATTRIBUTS
    // ============================================
    private ProduitDAO produitDAO;
    private Produit produitTrouve = null;

    // ============================================
    // INITIALISATION
    // ============================================
    @FXML
    private void initialize() {
        produitDAO = new ProduitDAO();
        quantiteField.setText("1");
        rechercheField.requestFocus();

        configurerRecherche();
        chargerStyles();
        updatePanierCount();
        ecouterChangementsPanier();
        chargerCategories();
    }

    /**
     * Configure le listener de recherche
     */
    private void configurerRecherche() {
        rechercheField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.trim().isEmpty()) {
                rechercherProduit(newVal.trim());
            } else {
                produitInfoLabel.setText("");
                produitTrouve = null;
            }
        });
    }

    /**
     * Charge les styles CSS
     */
    private void chargerStyles() {
        javafx.application.Platform.runLater(() -> {
            if (panierButton != null && panierButton.getScene() != null) {
                javafx.scene.Parent root = panierButton.getScene().getRoot();
                if (root != null) {
                    try {
                        String cssUrl = getClass().getResource("/styles/caisse.css").toExternalForm();
                        if (!root.getStylesheets().contains(cssUrl)) {
                            root.getStylesheets().add(cssUrl);
                        }
                    } catch (Exception e) {
                        System.err.println("Impossible de charger le CSS: " + e.getMessage());
                    }
                }
            }
        });
    }

    /**
     * Écoute les changements du panier global
     */
    private void ecouterChangementsPanier() {
        if (CategorieProduitsController.getPanierGlobal() != null) {
            CategorieProduitsController.getPanierGlobal().addListener(
                    (javafx.collections.ListChangeListener.Change<? extends DetailVente> c) -> {
                        updatePanierCount();
                    }
            );
        }
    }

    // ============================================
    // GESTION DES CATÉGORIES
    // ============================================

    /**
     * Charge et affiche toutes les catégories depuis la base de données
     */
    private void chargerCategories() {
        try {
            // Configuration du conteneur
            categoriesContainer.getChildren().clear();
            categoriesContainer.setHgap(20);
            categoriesContainer.setVgap(20);
            categoriesContainer.setPadding(new Insets(20));

            // Récupération des catégories depuis la base de données
            List<String> categories = produitDAO.findAllCategories();

            // Vérifier si des catégories existent
            if (categories == null || categories.isEmpty()) {
                afficherMessageAucuneCategorie();
                return;
            }

            // Créer un bouton pour chaque catégorie
            for (String categorie : categories) {
                if (categorie != null && !categorie.trim().isEmpty()) {
                    Button categoryButton = createCategoryButton(categorie);
                    categoriesContainer.getChildren().add(categoryButton);
                }
            }

            System.out.println("✓ " + categories.size() + " catégorie(s) chargée(s)");

        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des catégories: " + e.getMessage());
            e.printStackTrace();
            afficherErreurChargement();
        }
    }

    /**
     * Crée un bouton pour une catégorie
     */
    private Button createCategoryButton(String categorie) {
        // Créer un VBox pour contenir l'icône et le texte
        VBox content = new VBox(8);
        content.setAlignment(javafx.geometry.Pos.CENTER);

        // Icône selon la catégorie
        String icon = getCategoryIcon(categorie);
        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: " + ICON_SIZE + "px;");

        Label textLabel = new Label(categorie);
        textLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");
        textLabel.setWrapText(true);
        textLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        content.getChildren().addAll(iconLabel, textLabel);

        Button button = new Button();
        button.setGraphic(content);
        button.setPrefSize(BUTTON_WIDTH, BUTTON_HEIGHT);
        button.setContentDisplay(javafx.scene.control.ContentDisplay.CENTER);
        button.setStyle(STYLE_BUTTON_BASE);

        // Effet hover
        button.setOnMouseEntered(e -> button.setStyle(STYLE_BUTTON_HOVER));
        button.setOnMouseExited(e -> button.setStyle(STYLE_BUTTON_BASE));

        button.setOnAction(e -> ouvrirCategorie(categorie));

        return button;
    }

    /**
     * Retourne l'icône appropriée pour une catégorie
     */
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

    /**
     * Affiche un message si aucune catégorie n'est disponible
     */
    private void afficherMessageAucuneCategorie() {
        VBox messageBox = new VBox(15);
        messageBox.setAlignment(javafx.geometry.Pos.CENTER);
        messageBox.setStyle("-fx-padding: 40px;");

        Label iconLabel = new Label("📭");
        iconLabel.setStyle("-fx-font-size: 64px;");

        Label messageLabel = new Label("Aucune catégorie disponible");
        messageLabel.setStyle(
                "-fx-font-size: 18px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-text-fill: #999;"
        );

        Label infoLabel = new Label("Ajoutez des produits avec des catégories dans la gestion des stocks");
        infoLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;");
        infoLabel.setWrapText(true);
        infoLabel.setMaxWidth(400);
        infoLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        messageBox.getChildren().addAll(iconLabel, messageLabel, infoLabel);
        categoriesContainer.getChildren().add(messageBox);
    }

    /**
     * Affiche un message d'erreur en cas de problème de chargement
     */
    private void afficherErreurChargement() {
        VBox errorBox = new VBox(15);
        errorBox.setAlignment(javafx.geometry.Pos.CENTER);
        errorBox.setStyle("-fx-padding: 40px;");

        Label iconLabel = new Label("⚠️");
        iconLabel.setStyle("-fx-font-size: 64px;");

        Label errorLabel = new Label("Erreur de chargement");
        errorLabel.setStyle(
                "-fx-font-size: 18px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-text-fill: #f44336;"
        );

        Label infoLabel = new Label("Impossible de charger les catégories. Vérifiez la connexion à la base de données.");
        infoLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;");
        infoLabel.setWrapText(true);
        infoLabel.setMaxWidth(400);
        infoLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        Button retryButton = new Button("🔄 Réessayer");
        retryButton.setStyle(
                "-fx-background-color: #2196F3; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-weight: bold; " +
                        "-fx-padding: 10 20; " +
                        "-fx-background-radius: 5; " +
                        "-fx-cursor: hand;"
        );
        retryButton.setOnAction(e -> chargerCategories());

        errorBox.getChildren().addAll(iconLabel, errorLabel, infoLabel, retryButton);
        categoriesContainer.getChildren().add(errorBox);

        showAlert(Alert.AlertType.ERROR, "Erreur",
                "Impossible de charger les catégories depuis la base de données.");
    }

    /**
     * Ouvre la vue des produits d'une catégorie
     */
    private void ouvrirCategorie(String categorie) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/CategorieProduits.fxml"));
            javafx.scene.Parent root = loader.load();

            CategorieProduitsController controller = loader.getController();
            controller.setCategorie(categorie);

            Stage stage = (Stage) panierButton.getScene().getWindow();
            stage.getScene().setRoot(root);
            stage.setTitle("Produits - " + categorie);
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement de la page catégorie: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible d'ouvrir la catégorie: " + e.getMessage());
        }
    }

    // ============================================
    // GESTION DE LA RECHERCHE RAPIDE
    // ============================================

    /**
     * Recherche un produit et affiche les informations
     */
    private void rechercherProduit(String recherche) {
        produitTrouve = produitDAO.rechercherProduit(recherche);

        if (produitTrouve != null) {
            produitInfoLabel.setText("✓ " + produitTrouve.getNom() + " - " +
                    String.format("%.2f €", produitTrouve.getPrixVenteDefaut()) +
                    " (Stock: " + produitTrouve.getQuantiteStock() + ")");
            produitInfoLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold; -fx-font-size: 13px;");

            // Ajout automatique pour code-barres
            if (estCodeBarre(recherche)) {
                ajouterAutomatiquementApresDelai();
            }
        } else {
            produitInfoLabel.setText("❌ Produit introuvable");
            produitInfoLabel.setStyle("-fx-text-fill: #f44336; -fx-font-weight: bold; -fx-font-size: 13px;");
        }
    }

    /**
     * Vérifie si la recherche est un code-barres
     */
    private boolean estCodeBarre(String recherche) {
        return recherche.matches("\\d+") && recherche.length() >= 8;
    }

    /**
     * Ajoute automatiquement le produit après un court délai
     */
    private void ajouterAutomatiquementApresDelai() {
        javafx.application.Platform.runLater(() -> {
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            handleAjouterRapide();
        });
    }

    @FXML
    private void handleRecherche() {
        handleAjouterRapide();
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
            reinitialiserRecherche();
            return;
        }

        if (produitTrouve.getQuantiteStock() <= 0) {
            showAlert(Alert.AlertType.WARNING, "Stock insuffisant",
                    "Ce produit n'est plus en stock.");
            reinitialiserRecherche();
            return;
        }

        int quantite = obtenirQuantite();
        if (quantite <= 0) {
            showAlert(Alert.AlertType.WARNING, "Quantité invalide",
                    "Veuillez entrer une quantité valide.");
            return;
        }

        if (quantite > produitTrouve.getQuantiteStock()) {
            showAlert(Alert.AlertType.WARNING, "Stock insuffisant",
                    "Stock disponible: " + produitTrouve.getQuantiteStock());
            return;
        }

        ajouterAuPanier(quantite);
    }

    /**
     * Obtient la quantité saisie
     */
    private int obtenirQuantite() {
        try {
            int quantite = Integer.parseInt(quantiteField.getText().trim());
            return quantite > 0 ? quantite : -1;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * Ajoute un produit au panier
     */
    private void ajouterAuPanier(int quantite) {
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

        // Ajout automatique sans popup
        reinitialiserRecherche();
        updatePanierCount();
    }

    /**
     * Réinitialise les champs de recherche
     */
    private void reinitialiserRecherche() {
        rechercheField.clear();
        quantiteField.setText("1");
        produitInfoLabel.setText("");
        produitTrouve = null;
        rechercheField.requestFocus();
    }

    // ============================================
    // GESTION DE LA QUANTITÉ
    // ============================================

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

    // ============================================
    // NAVIGATION
    // ============================================

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

    // ============================================
    // UTILITAIRES
    // ============================================

    /**
     * Met à jour le compteur du panier
     */
    private void updatePanierCount() {
        int count = 0;
        if (CategorieProduitsController.getPanierGlobal() != null) {
            count = CategorieProduitsController.getPanierGlobal().size();
        }
        if (panierCountLabel != null) {
            panierCountLabel.setText("Panier: " + count);
        }
    }

    /**
     * Affiche une alerte
     */
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}