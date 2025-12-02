package com.example.game.component;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import com.example.Router;
import com.example.game.blocks.Block;
import com.example.game.component.MenuOverlay.MenuCallback;
import com.example.network.GameMessage;
import com.example.network.MessageListener;
import com.example.network.MessageType;
import com.example.network.NetworkManager;
import com.example.settings.GameSettings;
import com.example.utils.Logger;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

/**
 * 온라인 P2P 대전 모드 보드 VersusBoard의 구조를 그대로 가져오되, 네트워크 동기화를 추가
 */
public class OnlineVersusBoard implements MessageListener {

    private VersusGameModeDialog.VersusMode gameMode;
    private final GameSettings gameSettings;
    private final NetworkManager networkManager;
    private final boolean isServer;
    private final String localPlayerId;
    private String remotePlayerId;

    // UI 컴포넌트
    private StackPane mainContainer;
    private BorderPane root;
    private HBox gameArea;
    private MenuOverlay menuOverlay;
    private VersusGameOverScene gameOverScene;
    private Stage stage;

    // 플레이어 보드 (로컬 = 나, 원격 = 상대방)
    private PlayerBoard localBoard;
    private ScorePanel localScorePanel;
    private AttackQueueDisplay localAttackDisplay;

    private PlayerBoard remoteBoard;
    private ScorePanel remoteScorePanel;
    private AttackQueueDisplay remoteAttackDisplay;

    // 게임 상태
    private boolean gameActive = false;
    private AnimationTimer gameLoop;
    private long lastUpdateLocal = 0;
    private long lastUpdateRemote = 0;
    private long lastBoardStateSent = 0;
    private static final long BOARD_STATE_SEND_INTERVAL = 100_000_000; // 100ms = 10 updates/sec
    private boolean isPaused = false;

    // 시간제한 모드용
    private long gameStartTime;
    private long timeLimitMillis = 180000; // 3분
    private Label timerLabel;

    // 공격 시스템용 (VersusBoard와 동일)
    private Queue<AttackData> localAttackQueue = new LinkedList<>();
    private Queue<AttackData> remoteAttackQueue = new LinkedList<>();

    // 네트워크 관련
    private Label latencyLabel;
    private Label modeLabel;
    private javafx.scene.control.Button readyButton;
    private boolean localReady = false;
    private boolean remoteReady = false;

    // 블록 동기화용 Random seed
    private Long player1Seed = null;
    private Long player2Seed = null;

    // 공격 데이터 클래스
    private static class AttackData {

        List<String[]> lines;
        int count;

        AttackData(List<String[]> lines, int count) {
            this.lines = lines;
            this.count = count;
        }
    }

    public OnlineVersusBoard(Stage stage, VersusGameModeDialog.VersusMode mode,
            NetworkManager networkManager, boolean isServer) {
        this.stage = stage;
        this.gameMode = mode;
        this.gameSettings = GameSettings.getInstance();
        this.networkManager = networkManager;
        this.isServer = isServer;
        this.localPlayerId = isServer ? "Server" : "Client";
        this.menuOverlay = new MenuOverlay();

        networkManager.setListener(this);

        initializeUI();
        this.gameOverScene = new VersusGameOverScene(stage, mainContainer, this::restartGame, this::goToMainMenuWithDisconnect);
        setupKeyHandling();
    }

    private void initializeUI() {
        mainContainer = new StackPane();
        root = new BorderPane();
        root.getStyleClass().add("versus-root");
        root.setPadding(new Insets(10));

        VBox topInfo = createTopInfo();

        gameArea = new HBox(10);
        gameArea.setAlignment(Pos.CENTER);
        gameArea.setPadding(new Insets(10));

        boolean itemMode = (gameMode == VersusGameModeDialog.VersusMode.ITEM);

        BorderPane localContainer = createPlayerBoard(true, itemMode);
        BorderPane remoteContainer = createPlayerBoard(false, itemMode);

        gameArea.getChildren().addAll(localContainer, topInfo, remoteContainer);
        localContainer.prefWidthProperty().bind(gameArea.widthProperty().multiply(0.4));
        topInfo.prefWidthProperty().bind(gameArea.widthProperty().multiply(0.2));
        remoteContainer.prefWidthProperty().bind(gameArea.widthProperty().multiply(0.4));

        root.setCenter(gameArea);
        mainContainer.getChildren().addAll(root, menuOverlay.getOverlay());
    }

