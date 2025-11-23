package controller;

import dao.ProduitDAO;
import dao.VenteDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import model.DetailVente;
import model.Produit;
import model.Utilisateur;
import model.Vente;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Contrôleur pour l'interface de caisse (point de vente)
 */
public class CaisseController {
    
    @FXML
    private TextField codeBarreField;
    
    @FXML
    private TextField quantiteField;
    
    @FXML
    private TableView<DetailVente> panierTable;
    
    @FXML
    private TableColumn<DetailVente, String> produitColumn;
    
    @FXML
    private TableColumn<DetailVente, Integer> quantiteColumn;
    
    @FXML
    private TableColumn<DetailVente, BigDecimal> prixUnitaireColumn;
    
    @FXML
    private TableColumn<DetailVente, BigDecimal> sousTotalColumn;
    
    @FXML
    private Label totalLabel;
    
    @FXML
    private Button ajouterButton;
    
    @FXML
    private Button retirerButton;
    
    @FXML
    private Button validerButton;
    
    @FXML
    private Button annulerButton;
    
    @FXML
    private Button deconnexionButton;
    
    @FXML
    private Button categoriesButton;
    
    @FXML
    private Label produitInfoLabel;
    
    private ProduitDAO produitDAO;
    private VenteDAO venteDAO;
    private ObservableList<DetailVente> panierList;
    private Utilisateur utilisateur;
    
    @FXML
    private void initialize() {
        produitDAO = new ProduitDAO();
        venteDAO = new VenteDAO();
        // Utiliser le panier global si disponible
        if (CategorieProduitsController.getPanierGlobal() != null && 
            !CategorieProduitsController.getPanierGlobal().isEmpty()) {
            panierList = CategorieProduitsController.getPanierGlobal();
        } else {
            panierList = FXCollections.observableArrayList();
        }
        utilisateur = ConnexionController.getUtilisateurConnecte();
        
        // Configuration des colonnes
        produitColumn.setCellValueFactory(cellData -> {
            DetailVente detail = cellData.getValue();
            if (detail.getProduit() != null) {
                return javafx.beans.binding.Bindings.createStringBinding(
                    () -> detail.getProduit().getNom() + " (" + detail.getProduit().getCodeBarre() + ")"
                );
            }
            return javafx.beans.binding.Bindings.createStringBinding(() -> "Produit ID: " + detail.getProduitId());
        });
        
        quantiteColumn.setCellValueFactory(new PropertyValueFactory<>("quantite"));
        
        prixUnitaireColumn.setCellValueFactory(new PropertyValueFactory<>("prixVenteUnitaire"));
        prixUnitaireColumn.setCellFactory(column -> new TableCell<DetailVente, BigDecimal>() {
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
        
        sousTotalColumn.setCellValueFactory(cellData -> {
            DetailVente detail = cellData.getValue();
            BigDecimal sousTotal = detail.getSousTotal();
            return new javafx.beans.property.SimpleObjectProperty<>(sousTotal);
        });
        sousTotalColumn.setCellFactory(column -> new TableCell<DetailVente, BigDecimal>() {
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
        
        panierTable.setItems(panierList);
        
        // Mettre à jour le total quand le panier change
        panierList.addListener((javafx.collections.ListChangeListener.Change<? extends DetailVente> c) -> {
            calculerTotal();
        });
        
        quantiteField.setText("1");
        codeBarreField.requestFocus();
    }
    
    @FXML
    private void handleAjouter() {
        String codeBarre = codeBarreField.getText().trim();
        
        if (codeBarre.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Code-barres vide", 
                     "Veuillez entrer un code-barres.");
            return;
        }
        
        Produit produit = produitDAO.findByCodeBarre(codeBarre);
        
        if (produit == null) {
            showAlert(Alert.AlertType.WARNING, "Produit introuvable", 
                     "Aucun produit trouvé avec ce code-barres.");
            codeBarreField.clear();
            return;
        }
        
        if (produit.getQuantiteStock() <= 0) {
            showAlert(Alert.AlertType.WARNING, "Stock insuffisant", 
                     "Ce produit n'est plus en stock.");
            codeBarreField.clear();
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
        
        if (quantite > produit.getQuantiteStock()) {
            showAlert(Alert.AlertType.WARNING, "Stock insuffisant", 
                     "Stock disponible: " + produit.getQuantiteStock());
            return;
        }
        
        // Vérifier si le produit est déjà dans le panier
        DetailVente detailExistant = panierList.stream()
            .filter(d -> d.getProduitId() == produit.getId())
            .findFirst()
            .orElse(null);
        
        if (detailExistant != null) {
            int nouvelleQuantite = detailExistant.getQuantite() + quantite;
            if (nouvelleQuantite > produit.getQuantiteStock()) {
                showAlert(Alert.AlertType.WARNING, "Stock insuffisant", 
                         "Quantité totale demandée dépasse le stock disponible.");
                return;
            }
            detailExistant.setQuantite(nouvelleQuantite);
        } else {
            DetailVente detail = new DetailVente();
            detail.setProduitId(produit.getId());
            detail.setQuantite(quantite);
            detail.setPrixVenteUnitaire(produit.getPrixVenteDefaut());
            detail.setPrixAchatUnitaire(produit.getPrixAchatActuel());
            detail.setProduit(produit);
            panierList.add(detail);
        }
        
        codeBarreField.clear();
        quantiteField.setText("1");
        codeBarreField.requestFocus();
        calculerTotal();
    }
    
    @FXML
    private void handleRetirer() {
        DetailVente detailSelectionne = panierTable.getSelectionModel().getSelectedItem();
        
        if (detailSelectionne == null) {
            showAlert(Alert.AlertType.WARNING, "Aucune sélection", 
                     "Veuillez sélectionner un article à retirer.");
            return;
        }
        
        panierList.remove(detailSelectionne);
        calculerTotal();
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
                     "La vente a été enregistrée avec succès.\nTotal: " + 
                     String.format("%.2f €", total));
            
            // Vider le panier
            panierList.clear();
            calculerTotal();
            codeBarreField.requestFocus();
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
                codeBarreField.requestFocus();
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
    
    @FXML
    private void handleDeconnexion() {
        ConnexionController.deconnecter();
        try {
            javafx.stage.Stage stage = (javafx.stage.Stage) deconnexionButton.getScene().getWindow();
            util.FXMLUtils.changeScene(stage, "/view/Connexion.fxml", "Connexion");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", 
                     "Erreur lors de la déconnexion: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleCodeBarreScan() {
        // Lorsqu'un code-barres est scanné, ajouter automatiquement
        if (!codeBarreField.getText().trim().isEmpty()) {
            handleAjouter();
        }
    }
    
    @FXML
    private void handleRechercheProduit() {
        String codeBarre = codeBarreField.getText().trim();
        if (!codeBarre.isEmpty()) {
            Produit produit = produitDAO.findByCodeBarre(codeBarre);
            if (produit != null) {
                produitInfoLabel.setText(produit.getNom() + " - " + 
                                        String.format("%.2f €", produit.getPrixVenteDefaut()) + 
                                        " - Stock: " + produit.getQuantiteStock());
            } else {
                produitInfoLabel.setText("Produit introuvable");
            }
        } else {
            produitInfoLabel.setText("");
        }
    }
    
    private BigDecimal calculerTotal() {
        BigDecimal total = panierList.stream()
            .map(DetailVente::getSousTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        totalLabel.setText("Total: " + String.format("%.2f €", total));
        return total;
    }
    
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

