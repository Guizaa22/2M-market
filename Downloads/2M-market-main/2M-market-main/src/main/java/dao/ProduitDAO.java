package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import model.Produit;

/**
 * DAO pour les opérations CRUD sur la table Produits
 */
public class ProduitDAO {
    
    /**
     * Récupère tous les produits
     * @return Liste de tous les produits
     */
    public List<Produit> findAll() {
        List<Produit> produits = new ArrayList<>();
        String sql = "SELECT * FROM produits ORDER BY nom";
        
        try (Connection conn = DBConnector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                produits.add(mapResultSetToProduit(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des produits: " + e.getMessage());
        }
        
        return produits;
    }
    
    /**
     * Récupère un produit par son ID
     * @param id L'ID du produit
     * @return Le produit trouvé, null sinon
     */
    public Produit findById(int id) {
        String sql = "SELECT * FROM produits WHERE id = ?";
        
        try (Connection conn = DBConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToProduit(rs);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche de produit: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Récupère un produit par son code-barres
     * @param codeBarre Le code-barres du produit
     * @return Le produit trouvé, null sinon
     */
    public Produit findByCodeBarre(String codeBarre) {
        String sql = "SELECT * FROM produits WHERE code_barre = ?";
        
        try (Connection conn = DBConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, codeBarre);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToProduit(rs);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche de produit par code-barres: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Récupère un produit par son nom (recherche exacte)
     * @param nom Le nom du produit
     * @return Le produit trouvé, null sinon
     */
    public Produit findByNomExact(String nom) {
        String sql = "SELECT * FROM produits WHERE nom = ?";
        
        try (Connection conn = DBConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, nom);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToProduit(rs);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche de produit par nom: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Recherche intelligente : essaie d'abord par code-barres, puis par nom exact
     * @param recherche Le terme de recherche (code-barres ou nom)
     * @return Le produit trouvé, null sinon
     */
    public Produit rechercherProduit(String recherche) {
        // Essayer d'abord par code-barres
        Produit produit = findByCodeBarre(recherche);
        if (produit != null) {
            return produit;
        }
        
        // Ensuite par nom exact
        produit = findByNomExact(recherche);
        return produit;
    }
    
    /**
     * Récupère les produits avec stock faible
     * @return Liste des produits avec stock faible
     */
    public List<Produit> findStockFaible() {
        List<Produit> produits = new ArrayList<>();
        String sql = "SELECT * FROM produits WHERE quantite_stock <= seuil_alerte ORDER BY quantite_stock ASC";
        
        try (Connection conn = DBConnector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                produits.add(mapResultSetToProduit(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des produits à stock faible: " + e.getMessage());
        }
        
        return produits;
    }
    
    /**
     * Crée un nouveau produit
     * @param produit Le produit à créer
     * @return true si la création réussit, false sinon
     */
    public boolean create(Produit produit) {
        // Vérifier si la colonne unite existe
        boolean hasUnite = columnExists("unite");
        String sql;
        if (hasUnite) {
            sql = "INSERT INTO produits (code_barre, nom, categorie, prix_achat_actuel, prix_vente_defaut, quantite_stock, unite, seuil_alerte) " +
                  "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        } else {
            sql = "INSERT INTO produits (code_barre, nom, categorie, prix_achat_actuel, prix_vente_defaut, quantite_stock, seuil_alerte) " +
                  "VALUES (?, ?, ?, ?, ?, ?, ?)";
        }
        
        try (Connection conn = DBConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            int paramIndex = 1;
            stmt.setString(paramIndex++, produit.getCodeBarre());
            stmt.setString(paramIndex++, produit.getNom());
            stmt.setString(paramIndex++, produit.getCategorie() != null ? produit.getCategorie() : "");
            stmt.setBigDecimal(paramIndex++, produit.getPrixAchatActuel());
            stmt.setBigDecimal(paramIndex++, produit.getPrixVenteDefaut());
            stmt.setInt(paramIndex++, produit.getQuantiteStock());
            if (hasUnite) {
                stmt.setString(paramIndex++, produit.getUnite());
            }
            stmt.setInt(paramIndex++, produit.getSeuilAlerte());
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    produit.setId(rs.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la création de produit: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Met à jour un produit
     * @param produit Le produit à mettre à jour
     * @return true si la mise à jour réussit, false sinon
     */
    public boolean update(Produit produit) {
        // Vérifier si la colonne unite existe
        boolean hasUnite = columnExists("unite");
        String sql;
        if (hasUnite) {
            sql = "UPDATE produits SET code_barre = ?, nom = ?, categorie = ?, prix_achat_actuel = ?, " +
                  "prix_vente_defaut = ?, quantite_stock = ?, unite = ?, seuil_alerte = ? WHERE id = ?";
        } else {
            sql = "UPDATE produits SET code_barre = ?, nom = ?, categorie = ?, prix_achat_actuel = ?, " +
                  "prix_vente_defaut = ?, quantite_stock = ?, seuil_alerte = ? WHERE id = ?";
        }
        
        try (Connection conn = DBConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            int paramIndex = 1;
            stmt.setString(paramIndex++, produit.getCodeBarre());
            stmt.setString(paramIndex++, produit.getNom());
            stmt.setString(paramIndex++, produit.getCategorie() != null ? produit.getCategorie() : "");
            stmt.setBigDecimal(paramIndex++, produit.getPrixAchatActuel());
            stmt.setBigDecimal(paramIndex++, produit.getPrixVenteDefaut());
            stmt.setInt(paramIndex++, produit.getQuantiteStock());
            if (hasUnite) {
                stmt.setString(paramIndex++, produit.getUnite());
            }
            stmt.setInt(paramIndex++, produit.getSeuilAlerte());
            stmt.setInt(paramIndex++, produit.getId());
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour de produit: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Supprime un produit
     * @param id L'ID du produit à supprimer
     * @return true si la suppression réussit, false sinon
     */
    public boolean delete(int id) {
        String sql = "DELETE FROM produits WHERE id = ?";
        
        try (Connection conn = DBConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression de produit: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Met à jour la quantité en stock d'un produit
     * @param produitId L'ID du produit
     * @param quantite La nouvelle quantité
     * @return true si la mise à jour réussit, false sinon
     */
    public boolean updateStock(int produitId, int quantite) {
        String sql = "UPDATE produits SET quantite_stock = ? WHERE id = ?";
        
        try (Connection conn = DBConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, quantite);
            stmt.setInt(2, produitId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour du stock: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Vérifie si un code-barres existe déjà
     * @param codeBarre Le code-barres à vérifier
     * @return true si le code-barres existe, false sinon
     */
    public boolean codeBarreExists(String codeBarre) {
        String sql = "SELECT COUNT(*) FROM produits WHERE code_barre = ?";
        
        try (Connection conn = DBConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, codeBarre);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la vérification du code-barres: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Mappe un ResultSet vers un objet Produit
     */
    /**
     * Récupère toutes les catégories distinctes (méthode de compatibilité)
     * Utilise la table categories si disponible, sinon la colonne categorie
     * @return Liste des noms de catégories
     */
    public List<String> findAllCategories() {
        List<String> categories = new ArrayList<>();
        
        // Essayer d'abord avec la table categories
        try {
            String sql = "SELECT categorie FROM produits ORDER BY nom";
            try (Connection conn = DBConnector.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                
                while (rs.next()) {
                    categories.add(rs.getString("nom"));
                }
                return categories;
            }
        } catch (SQLException e) {
            // Si la table categories n'existe pas, utiliser l'ancienne méthode
            String sql = "SELECT DISTINCT categorie FROM produits WHERE categorie IS NOT NULL AND categorie != '' ORDER BY categorie";
            
            try (Connection conn = DBConnector.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                
                while (rs.next()) {
                    String categorie = rs.getString("categorie");
                    if (categorie != null && !categorie.isEmpty()) {
                        categories.add(categorie);
                    }
                }
            } catch (SQLException e2) {
                System.err.println("Erreur lors de la récupération des catégories: " + e2.getMessage());
            }
        }
        
        return categories;
    }
    
    /**
     * Récupère les produits d'une catégorie (par nom de catégorie)
     * @param categorieNom Le nom de la catégorie
     * @return Liste des produits de la catégorie
     */
    public List<Produit> findByCategorie(String categorieNom) {
        List<Produit> produits = new ArrayList<>();
        
        // Essayer d'abord avec la table categories (via category_id)
        try {
            String sql = "SELECT p.* FROM produits p " +
                       "INNER JOIN categories c ON p.category_id = c.id " +
                       "WHERE c.nom = ? AND p.quantite_stock > 0 ORDER BY p.nom";
            
            try (Connection conn = DBConnector.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                stmt.setString(1, categorieNom);
                ResultSet rs = stmt.executeQuery();
                
                while (rs.next()) {
                    produits.add(mapResultSetToProduit(rs));
                }
                return produits;
            }
        } catch (SQLException e) {
            // Si la table categories n'existe pas, utiliser l'ancienne méthode
            String sql = "SELECT * FROM produits WHERE categorie = ? AND quantite_stock > 0 ORDER BY nom";
            
            try (Connection conn = DBConnector.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                stmt.setString(1, categorieNom);
                ResultSet rs = stmt.executeQuery();
                
                while (rs.next()) {
                    produits.add(mapResultSetToProduit(rs));
                }
            } catch (SQLException e2) {
                System.err.println("Erreur lors de la récupération des produits par catégorie: " + e2.getMessage());
            }
        }
        
        return produits;
    }
    
    /**
     * Récupère les produits d'une catégorie (par ID de catégorie)
     * @param categoryId L'ID de la catégorie
     * @return Liste des produits de la catégorie
     */
    public List<Produit> findByCategoryId(int categoryId) {
        List<Produit> produits = new ArrayList<>();
        String sql = "SELECT * FROM produits WHERE category_id = ? AND quantite_stock > 0 ORDER BY nom";
        
        try (Connection conn = DBConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, categoryId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                produits.add(mapResultSetToProduit(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des produits par category_id: " + e.getMessage());
        }
        
        return produits;
    }
    
    private Produit mapResultSetToProduit(ResultSet rs) throws SQLException {
        // La base de données utilise snake_case
        String categorie = null;
        try {
            categorie = rs.getString("categorie");
        } catch (SQLException e) {
            // Colonne categorie peut ne pas exister dans certaines bases
        }
        
        String unite = null;
        try {
            unite = rs.getString("unite");
        } catch (SQLException e) {
            // Colonne unite peut ne pas exister dans certaines bases
        }
        
        return new Produit(
            rs.getInt("id"),
            rs.getString("code_barre"),
            rs.getString("nom"),
            categorie != null ? categorie : "",
            rs.getBigDecimal("prix_achat_actuel"),
            rs.getBigDecimal("prix_vente_defaut"),
            rs.getInt("quantite_stock"),
            unite,
            rs.getInt("seuil_alerte")
        );
    }
    
    /**
     * Vérifie si une colonne existe dans la table produits
     */
    private boolean columnExists(String columnName) {
        String sql = "SELECT COUNT(*) as count FROM INFORMATION_SCHEMA.COLUMNS " +
                     "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'produits' AND COLUMN_NAME = ?";
        
        try (Connection conn = DBConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, columnName);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("count") > 0;
            }
        } catch (SQLException e) {
            // En cas d'erreur, supposer que la colonne n'existe pas
            return false;
        }
        
        return false;
    }
}

