package com.comp2042;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class VersusController implements Initializable {

    @FXML
    private AnchorPane rootPane;
    @FXML
    private StackPane leftContainer;
    @FXML
    private StackPane rightContainer;

    private GuiController leftGui;
    private GuiController rightGui;
    private boolean finished = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadBoards();
        Platform.runLater(this::installKeyHandler);
    }

    @FXML
    private void backToMenu() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/modeMenu.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) rootPane.getScene().getWindow();
            Scene scene = new Scene(root, 800, 600);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadBoards() {
        try {
            FXMLLoader leftLoader = new FXMLLoader(getClass().getResource("/gameLayout.fxml"));
            Parent leftRoot = leftLoader.load();
            leftGui = leftLoader.getController();
            leftContainer.getChildren().setAll(leftRoot);
            new GameController(leftGui, GameDifficulty.MEDIUM);
            leftGui.gameOverProperty().addListener((obs, oldV, newV) -> {
                if (Boolean.TRUE.equals(newV)) {
                    handleGameOver(false); // left lost, right wins
                }
            });

            FXMLLoader rightLoader = new FXMLLoader(getClass().getResource("/gameLayout.fxml"));
            Parent rightRoot = rightLoader.load();
            rightGui = rightLoader.getController();
            rightContainer.getChildren().setAll(rightRoot);
            new GameController(rightGui, GameDifficulty.MEDIUM);
            rightGui.gameOverProperty().addListener((obs, oldV, newV) -> {
                if (Boolean.TRUE.equals(newV)) {
                    handleGameOver(true); // right lost, left wins
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void installKeyHandler() {
        Scene scene = rootPane.getScene();
        if (scene == null) {
            return;
        }
        scene.addEventFilter(KeyEvent.KEY_PRESSED, this::handleKey);
        rootPane.requestFocus();
    }

    private void handleKey(KeyEvent event) {
        if (leftGui != null) {
            if (event.getCode() == KeyCode.A) {
                leftGui.moveLeft();
                event.consume();
                return;
            }
            if (event.getCode() == KeyCode.D) {
                leftGui.moveRight();
                event.consume();
                return;
            }
            if (event.getCode() == KeyCode.W) {
                leftGui.rotate();
                event.consume();
                return;
            }
            if (event.getCode() == KeyCode.S) {
                leftGui.softDrop();
                event.consume();
                return;
            }
            if (event.getCode() == KeyCode.SPACE) {
                leftGui.hardDrop();
                event.consume();
                return;
            }
            if (event.getCode() == KeyCode.TAB) {
                leftGui.hold();
                event.consume();
                return;
            }
        }

        if (rightGui != null) {
            if (event.getCode() == KeyCode.LEFT) {
                rightGui.moveLeft();
                event.consume();
                return;
            }
            if (event.getCode() == KeyCode.RIGHT) {
                rightGui.moveRight();
                event.consume();
                return;
            }
            if (event.getCode() == KeyCode.UP) {
                rightGui.rotate();
                event.consume();
                return;
            }
            if (event.getCode() == KeyCode.DOWN) {
                rightGui.softDrop();
                event.consume();
                return;
            }
            if (event.getCode() == KeyCode.ENTER) {
                rightGui.hardDrop();
                event.consume();
                return;
            }
            if (event.getCode() == KeyCode.H) {
                rightGui.hold();
                event.consume();
                return;
            }
        }

        if (event.getCode() == KeyCode.P || event.getCode() == KeyCode.ESCAPE) {
            togglePauseBoth();
            event.consume();
        }
        if (event.getCode() == KeyCode.N) {
            newGameBoth();
            event.consume();
        }
    }

    private void togglePauseBoth() {
        if (leftGui != null) leftGui.togglePauseFromOutside();
        if (rightGui != null) rightGui.togglePauseFromOutside();
    }

    private void newGameBoth() {
        if (leftGui != null) leftGui.startNewGame();
        if (rightGui != null) rightGui.startNewGame();
        finished = false;
    }

    private void handleGameOver(boolean leftWins) {
        if (finished) return;
        finished = true;

        // Stop both loops
        if (leftGui != null) leftGui.stopGameLoop();
        if (rightGui != null) rightGui.stopGameLoop();

        if (leftWins) {
            if (leftGui != null) leftGui.showVictory();
        } else {
            if (rightGui != null) rightGui.showVictory();
        }
    }
}
