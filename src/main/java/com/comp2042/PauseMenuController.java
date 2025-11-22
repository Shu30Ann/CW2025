package com.comp2042;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Window;

import com.comp2042.SoundManager;

public class PauseMenuController {

    @FXML private StackPane rootPane;
    @FXML private Button resumeBtn;
    @FXML private Button mainMenuBtn;
    @FXML private Button highScoresBtn;
    @FXML private Button settingsBtn;

    private GuiController guiController; // parent reference

    public void initialize() {
        resumeBtn.setOnAction(e -> {
            if (guiController != null) {
                guiController.updatePauseState(false);
            }
        });

        mainMenuBtn.setOnAction(e -> navigateTo("/mainMenu.fxml"));
        highScoresBtn.setOnAction(e -> navigateToHighScores());
        settingsBtn.setOnAction(e -> navigateToSettings());
    }

    public void setGuiController(GuiController controller) {
        this.guiController = controller;
    }

    private void navigateTo(String resourcePath) {
        try {
            if (guiController != null) {
                guiController.stopGameLoop();
            }
            if ("/mainMenu.fxml".equals(resourcePath)) {
                SoundManager.stopBackgroundMusic();
            }
            Window owner = rootPane.getScene().getWindow();
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource(resourcePath));
            javafx.scene.Parent root = loader.load();
            javafx.scene.Scene scene = new javafx.scene.Scene(root, 800, 600);
            javafx.stage.Stage stage = (javafx.stage.Stage) owner;
            stage.setScene(scene);
            stage.show();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void navigateToHighScores() {
        try {
            Scene previousScene = rootPane.getScene();
            Stage stage = (Stage) previousScene.getWindow();

            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/highScores.fxml"));
            javafx.scene.Parent root = loader.load();
            HighScoresController controller = loader.getController();
            controller.setReturnScene(previousScene);
            controller.setOnBack(() -> {
                stage.setScene(previousScene);
                if (guiController != null) {
                    guiController.updatePauseState(true); // return to pause menu view, keep game paused
                }
            });

            stage.setScene(new javafx.scene.Scene(root, 800, 600));
            stage.show();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void navigateToSettings() {
        try {
            Scene previousScene = rootPane.getScene();
            Stage stage = (Stage) previousScene.getWindow();

            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/settings.fxml"));
            javafx.scene.Parent root = loader.load();
            SettingsController controller = loader.getController();
            controller.setReturnScene(previousScene);
            controller.setOnSaved(() -> {
                // Return to game and resume
                stage.setScene(previousScene);
                if (guiController != null) {
                    guiController.updatePauseState(false);
                }
            });

            stage.setScene(new javafx.scene.Scene(root, 800, 600));
            stage.show();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
