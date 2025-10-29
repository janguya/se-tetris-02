package com.example.game.items;

import java.util.Random;
import com.example.game.blocks.Block;

/**
 * Bomb 아이템 - 일반 블록의 랜덤한 위치에 B 마커를 추가
 * B 마커가 착지 시 B를 중심으로 3x3 범위를 폭파
 */
public class BombBlock extends Block {
    
    private Block baseBlock; // 기본 블록
    private int bRow; // B 마커의 행 위치 (블록 내부)
    private int bCol; // B 마커의 열 위치 (블록 내부)
    
    public BombBlock(Block baseBlock) {
        super();
        this.baseBlock = baseBlock;
        
        // 기본 블록의 shape 복사
        copyShapeFromBase();
        
        this.cssClass = baseBlock.getCssClass(); // 기본 블록의 CSS 클래스 사용
        
        // 랜덤한 위치에 B 마커 설정
        selectRandomBPosition();
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
     * 블록의 랜덤한 1x1 위치를 B 마커로 선택
     */
    private void selectRandomBPosition() {
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
            bRow = validPositions[randomIndex][0];
            bCol = validPositions[randomIndex][1];
            System.out.println(">>> BombBlock: B marker at (" + bRow + ", " + bCol + ") in block");
        }
    }
    
    /**
     * B 마커가 있는지 확인
     */
    public boolean hasBMarker(int row, int col) {
        return row == bRow && col == bCol;
    }
    
    /**
     * B 마커의 절대 좌표 반환 (보드 기준)
     */
    public int getBMarkerAbsoluteRow(int blockY) {
        return blockY + bRow;
    }
    
    public int getBMarkerAbsoluteCol(int blockX) {
        return blockX + bCol;
    }
    
    /**
     * B 마커 주변 3x3 범위에서 폭발할 셀 좌표 반환 (애니메이션용)
     * @param blockY 블록의 Y 좌표
     * @param blockX 블록의 X 좌표
     * @param boardHeight 보드 높이
     * @param boardWidth 보드 너비
     * @return 폭발할 셀 좌표 배열 [[row, col], ...]
     */
    public int[][] getExplosionCells(int blockY, int blockX, int boardHeight, int boardWidth) {
        int bombRow = getBMarkerAbsoluteRow(blockY);
        int bombCol = getBMarkerAbsoluteCol(blockX);
        
        System.out.println(">>> BombBlock: Getting explosion cells at (" + bombCol + ", " + bombRow + ")");
        
        // 최대 9개의 셀 (3x3)
        int[][] cells = new int[9][2];
        int count = 0;
        
        // B를 중심으로 3x3 범위
        for (int row = bombRow - 1; row <= bombRow + 1; row++) {
            for (int col = bombCol - 1; col <= bombCol + 1; col++) {
                // 보드 범위 내에 있는지 확인
                if (row >= 0 && row < boardHeight && 
                    col >= 0 && col < boardWidth) {
                    cells[count][0] = row;
                    cells[count][1] = col;
                    count++;
                }
            }
        }
        
        // 실제 셀 수만큼 배열 크기 조정
        int[][] result = new int[count][2];
        System.arraycopy(cells, 0, result, 0, count);
        
        System.out.println(">>> BombBlock: Total explosion cells: " + count);
        return result;
    }
    
    /**
     * 실제로 폭발 영역의 블록들을 삭제
     * @param board 게임 보드
     * @param blockTypes 블록 타입 배열
     * @param explosionCells 폭발할 셀 좌표
     * @return 삭제된 블록 수
     */
    public int executeExplosion(int[][] board, String[][] blockTypes, int[][] explosionCells) {
        int destroyedCount = 0;
        
        for (int[] cell : explosionCells) {
            int row = cell[0];
            int col = cell[1];
            
            // 블록이 있으면 삭제
            if (board[row][col] == 1) {
                board[row][col] = 0;
                blockTypes[row][col] = null;
                destroyedCount++;
                System.out.println(">>> BombBlock: Destroyed block at (" + col + ", " + row + ")");
            }
        }
        
        System.out.println(">>> BombBlock: Total destroyed blocks: " + destroyedCount);
        return destroyedCount;
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
        
        // 회전 전 마커의 실제 좌표와 높이 저장
        int oldHeight = shape.length;
        
        // 회전 후 shape 다시 복사
        copyShapeFromBase();
        
        // 회전 후 마커 위치 계산 (90도 시계방향 회전)
        // 회전 공식: (row, col) -> (col, height-1-row)
        int newRow = bCol;
        int newCol = oldHeight - 1 - bRow;
        
        // 새로운 shape 범위 내에서 유효한 위치인지 확인
        if (newRow >= 0 && newRow < shape.length && 
            newCol >= 0 && newCol < shape[0].length &&
            shape[newRow][newCol] == 1) {
            bRow = newRow;
            bCol = newCol;
            System.out.println(">>> BombBlock: B marker rotated to (" + bRow + ", " + bCol + ")");
        } else {
            // 유효하지 않으면 가장 가까운 유효한 위치 찾기
            selectRandomBPosition();
            System.out.println(">>> BombBlock: B marker repositioned after rotation to (" + bRow + ", " + bCol + ")");
        }
    }
    
    @Override
    public String getCssClass() {
        return baseBlock.getCssClass();
    }
    
    /**
     * B 마커의 행/열 위치 getter
     */
    public int getBRow() {
        return bRow;
    }
    
    public int getBCol() {
        return bCol;
    }
}
