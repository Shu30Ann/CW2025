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
            // Load the game scene
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gameLayout.fxml"));
            Parent gameRoot = loader.load();
            Scene gameScene = new Scene(gameRoot, 800, 600);
            
            // Get the current stage
            Stage stage = (Stage) menuRoot.getScene().getWindow();
            
            // Set up the game controller
            GuiController guiController = loader.getController();
            new GameController(guiController);
            
            // Set the game scene
            stage.setScene(gameScene);
            stage.show();
            
            // Give focus to the game scene for keyboard input
            gameRoot.requestFocus();
        } catch (IOException e) {
            showError("Error loading game scene: " + e.getMessage());
        }
    }

    @FXML
    private void showHighScores() {
        // TODO: Implement high scores view
        showInfo("High Scores feature coming soon!");
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