package com.comp2042;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

public final class Score {

    private final IntegerProperty score = new SimpleIntegerProperty(0);
    private final IntegerProperty combo = new SimpleIntegerProperty(0);
    private final IntegerProperty linesCleared = new SimpleIntegerProperty(0);
    private final IntegerProperty highScore = new SimpleIntegerProperty(0);

    public IntegerProperty scoreProperty() {
        return score;
    }

    public IntegerProperty comboProperty() {
        return combo;
    }

    public IntegerProperty linesClearedProperty() {
        return linesCleared;
    }

    public IntegerProperty highScoreProperty() {
        return highScore;
    }

    public void add(int points) {
        int comboMultiplier = Math.max(1, combo.get());
        int finalPoints = points * comboMultiplier;
        score.setValue(score.getValue() + finalPoints);
        
        if (score.getValue() > highScore.get()) {
            highScore.setValue(score.getValue());
        }
    }

    public void addCombo() {
        combo.setValue(combo.get() + 1);
    }

    public void resetCombo() {
        combo.setValue(0);
    }

    public void addLinesCleared(int lines) {
        linesCleared.setValue(linesCleared.getValue() + lines);
    }

    public void reset() {
        score.setValue(0);
        combo.setValue(0);
        linesCleared.setValue(0);
    }
}
