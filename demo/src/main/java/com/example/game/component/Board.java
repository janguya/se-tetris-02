package com.example.game.component;

import java.util.Map;

import com.example.Router;
import com.example.game.blocks.Block;
import com.example.game.component.GameInputHandler.GameInputCallback;
import com.example.game.component.MenuOverlay.MenuCallback;
import com.example.game.items.LItem;
import com.example.game.items.BombBlock;
import com.example.settings.GameSettings;

import javafx.animation.AnimationTimer;
import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class Board implements GameInputCallback {

    // 동적 셀 크기 (화면 크기에 따라 조정됨)
    private int cellSize;
    private int boardWidth;
    private int boardHeight;

    private StackPane mainContainer; // 메인 컨테이너 (오버레이 포함)
    private BorderPane root; // 게임 보드 레이아웃
    private Canvas canvas;
    private GraphicsContext gc;
    private GameLogic gameLogic;
    private ScorePanel scorePanel;
    private GameSettings gameSettings;
    private MenuOverlay menuOverlay; // 메뉴 오버레이 추가
    private GameInputHandler inputHandler; // 입력 핸들러 추가

    private AnimationTimer gameLoop; // 게임 루프 타이머
    private long lastUpdate = 0; // 블록 마지막 업데이트 시간

    private boolean isPaused = false; // 게임 일시정지 상태
    private boolean isGameOver = false; // 게임 오버 상태

    private final long baseDropInterval = 1_000_000_000L; // 1초 (기본 속도)

    public Board() {
        // 컴포넌츠 초기화
        gameSettings = GameSettings.getInstance();
        gameLogic = new GameLogic(gameSettings.isItemModeEnabled()); // GameSettings에서 아이템 모드 설정 가져오기
        scorePanel = new ScorePanel();
        menuOverlay = new MenuOverlay(); // 오버레이 초기화
        inputHandler = new GameInputHandler(this); // 입력 핸들러 초기화

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

    // GameInputCallback 구현
    @Override
    public void onMoveLeft() {
        gameLogic.moveLeft();
        drawBoard();
    }

    @Override
    public void onMoveRight() {
        gameLogic.moveRight();
        drawBoard();
    }

    @Override
    public void onMoveDown() {
        handleMoveDown();
        drawBoard();
    }

    @Override
    public void onRotate() {
        gameLogic.rotateBlock();
        drawBoard();
    }

    @Override
    public void onHardDrop() {
        hardDrop();
        drawBoard();
    }

    @Override
    public void onPause() {
        togglePause();
    }

// togglePause() 메서드 추가
    private void togglePause() {
        if (isPaused || menuOverlay.isVisible()) {
            // 현재 일시정지 상태 또는 메뉴가 열려있음 → 재개
            resumeGame();
        } else {
            // 현재 게임 중 → 일시정지 및 메뉴 표시
            pauseGame();
        }
    }

    @Override
    public void onSettings() {
        // 메뉴가 열려있으면 닫기
        if (menuOverlay.isVisible()) {
            resumeGame();
            return;
        }

        // 게임 오버 상태면 게임 오버 메뉴 표시
        if (gameLogic.isGameOver()) {
            showGameOverMenu();
            return;
        }

        // 그 외의 경우는 기존 ESC 키 처리
        handleEscapeKey();
    }

    @Override
    public boolean isGameActive() {
        return !isPaused && !isGameOver && !gameLogic.isGameOver();
    }

    @Override
    public boolean isMenuVisible() {
        return menuOverlay.isVisible();
    }

    // isPaused() 메서드 추가
    @Override
    public boolean isPaused() {
        return isPaused;
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
        // 메인 컨테이너 생성 (오버레이를 위한 StackPane)
        mainContainer = new StackPane();

        // 게임 보드 레이아웃
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

        // 메인 컨테이너에 게임 보드와 오버레이 추가
        mainContainer.getChildren().addAll(root, menuOverlay.getOverlay());
    }

    // 키 입력 처리 설정
    private void setupKeyHandling() {
        mainContainer.setFocusTraversable(true);
        mainContainer.setOnKeyPressed(event -> {
            // 입력 핸들러에게 위임
            boolean handled = inputHandler.handleKeyPressed(event);

            if (handled) {
                event.consume(); // 이벤트 처리됨을 표시
            }
        });
    }

    // ESC 키 처리
    private void handleEscapeKey() {
        if (gameLogic.isGameOver()) {
            showGameOverMenu();
        } else if (isPaused || menuOverlay.isVisible()) {
            resumeGame();
        } else {
            pauseGame();
        }
    }

    // 게임 일시정지 및 메뉴 표시
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
                showSettingsMenu();
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

    // 게임 재개
    private void resumeGame() {
        isPaused = false;
        menuOverlay.hide();
        if (!gameLogic.isGameOver()) {
            startGameLoop();
        }
        mainContainer.requestFocus();
    }

    // 게임 오버 메뉴 표시
    private void showGameOverMenu() {
        menuOverlay.showGameOverMenu(new MenuCallback() {
            @Override
            public void onResume() {
                // 게임 오버에서는 재개 불가
            }

            @Override
            public void onRestart() {
                restartGame();
            }

            @Override
            public void onSettings() {
                showSettingsMenu();
            }

            @Override
            public void onMainMenu() {
                goToMainMenu();
            }

            @Override
            public void onExit() {
                exitGame();
            }
        }, scorePanel.getScore(), gameSettings.isItemModeEnabled(), gameSettings.getDifficulty());
    }

    // 설정 메뉴 표시
    private void showSettingsMenu() {
        menuOverlay.showSettingsMenu(new MenuCallback() {
            @Override
            public void onResume() {
                if (gameLogic.isGameOver()) {
                    showGameOverMenu();
                } else {
                    resumeGame();
                }
            }

            @Override
            public void onRestart() {
                restartGame();
            }

            @Override
            public void onSettings() {
                // 이미 설정 메뉴에 있음
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

    // 메인 메뉴로 이동
    private void goToMainMenu() {
        if (gameLoop != null) {
            gameLoop.stop();
        }

        Stage stage = (Stage) mainContainer.getScene().getWindow();
        Router router = new Router(stage);
        router.showStartMenu();
    }

    // 게임 종료
    private void exitGame() {
        if (gameLoop != null) {
            gameLoop.stop();
        }

        Stage stage = (Stage) mainContainer.getScene().getWindow();
        stage.close();
    }

    private void startGameLoop() {
        if (gameLoop != null) {
            gameLoop.stop();
        }

        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (isPaused || menuOverlay.isVisible()) {
                    return;
                }

                // 동적으로 계산된 드롭 간격 사용
                long currentDropInterval = gameLogic.getDropInterval(baseDropInterval);

                if (now - lastUpdate >= currentDropInterval) {
                    handleMoveDown();
                    // 보드 다시 그리기
                    drawBoard();
                    lastUpdate = now;

                    // 속도 정보 업데이트
                    updateSpeedDisplay();
                }
            }
        };
        lastUpdate = System.nanoTime();
        gameLoop.start();
    }

    // 속도 표시 업데이트
    private void updateSpeedDisplay() {
        double speedMultiplier = gameLogic.getSpeedMultiplier();
        int speedLevel = gameLogic.getSpeedLevel();
        scorePanel.updateSpeed(speedMultiplier, speedLevel);
    }

    // 블록 아래로 이동 처리
    private void handleMoveDown() {
        boolean moved = gameLogic.moveDown();

        if (moved) {
            // 블록이 성공적으로 아래로 이동했을 때 점수 증가
            scorePanel.addScore(1);
        } else {

            // 라인 제거 및 점수 계산
            int linesCleared = gameLogic.clearLines();
            if (linesCleared > 0) {
                scorePanel.calculateLineScore(linesCleared);
                // 속도가 변경되었을 수 있으므로 업데이트
                updateSpeedDisplay();
            }

            // 블록이 맨 위에 닿았는지 확인
            if (gameLogic.isBlockAtTop()) {

                if (!isGameOver) {
                    isGameOver = true;
                    gameOver();
                }
                return;
            }

            // 게임 오버 체크
            boolean spawned = gameLogic.spawnNextPiece();
            if (!spawned) {
                if (!isGameOver) {
                    isGameOver = true;
                    gameOver();
                }
            }
        }
    }

    private void gameOver() {
        if (gameLoop != null) {
            gameLoop.stop();
        }

        // 현재 Stage 구해와서 GameOverScene 호출
        Stage stage = (Stage) root.getScene().getWindow();
        com.example.gameover.GameOverScene.show(stage, scorePanel.getScore());

    }

    // 보드 그리기
    private void drawBoard() {
        Map<String, Color> currentColors = gameSettings.getCurrentColors();

        // 배경 그리기
        gc.setFill(Color.web("#1a1a2e"));
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        drawGrid();
        drawPlacedBlocks(currentColors);
        drawCurrentBlock(currentColors);

        scorePanel.updateNextBlock(gameLogic.getNextBlock());

        // 일시정지 오버레이
        if (isPaused && !menuOverlay.isVisible()) {
            drawPauseOverlay();
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
        if (currentBlock == null) {
            return;
        }

        // 블록 색상 결정
        Color blockColor = colorMap.get(currentBlock.getCssClass());
        int currentX = gameLogic.getCurrentX();
        int currentY = gameLogic.getCurrentY();
        
        // LItem인지 확인
        boolean isLItem = currentBlock instanceof LItem;
        LItem lItem = isLItem ? (LItem) currentBlock : null;
        
        // BombBlock인지 확인
        boolean isBombBlock = currentBlock instanceof BombBlock;
        BombBlock bombBlock = isBombBlock ? (BombBlock) currentBlock : null;

        // 현재 블록 그리기
        for (int i = 0; i < currentBlock.width(); i++) {
            for (int j = 0; j < currentBlock.height(); j++) {
                if (currentBlock.getShape(i, j) == 1) {
                    // 셀 위치 계산
                    int drawX = (currentX + i) * cellSize;
                    int drawY = (currentY + j) * cellSize;
                    
                    // L 마커가 있는 셀인지 확인
                    if (isLItem && lItem.hasLMarker(j, i)) {
                        // L 마커 셀은 특별한 색상으로 그리고 "L" 텍스트 추가
                        Color lMarkerColor = colorMap.get("item-lmarker");
                        drawLMarkerCell(drawX, drawY, lMarkerColor);
                    }
                    // B 마커가 있는 셀인지 확인
                    else if (isBombBlock && bombBlock.hasBMarker(j, i)) {
                        // B 마커 셀은 검은색으로 그리고 "B" 텍스트 추가
                        Color bMarkerColor = colorMap.get("item-bmarker");
                        drawBMarkerCell(drawX, drawY, bMarkerColor);
                    }
                    else {
                        // 일반 셀 그리기
                        drawCell(drawX, drawY, blockColor);
                    }
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
    
    // L 마커 셀 그리기 (특별한 스타일)
    private void drawLMarkerCell(double x, double y, Color color) {
        // 메인 셀 (L 마커 색상)
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
        
        // "L" 텍스트 그리기
        gc.setFill(Color.WHITE);
        gc.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, cellSize * 0.6));
        
        // 텍스트 중앙 정렬을 위한 계산
        javafx.scene.text.Text tempText = new javafx.scene.text.Text("L");
        tempText.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, cellSize * 0.6));
        double textWidth = tempText.getBoundsInLocal().getWidth();
        double textHeight = tempText.getBoundsInLocal().getHeight();
        
        double textX = x + (cellSize - textWidth) / 2;
        double textY = y + (cellSize + textHeight) / 2 - 2;
        
        gc.fillText("L", textX, textY);
    }
    
    // B 마커 셀 그리기 (폭탄 마커)
    private void drawBMarkerCell(double x, double y, Color color) {
        // 메인 셀 (B 마커 색상 - 검은색)
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
        
        // "B" 텍스트 그리기 (흰색)
        gc.setFill(Color.WHITE);
        gc.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, cellSize * 0.6));
        
        // 텍스트 중앙 정렬을 위한 계산
        javafx.scene.text.Text tempText = new javafx.scene.text.Text("B");
        tempText.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, cellSize * 0.6));
        double textWidth = tempText.getBoundsInLocal().getWidth();
        double textHeight = tempText.getBoundsInLocal().getHeight();
        
        double textX = x + (cellSize - textWidth) / 2;
        double textY = y + (cellSize + textHeight) / 2 - 2;
        
        gc.fillText("B", textX, textY);
    }

    // 메인 컨테이너 반환 (오버레이 포함)
    public StackPane getRoot() {
        return mainContainer;
    }

    // 게임 재시작
    public void restartGame() {
        if (gameLoop != null) {
            gameLoop.stop();
        }

        gameLogic.resetGame();
        scorePanel.resetScore();
        isPaused = false;
        isGameOver = false;
        menuOverlay.hide();

        startGameLoop();
        drawBoard();
        mainContainer.requestFocus();
    }

    // 하드 드롭 (블록을 즉시 바닥까지 떨어뜨리기)
    private void hardDrop() {
        if (!isGameActive()) {
            return;
        }

        boolean dropped = false;
        while (gameLogic.moveDown()) {
            dropped = true;
            scorePanel.addScore(1);
        }

        if (!dropped) {
            return;
        }

        int linesCleared = gameLogic.clearLines();
        if (linesCleared > 0) {
            scorePanel.calculateLineScore(linesCleared);
            updateSpeedDisplay();
        }

        if (gameLogic.isBlockAtTop()) {
            if (!isGameOver) {
                isGameOver = true;
                gameOver();
            }
            return;
        }

        boolean spawned = gameLogic.spawnNextPiece();
        if (!spawned && !isGameOver) {
            isGameOver = true;
            gameOver();
        }
    }

    // 리소스 정리 (게임이 종료될 때 호출)
    public void cleanup() {
        if (gameLoop != null) {
            gameLoop.stop();
        }
        gameSettings.removeWindowSizeChangeListener(this::onWindowSizeChanged);
        scorePanel.cleanup();
    }

    // 일시정지 오버레이 그리기
    private void drawPauseOverlay() {
        // 반투명 배경
        gc.setFill(Color.color(0, 0, 0, 0.7));
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // "PAUSED" 텍스트
        gc.setFill(Color.WHITE);
        gc.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 24));

        String pauseText = "PAUSED";
        javafx.scene.text.Text tempText = new javafx.scene.text.Text(pauseText);
        tempText.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 24));
        double textWidth = tempText.getBoundsInLocal().getWidth();
        double textHeight = tempText.getBoundsInLocal().getHeight();

        double x = (canvas.getWidth() - textWidth) / 2;
        double y = (canvas.getHeight() + textHeight) / 2;

        gc.fillText(pauseText, x, y);

        // 안내 메시지
        gc.setFont(javafx.scene.text.Font.font("Arial", 14));
        String instructionText = "Press ESC to resume";
        javafx.scene.text.Text tempInstruction = new javafx.scene.text.Text(instructionText);
        tempInstruction.setFont(javafx.scene.text.Font.font("Arial", 14));
        double instructionWidth = tempInstruction.getBoundsInLocal().getWidth();

        double instructionX = (canvas.getWidth() - instructionWidth) / 2;
        double instructionY = y + 40;

        gc.setFill(Color.LIGHTGRAY);
        gc.fillText(instructionText, instructionX, instructionY);
    }

}
