package com.example.game.component;

import java.util.Random;

import com.example.game.blocks.Block;
import com.example.game.blocks.IBlock;
import com.example.game.blocks.JBlock;
import com.example.game.blocks.LBlock;
import com.example.game.blocks.OBlock;
import com.example.game.blocks.SBlock;
import com.example.game.blocks.TBlock;
import com.example.game.blocks.ZBlock;

public class GameLogic {

    // 보드 크기
    public static final int HEIGHT = 20; // 20줄
    public static final int WIDTH = 10; // 10칸

    private int[][] board; // 0: 빈칸, 1: 채워진 칸
    private String[][] blockTypes; // 색상 추적용
    private Block currentBlock; // 현재 블록
    private Block nextBlock; // 다음 블록
    private int x = 3; // 현재 블록 X좌표
    private int y = 0; // 현재 블록 Y좌표
    private Random random; // 랜덤 블록 생성용

    // 속도 관련 변수들 추가
    private int totalBlocksSpawned = 0;      // 생성된 총 블록 수
    private int totalLinesCleared = 0;       // 삭제된 총 줄 수
    private int currentLevel = 1;            // 현재 레벨
    private int speedLevel = 1;              // 속도 레벨 (별도 관리)
    
    // 속도 증가 기준
    private static final int BLOCKS_PER_SPEED_INCREASE = 10;  // 10개 블록마다 속도 증가
    private static final int LINES_PER_SPEED_INCREASE = 5;    // 5줄마다 속도 증가
    private static final double SPEED_MULTIPLIER = 0.9;      // 속도 증가율 (90% = 10% 빨라짐)
    private static final double MIN_SPEED_MULTIPLIER = 0.1;  // 최대 속도 (원래의 10%)

    public GameLogic() {
        random = new Random();
        initializeGame();
    }

    // 게임 초기화
    private void initializeGame() {
        board = new int[HEIGHT][WIDTH]; // 모두 0으로 초기화
        blockTypes = new String[HEIGHT][WIDTH]; // 모두 null로 초기화
        currentBlock = getRandomBlock(); // 첫 블록 생성
        nextBlock = getRandomBlock(); // 다음 블록 생성
        
        // 통계 초기화
        totalBlocksSpawned = 1; // 첫 블록 카운트
        totalLinesCleared = 0;
        currentLevel = 1;
        speedLevel = 1;
    }

    public Block getRandomBlock() {
        int blockType = random.nextInt(7);
        switch (blockType) {
            case 0:
                return new IBlock();
            case 1:
                return new JBlock();
            case 2:
                return new LBlock();
            case 3:
                return new ZBlock();
            case 4:
                return new SBlock();
            case 5:
                return new TBlock();
            case 6:
                return new OBlock();
            default:
                return new IBlock();
        }
    }

    // 블록 아래로 이동
    public boolean moveDown() {
        // 현재 블록 지우기
        eraseCurrent();
        // 아래로 이동 가능하면 이동
        if (canMove(x, y + 1, currentBlock)) {
            y++;
            placeCurrent(); // 이동 후 다시 놓기
            return true;
        } else { // 이동 불가하면 제자리
            placeCurrent();
            spawnNewBlock(); // 새 블록 생성
            return false;
        }
    }

    // 블록 좌우 이동
    public void moveLeft() {
        eraseCurrent();
        if (canMove(x - 1, y, currentBlock)) {
            x--;
        }
        placeCurrent();
    }

    public void moveRight() {
        eraseCurrent();
        if (canMove(x + 1, y, currentBlock)) {
            x++;
        }
        placeCurrent();
    }

    // 블록 회전
    public void rotateBlock() {
        eraseCurrent();
        currentBlock.rotate();
        if (!canMove(x, y, currentBlock)) {
            // 회전 후 이동 불가하면 원래대로 돌리기
            // 3번 회전하면 원래 상태
            for (int i = 0; i < 3; i++) {
                currentBlock.rotate();
            }
        }
        placeCurrent();
    }

