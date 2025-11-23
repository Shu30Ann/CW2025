package com.comp2042;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ClearRow {

    private final int linesRemoved;
    private final int[][] newMatrix;
    private final int scoreBonus;
    private final List<Integer> clearedRows;

    public ClearRow(int linesRemoved, int[][] newMatrix, int scoreBonus, List<Integer> clearedRows) {
        this.linesRemoved = linesRemoved;
        this.newMatrix = newMatrix;
        this.scoreBonus = scoreBonus;
        this.clearedRows = Collections.unmodifiableList(new ArrayList<>(clearedRows));
    }

    public int getLinesRemoved() {
        return linesRemoved;
    }

    public int[][] getNewMatrix() {
        return MatrixOperations.copy(newMatrix);
    }

    public int getScoreBonus() {
        return scoreBonus;
    }

    public List<Integer> getClearedRows() {
        return clearedRows;
    }
}
