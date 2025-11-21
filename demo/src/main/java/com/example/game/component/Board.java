package com.example.game.component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.example.Router;
import com.example.game.blocks.Block;
import com.example.game.component.GameInputHandler.GameInputCallback;
import com.example.game.component.MenuOverlay.MenuCallback;
import com.example.game.items.BombBlock;
import com.example.game.items.LItem;
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
    protected int cellSize;
    protected int boardWidth;
    protected int boardHeight;

    protected StackPane mainContainer; // 메인 컨테이너 (오버레이 포함)
    protected BorderPane root; // 게임 보드 레이아웃
    protected Canvas canvas;
    protected GraphicsContext gc;
    protected GameLogic gameLogic;
    protected ScorePanel scorePanel;
    protected GameSettings gameSettings;
    protected MenuOverlay menuOverlay; // 메뉴 오버레이 추가
    protected GameInputHandler inputHandler; // 입력 핸들러 추가

    protected AnimationTimer gameLoop; // 게임 루프 타이머
    protected long lastUpdate = 0; // 블록 마지막 업데이트 시간

    protected boolean isPaused = false; // 게임 일시정지 상태
    protected boolean isGameOver = false; // 게임 오버 상태
    protected final long baseDropInterval = 1_000_000_000L; // 1초 (기본 속도)
    protected Animation lineAnimation; // 애니메이션 객체 추가
    protected List<Integer> pendingLinesToClear; // 삭제 대기 중인 줄들
    protected int[][] pendingExplosionCells; // 폭발 대기 중인 셀들 (BombBlock용)
    protected boolean isExplosionAnimation = false; // 폭발 애니메이션 여부

    public Board() {
        // 컴포넌츠 초기화
        gameSettings = GameSettings.getInstance();
        gameLogic = new GameLogic(gameSettings.isItemModeEnabled()); // GameSettings에서 아이템 모드 설정 가져오기
        scorePanel = new ScorePanel();
        menuOverlay = new MenuOverlay(); // 오버레이 초기화
        inputHandler = new GameInputHandler(this); // 입력 핸들러 초기화
        lineAnimation = new Animation(); // 애니메이션 초기화
        pendingLinesToClear = new ArrayList<>();
        pendingExplosionCells = null;

        // // 동적 크기 계산
        // calculateDynamicSizes();

        // // UI 초기화
        // initializeUI();
        // // 키 입력 처리 설정
        // setupKeyHandling();
        // // 게임 루프 시작
        // startGameLoop();
        // // 초기 보드 그리기
        // drawBoard();

        // 화면 크기 변경 리스너 등록
        gameSettings.addWindowSizeChangeListener(this::onWindowSizeChanged);
    }

    // GameInputCallback 구현
    @Override
    public void onMoveLeft() {
        if (lineAnimation.isActive()) {
            return; // 애니메이션 중에는 입력 무시
        }
        gameLogic.moveLeft();
        drawBoard();
    }

    @Override
    public void onMoveRight() {
        if (lineAnimation.isActive()) {
            return; // 애니메이션 중에는 입력 무시
        }
        gameLogic.moveRight();
        drawBoard();
    }

    @Override
    public void onMoveDown() {
        if (lineAnimation.isActive()) {
            return; // 애니메이션 중에는 입력 무시
        }
        handleMoveDown();
        drawBoard();
    }

    @Override
    public void onRotate() {
        if (lineAnimation.isActive()) {
            return; // 애니메이션 중에는 입력 무시
        }
        gameLogic.rotateBlock();
        drawBoard();
    }

    @Override
    public void onHardDrop() {
        if (lineAnimation.isActive()) {
            return; // 애니메이션 중에는 입력 무시
        }
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
    protected void calculateDynamicSizes() {
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
    protected void onWindowSizeChanged() {
        calculateDynamicSizes();
        updateCanvasSize();
        drawBoard();
    }

    // 캔버스 크기 업데이트
    protected void updateCanvasSize() {
        if (canvas != null) {
            canvas.setWidth(boardWidth);
            canvas.setHeight(boardHeight);
        }
    }

    // UI 초기화
    protected void initializeUI() {
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
    protected void setupKeyHandling() {
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

    protected void startGameLoop() {
        if (gameLoop != null) {
            gameLoop.stop();
        }

        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (isPaused || menuOverlay.isVisible()) {
                    return;
                }
                // 애니메이션 업데이트
                boolean animationFinished = false;
                if (lineAnimation.isActive()) {
                    animationFinished = lineAnimation.update(now);
                    drawBoard();
                }
                
                // 애니메이션이 진행 중이면 애니메이션만 업데이트
                if (animationFinished) {  
                    if (isExplosionAnimation && pendingExplosionCells != null) {
                        // 폭발 애니메이션 완료 - 실제 폭발 실행
                        System.out.println(">>> BombBlock: Explosion animation finished, executing explosion");
                        
                        // 폭발 영역의 블록 삭제
                        int[][] board = gameLogic.getBoard();
                        String[][] blockTypes = gameLogic.getBlockTypes();
                        
                        int destroyedCount = 0;
                        for (int[] cell : pendingExplosionCells) {
                            int row = cell[0];
                            int col = cell[1];
                            if (board[row][col] == 1) {
                                board[row][col] = 0;
                                blockTypes[row][col] = null;
                                destroyedCount++;
                            }
                        }
                        
                        System.out.println(">>> BombBlock: Destroyed " + destroyedCount + " blocks");
                        scorePanel.addScoreWithDifficulty(destroyedCount * 10); // 파괴된 블록당 10점
                        
                        // 폭발 후 줄 삭제 체크
                        List<Integer> fullLines = gameLogic.findFullLines();
                        if (!fullLines.isEmpty()) {
                            gameLogic.executeLineClear(fullLines);
                            scorePanel.calculateLineScore(fullLines.size());
                            updateSpeedDisplay();
                        }
                        
                        pendingExplosionCells = null;
                        isExplosionAnimation = false;
                    } else {
                        // 일반 줄 삭제 애니메이션 완료
                        // L-item 줄은 이미 점수를 받았으므로, 꽉 찬 줄만 점수 계산
                        List<Integer> fullLines = gameLogic.findFullLines();
                        List<Integer> fullLinesInPending = new ArrayList<>();
                        for (Integer line : pendingLinesToClear) {
                            if (fullLines.contains(line)) {
                                fullLinesInPending.add(line);
                            }
                        }
                        
                        // 모든 줄 삭제 실행 (L-item 줄 + 꽉 찬 줄)
                        gameLogic.executeLineClear(pendingLinesToClear);
                        
                        // 꽉 찬 줄에 대해서만 추가 점수
                        if (fullLinesInPending.size() > 0) {
                            scorePanel.calculateLineScore(fullLinesInPending.size());
                            updateSpeedDisplay();
                        }
                        pendingLinesToClear.clear();
                    }
                        
                    // 블록이 맨 위에 닿았는지 확인
                    if (gameLogic.isBlockAtTop()) {
                        if (!isGameOver) {
                            isGameOver = true;
                            gameOver();
                        }
                        drawBoard();
                        return;
                    }
        
                    // 다음 블록 생성
                    boolean spawned = gameLogic.spawnNextPiece();
                    if (!spawned && !isGameOver) {
                        isGameOver = true;
                        gameOver();
                    }
                    drawBoard();
                    return;
                }

                if (lineAnimation.isActive()) {
                    return;
                }

                // 일반 게임 로직
                long currentDropInterval = gameLogic.getDropInterval(baseDropInterval);
                if (now - lastUpdate >= currentDropInterval) {
                    handleMoveDown();
                    drawBoard();
                    lastUpdate = now;
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
    protected void handleMoveDown() {
        Block currentBlock = gameLogic.getCurrentBlock();
        boolean isLItemBlock = currentBlock instanceof LItem;
        boolean isBombBlock = currentBlock instanceof BombBlock;
        int lItemRow = -1;
        
        boolean moved = gameLogic.moveDown();

        if (moved) {
            // 블록이 성공적으로 아래로 이동했을 때 점수 증가
            scorePanel.addScore(1);
        } else {
            // BombBlock이 착지했을 경우 폭발 애니메이션
            if (isBombBlock) {
                BombBlock bombBlock = (BombBlock) currentBlock;
                int[][] explosionCells = bombBlock.getExplosionCells(
                    gameLogic.getCurrentY(), 
                    gameLogic.getCurrentX(),
                    GameLogic.HEIGHT,
                    GameLogic.WIDTH
                );
                
                // 폭발 영역의 빈 셀을 임시로 채워서 애니메이션이 제대로 작동하도록 함
                fillExplosionCells(explosionCells);
                
                // 폭발 애니메이션 시작
                pendingExplosionCells = explosionCells;
                isExplosionAnimation = true;
                lineAnimation.start();
                System.out.println(">>> BombBlock: Starting explosion animation with " + explosionCells.length + " cells");
                return; // 애니메이션이 끝날 때까지 대기
            }
            
            // 줄 삭제 체크 - 먼저 꽉 찬 줄 찾기 (L-item 줄 채우기 전에)
            List<Integer> fullLines = gameLogic.findFullLines();
            
            // L-item이 착지했을 경우 L 줄을 애니메이션 대상에 추가
            if (isLItemBlock) {
                LItem lItem = (LItem) currentBlock;
                lItemRow = lItem.getLMarkerAbsoluteRow(gameLogic.getCurrentY());
                System.out.println(">>> L-item landed at row: " + lItemRow);
                
                // L-item 줄의 빈 셀을 임시로 채워서 애니메이션이 제대로 작동하도록 함
                fillEmptyCellsInLine(lItemRow);
            }
            
            List<Integer> linesToAnimate = new ArrayList<>();
            
            // L-item 줄 추가
            if (lItemRow >= 0) {
                linesToAnimate.add(lItemRow);
                System.out.println(">>> Adding L-item row to animate: " + lItemRow);
                scorePanel.addScoreWithDifficulty(100); // L-item 줄 삭제 점수 (난이도 배율 적용)
            }
            
            // 꽉 찬 줄 추가 (L-item 줄과 중복 제거)
            for (Integer line : fullLines) {
                if (!linesToAnimate.contains(line)) {
                    linesToAnimate.add(line);
                    System.out.println(">>> Adding full line to animate: " + line);
                }
            }
            
            // 애니메이션 시작
            if (!linesToAnimate.isEmpty()) {
                pendingLinesToClear = linesToAnimate;
                isExplosionAnimation = false;
                lineAnimation.start();
                return; // 애니메이션이 끝날 때까지 대기
            }

            // 블록이 맨 위에 닿았는지 확인
            if (gameLogic.isBlockAtTop()) {
                if (!isGameOver) {
                    isGameOver = true;
                    gameOver();
                }
                return;
            }

            // 다음 블록 생성
            boolean spawned = gameLogic.spawnNextPiece();
            if (!spawned) {
                if (!isGameOver) {
                    isGameOver = true;
                    gameOver();
                }
            }
        }
    }

    protected void gameOver() {
        if (gameLoop != null) {
            gameLoop.stop();
        }
        // Platform.runLater로 감싸기!
        javafx.application.Platform.runLater(() -> {
        Stage stage = (Stage) mainContainer.getScene().getWindow();
        
        com.example.gameover.GameOverScene.show(stage, scorePanel.getScore());
    });
}

    // 보드 그리기
    protected void drawBoard() {
        Map<String, Color> currentColors = gameSettings.getCurrentColors();

        // 배경 그리기
        gc.setFill(Color.web("#1a1a2e"));
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        drawGrid();
        drawPlacedBlocks(currentColors);

        // 애니메이션이 진행 중이면 애니메이션 그리기
        if (lineAnimation.isActive()) {
            if (isExplosionAnimation && pendingExplosionCells != null) {
                // 폭발 애니메이션 (하늘색)
                lineAnimation.drawExplosion(gc, pendingExplosionCells, gameLogic.getBlockTypes(), 
                                          currentColors, cellSize);
            } else {
                // 일반 줄 삭제 애니메이션 (노란색)
                lineAnimation.draw(gc, pendingLinesToClear, gameLogic.getBlockTypes(), 
                             currentColors, cellSize, GameLogic.WIDTH);
            }
        }else{
            drawCurrentBlock(currentColors);
        }

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
    protected void drawPlacedBlocks(Map<String, Color> colorMap) {
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
                    
                    // Sand 블록인지 확인하여 특별한 스타일로 그리기
                    if ("item-sand".equals(cssClass)) {
                        drawSandCell(col * cellSize, row * cellSize, blockColor);
                    } else {
                        // 일반 셀 그리기
                        drawCell(col * cellSize, row * cellSize, blockColor);
                    }
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
        
        // SandBlock인지 확인
        boolean isSandBlock = currentBlock instanceof com.example.game.items.SandBlock;

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
                    // Sand 블록인지 확인
                    else if (isSandBlock) {
                        // Sand 블록은 점박이 패턴으로 그리기
                        Color sandColor = colorMap.get("item-sand");
                        drawSandCell(drawX, drawY, sandColor);
                    } else {
                        // 일반 셀 그리기
                        drawCell(drawX, drawY, blockColor);
                    }
                }
            }
        }
    }

    // 개별 셀 그리기
    protected void drawCell(double x, double y, Color color) {
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
        gc.setFill(Color.BLACK);
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
        
        // "B" 텍스트 그리기 (검은색)
        gc.setFill(Color.BLACK);
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
    
    // Sand 셀 그리기 (점박이 패턴으로 모래 질감 표현)
    protected void drawSandCell(double x, double y, Color color) {
        // 메인 셀 (Sand 색상 - 흰색)
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
        
        // 점박이 패턴 그리기 (모래 질감)
        gc.setFill(Color.web("#8B7355")); // 갈색 (모래색)
        double dotSize = Math.max(2, cellSize * 0.15); // 점 크기
        
        // 규칙적인 점 패턴 (4x4 그리드로 촘촘하게)
        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 4; col++) {
                double dotX = x + cellSize * 0.15 + col * cellSize * 0.23;
                double dotY = y + cellSize * 0.15 + row * cellSize * 0.23;
                gc.fillOval(dotX, dotY, dotSize, dotSize);
            }
        }
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
    protected void hardDrop() {
        if (!isGameActive()) {
            return;
        }

        System.out.println(">>> Hard drop initiated");

        Block currentBlock = gameLogic.getCurrentBlock();
        boolean isLItemBlock = currentBlock instanceof LItem;
        boolean isBombBlock = currentBlock instanceof BombBlock;

        boolean dropped = false;
        while (gameLogic.moveDown()) {
            dropped = true;
            scorePanel.addScore(1);
        }

        if (!dropped) {
            return;
        }

        // BombBlock이 착지했을 경우 폭발 애니메이션
        if (isBombBlock) {
            BombBlock bombBlock = (BombBlock) currentBlock;
            int[][] explosionCells = bombBlock.getExplosionCells(
                gameLogic.getCurrentY(), 
                gameLogic.getCurrentX(),
                GameLogic.HEIGHT,
                GameLogic.WIDTH
            );
            
            // 폭발 영역의 빈 셀을 임시로 채워서 애니메이션이 제대로 작동하도록 함
            fillExplosionCells(explosionCells);
            
            // 폭발 애니메이션 시작
            pendingExplosionCells = explosionCells;
            isExplosionAnimation = true;
            lineAnimation.start();
            System.out.println(">>> BombBlock: Starting explosion animation (hard drop) with " + explosionCells.length + " cells");
            return; // 애니메이션이 끝날 때까지 대기
        }

        // 줄 삭제 체크 - 먼저 꽉 찬 줄 찾기 (L-item 줄 채우기 전에)
        List<Integer> fullLines = gameLogic.findFullLines();
        
        // L-item이 착지했을 경우 L 줄을 애니메이션 대상에 추가
        int lItemRow = -1;
        if (isLItemBlock) {
            LItem lItem = (LItem) currentBlock;
            lItemRow = lItem.getLMarkerAbsoluteRow(gameLogic.getCurrentY());
            System.out.println(">>> L-item hard-dropped at row: " + lItemRow);
            
            // L-item 줄의 빈 셀을 임시로 채워서 애니메이션이 제대로 작동하도록 함
            fillEmptyCellsInLine(lItemRow);
        }

        List<Integer> linesToAnimate = new ArrayList<>();
        
        // L-item 줄 추가
        if (lItemRow >= 0) {
            linesToAnimate.add(lItemRow);
            System.out.println(">>> Adding L-item row to animate: " + lItemRow);
            scorePanel.addScoreWithDifficulty(100); // L-item 줄 삭제 점수 (난이도 배율 적용)
        }
        
        // 꽉 찬 줄 추가 (L-item 줄과 중복 제거)
        for (Integer line : fullLines) {
            if (!linesToAnimate.contains(line)) {
                linesToAnimate.add(line);
                System.out.println(">>> Adding full line to animate: " + line);
            }
        }
        
        // 애니메이션 시작
        if (!linesToAnimate.isEmpty()) {
            pendingLinesToClear = linesToAnimate;
            isExplosionAnimation = false;
            lineAnimation.start();
            return; // 애니메이션이 끝날 때까지 대기
        }

        if (gameLogic.isBlockAtTop()) {
            if (!isGameOver) {
                isGameOver = true;
                gameOver();
            }
            return;
        }

        // boolean spawned = gameLogic.spawnNextPiece();
        // if (!spawned && !isGameOver) {
        //     isGameOver = true;
        //     gameOver();
        // }
    }

    // 리소스 정리 (게임이 종료될 때 호출)
    public void cleanup() {
        if (gameLoop != null) {
            gameLoop.stop();
        }
        gameSettings.removeWindowSizeChangeListener(this::onWindowSizeChanged);
        scorePanel.cleanup();
    }

    // L-item 줄의 빈 셀을 임시로 채우기 (애니메이션용)
    protected void fillEmptyCellsInLine(int row) {
        if (row < 0 || row >= GameLogic.HEIGHT) {
            return;
        }
        
        int[][] board = gameLogic.getBoard();
        String[][] blockTypes = gameLogic.getBlockTypes();
        
        for (int col = 0; col < GameLogic.WIDTH; col++) {
            if (board[row][col] == 0) {
                // 빈 셀을 임시로 채움 (애니메이션에만 사용)
                board[row][col] = 1;
                blockTypes[row][col] = "block-default"; // 기본 블록 타입
            }
        }
    }
    
    // BombBlock 폭발 영역의 빈 셀을 임시로 채우기 (애니메이션용)
    protected void fillExplosionCells(int[][] explosionCells) {
        int[][] board = gameLogic.getBoard();
        String[][] blockTypes = gameLogic.getBlockTypes();
        
        for (int[] cell : explosionCells) {
            int row = cell[0];
            int col = cell[1];
            
            if (board[row][col] == 0) {
                // 빈 셀을 임시로 채움 (애니메이션에만 사용)
                board[row][col] = 1;
                blockTypes[row][col] = "block-default"; // 기본 블록 타입
            }
        }
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

    public boolean getIsAnimationActive() {
        return lineAnimation.isActive();
    }
    
}