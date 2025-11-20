package com.comp2042;

import javafx.animation.KeyFrame;
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


    private Rectangle[][] displayMatrix;
    private Rectangle[][] rectangles;
    private InputEventListener eventListener;
    private Timeline timeLine;

    private final BooleanProperty isPause = new SimpleBooleanProperty();
    private final BooleanProperty isGameOver = new SimpleBooleanProperty();

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
                if (isPause.getValue() == Boolean.FALSE && isGameOver.getValue() == Boolean.FALSE) {
                    if (keyEvent.getCode() == KeyCode.LEFT || keyEvent.getCode() == KeyCode.A) {
                        refreshBrick(eventListener.onLeftEvent(new MoveEvent(EventType.LEFT, EventSource.USER)));
                        keyEvent.consume();
                    } else if (keyEvent.getCode() == KeyCode.RIGHT || keyEvent.getCode() == KeyCode.D) {
                        refreshBrick(eventListener.onRightEvent(new MoveEvent(EventType.RIGHT, EventSource.USER)));
                        keyEvent.consume();
                    } else if (keyEvent.getCode() == KeyCode.UP || keyEvent.getCode() == KeyCode.W) {
                        refreshBrick(eventListener.onRotateEvent(new MoveEvent(EventType.ROTATE, EventSource.USER)));
                        keyEvent.consume();
                    } else if (keyEvent.getCode() == KeyCode.DOWN || keyEvent.getCode() == KeyCode.S) {
                        moveDown(new MoveEvent(EventType.DOWN, EventSource.USER));
                        keyEvent.consume();
                    }
                    else if (keyEvent.getCode() == KeyCode.SPACE) {
                        eventListener.hardDrop();
                        keyEvent.consume();
                    }
                    else if (keyEvent.getCode() == KeyCode.H) {
                        eventListener.holdCurrentBrick();
                        keyEvent.consume();
                    }
                }
                if (keyEvent.getCode() == KeyCode.N) {
                    newGame(null);
                } else if (keyEvent.getCode() == KeyCode.P || keyEvent.getCode() == KeyCode.ESCAPE) {
                    pauseGame(null);
                    keyEvent.consume();
                }
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
     * Initialize the visible grid and the brick rectangles.
     * boardMatrix: full logical board (including hidden rows)
     * brick: current ViewData (brick shape + position)
     */
    public void initGameView(int[][] boardMatrix, ViewData brick, GameDifficulty difficulty) {
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

            brickPanel.setLayoutX(
                    gamePanel.getLayoutX() + brick.getxPosition() * cellW
            );

            brickPanel.setLayoutY(
                    gamePanel.getLayoutY() + (brick.getyPosition() - VISIBLE_ROW_OFFSET) * cellH
            );

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
                int scoreToShow = getScoreForLines(downData.getClearRow().getLinesRemoved());
                boolean isCombo = isPreviousLineCleared();
                String scoreText = "+" + scoreToShow;
                if (isCombo) {
                    scoreText += " x2";
                    scoreToShow *= 2;
                }
                NotificationPanel notificationPanel = new NotificationPanel(scoreText);
                groupNotification.getChildren().add(notificationPanel);
                notificationPanel.showScore(groupNotification.getChildren());
            }
            refreshBrick(downData.getViewData());
        }
        gamePanel.requestFocus();
    }

    private boolean isPreviousLineCleared() {
        return eventListener.getScore().comboProperty().get() > 1;
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

    public void setEventListener(InputEventListener eventListener) {
        this.eventListener = eventListener;
    }

    public void bindScore(IntegerProperty scoreProperty) {
        javafx.scene.control.Label scoreLabel = new javafx.scene.control.Label();
        scoreLabel.textProperty().bind(scoreProperty.asString("Score: %d"));
        scoreLabel.setTextFill(Color.WHITE);
        scoreLabel.setFont(Font.font("Arial", 20));
        scoreLabel.setLayoutX(300);
        scoreLabel.setLayoutY(20);
        groupNotification.getChildren().add(scoreLabel);
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
        holdBlockBox.getChildren().clear(); // clear previous
        if (holdBrick == null) return;

        int[][] shape = holdBrick.getBrickData();
        for (int i = 0; i < shape.length; i++) {
            for (int j = 0; j < shape[i].length; j++) {
                if (shape[i][j] != 0) {
                    Rectangle rect = new Rectangle(BRICK_SIZE, BRICK_SIZE);
                    rect.setFill(getFillColor(shape[i][j]));
                    rect.setArcHeight(6);
                    rect.setArcWidth(6);
                    holdBlockBox.getChildren().add(rect);
                    StackPane.setAlignment(rect, Pos.TOP_LEFT);
                    rect.setTranslateX(j * BRICK_SIZE);
                    rect.setTranslateY(i * BRICK_SIZE);
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
        
        // Optionally show notification if it's a new high score
        if (isNewHighScore) {
            Label highScoreLabel = new Label("NEW HIGH SCORE!");
            highScoreLabel.setTextFill(Color.GOLD);
            highScoreLabel.setFont(Font.font("Arial", 24));
            highScoreLabel.setStyle("-fx-font-weight: bold;");
            groupNotification.getChildren().add(highScoreLabel);
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
    }

    public void pauseGame(ActionEvent actionEvent) {
        updatePauseState(!isPause.getValue());
        gamePanel.requestFocus();
    }

    public void updatePauseState(boolean isPaused) {
        if (isPaused) {
                Rectangle overlay = new Rectangle();
                overlay.widthProperty().bind(gamePanel.widthProperty());
                overlay.heightProperty().bind(gamePanel.heightProperty());
            overlay.setFill(Color.BLACK);
            overlay.setOpacity(0.65);
            overlay.setId("pauseOverlay");

            // Pause menu box
            VBox pauseBox = new VBox(14);
            pauseBox.setAlignment(Pos.CENTER);
            pauseBox.getStyleClass().add("pauseBox");
            pauseBox.setId("pauseMenuBox");

                // position overlay and pauseBox relative to the gamePanel inside groupNotification
                overlay.layoutXProperty().bind(gamePanel.layoutXProperty().subtract(groupNotification.layoutXProperty()));
                overlay.layoutYProperty().bind(gamePanel.layoutYProperty().subtract(groupNotification.layoutYProperty()));

                pauseBox.layoutXProperty().bind(gamePanel.layoutXProperty().subtract(groupNotification.layoutXProperty()).add(
                    gamePanel.widthProperty().subtract(pauseBox.widthProperty()).divide(2)));
                pauseBox.layoutYProperty().bind(gamePanel.layoutYProperty().subtract(groupNotification.layoutYProperty()).add(
                    gamePanel.heightProperty().subtract(pauseBox.heightProperty()).divide(2)));

            Label pauseLabel = new Label("PAUSED");
            pauseLabel.setTextFill(Color.WHITE);
            pauseLabel.setFont(Font.font("Arial", 36));

            Button resumeBtn = new Button("Resume");
            resumeBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 16px; -fx-min-width: 160px;");
            resumeBtn.setOnAction(e -> updatePauseState(false));

            Button changeDiffBtn = new Button("Change Difficulty");
            changeDiffBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size: 14px; -fx-min-width: 180px;");
            changeDiffBtn.setOnAction(e -> {
                ChoiceDialog<GameDifficulty> dialog = new ChoiceDialog<>(GameDifficulty.MEDIUM, GameDifficulty.EASY, GameDifficulty.MEDIUM, GameDifficulty.HARD);
                dialog.setTitle("Change Difficulty");
                dialog.setHeaderText("Select a new difficulty");
                dialog.setContentText("Difficulty:");
                dialog.initOwner(gamePanel.getScene().getWindow());
                dialog.showAndWait().ifPresent(selected -> {
                    // recreate timeline with new delay and resume
                    createTimeline(selected.getDropDelayMillis());
                    updatePauseState(false);
                });
            });

            Button returnBtn = new Button("Return To Main Menu");
            returnBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-size: 14px; -fx-min-width: 180px;");
            returnBtn.setOnAction(e -> {
                try {
                    if (timeLine != null) timeLine.stop();
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/mainMenu.fxml"));
                    Parent root = loader.load();
                    Stage stage = (Stage) gamePanel.getScene().getWindow();
                    Scene scene = new Scene(root, 800, 600);
                    stage.setScene(scene);
                    stage.show();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            });

            pauseBox.getChildren().addAll(pauseLabel, resumeBtn, changeDiffBtn, returnBtn);

            groupNotification.getChildren().addAll(overlay, pauseBox);
            timeLine.pause();
        } else {
            groupNotification.getChildren().removeIf(node ->
                    node.getId() != null && (node.getId().equals("pauseOverlay") || node.getId().equals("pauseMenuBox"))
            );
            if (timeLine != null) timeLine.play();
        }
        isPause.setValue(isPaused);
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