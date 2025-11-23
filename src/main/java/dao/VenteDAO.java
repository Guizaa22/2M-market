package dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import model.DetailVente;
import model.Vente;

/**
 * DAO pour l'enregistrement des ventes et fonctions de rapport (profit/recette)
 */
public class VenteDAO {
    
    /**
     * Enregistre une vente avec ses détails
     * @param vente La vente à enregistrer
     * @return true si l'enregistrement réussit, false sinon
     */
    public boolean create(Vente vente) {
        Connection conn = null;
        try {
            conn = DBConnector.getConnection();
            conn.setAutoCommit(false); // Démarrer une transaction
            
            // Insérer la vente
            String sqlVente = "INSERT INTO ventes (date_vente, total_vente, id_utilisateur) VALUES (?, ?, ?)";
            PreparedStatement stmtVente = conn.prepareStatement(sqlVente, Statement.RETURN_GENERATED_KEYS);
            
            stmtVente.setTimestamp(1, Timestamp.valueOf(vente.getDateVente()));
            stmtVente.setBigDecimal(2, vente.getTotalVente());
            stmtVente.setInt(3, vente.getUtilisateurId());
            
            int rowsAffected = stmtVente.executeUpdate();
            
            if (rowsAffected == 0) {
                conn.rollback();
                return false;
            }
            
            // Récupérer l'ID généré
            ResultSet rs = stmtVente.getGeneratedKeys();
            int venteId;
            if (rs.next()) {
                venteId = rs.getInt(1);
                vente.setId(venteId);
            } else {
                conn.rollback();
                return false;
            }
            
            // Insérer les détails de vente et mettre à jour le stock
            // Note: Vérifier le nom exact de la table (peut être detailsvente ou detailventes)
            String sqlDetail = "INSERT INTO detailsvente (id_vente, id_produit, quantite, prix_vente_unitaire, prix_achat_unitaire) " +
                               "VALUES (?, ?, ?, ?, ?)";
            String sqlUpdateStock = "UPDATE produits SET quantite_stock = quantite_stock - ? WHERE id = ?";
            
            PreparedStatement stmtDetail = conn.prepareStatement(sqlDetail);
            PreparedStatement stmtStock = conn.prepareStatement(sqlUpdateStock);
            
            for (DetailVente detail : vente.getDetails()) {
                // Insérer le détail
                stmtDetail.setInt(1, venteId);
                stmtDetail.setInt(2, detail.getProduitId());
                stmtDetail.setInt(3, detail.getQuantite());
                stmtDetail.setBigDecimal(4, detail.getPrixVenteUnitaire());
                stmtDetail.setBigDecimal(5, detail.getPrixAchatUnitaire());
                stmtDetail.addBatch();
                
                // Mettre à jour le stock
                stmtStock.setInt(1, detail.getQuantite());
                stmtStock.setInt(2, detail.getProduitId());
                stmtStock.addBatch();
            }
            
            stmtDetail.executeBatch();
            stmtStock.executeBatch();
            
            conn.commit();
            return true;
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'enregistrement de la vente: " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("Erreur lors du rollback: " + ex.getMessage());
                }
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("Erreur lors de la fermeture de la connexion: " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * Récupère toutes les ventes
     * @return Liste de toutes les ventes
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
            System.err.println("Erreur lors de la récupération des ventes: " + e.getMessage());
        }
        
        return ventes;
    }
    
    /**
     * Récupère les ventes d'un utilisateur
     * @param utilisateurId L'ID de l'utilisateur
     * @return Liste des ventes de l'utilisateur
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
            System.err.println("Erreur lors de la récupération des ventes: " + e.getMessage());
        }
        
        return ventes;
    }
    
    /**
     * Récupère les détails d'une vente
     * @param venteId L'ID de la vente
     * @return Liste des détails de la vente
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
            System.err.println("Erreur lors de la récupération des détails de vente: " + e.getMessage());
        }
        
        return details;
    }
    
    /**
     * Calcule le total des recettes sur une période
     * @param dateDebut Date de début
     * @param dateFin Date de fin
     * @return Le total des recettes
     */
    public BigDecimal getTotalRecettes(LocalDateTime dateDebut, LocalDateTime dateFin) {
        String sql = "SELECT SUM(total_vente) FROM ventes WHERE date_vente BETWEEN ? AND ?";
        
        try (Connection conn = DBConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setTimestamp(1, Timestamp.valueOf(dateDebut));
            stmt.setTimestamp(2, Timestamp.valueOf(dateFin));
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                BigDecimal total = rs.getBigDecimal(1);
                return total != null ? total : BigDecimal.ZERO;
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors du calcul des recettes: " + e.getMessage());
        }
        
        return BigDecimal.ZERO;
    }
    
    /**
     * Calcule le profit total sur une période
     * @param dateDebut Date de début
     * @param dateFin Date de fin
     * @return Le profit total
     */
    public BigDecimal getTotalProfit(LocalDateTime dateDebut, LocalDateTime dateFin) {
        String sql = "SELECT SUM((prix_vente_unitaire - prix_achat_unitaire) * quantite) " +
                     "FROM detailsvente dv " +
                     "INNER JOIN ventes v ON dv.id_vente = v.id " +
                     "WHERE v.date_vente BETWEEN ? AND ?";
        
        try (Connection conn = DBConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setTimestamp(1, Timestamp.valueOf(dateDebut));
            stmt.setTimestamp(2, Timestamp.valueOf(dateFin));
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                BigDecimal profit = rs.getBigDecimal(1);
                return profit != null ? profit : BigDecimal.ZERO;
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors du calcul du profit: " + e.getMessage());
        }
        
        return BigDecimal.ZERO;
    }
    
    /**
     * Mappe un ResultSet vers un objet Vente
     */
    private Vente mapResultSetToVente(ResultSet rs) throws SQLException {
        Timestamp timestamp = rs.getTimestamp("date_vente");
        LocalDateTime dateVente = timestamp != null ? timestamp.toLocalDateTime() : null;
        
        return new Vente(
            rs.getInt("id"),
            dateVente,
            rs.getBigDecimal("total_vente"),
            rs.getInt("id_utilisateur")
        );
    }
}

