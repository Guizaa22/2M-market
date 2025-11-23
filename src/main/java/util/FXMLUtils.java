package util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Classe utilitaire pour charger les vues FXML
 */
public class FXMLUtils {
    
    /**
     * Charge une vue FXML et retourne le Parent
     * @param fxmlPath Le chemin vers le fichier FXML (ex: "/view/Connexion.fxml")
     * @return Le Parent chargé
     * @throws IOException Si le fichier FXML ne peut pas être chargé
     */
    public static Parent loadFXML(String fxmlPath) throws IOException {
        FXMLLoader loader = new FXMLLoader(FXMLUtils.class.getResource(fxmlPath));
        return loader.load();
    }
    
    /**
     * Charge une vue FXML et change la scène d'une fenêtre
     * @param stage La fenêtre dont la scène doit être changée
     * @param fxmlPath Le chemin vers le fichier FXML
     * @param title Le titre de la fenêtre
     * @throws IOException Si le fichier FXML ne peut pas être chargé
     */
    public static void changeScene(Stage stage, String fxmlPath, String title) throws IOException {
        Parent root = loadFXML(fxmlPath);
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle(title);
        stage.centerOnScreen();
    }
    
    /**
     * Charge une vue FXML avec un contrôleur personnalisé
     * @param fxmlPath Le chemin vers le fichier FXML
     * @param controller Le contrôleur à utiliser
     * @return Le Parent chargé
     * @throws IOException Si le fichier FXML ne peut pas être chargé
     */
    public static Parent loadFXML(String fxmlPath, Object controller) throws IOException {
        FXMLLoader loader = new FXMLLoader(FXMLUtils.class.getResource(fxmlPath));
        loader.setController(controller);
        return loader.load();
    }
}

