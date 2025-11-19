package com.comp2042;

public interface InputEventListener {

    DownData onDownEvent(MoveEvent event);

    ViewData onLeftEvent(MoveEvent event);

    ViewData onRightEvent(MoveEvent event);

    ViewData onRotateEvent(MoveEvent event);

    void createNewGame();

    /**
     * Gets the current score object.
     * @return the Score object containing current score and combo information
     */
    Score getScore();

    /**
     * Gets the current board matrix.
     * This is used by the GUI to calculate where the ghost piece should appear.
     * @return a 2D int array representing the current state of the board.
     */
    int[][] getBoardMatrix();

    void hardDrop();

    ViewData holdCurrentBrick();

}
