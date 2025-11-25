package controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import dao.ProduitDAO;
import dao.VenteDAO;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.DetailVente;
import model.Produit;
import model.Vente;
import util.TicketPrinter;

/**
 * Contrôleur principal de la caisse
 */
public class CaisseController {

    // ============================================
    // COMPOSANTS FXML
    // ============================================
    @FXML
    private VBox panierListContainer;

    @FXML
    private Label totalLabel;

    @FXML
    private Label tvaLabel;

    @FXML
    private Label userLabel;

    @FXML
    private Button validerButton;

    @FXML
    private Button annulerButton;

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
    private Button rechercheButton;

    @FXML
    private Button modifierQuantiteButton;

    @FXML
    private Button categoriesButton;

    // ============================================
    // ATTRIBUTS
    // ============================================
    private ProduitDAO produitDAO;
    private VenteDAO venteDAO;
    private String modePaiement = "ESPÈCES"; // Par défaut

    // ============================================
    // INITIALISATION
    // ============================================
    @FXML
    private void initialize() {
        produitDAO = new ProduitDAO();
        venteDAO = new VenteDAO();

        // Initialiser l'interface
        updatePanierView();
        updateTotal();

        // Écouter les changements du panier global
        CategorieProduitsController.getPanierGlobal().addListener((ListChangeListener.Change<? extends DetailVente> c) -> {
            updatePanierView();
            updateTotal();
        });
        
        // Charger les styles CSS si nécessaire
        Platform.runLater(() -> {
            if (panierListContainer.getScene() != null) {
                String globalCss = getClass().getResource("/styles/global.css").toExternalForm();
                String caisseCss = getClass().getResource("/styles/caisse.css").toExternalForm();
                if (!panierListContainer.getScene().getStylesheets().contains(globalCss)) {
                    panierListContainer.getScene().getStylesheets().add(globalCss);
                }
                if (!panierListContainer.getScene().getStylesheets().contains(caisseCss)) {
                    panierListContainer.getScene().getStylesheets().add(caisseCss);
                }
            }
        });
    }

    // ============================================
    // GESTION DU PANIER (AFFICHAGE)
    // ============================================

    /**
     * Met à jour l'affichage de la liste du panier
     */
    private void updatePanierView() {
        panierListContainer.getChildren().clear();

        if (CategorieProduitsController.getPanierGlobal().isEmpty()) {
            Label emptyLabel = new Label("Le panier est vide");
            emptyLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #999; -fx-padding: 20;");
            panierListContainer.getChildren().add(emptyLabel);
            return;
        }

        for (DetailVente detail : CategorieProduitsController.getPanierGlobal()) {
            HBox row = createPanierItemRow(detail);
            panierListContainer.getChildren().add(row);
        }
    }

    /**
     * Crée une ligne pour un article du panier
     */
    private HBox createPanierItemRow(DetailVente detail) {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("panier-card");
        row.setPadding(new Insets(10));

        // Récupérer le produit complet si nécessaire
        Produit produit = detail.getProduit();
        if (produit == null) {
            produit = produitDAO.findById(detail.getProduitId());
            detail.setProduit(produit);
        }
        
        final DetailVente finalDetail = detail;

        // Icône produit
        Label iconLabel = new Label("[P]");
        iconLabel.setStyle("-fx-font-size: 24px;");

        // Infos produit (Nom et Code)
        VBox infoBox = new VBox(5);
        infoBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(infoBox, Priority.ALWAYS);

        Label nomLabel = new Label(produit != null ? produit.getNom() : "Produit #" + detail.getProduitId());
        nomLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #333;");
        nomLabel.setWrapText(true);

        Label codeLabel = new Label(produit != null ? produit.getCodeBarre() : "");
        codeLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");

        infoBox.getChildren().addAll(nomLabel, codeLabel);

        // Quantité
        VBox quantiteContainer = new VBox(5);
        quantiteContainer.setAlignment(Pos.CENTER);
        
        Label qteTitleLabel = new Label("Qté");
        qteTitleLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #999;");
        
        Label qteLabel = new Label(String.valueOf(detail.getQuantite()));
        qteLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 18px; -fx-text-fill: #2E7D32;");
        
        quantiteContainer.getChildren().addAll(qteTitleLabel, qteLabel);

        // Prix
        VBox prixContainer = new VBox(5);
        prixContainer.setAlignment(Pos.CENTER_RIGHT);
        
        Label prixUnitLabel = new Label(String.format("%.2f € /u", detail.getPrixVenteUnitaire()));
        prixUnitLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");
        
        Label prixTotalLabel = new Label(String.format("%.2f €", detail.getSousTotal()));
        prixTotalLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 18px; -fx-text-fill: #2E7D32;");
        
        prixContainer.getChildren().addAll(prixTotalLabel, prixUnitLabel);

        // Actions
        VBox actionsBox = new VBox(5);
        actionsBox.setAlignment(Pos.CENTER);
        
        Button retirerButton = new Button("X");
        retirerButton.getStyleClass().addAll("btn", "btn-danger");
        retirerButton.setStyle("-fx-padding: 5 10; -fx-font-size: 12px;");
        retirerButton.setOnAction(e -> retirerDuPanier(finalDetail));
        
        Button modifierButton = new Button("Edit");
        modifierButton.getStyleClass().addAll("btn", "btn-secondary");
        modifierButton.setStyle("-fx-padding: 5 10; -fx-font-size: 12px;");
        modifierButton.setOnAction(e -> modifierQuantiteItem(finalDetail));
        
        actionsBox.getChildren().addAll(retirerButton, modifierButton);

        row.getChildren().addAll(iconLabel, infoBox, quantiteContainer, prixContainer, actionsBox);

        return row;
    }