    // 새 블록 생성
    private void spawnNewBlock() {
        currentBlock = nextBlock;
        nextBlock = getRandomBlock();
        // 새 블록 시작 위치 설정
        x = 3;
        y = 0;

        // 블록 생성 수 증가
        totalBlocksSpawned++;
        
        // 속도 레벨 업데이트
        updateSpeedLevel();

        if (!canMove(x, y, currentBlock)) {
            // 게임 오버 상태 설정을 위해 y를 0으로 되돌림
            y = 0;
        }
    }

    // 블록이 특정 위치로 이동 가능한지 확인
    // newX, newY: 블록의 새 좌표
    // block: 이동할 블록
    public boolean canMove(int newX, int newY, Block block) {
        if (block == null) {
            return false;
        }

        for (int i = 0; i < block.width(); i++) {
            for (int j = 0; j < block.height(); j++) {
                if (block.getShape(i, j) == 1) {
                    // 블록의 현재 좌표에 대한 보드 좌표 계산
                    int boardX = newX + i;
                    int boardY = newY + j;

                    // 좌우 경계 확인
                    if (boardX < 0 || boardX >= WIDTH) {
                        return false;
                    }

                    // 하단 경계 확인
                    if (boardY >= HEIGHT) {
                        return false;
                    }

                    // 상단 경계는 약간의 여유를 둠 (블록이 화면 위쪽에서 시작할 수 있도록)
                    if (boardY < -2) {
                        return false;
                    }

                    // 보드 내부의 충돌 확인 (boardY가 0 이상인 경우에만)
                    if (boardY >= 0 && board[boardY][boardX] == 1) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    // 현재 블록 지우기
    private void eraseCurrent() {
        if (currentBlock == null) {
            return;
        }

        for (int i = 0; i < currentBlock.width(); i++) {
            for (int j = 0; j < currentBlock.height(); j++) {
                if (currentBlock.getShape(i, j) == 1) {
                    // 현재 블록의 보드 좌표 계산
                    int boardX = x + i;
                    int boardY = y + j;

                    // 보드 경계 내에서만 지우기
                    if (boardX >= 0 && boardX < WIDTH && boardY >= 0 && boardY < HEIGHT) {
                        board[boardY][boardX] = 0;
                        blockTypes[boardY][boardX] = null;
                    }
                }
            }
        }
    }

    // 현재 블록 놓기
    private void placeCurrent() {
        if (currentBlock == null) {
            return;
        }

        for (int i = 0; i < currentBlock.width(); i++) {
            for (int j = 0; j < currentBlock.height(); j++) {
                if (currentBlock.getShape(i, j) == 1) {
                    // 현재 블록의 보드 좌표 계산
                    int boardX = x + i;
                    int boardY = y + j;

                    // 보드 경계 내에서만 놓기
                    if (boardX >= 0 && boardX < WIDTH && boardY >= 0 && boardY < HEIGHT) {
                        board[boardY][boardX] = 1;
                        blockTypes[boardY][boardX] = currentBlock.getCssClass();
                    }
                }
            }
        }
    }

    // 줄 삭제
    public int clearLines() {
        // 삭제된 줄 수 반환
        int linesCleared = 0;

        // 아래에서 위로 검사
        for (int row = HEIGHT - 1; row >= 0; row--) {
            boolean fullLine = true;
            for (int col = 0; col < WIDTH; col++) {
                if (board[row][col] == 0) {
                    fullLine = false;
                    break;
                }
            }

            // 줄이 꽉 찼으면 삭제
            if (fullLine) {
                // 위의 줄들을 한 칸씩 내리기
                for (int moveRow = row; moveRow > 0; moveRow--) {
                    System.arraycopy(board[moveRow - 1], 0, board[moveRow], 0, WIDTH);
                    System.arraycopy(blockTypes[moveRow - 1], 0, blockTypes[moveRow], 0, WIDTH);
                }
                // 맨 위 줄 지우기
                for (int col = 0; col < WIDTH; col++) {
                    board[0][col] = 0;
                    blockTypes[0][col] = null;
                }

                linesCleared++;
                row++;
            }
        }

        // 줄 삭제 통계 업데이트
        if (linesCleared > 0) {
            totalLinesCleared += linesCleared;
            updateSpeedLevel();
            
            // 디버깅 출력
            System.out.println("Lines cleared: " + linesCleared + ", Total lines: " + totalLinesCleared);
            System.out.println("Blocks spawned: " + totalBlocksSpawned + ", Speed level: " + speedLevel);
        }

        return linesCleared;
    }

    // 속도 레벨 업데이트
    private void updateSpeedLevel() {
        int oldSpeedLevel = speedLevel;
        
        // 블록 수에 따른 속도 증가
        int speedFromBlocks = totalBlocksSpawned / BLOCKS_PER_SPEED_INCREASE;
        
        // 줄 삭제 수에 따른 속도 증가
        int speedFromLines = totalLinesCleared / LINES_PER_SPEED_INCREASE;
        
        // 둘 중 높은 값을 사용 (더 빠른 진행)
        speedLevel = Math.max(speedFromBlocks, speedFromLines) + 1;
        
        // 레벨도 업데이트 (UI 표시용)
        currentLevel = speedLevel;
        
        //디버깅용 출력
        if (speedLevel != oldSpeedLevel) {
            System.out.println("Speed level increased to: " + speedLevel + " (was: " + oldSpeedLevel + ")");
        }
    }

    // 속도 계산 (배수 반환)
    public double getSpeedMultiplier() {
        double multiplier = Math.pow(SPEED_MULTIPLIER, speedLevel - 1);
        return Math.max(multiplier, MIN_SPEED_MULTIPLIER);
    }

    // 드롭 간격 계산 (나노초)
    public long getDropInterval(long baseInterval) {
        return (long)(baseInterval * getSpeedMultiplier());
    }

    public boolean isGameOver() {
        // // 현재 블록이 스폰 위치에 놓일 수 없는 경우
        // if (!canMove(x, y, currentBlock)) {
        //     return true;
        // }

        // // 2. 현재 블록이 기본 스폰 위치(3, 0)나 그 위쪽에서 놓을 수 없는 경우
        // if (!canMove(x, y, currentBlock)) {
        //     // 추가 확인: 보드 상단에 블록이 쌓여있는지 확인
        //     for (int row = 0; row < 3; row++) {
        //         for (int col = 0; col < WIDTH; col++) {
        //             if (board[row][col] == 1) {
        //                 return true; // 상단에 블록이 있으면 게임 오버
        //             }
        //         }
        //     }
        //     return true;
        // }

        return false;
    }

    // Getters
    public int[][] getBoard() {
        return board;
    }

    public String[][] getBlockTypes() {
        return blockTypes;
    }

    public Block getCurrentBlock() {
        return currentBlock;
    }

    public Block getNextBlock() {
        return nextBlock;
    }

    public int getCurrentX() {
        return x;
    }

    public int getCurrentY() {
        return y;
    }

    // 새로 추가된 Getters
    public int getTotalBlocksSpawned() {
        return totalBlocksSpawned;
    }

    public int getTotalLinesCleared() {
        return totalLinesCleared;
    }

    public int getCurrentLevel() {
        return currentLevel;
    }

    public int getSpeedLevel() {
        return speedLevel;
    }

    public void resetGame() {
        initializeGame();
    }

    // 디버깅용 메서드
    public void printSpeedInfo() {
        System.out.println("=== Speed Info ===");
        System.out.println("Blocks spawned: " + totalBlocksSpawned);
        System.out.println("Lines cleared: " + totalLinesCleared);
        System.out.println("Speed level: " + speedLevel);
        System.out.println("Speed multiplier: " + String.format("%.2f", getSpeedMultiplier()));
        System.out.println("==================");
    }
}
