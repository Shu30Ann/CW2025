package com.comp2042;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import java.io.IOException;
 

public class MainMenuController {

    @FXML
    private javafx.scene.layout.VBox menuRoot;

    @FXML
    private void startGame() {
        try {
            // Open the difficulty selection menu (styled like main menu)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/difficultyMenu.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 800, 600);

            Stage stage = (Stage) menuRoot.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showError("Error loading difficulty menu: " + e.getMessage());
        }
    }

    @FXML
    private void showHighScores() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/highScores.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 800, 600);

            Stage stage = (Stage) menuRoot.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showError("Error loading high scores: " + e.getMessage());
        }
    }

    @FXML
    private void showSettings() {
        // TODO: Implement settings view
        showInfo("Settings feature coming soon!");
    }

    @FXML
    private void exitGame() {
        Platform.exit();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}