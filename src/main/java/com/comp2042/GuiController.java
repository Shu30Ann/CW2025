package com.comp2042;

import javafx.animation.KeyFrame;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.effect.Reflection;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.util.Duration;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ChoiceDialog;
import javafx.geometry.Pos;
import javafx.scene.layout.VBox;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class GuiController implements Initializable {

    private static final int BRICK_SIZE = 20;
    private static final int VISIBLE_ROW_OFFSET = 0;

    @FXML private GridPane gamePanel;
    @FXML private Group groupNotification;
    @FXML private GridPane brickPanel;
    @FXML private GameOverPanel gameOverPanel;
    @FXML private Group gameCanvas;
    @FXML private StackPane nextBlockBox;
    @FXML private StackPane holdBlockBox;
    @FXML private GridPane nextShapePanel;
    @FXML private GridPane holdShapePanel;
    @FXML private GridPane ghostPanel;
    @FXML private Label scoreLabel;
    @FXML private Label linesLabel;


    private Rectangle[][] displayMatrix;
    private Rectangle[][] rectangles;
    private InputEventListener eventListener;
    private Timeline timeLine;
    // pixel origin of the top-left cell inside the gamePanel (accounts for padding/border)
    private double gridOriginX = 0;
    private double gridOriginY = 0;

    private final BooleanProperty isPause = new SimpleBooleanProperty();
    private final BooleanProperty isGameOver = new SimpleBooleanProperty();
    private StackPane pauseMenuRoot;
    private PauseMenuController pauseMenuController;
    private GameDifficulty currentDifficulty;

    private double getCellHeight() { return BRICK_SIZE + gamePanel.getVgap(); }
    private double getCellWidth()  { return BRICK_SIZE + gamePanel.getHgap(); }

    private int getVisibleRowCount(int[][] boardMatrix) { return boardMatrix.length - VISIBLE_ROW_OFFSET; }
    private double getGamePanelPixelWidth(int[][] boardMatrix) { return boardMatrix[0].length * getCellWidth(); }
    private double getGamePanelPixelHeight(int[][] boardMatrix) { return getVisibleRowCount(boardMatrix) * getCellHeight(); }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // load font (if available)
        try {
            Font.loadFont(getClass().getClassLoader().getResource("digital.ttf").toExternalForm(), 38);
        } catch (Exception ignored) {}

        gamePanel.setFocusTraversable(true);
        gamePanel.requestFocus();

        SoundManager.playBackgroundMusic();

        // Key handling
        gamePanel.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                handleKeyInput(keyEvent);
            }
        });

        gameOverPanel.setVisible(false);

        final Reflection reflection = new Reflection();
        reflection.setFraction(0.8);
        reflection.setTopOpacity(0.9);
        reflection.setTopOffset(-12);

        // When scene is attached, add resize listeners to recenter
        gamePanel.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.widthProperty().addListener((o, oldW, newW) -> centerGamePanel());
                newScene.heightProperty().addListener((o, oldH, newH) -> centerGamePanel());
                // bind scaling of the entire gameCanvas so UI scales when window is resized
                DoubleBinding scaleBinding = Bindings.createDoubleBinding(() -> {
                    double w = newScene.getWidth();
                    double h = newScene.getHeight();
                    double targetW = gamePanel.getPrefWidth() <= 0 ? gamePanel.getWidth() : gamePanel.getPrefWidth();
                    double targetH = gamePanel.getPrefHeight() <= 0 ? gamePanel.getHeight() : gamePanel.getPrefHeight();
                    if (targetW <= 0 || targetH <= 0) return 1.0;
                    double sx = w / targetW;
                    double sy = h / targetH;
                    return Math.max(0.5, Math.min(1.0, Math.min(sx, sy))); // clamp between 0.5 and 1.0
                }, newScene.widthProperty(), newScene.heightProperty(), gamePanel.prefWidthProperty(), gamePanel.prefHeightProperty());

                gameCanvas.scaleXProperty().bind(scaleBinding);
                gameCanvas.scaleYProperty().bind(scaleBinding);
            }
        });
    }

    /**
     * Public helpers so external controllers (e.g., Versus mode) can trigger the
     * same input handling logic without synthesizing KeyEvents.
     */
    public void moveLeft() {
        if (!canAcceptInput()) return;
        refreshBrick(eventListener.onLeftEvent(new MoveEvent(EventType.LEFT, EventSource.USER)));
    }

    public void moveRight() {
        if (!canAcceptInput()) return;
        refreshBrick(eventListener.onRightEvent(new MoveEvent(EventType.RIGHT, EventSource.USER)));
    }

    public void rotate() {
        if (!canAcceptInput()) return;
        refreshBrick(eventListener.onRotateEvent(new MoveEvent(EventType.ROTATE, EventSource.USER)));
    }

    public void softDrop() {
        if (!canAcceptInput()) return;
        moveDown(new MoveEvent(EventType.DOWN, EventSource.USER));
    }

    public void hardDrop() {
        if (!canAcceptInput()) return;
        eventListener.hardDrop();
    }

    public void hold() {
        if (!canAcceptInput()) return;
        eventListener.holdCurrentBrick();
    }

    public void togglePauseFromOutside() {
        pauseGame(null);
    }

    public void startNewGame() {
        gameOverPanel.resetText();
        newGame(null);
    }

    public void showVictory() {
        if (timeLine != null) {
            try { timeLine.stop(); } catch (Exception ignored) {}
        }
        gameOverPanel.showVictory();
        isGameOver.setValue(Boolean.TRUE);
    }

    public BooleanProperty gameOverProperty() {
        return isGameOver;
    }

    private boolean canAcceptInput() {
        return isPause.getValue() == Boolean.FALSE && isGameOver.getValue() == Boolean.FALSE;
    }

    private void handleKeyInput(KeyEvent keyEvent) {
        if (canAcceptInput()) {
            if (keyEvent.getCode() == KeyCode.LEFT || keyEvent.getCode() == KeyCode.A) {
                moveLeft();
                keyEvent.consume();
            } else if (keyEvent.getCode() == KeyCode.RIGHT || keyEvent.getCode() == KeyCode.D) {
                moveRight();
                keyEvent.consume();
            } else if (keyEvent.getCode() == KeyCode.UP || keyEvent.getCode() == KeyCode.W) {
                rotate();
                keyEvent.consume();
            } else if (keyEvent.getCode() == KeyCode.DOWN || keyEvent.getCode() == KeyCode.S) {
                softDrop();
                keyEvent.consume();
            }
            else if (keyEvent.getCode() == KeyCode.SPACE) {
                hardDrop();
                keyEvent.consume();
            }
            else if (keyEvent.getCode() == KeyCode.H) {
                hold();
                keyEvent.consume();
            }
        }
        if (keyEvent.getCode() == KeyCode.N) {
            startNewGame();
        } else if (keyEvent.getCode() == KeyCode.P || keyEvent.getCode() == KeyCode.ESCAPE) {
            togglePauseFromOutside();
            keyEvent.consume();
        }
    }

    /**
     * Initialize the visible grid and the brick rectangles.
     * boardMatrix: full logical board (including hidden rows)
     * brick: current ViewData (brick shape + position)
     */
    public void initGameView(int[][] boardMatrix, ViewData brick, GameDifficulty difficulty) {
        this.currentDifficulty = difficulty;
        // set the preferred size of gamePanel (so centering works)
        double panelW = getGamePanelPixelWidth(boardMatrix);
        double panelH = getGamePanelPixelHeight(boardMatrix);
        gamePanel.setPrefWidth(panelW);
        gamePanel.setPrefHeight(panelH);

        // Center the gamePanel in the window

        // create displayMatrix ONCE and fill visible rows only
        displayMatrix = new Rectangle[boardMatrix.length][boardMatrix[0].length];
        for (int i = VISIBLE_ROW_OFFSET; i < boardMatrix.length; i++) {
            for (int j = 0; j < boardMatrix[i].length; j++) {
                Rectangle rectangle = new Rectangle(BRICK_SIZE, BRICK_SIZE);
                rectangle.setFill(Color.TRANSPARENT);
                displayMatrix[i][j] = rectangle;
                gamePanel.add(rectangle, j, i - VISIBLE_ROW_OFFSET);
            }
        }

        // create rectangles for the current falling brick
        rectangles = new Rectangle[brick.getBrickData().length][brick.getBrickData()[0].length];
        for (int i = 0; i < brick.getBrickData().length; i++) {
            for (int j = 0; j < brick.getBrickData()[i].length; j++) {
                Rectangle rectangle = new Rectangle(BRICK_SIZE, BRICK_SIZE);
                rectangle.setFill(getFillColor(brick.getBrickData()[i][j]));
                rectangles[i][j] = rectangle;
                brickPanel.add(rectangle, j, i);
            }
        }

        // Initialize ghostPanel with the same grid size as gamePanel
        ghostPanel.getChildren().clear();
        ghostPanel.getColumnConstraints().clear();
        ghostPanel.getRowConstraints().clear();

        for (int col = 0; col < boardMatrix[0].length; col++) {
            ghostPanel.getColumnConstraints().add(new ColumnConstraints(BRICK_SIZE));
        }

        for (int row = 0; row < boardMatrix.length - VISIBLE_ROW_OFFSET; row++) {
            ghostPanel.getRowConstraints().add(new RowConstraints(BRICK_SIZE));
        }


        // start timeline
        createTimeline(difficulty.getDropDelayMillis());

        // center and align brick after layout pass
        Platform.runLater(() -> {
            // compute the pixel origin of the first visible cell so overlays align exactly
            try {
                Rectangle originRect = displayMatrix[VISIBLE_ROW_OFFSET][0];
                gridOriginX = originRect.getBoundsInParent().getMinX();
                gridOriginY = originRect.getBoundsInParent().getMinY();

                // position overlay panes to match the inner grid origin
                ghostPanel.setLayoutX(gridOriginX);
                ghostPanel.setLayoutY(gridOriginY);
                brickPanel.setLayoutX(gridOriginX);
                brickPanel.setLayoutY(gridOriginY);
            } catch (Exception ignored) {
                gridOriginX = 0; gridOriginY = 0;
            }

            centerGamePanel();
            refreshBrick(brick);
            gamePanel.requestFocus();
        });
    }

    private Paint getFillColor(int i) {
        switch (i) {
            case 0: return Color.TRANSPARENT;
            case 1: return Color.AQUA;
            case 2: return Color.BLUEVIOLET;
            case 3: return Color.DARKGREEN;
            case 4: return Color.YELLOW;
            case 5: return Color.RED;
            case 6: return Color.BEIGE;
            case 7: return Color.BURLYWOOD;
            default: return Color.WHITE;
        }
    }

    private void refreshBrick(ViewData brick) {
        if (!isPause.getValue()) {
            double cellW = getCellWidth();
            double cellH = getCellHeight();

                // Use computed grid origin to align overlays with the actual grid cell positions
                brickPanel.setLayoutX(gridOriginX + brick.getxPosition() * cellW);
                brickPanel.setLayoutY(gridOriginY + (brick.getyPosition() - VISIBLE_ROW_OFFSET) * cellH);

            for (int i = 0; i < brick.getBrickData().length; i++) {
                for (int j = 0; j < brick.getBrickData()[i].length; j++) {
                    setRectangleData(brick.getBrickData()[i][j], rectangles[i][j]);
                }
            }

            // at end of refreshBrick (after updating brickPanel and rectangles)
            refreshGhost(brick, eventListener.getBoardMatrix());

            
        }
    }


    public void refreshGameBackground(int[][] board) {
        // update visible rows only
        for (int i = VISIBLE_ROW_OFFSET; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                setRectangleData(board[i][j], displayMatrix[i][j]);
            }
        }
    }

    private void setRectangleData(int color, Rectangle rectangle) {
        rectangle.setFill(getFillColor(color));
        rectangle.setArcHeight(9);
        rectangle.setArcWidth(9);
    }

    private void moveDown(MoveEvent event) {
        if (isPause.getValue() == Boolean.FALSE) {
            DownData downData = eventListener.onDownEvent(event);
            if (downData.getClearRow() != null && downData.getClearRow().getLinesRemoved() > 0) {
                showLineClearPopup(downData.getClearRow());
            }
            refreshBrick(downData.getViewData());
        }
        gamePanel.requestFocus();
    }

    private int getScoreForLines(int lines) {
        switch (lines) {
            case 1: return 100;
            case 2: return 300;
            case 3: return 500;
            case 4: return 800;
            default: return 0;
        }
    }

    private String buildPopupText(int lines) {
        int score = getScoreForLines(lines);
        if (lines <= 1) {
            return "+" + score;
        }
        return "Combo! +" + score;
    }

    public void showLineClearPopup(ClearRow clearRow) {
        if (clearRow == null || clearRow.getLinesRemoved() == 0) {
            return;
        }

        int[][] boardMatrix = eventListener.getBoardMatrix();
        if (boardMatrix == null || boardMatrix.length == 0 || boardMatrix[0].length == 0) {
            return;
        }

        List<Integer> clearedRows = clearRow.getClearedRows();
        int anchorRow = clearedRows.stream()
                .mapToInt(Integer::intValue)
                .max()
                .orElse(boardMatrix.length - 1);
        anchorRow = Math.min(Math.max(anchorRow, VISIBLE_ROW_OFFSET), boardMatrix.length - 1);

        double cellW = getCellWidth();
        double cellH = getCellHeight();

        double layoutX = gridOriginX + (boardMatrix[0].length * cellW) / 2.0;
        double layoutY = gridOriginY + ((anchorRow - VISIBLE_ROW_OFFSET) + 0.5) * cellH;

        Label popup = new Label(buildPopupText(clearRow.getLinesRemoved()));
        popup.getStyleClass().add("line-clear-popup");
        if (clearRow.getLinesRemoved() > 1) {
            popup.getStyleClass().add("combo");
        }
        popup.setMouseTransparent(true);
        popup.setLayoutX(layoutX);
        popup.setLayoutY(layoutY);

        popup.layoutBoundsProperty().addListener((obs, oldBounds, newBounds) -> {
            popup.setTranslateX(-newBounds.getWidth() / 2.0);
            popup.setTranslateY(-newBounds.getHeight() / 2.0);
        });

        FadeTransition fade = new FadeTransition(Duration.millis(1400), popup);
        fade.setFromValue(1);
        fade.setToValue(0);

        TranslateTransition slide = new TranslateTransition(Duration.millis(1400), popup);
        slide.setByY(-28);

        ParallelTransition animation = new ParallelTransition(fade, slide);
        animation.setOnFinished(e -> groupNotification.getChildren().remove(popup));

        groupNotification.getChildren().add(popup);
        animation.play();
    }

    public void setEventListener(InputEventListener eventListener) {
        this.eventListener = eventListener;
    }

    public void bindScore(IntegerProperty scoreProperty) {
        // Bind the provided score property to the score label in the side panel (if present)
        if (scoreLabel != null) {
            scoreLabel.textProperty().bind(scoreProperty.asString("%d"));
            scoreLabel.setTextFill(Color.web("#ffd86b"));
            scoreLabel.setFont(Font.font("Arial", 20));
        } else {
            // fallback: if side label not present, attach to notifications area (safe fallback)
            javafx.scene.control.Label fallback = new javafx.scene.control.Label();
            fallback.textProperty().bind(scoreProperty.asString("Score: %d"));
            fallback.setTextFill(Color.WHITE);
            fallback.setFont(Font.font("Arial", 20));
            fallback.setLayoutX(300);
            fallback.setLayoutY(20);
            groupNotification.getChildren().add(fallback);
        }
    }

    public void bindLinesCleared(IntegerProperty linesClearedProperty) {
        // Bind the lines cleared property to the lines label in the side panel (if present)
        if (linesLabel != null) {
            linesLabel.textProperty().bind(linesClearedProperty.asString("%d"));
            linesLabel.setTextFill(Color.web("#a0d8ff"));
            linesLabel.setFont(Font.font("Arial", 14));
        }
    }

    public void showNextShape(ViewData nextBrick) {
        nextShapePanel.getChildren().clear();

        int[][] shape = nextBrick.getBrickData();
        for (int i = 0; i < shape.length; i++) {
            for (int j = 0; j < shape[i].length; j++) {
                if (shape[i][j] != 0) {
                    Rectangle rect = new Rectangle(BRICK_SIZE, BRICK_SIZE);
                    rect.setFill(getFillColor(shape[i][j]));
                    rect.setArcHeight(6);
                    rect.setArcWidth(6);
                    nextShapePanel.add(rect, j, i);
                }
            }
        }
    }

    public void showHoldShape(ViewData holdBrick) {
        holdShapePanel.getChildren().clear();

        if (holdBrick == null) return;

        int[][] shape = holdBrick.getBrickData();
        for (int i = 0; i < shape.length; i++) {
            for (int j = 0; j < shape[i].length; j++) {
                if (shape[i][j] != 0) {
                    Rectangle rect = new Rectangle(BRICK_SIZE, BRICK_SIZE);
                    rect.setFill(getFillColor(shape[i][j]));
                    rect.setArcHeight(6);
                    rect.setArcWidth(6);
                    holdShapePanel.add(rect, j, i);
                }
            }
        }
    }

    public void updateHoldDisplay(ViewData holdBrick) {
        // Use the inner GridPane so the preview is centered and aligned like the "next" preview
        holdShapePanel.getChildren().clear(); // clear previous
        if (holdBrick == null) return;

        int[][] shape = holdBrick.getBrickData();
        for (int i = 0; i < shape.length; i++) {
            for (int j = 0; j < shape[i].length; j++) {
                if (shape[i][j] != 0) {
                    Rectangle rect = new Rectangle(BRICK_SIZE, BRICK_SIZE);
                    rect.setFill(getFillColor(shape[i][j]));
                    rect.setArcHeight(6);
                    rect.setArcWidth(6);
                    holdShapePanel.add(rect, j, i);
                }
            }
        }
    }



    public void gameOver() {
        timeLine.stop();
        gameOverPanel.setVisible(true);
        isGameOver.setValue(Boolean.TRUE);

        SoundManager.stopBackgroundMusic();
        SoundManager.playGameOverSound();
        
        // Save the final score to high score manager
        HighScoreManager highScoreManager = new HighScoreManager();
        int finalScore = eventListener.getScore().scoreProperty().get();
        boolean isNewHighScore = highScoreManager.addScore(finalScore);

        if (isNewHighScore) {
            gameOverPanel.showNewHighScore();
        } else {
            gameOverPanel.hideNewHighScore();
        }
    }


    private void createTimeline(int delayMillis) {
        if (timeLine != null) {
            try { timeLine.stop(); } catch (Exception ignored) {}
        }
        timeLine = new Timeline(new KeyFrame(Duration.millis(delayMillis),
                ae -> moveDown(new MoveEvent(EventType.DOWN, EventSource.THREAD))
        ));
        timeLine.setCycleCount(Timeline.INDEFINITE);
        timeLine.play();
    }

    public void newGame(ActionEvent actionEvent) {
        timeLine.stop();
        gameOverPanel.setVisible(false);
        eventListener.createNewGame();
        gamePanel.requestFocus();
        // Restart background music when starting a new game
        SoundManager.playBackgroundMusic();
        timeLine.play();
        isPause.setValue(Boolean.FALSE);
        isGameOver.setValue(Boolean.FALSE);
        gameOverPanel.hideNewHighScore();
    }

    public void pauseGame(ActionEvent actionEvent) {
        updatePauseState(!isPause.getValue());
        gamePanel.requestFocus();
    }

    public void updatePauseState(boolean paused) {
        if (paused) {
            if (pauseMenuRoot == null) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/pauseMenu.fxml"));
                    pauseMenuRoot = loader.load();
                    pauseMenuController = loader.getController();
                    pauseMenuController.setGuiController(this);
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            }

            if (!groupNotification.getChildren().contains(pauseMenuRoot)) {
                groupNotification.getChildren().add(pauseMenuRoot);
            }

            timeLine.pause();
            isPause.set(true);

        } else {
            groupNotification.getChildren().remove(pauseMenuRoot);
            timeLine.play();
            isPause.set(false);
        }

        gamePanel.requestFocus();
    }

    public void stopGameLoop() {
        if (timeLine != null) {
            try {
                timeLine.stop();
            } catch (Exception ignored) {}
        }
    }

    public void changeGameDifficulty(GameDifficulty difficulty) {
        this.currentDifficulty = difficulty;
        createTimeline(difficulty.getDropDelayMillis());
        if (isPause.get()) {
            timeLine.pause(); // respect paused state when difficulty is changed mid-game
        }
    }


    private void centerGamePanel() {
        if (gamePanel.getScene() == null) return;

        double sceneWidth = gamePanel.getScene().getWidth();
        double sceneHeight = gamePanel.getScene().getHeight();

        double gamePanelWidth = gamePanel.getPrefWidth();
        double gamePanelHeight = gamePanel.getPrefHeight();

        if (gamePanelWidth <= 0) gamePanelWidth = gamePanel.getWidth();
        if (gamePanelHeight <= 0) gamePanelHeight = gamePanel.getHeight();

        double centeredX = Math.max(0, (sceneWidth - gamePanelWidth) / 2.0);
        double centeredY = Math.max(0, (sceneHeight - gamePanelHeight) / 2.0);

        // 👇 Only center the entire canvas, not individual parts
        gameCanvas.setLayoutX(centeredX);
        gameCanvas.setLayoutY(centeredY);
    }


    /**
     * Draw ghost of current falling brick.
     * ghost X = brick.getxPosition()
     * ghost Y = maximum drop before collision
     *
     * We add rectangles to ghostPanel at board grid coords (col = j + ghostX, row = i + ghostY - VISIBLE_ROW_OFFSET).
     */
    private void refreshGhost(ViewData brick, int[][] boardMatrix) {
        // defensive
        if (brick == null || boardMatrix == null) {
            ghostPanel.getChildren().clear();
            return;
        }

        int[][] shape = brick.getBrickData();
        if (shape == null) {
            ghostPanel.getChildren().clear();
            return;
        }

        // start from falling block's X and Y (ghost must follow X exactly)
        final int ghostX = brick.getxPosition();
        int ghostY = brick.getyPosition();

        // compute landing Y: increment while placing at (ghostX, ghostY+1) does NOT intersect
        while (!MatrixOperations.intersect(boardMatrix, shape, ghostX, ghostY + 1)) {
            ghostY++;
        }

        // clear previous ghost and draw new ghost directly on the same board grid
        ghostPanel.getChildren().clear();

        for (int i = 0; i < shape.length; i++) {
            for (int j = 0; j < shape[i].length; j++) {
                if (shape[i][j] != 0) {
                    int boardCol = j + ghostX;
                    int boardRow = i + ghostY - VISIBLE_ROW_OFFSET;
                    
                    // Only draw the ghost block if it's within visible bounds
                    if (boardCol >= 0 && boardCol < boardMatrix[0].length && 
                        boardRow >= 0 && boardRow < boardMatrix.length - VISIBLE_ROW_OFFSET) {
                        Rectangle rect = new Rectangle(BRICK_SIZE, BRICK_SIZE);
                        rect.setFill(Color.web("#FFFFFF", 0.22)); // translucent shadow
                        rect.setArcHeight(6);
                        rect.setArcWidth(6);
                        ghostPanel.add(rect, boardCol, boardRow);
                    }
                }
            }
        }
    }






}
