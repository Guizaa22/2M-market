package app;

import javafx.application.Application;
import javafx.stage.Stage;
import util.FXMLUtils;

/**
 * La classe principale qui démarre l'application
 */
public class MainApp extends Application {
    
    @Override
    public void start(Stage primaryStage) {
        try {
            // Charger la vue de connexion
            FXMLUtils.changeScene(primaryStage, "/view/Connexion.fxml", "Connexion - 2M Market");
            primaryStage.setResizable(true);
            primaryStage.setMinWidth(1024);
            primaryStage.setMinHeight(768);
            // Maximize window for better UX
            primaryStage.setMaximized(true);
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erreur lors du démarrage de l'application: " + e.getMessage());
        }
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}

