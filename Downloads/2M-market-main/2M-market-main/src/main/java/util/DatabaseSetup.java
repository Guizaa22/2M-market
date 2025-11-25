package util;

import dao.DBConnector;
import dao.UtilisateurDAO;
import model.Utilisateur;

/**
 * Classe utilitaire pour initialiser la base de données avec des utilisateurs par défaut
 * Exécuter cette classe après avoir créé la base de données pour créer les utilisateurs
 */
public class DatabaseSetup {
    
    public static void main(String[] args) {
        try {
            // Tester la connexion
            if (!DBConnector.testConnection()) {
                System.err.println("Erreur: Impossible de se connecter à la base de données.");
                System.err.println("Vérifiez que MySQL est démarré et que la base '2market' existe.");
                return;
            }
            
            System.out.println("Connexion à la base de données réussie.");
            
            UtilisateurDAO utilisateurDAO = new UtilisateurDAO();
            
            // Créer l'utilisateur admin s'il n'existe pas
            if (!utilisateurDAO.usernameExists("admin")) {
                Utilisateur admin = new Utilisateur(
                    "admin",
                    SecurityUtil.hashPassword("admin123"),
                    Utilisateur.Role.Admin
                );
                if (utilisateurDAO.create(admin)) {
                    System.out.println("✓ Utilisateur 'admin' créé avec succès (mot de passe: admin123)");
                } else {
                    System.err.println("✗ Erreur lors de la création de l'utilisateur 'admin'");
                }
            } else {
                System.out.println("⚠ L'utilisateur 'admin' existe déjà.");
            }
            
            // Créer l'utilisateur employé s'il n'existe pas
            if (!utilisateurDAO.usernameExists("employe")) {
                Utilisateur employe = new Utilisateur(
                    "employe",
                    SecurityUtil.hashPassword("admin123"),
                    Utilisateur.Role.Employé
                );
                if (utilisateurDAO.create(employe)) {
                    System.out.println("✓ Utilisateur 'employe' créé avec succès (mot de passe: admin123)");
                } else {
                    System.err.println("✗ Erreur lors de la création de l'utilisateur 'employe'");
                }
            } else {
                System.out.println("⚠ L'utilisateur 'employe' existe déjà.");
            }
            
            System.out.println("\n✓ Configuration terminée !");
            System.out.println("Vous pouvez maintenant vous connecter avec:");
            System.out.println("  - admin / admin123 (Admin)");
            System.out.println("  - employe / admin123 (Employé)");
            
        } catch (Exception e) {
            System.err.println("Erreur lors de la configuration: " + e.getMessage());
            System.err.println("Détails: " + e.getClass().getSimpleName());
        } finally {
            DBConnector.closeConnection();
        }
    }
}

