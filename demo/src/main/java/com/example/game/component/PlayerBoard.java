package com.example.game.component;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import com.example.game.blocks.Block;
import com.example.game.items.BombBlock;
import com.example.game.items.LItem;
import com.example.utils.Logger;

import javafx.scene.paint.Color;

/**
 * 대전 모드용 플레이어 보드
 * Board를 확장하여 공격 시스템과 별도의 키 입력 처리를 추가
 */
public class PlayerBoard extends Board {
    
    public interface LineClearCallback {
        void onLinesCleared(int playerNumber, int linesCleared, List<String[]> clearedLines);
    }
    
    private final int playerNumber;
    private final LineClearCallback callback;
    
    // 공격받은 줄 관리
    private Queue<String[]> pendingAttackLines;
    
    public PlayerBoard(int playerNumber, LineClearCallback callback, boolean itemMode) {
        super();
        this.playerNumber = playerNumber;
        this.callback = callback;
        this.pendingAttackLines = new LinkedList<>();
        
        // GameLogic을 아이템 모드로 재초기화
        gameLogic = new GameLogic(itemMode);
        
        // UI 초기화 (캔버스와 GraphicsContext 생성)
        initializeUI();
        
        // 키 입력 핸들러는 설정하지 않음 (VersusBoard에서 직접 처리)
        // 자동 게임 루프도 시작하지 않음
    }
    
    @Override
    protected void setupKeyHandling() {
        // 키 입력 처리는 VersusBoard에서 담당하므로 여기서는 비활성화
    }
    
    @Override
    protected void initializeUI() {
        // UI 초기화는 최소한으로 (캔버스만 생성)
        calculateDynamicSizes();
        canvas = new javafx.scene.canvas.Canvas(boardWidth, boardHeight);
        gc = canvas.getGraphicsContext2D();
        lineAnimation = new Animation();
        pendingLinesToClear = new ArrayList<>();
        
        // 초기 보드 그리기
        drawBoard();
    }
    
    // 자동 게임 루프 대신 수동 업데이트
    public void update() {
        // 애니메이션 업데이트
        if (lineAnimation.isActive()) {
            boolean animationFinished = lineAnimation.update(System.nanoTime());
            drawBoard();
            
            if (animationFinished) {
                handleAnimationFinished();
            }
            return;
        }
        
        // 블록 자동 낙하
        handleMoveDown();
        
        drawBoard();
    }
    
    private void handleAnimationFinished() {
        if (isExplosionAnimation && pendingExplosionCells != null) {
            // 폭발 처리
            executeExplosion();
            pendingExplosionCells = null;
            isExplosionAnimation = false;
        } else {
            // 줄 삭제 처리
            executeLineClear();
        }
        
        // 게임 오버 체크
        if (gameLogic.isBlockAtTop()) {
            isGameOver = true;
            return;
        }
        
        // 공격받은 줄 추가 (다음 블록 생성 전에)
        if (!pendingAttackLines.isEmpty()) {
            addPendingAttackLines();
        }
        
        // 다음 블록 생성
        if (!gameLogic.spawnNextPiece()) {
            isGameOver = true;
        }
    }
    
    private void executeExplosion() {
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
        
        // 폭발 후 줄 삭제 체크
        List<Integer> fullLines = gameLogic.findFullLines();
        if (!fullLines.isEmpty()) {
            gameLogic.executeLineClear(fullLines);
        }
    }
    
    private void executeLineClear() {
        List<Integer> fullLines = gameLogic.findFullLines();
        List<Integer> fullLinesInPending = new ArrayList<>();
        
        for (Integer line : pendingLinesToClear) {
            if (fullLines.contains(line)) {
                fullLinesInPending.add(line);
            }
        }
        
        gameLogic.executeLineClear(pendingLinesToClear);
        pendingLinesToClear.clear();
    }
    
    @Override
    protected void handleMoveDown() {
        Block currentBlock = gameLogic.getCurrentBlock();
        boolean isLItemBlock = currentBlock instanceof LItem;
        boolean isBombBlock = currentBlock instanceof BombBlock;
        
        boolean moved = gameLogic.moveDown();
        
        if (moved) {
            // 블록이 성공적으로 아래로 이동했을 때 점수 증가 (일반 모드와 동일)
            scorePanel.addScore(1);
        } else {
            handleBlockLanded(isLItemBlock, isBombBlock);
        }
    }
    