    private VBox createTopInfo() {
        VBox topInfo = new VBox(5);
        topInfo.setAlignment(Pos.CENTER);
        topInfo.setPadding(new Insets(5));
        topInfo.getStyleClass().add("versus-top-info");
        topInfo.setPrefWidth(250);

        String modeDisplay = gameMode != null ? gameMode.getDisplayName() : "대기 중...";

        String gamemodetext = "";
        if (gameMode == VersusGameModeDialog.VersusMode.NORMAL) {
            gamemodetext = "Normal";
        } else if (gameMode == VersusGameModeDialog.VersusMode.TIME_LIMIT) {
            gamemodetext = "Time Limit";
        } else if (gameMode == VersusGameModeDialog.VersusMode.ITEM) {
            gamemodetext = "Item";
        }

        modeLabel = new Label(gamemodetext);

        if (gameMode == VersusGameModeDialog.VersusMode.NORMAL) {
            modeLabel.getStyleClass().add("normal-mode-label");
        } else if (gameMode == VersusGameModeDialog.VersusMode.TIME_LIMIT) {
            modeLabel.getStyleClass().add("timelimit-mode-label");
        } else if (gameMode == VersusGameModeDialog.VersusMode.ITEM) {
            modeLabel.getStyleClass().add("item-mode-versus-label");
        }

        // modeLabel = new Label("⚔ 온라인 대전: " + modeDisplay + " ⚔");
        // modeLabel.setFont(Font.font("Arial", FontWeight.BOLD, 32));
        // modeLabel.setStyle("-fx-text-fill: white;" +
        //                    "-fx-effect: dropshadow(gaussian, rgba(0,212,255,0.5), 10, 0, 0, 0);");
        topInfo.getChildren().add(modeLabel);

        latencyLabel = new Label("📡 연결 중...");
        latencyLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        latencyLabel.setStyle("-fx-text-fill: #ffeb3b;");
        topInfo.getChildren().add(latencyLabel);

        if (gameMode == VersusGameModeDialog.VersusMode.TIME_LIMIT) {
            timerLabel = new Label("⏱ 03:00");
            timerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 35));
            timerLabel.setStyle("-fx-text-fill: #ffeb3b;"
                    + "-fx-effect: dropshadow(gaussian, rgba(255,235,59,0.6), 10, 0, 0, 0);");
            topInfo.getChildren().add(timerLabel);
        }

        readyButton = new javafx.scene.control.Button("준비");
        readyButton.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        readyButton.setPrefWidth(150);
        readyButton.setPrefHeight(50);
        readyButton.setStyle("-fx-background-color: #00d4ff; -fx-text-fill: white; -fx-background-radius: 10;");
        readyButton.setDisable(true);
        readyButton.setOnAction(e -> onReadyButtonClick());
        topInfo.getChildren().add(readyButton);

        Label infoLabel = new Label("2줄 이상 삭제하면 상대방을 공격!");
        infoLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        infoLabel.setStyle("-fx-text-fill: #bbbbbb;");
        //topInfo.getChildren().add(infoLabel);

        return topInfo;
    }

    private BorderPane createPlayerBoard(boolean isLocal, boolean itemMode) {
        BorderPane playerContainer = new BorderPane();
        playerContainer.getStyleClass().add("versus-player-container");

        VBox playerHeader = new VBox(5);
        playerHeader.setAlignment(Pos.CENTER);
        playerHeader.setPadding(new Insets(10));
        playerHeader.getStyleClass().add("versus-player-header");

        String playerName;
        if (isLocal) {
            playerName = isServer ? "Player 1 (Host)" : "Player 2 (Client)";
        } else {
            playerName = isServer ? "Player 2 (Client)" : "Player 1 (Host)";
        }
        Label playerLabel = new Label(playerName);
        playerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        playerLabel.setStyle("-fx-text-fill: " + (isLocal ? "#00d4ff" : "#ff6b6b") + ";");

        String controls = isLocal ? "화살표 키 + Enter" : "자동 동기화";
        Label controlsLabel = new Label(controls);
        controlsLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 15));
        controlsLabel.setStyle("-fx-text-fill: #bbbbbb;");

        playerHeader.getChildren().addAll(playerLabel, controlsLabel);
        playerContainer.setTop(playerHeader);

        if (isLocal) {
            localBoard = new PlayerBoard(1, this::onLocalLinesCleared, itemMode);
            localBoard.setAutoDropCallback(this::onLocalAutoDrop);
            localScorePanel = new ScorePanel();
            localBoard.scorePanel = localScorePanel;
            localAttackDisplay = new AttackQueueDisplay("You");

            VBox rightPanel = new VBox(10);
            rightPanel.setAlignment(Pos.TOP_CENTER);
            rightPanel.getChildren().addAll(
                    localScorePanel.getPanel(),
                    localAttackDisplay.getContainer()
            );

            playerContainer.setCenter(localBoard.getCanvas());
            playerContainer.setRight(rightPanel);
            rightPanel.getStyleClass().add("side-panel");
            BorderPane.setMargin(rightPanel, new Insets(0, 0, 0, 10));

        } else {
            remoteBoard = new PlayerBoard(2, this::onRemoteLinesCleared, itemMode);
            remoteScorePanel = new ScorePanel();
            remoteBoard.scorePanel = remoteScorePanel;
            remoteAttackDisplay = new AttackQueueDisplay("Opponent");

            VBox leftPanel = new VBox(10);
            leftPanel.setAlignment(Pos.TOP_CENTER);
            leftPanel.getChildren().addAll(
                    remoteScorePanel.getPanel(),
                    remoteAttackDisplay.getContainer()
            );

            playerContainer.setLeft(leftPanel);
            playerContainer.setCenter(remoteBoard.getCanvas());
            leftPanel.getStyleClass().add("side-panel");
            BorderPane.setMargin(leftPanel, new Insets(0, 10, 0, 0));
        }

        return playerContainer;
    }

    private void setupKeyHandling() {
        mainContainer.setFocusTraversable(true);
        mainContainer.setOnKeyPressed(event -> {
            if (isPaused) {
                if (event.getCode() == KeyCode.ESCAPE) {
                    togglePause();
                }
                return;
            }

            if (!gameActive) {
                return;
            }

            KeyCode code = event.getCode();

            switch (code) {
                case LEFT:
                    localBoard.onMoveLeft();
                    sendBoardState(); // 즉시 상태 전송
                    break;
                case RIGHT:
                    localBoard.onMoveRight();
                    sendBoardState(); // 즉시 상태 전송
                    break;
                case DOWN:
                    localBoard.onMoveDown();
                    sendBoardState(); // 즉시 상태 전송
                    break;
                case UP:
                    localBoard.onRotate();
                    sendBoardState(); // 즉시 상태 전송
                    break;
                case ENTER:
                case SPACE:
                    localBoard.onHardDrop();
                    sendBoardState(); // 즉시 상태 전송
                    break;
                case ESCAPE:
                    togglePause();
                    break;
                default:
                    break;
            }

            event.consume();
        });

        mainContainer.requestFocus();
    }

    private void togglePause() {
        if (isPaused) {
            resumeGame();
        } else {
            pauseGame();
        }
    }

    private void pauseGame() {
        isPaused = true;
        if (gameLoop != null) {
            gameLoop.stop();
        }

        menuOverlay.showPauseMenu(new MenuCallback() {
            @Override
            public void onResume() {
                resumeGame();
            }

            @Override
            public void onRestart() {
                // 온라인에서는 재시작 불가
            }

            @Override
            public void onSettings() {
                // 설정 제한적
            }

            @Override
            public void onMainMenu() {
                disconnect();
                goToMainMenu();
            }

            @Override
            public void onExit() {
                disconnect();
                exitGame();
            }
        });
    }

    private void resumeGame() {
        isPaused = false;
        menuOverlay.hide();
        if (gameActive) {
            startGameLoop();
        }
        mainContainer.requestFocus();
    }

    private void startGame() {
        gameStartTime = System.currentTimeMillis();
        gameActive = true;

        if (player1Seed != null && player2Seed != null) {
            Long mySeed = isServer ? player1Seed : player2Seed;

            localBoard.getGameLogic().setRandomSeed(mySeed);

            Logger.info(">>> Seed applied - Local: " + mySeed);
        }

        // Remote Board는 seed 없이 네트워크 상태만 표시
        startGameLoop();

        Platform.runLater(() -> {
            mainContainer.requestFocus();
            Logger.info(">>> Online game started!");
        });
    }

    private void startGameLoop() {
        if (gameLoop != null) {
            gameLoop.stop();
        }

        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (!gameActive || isPaused) {
                    return;
                }

                if (gameMode == VersusGameModeDialog.VersusMode.TIME_LIMIT) {
                    updateTimer();
                }

                long elapsedNanos1 = now - lastUpdateLocal;
                long dropInterval1 = localBoard.getDropInterval();

                if (elapsedNanos1 >= dropInterval1) {
                    localBoard.update();
                    sendBoardState();  // 보드 상태 전송
                    lastUpdateLocal = now;
                }

                if (localBoard.isAnimationActive()) {
                    localBoard.update();
                }

                // 원격 보드는 네트워크로 받은 상태만 표시 (자동 업데이트 없음)
                if (remoteBoard.isAnimationActive()) {
                    remoteBoard.update();
                }

                processAttacks();
                updateAttackDisplays();
                checkGameEnd();
            }
        };

        lastUpdateLocal = System.nanoTime();
        lastUpdateRemote = System.nanoTime();
        gameLoop.start();
    }

    private void updateTimer() {
        if (timerLabel == null) {
            return;
        }

        long elapsed = System.currentTimeMillis() - gameStartTime;
        long remaining = Math.max(0, timeLimitMillis - elapsed);

        if (remaining == 0) {
            endGameByTime();
            return;
        }

        long minutes = remaining / 60000;
        long seconds = (remaining % 60000) / 1000;
        timerLabel.setText(String.format("⏱ %02d:%02d", minutes, seconds));

        if (remaining < 30000) {
            timerLabel.setStyle("-fx-text-fill: #ff0000; -fx-font-weight: bold;");
        } else {
            timerLabel.setStyle("-fx-text-fill: #00ff00;");
        }
    }

    private void processAttacks() {
        if (!localAttackQueue.isEmpty()) {
            AttackData attack = localAttackQueue.poll();
            localBoard.receiveAttackLines(attack.lines);
        }

        if (!remoteAttackQueue.isEmpty()) {
            AttackData attack = remoteAttackQueue.poll();
            remoteBoard.receiveAttackLines(attack.lines);
        }
    }

    private void updateAttackDisplays() {
        int localPending = localBoard.getPendingAttackCount();
        int remotePending = remoteBoard.getPendingAttackCount();

        if (localAttackDisplay != null) {
            localAttackDisplay.syncWithActualQueue(localPending);
        }
        if (remoteAttackDisplay != null) {
            remoteAttackDisplay.syncWithActualQueue(remotePending);
        }
    }

    private void checkGameEnd() {
        if (localBoard.isGameOver()) {
            sendGameOver(localBoard.getScore());
            endGame(false);
            return;
        }

        if (remoteBoard.isGameOver()) {
            endGame(true);
            return;
        }
    }

    private void endGameByTime() {
        gameActive = false;
        if (gameLoop != null) {
            gameLoop.stop();
        }

        int localScore = localBoard.getScore();
        int remoteScore = remoteBoard.getScore();

        sendGameOver(localScore);

        VersusGameOverScene.GameResult result;
        if (localScore > remoteScore) {
            result = isServer ? VersusGameOverScene.GameResult.PLAYER1_WIN
                    : VersusGameOverScene.GameResult.PLAYER2_WIN;
        } else if (remoteScore > localScore) {
            result = isServer ? VersusGameOverScene.GameResult.PLAYER2_WIN
                    : VersusGameOverScene.GameResult.PLAYER1_WIN;
        } else {
            result = VersusGameOverScene.GameResult.DRAW;
        }

        Platform.runLater(() -> {
            if (isServer) {
                gameOverScene.show(result, localScore, remoteScore);
            } else {
                gameOverScene.show(result, remoteScore, localScore);
            }
        });
    }

    private void endGame(boolean iWon) {
        gameActive = false;
        if (gameLoop != null) {
            gameLoop.stop();
        }

        int localScore = localBoard.getScore();
        int remoteScore = remoteBoard.getScore();

        VersusGameOverScene.GameResult result;
        if (iWon) {
            result = isServer ? VersusGameOverScene.GameResult.PLAYER1_WIN
                    : VersusGameOverScene.GameResult.PLAYER2_WIN;
        } else {
            result = isServer ? VersusGameOverScene.GameResult.PLAYER2_WIN
                    : VersusGameOverScene.GameResult.PLAYER1_WIN;
        }

        Platform.runLater(() -> {
            if (isServer) {
                gameOverScene.show(result, localScore, remoteScore);
            } else {
                gameOverScene.show(result, remoteScore, localScore);
            }
        });
    }

    private void sendBoardState() {
        GameMessage message = new GameMessage(MessageType.BOARD_UPDATE, localPlayerId);

        // 현재 블록 정보
        Block currentBlock = localBoard.getGameLogic().getCurrentBlock();
        if (currentBlock != null) {
            message.put("blockType", currentBlock.getClass().getSimpleName());
            message.put("blockX", localBoard.getGameLogic().getCurrentX());
            message.put("blockY", localBoard.getGameLogic().getCurrentY());
            // 블록의 현재 shape 배열을 직렬화해서 전송
            message.put("blockShape", serializeBlockShape(currentBlock));
        }

        // 다음 블록 정보 추가
        Block nextBlock = localBoard.getGameLogic().getNextBlock();
        if (nextBlock != null) {
            message.put("nextBlockType", nextBlock.getClass().getSimpleName());
            message.put("nextBlockShape", serializeBlockShape(nextBlock));
        }

        // 보드 상태 (착지된 블록들)
        String[][] boardTypes = localBoard.getGameLogic().getBlockTypes();
        message.put("boardData", serializeBoardData(boardTypes));

        // 점수
        message.put("score", localBoard.getScore());

        networkManager.sendMessage(message);
    }

    private String serializeBlockShape(Block block) {
        StringBuilder sb = new StringBuilder();
        int height = block.height();
        int width = block.width();
        sb.append(height).append("x").append(width).append(":");
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                sb.append(block.getShape(x, y));
            }
            if (y < height - 1) {
                sb.append(",");
            }
        }
        return sb.toString();
    }

    private String serializeBoardData(String[][] board) {
        StringBuilder sb = new StringBuilder();
        for (int y = 0; y < board.length; y++) {
            for (int x = 0; x < board[y].length; x++) {
                String blockType = board[y][x];
                if (blockType == null) {
                    sb.append("0");
                } else {
                    sb.append(blockType);
                }
                if (x < board[y].length - 1) {
                    sb.append(",");
                }
            }
            if (y < board.length - 1) {
                sb.append(";");
            }
        }
        return sb.toString();
    }

    private void updateRemoteBoard(GameMessage message) {
        // 블록 정보 복원
        String blockType = message.getString("blockType");
        Integer blockX = (Integer) message.get("blockX");
        Integer blockY = (Integer) message.get("blockY");
        String blockShape = message.getString("blockShape");

        // 보드 데이터 복원
        String boardData = message.getString("boardData");

        // 점수 업데이트
        Integer score = (Integer) message.get("score");
        if (score != null) {
            remoteScorePanel.setScore(score);
        }

        // 다음 블록 정보 복원
        String nextBlockType = message.getString("nextBlockType");
        String nextBlockShape = message.getString("nextBlockShape");
        if (nextBlockType != null && nextBlockShape != null) {
            int[][] nextShape = deserializeBlockShape(nextBlockShape);
            remoteBoard.getGameLogic().setNextBlockFromNetwork(nextBlockType, nextShape);
        }

        // Remote Board의 GameLogic에 상태 적용
        if (blockType != null && blockX != null && blockY != null && blockShape != null) {
            int[][] shape = deserializeBlockShape(blockShape);
            remoteBoard.getGameLogic().setCurrentBlockFromNetwork(blockType, blockX, blockY, shape);
        }

        if (boardData != null) {
            String[][] board = deserializeBoardData(boardData);
            remoteBoard.getGameLogic().setBoardFromNetwork(board);
        }

        // Canvas를 완전히 클리어하여 잔상 제거
        javafx.scene.canvas.GraphicsContext gc = remoteBoard.getCanvas().getGraphicsContext2D();
        gc.clearRect(0, 0, remoteBoard.getCanvas().getWidth(), remoteBoard.getCanvas().getHeight());

        // 화면 갱신
        remoteBoard.drawBoard();
    }

    private int[][] deserializeBlockShape(String data) {
        // Format: "height x width : row1,row2,..."
        String[] parts = data.split(":");
        String[] dimensions = parts[0].split("x");
        int height = Integer.parseInt(dimensions[0]);
        int width = Integer.parseInt(dimensions[1]);

        int[][] shape = new int[height][width];
        String[] rows = parts[1].split(",");

        for (int y = 0; y < height; y++) {
            String row = rows[y];
            for (int x = 0; x < width; x++) {
                shape[y][x] = Character.getNumericValue(row.charAt(x));
            }
        }

        return shape;
    }

    private String[][] deserializeBoardData(String data) {
        String[] rows = data.split(";");
        String[][] board = new String[rows.length][10];

        for (int y = 0; y < rows.length; y++) {
            String[] cells = rows[y].split(",");
            for (int x = 0; x < Math.min(cells.length, 10); x++) {
                String cell = cells[x];
                board[y][x] = cell.equals("0") ? null : cell;
            }
        }

        return board;
    }

    private void onLocalAutoDrop() {
        // 자동 낙하는 sendBoardState()로 처리되므로 여기서는 별도 전송 불필요
    }

    private void onLocalLinesCleared(int playerNumber, int linesCleared, List<String[]> clearedLines) {
        if (linesCleared < 2) {
            return;
        }

        Logger.info(">>> Local player cleared " + linesCleared + " lines");

        remoteAttackQueue.offer(new AttackData(clearedLines, linesCleared));
        sendAttack(linesCleared, clearedLines);
    }

    private void onRemoteLinesCleared(int playerNumber, int linesCleared, List<String[]> clearedLines) {
        // 원격 플레이어의 줄 삭제는 네트워크 메시지로만 처리
    }

    public void restartGame() {
        // 온라인 대전에서는 재시작 시 연결을 유지하고 준비 단계로 돌아감
        gameOverScene.hide();

        // 게임 상태 초기화
        gameActive = false;
        localReady = false;
        remoteReady = false;

        // 보드 초기화
        localBoard.restart();
        remoteBoard.restart();
        localScorePanel.resetScore();
        remoteScorePanel.resetScore();

        localAttackQueue.clear();
        remoteAttackQueue.clear();

        if (localAttackDisplay != null) {
            localAttackDisplay.clear();
        }
        if (remoteAttackDisplay != null) {
            remoteAttackDisplay.clear();
        }

        // 준비 버튼 재활성화
        Platform.runLater(() -> {
            readyButton.setText("준비");
            readyButton.setStyle("-fx-background-color: #00d4ff; -fx-text-fill: white; -fx-background-radius: 10;");
            readyButton.setDisable(false);

            // 서버라면 새로운 시드 생성 및 전송
            if (isServer) {
                player1Seed = System.nanoTime();
                try {
                    Thread.sleep(1);
                } catch (Exception e) {
                }
                player2Seed = System.nanoTime();
                sendGameStart();
            }

            mainContainer.requestFocus();
        });
    }

    private void goToMainMenu() {
        Platform.runLater(() -> {
            Router router = new Router(stage);
            router.showStartMenu();
        });
    }

    private void goToMainMenuWithDisconnect() {
        disconnect();
        goToMainMenu();
    }

    private void exitGame() {
        disconnect();
        Platform.exit();
    }

    private void sendGameAction(MessageType type, String key, String value) {
        GameMessage message = new GameMessage(type, localPlayerId);
        if (key != null && value != null) {
            message.put(key, value);
        }
        networkManager.sendMessage(message);
    }

    private void sendAttack(int linesCleared, List<String[]> clearedLines) {
        GameMessage message = new GameMessage(MessageType.ATTACK, localPlayerId);
        message.put("linesCleared", linesCleared);
        message.put("attackData", serializeAttackLines(clearedLines));
        networkManager.sendMessage(message);
    }

    private void sendGameOver(int finalScore) {
        GameMessage message = new GameMessage(MessageType.GAME_OVER, localPlayerId);
        message.put("score", finalScore);
        networkManager.sendMessage(message);
    }

    private void sendPlayerReady() {
        GameMessage message = new GameMessage(MessageType.PLAYER_READY, localPlayerId);
        networkManager.sendMessage(message);
    }

    private void sendGameStart() {
        GameMessage message = new GameMessage(MessageType.GAME_START, localPlayerId);
        message.put("mode", gameMode.name());
        message.put("player1Seed", player1Seed);
        message.put("player2Seed", player2Seed);
        networkManager.sendMessage(message);
    }

    private String serializeAttackLines(List<String[]> lines) {
        StringBuilder sb = new StringBuilder();
        for (String[] line : lines) {
            for (String cell : line) {
                sb.append(cell != null ? "1" : "0");
            }
            sb.append(";");
        }
        return sb.toString();
    }

    private List<String[]> deserializeAttackLines(String data) {
        List<String[]> result = new java.util.ArrayList<>();
        String[] lines = data.split(";");

        for (String line : lines) {
            if (line.isEmpty()) {
                continue;
            }
            String[] cells = new String[10];
            for (int i = 0; i < Math.min(line.length(), 10); i++) {
                cells[i] = line.charAt(i) == '1' ? "attack-block" : null;
            }
            result.add(cells);
        }

        return result;
    }

    @Override
    public void onMessageReceived(GameMessage message) {
        Platform.runLater(() -> {
            handleGameMessage(message);
        });
    }

    private void handleGameMessage(GameMessage message) {
        MessageType type = message.getType();

        switch (type) {
            case GAME_START:
                String modeName = message.getString("mode");
                if (modeName != null) {
                    this.gameMode = VersusGameModeDialog.VersusMode.valueOf(modeName);
                }

                Long p1Seed = (Long) message.get("player1Seed");
                Long p2Seed = (Long) message.get("player2Seed");
                if (p1Seed != null && p2Seed != null) {
                    this.player1Seed = p1Seed;
                    this.player2Seed = p2Seed;
                }

                Platform.runLater(() -> {
                    if (modeLabel != null && gameMode != null) {

                        String gamemodetext = "";
                        if (gameMode == VersusGameModeDialog.VersusMode.NORMAL) {
                            gamemodetext = "Normal";
                        } else if (gameMode == VersusGameModeDialog.VersusMode.TIME_LIMIT) {
                            gamemodetext = "Time Limit";
                        } else if (gameMode == VersusGameModeDialog.VersusMode.ITEM) {
                            gamemodetext = "Item";
                        }

                        modeLabel = new Label(gamemodetext);

                        if (gameMode == VersusGameModeDialog.VersusMode.NORMAL) {
                            modeLabel.getStyleClass().add("normal-mode-label");
                        } else if (gameMode == VersusGameModeDialog.VersusMode.TIME_LIMIT) {
                            modeLabel.getStyleClass().add("timelimit-mode-label");
                        } else if (gameMode == VersusGameModeDialog.VersusMode.ITEM) {
                            modeLabel.getStyleClass().add("item-mode-versus-label");
                        }

                    }
                    if (remotePlayerId != null) {
                        readyButton.setDisable(false);
                    }
                });
                break;

            case PLAYER_READY:
                remoteReady = true;
                Platform.runLater(() -> {
                    readyButton.setText("상대방 준비 완료!");
                    readyButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                });
                checkBothReady();
                break;

            case GAME_READY:
                startGame();
                break;

            case BOARD_UPDATE:
                updateRemoteBoard(message);
                break;

            case ATTACK:
                int linesCleared = message.getInt("linesCleared", 0);
                String attackData = message.getString("attackData");
                List<String[]> attackLines = deserializeAttackLines(attackData);

                localAttackQueue.offer(new AttackData(attackLines, linesCleared));
                Logger.info(">>> Received attack: " + linesCleared + " lines");
                break;

            case GAME_OVER:
                endGame(true);
                break;

            default:
                Logger.info(">>> Unhandled message: " + type);
                break;
        }
    }

    @Override
    public void onConnected(String peerId) {
        this.remotePlayerId = peerId;
        Platform.runLater(() -> {
            latencyLabel.setText("📡 연결됨: " + peerId);
            latencyLabel.setStyle("-fx-text-fill: green;");
            readyButton.setDisable(false);

            if (isServer && gameMode != null) {
                player1Seed = System.nanoTime();
                try {
                    Thread.sleep(1);
                } catch (Exception e) {
                }
                player2Seed = System.nanoTime();
                sendGameStart();
            }
        });
    }

    @Override
    public void onDisconnected(String peerId, String reason) {
        Platform.runLater(() -> {
            latencyLabel.setText("❌ 연결 끊김: " + reason);
            latencyLabel.setStyle("-fx-text-fill: red;");

            if (gameActive) {
                endGame(true);
            } else {
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                        javafx.scene.control.Alert.AlertType.WARNING
                );
                alert.setTitle("연결 끊김");
                alert.setHeaderText("상대방과의 연결이 끊어졌습니다.");
                alert.setContentText(reason);
                alert.showAndWait();
                goToMainMenu();
            }
        });
    }

    @Override
    public void onError(String errorMessage, Exception exception) {
        Platform.runLater(() -> {
            Logger.error(">>> Network error: " + errorMessage);
            if (exception != null) {
                exception.printStackTrace();
            }
        });
    }

    @Override
    public void onLatencyUpdate(long latencyMs) {
        Platform.runLater(() -> {
            String color = latencyMs < 50 ? "green"
                    : latencyMs < 100 ? "yellow"
                            : latencyMs < 200 ? "orange" : "red";

            latencyLabel.setText("📡 레이턴시: " + latencyMs + "ms");
            latencyLabel.setStyle("-fx-text-fill: " + color + ";");
        });
    }

    private void onReadyButtonClick() {
        if (localReady) {
            return;
        }

        localReady = true;
        readyButton.setText("준비 완료!");
        readyButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        readyButton.setDisable(true);

        sendPlayerReady();
        checkBothReady();
    }

    private void checkBothReady() {
        if (localReady && remoteReady) {
            if (isServer) {
                GameMessage readyMsg = new GameMessage(MessageType.GAME_READY, localPlayerId);
                networkManager.sendMessage(readyMsg);
                startGame();
            }
        }
    }

    private void disconnect() {
        if (networkManager != null) {
            networkManager.disconnect("Game ended");
        }
    }

    public StackPane getRoot() {
        return mainContainer;
    }

    public void cleanup() {
        if (gameLoop != null) {
            gameLoop.stop();
        }
        if (networkManager != null) {
            networkManager.shutdown();
        }
        if (localBoard != null) {
            localBoard.cleanup();
        }
        if (remoteBoard != null) {
            remoteBoard.cleanup();
        }
    }
}
