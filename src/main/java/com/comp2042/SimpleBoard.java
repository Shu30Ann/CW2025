package com.comp2042;

import com.comp2042.logic.bricks.Brick;
import com.comp2042.logic.bricks.BrickGenerator;
import com.comp2042.logic.bricks.RandomBrickGenerator;

import com.comp2042.PointInt;

public class SimpleBoard implements Board {

    // width = number of columns, height = number of rows
    private final int width;
    private final int height;
    private final BrickGenerator brickGenerator;
    private final BrickRotator brickRotator;
    private int[][] currentGameMatrix;
    private PointInt currentOffset;
    private final Score score;
    private Brick nextBrick;

    private static final int ROTATION_STATES = 4;


    // Constructor arguments are passed as (rows, cols) by callers (e.g. new SimpleBoard(25, 10)).
    // Internally we want currentGameMatrix[row][col] so interpret the first arg as rows (height)
    // and the second as cols (width).
    public SimpleBoard(int rows, int cols) {
        this.width = cols;
        this.height = rows;
        // matrix indexed as [rows][cols]
        currentGameMatrix = new int[height][width];
        brickGenerator = new RandomBrickGenerator();
        nextBrick = brickGenerator.getBrick();
        brickRotator = new BrickRotator();
        score = new Score();
    }

    @Override
    public boolean moveBrickDown() {
        return tryMove(0, 1);
    }


    @Override
    public boolean moveBrickLeft() {
        return tryMove(-1, 0);
    }

    @Override
    public boolean moveBrickRight() {
        return tryMove(1, 0);
    }

    @Override
    public boolean rotateLeftBrick() {
        int from = brickRotator.getCurrentOrientation();
        int to = (from + 1) % brickRotator.getOrientationCount();
        int[][] rotatedShape = brickRotator.getShapeAt(to);

        for (PointInt kick : SrsKickTable.getKickData(brickRotator.getBrick(), from, to)) {
            int testX = currentOffset.getX() + kick.getX();
            int testY = currentOffset.getY() - kick.getY(); // kick tables use +Y as up
            if (!MatrixOperations.intersect(currentGameMatrix, rotatedShape, testX, testY)) {
                currentOffset = new PointInt(testX, testY);
                brickRotator.setCurrentShape(to);
                return true;
            }
        }
        // as a safety net (helps near tight walls/floors even if tables mismatch shapes)
        int[][] fallbackOffsets = { {-2,0}, {-1,0}, {1,0}, {2,0}, {0,-1}, {0,1} };
        for (int[] off : fallbackOffsets) {
            int testX = currentOffset.getX() + off[0];
            int testY = currentOffset.getY() + off[1];
            if (!MatrixOperations.intersect(currentGameMatrix, rotatedShape, testX, testY)) {
                currentOffset = new PointInt(testX, testY);
                brickRotator.setCurrentShape(to);
                return true;
            }
        }
        return false;
    }


    @Override
    public boolean createNewBrick() {
        // Use the previously prepared next brick as the current brick
        Brick currentBrick = nextBrick;
        brickRotator.setBrick(currentBrick);

        // Generate a new next brick
        nextBrick = brickGenerator.getBrick();

        // Center the new brick horizontally on the board
        int[][] shape = brickRotator.getCurrentShape();
        int shapeWidth = shape[0].length;
        int boardCols = currentGameMatrix[0].length;
        int startX = Math.round((boardCols - shapeWidth) / 2.0f);
        if (startX < 0) {
            startX = 0;
        }
    currentOffset = new PointInt(startX, 0);

        // Return true if new brick immediately collides (game over)
        return MatrixOperations.intersect(currentGameMatrix, shape, (int) currentOffset.getX(), (int) currentOffset.getY());
    }


    @Override
    public int[][] getBoardMatrix() {
        return MatrixOperations.copy(currentGameMatrix);
    }


    @Override
    public ViewData getViewData() {
        int[][] currentShape = brickRotator.getCurrentShape();
        PointInt ghostPos = getGhostPosition();
        

        return new ViewData(
            currentShape,
            (int) currentOffset.getX(),
            (int) currentOffset.getY(),
            nextBrick.getShapeMatrix().get(0),
            ghostPos
        );
    }


    @Override
    public void mergeBrickToBackground() {
        currentGameMatrix = MatrixOperations.merge(currentGameMatrix, brickRotator.getCurrentShape(), (int) currentOffset.getX(), (int) currentOffset.getY());
    }

    @Override
    public ClearRow clearRows() {
        ClearRow clearRow = MatrixOperations.checkRemoving(currentGameMatrix);
        currentGameMatrix = clearRow.getNewMatrix();
        return clearRow;

    }

    @Override
    public Score getScore() {
        return score;
    }


    @Override
    public void newGame() {
        // ensure matrix dimensions are [rows][cols]
        currentGameMatrix = new int[height][width];
        score.reset();
        createNewBrick();
    }
    
    public ViewData getNextBrickViewData() {
        return new ViewData(
            nextBrick.getShapeMatrix().get(0),
            0, 0,
            nextBrick.getShapeMatrix().get(0)
        );
    }

    public ViewData getCurrentBrickViewData() {
        int[][] shape = brickRotator.getCurrentShape(); // current brick shape
        int x = currentOffset.getX(); // current X position
        int y = currentOffset.getY(); // current Y position
        int[][] nextShapeMatrix = nextBrick.getShapeMatrix().get(0); // for next brick preview
        PointInt ghostPos = getGhostPosition(); // ghost brick position

        return new ViewData(shape, x, y, nextShapeMatrix, ghostPos);
    }



    public PointInt getGhostPosition() {
        int[][] matrix = MatrixOperations.copy(currentGameMatrix);
        int[][] shape = brickRotator.getCurrentShape();

        // Use the board's current offset for X
        int ghostX = currentOffset.x;

        // Start from current Y.
        int ghostY = currentOffset.y;

        // Move down until *next step* collides.
        while (!MatrixOperations.intersect(matrix, shape, ghostX, ghostY + 1)) {
            ghostY++;
        }

        PointInt ghost = new PointInt(ghostX, ghostY);

        return ghost;
    }

    @Override
    public void setCurrentBrick(Brick brick) {
        brickRotator.setBrick(brick);
        
        // Center the brick horizontally on the board
        int[][] shape = brickRotator.getCurrentShape();
        int shapeWidth = shape[0].length;
        int boardCols = currentGameMatrix[0].length;
        int startX = Math.round((boardCols - shapeWidth) / 2.0f);
        if (startX < 0) {
            startX = 0;
        }
        currentOffset = new PointInt(startX, 0);
    }

    @Override
    public Brick getCurrentBrick() {
        return brickRotator.getBrick();
    }

    @Override
    public boolean isBrickGrounded() {
        int[][] shape = brickRotator.getCurrentShape();
        return MatrixOperations.intersect(currentGameMatrix, shape, currentOffset.getX(), currentOffset.getY() + 1);
    }

    private boolean tryMove(int dx, int dy) {
        int[][] currentMatrix = MatrixOperations.copy(currentGameMatrix);
        PointInt p = new PointInt(currentOffset);
        p.translate(dx, dy);
        boolean conflict = MatrixOperations.intersect(currentMatrix, brickRotator.getCurrentShape(), p.getX(), p.getY());
        if (conflict) {
            return false;
        }
        currentOffset = p;
        return true;
    }
}
