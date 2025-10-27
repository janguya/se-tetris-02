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
import com.example.game.items.ItemManager;
import com.example.game.items.weightedBlock;
import com.example.game.items.LItem;
import com.example.game.items.SandBlock;

public class GameLogic {

    // 보드 크기
    public static final int HEIGHT = 20; // 20줄
    public static final int WIDTH = 10; // 10칸

    private int[][] board; // 0: 빈칸, 1: 채워진 칸
    private String[][] blockTypes; // 색상 추적용
    private Block currentBlock; // 현재 블록
    private Block nextBlock; // 다음 블록
    private int x = 3; // 현재 블록 X좌표
    private int y = -1; // 현재 블록 Y좌표
    private Random random; // 랜덤 블록 생성용
    private boolean gameOver = false; // 게임 오버 상태

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
    
    // 아이템 매니저
    private ItemManager itemManager;
    private boolean nextBlockShouldBeItem = false; // 다음 블록이 아이템이어야 하는지

    public GameLogic() {
        this(true); // 기본값: 아이템 모드 비활성화
    }
    
    public GameLogic(boolean itemModeEnabled) {
        random = new Random();
        itemManager = new ItemManager(itemModeEnabled);
        initializeGame();
    }

    // 게임 초기화
    private void initializeGame() {
        board = new int[HEIGHT][WIDTH]; // 모두 0으로 초기화
        blockTypes = new String[HEIGHT][WIDTH]; // 모두 null로 초기화
        currentBlock = getRandomBlock(); // 첫 블록 생성
        nextBlock = getRandomBlock(); // 다음 블록 생성
        x=3;
        y=-1;
        gameOver = false;
        placeCurrent(); // 현재 블록 보드에 놓기
        
        // 통계 초기화
        totalBlocksSpawned = 1; // 첫 블록 카운트
        totalLinesCleared = 0;
        currentLevel = 1;
        speedLevel = 1;
    }

    public Block getRandomBlock() {
        // 다음 블록이 아이템이어야 하는 경우
        if (nextBlockShouldBeItem) {
            nextBlockShouldBeItem = false;
            Block itemBlock = itemManager.spawnRandomItem();
            if (itemBlock != null) {
                return itemBlock;
            }
        }
        
        // 일반 블록 생성
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
            // SandBlock이면 중력 효과 적용 (고정하지 않고 떨어뜨림)
            if (currentBlock instanceof SandBlock) {
                System.out.println(">>> SandBlock landed! Applying gravity effect...");
                SandBlock sandBlock = (SandBlock) currentBlock;
                sandBlock.applyGravity(board, blockTypes, y, x);
                // SandBlock은 고정하지 않음 - 바로 다음 블록으로
            }
            // weightedBlock이면 바닥까지 떨어지면서 아래 블록들 삭제
            else if (currentBlock instanceof weightedBlock) {
                System.out.println(">>> WeightedBlock landed! Starting fall to bottom...");
                // placeCurrent() 하지 않고 바로 fallToBottom 호출
                int finalY = ((weightedBlock) currentBlock).fallToBottom(board, blockTypes, y, x);
                y = finalY; // 최종 위치로 업데이트
                placeCurrent(); // 최종 위치에만 배치
            } else {
                placeCurrent(); // 일반 블록은 현재 위치에 배치
            }
            
            // LItem이면 L 마커가 있는 줄 삭제
            if (currentBlock instanceof LItem) {
                LItem lItem = (LItem) currentBlock;
                int lRow = lItem.getLMarkerAbsoluteRow(y);
                System.out.println(">>> LItem landed! L marker at row " + lRow);
                clearSingleLine(lRow);
            }
            
            // 아이템 블록 착지 디버깅
            if (currentBlock.isItemBlock()) {
                System.out.println(">>> Item block landed: " + currentBlock.getCssClass() + " at (" + x + ", " + y + ")");
            }
            
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
    public boolean spawnNextPiece() {
    // 1) 다음 블록을 현재로 승격
    currentBlock = nextBlock;
    nextBlock = getRandomBlock();

    // 2) 스폰 좌표 설정 (현재 x=3, y=0을 기본으로 사용하셨으므로 유지)
    x = 3;
    y = -1;

    // 3) 스폰 가능? (경계/충돌 검사)
    if (!canMove(x, y, currentBlock)) {
        
        // 스폰 불가 → 게임오버 플래그
        gameOver = true;
        return false;
    }
    // 블록 생성 수 증가
        totalBlocksSpawned++;
        
        // 속도 레벨 업데이트
        updateSpeedLevel();
    // 4) 스폰 성공 → 보드에 반영(지우개/그리기 방식 유지)
    placeCurrent();
    return true;
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

    // 특정 줄 하나만 삭제 (LItem용)
    public void clearSingleLine(int row) {
        if (row < 0 || row >= HEIGHT) {
            return;
        }
        
        System.out.println(">>> Clearing single line at row " + row);
        
        // 해당 줄 위의 모든 줄을 한 칸씩 내리기
        for (int moveRow = row; moveRow > 0; moveRow--) {
            System.arraycopy(board[moveRow - 1], 0, board[moveRow], 0, WIDTH);
            System.arraycopy(blockTypes[moveRow - 1], 0, blockTypes[moveRow], 0, WIDTH);
        }
        
        // 맨 위 줄 지우기
        for (int col = 0; col < WIDTH; col++) {
            board[0][col] = 0;
            blockTypes[0][col] = null;
        }
        
        // 통계 업데이트
        totalLinesCleared++;
        updateSpeedLevel();
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
            
            // 아이템 생성 조건 체크
            if (itemManager.shouldSpawnItem(totalLinesCleared)) {
                nextBlockShouldBeItem = true;
                System.out.println(">>> Item will spawn at next block! (Total lines: " + totalLinesCleared + ")");
            }
            
            // 디버깅 출력
            System.out.println("Lines cleared: " + linesCleared + ", Total lines: " + totalLinesCleared);
            System.out.println("Blocks spawned: " + totalBlocksSpawned + ", Speed level: " + speedLevel);
        }

        return linesCleared;
    }

    public boolean isBlockAtTop() {
    if (currentBlock == null) {
        return false;
    }
    
    boolean atTop = false;
    for (int i = 0; i < currentBlock.width(); i++) {
        for (int j = 0; j < currentBlock.height(); j++) {
            if (currentBlock.getShape(i, j) == 1) {
                int boardY = y + j;
                if (boardY == 0) {
                    atTop = true;
        
                }
            }
        }
    }
    return atTop;
}

    // 게임 종료 확인
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
    return gameOver;
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
        if (itemManager != null) {
            itemManager.reset();
        }
        nextBlockShouldBeItem = false;
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
    
    // 아이템 매니저 getter
    public ItemManager getItemManager() {
        return itemManager;
    }
    
    // 다음 아이템까지 남은 줄 수
    public int getLinesUntilNextItem() {
        if (itemManager == null) {
            return -1;
        }
        return itemManager.getLinesUntilNextItem(totalLinesCleared);
    }
}
