package controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import dao.ProduitDAO;
import dao.VenteDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import model.DetailVente;
import model.Produit;
import model.Vente;

/**
 * Contrôleur pour la gestion des ventes de tabac (Admin uniquement)
 * Avec statistiques détaillées et meilleurs produits
 */
public class GestionTabacController {
    
    @FXML
    private TableView<Vente> ventesTabacTable;
    
    @FXML
    private TableColumn<Vente, Integer> idColumn;
    
    @FXML
    private TableColumn<Vente, String> dateColumn;
    
    @FXML
    private TableColumn<Vente, BigDecimal> totalColumn;
    
    @FXML
    private TableColumn<Vente, Integer> utilisateurColumn;
    
    @FXML
    private TableView<DetailVente> detailsTable;
    
    @FXML
    private TableColumn<DetailVente, String> produitColumn;
    
    @FXML
    private TableColumn<DetailVente, Integer> quantiteColumn;
    
    @FXML
    private TableColumn<DetailVente, BigDecimal> prixColumn;
    
    @FXML
    private TableColumn<DetailVente, BigDecimal> sousTotalColumn;
    
    @FXML
    private TableView<Map<String, Object>> topProduitsTable;
    
    @FXML
    private TableColumn<Map<String, Object>, String> produitTopColumn;
    
    @FXML
    private TableColumn<Map<String, Object>, Integer> quantiteTopColumn;
    
    @FXML
    private TableColumn<Map<String, Object>, BigDecimal> caTopColumn;
    
    @FXML
    private Label totalTabacLabel;
    
    @FXML
    private Label nombreVentesLabel;
    
    @FXML
    private Label recetteAujourdhuiLabel;
    
    @FXML
    private Label recetteSemaineLabel;
    
    @FXML
    private Label recetteMoisLabel;
    
    private VenteDAO venteDAO;
    private ProduitDAO produitDAO;
    private ObservableList<Vente> ventesTabac;
    private ObservableList<Map<String, Object>> topProduits;
    
