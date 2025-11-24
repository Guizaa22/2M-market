package controller;

import java.math.BigDecimal;

import dao.ProduitDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import model.Produit;

/**
 * Contrôleur pour la gestion de stock (Admin uniquement)
 * Version moderne avec interface améliorée
 */
public class GestionStockController {

    // ========================================
    // LABELS & TITRE
    // ========================================
    @FXML
    private Label formTitleLabel;

    // ========================================
    // CHAMPS DE FORMULAIRE
    // ========================================
    @FXML
    private TextField codeBarreField;

    @FXML
    private TextField nomField;

    @FXML
    private TextField categorieField;

    @FXML
    private TextField prixAchatField;

    @FXML
    private TextField prixVenteField;

    @FXML
    private TextField quantiteStockField;

    @FXML
    private TextField seuilAlerteField;

    @FXML
    private TextField rechercheField;

    // ========================================
    // TABLEVIEW & COLONNES
    // ========================================
    @FXML
    private TableView<Produit> produitsTable;

    @FXML
    private TableColumn<Produit, String> codeBarreColumn;

    @FXML
    private TableColumn<Produit, String> nomColumn;

    @FXML
    private TableColumn<Produit, String> categorieColumn;

    @FXML
    private TableColumn<Produit, BigDecimal> prixAchatColumn;

    @FXML
    private TableColumn<Produit, BigDecimal> prixVenteColumn;

    @FXML
    private TableColumn<Produit, Integer> quantiteStockColumn;

    @FXML
    private TableColumn<Produit, Integer> seuilAlerteColumn;

    @FXML
    private TableColumn<Produit, Void> actionsColumn;

    // ========================================
    // BOUTONS
    // ========================================
    @FXML
    private Button ajouterButton;

    @FXML
    private Button modifierButton;

    @FXML
    private Button annulerButton;

    @FXML
    private HBox editButtonsBox;

    @FXML
    private Button retourButton;

    // ========================================
    // DONNÉES & DAO
    // ========================================
    private ProduitDAO produitDAO;
    private ObservableList<Produit> produitsList;
    private Produit produitSelectionne;
    private boolean modeEdition = false;

