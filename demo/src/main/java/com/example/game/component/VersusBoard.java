package com.example.game.component;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import com.example.Router;
import com.example.game.component.MenuOverlay.MenuCallback;
import com.example.settings.GameSettings;
import com.example.utils.Logger;

import javafx.animation.AnimationTimer;
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
import javafx.stage.Stage;

public class VersusBoard {
    
    public interface VersusGameCallback {
        void onPlayerWin(int winnerPlayer, int player1Score, int player2Score);
        void onGameEnd();
    }
    
    private final VersusGameModeDialog.VersusMode gameMode;
    private final VersusGameCallback callback;
    private final GameSettings gameSettings;
    
    // UI 컴포넌트
    private StackPane mainContainer;
    private BorderPane root;
    private HBox gameArea;
    private MenuOverlay menuOverlay;
    
    // 플레이어 1 (왼쪽) - WASD 조작
    private PlayerBoard player1Board;
    private ScorePanel player1ScorePanel;
    private AttackQueueDisplay player1AttackDisplay;
    
    // 플레이어 2 (오른쪽) - 방향키 조작
    private PlayerBoard player2Board;
    private ScorePanel player2ScorePanel;
    private AttackQueueDisplay player2AttackDisplay;
    
    // 게임 상태
    private boolean gameActive = true;
    private AnimationTimer gameLoop;
    private long lastUpdate1 = 0;
    private long lastUpdate2 = 0;
    private boolean isPaused = false;
    
    // 시간제한 모드용
    private long gameStartTime;
    private long timeLimitMillis = 180000; // 3분
    private Label timerLabel;
    
    // 공격 시스템용
    private Queue<AttackData> player1AttackQueue = new LinkedList<>();
    private Queue<AttackData> player2AttackQueue = new LinkedList<>();
    
    // 공격 데이터 클래스
    private static class AttackData {
        List<String[]> lines;
        int count;
        
        AttackData(List<String[]> lines, int count) {
            this.lines = lines;
            this.count = count;
        }
    }
    
    public VersusBoard(VersusGameModeDialog.VersusMode mode, VersusGameCallback callback) {
        this.gameMode = mode;
        this.callback = callback;
        this.gameSettings = GameSettings.getInstance();
        this.menuOverlay = new MenuOverlay();
        
        // 초기화 순서 수정: UI -> 키 핸들링 -> 게임 시작
        initializeUI();
        setupKeyHandling();
        startGame();
    }
    
    /**
     * UI 초기화
     */
    private void initializeUI() {
        mainContainer = new StackPane();
        root = new BorderPane();
        root.getStyleClass().add("versus-root");
        root.setPadding(new Insets(30));
        
        // 상단 정보 (모드, 타이머 등)
        VBox topInfo = createTopInfo();
        root.setTop(topInfo);
        BorderPane.setMargin(topInfo, new Insets(0, 0, 20, 0));
        
        // 게임 영역 (중앙) - 두 보드를 가로로 배치
        gameArea = new HBox(40);
        gameArea.setAlignment(Pos.CENTER);
        gameArea.setPadding(new Insets(10));
        
        // 플레이어 1 영역 (왼쪽)
        BorderPane player1Container = createPlayerBoard(1, "WASD + Space");
        
        // 플레이어 2 영역 (오른쪽)
        BorderPane player2Container = createPlayerBoard(2, "Arrow Keys + Enter");
        
        // 게임 영역에 두 플레이어 보드 추가
        gameArea.getChildren().addAll(player1Container, player2Container);
        HBox.setHgrow(player1Container, Priority.ALWAYS);
        HBox.setHgrow(player2Container, Priority.ALWAYS);
        
        root.setCenter(gameArea);
        
        // 메인 컨테이너에 게임 보드와 오버레이 추가
        mainContainer.getChildren().addAll(root, menuOverlay.getOverlay());
    }