    private void handleBlockLanded(boolean isLItemBlock, boolean isBombBlock) {
        Block currentBlock = gameLogic.getCurrentBlock();
        
        // BombBlock 처리
        if (isBombBlock) {
            BombBlock bombBlock = (BombBlock) currentBlock;
            int[][] explosionCells = bombBlock.getExplosionCells(
                gameLogic.getCurrentY(), 
                gameLogic.getCurrentX(),
                GameLogic.HEIGHT,
                GameLogic.WIDTH
            );
            
            fillExplosionCells(explosionCells);
            pendingExplosionCells = explosionCells;
            isExplosionAnimation = true;
            lineAnimation.start();
            return;
        }
        
        // 줄 삭제 체크
        List<Integer> fullLines = gameLogic.findFullLines();
        List<Integer> linesToAnimate = new ArrayList<>(fullLines);
        
        // L-item 처리
        if (isLItemBlock) {
            LItem lItem = (LItem) currentBlock;
            int lItemRow = lItem.getLMarkerAbsoluteRow(gameLogic.getCurrentY());
            if (lItemRow >= 0 && !linesToAnimate.contains(lItemRow)) {
                fillEmptyCellsInLine(lItemRow);
                linesToAnimate.add(lItemRow);
            }
        }
        
        if (!linesToAnimate.isEmpty()) {
            Logger.info(">>> Player %d cleared lines: %s", playerNumber, linesToAnimate.toString());
            // 2줄 이상이면 상대방에게 공격
            if (linesToAnimate.size() >= 2) {
                Logger.info(">>> Player %d is attacking with %d lines", playerNumber, linesToAnimate.size());
                List<String[]> attackLines = getClearedLinesForAttack(linesToAnimate);
                callback.onLinesCleared(playerNumber, linesToAnimate.size(), attackLines);
            }
            
            // 애니메이션 시작
            pendingLinesToClear = linesToAnimate;
            isExplosionAnimation = false;
            lineAnimation.start();
            return;
        }
        
        // 게임 오버 체크
        if (gameLogic.isBlockAtTop()) {
            isGameOver = true;
            Logger.info(">>> Player " + playerNumber + " Game Over!");
            return;
        }
        
        // 공격받은 줄 추가 (다음 블록 생성 전에)
        if (!pendingAttackLines.isEmpty()) {
            addPendingAttackLines();
        }
        
        // 다음 블록 생성
        if (!gameLogic.spawnNextPiece()) {
            isGameOver = true;
        }
    }
    
    /**
     * 삭제된 줄을 공격용 데이터로 변환 (현재 블록 부분 제외)
     */
    private List<String[]> getClearedLinesForAttack(List<Integer> lineNumbers) {
        List<String[]> result = new ArrayList<>();
        int[][] board = gameLogic.getBoard();
        String[][] blockTypes = gameLogic.getBlockTypes();
        
        // 줄 번호를 정렬하여 위에서 아래 순서 보장 (작은 번호가 위, 큰 번호가 아래)
        List<Integer> sortedLines = new ArrayList<>(lineNumbers);
        sortedLines.sort(Integer::compareTo);
        
        Logger.info("[Player " + playerNumber + "] Cleared lines for attack (sorted): " + sortedLines);
        
        for (int lineNum : sortedLines) {
            String[] line = new String[GameLogic.WIDTH];
            for (int col = 0; col < GameLogic.WIDTH; col++) {
                if (board[lineNum][col] == 1) {
                    if (isCurrentBlockCell(lineNum, col)) {
                        line[col] = null; // 현재 블록 부분은 빈 칸으로
                    } else {
                        line[col] = "attack-block"; // 공격용 회색 블록
                    }
                } else {
                    line[col] = null;
                }
            }
            result.add(line);
        }

        Logger.info(">>> Player %d cleared lines for attack: %d lines", playerNumber, result.size());
        
        return result;
    }
    
