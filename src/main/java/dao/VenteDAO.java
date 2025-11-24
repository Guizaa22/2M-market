package dao;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import model.DetailVente;
import model.Vente;

/**
 * DAO pour la gestion des ventes + statistiques
 */
public class VenteDAO {

    /**
     * Créer une vente avec ses détails + mise à jour stock
     */
    public boolean create(Vente vente) {
        Connection conn = null;

        try {
            conn = DBConnector.getConnection();
            conn.setAutoCommit(false);

            // INSERT VENTE
            String sqlVente = "INSERT INTO ventes (date_vente, total_vente, id_utilisateur) VALUES (?, ?, ?)";
            PreparedStatement stmtVente = conn.prepareStatement(sqlVente, Statement.RETURN_GENERATED_KEYS);

            stmtVente.setTimestamp(1, Timestamp.valueOf(vente.getDateVente()));
            stmtVente.setBigDecimal(2, vente.getTotalVente());
            stmtVente.setInt(3, vente.getUtilisateurId());
            stmtVente.executeUpdate();

            ResultSet rs = stmtVente.getGeneratedKeys();
            if (!rs.next()) {
                conn.rollback();
                return false;
            }
            int venteId = rs.getInt(1);
            vente.setId(venteId);

            // INSERT DETAILS + UPDATE STOCK
            String sqlDetail = "INSERT INTO detailsvente (id_vente, id_produit, quantite, prix_vente_unitaire, prix_achat_unitaire) VALUES (?, ?, ?, ?, ?)";
            String sqlStock = "UPDATE produits SET quantite_stock = quantite_stock - ? WHERE id = ?";

            PreparedStatement stmtDetail = conn.prepareStatement(sqlDetail);
            PreparedStatement stmtStock = conn.prepareStatement(sqlStock);

            for (DetailVente d : vente.getDetails()) {

                stmtDetail.setInt(1, venteId);
                stmtDetail.setInt(2, d.getProduitId());
                stmtDetail.setInt(3, d.getQuantite());
                stmtDetail.setBigDecimal(4, d.getPrixVenteUnitaire());
                stmtDetail.setBigDecimal(5, d.getPrixAchatUnitaire());
                stmtDetail.addBatch();

                stmtStock.setInt(1, d.getQuantite());
                stmtStock.setInt(2, d.getProduitId());
                stmtStock.addBatch();
            }

            stmtDetail.executeBatch();
            stmtStock.executeBatch();

            conn.commit();
            return true;

        } catch (SQLException e) {
            System.err.println("Erreur création vente: " + e.getMessage());
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                System.err.println("Rollback error: " + ex.getMessage());
            }
        } finally {
            try {
                if (conn != null) conn.setAutoCommit(true);
                if (conn != null) conn.close();
            } catch (SQLException ignored) {}
        }
        return false;
    }

    /**
     * Liste toutes les ventes
     */
    public List<Vente> findAll() {
        List<Vente> ventes = new ArrayList<>();
        String sql = "SELECT * FROM ventes ORDER BY date_vente DESC";

        try (Connection conn = DBConnector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                ventes.add(mapResultSetToVente(rs));
            }

        } catch (SQLException e) {
            System.err.println("Erreur findAll: " + e.getMessage());
        }
        return ventes;
    }

    /**
     * 🔥 Nouvelle méthode : dernières ventes limitées
     */
    public List<Vente> findRecent(int limit) {
        List<Vente> ventes = new ArrayList<>();
        String sql = "SELECT * FROM ventes ORDER BY date_vente DESC LIMIT ?";

        try (Connection conn = DBConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, limit);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                ventes.add(mapResultSetToVente(rs));
            }

        } catch (SQLException e) {
            System.err.println("Erreur findRecent: " + e.getMessage());
        }

        return ventes;
    }

    /**
     * Ventes par utilisateur
     */
    public List<Vente> findByUtilisateur(int utilisateurId) {
        List<Vente> ventes = new ArrayList<>();
        String sql = "SELECT * FROM ventes WHERE id_utilisateur = ? ORDER BY date_vente DESC";

        try (Connection conn = DBConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, utilisateurId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                ventes.add(mapResultSetToVente(rs));
            }

        } catch (SQLException e) {
            System.err.println("Erreur findByUtilisateur: " + e.getMessage());
        }

        return ventes;
    }

    /**
     * Charger les détails d'une vente
     */
    public List<DetailVente> findDetailsByVente(int venteId) {
        List<DetailVente> details = new ArrayList<>();
        String sql = "SELECT * FROM detailsvente WHERE id_vente = ?";

        try (Connection conn = DBConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, venteId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                details.add(new DetailVente(
                        rs.getInt("id"),
                        rs.getInt("id_vente"),
                        rs.getInt("id_produit"),
                        rs.getInt("quantite"),
                        rs.getBigDecimal("prix_vente_unitaire"),
                        rs.getBigDecimal("prix_achat_unitaire")
                ));
            }

        } catch (SQLException e) {
            System.err.println("Erreur détails vente: " + e.getMessage());
        }

        return details;
    }

    /**
     * Total des recettes sur une période
     */
    public BigDecimal getTotalRecettes(LocalDateTime debut, LocalDateTime fin) {
        String sql = "SELECT SUM(total_vente) FROM ventes WHERE date_vente BETWEEN ? AND ?";
        try (Connection conn = DBConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setTimestamp(1, Timestamp.valueOf(debut));
            stmt.setTimestamp(2, Timestamp.valueOf(fin));
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getBigDecimal(1) != null ? rs.getBigDecimal(1) : BigDecimal.ZERO;
            }

        } catch (SQLException e) {
            System.err.println("Erreur total recettes: " + e.getMessage());
        }

        return BigDecimal.ZERO;
    }

    /**
     * 🔥 Nouvelle méthode : CA (chiffre d'affaires)
     */
    public BigDecimal getCAParPeriode(LocalDateTime debut, LocalDateTime fin) {
        return getTotalRecettes(debut, fin);
    }

    /**
     * 🔥 Nouvelle méthode : nombre total de ventes sur une période
     */
    public int getNombreVentesParPeriode(LocalDateTime debut, LocalDateTime fin) {
        String sql = "SELECT COUNT(*) FROM ventes WHERE date_vente BETWEEN ? AND ?";
        try (Connection conn = DBConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setTimestamp(1, Timestamp.valueOf(debut));
            stmt.setTimestamp(2, Timestamp.valueOf(fin));
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) return rs.getInt(1);

        } catch (SQLException e) {
            System.err.println("Erreur nombre ventes: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Profit total sur une période
     */
    public BigDecimal getTotalProfit(LocalDateTime debut, LocalDateTime fin) {
        String sql = """
                SELECT SUM((dv.prix_vente_unitaire - dv.prix_achat_unitaire) * dv.quantite)
                FROM detailsvente dv
                JOIN ventes v ON dv.id_vente = v.id
                WHERE v.date_vente BETWEEN ? AND ?
                """;

        try (Connection conn = DBConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setTimestamp(1, Timestamp.valueOf(debut));
            stmt.setTimestamp(2, Timestamp.valueOf(fin));
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                BigDecimal profit = rs.getBigDecimal(1);
                return profit != null ? profit : BigDecimal.ZERO;
            }

        } catch (SQLException e) {
            System.err.println("Erreur profit: " + e.getMessage());
        }

        return BigDecimal.ZERO;
    }

    /**
     * Mapping ResultSet → Vente
     */
    private Vente mapResultSetToVente(ResultSet rs) throws SQLException {
        LocalDateTime date = rs.getTimestamp("date_vente") != null
                ? rs.getTimestamp("date_vente").toLocalDateTime()
                : null;

        return new Vente(
                rs.getInt("id"),
                date,
                rs.getBigDecimal("total_vente"),
                rs.getInt("id_utilisateur")
        );
    }
}
