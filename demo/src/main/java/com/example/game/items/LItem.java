package com.example.game.items;

import java.util.Random;
import com.example.game.blocks.Block;

/**
 * L 아이템 - 일반 블록의 랜덤한 위치에 L 마커를 추가
 * L 마커가 있는 줄이 바닥에 닿으면 그 줄 전체를 삭제
 */
public class LItem extends Block {
    
    private Block baseBlock; // 기본 블록
    private int lRow; // L 마커의 행 위치 (블록 내부)
    private int lCol; // L 마커의 열 위치 (블록 내부)
    
    public LItem(Block baseBlock) {
        super();
        this.baseBlock = baseBlock;
        
        // 기본 블록의 shape 복사
        copyShapeFromBase();
        
        this.cssClass = baseBlock.getCssClass(); // 기본 블록의 CSS 클래스 사용
        
        // 랜덤한 위치에 L 마커 설정
        selectRandomLPosition();
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
     * 블록의 랜덤한 1x1 위치를 L 마커로 선택
     */
    private void selectRandomLPosition() {
        Random random = new Random();
        
        // shape에서 1인 위치들 찾기
        int[][] validPositions = new int[shape.length * shape[0].length][2];
        int count = 0;
        
        for (int row = 0; row < shape.length; row++) {
            for (int col = 0; col < shape[row].length; col++) {
                if (shape[row][col] == 1) {
                    validPositions[count][0] = row;
                    validPositions[count][1] = col;
                    count++;
                }
            }
        }
        
        // 랜덤하게 하나 선택
        if (count > 0) {
            int randomIndex = random.nextInt(count);
            lRow = validPositions[randomIndex][0];
            lCol = validPositions[randomIndex][1];
            System.out.println(">>> LItem: L marker at (" + lRow + ", " + lCol + ") in block");
        }
    }
    
    /**
     * L 마커가 있는지 확인
     */
    public boolean hasLMarker(int row, int col) {
        return row == lRow && col == lCol;
    }
    
    /**
     * L 마커의 절대 행 위치 반환 (보드 기준)
     */
    public int getLMarkerAbsoluteRow(int blockY) {
        return blockY + lRow;
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
        // 회전 후 L 마커 위치 다시 선택
        selectRandomLPosition();
    }
    
    @Override
    public String getCssClass() {
        return baseBlock.getCssClass();
    }
    
    /**
     * L 마커의 행/열 위치 getter
     */
    public int getLRow() {
        return lRow;
    }
    
    public int getLCol() {
        return lCol;
    }
}