    /**
     * 개별 플레이어 보드 생성
     */
    private BorderPane createPlayerBoard(int playerNumber, String controls) {
        // 플레이어 보드 컨테이너
        BorderPane playerContainer = new BorderPane();
        playerContainer.getStyleClass().add("versus-player-container");
        playerContainer.setMaxWidth(600);
        
        // 플레이어 정보 헤더 (상단)
        VBox playerHeader = new VBox(8);
        playerHeader.setAlignment(Pos.CENTER);
        playerHeader.setPadding(new Insets(15));
        playerHeader.getStyleClass().add("versus-player-header");
        
        Label playerLabel = new Label("Player " + playerNumber);
        playerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        playerLabel.setStyle("-fx-text-fill: " + (playerNumber == 1 ? "#00d4ff" : "#ff6b6b") + ";");
        
        Label controlsLabel = new Label(controls);
        controlsLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
        controlsLabel.setStyle("-fx-text-fill: #bbbbbb;");
        
        playerHeader.getChildren().addAll(playerLabel, controlsLabel);
        playerContainer.setTop(playerHeader);
        
        // 아이템 모드 여부
        boolean itemMode = (gameMode == VersusGameModeDialog.VersusMode.ITEM);
        
        // 게임 보드 생성 및 초기화
        if (playerNumber == 1) {
            player1Board = new PlayerBoard(1, this::onLinesCleared, itemMode);
            player1Board.initializeUI();
            player1ScorePanel = player1Board.scorePanel;
            player1AttackDisplay = new AttackQueueDisplay("Player 1");
            
            // 레이아웃: 중앙: 캔버스, 오른쪽: 점수판 + 공격표시
            VBox rightPanel = new VBox(15);
            rightPanel.setAlignment(Pos.TOP_CENTER);
            rightPanel.getChildren().addAll(
                player1ScorePanel.getPanel(),
                player1AttackDisplay.getContainer()
            );
            
            playerContainer.setCenter(player1Board.getCanvas());
            playerContainer.setRight(rightPanel);
            
            player1ScorePanel.getPanel().getStyleClass().add("side-panel");
            BorderPane.setMargin(rightPanel, new Insets(0, 0, 0, 15));
            
            // 캔버스에 그림자 효과
            player1Board.getCanvas().setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 15, 0, 0, 0);");
            
        } else {
            player2Board = new PlayerBoard(2, this::onLinesCleared, itemMode);
            player2Board.initializeUI();
            player2ScorePanel = player2Board.scorePanel;
            player2AttackDisplay = new AttackQueueDisplay("Player 2");
            
            // 레이아웃: 왼쪽: 점수판 + 공격표시, 중앙: 캔버스
            VBox leftPanel = new VBox(15);
            leftPanel.setAlignment(Pos.TOP_CENTER);
            leftPanel.getChildren().addAll(
                player2ScorePanel.getPanel(),
                player2AttackDisplay.getContainer()
            );
            
            playerContainer.setLeft(leftPanel);
            playerContainer.setCenter(player2Board.getCanvas());
            
            player2ScorePanel.getPanel().getStyleClass().add("side-panel");
            BorderPane.setMargin(leftPanel, new Insets(0, 15, 0, 0));
            
            // 캔버스에 그림자 효과
            player2Board.getCanvas().setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 15, 0, 0, 0);");
        }
        
        return playerContainer;
    }

    /**
     * 상단 정보 영역 생성
     */
    private VBox createTopInfo() {
        VBox topInfo = new VBox(15);
        topInfo.setAlignment(Pos.CENTER);
        topInfo.setPadding(new Insets(10));
        topInfo.getStyleClass().add("versus-top-info");
        
        // 대전 모드 타이틀
        Label modeLabel = new Label("⚔ " + gameMode.getDisplayName() + " ⚔");
        modeLabel.setFont(Font.font("Arial", FontWeight.BOLD, 32));
        modeLabel.setStyle("-fx-text-fill: white;" +
                           "-fx-effect: dropshadow(gaussian, rgba(0,212,255,0.5), 10, 0, 0, 0);");
        
        topInfo.getChildren().add(modeLabel);
        
        // 시간제한 모드에서는 타이머 추가
        if (gameMode == VersusGameModeDialog.VersusMode.TIME_LIMIT) {
            timerLabel = new Label("⏱ 03:00");
            timerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 28));
            timerLabel.setStyle("-fx-text-fill: #ffeb3b;" +
                               "-fx-effect: dropshadow(gaussian, rgba(255,235,59,0.6), 10, 0, 0, 0);");
            topInfo.getChildren().add(timerLabel);
        }
        
        // 게임 설명
        Label infoLabel = new Label("2줄 이상 삭제하면 상대방을 공격!");
        infoLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        infoLabel.setStyle("-fx-text-fill: #bbbbbb;");
        topInfo.getChildren().add(infoLabel);
        
        return topInfo;
    }
    
    /**
     * 키 입력 처리 설정
     */
    private void setupKeyHandling() {
        mainContainer.setFocusTraversable(true);
        mainContainer.setOnKeyPressed(event -> {
            if (!gameActive || isPaused) return;
            
            KeyCode code = event.getCode();
            
            // Player 1 controls (WASD + Space)
            switch (code) {
                case A:
                    player1Board.onMoveLeft();
                    break;
                case D:
                    player1Board.onMoveRight();
                    break;
                case S:
                    player1Board.onMoveDown();
                    break;
                case W:
                    player1Board.onRotate();
                    break;
                case SPACE:
                    player1Board.onHardDrop();
                    break;
            }
            
            // Player 2 controls (Arrow keys + Enter)
            switch (code) {
                case LEFT:
                    player2Board.onMoveLeft();
                    break;
                case RIGHT:
                    player2Board.onMoveRight();
                    break;
                case DOWN:
                    player2Board.onMoveDown();
                    break;
                case UP:
                    player2Board.onRotate();
                    break;
                case ENTER:
                    player2Board.onHardDrop();
                    break;
            }
            
            // ESC for pause/menu
            if (code == KeyCode.ESCAPE) {
                togglePause();
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
                restartGame();
            }
            
            @Override
            public void onSettings() {
                // 설정은 대전 모드에서 제한적으로 제공
            }
            
            @Override
            public void onMainMenu() {
                goToMainMenu();
            }
            
            @Override
            public void onExit() {
                exitGame();
            }
        });
    }
    
    private void resumeGame() {
        isPaused = false;
        menuOverlay.hide();
        startGameLoop();
        mainContainer.requestFocus();
    }
    
    /**
     * 게임 시작
     */
    private void startGame() {
        gameStartTime = System.currentTimeMillis();
        startGameLoop();
    }
    
    /**
     * 게임 루프 시작
     */
    private void startGameLoop() {
        if (gameLoop != null) {
            gameLoop.stop();
        }
        
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (!gameActive || isPaused) return;
                
                // 시간제한 모드 타이머 업데이트
                if (gameMode == VersusGameModeDialog.VersusMode.TIME_LIMIT) {
                    updateTimer();
                }
                
                // 플레이어 1 업데이트
                if (now - lastUpdate1 >= player1Board.getDropInterval()) {
                    player1Board.update();
                    lastUpdate1 = now;
                }
                
                // 플레이어 2 업데이트
                if (now - lastUpdate2 >= player2Board.getDropInterval()) {
                    player2Board.update();
                    lastUpdate2 = now;
                }
                
                // 공격 처리
                processAttacks();
                
                // AttackDisplay 업데이트 (공격 줄이 실제로 추가된 후 반영)
                updateAttackDisplays();
                
                // 게임 종료 조건 체크
                checkGameEnd();
            }
        };
        
        lastUpdate1 = System.nanoTime();
        lastUpdate2 = System.nanoTime();
        gameLoop.start();
    }
    
    /**
     * 타이머 업데이트 (시간제한 모드)
     */
    private void updateTimer() {
        if (timerLabel == null) return;
        
        long elapsed = System.currentTimeMillis() - gameStartTime;
        long remaining = Math.max(0, timeLimitMillis - elapsed);
        
        if (remaining == 0) {
            endGameByTime();
            return;
        }
        
        long minutes = remaining / 60000;
        long seconds = (remaining % 60000) / 1000;
        timerLabel.setText(String.format("⏱ %02d:%02d", minutes, seconds));
        
        // 30초 미만일 때 경고 스타일 적용
        if (remaining < 30000) {
            timerLabel.setStyle("-fx-text-fill: #ff6b6b;" +
                               "-fx-effect: dropshadow(gaussian, rgba(255,107,107,0.8), 15, 0, 0, 0);");
        } else {
            timerLabel.setStyle("-fx-text-fill: #ffeb3b;" +
                               "-fx-effect: dropshadow(gaussian, rgba(255,235,59,0.6), 10, 0, 0, 0);");
        }
    }
    
    /**
     * 공격 처리
     */
    private void processAttacks() {
        // Player 1에게 들어온 공격 처리
        if (!player1AttackQueue.isEmpty()) {
            AttackData attack = player1AttackQueue.poll();
            player1Board.receiveAttackLines(attack.lines);
            Logger.info("Player 1 received " + attack.count + " attack lines");
        }
        
        // Player 2에게 들어온 공격 처리
        if (!player2AttackQueue.isEmpty()) {
            AttackData attack = player2AttackQueue.poll();
            player2Board.receiveAttackLines(attack.lines);
            Logger.info("Player 2 received " + attack.count + " attack lines");
        }
    }
    
    /**
     * AttackDisplay 업데이트 (실제 대기 중인 공격 줄 개수와 동기화)
     */
    private void updateAttackDisplays() {
        // Player 1의 대기 중인 공격 줄 개수와 Display 동기화
        int player1Pending = player1Board.getPendingAttackCount();
        if (player1AttackDisplay.getQueueSize() != player1Pending) {
            player1AttackDisplay.syncWithActualQueue(player1Pending);
        }
        
        // Player 2의 대기 중인 공격 줄 개수와 Display 동기화
        int player2Pending = player2Board.getPendingAttackCount();
        if (player2AttackDisplay.getQueueSize() != player2Pending) {
            player2AttackDisplay.syncWithActualQueue(player2Pending);
        }
    }
    
    /**
     * 게임 종료 조건 체크
     */
    private void checkGameEnd() {
        boolean player1Lost = player1Board.isGameOver();
        boolean player2Lost = player2Board.isGameOver();
        
        if (player1Lost && player2Lost) {
            endGameByScore();
        } else if (player1Lost) {
            endGame(2);
        } else if (player2Lost) {
            endGame(1);
        }
    }
    
    private void endGameByTime() {
        int player1Score = player1Board.getScore();
        int player2Score = player2Board.getScore();
        
        if (player1Score > player2Score) {
            endGame(1);
        } else if (player2Score > player1Score) {
            endGame(2);
        } else {
            endGameByScore();
        }
    }
    
    private void endGameByScore() {
        int player1Score = player1Board.getScore();
        int player2Score = player2Board.getScore();
        
        int winner = player1Score >= player2Score ? 1 : 2;
        endGame(winner);
    }
    
    private void endGame(int winner) {
        gameActive = false;
        if (gameLoop != null) {
            gameLoop.stop();
        }
        
        showVictoryMessage(winner);
        callback.onPlayerWin(winner, player1Board.getScore(), player2Board.getScore());
    }
    
    /**
     * 승리 메시지 표시
     */
    private void showVictoryMessage(int winner) {
        VBox victoryBox = new VBox(30);
        victoryBox.setAlignment(Pos.CENTER);
        victoryBox.setPadding(new Insets(50));
        victoryBox.getStyleClass().add("versus-result-panel");
        victoryBox.setMaxWidth(500);
        
        Label winnerLabel = new Label("Player " + winner + " Wins!");
        winnerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 48));
        winnerLabel.setStyle("-fx-text-fill: " + (winner == 1 ? "#00d4ff" : "#ff6b6b") + ";" +
                            "-fx-effect: dropshadow(gaussian, " + 
                            (winner == 1 ? "rgba(0,212,255,0.8)" : "rgba(255,107,107,0.8)") + 
                            ", 20, 0, 0, 0);");
        
        VBox scoresBox = new VBox(10);
        scoresBox.setAlignment(Pos.CENTER);
        
        Label player1ScoreLabel = new Label("Player 1: " + player1Board.getScore());
        player1ScoreLabel.setFont(Font.font("Courier New", FontWeight.BOLD, 24));
        player1ScoreLabel.setStyle("-fx-text-fill: #00d4ff;");
        
        Label player2ScoreLabel = new Label("Player 2: " + player2Board.getScore());
        player2ScoreLabel.setFont(Font.font("Courier New", FontWeight.BOLD, 24));
        player2ScoreLabel.setStyle("-fx-text-fill: #ff6b6b;");
        
        scoresBox.getChildren().addAll(player1ScoreLabel, player2ScoreLabel);
        
        HBox buttonsBox = new HBox(20);
        buttonsBox.setAlignment(Pos.CENTER);
        
        javafx.scene.control.Button restartButton = new javafx.scene.control.Button("다시 하기");
        restartButton.getStyleClass().add("versus-button");
        restartButton.setPrefWidth(150);
        restartButton.setOnAction(e -> {
            mainContainer.getChildren().remove(victoryBox);
            restartGame();
        });
        
        javafx.scene.control.Button menuButton = new javafx.scene.control.Button("메인 메뉴");
        menuButton.getStyleClass().add("versus-button");
        menuButton.setPrefWidth(150);
        menuButton.setOnAction(e -> goToMainMenu());
        
        buttonsBox.getChildren().addAll(restartButton, menuButton);
        victoryBox.getChildren().addAll(winnerLabel, scoresBox, buttonsBox);
        
        mainContainer.getChildren().add(victoryBox);
    }
    
    /**
     * 라인 클리어 콜백 (공격 시스템)
     */
    private void onLinesCleared(int playerNumber, int linesCleared, List<String[]> clearedLines) {
        if (linesCleared < 2) return;
        
        AttackData attack = new AttackData(clearedLines, linesCleared);
        if (playerNumber == 1) {
            // Player 1이 공격 → Player 2가 받음
            player2AttackQueue.offer(attack);
            player2AttackDisplay.addAttackLines(clearedLines);
            Logger.info("Player 1 sent " + linesCleared + " attack lines to Player 2");
        } else {
            // Player 2가 공격 → Player 1이 받음
            player1AttackQueue.offer(attack);
            player1AttackDisplay.addAttackLines(clearedLines);
            Logger.info("Player 2 sent " + linesCleared + " attack lines to Player 1");
        }
    }
    
    /**
     * 게임 재시작
     */
    public void restartGame() {
        gameActive = true;
        isPaused = false;
        menuOverlay.hide();
        
        player1Board.restart();
        player2Board.restart();
        player1ScorePanel.resetScore();
        player2ScorePanel.resetScore();
        
        player1AttackQueue.clear();
        player2AttackQueue.clear();
        player1AttackDisplay.clear();
        player2AttackDisplay.clear();
        
        gameStartTime = System.currentTimeMillis();
        startGameLoop();
        mainContainer.requestFocus();
    }
    
    private void goToMainMenu() {
        cleanup();
        Stage stage = (Stage) mainContainer.getScene().getWindow();
        Router router = new Router(stage);
        router.showStartMenu();
    }
    
    private void exitGame() {
        cleanup();
        Stage stage = (Stage) mainContainer.getScene().getWindow();
        stage.close();
    }
    
    public StackPane getRoot() {
        return mainContainer;
    }
    
    public void cleanup() {
        if (gameLoop != null) {
            gameLoop.stop();
        }
        
        if (player1Board != null) {
            // PlayerBoard는 cleanup 메서드가 없으므로 제거
        }
        
        if (player2Board != null) {
            // PlayerBoard는 cleanup 메서드가 없으므로 제거
        }
    }
}