    /**
     * Initialisation du contrôleur
     */
    @FXML
    private void initialize() {
        produitDAO = new ProduitDAO();
        produitsList = FXCollections.observableArrayList();

        // Configuration des colonnes
        configureTableColumns();

        // Configuration de la colonne Actions avec boutons Modifier/Supprimer
        configureActionsColumn();

        produitsTable.setItems(produitsList);

        // Listener de sélection (optionnel, car on utilise les boutons dans la table)
        produitsTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    produitSelectionne = newSelection;
                }
        );

        // Charger les produits
        chargerProduits();

        // Configuration de la recherche en temps réel
        rechercheField.textProperty().addListener((observable, oldValue, newValue) -> {
            handleRechercher();
        });
    }

    /**
     * Configuration des colonnes du tableau
     */
    private void configureTableColumns() {
        codeBarreColumn.setCellValueFactory(new PropertyValueFactory<>("codeBarre"));
        nomColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        categorieColumn.setCellValueFactory(new PropertyValueFactory<>("categorie"));
        prixAchatColumn.setCellValueFactory(new PropertyValueFactory<>("prixAchatActuel"));
        prixVenteColumn.setCellValueFactory(new PropertyValueFactory<>("prixVenteDefaut"));
        quantiteStockColumn.setCellValueFactory(new PropertyValueFactory<>("quantiteStock"));
        seuilAlerteColumn.setCellValueFactory(new PropertyValueFactory<>("seuilAlerte"));

        // Formatage des colonnes de prix
        prixAchatColumn.setCellFactory(column -> new TableCell<Produit, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f €", item));
                }
            }
        });

        prixVenteColumn.setCellFactory(column -> new TableCell<Produit, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f €", item));
                }
            }
        });

        // Mise en évidence des produits à stock faible avec badges
        quantiteStockColumn.setCellFactory(column -> new TableCell<Produit, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                    setGraphic(null);
                } else {
                    Produit produit = getTableView().getItems().get(getIndex());
                    if (produit != null && produit.isStockFaible()) {
                        // Badge rouge pour stock faible
                        Label badge = new Label("⚠️ " + item);
                        badge.getStyleClass().add("stock-badge");
                        badge.getStyleClass().add("stock-badge-low");
                        setGraphic(badge);
                        setText(null);
                    } else {
                        // Badge vert pour stock normal
                        Label badge = new Label(item.toString());
                        badge.getStyleClass().add("stock-badge");
                        badge.getStyleClass().add("stock-badge-normal");
                        setGraphic(badge);
                        setText(null);
                    }
                    setAlignment(Pos.CENTER);
                }
            }
        });
    }

    /**
     * Configuration de la colonne Actions avec boutons Modifier et Supprimer
     */
    private void configureActionsColumn() {
        actionsColumn.setCellFactory(column -> new TableCell<Produit, Void>() {
            private final Button btnEdit = new Button("✎");
            private final Button btnDelete = new Button("🗑");
            private final HBox actionBox = new HBox(8, btnEdit, btnDelete);

            {
                // Style des boutons
                btnEdit.getStyleClass().add("action-button-edit");
                btnDelete.getStyleClass().add("action-button-delete");

                btnEdit.setTooltip(new Tooltip("Modifier"));
                btnDelete.setTooltip(new Tooltip("Supprimer"));

                actionBox.setAlignment(Pos.CENTER);

                // Actions des boutons
                btnEdit.setOnAction(event -> {
                    Produit produit = getTableView().getItems().get(getIndex());
                    editerProduit(produit);
                });

                btnDelete.setOnAction(event -> {
                    Produit produit = getTableView().getItems().get(getIndex());
                    supprimerProduit(produit);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(actionBox);
                }
            }
        });
    }

    /**
     * Éditer un produit depuis le tableau
     */
    private void editerProduit(Produit produit) {
        produitSelectionne = produit;
        remplirFormulaire(produit);
        activerModeEdition();
    }

    /**
     * Supprimer un produit depuis le tableau
     */
    private void supprimerProduit(Produit produit) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation de suppression");
        confirmAlert.setHeaderText(null);
        confirmAlert.setContentText("Êtes-vous sûr de vouloir supprimer le produit \"" +
                produit.getNom() + "\" ?");

        if (confirmAlert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            if (produitDAO.delete(produit.getId())) {
                showAlert(Alert.AlertType.INFORMATION, "Succès",
                        "Produit supprimé avec succès.");
                chargerProduits();
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur",
                        "Erreur lors de la suppression du produit.");
            }
        }
    }

    /**
     * Activer le mode édition
     */
    private void activerModeEdition() {
        modeEdition = true;
        formTitleLabel.setText("Modifier Produit");
        ajouterButton.setVisible(false);
        ajouterButton.setManaged(false);
        editButtonsBox.setVisible(true);
        editButtonsBox.setManaged(true);
    }

    /**
     * Désactiver le mode édition
     */
    private void desactiverModeEdition() {
        modeEdition = false;
        formTitleLabel.setText("Nouveau Produit");
        ajouterButton.setVisible(true);
        ajouterButton.setManaged(true);
        editButtonsBox.setVisible(false);
        editButtonsBox.setManaged(false);
        produitSelectionne = null;
    }

    /**
     * Handler pour ajouter un produit
     */
    @FXML
    private void handleAjouter() {
        if (validerFormulaire()) {
            Produit produit = creerProduitDepuisFormulaire();

            if (produitDAO.codeBarreExists(produit.getCodeBarre())) {
                showAlert(Alert.AlertType.WARNING, "Code-barres existant",
                        "Un produit avec ce code-barres existe déjà.");
                return;
            }

            if (produitDAO.create(produit)) {
                showAlert(Alert.AlertType.INFORMATION, "Succès",
                        "Produit ajouté avec succès.");
                viderFormulaire();
                chargerProduits();
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur",
                        "Erreur lors de l'ajout du produit.");
            }
        }
    }

    /**
     * Handler pour modifier un produit
     */
    @FXML
    private void handleModifier() {
        if (produitSelectionne == null) {
            showAlert(Alert.AlertType.WARNING, "Aucune sélection",
                    "Veuillez sélectionner un produit à modifier.");
            return;
        }

        if (validerFormulaire()) {
            Produit produit = creerProduitDepuisFormulaire();
            produit.setId(produitSelectionne.getId());

            // Vérifier si le code-barres a changé et s'il existe déjà
            if (!produit.getCodeBarre().equals(produitSelectionne.getCodeBarre()) &&
                    produitDAO.codeBarreExists(produit.getCodeBarre())) {
                showAlert(Alert.AlertType.WARNING, "Code-barres existant",
                        "Un produit avec ce code-barres existe déjà.");
                return;
            }

            if (produitDAO.update(produit)) {
                showAlert(Alert.AlertType.INFORMATION, "Succès",
                        "Produit modifié avec succès.");
                viderFormulaire();
                desactiverModeEdition();
                chargerProduits();
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur",
                        "Erreur lors de la modification du produit.");
            }
        }
    }

    /**
     * Handler pour annuler l'édition
     */
    @FXML
    public void handleAnnuler(ActionEvent actionEvent) {
        viderFormulaire();
        desactiverModeEdition();
    }

    /**
     * Handler pour la recherche (appelé automatiquement)
     */
    @FXML
    private void handleRechercher() {
        String recherche = rechercheField.getText().trim().toLowerCase();

        if (recherche.isEmpty()) {
            chargerProduits();
            return;
        }

        produitsList.clear();
        produitDAO.findAll().stream()
                .filter(p -> p.getNom().toLowerCase().contains(recherche) ||
                        p.getCodeBarre().contains(recherche) ||
                        p.getCategorie().toLowerCase().contains(recherche))
                .forEach(produitsList::add);
    }

    /**
     * Handler pour le retour au dashboard
     */
    @FXML
    private void handleRetour() {
        try {
            javafx.stage.Stage stage = (javafx.stage.Stage) retourButton.getScene().getWindow();
            util.FXMLUtils.changeScene(stage, "/view/AdminDashboard.fxml", "Dashboard Administrateur");
        } catch (java.lang.Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Erreur lors du retour: " + e.getMessage());
        }
    }

    /**
     * Handler pour le scan de code-barres
     */
    @FXML
    private void handleCodeBarreScan() {
        String codeBarre = codeBarreField.getText().trim();
        if (!codeBarre.isEmpty()) {
            Produit produit = produitDAO.findByCodeBarre(codeBarre);
            if (produit != null) {
                remplirFormulaire(produit);
                produitSelectionne = produit;
                activerModeEdition();
            }
        }
    }

    /**
     * Charger tous les produits depuis la base de données
     */
    private void chargerProduits() {
        produitsList.clear();
        produitsList.addAll(produitDAO.findAll());
    }

    /**
     * Remplir le formulaire avec les données d'un produit
     */
    private void remplirFormulaire(Produit produit) {
        codeBarreField.setText(produit.getCodeBarre());
        nomField.setText(produit.getNom());
        categorieField.setText(produit.getCategorie());
        prixAchatField.setText(produit.getPrixAchatActuel().toString());
        prixVenteField.setText(produit.getPrixVenteDefaut().toString());
        quantiteStockField.setText(String.valueOf(produit.getQuantiteStock()));
        seuilAlerteField.setText(String.valueOf(produit.getSeuilAlerte()));
    }

    /**
     * Vider tous les champs du formulaire
     */
    private void viderFormulaire() {
        codeBarreField.clear();
        nomField.clear();
        categorieField.clear();
        prixAchatField.clear();
        prixVenteField.clear();
        quantiteStockField.clear();
        seuilAlerteField.clear();
        produitsTable.getSelectionModel().clearSelection();
    }

    /**
     * Créer un objet Produit depuis les données du formulaire
     */
    private Produit creerProduitDepuisFormulaire() {
        String codeBarre = codeBarreField.getText().trim();
        String nom = nomField.getText().trim();
        String categorieValeur = categorieField.getText().trim();
        BigDecimal prixAchat = new BigDecimal(prixAchatField.getText().trim());
        BigDecimal prixVente = new BigDecimal(prixVenteField.getText().trim());
        int quantiteStock = Integer.parseInt(quantiteStockField.getText().trim());
        int seuilAlerte = Integer.parseInt(seuilAlerteField.getText().trim());

        return new Produit(codeBarre, nom, categorieValeur, prixAchat, prixVente, quantiteStock, seuilAlerte);
    }

    /**
     * Valider les données du formulaire
     */
    private boolean validerFormulaire() {
        if (codeBarreField.getText().trim().isEmpty() ||
                nomField.getText().trim().isEmpty() ||
                prixAchatField.getText().trim().isEmpty() ||
                prixVenteField.getText().trim().isEmpty() ||
                quantiteStockField.getText().trim().isEmpty() ||
                seuilAlerteField.getText().trim().isEmpty()) {

            showAlert(Alert.AlertType.WARNING, "Champs vides",
                    "Veuillez remplir tous les champs obligatoires.");
            return false;
        }

        try {
            BigDecimal prixAchat = new BigDecimal(prixAchatField.getText().trim());
            BigDecimal prixVente = new BigDecimal(prixVenteField.getText().trim());
            int quantiteStock = Integer.parseInt(quantiteStockField.getText().trim());
            int seuilAlerte = Integer.parseInt(seuilAlerteField.getText().trim());

            // Validation des valeurs
            if (prixAchat.compareTo(BigDecimal.ZERO) < 0 || prixVente.compareTo(BigDecimal.ZERO) < 0 ||
                    quantiteStock < 0 || seuilAlerte < 0) {
                showAlert(Alert.AlertType.WARNING, "Valeurs invalides",
                        "Les valeurs ne peuvent pas être négatives.");
                return false;
            }

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Format invalide",
                    "Veuillez entrer des valeurs numériques valides.");
            return false;
        }

        return true;
    }

    /**
     * Afficher une alerte
     */
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}