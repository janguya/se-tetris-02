package com.example.game.component;

import java.util.Map;

import com.example.Router;
import com.example.settings.GameSettings;
import com.example.game.blocks.Block;

import javafx.animation.AnimationTimer;
import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class Board {
    // 동적 셀 크기 (화면 크기에 따라 조정됨)
    private int cellSize;
    private int boardWidth;
    private int boardHeight;
    
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
        
        // 동적 크기 계산
        calculateDynamicSizes();
        
        // UI 초기화
        initializeUI();
        // 키 입력 처리 설정
        setupKeyHandling();
        // 게임 루프 시작
        startGameLoop();
        // 초기 보드 그리기
        drawBoard();
        
        // 화면 크기 변경 리스너 등록
        gameSettings.addWindowSizeChangeListener(this::onWindowSizeChanged);
    }
    
    // 동적 크기 계산
    private void calculateDynamicSizes() {
        int windowWidth = gameSettings.getWindowWidth();
        int windowHeight = gameSettings.getWindowHeight();
        
        // 점수판과 패딩을 고려한 게임 보드 영역 계산
        int availableWidth = windowWidth - 200; // 점수판 및 패딩 고려
        int availableHeight = windowHeight - 80; // 상하 패딩 고려
        
        // 가로/세로 비율에 맞춰 셀 크기 계산 (더 제한적인 쪽에 맞춤)
        int cellByWidth = availableWidth / GameLogic.WIDTH;
        int cellByHeight = availableHeight / GameLogic.HEIGHT;
        
        // 최소 15, 최대 40의 셀 크기 제한
        cellSize = Math.max(15, Math.min(40, Math.min(cellByWidth, cellByHeight)));
        
        boardWidth = GameLogic.WIDTH * cellSize;
        boardHeight = GameLogic.HEIGHT * cellSize;
    }
    
    // 화면 크기 변경 콜백
    private void onWindowSizeChanged() {
        calculateDynamicSizes();
        updateCanvasSize();
        drawBoard();
    }
    
    // 캔버스 크기 업데이트
    private void updateCanvasSize() {
        if (canvas != null) {
            canvas.setWidth(boardWidth);
            canvas.setHeight(boardHeight);
        }
    }
    
    // UI 초기화
    private void initializeUI() {
        // 메인 레이아웃 설정
        root = new BorderPane();
        root.getStyleClass().add("game-root");
        
        // 점수판 설정
        scorePanel.getPanel().getStyleClass().add("side-panel");
        canvas = new Canvas(boardWidth, boardHeight);
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
            
            // 커스텀 키 바인딩 사용
            if (code == gameSettings.getKeyBinding("MOVE_LEFT")) {
                // 왼쪽 이동
                gameLogic.moveLeft();
            } else if (code == gameSettings.getKeyBinding("MOVE_RIGHT")) {
                // 오른쪽 이동
                gameLogic.moveRight();
            } else if (code == gameSettings.getKeyBinding("MOVE_DOWN")) {
                // 아래로 이동
                handleMoveDown();
            } else if (code == gameSettings.getKeyBinding("ROTATE")) {
                // 회전
                gameLogic.rotateBlock();
            } else if (code == gameSettings.getKeyBinding("SETTINGS")) {
                // 설정 다이얼로그 표시
                showSettings();
            } else if (code == gameSettings.getKeyBinding("PAUSE")) {
                // 일시정지 토글
                togglePause();
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
        
        if (!moved) {
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
            gc.strokeLine(i * cellSize, 0, i * cellSize, GameLogic.HEIGHT * cellSize);
        }

        // 가로 선
        for (int i = 0; i <= GameLogic.HEIGHT; i++) {
            gc.strokeLine(0, i * cellSize, GameLogic.WIDTH * cellSize, i * cellSize);
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
                    drawCell(col * cellSize, row * cellSize, blockColor);
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
                    int drawX = (currentX + i) * cellSize;
                    int drawY = (currentY + j) * cellSize;
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
        gc.fillRect(x + 1, y + 1, cellSize - 2, cellSize - 2);
        
        // 하이라이트 효과
        gc.setFill(color.brighter());
        gc.fillRect(x + 2, y + 2, cellSize - 4, 3);
        gc.fillRect(x + 2, y + 2, 3, cellSize - 4);

        // 그림자 효과
        gc.setFill(color.darker());
        gc.fillRect(x + 2, y + cellSize - 5, cellSize - 4, 3);
        gc.fillRect(x + cellSize - 5, y + 2, 3, cellSize - 4);
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
    
    // 리소스 정리 (게임이 종료될 때 호출)
    public void cleanup() {
        if (gameLoop != null) {
            gameLoop.stop();
        }
        gameSettings.removeWindowSizeChangeListener(this::onWindowSizeChanged);
        scorePanel.cleanup();
    }
}
