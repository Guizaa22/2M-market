package controller;

import dao.VenteDAO;
import dao.DetailVenteDAO;
import dao.ProduitDAO;
import dao.UtilisateurDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import model.Vente;
import model.DetailVente;
import model.Produit;
import util.FXMLUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Contrôleur pour la gestion des ventes
 */
public class GestionVentesController {

    // ========================================
    // LABELS & DATE
    // ========================================
    @FXML
    private Button retourButton;

    @FXML
    private Label dateLabel;

    @FXML
    private Label caJourLabel;

    @FXML
    private Label caSemaineLabel;

    @FXML
    private Label caMoisLabel;

    @FXML
    private Label nbVentesLabel;

    @FXML
    private Label panierMoyenLabel;

    @FXML
    private Label beneficeLabel;

    // ========================================
    // BOUTONS DE NAVIGATION
    // ========================================
    @FXML
    private Button btnDashboard;

    @FXML
    private Button btnVentes;

    @FXML
    private Button btnProduits;

    @FXML
    private Button btnRapports;

    // ========================================
    // VUES (CONTAINERS)
    // ========================================
    @FXML
    private javafx.scene.layout.VBox dashboardView;

    @FXML
    private javafx.scene.layout.VBox ventesView;

    @FXML
    private javafx.scene.layout.VBox produitsView;

    @FXML
    private javafx.scene.layout.VBox rapportsView;

    // ========================================
    // GRAPHIQUES
    // ========================================
    @FXML
    private LineChart<String, Number> ventesLineChart;

    @FXML
    private PieChart categoriesPieChart;

    @FXML
    private BarChart<String, Number> caBarChart;

    // ========================================
    // TABLEVIEWS
    // ========================================
    @FXML
    private TableView<VenteDisplay> ventesTable;

    @FXML
    private TableColumn<VenteDisplay, String> colId;

    @FXML
    private TableColumn<VenteDisplay, String> colDate;

    @FXML
    private TableColumn<VenteDisplay, String> colMontant;

    @FXML
    private TableColumn<VenteDisplay, Integer> colArticles;

    @FXML
    private TableColumn<VenteDisplay, String> colCaissier;

    @FXML
    private TableColumn<VenteDisplay, Void> colAction;

    @FXML
    private TableView<ProduitStats> produitsTable;

    @FXML
    private TableColumn<ProduitStats, Integer> colRang;

    @FXML
    private TableColumn<ProduitStats, String> colProduit;

    @FXML
    private TableColumn<ProduitStats, Integer> colQuantite;

    @FXML
    private TableColumn<ProduitStats, String> colCA;

    // ========================================
    // DONNÉES & DAO
    // ========================================
    private VenteDAO venteDAO;
    private DetailVenteDAO detailVenteDAO;
    private ProduitDAO produitDAO;
    private UtilisateurDAO utilisateurDAO;

    private ObservableList<VenteDisplay> ventesList;
    private ObservableList<ProduitStats> produitStatsList;