    /**
     * 해당 위치가 현재 블록의 일부인지 확인
     */
    private boolean isCurrentBlockCell(int row, int col) {
        Block currentBlock = gameLogic.getCurrentBlock();
        if (currentBlock == null) return false;
        
        int currentX = gameLogic.getCurrentX();
        int currentY = gameLogic.getCurrentY();
        
        for (int i = 0; i < currentBlock.width(); i++) {
            for (int j = 0; j < currentBlock.height(); j++) {
                if (currentBlock.getShape(i, j) == 1) {
                    if (currentY + j == row && currentX + i == col) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    /**
     * 공격받은 줄 수신
     */
    public void receiveAttackLines(List<String[]> attackLines) {
        for (String[] line : attackLines) {
            pendingAttackLines.offer(line);
        }
        Logger.info("[Player " + playerNumber + "] Received " + attackLines.size() + " attack lines. Queue size: " + pendingAttackLines.size());
    }
    
    /**
     * 대기 중인 공격 줄 개수 반환
     */
    public int getPendingAttackCount() {
        return pendingAttackLines.size();
    }
    
    /**
     * 대기 중인 공격 줄을 보드 하단에 추가
     * 다음 블록 생성 전에 호출됨
     */
    private void addPendingAttackLines() {
        int[][] board = gameLogic.getBoard();
        String[][] blockTypes = gameLogic.getBlockTypes();
        
        int linesToAdd = pendingAttackLines.size();
        Logger.info("[Player " + playerNumber + "] Adding " + linesToAdd + " attack lines to board");
        
        // 기존 블록들을 위로 밀어올리기
        for (int row = 0; row < GameLogic.HEIGHT - linesToAdd; row++) {
            for (int col = 0; col < GameLogic.WIDTH; col++) {
                int sourceRow = row + linesToAdd;
                board[row][col] = board[sourceRow][col];
                blockTypes[row][col] = blockTypes[sourceRow][col];
            }
        }
        
        // 큐에서 poll한 줄들을 임시 리스트에 저장
        List<String[]> tempLines = new ArrayList<>();
        while (!pendingAttackLines.isEmpty()) {
            tempLines.add(pendingAttackLines.poll());
        }
        
        Logger.info("[Player " + playerNumber + "] Received attack lines count: " + tempLines.size());
        
        // 보드 하단에 추가 (tempLines[0]이 맨 위에, tempLines[n-1]이 맨 아래에)
        // 예: tempLines = [line18, line19] -> line18이 보드 18번에, line19가 19번에
        int startRow = GameLogic.HEIGHT - tempLines.size();
        for (int i = 0; i < tempLines.size(); i++) {
            int targetRow = startRow + i;
            String[] attackLine = tempLines.get(i);
            
            Logger.info("[Player " + playerNumber + "] Adding attack line " + i + " to row " + targetRow);
            
            for (int col = 0; col < GameLogic.WIDTH; col++) {
                if (attackLine[col] != null) {
                    board[targetRow][col] = 1;
                    blockTypes[targetRow][col] = "attack-block";
                } else {
                    board[targetRow][col] = 0;
                    blockTypes[targetRow][col] = null;
                }
            }
        }
        
    }
    
    /**
     * 대기 중인 공격 줄 미리보기 그리기
     */
    @Override
    protected void drawBoard() {
        super.drawBoard();
        
        // 공격 줄 미리보기 (반투명 빨간색)
        if (!pendingAttackLines.isEmpty()) {
            gc.setFill(Color.color(1, 0, 0, 0.3));
            int previewLines = Math.min(3, pendingAttackLines.size());
            
            for (int i = 0; i < previewLines; i++) {
                int y = (GameLogic.HEIGHT - 1 - i) * cellSize;
                gc.fillRect(0, y, GameLogic.WIDTH * cellSize, cellSize);
            }
        }
    }
    
    @Override
    protected void drawPlacedBlocks(Map<String, Color> colorMap) {
        // 공격받은 블록용 회색 추가
        colorMap.put("attack-block", Color.GRAY);
        
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
                    
                    // Attack 블록인지 확인 (회색, 대전 모드 전용)
                    if ("attack-block".equals(cssClass)) {
                        drawAttackCell(col * cellSize, row * cellSize, blockColor);
                    }
                    // Sand 블록인지 확인하여 특별한 스타일로 그리기
                    else if ("item-sand".equals(cssClass)) {
                        drawSandCell(col * cellSize, row * cellSize, blockColor);
                    } else {
                        // 일반 셀 그리기
                        drawCell(col * cellSize, row * cellSize, blockColor);
                    }
                }
            }
        }
    }
    
    /**
     * 공격 블록 셀 그리기 (회색, 대전 모드 전용)
     */
    private void drawAttackCell(double x, double y, Color color) {
        // 메인 셀 (회색)
        gc.setFill(color);
        gc.fillRect(x + 1, y + 1, cellSize - 2, cellSize - 2);

        // 가벼운 하이라이트
        gc.setFill(color.brighter());
        gc.fillRect(x + 2, y + 2, cellSize - 4, 2);
        gc.fillRect(x + 2, y + 2, 2, cellSize - 4);

        // 가벼운 그림자
        gc.setFill(color.darker());
        gc.fillRect(x + 2, y + cellSize - 4, cellSize - 4, 2);
        gc.fillRect(x + cellSize - 4, y + 2, 2, cellSize - 4);
        
        // 공격 블록 표시를 위한 대각선 패턴
        gc.setStroke(color.darker().darker());
        gc.setLineWidth(1);
        gc.strokeLine(x + 3, y + 3, x + cellSize - 3, y + cellSize - 3);
        gc.strokeLine(x + cellSize - 3, y + 3, x + 3, y + cellSize - 3);
    }
    
    // 수동 조작 메서드들
    @Override
    public void onMoveLeft() {
        if (lineAnimation.isActive()) return;
        gameLogic.moveLeft();
        drawBoard();
    }
    
    @Override
    public void onMoveRight() {
        if (lineAnimation.isActive()) return;
        gameLogic.moveRight();
        drawBoard();
    }
    
    @Override
    public void onMoveDown() {
        if (lineAnimation.isActive()) return;
        handleMoveDown();
        drawBoard();
    }
    
    @Override
    public void onRotate() {
        if (lineAnimation.isActive()) return;
        gameLogic.rotateBlock();
        drawBoard();
    }
    
    @Override
    public void onHardDrop() {
        if (lineAnimation.isActive()) return;

        Block currentBlock = gameLogic.getCurrentBlock();
        boolean isLItemBlock = currentBlock instanceof LItem;
        boolean isBombBlock = currentBlock instanceof BombBlock;

        // 하드 드롭 실행 (떨어진 거리만큼 점수 추가)
        performHardDrop();

        handleBlockLanded(isLItemBlock, isBombBlock);
        drawBoard();
    }
    
    /**
     * 하드 드롭 실행 - 떨어진 거리만큼 점수 추가
     */
    private void performHardDrop() {
        boolean dropped = false;
        while (gameLogic.moveDown()) {
            dropped = true;
            scorePanel.addScore(1); // 한 칸당 1점
        }
    }
    
    // Getters
    public javafx.scene.canvas.Canvas getCanvas() {
        return canvas;
    }
    
    public int getScore() {
        // ScorePanel의 실제 점수 반환
        return scorePanel != null ? scorePanel.getScore() : 0;
    }
    
    @Override
    public boolean isGameActive() {
        return !isGameOver;
    }
    
    public boolean isGameOver() {
        return isGameOver || gameLogic.isGameOver();
    }
    
    public long getDropInterval() {
        return gameLogic.getDropInterval(baseDropInterval);
    }
    
    /**
     * 애니메이션 활성 상태 확인 (VersusBoard에서 매 프레임 업데이트 여부 결정)
     */
    public boolean isAnimationActive() {
        return lineAnimation != null && lineAnimation.isActive();
    }
    
    /**
     * AI가 사용할 수 있는 getter 메서드들
     */
    public GameLogic getGameLogic() {
        return gameLogic;
    }
    
    public Block getCurrentBlock() {
        return gameLogic.getCurrentBlock();
    }
    
    /**
     * 게임 재시작
     */
    public void restart() {
        gameLogic.resetGame();
        pendingAttackLines.clear();
        pendingLinesToClear.clear();
        isGameOver = false;
        isExplosionAnimation = false;
        pendingExplosionCells = null;
        drawBoard();
    }
}
