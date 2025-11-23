package com.comp2042;

import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;


public class GameOverPanel extends BorderPane {

    private final Label gameOverLabel;
    private final Label highScoreLabel;

    public GameOverPanel() {
        gameOverLabel = new Label("GAME OVER");
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

}
