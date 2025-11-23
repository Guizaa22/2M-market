package util;

import dao.DBConnector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Script pour créer/corriger l'utilisateur employé dans la base de données
 */
public class FixEmploye {
    
    public static void main(String[] args) {
        System.out.println("=== CRÉATION/CORRECTION DE L'UTILISATEUR EMPLOYÉ ===\n");
        
        try {
            // 1. Tester la connexion
            if (!DBConnector.testConnection()) {
                System.err.println("✗ Erreur: Impossible de se connecter à la base de données.");
                System.err.println("Vérifiez que MySQL est démarré dans XAMPP.");
                return;
            }
            System.out.println("✓ Connexion à la base de données réussie.\n");
            
            Connection conn = DBConnector.getConnection();
            
            // 2. Vérifier si l'utilisateur employé existe
            System.out.println("2. Vérification de l'utilisateur 'employe'...");
            PreparedStatement checkStmt = conn.prepareStatement("SELECT id, username, password_hash, role FROM utilisateurs WHERE username = ?");
            checkStmt.setString(1, "employe");
            ResultSet rs = checkStmt.executeQuery();
            
            if (!rs.next()) {
                // L'utilisateur n'existe pas, le créer
                System.out.println("  ✗ Utilisateur 'employe' non trouvé.");
                System.out.println("  Création de l'utilisateur employé...");
                
                String hash = SecurityUtil.hashPassword("admin123");
                PreparedStatement createStmt = conn.prepareStatement(
                    "INSERT INTO utilisateurs (username, password_hash, role) VALUES (?, ?, 'Employé')"
                );
                createStmt.setString(1, "employe");
                createStmt.setString(2, hash);
                createStmt.executeUpdate();
                createStmt.close();
                
                System.out.println("  ✓ Utilisateur 'employe' créé avec succès !");
                System.out.println("    Mot de passe: admin123");
                System.out.println("    Role: Employé");
            } else {
                // L'utilisateur existe, vérifier et corriger le mot de passe si nécessaire
                int id = rs.getInt("id");
                String currentHash = rs.getString("password_hash");
                String role = rs.getString("role");
                
                System.out.println("  ✓ Utilisateur 'employe' trouvé.");
                System.out.println("    ID: " + id);
                System.out.println("    Role: " + role);
                System.out.println("    Hash actuel: " + (currentHash != null ? currentHash.substring(0, Math.min(30, currentHash.length())) + "..." : "NULL"));
                
                boolean isBCrypt = currentHash != null && currentHash.startsWith("$2a$");
                System.out.println("    Est un hash BCrypt: " + isBCrypt);
                
                if (!isBCrypt || currentHash == null) {
                    // Le mot de passe n'est pas un hash BCrypt valide, le corriger
                    System.out.println("\n3. Génération d'un nouveau hash BCrypt pour 'admin123'...");
                    String newHash = SecurityUtil.hashPassword("admin123");
                    System.out.println("  Nouveau hash: " + newHash.substring(0, Math.min(30, newHash.length())) + "...");
                    
                    System.out.println("\n4. Mise à jour du mot de passe dans la base de données...");
                    PreparedStatement updateStmt = conn.prepareStatement(
                        "UPDATE utilisateurs SET password_hash = ? WHERE username = ?"
                    );
                    updateStmt.setString(1, newHash);
                    updateStmt.setString(2, "employe");
                    
                    int rowsAffected = updateStmt.executeUpdate();
                    updateStmt.close();
                    
                    if (rowsAffected > 0) {
                        System.out.println("  ✓ Mot de passe mis à jour avec succès !");
                    } else {
                        System.err.println("  ✗ Erreur: Aucune ligne mise à jour.");
                    }
                } else {
                    System.out.println("\n3. Le mot de passe est déjà un hash BCrypt valide.");
                }
            }
            
            rs.close();
            checkStmt.close();
            
            // 5. Vérifier que l'authentification fonctionne
            System.out.println("\n5. Vérification de l'authentification...");
            dao.UtilisateurDAO dao = new dao.UtilisateurDAO();
            model.Utilisateur user = dao.authenticate("employe", "admin123");
            
            if (user != null) {
                System.out.println("  ✓ Authentification réussie !");
                System.out.println("    Username: " + user.getUsername());
                System.out.println("    Role: " + user.getRole());
                System.out.println("\n=== SUCCÈS ===");
                System.out.println("L'utilisateur employé peut maintenant se connecter avec:");
                System.out.println("  Username: employe");
                System.out.println("  Password: admin123");
            } else {
                System.err.println("  ✗ Erreur: L'authentification échoue toujours.");
                System.err.println("  Vérifiez les logs ci-dessus pour plus de détails.");
            }
            
        } catch (Exception e) {
            System.err.println("✗ ERREUR: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBConnector.closeConnection();
        }
    }
}

