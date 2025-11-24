package com.comp2042;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.function.Consumer;

public class DifficultyController {

    @FXML
    private VBox menuRoot;

    private Scene returnScene;
    private Consumer<GameDifficulty> onDifficultySelected;
    private Runnable onReturn;

    @FXML
    private void startEasy() {
        startGameWithDifficulty(GameDifficulty.EASY);
    }

    @FXML
    private void startMedium() {
        startGameWithDifficulty(GameDifficulty.MEDIUM);
    }

    @FXML
    private void startHard() {
        startGameWithDifficulty(GameDifficulty.HARD);
    }

    @FXML
    private void backToMain() {
        // Return to main menu scene
        try {
            Stage stage = (Stage) menuRoot.getScene().getWindow();

            if (returnScene != null) {
                stage.setScene(returnScene);
                stage.show();
                if (onReturn != null) {
                    onReturn.run();
                }
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/mainMenu.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 800, 600);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            // fallback: ignore
            e.printStackTrace();
        }
    }

    private void startGameWithDifficulty(GameDifficulty difficulty) {
        try {
            Stage stage = (Stage) menuRoot.getScene().getWindow();

            if (onDifficultySelected != null) {
                onDifficultySelected.accept(difficulty);
                if (returnScene != null) {
                    stage.setScene(returnScene);
                    stage.show();
                }
                if (onReturn != null) {
                    onReturn.run();
                }
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gameLayout.fxml"));
            Parent gameRoot = loader.load();
            Scene gameScene = new Scene(gameRoot, 800, 600);

            GuiController guiController = loader.getController();
            new GameController(guiController, difficulty);

            stage.setScene(gameScene);
            stage.show();

            gameRoot.requestFocus();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setReturnScene(Scene returnScene) {
        this.returnScene = returnScene;
    }

    public void setOnDifficultySelected(Consumer<GameDifficulty> onDifficultySelected) {
        this.onDifficultySelected = onDifficultySelected;
    }

    public void setOnReturn(Runnable onReturn) {
        this.onReturn = onReturn;
    }
}
