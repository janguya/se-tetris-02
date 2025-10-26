package com.example.game.blocks;

import com.example.game.component.GameLogic;

//무게추 블록 - 첫 접촉 후 아래 블록을 한 줄씩 파괴하며 내려감
public class WeightItemBlock extends Block {
    
    private boolean touchedGround = false;
    private boolean canMoveAfterTouch = false;
    
    public WeightItemBlock() {
        // 무게추 모양
        //  WW
        // WWWW
        shape = new int[][]{
            {0, 1, 1, 0},
            {1, 1, 1, 1}
        };
        
        cssClass = "item-weight";
        
        // itemPositions는 모두 W (무게추의 타입 ID)로 설정
        itemPositions = new int[][]{
            {0, 4, 4, 0},  // 4 = 무게추 타입 ID
            {4, 4, 4, 4}
        };
    }
    
    @Override
    public boolean isItemBlock() {
        return true;
    }
    
    /**
     * 독립 아이템 블록은 회전 불가
     */
    @Override
    public void rotate() {
        // 회전하지 않음
    }
    
    /**
     * 바닥에 닿았음을 표시
     */
    public void markTouchedGround() {
        this.touchedGround = true;
    }
    
    public boolean hasTouchedGround() {
        return touchedGround;
    }
    
    public boolean canMoveAfterTouch() {
        return canMoveAfterTouch;
    }
    
    /**
     * 무게추 효과 - 바로 아래 한 줄만 제거
     */
    public void activate(GameLogic gameLogic, int blockX, int blockY) {
        System.out.println("=== Weight Item (W) Crushing blocks ===");
        
        int[][] board = gameLogic.getBoard();
        String[][] blockTypes = gameLogic.getBlockTypes();
        int blocksDestroyed = 0;
        
        // 무게추의 각 셀에 대해 바로 아래 블록만 파괴
        for (int i = 0; i < width(); i++) {
            for (int j = 0; j < height(); j++) {
                if (shape[j][i] == 1) {
                    int col = blockX + i;
                    int row = blockY + j;
                    
                    // 바로 아래 한 칸만 제거
                    int belowRow = row + 1;
                    if (col >= 0 && col < GameLogic.WIDTH && 
                        belowRow >= 0 && belowRow < GameLogic.HEIGHT) {
                        if (board[belowRow][col] == 1) {
                            board[belowRow][col] = 0;
                            blockTypes[belowRow][col] = null;
                            blocksDestroyed++;
                        }
                    }
                }
            }
        }
        
        if (blocksDestroyed > 0) {
            System.out.println("Blocks crushed: " + blocksDestroyed);
        }
    }
    
    // 표시 문자 반환
    public char getDisplayChar() {
        return 'W';
    }
    
    public String getItemName() {
        return "무게추";
    }
    
    public String getDescription() {
        return "아래 블록을 부수며 내려갑니다";
    }
}