    // ============================================
    // ACTIONS UTILISATEUR
    // ============================================

    @FXML
    private void handleValider() {
        if (CategorieProduitsController.getPanierGlobal().isEmpty()) {
            afficherAlerte(Alert.AlertType.WARNING, "Panier vide", "Veuillez ajouter des articles au panier.");
            return;
        }

        BigDecimal total = calculerTotal();

        // Créer la vente
        Vente vente = new Vente();
        vente.setDateVente(LocalDateTime.now());
        vente.setTotalVente(total);
        vente.setUtilisateurId(1); // TODO: Utiliser l'ID de l'utilisateur connecté
        
        // Ajouter les détails à la vente
        for (DetailVente detail : CategorieProduitsController.getPanierGlobal()) {
            vente.addDetail(detail);
        }

        // Sauvegarder la vente et ses détails
        boolean succes = venteDAO.create(vente);

        if (succes) {
            // Imprimer le ticket
            TicketPrinter.imprimerTicket(vente, CategorieProduitsController.getPanierGlobal());

            afficherAlerte(Alert.AlertType.INFORMATION, "Vente validée", "La vente a été enregistrée avec succès.");
            
            // Vider le panier
            CategorieProduitsController.getPanierGlobal().clear();
            updateTotal();
        } else {
            afficherAlerte(Alert.AlertType.ERROR, "Erreur", "Une erreur est survenue lors de l'enregistrement de la vente.");
        }
    }

