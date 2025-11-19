package com.comp2042;

import com.comp2042.logic.bricks.Brick;

public interface Board {

    boolean moveBrickDown();

    boolean moveBrickLeft();

    boolean moveBrickRight();

    boolean rotateLeftBrick();

    boolean createNewBrick();

    int[][] getBoardMatrix();

    ViewData getViewData();

    void mergeBrickToBackground();

    ClearRow clearRows();

    Score getScore();

    void newGame();

    ViewData getNextBrickViewData();

    ViewData getCurrentBrickViewData();

    Brick getCurrentBrick();

    void setCurrentBrick(Brick brick);
}
