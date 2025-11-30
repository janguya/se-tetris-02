package com.example.game.component;

import java.util.List;

import com.example.network.GameMessage;
import com.example.network.MessageListener;
import com.example.network.MessageType;
import com.example.network.NetworkManager;
import com.example.settings.GameSettings;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class OnlineVersusBoard implements MessageListener {

    private VersusGameModeDialog.VersusMode gameMode;
    private final GameSettings gameSettings;
    private final NetworkManager networkManager;
    private final String localPlayerId;
    private final javafx.stage.Stage stage;
    
    // UI 컴포넌트
    private StackPane mainContainer;
    private BorderPane root;
    private HBox gameArea;
    private MenuOverlay menuOverlay;
    private VersusGameOverScene gameOverScene;
    
    // 플레이어 보드
    private PlayerBoard localBoard;   // 내 보드
    private PlayerBoard remoteBoard;  // 상대방 보드 (읽기 전용)
    private ScorePanel localScorePanel;
    private ScorePanel remoteScorePanel;
    private AttackQueueDisplay localAttackDisplay;
    private AttackQueueDisplay remoteAttackDisplay;
    
    // 게임 상태
    private boolean gameActive = false;
    private boolean isPaused = false;
    private boolean isServer;
    private String remotePlayerId;

    // 준비 상태
    private boolean localReady = false;
    private boolean remoteReady = false;
    private javafx.scene.control.Button readyButton;
    
    // 게임 루프
    private javafx.animation.AnimationTimer gameLoop;
    private long lastUpdate = 0;
    
    // 레이턴시 표시
    private Label latencyLabel;
    private Label modeLabel;
    private Label timerLabel;
    
    // 시간제한 모드용
    private long gameStartTime;
    private long timeLimitMillis = 180000; // 3분

    // 블록 동기화용 Random seed (2개)
    private Long player1Seed = null;  // 플레이어 1(서버)의 블록 순서
    private Long player2Seed = null;  // 플레이어 2(클라이언트)의 블록 순서

    // 생성자
    public OnlineVersusBoard(javafx.stage.Stage stage,
                            VersusGameModeDialog.VersusMode mode, 
                            NetworkManager networkManager, 
                            boolean isServer) {
        this.stage = stage;
        this.gameMode = mode;
        this.gameSettings = GameSettings.getInstance();
        this.networkManager = networkManager;
        this.isServer = isServer;
        this.localPlayerId = isServer ? "Server" : "Client";
        this.menuOverlay = new MenuOverlay();
        networkManager.setListener(this);
        
        initializeUI();
        this.gameOverScene = new VersusGameOverScene(stage, mainContainer, this::restartGame);
        setupKeyHandling();
    }

    // UI 초기화
    private void initializeUI() {
        mainContainer = new StackPane();
        root = new BorderPane();
        root.getStyleClass().add("versus-root");
        root.setPadding(new Insets(30));
        
        // 상단 정보
        VBox topInfo = createTopInfo();
        root.setTop(topInfo);
        BorderPane.setMargin(topInfo, new Insets(0, 0, 20, 0));
        
        // 게임 영역
        gameArea = new HBox(40);
        gameArea.setAlignment(Pos.CENTER);
        gameArea.setPadding(new Insets(10));
        
        // 아이템 모드 여부
        boolean itemMode = (gameMode != null && gameMode == VersusGameModeDialog.VersusMode.ITEM);
        
        // 로컬 플레이어 보드 (왼쪽)
        BorderPane localContainer = createPlayerBoard(true, itemMode);
        
        // 원격 플레이어 보드 (오른쪽)
        BorderPane remoteContainer = createPlayerBoard(false, itemMode);
        
        gameArea.getChildren().addAll(localContainer, remoteContainer);
        HBox.setHgrow(localContainer, Priority.ALWAYS);
        HBox.setHgrow(remoteContainer, Priority.ALWAYS);
        
        root.setCenter(gameArea);
        
        mainContainer.getChildren().addAll(root, menuOverlay.getOverlay());
    }

    // 상단 정보 생성
    private VBox createTopInfo() {
        VBox topInfo = new VBox(15);
        topInfo.setAlignment(Pos.CENTER);
        topInfo.setPadding(new Insets(10));
        topInfo.getStyleClass().add("versus-top-info");
        
        // 타이틀
        String modeDisplay = gameMode != null ? gameMode.getDisplayName() : "대기 중...";
        modeLabel = new Label("⚔ 온라인 대전: " + modeDisplay + " ⚔");
        modeLabel.setFont(Font.font("Arial", FontWeight.BOLD, 32));
        modeLabel.setStyle("-fx-text-fill: white;" +
                          "-fx-effect: dropshadow(gaussian, rgba(0,212,255,0.5), 10, 0, 0, 0);");
        
        // 레이턴시 표시
        latencyLabel = new Label("📡 연결 중...");
        latencyLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        latencyLabel.setStyle("-fx-text-fill: #ffeb3b;");
        
        topInfo.getChildren().addAll(modeLabel, latencyLabel);
        
        // 시간제한 모드에서는 타이머 추가
        if (gameMode == VersusGameModeDialog.VersusMode.TIME_LIMIT) {
            timerLabel = new Label("⏱ 03:00");
            timerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
            timerLabel.setStyle("-fx-text-fill: #00ff00;");
            topInfo.getChildren().add(timerLabel);
        }

        // 준비 버튼
        readyButton = new javafx.scene.control.Button("준비");
        readyButton.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        readyButton.setPrefWidth(150);
        readyButton.setPrefHeight(50);
        readyButton.setStyle("-fx-background-color: #00d4ff; -fx-text-fill: white; -fx-background-radius: 10;");
        readyButton.setDisable(true); // 연결 전에는 비활성화
        readyButton.setOnAction(e -> onReadyButtonClick());
        
        topInfo.getChildren().add(readyButton);
        return topInfo;
    }

    // 모드 레이블 업데이트
    private void updateModeLabel() {
        if (modeLabel != null && gameMode != null) {
            Platform.runLater(() -> {
                modeLabel.setText("⚔ 온라인 대전: " + gameMode.getDisplayName() + " ⚔");
            });
        }
    }

    // 개별 플레이어 보드 생성
    private BorderPane createPlayerBoard(boolean isLocal, boolean itemMode) {
        BorderPane container = new BorderPane();
        container.getStyleClass().add("versus-player-container");
        container.setMaxWidth(500);
        
        // 헤더
        VBox header = new VBox(8);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(15));
        header.getStyleClass().add("versus-player-header");
        
        String playerName = isLocal ? "나 (You)" : "상대방 (Opponent)";
        Label nameLabel = new Label(playerName);
        nameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        nameLabel.setStyle("-fx-text-fill: " + (isLocal ? "#00d4ff" : "#ff6b6b") + ";");
        
        String controls = isLocal ? "화살표 키 + Enter" : "자동 동기화";
        Label controlLabel = new Label(controls);
        controlLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
        controlLabel.setStyle("-fx-text-fill: #bbbbbb;");
        
        header.getChildren().addAll(nameLabel, controlLabel);
        container.setTop(header);
        
        // 게임 보드 생성
        if (isLocal) {
            localBoard = new PlayerBoard(1, this::onLocalLinesCleared, itemMode);
            localBoard.initializeUI();
            localScorePanel = localBoard.scorePanel;
            localAttackDisplay = new AttackQueueDisplay("You");
            
            // 스코어 패널에 공격 표시 추가
            VBox scorePanelWithAttack = new VBox(15);
            scorePanelWithAttack.setAlignment(Pos.TOP_CENTER);
            scorePanelWithAttack.getChildren().addAll(
                localScorePanel.getPanel(),
                localAttackDisplay.getContainer()
            );
            scorePanelWithAttack.getStyleClass().add("side-panel");
            
            container.setCenter(localBoard.getCanvas());
            container.setRight(scorePanelWithAttack);
            BorderPane.setMargin(scorePanelWithAttack, new Insets(0, 0, 0, 15));
            
        } else {
            remoteBoard = new PlayerBoard(2, (pn, lc, cl) -> {}, itemMode);
            remoteBoard.initializeUI();
            remoteScorePanel = remoteBoard.scorePanel;
            remoteAttackDisplay = new AttackQueueDisplay("Opponent");
            
            // 스코어 패널에 공격 표시 추가
            VBox scorePanelWithAttack = new VBox(15);
            scorePanelWithAttack.setAlignment(Pos.TOP_CENTER);
            scorePanelWithAttack.getChildren().addAll(
                remoteScorePanel.getPanel(),
                remoteAttackDisplay.getContainer()
            );
            scorePanelWithAttack.getStyleClass().add("side-panel");
            
            container.setLeft(scorePanelWithAttack);
            container.setCenter(remoteBoard.getCanvas());
            BorderPane.setMargin(scorePanelWithAttack, new Insets(0, 15, 0, 0));
        }
        
        return container;
    }

    // 키 입력 처리
    private void setupKeyHandling() {
        mainContainer.setFocusTraversable(true);
        mainContainer.setOnKeyPressed(event -> {
            if (!gameActive || isPaused) {
                if (event.getCode() == KeyCode.ESCAPE) {
                    togglePause();
                }
                return;
            }
            
            KeyCode code = event.getCode();
            
            // 로컬 플레이어 조작
            switch (code) {
                case LEFT:
                    localBoard.onMoveLeft();
                    sendGameAction(MessageType.BLOCK_MOVE, "direction", "left");
                    break;
                case RIGHT:
                    localBoard.onMoveRight();
                    sendGameAction(MessageType.BLOCK_MOVE, "direction", "right");
                    break;
                case DOWN:
                    localBoard.onMoveDown();
                    sendGameAction(MessageType.BLOCK_MOVE, "direction", "down");
                    break;
                case UP:
                    localBoard.onRotate();
                    sendGameAction(MessageType.BLOCK_ROTATE, null, null);
                    break;
                case ENTER:
                case SPACE:
                    localBoard.onHardDrop();
                    sendGameAction(MessageType.BLOCK_DROP, null, null);
                    break;
                case ESCAPE:
                    togglePause();
                    break;
                default:
                    // 기타 키는 무시
                    break;
            }
            
            event.consume();
        });
        
        mainContainer.requestFocus();
    }

    // 게임 액션 전송
    private void sendGameAction(MessageType type, String key, String value) {
        GameMessage message = new GameMessage(type, localPlayerId);
        if (key != null && value != null) {
            message.put(key, value);
        }
        networkManager.sendMessage(message);
    }

    // 게임 시작 메시지 전송 (서버 → 클라이언트)
    private void sendGameStart() {
        GameMessage message = new GameMessage(MessageType.GAME_START, localPlayerId);
        message.put("mode", gameMode.name()); // 모드 정보 전송
        message.put("player1Seed", player1Seed); // 플레이어 1 seed
        message.put("player2Seed", player2Seed); // 플레이어 2 seed
        networkManager.sendMessage(message);
        System.out.println(">>> Sent GAME_START with mode: " + gameMode.getDisplayName());
    }

    // 게임 준비 완료 메시지 전송 (클라이언트 → 서버)
    private void sendGameReady() {
        GameMessage message = new GameMessage(MessageType.GAME_READY, localPlayerId);
        networkManager.sendMessage(message);
        System.out.println(">>> Sent GAME_READY");
    }

    // remoteBoard 화면 강제 갱신
    private void refreshRemoteBoard() {
         Platform.runLater(() -> remoteBoard.drawBoard());
    }

    // 로컬 플레이어의 줄 삭제 처리(공격)
    private void onLocalLinesCleared(int playerNumber, int linesCleared, List<String[]> clearedLines) {
        if (linesCleared < 2) return;
        
        System.out.println(">>> Sending attack: " + linesCleared + " lines");
        
        // 공격 메시지 전송
        GameMessage attackMsg = new GameMessage(MessageType.ATTACK, localPlayerId);
        attackMsg.put("linesCleared", linesCleared);
        attackMsg.put("attackData", serializeAttackLines(clearedLines));
        
        networkManager.sendMessage(attackMsg);
    }

    // 공격 라인 직렬화
    private String serializeAttackLines(List<String[]> lines) {
        // 간단한 직렬화: JSON 형태로 변환
        StringBuilder sb = new StringBuilder();
        for (String[] line : lines) {
            for (String cell : line) {
                sb.append(cell != null ? "1" : "0");
            }
            sb.append(";");
        }
        return sb.toString();
    }

    // 공격 라인 역직렬화
    private List<String[]> deserializeAttackLines(String data) {
        List<String[]> result = new java.util.ArrayList<>();
        String[] lines = data.split(";");
        
        for (String line : lines) {
            if (line.isEmpty()) continue;
            String[] cells = new String[GameLogic.WIDTH];
            for (int i = 0; i < Math.min(line.length(), GameLogic.WIDTH); i++) {
                cells[i] = line.charAt(i) == '1' ? "attack-block" : null;
            }
            result.add(cells);
        }
        
        return result;
    }

    // 게임 시작
    private void startGame() {
        gameActive = true;
        gameStartTime = System.currentTimeMillis(); // 타이머 초기화
        
        // Random seed 적용 (블록 동기화)
        if (player1Seed != null && player2Seed != null) {
            // 서버: 내 블록은 player1Seed, 상대 블록은 player2Seed
            // 클라이언트: 내 블록은 player2Seed, 상대 블록은 player1Seed
            Long mySeed = isServer ? player1Seed : player2Seed;
            Long opponentSeed = isServer ? player2Seed : player1Seed;
            
            localBoard.gameLogic.setRandomSeed(mySeed);
            remoteBoard.gameLogic.setRandomSeed(opponentSeed);

            System.out.println(">>> " + (isServer ? "Server" : "Client") + 
                 " - localBoard seed: " + mySeed + 
                 ", remoteBoard seed: " + opponentSeed);
            // 블록이 재생성되었으므로 화면 강제 갱신
                localBoard.drawBoard();
                remoteBoard.drawBoard();

        } else {
            System.out.println(">>> WARNING: No random seed set! Blocks will desync!");
        }
        
        startGameLoop();

        // 게임 시작 시 포커스 요청
         Platform.runLater(() -> {
            mainContainer.requestFocus();
            System.out.println(">>> Focus requested for keyboard input");
        });

        System.out.println(">>> Online game started!");
    }

    // 게임 루프 시작
    private void startGameLoop() {
        if (gameLoop != null) {
            gameLoop.stop();
        }

        final long[] lastRemoteUpdate = {System.nanoTime()}; // 독립적인 타이머
        
        gameLoop = new javafx.animation.AnimationTimer() {
            @Override
            public void handle(long now) {
                if (!gameActive || isPaused) return;
                
                // 시간제한 모드에서 타이머 업데이트
                if (gameMode == VersusGameModeDialog.VersusMode.TIME_LIMIT) {
                    updateTimer();
                }
                
                // 로컬 보드 업데이트
                long elapsedNanos = now - lastUpdate;
                long dropInterval = localBoard.getDropInterval();
                
                if (elapsedNanos >= dropInterval * 1_000_000) {
                    localBoard.update();
                    lastUpdate = now;
                }
                
                // 애니메이션 중일 때는 매 프레임 업데이트
                if (localBoard.isAnimationActive()) {
                    localBoard.update();
                }
                
                // 상대방 보드도 자동 낙하 (동일한 간격으로)
                if (now - lastRemoteUpdate[0] >= remoteBoard.getDropInterval() * 1_000_000) {
                    remoteBoard.update();
                    lastRemoteUpdate[0] = now;
                }
                
                // 상대방 보드 애니메이션 중일 때도 매 프레임 업데이트
                if (remoteBoard.isAnimationActive()) {
                    remoteBoard.update();
                }
                
                // AttackDisplay 업데이트
                updateAttackDisplays();
                
                // 게임 종료 체크
                checkGameEnd();
            }
        };
        
        lastUpdate = System.nanoTime();
        gameLoop.start();
    }

    // 게임 종료 체크
    private void checkGameEnd() {
        if (localBoard.isGameOver()) {
            // 상대방에게 내가 졌다는 메시지 전송
            GameMessage gameOverMsg = new GameMessage(MessageType.GAME_OVER, localPlayerId);
            gameOverMsg.put("score", localBoard.getScore());
            networkManager.sendMessage(gameOverMsg);
            
            endGame(false); // 내가 짐
        }
        // 상대방이 졌다는 메시지를 받으면 endGame(true) 호출
        
        // 시간제한 모드에서 시간 종료 체크
        if (gameMode == VersusGameModeDialog.VersusMode.TIME_LIMIT) {
            long elapsed = System.currentTimeMillis() - gameStartTime;
            if (elapsed >= timeLimitMillis) {
                endGameByTime();
            }
        }
    }
    
    // 게임 종료
    private void endGame(boolean iWon) {
        gameActive = false;
        if (gameLoop != null) {
            gameLoop.stop();
        }
        
        int localScore = localBoard.getScore();
        int remoteScore = remoteBoard.getScore();
        
        VersusGameOverScene.GameResult result;
        if (iWon) {
            result = isServer ? VersusGameOverScene.GameResult.PLAYER1_WIN : VersusGameOverScene.GameResult.PLAYER2_WIN;
        } else {
            result = isServer ? VersusGameOverScene.GameResult.PLAYER2_WIN : VersusGameOverScene.GameResult.PLAYER1_WIN;
        }
        
        Platform.runLater(() -> {
            if (isServer) {
                gameOverScene.show(result, localScore, remoteScore);
            } else {
                gameOverScene.show(result, remoteScore, localScore);
            }
        });
    }
    
    // 시간제한 모드 종료
    private void endGameByTime() {
        gameActive = false;
        if (gameLoop != null) {
            gameLoop.stop();
        }
        
        int localScore = localBoard.getScore();
        int remoteScore = remoteBoard.getScore();
        
        // 상대방에게 시간 종료 메시지 전송
        GameMessage timeUpMsg = new GameMessage(MessageType.GAME_OVER, localPlayerId);
        timeUpMsg.put("reason", "time_limit");
        timeUpMsg.put("score", localScore);
        networkManager.sendMessage(timeUpMsg);
        
        VersusGameOverScene.GameResult result;
        if (localScore > remoteScore) {
            result = isServer ? VersusGameOverScene.GameResult.PLAYER1_WIN : VersusGameOverScene.GameResult.PLAYER2_WIN;
        } else if (remoteScore > localScore) {
            result = isServer ? VersusGameOverScene.GameResult.PLAYER2_WIN : VersusGameOverScene.GameResult.PLAYER1_WIN;
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
    
    /**
     * 타이머 업데이트 (시간제한 모드)
     */
    private void updateTimer() {
        if (timerLabel == null) return;
        
        long elapsed = System.currentTimeMillis() - gameStartTime;
        long remaining = Math.max(0, timeLimitMillis - elapsed);
        
        long minutes = remaining / 60000;
        long seconds = (remaining % 60000) / 1000;
        
        Platform.runLater(() -> {
            timerLabel.setText(String.format("⏱ %02d:%02d", minutes, seconds));
            
            // 30초 미만일 때 경고 스타일
            if (remaining < 30000) {
                timerLabel.setStyle("-fx-text-fill: #ff0000; -fx-font-weight: bold;");
            } else {
                timerLabel.setStyle("-fx-text-fill: #00ff00;");
            }
        });
    }
    
    /**
     * AttackDisplay 업데이트
     */
    private void updateAttackDisplays() {
        int localPending = localBoard.getPendingAttackCount();
        int remotePending = remoteBoard.getPendingAttackCount();
        
        Platform.runLater(() -> {
            if (localAttackDisplay != null) {
                localAttackDisplay.syncWithActualQueue(localPending);
            }
            if (remoteAttackDisplay != null) {
                remoteAttackDisplay.syncWithActualQueue(remotePending);
            }
        });
    }

    // 일시정지 토글
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
        
        menuOverlay.showPauseMenu(new MenuOverlay.MenuCallback() {
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
            }
            
            @Override
            public void onExit() {
                disconnect();
            }
        });
    }
    
    private void resumeGame() {
        isPaused = false;
        menuOverlay.hide();
        startGameLoop();
        mainContainer.requestFocus();
    }

    // 연결 종료
    private void disconnect() {
        if (networkManager != null) {
            networkManager.disconnect("User left game");
        }
        // 메인 메뉴로 이동은 Router에서 처리
    }
    
    // ============== MessageListener 구현 ==============
    
    @Override
    public void onMessageReceived(GameMessage message) {
        Platform.runLater(() -> {
            handleGameMessage(message);
        });
    }

    // 게임 메시지 처리
    private void handleGameMessage(GameMessage message) {
        MessageType type = message.getType();
        
        switch (type) {
            case GAME_START:
                // 서버로부터 게임 시작 메시지 받음 (모드 정보 + Random seed)
                String modeName = message.getString("mode");
                if (modeName != null) {
                    this.gameMode = VersusGameModeDialog.VersusMode.valueOf(modeName);
                    System.out.println(">>> Received game mode from server: " + gameMode.getDisplayName());
                }
                
                // 2개의 Random seed 받기
                Long p1Seed = (Long) message.get("player1Seed");
                Long p2Seed = (Long) message.get("player2Seed");
                if (p1Seed != null && p2Seed != null) {
                    this.player1Seed = p1Seed;
                    this.player2Seed = p2Seed;
                    System.out.println(">>> Received seeds - P1: " + p1Seed + ", P2: " + p2Seed);
                }
                
                // UI 업데이트 (모드 표시)
                Platform.runLater(() -> {
                    VBox topInfo = createTopInfo();
                    root.setTop(topInfo);
                    BorderPane.setMargin(topInfo, new Insets(0, 0, 20, 0));

                    // 연결된 상태면 준비 버튼 활성화
                    if (remotePlayerId != null) {
                        readyButton.setDisable(false);
                    }
                });
                break;
                
            case PLAYER_READY:
                // 상대방이 준비 완료
                remoteReady = true;
                System.out.println(">>> Remote player is ready");
                
                Platform.runLater(() -> {
                    readyButton.setText("상대방 준비 완료!");
                    readyButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-background-radius: 10;");
                });
                
                // 양쪽 모두 준비되면 게임 시작
                checkBothReady();
                break;
                
            case GAME_READY:
                // 양쪽 모두 준비 완료 - 게임 시작
                System.out.println(">>> Both players ready, starting game...");
                startGame();
                break;
            
            case BLOCK_MOVE:
                String direction = message.getString("direction");
                if ("left".equals(direction)) {
                    remoteBoard.onMoveLeft();
                } else if ("right".equals(direction)) {
                    remoteBoard.onMoveRight();
                } else if ("down".equals(direction)) {
                    remoteBoard.onMoveDown();
                }
                // 화면 갱신 강제 트리거
                refreshRemoteBoard();
                break;
                
            case BLOCK_ROTATE:
                remoteBoard.onRotate();
                // 화면 갱신 강제 트리거
                refreshRemoteBoard();
                break;
                
            case BLOCK_DROP:
                remoteBoard.onHardDrop();
                // 화면 갱신 강제 트리거
                refreshRemoteBoard();
                break;
                
            case ATTACK:
                int linesCleared = message.getInt("linesCleared", 0);
                String attackData = message.getString("attackData");
                List<String[]> attackLines = deserializeAttackLines(attackData);
                localBoard.receiveAttackLines(attackLines);
                System.out.println(">>> Received attack: " + linesCleared + " lines");
                break;
                
            case GAME_OVER:
                // 상대방이 게임 오버
                int opponentScore = message.getInt("score", 0);
                // 상대방 점수 동기화 (만약 메시지에 포함되어 있다면)
                if (opponentScore > 0 && remoteBoard != null) {
                    // remoteBoard의 점수는 실시간으로 동기화되지 않으므로 여기서 참고만
                    System.out.println(">>> Opponent final score: " + opponentScore);
                }
                endGame(true); // 내가 승리
                break;
                
            default:
                System.out.println(">>> Unhandled message: " + type);
                break;
        }
    }
    
    @Override
    public void onConnected(String peerId) {
        this.remotePlayerId = peerId;
        Platform.runLater(() -> {
            latencyLabel.setText("📡 연결됨: " + peerId);
            latencyLabel.setStyle("-fx-text-fill: green;");
        
            // 준비 버튼 활성화
            readyButton.setDisable(false);
            
            // 서버: GAME_START 메시지 전송 (모드 정보 + Random seed)
            if (isServer && gameMode != null) {
                // 2개의 다른 Random seed 생성
                player1Seed = System.nanoTime();
try { Thread.sleep(1); } catch (Exception e) { }  // 확실한 시간 차이 보장
player2Seed = System.nanoTime();
System.out.println(">>> Server generated seeds - P1: " + player1Seed + ", P2: " + player2Seed);
                sendGameStart();
            }

            // 클라이언트: GAME_START 메시지 대기 (아무것도 안 함)
        });
    }
    
    @Override
    public void onDisconnected(String peerId, String reason) {
        Platform.runLater(() -> {
            latencyLabel.setText("❌ 연결 끊김: " + reason);
            latencyLabel.setStyle("-fx-text-fill: red;");
            
            // 게임이 진행 중이었다면 게임 오버 처리
            if (gameActive) {
                gameActive = false;
                if (gameLoop != null) {
                    gameLoop.stop();
                }
                
                // 연결 끊김으로 인한 승리 처리
                int localScore = localBoard.getScore();
                int remoteScore = remoteBoard.getScore();
                
                VersusGameOverScene.GameResult result = isServer ? 
                    VersusGameOverScene.GameResult.PLAYER1_WIN : 
                    VersusGameOverScene.GameResult.PLAYER2_WIN;
                
                if (isServer) {
                    gameOverScene.show(result, localScore, remoteScore);
                } else {
                    gameOverScene.show(result, remoteScore, localScore);
                }
            } else {
                // 게임 시작 전이라면 경고 다이얼로그만 표시
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.WARNING
                );
                alert.setTitle("연결 끊김");
                alert.setHeaderText("상대방과의 연결이 끊어졌습니다.");
                alert.setContentText(reason);
                alert.showAndWait();
                
                // 메인 메뉴로 이동
                com.example.Router router = new com.example.Router((javafx.stage.Stage) mainContainer.getScene().getWindow());
                router.showStartMenu();
            }
        });
    }
    
    @Override
    public void onError(String errorMessage, Exception exception) {
        Platform.runLater(() -> {
            System.err.println(">>> Network error: " + errorMessage);
            if (exception != null) {
                exception.printStackTrace();
            }
        });
    }
    
    @Override
    public void onLatencyUpdate(long latencyMs) {
        Platform.runLater(() -> {
            String color = latencyMs < 50 ? "green" : 
                         latencyMs < 100 ? "yellow" : 
                         latencyMs < 200 ? "orange" : "red";
            
            latencyLabel.setText("📡 레이턴시: " + latencyMs + "ms");
            latencyLabel.setStyle("-fx-text-fill: " + color + ";");
        });
    }

    // ============== 준비 및 게임 시작 ==============
    
    // 준비 버튼 클릭 처리
    private void onReadyButtonClick() {
        if (localReady) return; // 이미 준비됨
        
        localReady = true;
        readyButton.setText("준비 완료!");
        readyButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-background-radius: 10;");
        readyButton.setDisable(true);
        
        // PLAYER_READY 메시지 전송
        sendPlayerReady();
        
        System.out.println(">>> Local player is ready");
        
        // 양쪽 모두 준비되면 게임 시작
        checkBothReady();
    }
    
    // 양쪽 모두 준비되었는지 확인
    private void checkBothReady() {
        if (localReady && remoteReady) {
            System.out.println(">>> Both players ready!");
            
            // 서버만 GAME_READY 메시지 전송 (양쪽 동시 시작 신호)
            if (isServer) {
                sendGameReady();
                startGame();
            }
            // 클라이언트는 GAME_READY 받으면 시작
        }
    }
    
    // PLAYER_READY 메시지 전송
    private void sendPlayerReady() {
        GameMessage message = new GameMessage(MessageType.PLAYER_READY, localPlayerId);
        networkManager.sendMessage(message);
    }
    
    /**
     * 게임 재시작 (네트워크 모드에서는 메인 메뉴로 이동)
     */
    public void restartGame() {
        gameOverScene.hide();
        
        // 네트워크 연결 종료
        if (networkManager != null) {
            networkManager.disconnect("Game ended");
        }
        
        // 메인 메뉴로 이동
        Platform.runLater(() -> {
            com.example.Router router = new com.example.Router(stage);
            router.showStartMenu();
        });
    }
    
    // ============== Getters ==============
    
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
