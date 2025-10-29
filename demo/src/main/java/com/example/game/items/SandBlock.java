package com.example.game.items;

import com.example.game.blocks.Block;

/**
 * Sand 아이템 - 일반 블록을 래핑하여 모래처럼 동작
 * 땅에 닿을 때 블록이 고정되지 않고 각 셀이 중력에 따라 개별적으로 떨어짐
 */
public class SandBlock extends Block {
    
    private Block baseBlock; // 기본 블록
    private boolean hasActivated = false; // 모래 효과가 활성화되었는지 여부
    
    public SandBlock(Block baseBlock) {
        super();
        this.baseBlock = baseBlock;
        
        // 기본 블록의 shape 복사
        copyShapeFromBase();
        
        this.cssClass = "item-sand"; // 모래 블록 전용 CSS 클래스
    }
    
    /**
     * 기본 블록의 shape를 복사
     */
    private void copyShapeFromBase() {
        int height = baseBlock.height();
        int width = baseBlock.width();
        
        shape = new int[height][width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                shape[i][j] = baseBlock.getShape(j, i);
            }
        }
    }
    
    /**
     * 모래처럼 떨어지는 효과 - 각 셀이 개별적으로 중력에 따라 아래로 떨어짐
     * @param board 게임 보드
     * @param blockTypes 블록 타입 배열
     * @param startY 블록의 시작 Y 좌표
     * @param startX 블록의 시작 X 좌표
     */
    public void applyGravity(int[][] board, String[][] blockTypes, int startY, int startX) {
        if (hasActivated) {
            return;
        }
        
        hasActivated = true;
        System.out.println(">>> SandBlock: Applying gravity effect from (" + startX + ", " + startY + ")");
        
        // 각 열별로 아래에서 위로 처리하여 중력 적용
        for (int i = 0; i < width(); i++) {
            int boardX = startX + i;
            
            // 범위 체크
            if (boardX < 0 || boardX >= board[0].length) {
                continue;
            }
            
            // 해당 열의 모든 셀을 아래에서 위로 처리
            for (int j = height() - 1; j >= 0; j--) {
                if (getShape(i, j) == 1) {
                    int boardY = startY + j;
                    
                    // 해당 위치의 블록을 지우고 중력 적용
                    if (boardY >= 0 && boardY < board.length) {
                        
                        // 현재 셀 제거
                        board[boardY][boardX] = 0;
                        blockTypes[boardY][boardX] = null;
                        
                        // 아래로 떨어뜨리기 - 바닥이나 다른 블록을 만날 때까지
                        int finalY = boardY;
                        while (finalY + 1 < board.length && board[finalY + 1][boardX] == 0) {
                            finalY++;
                        }
                        
                        // 최종 위치에 배치
                        board[finalY][boardX] = 1;
                        blockTypes[finalY][boardX] = cssClass;
                        
                        System.out.println(">>> SandBlock: Cell at (" + boardX + ", " + boardY + ") fell to (" + boardX + ", " + finalY + ")");
                    }
                }
            }
        }
    }
    
    /**
     * 효과 활성화 여부 확인
     */
    public boolean hasActivated() {
        return hasActivated;
    }
    
    /**
     * 아이템 블록 여부
     */
    @Override
    public boolean isItemBlock() {
        return true;
    }
    
    /**
     * 기본 블록의 회전을 따라감
     */
    @Override
    public void rotate() {
        baseBlock.rotate();
        // 회전 후 shape 다시 복사
        copyShapeFromBase();
    }
    
    @Override
    public String getCssClass() {
        return cssClass;
    }
}
