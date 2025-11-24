package com.comp2042;

import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;


public class GameOverPanel extends BorderPane {

    private final Label gameOverLabel;
    private final Label highScoreLabel;
    private static final String DEFAULT_TEXT = "GAME OVER";
    private static final String VICTORY_TEXT = "VICTORY!!!";

    public GameOverPanel() {
        gameOverLabel = new Label(DEFAULT_TEXT);
        gameOverLabel.getStyleClass().add("gameOverStyle");

        highScoreLabel = new Label("NEW HIGH SCORE!");
        highScoreLabel.getStyleClass().add("highScoreBanner");
        highScoreLabel.setVisible(false);
        highScoreLabel.setManaged(false);

        VBox container = new VBox(10, gameOverLabel, highScoreLabel);
        container.setAlignment(Pos.CENTER);
        setCenter(container);
    }

    public void showNewHighScore() {
        highScoreLabel.setVisible(true);
        highScoreLabel.setManaged(true);
    }

    public void hideNewHighScore() {
        highScoreLabel.setVisible(false);
        highScoreLabel.setManaged(false);
    }

    public void showVictory() {
        gameOverLabel.setText(VICTORY_TEXT);
        setVisible(true);
    }

    public void resetText() {
        gameOverLabel.setText(DEFAULT_TEXT);
        hideNewHighScore();
    }

}
