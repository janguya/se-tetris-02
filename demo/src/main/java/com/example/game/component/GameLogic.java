package com.example.game.component;

import java.util.ArrayList;
import java.util.List;
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
import com.example.game.items.SandBlock;
import com.example.game.items.weightedBlock;
import com.example.settings.GameSettings;
import com.example.utils.Logger;

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

    // 줄 삭제 애니메이션 관련
    private List<Integer> linesToClear = new ArrayList<>();
    private boolean isAnimatingClear = false;

    public GameLogic() {
        this(true); // 기본값: 아이템 모드 비활성화
    }
    
    public GameLogic(boolean itemModeEnabled) {
        random = new Random();
        itemManager = new ItemManager(itemModeEnabled);
        initializeGame();
    }

    // Random seed 설정 (P2P 동기화용)
    // @param seed Random seed 값
    public void setRandomSeed(long seed) {
        this.random = new Random(seed);
        System.out.println(">>> GameLogic: Random seed set to " + seed);

        // 기존 블록 지우기 (겹치지 않도록)
        eraseCurrent();

        // 블록 재생성 (동기화를 위해)
        currentBlock = getRandomBlock();
        nextBlock = getRandomBlock();
    
        // 현재 블록을 보드에 다시 배치
        x = 3;
        y = 0;
        placeCurrent();
    }

    // 게임 초기화
    private void initializeGame() {
        board = new int[HEIGHT][WIDTH]; // 모두 0으로 초기화
        blockTypes = new String[HEIGHT][WIDTH]; // 모두 null로 초기화
        currentBlock = getRandomBlock(); // 첫 블록 생성
        nextBlock = getRandomBlock(); // 다음 블록 생성
        x=3;
        y=0;
        gameOver = false;
        placeCurrent(); // 현재 블록 보드에 놓기
        
        // 통계 초기화
        totalBlocksSpawned = 1; // 첫 블록 카운트
        totalLinesCleared = 0;
        currentLevel = 1;
        speedLevel = 1;
    }

    public Block getRandomBlock() {
        // Base weight for each piece
        double baseWeight = 1.0;

        // Adjust I-block weight based on global difficulty from GameSettings
        GameSettings.Difficulty difficulty = GameSettings.getInstance().getDifficulty();
        double iWeight = baseWeight;
        if (difficulty == GameSettings.Difficulty.EASY) {
            iWeight *= 1.2; // 20% more likely
        } else if (difficulty == GameSettings.Difficulty.HARD) {
            iWeight *= 0.8; // 20% less likely
        }

        // weights order: I, J, L, Z, S, T, O
        double[] weights = new double[] { iWeight, baseWeight, baseWeight, baseWeight, baseWeight, baseWeight, baseWeight };
        double sum = 0.0;
        for (double w : weights) sum += w;

        double r = random.nextDouble() * sum;
        double acc = 0.0;
        int chosen = 0;
        for (int i = 0; i < weights.length; i++) {
            acc += weights[i];
            if (r < acc) {
                chosen = i;
                break;
            }
        }
      
        // 다음 블록이 아이템이어야 하는 경우
        if (nextBlockShouldBeItem) {
            nextBlockShouldBeItem = false;
            Block itemBlock = itemManager.spawnRandomItem();
            if (itemBlock != null) {
                return itemBlock;
            }
        }
        

        switch (chosen) {
            case 0: return new IBlock();
            case 1: return new JBlock();
            case 2: return new LBlock();
            case 3: return new ZBlock();
            case 4: return new SBlock();
            case 5: return new TBlock();
            case 6: return new OBlock();
            default: return new IBlock();
        }
    }

    // 블록 아래로 이동
    public boolean moveDown() {
        // weightedBlock이 이미 접촉한 상태인지 먼저 확인
        boolean isWeightAfterTouch = false;
        if (currentBlock instanceof weightedBlock) {
            weightedBlock weight = (weightedBlock) currentBlock;
            isWeightAfterTouch = weight.hasTouched();
        }
        
        // 현재 블록 지우기
        eraseCurrent();
        
        // 무게추가 접촉 후라면, 아래 블록을 부수고 강제로 내려감
        if (isWeightAfterTouch) {
            weightedBlock weight = (weightedBlock) currentBlock;
            
            // 다음 위치의 블록들 파괴
            for (int i = 0; i < currentBlock.width(); i++) {
                for (int j = 0; j < currentBlock.height(); j++) {
                    if (currentBlock.getShape(i, j) == 1) {
                        int col = x + i;
                        int row = y + j + 1;  // 다음 위치
                        
                        if (col >= 0 && col < WIDTH && 
                            row >= 0 && row < HEIGHT) {
                            if (board[row][col] == 1) {
                                board[row][col] = 0;
                                blockTypes[row][col] = null;
                            }
                        }
                    }
                }
            }
            
            // 바닥에 닿았는지 확인
            if (y + currentBlock.height() >= HEIGHT) {
                // 바닥 도달 - 무게추를 board에 고정
                placeCurrent();
                return false;
            }
            
            // 계속 내려감
            y++;
            placeCurrent();
            return true;
        }
        
        // 일반 블록 처리
        // 아래로 이동 가능하면 이동
        if (canMove(x, y + 1, currentBlock)) {
            y++;
            placeCurrent(); // 이동 후 다시 놓기
            return true;
        } else { // 이동 불가하면 제자리
            // 무게추라면 첫 접촉 처리
            if (currentBlock instanceof weightedBlock) {
                weightedBlock weight = (weightedBlock) currentBlock;
                weight.setTouched(true);
                Logger.info(">>> WeightedBlock: First touch! Marking as touched.");
                
                // 다음 위치의 블록들 파괴
                for (int i = 0; i < currentBlock.width(); i++) {
                    for (int j = 0; j < currentBlock.height(); j++) {
                        if (currentBlock.getShape(i, j) == 1) {
                            int col = x + i;
                            int row = y + j + 1;  // 다음 위치
                            
                            if (col >= 0 && col < WIDTH && 
                                row >= 0 && row < HEIGHT) {
                                if (board[row][col] == 1) {
                                    board[row][col] = 0;
                                    blockTypes[row][col] = null;
                                }
                            }
                        }
                    }
                }
                
                // 바닥 체크
                if (y + currentBlock.height() >= HEIGHT) {
                    placeCurrent();
                    return false;
                }
                
                // 계속 내려감
                y++;
                placeCurrent();
                return true;  // 계속 진행
            }
            
            // SandBlock이면 중력 효과 적용 (고정하지 않고 떨어뜨림)
            if (currentBlock instanceof SandBlock) {
                Logger.info(">>> SandBlock landed! Applying gravity effect...");
                SandBlock sandBlock = (SandBlock) currentBlock;
                sandBlock.applyGravity(board, blockTypes, y, x);
                // SandBlock은 고정하지 않음 - 바로 다음 블록으로
            } else {
                placeCurrent(); // 일반 블록은 현재 위치에 배치
            }
            
            // LItem과 BombBlock은 Board.java에서 애니메이션과 함께 처리됨
            // 여기서는 착지 처리만 수행
            
            // 아이템 블록 착지 디버깅
            if (currentBlock.isItemBlock()) {
                Logger.info(">>> Item block landed: " + currentBlock.getClass().getSimpleName() + " at (" + x + ", " + y + ")");
            }
            
            return false;
        }
    }

    // 블록 좌우 이동
    public void moveLeft() {
        // weightedBlock이 접촉 후이고 이동 불가능하면 무시
        if (currentBlock instanceof weightedBlock) {
            weightedBlock weight = (weightedBlock) currentBlock;
            if (weight.hasTouched() && !weight.canMove()) {
                Logger.info(">>> moveLeft: BLOCKED - weightedBlock cannot move after touch");
                return;
            }
        }
        
        eraseCurrent();
        if (canMove(x - 1, y, currentBlock)) {
            x--;
        }
        placeCurrent();
    }

    public void moveRight() {
        // weightedBlock이 접촉 후이고 이동 불가능하면 무시
        if (currentBlock instanceof weightedBlock) {
            weightedBlock weight = (weightedBlock) currentBlock;
            if (weight.hasTouched() && !weight.canMove()) {
                Logger.info(">>> moveRight: BLOCKED - weightedBlock cannot move after touch");
                return;
            }
        }
        
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
    y = 0; // 두 칸 짜리 블럭은 -1에서 스폰

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
    // 반환값: true = 줄이 삭제됨, false = 줄이 비어있어서 삭제 안됨
    public boolean clearSingleLine(int row) {
        if (row < 0 || row >= HEIGHT) {
            return false;
        }
        
        Logger.info(">>> Clearing single line at row " + row);
        
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
        
        return true;
    }

    // 1단계: 삭제할 줄 찾기 (애니메이션용)
    public List<Integer> findFullLines() {
        List<Integer> fullLines = new ArrayList<>();
        for (int row = HEIGHT - 1; row >= 0; row--) {
            boolean fullLine = true;
            for (int col = 0; col < WIDTH; col++) {
                if (board[row][col] == 0) {
                    fullLine = false;
                    break;
                }
            }
            if (fullLine) {
                fullLines.add(row);
            }
        }
        return fullLines;
    }

    // 2단계: 실제로 줄 삭제 실행
    public int executeLineClear(List<Integer> linesToClear) {
        if (linesToClear == null || linesToClear.isEmpty()) {
        return 0;
        }
        
        int linesCleared = linesToClear.size();
    
        //새 보드 생성 방식 - 삭제 대상이 아닌 줄만 복사
        int[][] newBoard = new int[HEIGHT][WIDTH];
        String[][] newBlockTypes = new String[HEIGHT][WIDTH];
        
        int newRow = HEIGHT - 1; // 새 보드의 맨 아래부터 채움
        
        // 기존 보드를 아래에서 위로 순회
        for (int oldRow = HEIGHT - 1; oldRow >= 0; oldRow--) {
            // 삭제 대상이 아닌 줄만 새 보드에 복사
            if (!linesToClear.contains(oldRow)) {
                System.arraycopy(board[oldRow], 0, newBoard[newRow], 0, WIDTH);
                System.arraycopy(blockTypes[oldRow], 0, newBlockTypes[newRow], 0, WIDTH);
                newRow--;
            }
        }
        
        // 새 보드로 교체
        board = newBoard;
        blockTypes = newBlockTypes;
    
        // 통계 업데이트
        if (linesCleared > 0) {
            totalLinesCleared += linesCleared;
            updateSpeedLevel();
        
            // 아이템 생성 조건 체크
            if (itemManager.shouldSpawnItem(totalLinesCleared)) {
                nextBlockShouldBeItem = true;
            }
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
                if (boardY <= 0) {
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
            Logger.info("Speed level increased to: " + speedLevel + " (was: " + oldSpeedLevel + ")");
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
        Logger.info("=== Speed Info ===");
        Logger.info("Blocks spawned: " + totalBlocksSpawned);
        Logger.info("Lines cleared: " + totalLinesCleared);
        Logger.info("Speed level: " + speedLevel);
        Logger.info("Speed multiplier: " + String.format("%.2f", getSpeedMultiplier()));
        Logger.info("==================");
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
    
    // 네트워크에서 받은 블록 정보로 현재 블록 설정
    public void setCurrentBlockFromNetwork(String blockType, int blockX, int blockY, int rotation) {
        eraseCurrent();
        
        // 블록 타입에 따라 생성
        switch (blockType) {
            case "IBlock":
                currentBlock = new IBlock();
                break;
            case "JBlock":
                currentBlock = new JBlock();
                break;
            case "LBlock":
                currentBlock = new LBlock();
                break;
            case "OBlock":
                currentBlock = new OBlock();
                break;
            case "SBlock":
                currentBlock = new SBlock();
                break;
            case "TBlock":
                currentBlock = new TBlock();
                break;
            case "ZBlock":
                currentBlock = new ZBlock();
                break;
            default:
                return;
        }
        
        // 회전 적용
        for (int i = 0; i < rotation; i++) {
            currentBlock.rotate();
        }
        
        // 위치 설정
        this.x = blockX;
        this.y = blockY;
        
        placeCurrent();
    }
    
    // 네트워크에서 받은 보드 데이터로 보드 설정
    public void setBoardFromNetwork(String[][] networkBoard) {
        eraseCurrent();
        
        // 보드 데이터 복사 (착지된 블록들만)
        for (int row = 0; row < HEIGHT && row < networkBoard.length; row++) {
            for (int col = 0; col < WIDTH && col < networkBoard[row].length; col++) {
                if (networkBoard[row][col] != null) {
                    board[row][col] = 1;
                    blockTypes[row][col] = networkBoard[row][col];
                } else {
                    board[row][col] = 0;
                    blockTypes[row][col] = null;
                }
            }
        }
        
        placeCurrent();
    }
}