    @FXML
    private void initialize() {
        venteDAO = new VenteDAO();
        produitDAO = new ProduitDAO();
        ventesTabac = FXCollections.observableArrayList();
        topProduits = FXCollections.observableArrayList();
        
        // Configuration de la table des ventes
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        dateColumn.setCellValueFactory(cellData -> {
            Vente vente = cellData.getValue();
            if (vente.getDateVente() != null) {
                return javafx.beans.binding.Bindings.createStringBinding(() ->
                    vente.getDateVente().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                );
            }
            return javafx.beans.binding.Bindings.createStringBinding(() -> "");
        });
        totalColumn.setCellValueFactory(new PropertyValueFactory<>("totalVente"));
        utilisateurColumn.setCellValueFactory(new PropertyValueFactory<>("utilisateurId"));
        
        // Configuration de la table des détails
        produitColumn.setCellValueFactory(cellData -> {
            DetailVente detail = cellData.getValue();
            Produit produit = detail.getProduit();
            if (produit == null) {
                produit = produitDAO.findById(detail.getProduitId());
                detail.setProduit(produit);
            }
            final Produit finalProduit = produit;
            return javafx.beans.binding.Bindings.createStringBinding(() ->
                finalProduit != null ? finalProduit.getNom() : "Produit ID: " + detail.getProduitId()
            );
        });
        quantiteColumn.setCellValueFactory(new PropertyValueFactory<>("quantite"));
        prixColumn.setCellValueFactory(new PropertyValueFactory<>("prixVenteUnitaire"));
        sousTotalColumn.setCellValueFactory(new PropertyValueFactory<>("sousTotal"));
        
        // Configuration de la table des meilleurs produits
        produitTopColumn.setCellValueFactory(cellData -> {
            Map<String, Object> produit = cellData.getValue();
            String nom = (String) produit.get("nom");
            return javafx.beans.binding.Bindings.createStringBinding(() -> nom != null ? nom : "");
        });
        quantiteTopColumn.setCellValueFactory(cellData -> {
            Map<String, Object> produit = cellData.getValue();
            Integer quantite = (Integer) produit.get("quantite");
            return javafx.beans.binding.Bindings.createObjectBinding(() -> quantite != null ? quantite : 0);
        });
        caTopColumn.setCellValueFactory(cellData -> {
            Map<String, Object> produit = cellData.getValue();
            BigDecimal ca = (BigDecimal) produit.get("ca");
            return javafx.beans.binding.Bindings.createObjectBinding(() -> ca != null ? ca : BigDecimal.ZERO);
        });
        
        ventesTabacTable.setItems(ventesTabac);
        topProduitsTable.setItems(topProduits);
        
        // Écouter la sélection d'une vente pour afficher ses détails
        ventesTabacTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVente, newVente) -> {
            if (newVente != null) {
                chargerDetailsVente(newVente);
            }
        });
        
        chargerVentesTabac();
        chargerTopProduits();
        calculerStatistiques();
    }
    
    /**
     * Charge toutes les ventes de tabac
     */
    private void chargerVentesTabac() {
        ventesTabac.clear();
        java.util.List<Vente> ventes = venteDAO.findVentesTabac();
        ventesTabac.addAll(ventes);
    }
    
    /**
     * Charge les meilleurs produits de tabac
     */
    private void chargerTopProduits() {
        topProduits.clear();
        java.util.List<Map<String, Object>> produits = venteDAO.getTopProduitsTabac(10);
        topProduits.addAll(produits);
    }
    
    /**
     * Charge les détails d'une vente sélectionnée
     */
    private void chargerDetailsVente(Vente vente) {
        ObservableList<DetailVente> details = FXCollections.observableArrayList();
        java.util.List<DetailVente> detailsList = venteDAO.findDetailsByVente(vente.getId());
        
        // Filtrer pour ne garder que les produits de tabac
        for (DetailVente detail : detailsList) {
            Produit produit = produitDAO.findById(detail.getProduitId());
            if (produit != null && produit.isTabac()) {
                detail.setProduit(produit);
                details.add(detail);
            }
        }
        
        detailsTable.setItems(details);
    }
    
    /**
     * Calcule et affiche les statistiques (jour, semaine, mois, total)
     */
    private void calculerStatistiques() {
        LocalDateTime maintenant = LocalDateTime.now();
        
        // Total aujourd'hui (00:00:00 à 23:59:59)
        LocalDateTime debutJour = maintenant.withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime finJour = maintenant.withHour(23).withMinute(59).withSecond(59).withNano(999999999);
        BigDecimal recetteAujourdhui = venteDAO.getTotalVentesTabac(debutJour, finJour);
        recetteAujourdhuiLabel.setText(String.format("%.2f €", recetteAujourdhui));
        
        // Total cette semaine (lundi à aujourd'hui)
        LocalDateTime debutSemaine = maintenant.minusDays(maintenant.getDayOfWeek().getValue() - 1)
                                                .withHour(0).withMinute(0).withSecond(0).withNano(0);
        BigDecimal recetteSemaine = venteDAO.getTotalVentesTabac(debutSemaine, finJour);
        recetteSemaineLabel.setText(String.format("%.2f €", recetteSemaine));
        
        // Total ce mois (1er du mois à aujourd'hui)
        LocalDateTime debutMois = maintenant.withDayOfMonth(1)
                                            .withHour(0).withMinute(0).withSecond(0).withNano(0);
        BigDecimal recetteMois = venteDAO.getTotalVentesTabac(debutMois, finJour);
        recetteMoisLabel.setText(String.format("%.2f €", recetteMois));
        
        // Total général des ventes de tabac
        BigDecimal totalGeneral = BigDecimal.ZERO;
        for (Vente vente : ventesTabac) {
            java.util.List<DetailVente> details = venteDAO.findDetailsByVente(vente.getId());
            for (DetailVente detail : details) {
                Produit produit = produitDAO.findById(detail.getProduitId());
                if (produit != null && produit.isTabac()) {
                    totalGeneral = totalGeneral.add(detail.getSousTotal());
                }
            }
        }
        totalTabacLabel.setText(String.format("%.2f €", totalGeneral));
        
        // Nombre de ventes
        nombreVentesLabel.setText(String.valueOf(ventesTabac.size()));
    }
    
    @FXML
    private void handleRetour() {
        try {
            javafx.stage.Stage stage = (javafx.stage.Stage) ventesTabacTable.getScene().getWindow();
            util.FXMLUtils.changeScene(stage, "/view/AdminDashboard.fxml", "Dashboard Administrateur");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", 
                     "Erreur lors du retour: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleRafraichir() {
        chargerVentesTabac();
        chargerTopProduits();
        calculerStatistiques();
    }
    
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
