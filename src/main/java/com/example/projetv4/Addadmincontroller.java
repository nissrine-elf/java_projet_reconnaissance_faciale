package com.example.projetv4;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;

public class Addadmincontroller {
    @FXML
    private TextField nomm;
    @FXML
    private TextField mdp;

    public void addadminn(ActionEvent actionEvent) {
        String name = nomm.getText();
        String prenom = mdp.getText();
        if (name.isEmpty() || prenom.isEmpty()) {
            showAlert("Erreur", "Veuillez remplir tous les champs.");
            return;
        }
        DatabaseManager.addAdmin(nomm.getText(),mdp.getText());
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText("L'admin a été ajouté avec succès.");
        alert.setTitle("ajouter admin");
        alert.showAndWait();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

