package com.comp2042;

public class GameController implements InputEventListener {

    private Board board = new SimpleBoard(25, 10);
    private final GuiController viewGuiController;
    private boolean isPaused;
    private GameDifficulty difficulty;
    
    public GameController(GuiController c) {
        this(c, GameDifficulty.MEDIUM);
    }

    public GameController(GuiController c, GameDifficulty difficulty) {
        this.difficulty = difficulty;
        viewGuiController = c;
        board.createNewBrick();
        viewGuiController.showNextShape(board.getNextBrickViewData());
        viewGuiController.setEventListener(this);
        viewGuiController.initGameView(board.getBoardMatrix(), board.getViewData(), difficulty);
        viewGuiController.bindScore(board.getScore().scoreProperty());
    }

    public void togglePause() {
        isPaused = !isPaused;
        viewGuiController.updatePauseState(isPaused);
    }

    @Override
    public int[][] getBoardMatrix() {
        // Return your game’s board matrix here.
        // Example:
        return board.getBoardMatrix(); // if your Board class has such a method
    }


    @Override
    public DownData onDownEvent(MoveEvent event) {
        if (isPaused) {
            return new DownData(null, board.getViewData());
        }
        boolean canMove = board.moveBrickDown();
        ClearRow clearRow = null;
        if (!canMove) {
            board.mergeBrickToBackground();
            clearRow = board.clearRows();
            if (clearRow.getLinesRemoved() > 0) {
                // Calculate score based on number of lines cleared and current combo
                int baseScore = calculateLineScore(clearRow.getLinesRemoved());
                board.getScore().add(baseScore);
                
                // Handle combo
                board.getScore().addCombo();
                if (board.getScore().comboProperty().get() > 1) {
                    // Double the score for combos
                    board.getScore().add(baseScore);
                }
            } else {
                board.getScore().resetCombo();
            }

            boolean isCollision = board.createNewBrick();
            if (isCollision) {
                viewGuiController.gameOver();
            } else {
                // Update the next shape preview
                viewGuiController.showNextShape(board.getNextBrickViewData());
            }

            viewGuiController.refreshGameBackground(board.getBoardMatrix());

        }
        return new DownData(clearRow, board.getViewData());
    }

    @Override
    public ViewData onLeftEvent(MoveEvent event) {
        board.moveBrickLeft();
        return board.getViewData();
    }

    @Override
    public ViewData onRightEvent(MoveEvent event) {
        board.moveBrickRight();
        return board.getViewData();
    }

    @Override
    public ViewData onRotateEvent(MoveEvent event) {
        board.rotateLeftBrick();
        return board.getViewData();
    }

    public void hardDrop() {
        if (isPaused) return;

        // Keep moving down until cannot
        boolean moved = true;
        while (moved) {
            moved = board.moveBrickDown();
        }

        // The brick stops -> lock it
        board.mergeBrickToBackground();

        // Clear completed rows
        ClearRow clearRow = board.clearRows();
        if (clearRow.getLinesRemoved() > 0) {
            int baseScore = calculateLineScore(clearRow.getLinesRemoved());
            board.getScore().add(baseScore);

            board.getScore().addCombo();
            if (board.getScore().comboProperty().get() > 1) {
                board.getScore().add(baseScore);
            }
        } else {
            board.getScore().resetCombo();
        }

        // Spawn the next brick
        boolean isCollision = board.createNewBrick();
        if (isCollision) {
            viewGuiController.gameOver();
            return;
        }

        // Update panels
        viewGuiController.showNextShape(board.getNextBrickViewData());
        viewGuiController.refreshGameBackground(board.getBoardMatrix());
    }


    @Override
    public void createNewGame() {
        board.newGame();
        viewGuiController.refreshGameBackground(board.getBoardMatrix());
    }

    @Override
    public Score getScore() {
        return board.getScore();
    }

    /**
     * Calculates the score for clearing lines.
     * Scoring system:
     * 1 line = 100 points
     * 2 lines = 300 points
     * 3 lines = 500 points
     * 4 lines = 800 points
     */
    private int calculateLineScore(int lines) {
        switch (lines) {
            case 1: return 100;
            case 2: return 300;
            case 3: return 500;
            case 4: return 800;
            default: return 0;
        }
    }
}
