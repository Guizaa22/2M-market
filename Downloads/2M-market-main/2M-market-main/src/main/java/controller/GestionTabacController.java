package controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
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
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.DetailVente;
import model.Produit;
import model.Vente;

/**
 * Contrôleur pour la gestion des ventes de tabac (Admin uniquement)
 * Avec statistiques détaillées et meilleurs produits
 */
public class GestionTabacController {
    
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
    
    @FXML
    private FlowPane produitsTabacContainer;
    
    @FXML
    private Label produitsTabacCountLabel;
    
    private VenteDAO venteDAO;
    private ProduitDAO produitDAO;
    private List<Vente> ventesTabac;
    private ObservableList<Map<String, Object>> topProduits;
    
    @FXML
    private void initialize() {
        venteDAO = new VenteDAO();
        produitDAO = new ProduitDAO();
        ventesTabac = new java.util.ArrayList<>();
        topProduits = FXCollections.observableArrayList();
        
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
        
        topProduitsTable.setItems(topProduits);
        
        chargerVentesTabac();
        chargerTopProduits();
        calculerStatistiques();
        chargerProduitsTabac();
    }
    
    /**
     * Charge toutes les ventes de tabac
     */
    private void chargerVentesTabac() {
        List<Vente> ventes = venteDAO.findVentesTabac();
        if (ventes != null) {
            ventesTabac = ventes;
        } else {
            ventesTabac = new java.util.ArrayList<>();
        }
    }
    
    private void chargerTopProduits() {
        topProduits.clear();
        java.util.List<Map<String, Object>> produits = venteDAO.getTopProduitsTabac(10);
        topProduits.addAll(produits);
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
            Stage stage = (Stage) totalTabacLabel.getScene().getWindow();
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
        chargerProduitsTabac();
    }
    
    /**
     * Charge et affiche les produits de tabac disponibles
     */
    private void chargerProduitsTabac() {
        if (produitsTabacContainer == null) {
            return;
        }
        
        produitsTabacContainer.getChildren().clear();
        List<Produit> produitsTabac = produitDAO.findProduitsTabac();
        
        if (produitsTabac == null || produitsTabac.isEmpty()) {
            produitsTabacContainer.getChildren().add(creerBoiteMessageProduitTabac());
            if (produitsTabacCountLabel != null) {
                produitsTabacCountLabel.setText("Aucun produit tabac disponible");
            }
            return;
        }
        
        for (Produit produit : produitsTabac) {
            if (produit.getQuantiteStock() > 0) {
                produitsTabacContainer.getChildren().add(creerCarteProduitTabac(produit));
            }
        }
        
        if (produitsTabacCountLabel != null) {
            produitsTabacCountLabel.setText(
                String.format("%d produit(s) tabac disponibles", produitsTabac.size())
            );
        }
    }
    
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Crée une carte visuelle pour un produit de tabac
     */
    private VBox creerCarteProduitTabac(Produit produit) {
        VBox card = new VBox(10);
        card.getStyleClass().add("product-card");
        card.setAlignment(Pos.TOP_LEFT);
        card.setPadding(new Insets(16));
        card.setPrefWidth(240);
        
        Label nomLabel = new Label(produit.getNom());
        nomLabel.getStyleClass().add("product-name");
        nomLabel.setWrapText(true);
        
        Label categorieLabel = new Label("📂 " + (produit.getCategorie() != null ? produit.getCategorie() : "Tabac"));
        categorieLabel.setStyle("-fx-text-fill: #7B1FA2; -fx-font-size: 12px; -fx-font-weight: bold;");
        
        Label codeLabel = new Label("📋 " + produit.getCodeBarre());
        codeLabel.setStyle("-fx-text-fill: #555; -fx-font-size: 12px;");
        
        Label prixVenteLabel = new Label(String.format("💰 %.2f DT", produit.getPrixVenteDefaut()));
        prixVenteLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #4CAF50;");
        
        Label prixAchatLabel = new Label(String.format("🏷️ Achat: %.2f DT", produit.getPrixAchatActuel()));
        prixAchatLabel.setStyle("-fx-text-fill: #777;");
        
        HBox stockBox = new HBox(6);
        stockBox.setAlignment(Pos.CENTER_LEFT);
        stockBox.setPadding(new Insets(6, 0, 0, 0));
        
        Label stockBadge = new Label("Stock: " + produit.getQuantiteStock());
        stockBadge.getStyleClass().add("product-stock");
        stockBadge.getStyleClass().add(getStockStyleClass(produit));
        stockBox.getChildren().add(stockBadge);
        
        Label uniteLabel = new Label("📦 " + produit.getUnite());
        uniteLabel.setStyle("-fx-text-fill: #555;");
        
        VBox seuilBox = new VBox(2);
        Label seuilLabel = new Label("⚠️ Seuil: " + produit.getSeuilAlerte());
        seuilLabel.setStyle("-fx-text-fill: #FF9800; -fx-font-weight: bold;");
        seuilBox.getChildren().add(seuilLabel);
        
        card.getChildren().addAll(
            nomLabel,
            categorieLabel,
            codeLabel,
            prixVenteLabel,
            prixAchatLabel,
            uniteLabel,
            stockBox,
            seuilBox
        );
        
        return card;
    }
    
    private String getStockStyleClass(Produit produit) {
        if (produit.getQuantiteStock() == 0) {
            return "stock-critical";
        } else if (produit.getQuantiteStock() <= produit.getSeuilAlerte()) {
            return "stock-low";
        } else if (produit.getQuantiteStock() > 50) {
            return "stock-high";
        }
        return "stock-medium";
    }
    
    private VBox creerBoiteMessageProduitTabac() {
        VBox box = new VBox(10);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(30));
        box.setStyle("-fx-background-color: rgba(255,255,255,0.6); -fx-background-radius: 12;");
        
        Label icon = new Label("🛑");
        icon.setStyle("-fx-font-size: 36px;");
        
        Label title = new Label("Aucun produit tabac disponible");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #7B1FA2;");
        
        Label desc = new Label("Ajoutez des produits avec la catégorie Tabac, Puff, Terrea ou Cigarette pour les voir apparaître ici automatiquement.");
        desc.setWrapText(true);
        desc.setStyle("-fx-text-fill: #555;");
        desc.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        
        box.getChildren().addAll(icon, title, desc);
        return box;
    }
}
