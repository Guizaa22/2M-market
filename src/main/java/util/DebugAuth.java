package util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import dao.DBConnector;
import dao.UtilisateurDAO;

/**
 * Script de débogage pour l'authentification
 */
public class DebugAuth {
    
    public static void main(String[] args) {
        System.out.println("=== DEBUG AUTHENTIFICATION ===\n");
        
        try {
            // 1. Tester la connexion
            System.out.println("1. Test de connexion à la base de données...");
            if (!DBConnector.testConnection()) {
                System.err.println("   ✗ Connexion échouée !");
                return;
            }
            System.out.println("   ✓ Connexion réussie\n");
            
            // 2. Vérifier la structure de la table
            System.out.println("2. Vérification de la structure de la table...");
            try (Connection conn = DBConnector.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("DESCRIBE utilisateurs")) {
                
                System.out.println("   Colonnes de la table 'utilisateurs':");
                while (rs.next()) {
                    System.out.println("   - " + rs.getString("Field") + " (" + rs.getString("Type") + ")");
                }
            }
            System.out.println();
            
            // 3. Vérifier les données de l'utilisateur admin
            System.out.println("3. Données de l'utilisateur 'admin'...");
            try (Connection conn = DBConnector.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT * FROM utilisateurs WHERE username = 'admin'")) {
                
                if (rs.next()) {
                    int id = rs.getInt("id");
                    String username = rs.getString("username");
                    String passwordHash = null;
                    
                    // Essayer les deux noms de colonnes
                    try {
                        passwordHash = rs.getString("passwordHash");
                        System.out.println("   Colonne trouvée: passwordHash (camelCase)");
                    } catch (java.sql.SQLException e) {
                        try {
                            passwordHash = rs.getString("password_hash");
                            System.out.println("   Colonne trouvée: password_hash (snake_case)");
                        } catch (java.sql.SQLException e2) {
                            System.err.println("   ✗ Aucune colonne de mot de passe trouvée !");
                        }
                    }
                
                    String role = rs.getString("role");
                    
                    System.out.println("   ID: " + id);
                    System.out.println("   Username: " + username);
                    System.out.println("   Password Hash: " + (passwordHash != null ? passwordHash : "NULL"));
                    System.out.println("   Role: " + role);
                    System.out.println();
                    
                    // 4. Tester le hash
                    if (passwordHash != null) {
                        System.out.println("4. Test du hash BCrypt...");
                        System.out.println("   Hash stocké: " + passwordHash);
                        System.out.println("   Longueur: " + passwordHash.length());
                        System.out.println("   Commence par $2a$: " + passwordHash.startsWith("$2a$"));
                        System.out.println();
                        
                        // Tester avec "admin123"
                        System.out.println("5. Test d'authentification avec 'admin123'...");
                        boolean isValid = util.SecurityUtil.checkPassword("admin123", passwordHash);
                        System.out.println("   Résultat: " + (isValid ? "✓ VALIDE" : "✗ INVALIDE"));
                        System.out.println();
                        
                        // Tester avec "password123" (au cas où)
                        System.out.println("6. Test d'authentification avec 'password123'...");
                        boolean isValid2 = util.SecurityUtil.checkPassword("password123", passwordHash);
                        System.out.println("   Résultat: " + (isValid2 ? "✓ VALIDE" : "✗ INVALIDE"));
                        System.out.println();
                        
                        // 7. Tester via UtilisateurDAO
                        System.out.println("7. Test via UtilisateurDAO.authenticate('admin', 'admin123')...");
                        UtilisateurDAO dao = new UtilisateurDAO();
                        model.Utilisateur user = dao.authenticate("admin", "admin123");
                        if (user != null) {
                            System.out.println("   ✓ Authentification réussie !");
                            System.out.println("   Utilisateur: " + user.getUsername() + " (" + user.getRole() + ")");
                        } else {
                            System.out.println("   ✗ Authentification échouée !");
                        }
                    } else {
                        System.err.println("   ✗ Impossible de récupérer le hash du mot de passe !");
                    }
                } else {
                    System.err.println("   ✗ Utilisateur 'admin' non trouvé dans la base de données !");
                }
            }
            
        } catch (java.lang.Exception e) {
            System.err.println("✗ ERREUR: " + e.getMessage());
            System.err.println("Type: " + e.getClass().getSimpleName());
        } finally {
            DBConnector.closeConnection();
        }
        
        System.out.println("\n=== FIN DU DEBUG ===");
    }
}

