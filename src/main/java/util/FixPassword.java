package util;

import dao.DBConnector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Script pour corriger automatiquement le mot de passe dans la base de données
 */
public class FixPassword {
    
    public static void main(String[] args) {
        System.out.println("=== CORRECTION DU MOT DE PASSE ===\n");
        
        try {
            // 1. Tester la connexion
            if (!DBConnector.testConnection()) {
                System.err.println("✗ Erreur: Impossible de se connecter à la base de données.");
                System.err.println("Vérifiez que MySQL est démarré dans XAMPP.");
                return;
            }
            System.out.println("✓ Connexion à la base de données réussie.\n");
            
            Connection conn = DBConnector.getConnection();
            
            // 2. Vérifier l'utilisateur admin
            System.out.println("2. Vérification de l'utilisateur 'admin'...");
            PreparedStatement checkStmt = conn.prepareStatement("SELECT id, username, password_hash FROM utilisateurs WHERE username = ?");
            checkStmt.setString(1, "admin");
            ResultSet rs = checkStmt.executeQuery();
            
            if (!rs.next()) {
                System.err.println("✗ Utilisateur 'admin' non trouvé !");
                System.out.println("Création de l'utilisateur admin...");
                
                // Créer l'utilisateur admin
                String hash = SecurityUtil.hashPassword("admin123");
                PreparedStatement createStmt = conn.prepareStatement(
                    "INSERT INTO utilisateurs (username, password_hash, role) VALUES (?, ?, 'Admin')"
                );
                createStmt.setString(1, "admin");
                createStmt.setString(2, hash);
                createStmt.executeUpdate();
                createStmt.close();
                
                System.out.println("✓ Utilisateur 'admin' créé avec succès !");
                System.out.println("  Mot de passe: admin123");
            } else {
                int id = rs.getInt("id");
                String currentHash = rs.getString("password_hash");
                
                System.out.println("  ID: " + id);
                System.out.println("  Hash actuel: " + (currentHash != null ? currentHash.substring(0, Math.min(30, currentHash.length())) + "..." : "NULL"));
                System.out.println("  Est un hash BCrypt: " + (currentHash != null && currentHash.startsWith("$2a$")));
                
                // 3. Générer un nouveau hash BCrypt pour "admin123"
                System.out.println("\n3. Génération d'un nouveau hash BCrypt pour 'admin123'...");
                String newHash = SecurityUtil.hashPassword("admin123");
                System.out.println("  Nouveau hash: " + newHash.substring(0, Math.min(30, newHash.length())) + "...");
                
                // 4. Mettre à jour le mot de passe
                System.out.println("\n4. Mise à jour du mot de passe dans la base de données...");
                PreparedStatement updateStmt = conn.prepareStatement(
                    "UPDATE utilisateurs SET password_hash = ? WHERE username = ?"
                );
                updateStmt.setString(1, newHash);
                updateStmt.setString(2, "admin");
                
                int rowsAffected = updateStmt.executeUpdate();
                updateStmt.close();
                
                if (rowsAffected > 0) {
                    System.out.println("✓ Mot de passe mis à jour avec succès !");
                } else {
                    System.err.println("✗ Erreur: Aucune ligne mise à jour.");
                }
            }
            
            rs.close();
            checkStmt.close();
            
            // 5. Vérifier que ça fonctionne
            System.out.println("\n5. Vérification de l'authentification...");
            dao.UtilisateurDAO dao = new dao.UtilisateurDAO();
            model.Utilisateur user = dao.authenticate("admin", "admin123");
            
            if (user != null) {
                System.out.println("✓ Authentification réussie !");
                System.out.println("  Username: " + user.getUsername());
                System.out.println("  Role: " + user.getRole());
                System.out.println("\n=== SUCCÈS ===");
                System.out.println("Vous pouvez maintenant vous connecter avec:");
                System.out.println("  Username: admin");
                System.out.println("  Password: admin123");
            } else {
                System.err.println("✗ Erreur: L'authentification échoue toujours.");
                System.err.println("Vérifiez les logs ci-dessus pour plus de détails.");
            }
            
        } catch (Exception e) {
            System.err.println("✗ ERREUR: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBConnector.closeConnection();
        }
    }
}

