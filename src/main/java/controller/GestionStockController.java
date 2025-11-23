package controller;

import java.math.BigDecimal;

import dao.ProduitDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Produit;

/**
 * Contrôleur pour la gestion de stock (Admin uniquement)
 */
public class GestionStockController {
    
    @FXML
    private TextField codeBarreField;
    
    @FXML
    private TextField nomField;
    
    @FXML
    private TextField prixAchatField;
    
    @FXML
    private TextField prixVenteField;
    
    @FXML
    private TextField quantiteStockField;
    
    @FXML
    private TextField seuilAlerteField;
    
    @FXML
    private TableView<Produit> produitsTable;
    
    @FXML
    private TableColumn<Produit, String> codeBarreColumn;
    
    @FXML
    private TableColumn<Produit, String> nomColumn;
    
    @FXML
    private TableColumn<Produit, BigDecimal> prixAchatColumn;
    
    @FXML
    private TableColumn<Produit, BigDecimal> prixVenteColumn;
    
    @FXML
    private TableColumn<Produit, Integer> quantiteStockColumn;
    
    @FXML
    private TableColumn<Produit, Integer> seuilAlerteColumn;
    
    @FXML
    private Button ajouterButton;
    
    @FXML
    private Button modifierButton;
    
    @FXML
    private Button supprimerButton;
    
    @FXML
    private Button retourButton;
    
    @FXML
    private Button rechercherButton;
    
    @FXML
    private TextField rechercheField;
    
    private ProduitDAO produitDAO;
    private ObservableList<Produit> produitsList;
    private Produit produitSelectionne;
    
    @FXML
    private void initialize() {
        produitDAO = new ProduitDAO();
        produitsList = FXCollections.observableArrayList();
        
        // Configuration des colonnes
        codeBarreColumn.setCellValueFactory(new PropertyValueFactory<>("codeBarre"));
        nomColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
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
        
        // Mise en évidence des produits à stock faible
        quantiteStockColumn.setCellFactory(column -> new TableCell<Produit, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item.toString());
                    Produit produit = getTableView().getItems().get(getIndex());
                    if (produit != null && produit.isStockFaible()) {
                        setStyle("-fx-background-color: #ffcccc;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });
        
        produitsTable.setItems(produitsList);
        produitsTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                produitSelectionne = newSelection;
                if (newSelection != null) {
                    remplirFormulaire(newSelection);
                }
            }
        );
        
        chargerProduits();
    }
    
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
                chargerProduits();
                produitSelectionne = null;
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", 
                         "Erreur lors de la modification du produit.");
            }
        }
    }
    
    @FXML
    private void handleSupprimer() {
        if (produitSelectionne == null) {
            showAlert(Alert.AlertType.WARNING, "Aucune sélection", 
                     "Veuillez sélectionner un produit à supprimer.");
            return;
        }
        
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation de suppression");
        confirmAlert.setHeaderText(null);
        confirmAlert.setContentText("Êtes-vous sûr de vouloir supprimer le produit \"" + 
                                   produitSelectionne.getNom() + "\" ?");
        
        if (confirmAlert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            if (produitDAO.delete(produitSelectionne.getId())) {
                showAlert(Alert.AlertType.INFORMATION, "Succès", 
                         "Produit supprimé avec succès.");
                viderFormulaire();
                chargerProduits();
                produitSelectionne = null;
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", 
                         "Erreur lors de la suppression du produit.");
            }
        }
    }
    
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
                        p.getCodeBarre().contains(recherche))
            .forEach(produitsList::add);
    }
    
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
    
    @FXML
    private void handleCodeBarreScan() {
        // Lorsqu'un code-barres est scanné, rechercher le produit
        String codeBarre = codeBarreField.getText().trim();
        if (!codeBarre.isEmpty()) {
            Produit produit = produitDAO.findByCodeBarre(codeBarre);
            if (produit != null) {
                remplirFormulaire(produit);
                produitSelectionne = produit;
                produitsTable.getSelectionModel().select(produit);
            }
        }
    }
    
    private void chargerProduits() {
        produitsList.clear();
        produitsList.addAll(produitDAO.findAll());
    }
    
    private void remplirFormulaire(Produit produit) {
        codeBarreField.setText(produit.getCodeBarre());
        nomField.setText(produit.getNom());
        prixAchatField.setText(produit.getPrixAchatActuel().toString());
        prixVenteField.setText(produit.getPrixVenteDefaut().toString());
        quantiteStockField.setText(String.valueOf(produit.getQuantiteStock()));
        seuilAlerteField.setText(String.valueOf(produit.getSeuilAlerte()));
        // Note: Le champ categorie n'est pas encore dans le formulaire FXML
    }
    
    private void viderFormulaire() {
        codeBarreField.clear();
        nomField.clear();
        prixAchatField.clear();
        prixVenteField.clear();
        quantiteStockField.clear();
        seuilAlerteField.clear();
        produitsTable.getSelectionModel().clearSelection();
    }
    
    private Produit creerProduitDepuisFormulaire() {
        String codeBarre = codeBarreField.getText().trim();
        String nom = nomField.getText().trim();
        String categorie = ""; // Par défaut, pas de catégorie
        BigDecimal prixAchat = new BigDecimal(prixAchatField.getText().trim());
        BigDecimal prixVente = new BigDecimal(prixVenteField.getText().trim());
        int quantiteStock = Integer.parseInt(quantiteStockField.getText().trim());
        int seuilAlerte = Integer.parseInt(seuilAlerteField.getText().trim());
        
        return new Produit(codeBarre, nom, categorie, prixAchat, prixVente, quantiteStock, seuilAlerte);
    }
    
    private boolean validerFormulaire() {
        if (codeBarreField.getText().trim().isEmpty() ||
            nomField.getText().trim().isEmpty() ||
            prixAchatField.getText().trim().isEmpty() ||
            prixVenteField.getText().trim().isEmpty() ||
            quantiteStockField.getText().trim().isEmpty() ||
            seuilAlerteField.getText().trim().isEmpty()) {
            
            showAlert(Alert.AlertType.WARNING, "Champs vides", 
                     "Veuillez remplir tous les champs.");
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
    
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

