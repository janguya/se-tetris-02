package com.example.game.component;

import java.util.Map;

import com.example.Router;
import com.example.game.blocks.Block;
import com.example.settings.GameSettings;

import javafx.animation.AnimationTimer;
import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class Board {
    public static final int CELL_SIZE = 25;
    
    private BorderPane root; // 메인 레이아웃
    private Canvas canvas; // 게임 보드용 캔버스
    private GraphicsContext gc; // 그래픽 컨텍스트
    private GameLogic gameLogic; // 게임 로직 인스턴스
    private ScorePanel scorePanel; // 점수판 컴포넌트
    private GameSettings gameSettings; // 게임 설정 인스턴스
    
    private AnimationTimer gameLoop; // 게임 루프 타이머
    private long lastUpdate = 0; // 블록 마지막 업데이트 시간
    private long dropInterval = 1_000_000_000L; // 1초
    private boolean isPaused = false; // 게임 일시정지 상태
    
    public Board() {
        // 컴포넌츠 초기화
        gameSettings = GameSettings.getInstance();
        gameLogic = new GameLogic();
        scorePanel = new ScorePanel();
        
        // UI 초기화
        initializeUI();
        // 키 입력 처리 설정
        setupKeyHandling();
        // 게임 루프 시작
        startGameLoop();
        // 초기 보드 그리기
        drawBoard();
    }
    
    // UI 초기화
    private void initializeUI() {
        // 메인 레이아웃 설정
        root = new BorderPane();
        root.getStyleClass().add("game-root");
        
        // 점수판 설정
        scorePanel.getPanel().getStyleClass().add("side-panel");
        canvas = new Canvas(GameLogic.WIDTH * CELL_SIZE, GameLogic.HEIGHT * CELL_SIZE);
        gc = canvas.getGraphicsContext2D();

        // 레이아웃 설정
        root.setCenter(canvas);
        root.setRight(scorePanel.getPanel());
        root.setPadding(new Insets(20));
    }
    
    // 키 입력 처리 설정
    private void setupKeyHandling() {
        root.setFocusTraversable(true);
        root.setOnKeyPressed(event -> {
            if (isPaused) return;
            if (gameLogic.isGameOver()) return;
            
            KeyCode code = event.getCode();
            switch (code) {
                case LEFT:
                // 왼쪽 이동
                    gameLogic.moveLeft();
                    break;
                case RIGHT:
                // 오른쪽 이동
                    gameLogic.moveRight();
                    break;
                case DOWN:
                // 아래로 이동
                    handleMoveDown();
                    break;
                case UP:
                // 회전
                    gameLogic.rotateBlock();
                    break;
                case ESCAPE:
                // 설정 다이얼로그 표시
                    showSettings();
                    break;
                case SPACE:
                // 일시정지 토글
                    togglePause();
                    break;
            }
            drawBoard();
        });
    }
    
    // 게임 루프 시작
    private void startGameLoop() {
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (isPaused) return;
                if (gameLogic.isGameOver()) return;
                
                // 일정 시간마다 블록 자동 하강
                if (now - lastUpdate >= getAdjustedDropInterval()) {
                    // 블록 자동 하강 처리
                    handleMoveDown();
                    // 보드 다시 그리기
                    drawBoard();
                    lastUpdate = now;
                }
            }
        };
        gameLoop.start();
    }
    
    // 블록 아래로 이동 처리
    private void handleMoveDown() {
        boolean moved = gameLogic.moveDown();

        if(moved) {
            // 블록이 성공적으로 아래로 이동했을 때 점수 증가
            scorePanel.addScore(1);
            return;
        }
        else {
            // 라인 제거 및 점수 계산
            int linesCleared = gameLogic.clearLines();
            if (linesCleared > 0) {
                scorePanel.calculateLineScore(linesCleared);
            }
            
            // 게임 오버 체크
            if (gameLogic.isGameOver()) {
                gameOver();
            }
        }
    }
    
    // 레벨에 따른 떨어지는 속도 조정
    private long getAdjustedDropInterval() {
        // 레벨이 올라갈수록 떨어지는 속도 빨라짐
        int level = scorePanel.getLevel();
        // 최소 속도 제한
        return Math.max(100_000_000L, dropInterval - (level - 1) * 100_000_000L);
    }
    
    // 일시정지 토글
    private void togglePause() {
        isPaused = !isPaused;
    }
    
    // 설정 다이얼로그 표시
    private void showSettings() {
        isPaused = true;
        // get current stage and size from root's scene
        Stage stage = (Stage) root.getScene().getWindow();

        Router router = new Router(stage);
        router.showSettings(this::onSettingsChanged);
    }
    
    // 설정 변경 후 콜백
    private void onSettingsChanged() {
        isPaused = false;
        drawBoard();
    }
    
    // 게임 오버 처리
    private void gameOver() {
        gameLoop.stop();
        // 화면에 점수 표시
        gc.setFill(Color.color(0, 0, 0, 0.7));
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.setFill(Color.WHITE);
        gc.fillText("GAME OVER", canvas.getWidth() / 2 - 40, canvas.getHeight() / 2);
        gc.fillText("Final Score: " + scorePanel.getScore(), canvas.getWidth() / 2 - 50, canvas.getHeight() / 2 + 30);
    }
    
    // 보드 그리기
    private void drawBoard() {
        Map<String, Color> currentColors = gameSettings.getCurrentColors();
        
        // 배경 그리기
        gc.setFill(Color.web("#1a1a2e"));
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        
        // 그리드 및 블록 그리기
        drawGrid();
        drawPlacedBlocks(currentColors);
        drawCurrentBlock(currentColors);

        // 스코어 패널의 다음 블록 업데이트
        scorePanel.updateNextBlock(gameLogic.getNextBlock());


        // 게임 종료 체크
        if (gameLogic.isGameOver()) {
            drawGameOverOverlay();
        }

        // 일시정지 오버레이
        if (isPaused) {
            drawPauseOverlay();
        }
        if (!isPaused && !gameLogic.isGameOver() && (gameLoop != null)) {
            gameLoop.start();
        }
    }
    
    // 그리드 그리기
    private void drawGrid() {
        gc.setStroke(Color.web("#16213e"));
        gc.setLineWidth(1);
        
        // 세로 선
        for (int i = 0; i <= GameLogic.WIDTH; i++) {
            gc.strokeLine(i * CELL_SIZE, 0, i * CELL_SIZE, GameLogic.HEIGHT * CELL_SIZE);
        }

        // 가로 선
        for (int i = 0; i <= GameLogic.HEIGHT; i++) {
            gc.strokeLine(0, i * CELL_SIZE, GameLogic.WIDTH * CELL_SIZE, i * CELL_SIZE);
        }
    }
    
    // 놓여진 블록 그리기
    private void drawPlacedBlocks(Map<String, Color> colorMap) {
        // 보드 상태 가져오기
        int[][] board = gameLogic.getBoard();
        String[][] blockTypes = gameLogic.getBlockTypes();
        
        // 놓여진 블록 그리기
        for (int row = 0; row < GameLogic.HEIGHT; row++) {
            for (int col = 0; col < GameLogic.WIDTH; col++) {
                if (board[row][col] == 1) {
                    // 블록 색상 결정
                    String cssClass = blockTypes[row][col];
                    // cssClass에 해당하는 색상을 가져오고, 없으면 기본 색상 사용
                    Color blockColor = colorMap.getOrDefault(cssClass, colorMap.get("block-default"));
                    // 셀 그리기
                    drawCell(col * CELL_SIZE, row * CELL_SIZE, blockColor);
                }
            }
        }
    }
    
    // 현재 떨어지는 블록 그리기
    private void drawCurrentBlock(Map<String, Color> colorMap) {
        // 현재 블록 정보 가져오기
        Block currentBlock = gameLogic.getCurrentBlock();
        if (currentBlock == null) return;
        
        // 블록 색상 결정
        Color blockColor = colorMap.get(currentBlock.getCssClass());
        int currentX = gameLogic.getCurrentX();
        int currentY = gameLogic.getCurrentY();
        
        // 현재 블록 그리기
        for (int i = 0; i < currentBlock.width(); i++) {
            for (int j = 0; j < currentBlock.height(); j++) {
                if (currentBlock.getShape(i, j) == 1) {
                    // 셀 위치 계산
                    int drawX = (currentX + i) * CELL_SIZE;
                    int drawY = (currentY + j) * CELL_SIZE;
                    // 셀 그리기
                    drawCell(drawX, drawY, blockColor);
                }
            }
        }
    }
    
    // 개별 셀 그리기
    private void drawCell(double x, double y, Color color) {
        // 메인 셀
        gc.setFill(color);
        gc.fillRect(x + 1, y + 1, CELL_SIZE - 2, CELL_SIZE - 2);
        
        // 하이라이트 효과
        gc.setFill(color.brighter());
        gc.fillRect(x + 2, y + 2, CELL_SIZE - 4, 3);
        gc.fillRect(x + 2, y + 2, 3, CELL_SIZE - 4);

        // 그림자 효과
        gc.setFill(color.darker());
        gc.fillRect(x + 2, y + CELL_SIZE - 5, CELL_SIZE - 4, 3);
        gc.fillRect(x + CELL_SIZE - 5, y + 2, 3, CELL_SIZE - 4);
    }
    
    // 게임 종료: 오버레이 그리기
    private void drawGameOverOverlay() {
        // 반투명 오버레이
        gc.setFill(Color.color(0, 0, 0, 0.7));
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        
        // 게임 종료 텍스트
        gc.setFill(Color.WHITE);
        gc.fillText("GAME OVER", canvas.getWidth() / 2 - 40, canvas.getHeight() / 2);
        gc.fillText("Final Score: " + scorePanel.getScore(), canvas.getWidth() / 2 - 50, canvas.getHeight() / 2 + 30);
    }

    // 일시정지 오버레이 그리기
    private void drawPauseOverlay() {
        // 반투명 오버레이
        gc.setFill(Color.color(0, 0, 0, 0.7));
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        
        // 일시정지 텍스트
        gc.setFill(Color.WHITE);
        gc.fillText("PAUSED", canvas.getWidth() / 2 - 30, canvas.getHeight() / 2);
    }
    
    // 메인 레이아웃 반환
    public BorderPane getRoot() {
        return root;
    }
    
    // 게임 재시작
    public void restartGame() {
        gameLogic.resetGame();
        scorePanel.resetScore();
        isPaused = false;
        if (gameLoop != null) {
            gameLoop.stop();
        }
        startGameLoop();
        drawBoard();
    }
}
