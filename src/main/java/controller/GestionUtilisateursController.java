package controller;

import dao.UtilisateurDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Utilisateur;
import util.SecurityUtil;

/**
 * Contrôleur pour la gestion des utilisateurs (Admin uniquement)
 */
public class GestionUtilisateursController {
    
    @FXML
    private TextField usernameField;
    
    @FXML
    private PasswordField passwordField;
    
    @FXML
    private ComboBox<Utilisateur.Role> roleComboBox;
    
    @FXML
    private TableView<Utilisateur> utilisateursTable;
    
    @FXML
    private TableColumn<Utilisateur, String> usernameColumn;
    
    @FXML
    private TableColumn<Utilisateur, String> roleColumn;
    
    @FXML
    private Button ajouterButton;
    
    @FXML
    private Button modifierButton;
    
    @FXML
    private Button supprimerButton;
    
    @FXML
    private Button retourButton;
    
    private UtilisateurDAO utilisateurDAO;
    private ObservableList<Utilisateur> utilisateursList;
    private Utilisateur utilisateurSelectionne;
    
    @FXML
    private void initialize() {
        utilisateurDAO = new UtilisateurDAO();
        utilisateursList = FXCollections.observableArrayList();
        
        // Configuration du ComboBox de rôle
        roleComboBox.setItems(FXCollections.observableArrayList(Utilisateur.Role.values()));
        roleComboBox.setValue(Utilisateur.Role.Employé);
        
        // Configuration des colonnes
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
        
        utilisateursTable.setItems(utilisateursList);
        utilisateursTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                utilisateurSelectionne = newSelection;
                if (newSelection != null) {
                    remplirFormulaire(newSelection);
                }
            }
        );
        
        chargerUtilisateurs();
    }
    
    @FXML
    private void handleAjouter() {
        if (validerFormulaire()) {
            String username = usernameField.getText().trim();
            String password = passwordField.getText();
            Utilisateur.Role role = roleComboBox.getValue();
            
            if (utilisateurDAO.usernameExists(username)) {
                showAlert(Alert.AlertType.WARNING, "Nom d'utilisateur existant", 
                         "Un utilisateur avec ce nom existe déjà.");
                return;
            }
            
            Utilisateur utilisateur = new Utilisateur(
                username,
                SecurityUtil.hashPassword(password),
                role
            );
            
            if (utilisateurDAO.create(utilisateur)) {
                showAlert(Alert.AlertType.INFORMATION, "Succès", 
                         "Utilisateur ajouté avec succès.");
                viderFormulaire();
                chargerUtilisateurs();
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", 
                         "Erreur lors de l'ajout de l'utilisateur.");
            }
        }
    }
    
    @FXML
    private void handleModifier() {
        if (utilisateurSelectionne == null) {
            showAlert(Alert.AlertType.WARNING, "Aucune sélection", 
                     "Veuillez sélectionner un utilisateur à modifier.");
            return;
        }
        
        if (validerFormulaire()) {
            String username = usernameField.getText().trim();
            String password = passwordField.getText();
            Utilisateur.Role role = roleComboBox.getValue();
            
            // Vérifier si le nom d'utilisateur a changé et s'il existe déjà
            if (!username.equals(utilisateurSelectionne.getUsername()) &&
                utilisateurDAO.usernameExists(username)) {
                showAlert(Alert.AlertType.WARNING, "Nom d'utilisateur existant", 
                         "Un utilisateur avec ce nom existe déjà.");
                return;
            }
            
            utilisateurSelectionne.setUsername(username);
            utilisateurSelectionne.setRole(role);
            
            // Mettre à jour le mot de passe seulement s'il n'est pas vide
            if (!password.isEmpty()) {
                utilisateurSelectionne.setPasswordHash(SecurityUtil.hashPassword(password));
            }
            
            if (utilisateurDAO.update(utilisateurSelectionne)) {
                showAlert(Alert.AlertType.INFORMATION, "Succès", 
                         "Utilisateur modifié avec succès.");
                viderFormulaire();
                chargerUtilisateurs();
                utilisateurSelectionne = null;
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", 
                         "Erreur lors de la modification de l'utilisateur.");
            }
        }
    }
    
    @FXML
    private void handleSupprimer() {
        if (utilisateurSelectionne == null) {
            showAlert(Alert.AlertType.WARNING, "Aucune sélection", 
                     "Veuillez sélectionner un utilisateur à supprimer.");
            return;
        }
        
        // Empêcher la suppression de l'utilisateur actuellement connecté
        Utilisateur utilisateurConnecte = ConnexionController.getUtilisateurConnecte();
        if (utilisateurConnecte != null && utilisateurConnecte.getId() == utilisateurSelectionne.getId()) {
            showAlert(Alert.AlertType.WARNING, "Action interdite", 
                     "Vous ne pouvez pas supprimer votre propre compte.");
            return;
        }
        
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation de suppression");
        confirmAlert.setHeaderText(null);
        confirmAlert.setContentText("Êtes-vous sûr de vouloir supprimer l'utilisateur \"" + 
                                   utilisateurSelectionne.getUsername() + "\" ?");
        
        if (confirmAlert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            if (utilisateurDAO.delete(utilisateurSelectionne.getId())) {
                showAlert(Alert.AlertType.INFORMATION, "Succès", 
                         "Utilisateur supprimé avec succès.");
                viderFormulaire();
                chargerUtilisateurs();
                utilisateurSelectionne = null;
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", 
                         "Erreur lors de la suppression de l'utilisateur.");
            }
        }
    }
    
    @FXML
    private void handleRetour() {
        try {
            javafx.stage.Stage stage = (javafx.stage.Stage) retourButton.getScene().getWindow();
            util.FXMLUtils.changeScene(stage, "/view/AdminDashboard.fxml", "Dashboard Administrateur");
        } catch (java.lang.Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", 
                     "Erreur lors du retour: " + e.getMessage());
        }
    }
    
    private void chargerUtilisateurs() {
        utilisateursList.clear();
        utilisateursList.addAll(utilisateurDAO.findAll());
    }
    
    private void remplirFormulaire(Utilisateur utilisateur) {
        usernameField.setText(utilisateur.getUsername());
        passwordField.clear(); // Ne pas afficher le mot de passe hashé
        roleComboBox.setValue(utilisateur.getRole());
    }
    
    private void viderFormulaire() {
        usernameField.clear();
        passwordField.clear();
        roleComboBox.setValue(Utilisateur.Role.Employé);
        utilisateursTable.getSelectionModel().clearSelection();
    }
    
    private boolean validerFormulaire() {
        if (usernameField.getText().trim().isEmpty() ||
            roleComboBox.getValue() == null) {
            
            showAlert(Alert.AlertType.WARNING, "Champs vides", 
                     "Veuillez remplir tous les champs obligatoires.");
            return false;
        }
        
        // Pour la création, le mot de passe est obligatoire
        if (utilisateurSelectionne == null && passwordField.getText().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Mot de passe requis", 
                     "Veuillez entrer un mot de passe pour créer un nouvel utilisateur.");
            return false;
        }
        
        return true;
    }
    
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

