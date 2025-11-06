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
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.util.Duration;
import javafx.beans.value.ObservableValue;

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
    @FXML private GridPane nextShapePanel;
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
            }
        });
    }

    /**
     * Initialize the visible grid and the brick rectangles.
     * boardMatrix: full logical board (including hidden rows)
     * brick: current ViewData (brick shape + position)
     */
    public void initGameView(int[][] boardMatrix, ViewData brick) {
        // set the preferred size of gamePanel (so centering works)
        double panelW = getGamePanelPixelWidth(boardMatrix);
        double panelH = getGamePanelPixelHeight(boardMatrix);
        gamePanel.setPrefWidth(panelW);
        gamePanel.setPrefHeight(panelH);

        // Center the gamePanel in the window
        double windowWidth = 600;
        double windowHeight = 800;
        double gameWidth = boardMatrix[0].length * BRICK_SIZE;
        double gameHeight = (boardMatrix.length - VISIBLE_ROW_OFFSET) * BRICK_SIZE;

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

        // start timeline
        timeLine = new Timeline(new KeyFrame(Duration.millis(400),
                ae -> moveDown(new MoveEvent(EventType.DOWN, EventSource.THREAD))
        ));
        timeLine.setCycleCount(Timeline.INDEFINITE);
        timeLine.play();

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


    public void gameOver() {
        timeLine.stop();
        gameOverPanel.setVisible(true);
        isGameOver.setValue(Boolean.TRUE);
    }

    public void newGame(ActionEvent actionEvent) {
        timeLine.stop();
        gameOverPanel.setVisible(false);
        eventListener.createNewGame();
        gamePanel.requestFocus();
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
            Rectangle overlay = new Rectangle(gamePanel.getWidth(), gamePanel.getHeight());
            overlay.setFill(Color.BLACK);
            overlay.setOpacity(0.7);

            javafx.scene.control.Label pauseLabel = new javafx.scene.control.Label("PAUSED");
            pauseLabel.setTextFill(Color.WHITE);
            pauseLabel.setFont(Font.font("Arial", 40));
            pauseLabel.setLayoutX(gamePanel.getLayoutX() + 80);
            pauseLabel.setLayoutY(gamePanel.getLayoutY() + 200);

            overlay.setId("pauseOverlay");
            pauseLabel.setId("pauseLabel");

            groupNotification.getChildren().addAll(overlay, pauseLabel);
            timeLine.pause();
        } else {
            groupNotification.getChildren().removeIf(node ->
                    node.getId() != null && (node.getId().equals("pauseOverlay") || node.getId().equals("pauseLabel"))
            );
            timeLine.play();
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

}