    /**
     * Initialisation du contrôleur
     */
    @FXML
    private void initialize() {
        // Initialisation des DAOs
        venteDAO = new VenteDAO();
        detailVenteDAO = new DetailVenteDAO();
        produitDAO = new ProduitDAO();
        utilisateurDAO = new UtilisateurDAO();

        // Initialisation des listes
        ventesList = FXCollections.observableArrayList();
        produitStatsList = FXCollections.observableArrayList();

        // Configuration de la date
        dateLabel.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.FRENCH)));

        // Configuration des tables
        configureVentesTable();
        configureProduitsTable();

        // Charger les données
        chargerDonnees();

        // Afficher le dashboard par défaut
        showDashboard();
    }

    /**
     * Configuration de la table des ventes
     */
    private void configureVentesTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateHeure"));
        colMontant.setCellValueFactory(new PropertyValueFactory<>("montant"));
        colArticles.setCellValueFactory(new PropertyValueFactory<>("nbArticles"));
        colCaissier.setCellValueFactory(new PropertyValueFactory<>("caissier"));

        // Configuration de la colonne Action
        colAction.setCellFactory(column -> new TableCell<VenteDisplay, Void>() {
            private final Button btnDetails = new Button("👁️ Détails");

            {
                btnDetails.getStyleClass().add("btn-details");
                btnDetails.setOnAction(event -> {
                    VenteDisplay vente = getTableView().getItems().get(getIndex());
                    afficherDetailsVente(vente);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btnDetails);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        ventesTable.setItems(ventesList);
    }

    /**
     * Configuration de la table des produits
     */
    private void configureProduitsTable() {
        colRang.setCellValueFactory(new PropertyValueFactory<>("rang"));
        colProduit.setCellValueFactory(new PropertyValueFactory<>("nomProduit"));
        colQuantite.setCellValueFactory(new PropertyValueFactory<>("quantiteVendue"));
        colCA.setCellValueFactory(new PropertyValueFactory<>("caGenere"));

        // Afficher les médailles pour le top 3
        colRang.setCellFactory(column -> new TableCell<ProduitStats, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    String emoji = item == 1 ? "🥇" : item == 2 ? "🥈" : item == 3 ? "🥉" : String.valueOf(item);
                    Label label = new Label(emoji);
                    label.setStyle("-fx-font-size: 24px;");
                    setGraphic(label);
                    setText(null);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        // Mise en forme de la quantité
        colQuantite.setCellFactory(column -> new TableCell<ProduitStats, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item.toString());
                    setStyle("-fx-font-weight: bold; -fx-text-fill: #4CAF50; -fx-font-size: 16px;");
                    setAlignment(Pos.CENTER);
                }
            }
        });

        // Mise en forme du CA
        colCA.setCellFactory(column -> new TableCell<ProduitStats, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setStyle("-fx-font-weight: bold; -fx-text-fill: #2196F3; -fx-font-size: 16px;");
                    setAlignment(Pos.CENTER_RIGHT);
                }
            }
        });

        produitsTable.setItems(produitStatsList);
    }

    /**
     * Charger toutes les données
     */
    private void chargerDonnees() {
        chargerStatistiques();
        chargerGraphiques();
        chargerVentes();
        chargerTopProduits();
    }

    /**
     * Charger les statistiques (KPIs)
     */
    private void chargerStatistiques() {
        LocalDateTime maintenant = LocalDateTime.now();
        LocalDateTime debutJour = maintenant.toLocalDate().atStartOfDay();
        LocalDateTime debutSemaine = maintenant.minusDays(7);
        LocalDateTime debutMois = maintenant.minusDays(30);

        // CA Aujourd'hui
        BigDecimal caJour = venteDAO.getCAParPeriode(debutJour, maintenant);
        caJourLabel.setText(String.format("%.2f €", caJour));

        // CA Semaine
        BigDecimal caSemaine = venteDAO.getCAParPeriode(debutSemaine, maintenant);
        caSemaineLabel.setText(String.format("%.2f €", caSemaine));

        // CA Mois
        BigDecimal caMois = venteDAO.getCAParPeriode(debutMois, maintenant);
        caMoisLabel.setText(String.format("%.2f €", caMois));

        // Nombre de ventes aujourd'hui
        int nbVentes = venteDAO.getNombreVentesParPeriode(debutJour, maintenant);
        nbVentesLabel.setText(String.valueOf(nbVentes));

        // Panier moyen
        BigDecimal panierMoyen = nbVentes > 0 ? caJour.divide(BigDecimal.valueOf(nbVentes), 2, BigDecimal.ROUND_HALF_UP) : BigDecimal.ZERO;
        panierMoyenLabel.setText(String.format("%.2f €", panierMoyen));

        // Bénéfice estimé (20% du CA pour cet exemple)
        BigDecimal benefice = caMois.multiply(BigDecimal.valueOf(0.20));
        beneficeLabel.setText(String.format("%.2f €", benefice));
    }

    /**
     * Charger les données des graphiques
     */
    private void chargerGraphiques() {
        chargerLineChart();
        chargerPieChart();
        chargerBarChart();
    }

    /**
     * Charger le graphique en ligne (Évolution des ventes 7 jours)
     */
    private void chargerLineChart() {
        ventesLineChart.getData().clear();

        XYChart.Series<String, Number> seriesMontant = new XYChart.Series<>();
        seriesMontant.setName("Montant (€)");

        XYChart.Series<String, Number> seriesNbVentes = new XYChart.Series<>();
        seriesNbVentes.setName("Nombre de ventes");

        // Récupérer les données des 7 derniers jours
        LocalDateTime maintenant = LocalDateTime.now();
        String[] jours = {"Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim"};

        for (int i = 6; i >= 0; i--) {
            LocalDateTime debut = maintenant.minusDays(i).toLocalDate().atStartOfDay();
            LocalDateTime fin = debut.plusDays(1);

            BigDecimal ca = venteDAO.getCAParPeriode(debut, fin);
            int nbVentes = venteDAO.getNombreVentesParPeriode(debut, fin);

            int jourIndex = debut.getDayOfWeek().getValue() - 1;
            String jour = jours[jourIndex];

            seriesMontant.getData().add(new XYChart.Data<>(jour, ca.doubleValue()));
            seriesNbVentes.getData().add(new XYChart.Data<>(jour, nbVentes));
        }

        ventesLineChart.getData().addAll(seriesMontant, seriesNbVentes);
    }

    /**
     * Charger le graphique circulaire (Ventes par catégorie)
     */
    private void chargerPieChart() {
        categoriesPieChart.getData().clear();

        Map<String, BigDecimal> ventesParCategorie = detailVenteDAO.getVentesParCategorie();

        for (Map.Entry<String, BigDecimal> entry : ventesParCategorie.entrySet()) {
            PieChart.Data slice = new PieChart.Data(
                    entry.getKey(),
                    entry.getValue().doubleValue()
            );
            categoriesPieChart.getData().add(slice);
        }
    }

    /**
     * Charger le graphique à barres (CA par catégorie)
     */
    private void chargerBarChart() {
        caBarChart.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("CA (€)");

        Map<String, BigDecimal> ventesParCategorie = detailVenteDAO.getVentesParCategorie();

        for (Map.Entry<String, BigDecimal> entry : ventesParCategorie.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue().doubleValue()));
        }

        caBarChart.getData().add(series);
    }

    /**
     * Charger la liste des ventes récentes
     */
    private void chargerVentes() {
        ventesList.clear();

        List<Vente> ventes = venteDAO.findRecent(50); // 50 ventes les plus récentes

        for (Vente vente : ventes) {
            // Récupérer les détails de la vente
            List<DetailVente> details = detailVenteDAO.findByVenteId(vente.getId());
            int nbArticles = details.stream().mapToInt(DetailVente::getQuantite).sum();

            // Récupérer le nom du caissier
            String caissier = utilisateurDAO.findById(vente.getUtilisateurId()).getUsername();

            VenteDisplay display = new VenteDisplay(
                    "#" + vente.getId(),
                    vente.getDateVente().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                    String.format("%.2f €", vente.getTotalVente()),
                    nbArticles,
                    caissier,
                    vente.getId()
            );

            ventesList.add(display);
        }
    }

    /**
     * Charger le top 5 des produits
     */
    private void chargerTopProduits() {
        produitStatsList.clear();

        List<ProduitStats> topProduits = detailVenteDAO.getTopProduits(5);

        int rang = 1;
        for (ProduitStats stats : topProduits) {
            stats.setRang(rang++);
            produitStatsList.add(stats);
        }
    }

    /**
     * Afficher les détails d'une vente
     */
    private void afficherDetailsVente(VenteDisplay venteDisplay) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Détails de la vente");
        alert.setHeaderText("Vente " + venteDisplay.getId());

        // Récupérer les détails de la vente
        List<DetailVente> details = detailVenteDAO.findByVenteId(venteDisplay.getVenteId());

        StringBuilder content = new StringBuilder();
        content.append(String.format("Date: %s\n", venteDisplay.getDateHeure()));
        content.append(String.format("Caissier: %s\n", venteDisplay.getCaissier()));
        content.append(String.format("Montant total: %s\n\n", venteDisplay.getMontant()));
        content.append("Articles:\n");
        content.append("─────────────────────────────────\n");

        for (DetailVente detail : details) {
            Produit produit = produitDAO.findById(detail.getProduitId());
            content.append(String.format("• %s\n", produit.getNom()));
            content.append(String.format("  Quantité: %d × %.2f € = %.2f €\n",
                    detail.getQuantite(),
                    detail.getPrixVenteUnitaire(),
                    detail.getSousTotal()));
        }

        alert.setContentText(content.toString());
        alert.showAndWait();
    }

    /**
     * Afficher le dashboard
     */
    @FXML
    private void showDashboard() {
        activerVue(dashboardView);
        activerBouton(btnDashboard);
        chargerStatistiques();
        chargerGraphiques();
    }

    /**
     * Afficher l'historique des ventes
     */
    @FXML
    private void showVentes() {
        activerVue(ventesView);
        activerBouton(btnVentes);
        chargerVentes();
    }

    /**
     * Afficher le top produits
     */
    @FXML
    private void showProduits() {
        activerVue(produitsView);
        activerBouton(btnProduits);
        chargerTopProduits();
    }

    /**
     * Afficher la page des rapports
     */
    @FXML
    private void showRapports() {
        activerVue(rapportsView);
        activerBouton(btnRapports);
    }

    /**
     * Activer une vue spécifique et masquer les autres
     */
    private void activerVue(javafx.scene.layout.VBox vueAActiver) {
        dashboardView.setVisible(false);
        ventesView.setVisible(false);
        produitsView.setVisible(false);
        rapportsView.setVisible(false);

        vueAActiver.setVisible(true);
    }

    /**
     * Activer un bouton de navigation et désactiver les autres
     */
    private void activerBouton(Button boutonActif) {
        btnDashboard.getStyleClass().remove("active");
        btnVentes.getStyleClass().remove("active");
        btnProduits.getStyleClass().remove("active");
        btnRapports.getStyleClass().remove("active");

        boutonActif.getStyleClass().add("active");
    }

    /**
     * Retour au dashboard principal
     */
    @FXML
    private void handleRetour() {
        try {
            javafx.stage.Stage stage = (javafx.stage.Stage) btnDashboard.getScene().getWindow();
            FXMLUtils.changeScene(stage, "/view/AdminDashboard.fxml", "Dashboard Administrateur");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du retour: " + e.getMessage());
        }
    }

    /**
     * Générer un rapport (placeholder)
     */
    @FXML
    private void genererRapport(String typeRapport) {
        showAlert(Alert.AlertType.INFORMATION, "Rapport",
                "Génération du rapport " + typeRapport + " en cours...");
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

    public void retourDashboard() {
        try {
            javafx.stage.Stage stage = (javafx.stage.Stage) retourButton.getScene().getWindow();
            util.FXMLUtils.changeScene(stage, "/view/AdminDashboard.fxml", "Dashboard Administrateur");
        } catch (java.lang.Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Erreur lors du retour: " + e.getMessage());
        }
    }

    // ========================================
    // CLASSES INTERNES POUR L'AFFICHAGE
    // ========================================

    /**
     * Classe pour l'affichage des ventes dans le tableau
     */
    public static class VenteDisplay {
        private String id;
        private String dateHeure;
        private String montant;
        private int nbArticles;
        private String caissier;
        private int venteId;

        public VenteDisplay(String id, String dateHeure, String montant, int nbArticles, String caissier, int venteId) {
            this.id = id;
            this.dateHeure = dateHeure;
            this.montant = montant;
            this.nbArticles = nbArticles;
            this.caissier = caissier;
            this.venteId = venteId;
        }

        public String getId() { return id; }
        public String getDateHeure() { return dateHeure; }
        public String getMontant() { return montant; }
        public int getNbArticles() { return nbArticles; }
        public String getCaissier() { return caissier; }
        public int getVenteId() { return venteId; }
    }

    /**
     * Classe pour les statistiques des produits
     */
    public static class ProduitStats {
        private int rang;
        private String nomProduit;
        private int quantiteVendue;
        private String caGenere;

        public ProduitStats(String nomProduit, int quantiteVendue, BigDecimal ca) {
            this.nomProduit = nomProduit;
            this.quantiteVendue = quantiteVendue;
            this.caGenere = String.format("%.2f €", ca);
        }

        public int getRang() { return rang; }
        public void setRang(int rang) { this.rang = rang; }
        public String getNomProduit() { return nomProduit; }
        public int getQuantiteVendue() { return quantiteVendue; }
        public String getCaGenere() { return caGenere; }
    }
}