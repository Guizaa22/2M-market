package util;

/**
 * Utilitaire pour générer des hachages de mots de passe
 * Exécuter cette classe pour générer un hash BCrypt pour un mot de passe
 */
public class PasswordHashGenerator {
    
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: java PasswordHashGenerator <password>");
            System.out.println("Example: java PasswordHashGenerator admin123");
            return;
        }
        
        String password = args[0];
        String hash = SecurityUtil.hashPassword(password);
        System.out.println("Password: " + password);
        System.out.println("Hash: " + hash);
        System.out.println("\nSQL INSERT statement:");
        System.out.println("INSERT INTO utilisateurs (username, passwordHash, role) VALUES ");
        System.out.println("('admin', '" + hash + "', 'Admin');");
    }
}

