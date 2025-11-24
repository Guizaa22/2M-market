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

    // ============================================
    // CONSTANTES DE STYLE
    // ============================================
    private static final String STYLE_CARD_BASE =
            "-fx-background-color: white; " +
                    "-fx-background-radius: 12; " +
                    "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.15), 10, 0, 0, 4); " +
                    "-fx-border-color: #e0e0e0; " +
                    "-fx-border-width: 1; " +
                    "-fx-border-radius: 12;";

    private static final String STYLE_CARD_HOVER =
            "-fx-background-color: linear-gradient(to bottom, #f9f9f9, #f5f5f5); " +
                    "-fx-background-radius: 12; " +
                    "-fx-effect: dropshadow(three-pass-box, rgba(76, 175, 80, 0.4), 18, 0, 0, 8); " +
                    "-fx-border-color: #4CAF50; " +
                    "-fx-border-width: 2.5; " +
                    "-fx-border-radius: 12;";

    private static final String STYLE_BUTTON_BASE =
            "-fx-background-color: linear-gradient(to bottom, #4CAF50, #2E7D32); " +
                    "-fx-text-fill: white; " +
                    "-fx-font-size: 15px; " +
                    "-fx-font-weight: bold; " +
                    "-fx-background-radius: 8; " +
                    "-fx-cursor: hand; " +
                    "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 5, 0, 0, 2);";

    private static final String STYLE_BUTTON_HOVER =
            "-fx-background-color: linear-gradient(to bottom, #66BB6A, #4CAF50); " +
                    "-fx-text-fill: white; " +
                    "-fx-font-size: 16px; " +
                    "-fx-font-weight: bold; " +
                    "-fx-background-radius: 8; " +
                    "-fx-cursor: hand; " +
                    "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 8, 0, 0, 4); " +
                    "-fx-scale-x: 1.08; " +
                    "-fx-scale-y: 1.08;";

    private static final int CARD_WIDTH = 220;
    private static final int CARD_HEIGHT = 280;
    private static final int BUTTON_WIDTH = 184;
    private static final int BUTTON_HEIGHT = 40;
    private static final int ANIMATION_DURATION = 200;
    private static final int HOVER_TRANSLATE_Y = -8;

    // ============================================
    // COMPOSANTS FXML
    // ============================================
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

    // ============================================
    // ATTRIBUTS
    // ============================================
    private ProduitDAO produitDAO;
    private String categorie;
    private static javafx.collections.ObservableList<DetailVente> panierGlobal;

    // ============================================
    // INITIALISATION
    // ============================================
    @FXML
    private void initialize() {
        produitDAO = new ProduitDAO();
        initialiserPanierGlobal();
        updatePanierCount();
    }

    /**
     * Initialise le panier global s'il n'existe pas
     */
    private void initialiserPanierGlobal() {
        if (panierGlobal == null) {
            panierGlobal = javafx.collections.FXCollections.observableArrayList();
        }
    }

    /**
     * Définit la catégorie et charge les produits
     */
    public void setCategorie(String categorie) {
        this.categorie = categorie;
        categorieLabel.setText("Catégorie: " + categorie);
        chargerProduits();
    }

    // ============================================
    // GESTION DES PRODUITS
    // ============================================

    /**
     * Charge et affiche tous les produits de la catégorie
     */
    private void chargerProduits() {
        try {
            // Configuration du conteneur
            produitsContainer.getChildren().clear();
            produitsContainer.setHgap(15);
            produitsContainer.setVgap(15);
            produitsContainer.setPadding(new Insets(20));

            // Récupération des produits
            java.util.List<Produit> produits = produitDAO.findByCategorie(categorie);

            // Vérifier si des produits existent
            if (produits == null || produits.isEmpty()) {
                afficherMessageAucunProduit();
                return;
            }

            // Créer une carte pour chaque produit
            for (Produit produit : produits) {
                VBox card = createProductCard(produit);
                produitsContainer.getChildren().add(card);
            }

            System.out.println("✓ " + produits.size() + " produit(s) chargé(s) pour la catégorie: " + categorie);

        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des produits: " + e.getMessage());
            e.printStackTrace();
            afficherErreurChargement();
        }
    }

    /**
     * Crée une carte produit complète avec tous les détails
     */
    private VBox createProductCard(Produit produit) {
        VBox card = new VBox(12);
        card.setPrefSize(CARD_WIDTH, CARD_HEIGHT);
        card.setPadding(new Insets(18));
        card.setStyle(STYLE_CARD_BASE);

        // Contenu de la carte
        Label nomLabel = creerLabelNom(produit);
        Label categorieLabel = creerLabelCategorie(produit);
        Label codeBarreLabel = creerLabelCodeBarre(produit);
        Label prixLabel = creerLabelPrix(produit);
        Label stockLabel = creerLabelStock(produit);
        Button ajouterButton = creerBoutonAjouter(produit);

        // Effets hover sur la carte
        configurerEffetsHoverCarte(card);

        card.getChildren().addAll(nomLabel, categorieLabel, codeBarreLabel,
                prixLabel, stockLabel, ajouterButton);

        return card;
    }

    /**
     * Crée le label du nom du produit
     */
    private Label creerLabelNom(Produit produit) {
        Label nomLabel = new Label(produit.getNom());
        nomLabel.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: #2E7D32;");
        nomLabel.setWrapText(true);
        nomLabel.setMaxWidth(BUTTON_WIDTH);
        nomLabel.setMinHeight(40);
        return nomLabel;
    }

    /**
     * Crée le label de la catégorie
     */
    private Label creerLabelCategorie(Produit produit) {
        String categorieProduit = produit.getCategorie();
        if (categorieProduit == null || categorieProduit.isEmpty()) {
            categorieProduit = "Sans catégorie";
        }
        Label categorieLabel = new Label("🏷️ " + categorieProduit);
        categorieLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #666; -fx-font-style: italic;");
        return categorieLabel;
    }

    /**
     * Crée le label du code-barres
     */
    private Label creerLabelCodeBarre(Produit produit) {
        Label codeBarreLabel = new Label("📋 " + produit.getCodeBarre());
        codeBarreLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #999;");
        return codeBarreLabel;
    }

    /**
     * Crée le label du prix
     */
    private Label creerLabelPrix(Produit produit) {
        Label prixLabel = new Label(String.format("%.2f €", produit.getPrixVenteDefaut()));
        prixLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #4CAF50;");
        return prixLabel;
    }

    /**
     * Crée le label du stock avec couleur selon disponibilité
     */
    private Label creerLabelStock(Produit produit) {
        Label stockLabel = new Label("📦 Stock: " + produit.getQuantiteStock());
        String couleur = determinerCouleurStock(produit);
        stockLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: " + couleur + "; -fx-font-weight: bold;");
        return stockLabel;
    }

    /**
     * Détermine la couleur du stock selon la disponibilité
     */
    private String determinerCouleurStock(Produit produit) {
        if (produit.isStockFaible()) {
            return "#f44336"; // Rouge
        } else if (produit.getQuantiteStock() > 50) {
            return "#4CAF50"; // Vert
        } else {
            return "#FF9800"; // Orange
        }
    }

    /**
     * Crée le bouton Ajouter au panier
     */
    private Button creerBoutonAjouter(Produit produit) {
        Button ajouterButton = new Button("➕ Ajouter");
        ajouterButton.setPrefWidth(BUTTON_WIDTH);
        ajouterButton.setPrefHeight(BUTTON_HEIGHT);
        ajouterButton.setStyle(STYLE_BUTTON_BASE);

        // Effets hover
        ajouterButton.setOnMouseEntered(e -> ajouterButton.setStyle(STYLE_BUTTON_HOVER));
        ajouterButton.setOnMouseExited(e -> ajouterButton.setStyle(STYLE_BUTTON_BASE));

        ajouterButton.setOnAction(e -> ajouterAuPanier(produit));

        return ajouterButton;
    }

    /**
     * Configure les effets hover avec animation pour la carte
     */
    private void configurerEffetsHoverCarte(VBox card) {
        card.setOnMouseEntered(e -> {
            card.setStyle(STYLE_CARD_HOVER);
            animerCarte(card, HOVER_TRANSLATE_Y);
        });

        card.setOnMouseExited(e -> {
            card.setStyle(STYLE_CARD_BASE);
            animerCarte(card, 0);
        });
    }

    /**
     * Anime la carte avec une translation verticale
     */
    private void animerCarte(VBox card, double translateY) {
        TranslateTransition transition = new TranslateTransition(
                Duration.millis(ANIMATION_DURATION), card
        );
        transition.setToY(translateY);
        transition.play();
    }

    /**
     * Affiche un message si aucun produit n'est disponible
     */
    private void afficherMessageAucunProduit() {
        VBox messageBox = creerBoiteMessage(
                "📭",
                "Aucun produit disponible",
                "Cette catégorie ne contient aucun produit en stock"
        );
        produitsContainer.getChildren().add(messageBox);
    }

    /**
     * Affiche un message d'erreur de chargement
     */
    private void afficherErreurChargement() {
        VBox errorBox = creerBoiteMessage(
                "⚠️",
                "Erreur de chargement",
                "Impossible de charger les produits. Vérifiez la connexion à la base de données."
        );

        Button retryButton = new Button("🔄 Réessayer");
        retryButton.setStyle(
                "-fx-background-color: #2196F3; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-weight: bold; " +
                        "-fx-padding: 10 20; " +
                        "-fx-background-radius: 5; " +
                        "-fx-cursor: hand;"
        );
        retryButton.setOnAction(e -> chargerProduits());
        errorBox.getChildren().add(retryButton);

        produitsContainer.getChildren().add(errorBox);
    }

    /**
     * Crée une boîte de message centrée
     */
    private VBox creerBoiteMessage(String icone, String titre, String description) {
        VBox box = new VBox(15);
        box.setAlignment(javafx.geometry.Pos.CENTER);
        box.setStyle("-fx-padding: 40px;");

        Label iconLabel = new Label(icone);
        iconLabel.setStyle("-fx-font-size: 64px;");

        Label titleLabel = new Label(titre);
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #666;");

        Label descLabel = new Label(description);
        descLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #999;");
        descLabel.setWrapText(true);
        descLabel.setMaxWidth(400);
        descLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        box.getChildren().addAll(iconLabel, titleLabel, descLabel);
        return box;
    }

    // ============================================
    // GESTION DU PANIER
    // ============================================

    /**
     * Ajoute un produit au panier ou incrémente la quantité
     */
    private void ajouterAuPanier(Produit produit) {
        // Vérifier si le produit est déjà dans le panier
        DetailVente detailExistant = rechercherProduitDansPanier(produit.getId());

        if (detailExistant != null) {
            incrementerQuantitePanier(detailExistant, produit);
        } else {
            ajouterNouveauProduitAuPanier(produit);
        }

        updatePanierCount();
    }

    /**
     * Recherche un produit dans le panier par son ID
     */
    private DetailVente rechercherProduitDansPanier(int produitId) {
        return panierGlobal.stream()
                .filter(d -> d.getProduitId() == produitId)
                .findFirst()
                .orElse(null);
    }

    /**
     * Incrémente la quantité d'un produit existant dans le panier
     */
    private void incrementerQuantitePanier(DetailVente detail, Produit produit) {
        int nouvelleQuantite = detail.getQuantite() + 1;

        if (nouvelleQuantite > produit.getQuantiteStock()) {
            afficherAlerte(
                    Alert.AlertType.WARNING,
                    "Stock insuffisant",
                    "Stock disponible: " + produit.getQuantiteStock()
            );
            return;
        }

        detail.setQuantite(nouvelleQuantite);
    }

    /**
     * Ajoute un nouveau produit au panier
     */
    private void ajouterNouveauProduitAuPanier(Produit produit) {
        DetailVente detail = new DetailVente();
        detail.setProduitId(produit.getId());
        detail.setQuantite(1);
        detail.setPrixVenteUnitaire(produit.getPrixVenteDefaut());
        detail.setPrixAchatUnitaire(produit.getPrixAchatActuel());
        detail.setProduit(produit);
        panierGlobal.add(detail);
    }

    /**
     * Met à jour le compteur d'articles dans le panier
     */
    private void updatePanierCount() {
        int count = panierGlobal != null ? panierGlobal.size() : 0;
        if (panierCountLabel != null) {
            panierCountLabel.setText("Panier: " + count + " article(s)");
        }
    }

    /**
     * Retourne le panier global
     */
    public static javafx.collections.ObservableList<DetailVente> getPanierGlobal() {
        if (panierGlobal == null) {
            panierGlobal = javafx.collections.FXCollections.observableArrayList();
        }
        return panierGlobal;
    }

    // ============================================
    // NAVIGATION
    // ============================================

    @FXML
    private void handleRetour() {
        try {
            Stage stage = (Stage) retourButton.getScene().getWindow();
            util.FXMLUtils.changeScene(stage, "/view/CaisseCategories.fxml", "Catégories");
        } catch (Exception e) {
            System.err.println("Erreur lors du retour: " + e.getMessage());
            afficherAlerte(
                    Alert.AlertType.ERROR,
                    "Erreur",
                    "Erreur lors du retour: " + e.getMessage()
            );
        }
    }

    @FXML
    private void handleVoirPanier() {
        try {
            Stage stage = (Stage) panierButton.getScene().getWindow();
            util.FXMLUtils.changeScene(stage, "/view/Caisse.fxml", "Caisse - Point de Vente");
        } catch (Exception e) {
            afficherAlerte(
                    Alert.AlertType.ERROR,
                    "Erreur",
                    "Erreur lors de l'ouverture du panier: " + e.getMessage()
            );
        }
    }

    // ============================================
    // UTILITAIRES
    // ============================================

    /**
     * Affiche une alerte
     */
    private void afficherAlerte(Alert.AlertType type, String titre, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}