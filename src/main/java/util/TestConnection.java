package util;

import dao.DBConnector;

/**
 * Classe utilitaire pour tester la connexion à la base de données MySQL
 */
public class TestConnection {
    
    public static void main(String[] args) {
        System.out.println("=== Test de connexion MySQL ===");
        System.out.println("URL: " + Config.DB_URL);
        System.out.println("User: " + Config.DB_USER);
        System.out.println("Password: " + (Config.DB_PASSWORD.isEmpty() ? "(vide)" : "***"));
        System.out.println();
        
        try {
            if (DBConnector.testConnection()) {
                System.out.println("✓ Connexion réussie !");
                System.out.println("La base de données MySQL est accessible.");
                
                // Tester si la base de données existe
                try {
                    java.sql.Connection conn = DBConnector.getConnection();
                    java.sql.DatabaseMetaData metaData = conn.getMetaData();
                    java.sql.ResultSet databases = metaData.getCatalogs();
                    
                    boolean dbExists = false;
                    while (databases.next()) {
                        String dbName = databases.getString("TABLE_CAT");
                        if (Config.DATABASE_NAME.equals(dbName)) {
                            dbExists = true;
                            break;
                        }
                    }
                    databases.close();
                    
                    if (dbExists) {
                        System.out.println("✓ La base de données '" + Config.DATABASE_NAME + "' existe.");
                    } else {
                        System.out.println("⚠ La base de données '" + Config.DATABASE_NAME + "' n'existe pas.");
                        System.out.println("  Exécutez le script: database/init_database.sql");
                    }
                    
                    DBConnector.closeConnection();
                } catch (Exception e) {
                    System.err.println("Erreur lors de la vérification de la base de données: " + e.getMessage());
                }
                
            } else {
                System.out.println("✗ Connexion échouée !");
                System.out.println();
                System.out.println("Vérifiez que:");
                System.out.println("1. MySQL est démarré dans XAMPP");
                System.out.println("2. Le port 3306 est correct");
                System.out.println("3. Les identifiants dans Config.java sont corrects");
            }
        } catch (Exception e) {
            System.err.println("✗ Erreur: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBConnector.closeConnection();
        }
    }
}

