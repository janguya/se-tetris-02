package com.example.game.component;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import com.example.game.blocks.Block;
import com.example.game.items.BombBlock;
import com.example.game.items.LItem;

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
    private boolean blockLanded = false;
    
    public PlayerBoard(int playerNumber, LineClearCallback callback, boolean itemMode) {
        super();
        this.playerNumber = playerNumber;
        this.callback = callback;
        this.pendingAttackLines = new LinkedList<>();
        
        // GameLogic을 아이템 모드로 재초기화
        gameLogic = new GameLogic(itemMode);
        
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
        
        // 공격받은 줄 추가 (블록이 착지했을 때)
        if (blockLanded && !pendingAttackLines.isEmpty()) {
            addPendingAttackLines();
            blockLanded = false;
        }
        
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
        
        if (!moved) {
            blockLanded = true;
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
            // 2줄 이상이면 상대방에게 공격
            if (linesToAnimate.size() >= 2) {
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
            return;
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
        
        for (int lineNum : lineNumbers) {
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
    }
    
    /**
     * 공격을 받을 수 있는 상태인지 확인
     */
    public boolean canReceiveAttack() {
        return !lineAnimation.isActive() && blockLanded;
    }
    
    /**
     * 대기 중인 공격 줄을 보드에 추가
     */
    private void addPendingAttackLines() {
        int[][] board = gameLogic.getBoard();
        String[][] blockTypes = gameLogic.getBlockTypes();
        
        int linesToAdd = pendingAttackLines.size();
        
        // 기존 블록들을 위로 밀어올리기
        for (int row = linesToAdd; row < GameLogic.HEIGHT; row++) {
            for (int col = 0; col < GameLogic.WIDTH; col++) {
                board[row - linesToAdd][col] = board[row][col];
                blockTypes[row - linesToAdd][col] = blockTypes[row][col];
            }
        }
        
        // 아래쪽에 공격받은 줄 추가
        int targetRow = GameLogic.HEIGHT - linesToAdd;
        while (!pendingAttackLines.isEmpty() && targetRow < GameLogic.HEIGHT) {
            String[] attackLine = pendingAttackLines.poll();
            for (int col = 0; col < GameLogic.WIDTH; col++) {
                if (attackLine[col] != null) {
                    board[targetRow][col] = 1;
                    blockTypes[targetRow][col] = "attack-block";
                } else {
                    board[targetRow][col] = 0;
                    blockTypes[targetRow][col] = null;
                }
            }
            targetRow++;
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
        super.drawPlacedBlocks(colorMap);
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
        hardDrop();
        drawBoard();
    }
    
    // Getters
    public javafx.scene.canvas.Canvas getCanvas() {
        return canvas;
    }
    
    public int getScore() {
        return gameLogic.getTotalLinesCleared() * 100;
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
     * 게임 재시작
     */
    public void restart() {
        gameLogic.resetGame();
        pendingAttackLines.clear();
        pendingLinesToClear.clear();
        blockLanded = false;
        isGameOver = false;
        isExplosionAnimation = false;
        pendingExplosionCells = null;
        drawBoard();
    }
}