    @FXML
    private void handleAnnuler() {
        if (!CategorieProduitsController.getPanierGlobal().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Annuler la vente");
            alert.setHeaderText("Êtes-vous sûr de vouloir vider le panier ?");
            alert.setContentText("Cette action est irréversible.");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                CategorieProduitsController.getPanierGlobal().clear();
                updateTotal();
            }
        }
    }

    @FXML
    private void handleScanner() {
        // Simulation scanner
        afficherAlerte(Alert.AlertType.INFORMATION, "Scanner", "Fonctionnalité de scanner à implémenter (nécessite matériel).");
    }

    @FXML
    private void handleCodeBarre() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Saisie Code-barres");
        dialog.setHeaderText("Entrez le code-barres du produit :");
        dialog.setContentText("Code-barres:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(codeBarre -> {
            Produit produit = produitDAO.findByCodeBarre(codeBarre);
            if (produit != null) {
                ajouterAuPanier(produit, 1);
            } else {
                afficherAlerte(Alert.AlertType.WARNING, "Produit introuvable", "Aucun produit trouvé avec ce code-barres.");
            }
        });
    }

    @FXML
    private void handleRechercheProduit() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Recherche Produit");
        dialog.setHeaderText("Rechercher par nom :");
        dialog.setContentText("Nom:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(nom -> {
            Produit produit = produitDAO.rechercherProduit(nom);
            if (produit != null) {
                ajouterAuPanier(produit, 1);
            } else {
                afficherAlerte(Alert.AlertType.WARNING, "Produit introuvable", "Aucun produit trouvé avec ce nom.");
            }
        });
    }

    @FXML
    private void handleModifierQuantite() {
        // Logique pour modifier la quantité du dernier article ou sélectionné
        afficherAlerte(Alert.AlertType.INFORMATION, "Info", "Utilisez les boutons crayons dans la liste pour modifier la quantité.");
    }

    @FXML
    private void handleCategories() {
        try {
            Stage stage = (Stage) categoriesButton.getScene().getWindow();
            util.FXMLUtils.changeScene(stage, "/view/CaisseCategories.fxml", "Catégories");
        } catch (Exception e) {
            e.printStackTrace();
            afficherAlerte(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir les catégories: " + e.getMessage());
        }
    }

    // ============================================
    // GESTION PAIEMENT
    // ============================================

    @FXML
    private void handlePaiementEspeces() {
        modePaiement = "ESPÈCES";
        updateBoutonsPaiement();
    }

    @FXML
    private void handlePaiementCarte() {
        modePaiement = "CARTE BANCAIRE";
        updateBoutonsPaiement();
    }

    @FXML
    private void handlePaiementAutre() {
        modePaiement = "AUTRE";
        updateBoutonsPaiement();
    }

    private void updateBoutonsPaiement() {
        // Réinitialiser les styles
        resetButtonStyle(especesButton, "btn-primary");
        resetButtonStyle(carteButton, "btn-secondary");
        resetButtonStyle(autreButton, "btn-accent");

        // Mettre en évidence le sélectionné
        String selectedStyle = "-fx-border-color: white; -fx-border-width: 3; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.5), 10, 0, 0, 0);";
        
        if ("ESPÈCES".equals(modePaiement)) {
            especesButton.setStyle(especesButton.getStyle() + selectedStyle);
        } else if ("CARTE BANCAIRE".equals(modePaiement)) {
            carteButton.setStyle(carteButton.getStyle() + selectedStyle);
        } else {
            autreButton.setStyle(autreButton.getStyle() + selectedStyle);
        }
    }
    
    private void resetButtonStyle(Button btn, String styleClass) {
        btn.getStyleClass().clear();
        btn.getStyleClass().add("btn");
        btn.getStyleClass().add(styleClass);
        btn.setStyle(""); // Clear inline styles
    }

    // ============================================
    // LOGIQUE MÉTIER
    // ============================================

    private void ajouterAuPanier(Produit produit, int quantite) {
        // Vérifier stock
        if (produit.getQuantiteStock() < quantite) {
            afficherAlerte(Alert.AlertType.WARNING, "Stock insuffisant", "Stock disponible: " + produit.getQuantiteStock());
            return;
        }

        // Vérifier si déjà dans le panier
        Optional<DetailVente> existing = CategorieProduitsController.getPanierGlobal().stream()
                .filter(d -> d.getProduitId() == produit.getId())
                .findFirst();

        if (existing.isPresent()) {
            DetailVente detail = existing.get();
            if (detail.getQuantite() + quantite > produit.getQuantiteStock()) {
                afficherAlerte(Alert.AlertType.WARNING, "Stock insuffisant", "Stock disponible: " + produit.getQuantiteStock());
                return;
            }
            detail.setQuantite(detail.getQuantite() + quantite);
            // Trigger update via listener
            int index = CategorieProduitsController.getPanierGlobal().indexOf(detail);
            CategorieProduitsController.getPanierGlobal().set(index, detail);
        } else {
            DetailVente detail = new DetailVente();
            detail.setProduitId(produit.getId());
            detail.setProduit(produit);
            detail.setQuantite(quantite);
            detail.setPrixVenteUnitaire(produit.getPrixVenteDefaut());
            detail.setPrixAchatUnitaire(produit.getPrixAchatActuel());
            CategorieProduitsController.getPanierGlobal().add(detail);
        }
    }

    private void retirerDuPanier(DetailVente detail) {
        CategorieProduitsController.getPanierGlobal().remove(detail);
    }

    private void modifierQuantiteItem(DetailVente detail) {
        TextInputDialog dialog = new TextInputDialog(String.valueOf(detail.getQuantite()));
        dialog.setTitle("Modifier Quantité");
        dialog.setHeaderText("Nouvelle quantité pour " + detail.getProduit().getNom() + ":");
        dialog.setContentText("Quantité:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(qtyStr -> {
            try {
                int qty = Integer.parseInt(qtyStr);
                if (qty > 0) {
                    if (qty <= detail.getProduit().getQuantiteStock()) {
                        detail.setQuantite(qty);
                        // Trigger update
                        int index = CategorieProduitsController.getPanierGlobal().indexOf(detail);
                        CategorieProduitsController.getPanierGlobal().set(index, detail);
                    } else {
                        afficherAlerte(Alert.AlertType.WARNING, "Stock insuffisant", "Stock disponible: " + detail.getProduit().getQuantiteStock());
                    }
                } else {
                    retirerDuPanier(detail);
                }
            } catch (NumberFormatException e) {
                afficherAlerte(Alert.AlertType.ERROR, "Erreur", "Veuillez entrer un nombre valide.");
            }
        });
    }

    private BigDecimal calculerTotal() {
        return CategorieProduitsController.getPanierGlobal().stream()
                .map(DetailVente::getSousTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void updateTotal() {
        BigDecimal total = calculerTotal();
        totalLabel.setText(String.format("€ %.2f", total));
        
        BigDecimal tva = total.multiply(new BigDecimal("0.20")); // Exemple TVA 20%
        tvaLabel.setText(String.format("Dont TVA (20%%): € %.2f", tva));
    }

    private void afficherAlerte(Alert.AlertType type, String titre, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
