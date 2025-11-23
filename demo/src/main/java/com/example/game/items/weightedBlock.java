package com.example.game.items;

/**
 * 무게 아이템 블록
 * 떨어지면서 아래에 있는 블록들을 삭제하는 특수 블록
 * 형태:
 *   oo
 * oooo
 */
public class weightedBlock extends ItemBlock {
    
    private boolean hasActivated = false; // 효과가 활성화되었는지 여부
    
    public weightedBlock() {
        super();
        // 무게 블록 형태
        shape = new int[][] {
            {0, 1, 1, 0},
            {1, 1, 1, 1}
        };
        cssClass = "item";
    }
    
    /**
     * 무게 블록이 떨어지면서 아래 블록들을 부수고 맨 바닥까지 내려가는 메서드
     * @param board 게임 보드
     * @param blockTypes 블록 타입 배열
     * @param startRow 시작 행 위치
     * @param startCol 시작 열 위치
     * @return 최종 도착한 행 위치
     */
    public int fallToBottom(int[][] board, String[][] blockTypes, int startRow, int startCol) {
        if (hasActivated) {
            // 이미 활성화됨 - 중복 실행 방지
            return startRow;
        }
        
        hasActivated = true;
        System.out.println(">>> WeightedBlock: Starting fall from row " + startRow);
        
        int currentRow = startRow;
        int blocksDestroyed = 0;
        
        // 바닥(HEIGHT - shape.length)까지 내려가면서 아래 블록 삭제
        while (currentRow < board.length - shape.length) {
            currentRow++;
            
            // 현재 위치에서 블록의 각 셀 아래에 있는 블록 삭제
            for (int row = 0; row < shape.length; row++) {
                for (int col = 0; col < shape[row].length; col++) {
                    if (shape[row][col] != 0) {
                        int boardRow = currentRow + row;
                        int boardCol = startCol + col;
                        
                        // 보드 범위 내에서만 삭제
                        if (boardRow >= 0 && boardRow < board.length && 
                            boardCol >= 0 && boardCol < board[boardRow].length) {
                            if (board[boardRow][boardCol] == 1) {
                                board[boardRow][boardCol] = 0;
                                blockTypes[boardRow][boardCol] = null;
                                blocksDestroyed++;
                            }
                        }
                    }
                }
            }
        }
        
        System.out.println(">>> WeightedBlock: Reached bottom at row " + currentRow + ", destroyed " + blocksDestroyed + " blocks");
        return currentRow;
    }
    
    public boolean hasActivated() {
        return hasActivated;
    }
    
    private boolean hasTouched = false; // 블록이나 바닥에 닿았는지 여부
    
    /**
     * 닿음 상태 설정
     */
    public void setTouched(boolean touched) {
        this.hasTouched = touched;
        if (touched) {
            System.out.println(">>> WeightedBlock: setTouched(true) - Cannot move horizontally anymore!");
        }
    }
    
    /**
     * 닿음 상태 확인
     */
    public boolean hasTouched() {
        return hasTouched;
    }
    
    /**
     * 닿은 후 좌우 이동 불가 확인
     * @return 이동 가능 여부
     */
    public boolean canMove() {
        return !hasTouched;
    }
    
    /**
     * 무게 블록은 회전 불가
     */
    @Override
    public void rotate() {
        // 무게 블록은 회전하지 않음
    }
}
