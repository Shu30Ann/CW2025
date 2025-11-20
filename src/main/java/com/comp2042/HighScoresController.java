package com.comp2042;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

/**
 * Controller for displaying high scores leaderboard.
 */
public class HighScoresController {

    @FXML
    private VBox highScoresContainer;

    private HighScoreManager highScoreManager;

    @FXML
    public void initialize() {
        this.highScoreManager = new HighScoreManager();
        displayHighScores();
    }

    /**
     * Display the high scores in the UI.
     */
    private void displayHighScores() {
        highScoresContainer.getChildren().clear();

        // Title
        Label titleLabel = new Label("HIGH SCORES");
        titleLabel.setTextFill(Color.WHITE);
        titleLabel.setFont(Font.font("Arial", 48));
        titleLabel.setStyle("-fx-font-weight: bold;");
        highScoresContainer.getChildren().add(titleLabel);

        // Get top 5 scores
        List<HighScore> topScores = highScoreManager.getTopScores(5);

        if (topScores.isEmpty()) {
            Label emptyLabel = new Label("No High Scores Yet!");
            emptyLabel.setTextFill(Color.WHITE);
            emptyLabel.setFont(Font.font("Arial", 24));
            emptyLabel.setStyle("-fx-opacity: 0.7;");
            highScoresContainer.getChildren().add(emptyLabel);
        } else {
            // Display each score with ranking
            for (int i = 0; i < topScores.size(); i++) {
                HighScore score = topScores.get(i);
                Label scoreLabel = new Label((i + 1) + ". " + score.getScore() + " points");
                scoreLabel.setTextFill(Color.WHITE);
                scoreLabel.setFont(Font.font("Arial", 32));
                scoreLabel.setStyle("-fx-font-weight: bold; -fx-padding: 10px;");
                highScoresContainer.getChildren().add(scoreLabel);
            }
        }

        // Add spacing before buttons
        Label spacer = new Label();
        spacer.setPrefHeight(40);
        highScoresContainer.getChildren().add(spacer);

        // Back button
        Button backButton = new Button("Back To Main Menu");
        backButton.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-font-size: 18px; -fx-min-width: 220px; -fx-padding: 10px;");
        backButton.setOnAction(e -> backToMainMenu());
        highScoresContainer.getChildren().add(backButton);
    }

    /**
     * Return to the main menu.
     */
    @FXML
    private void backToMainMenu() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/mainMenu.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) highScoresContainer.getScene().getWindow();
            Scene scene = new Scene(root, 800, 600);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            System.err.println("Error loading main menu: " + e.getMessage());
        }
    }
}
