package util;

import dao.DBConnector;
import dao.UtilisateurDAO;
import model.Utilisateur;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Script pour créer/corriger tous les utilisateurs (admin et employé) dans la base de données
 */
public class SetupAllUsers {
    
    public static void main(String[] args) {
        System.out.println("=== CONFIGURATION DE TOUS LES UTILISATEURS ===\n");
        
        try {
            // 1. Tester la connexion
            if (!DBConnector.testConnection()) {
                System.err.println("✗ Erreur: Impossible de se connecter à la base de données.");
                System.err.println("Vérifiez que MySQL est démarré dans XAMPP.");
                return;
            }
            System.out.println("✓ Connexion à la base de données réussie.\n");
            
            Connection conn = DBConnector.getConnection();
            UtilisateurDAO dao = new UtilisateurDAO();
            
            // 2. Créer/Corriger l'utilisateur admin
            System.out.println("2. Configuration de l'utilisateur 'admin'...");
            setupUser(conn, "admin", "admin123", Utilisateur.Role.Admin);
            
            // 3. Créer/Corriger l'utilisateur employé
            System.out.println("\n3. Configuration de l'utilisateur 'employe'...");
            setupUser(conn, "employe", "admin123", Utilisateur.Role.Employé);
            
            // 4. Vérifier que les deux utilisateurs fonctionnent
            System.out.println("\n4. Vérification de l'authentification...");
            
            model.Utilisateur admin = dao.authenticate("admin", "admin123");
            if (admin != null) {
                System.out.println("  ✓ Admin: Authentification réussie");
            } else {
                System.err.println("  ✗ Admin: Authentification échouée");
            }
            
            model.Utilisateur employe = dao.authenticate("employe", "admin123");
            if (employe != null) {
                System.out.println("  ✓ Employé: Authentification réussie");
            } else {
                System.err.println("  ✗ Employé: Authentification échouée");
            }
            
            System.out.println("\n=== SUCCÈS ===");
            System.out.println("Tous les utilisateurs sont configurés !");
            System.out.println("\nComptes disponibles:");
            System.out.println("  Admin:");
            System.out.println("    Username: admin");
            System.out.println("    Password: admin123");
            System.out.println("  Employé:");
            System.out.println("    Username: employe");
            System.out.println("    Password: admin123");
            
        } catch (Exception e) {
            System.err.println("✗ ERREUR: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBConnector.closeConnection();
        }
    }
    
    private static void setupUser(Connection conn, String username, String password, Utilisateur.Role role) {
        try {
            PreparedStatement checkStmt = conn.prepareStatement(
                "SELECT id, password_hash FROM utilisateurs WHERE username = ?"
            );
            checkStmt.setString(1, username);
            ResultSet rs = checkStmt.executeQuery();
            
            if (!rs.next()) {
                // L'utilisateur n'existe pas, le créer
                System.out.println("  Création de l'utilisateur '" + username + "'...");
                String hash = SecurityUtil.hashPassword(password);
                
                PreparedStatement createStmt = conn.prepareStatement(
                    "INSERT INTO utilisateurs (username, password_hash, role) VALUES (?, ?, ?)"
                );
                createStmt.setString(1, username);
                createStmt.setString(2, hash);
                createStmt.setString(3, role.name());
                createStmt.executeUpdate();
                createStmt.close();
                
                System.out.println("  ✓ Utilisateur '" + username + "' créé avec succès !");
            } else {
                // L'utilisateur existe, vérifier le hash
                String currentHash = rs.getString("password_hash");
                boolean isBCrypt = currentHash != null && currentHash.startsWith("$2a$");
                
                if (!isBCrypt || currentHash == null) {
                    // Le mot de passe n'est pas un hash BCrypt valide, le corriger
                    System.out.println("  Correction du mot de passe pour '" + username + "'...");
                    String newHash = SecurityUtil.hashPassword(password);
                    
                    PreparedStatement updateStmt = conn.prepareStatement(
                        "UPDATE utilisateurs SET password_hash = ? WHERE username = ?"
                    );
                    updateStmt.setString(1, newHash);
                    updateStmt.setString(2, username);
                    updateStmt.executeUpdate();
                    updateStmt.close();
                    
                    System.out.println("  ✓ Mot de passe corrigé pour '" + username + "' !");
                } else {
                    System.out.println("  ✓ Utilisateur '" + username + "' existe déjà avec un hash valide.");
                }
            }
            
            rs.close();
            checkStmt.close();
            
        } catch (Exception e) {
            System.err.println("  ✗ Erreur lors de la configuration de '" + username + "': " + e.getMessage());
        }
    }
}

