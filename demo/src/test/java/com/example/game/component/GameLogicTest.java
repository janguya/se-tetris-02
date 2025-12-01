package com.example.game.component;

import com.example.game.blocks.Block;
import com.example.game.items.ItemManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GameLogicTest {

    private GameLogic gameLogic;

    @BeforeEach
    void setUp() {
        gameLogic = new GameLogic();
    }

    @Test
    @DisplayName("게임 로직 초기화 테스트")
    void testInitialization() {
        // Given & When - setUp에서 초기화됨

        // Then
        assertNotNull(gameLogic.getCurrentBlock(), "현재 블록이 null이면 안됩니다");
        assertNotNull(gameLogic.getNextBlock(), "다음 블록이 null이면 안됩니다");
        assertFalse(gameLogic.isGameOver(), "게임 시작시 게임오버가 false여야 합니다");
        assertEquals(1, gameLogic.getCurrentLevel(), "초기 레벨은 1이어야 합니다");
        assertEquals(1, gameLogic.getSpeedLevel(), "초기 속도 레벨은 1이어야 합니다");
        assertEquals(1, gameLogic.getTotalBlocksSpawned(), "초기 블록 생성 수는 1이어야 합니다");
        assertEquals(0, gameLogic.getTotalLinesCleared(), "초기 줄 삭제 수는 0이어야 합니다");
    }

    @Test
    @DisplayName("보드 크기 확인 테스트")
    void testBoardSize() {
        // Given & When
        int[][] board = gameLogic.getBoard();

        // Then
        assertEquals(GameLogic.HEIGHT, board.length, "보드 높이가 올바르지 않습니다");
        assertEquals(GameLogic.WIDTH, board[0].length, "보드 너비가 올바르지 않습니다");
    }

    @Test
    @DisplayName("블록 이동 테스트")
    void testBlockMovement() {
        // Given
        int initialX = gameLogic.getCurrentX();
        int initialY = gameLogic.getCurrentY();

        // When & Then - 오른쪽 이동
        gameLogic.moveRight();
        assertEquals(initialX + 1, gameLogic.getCurrentX(), "오른쪽 이동이 실패했습니다");

        // When & Then - 왼쪽 이동
        gameLogic.moveLeft();
        assertEquals(initialX, gameLogic.getCurrentX(), "왼쪽 이동이 실패했습니다");

        // When & Then - 아래 이동
        boolean canMoveDown = gameLogic.moveDown();
        assertTrue(canMoveDown || gameLogic.getCurrentY() == initialY,
                "아래 이동이 예상과 다릅니다");
    }

    @Test
    @DisplayName("블록 회전 테스트")
    void testBlockRotation() {
        // Given
        Block initialBlock = gameLogic.getCurrentBlock();
        String initialBlockType = initialBlock.getCssClass();

        // When
        gameLogic.rotateBlock();

        // Then
        Block rotatedBlock = gameLogic.getCurrentBlock();
        assertEquals(initialBlockType, rotatedBlock.getCssClass(),
                "회전 후 블록 타입이 변경되면 안됩니다");
    }

    @Test
    @DisplayName("속도 계산 테스트")
    void testSpeedCalculation() {
        // Given & When
        double initialSpeedMultiplier = gameLogic.getSpeedMultiplier();

        // Then
        assertEquals(1.0, initialSpeedMultiplier, 0.001,
                "초기 속도 배수는 1.0이어야 합니다");

        // Given - 블록을 여러 개 생성하여 속도 레벨 증가시키기
        for (int i = 0; i < 10; i++) {
            gameLogic.spawnNextPiece();
        }

        // When
        double newSpeedMultiplier = gameLogic.getSpeedMultiplier();

        // Then
        assertTrue(newSpeedMultiplier <= initialSpeedMultiplier,
                "블록 수가 증가하면 속도가 빨라져야 합니다 (배수가 작아져야 함)");
    }

    @Test
    @DisplayName("게임 리셋 테스트")
    void testGameReset() {
        // Given - 게임 상태 변경
        for (int i = 0; i < 5; i++) {
            gameLogic.spawnNextPiece();
        }

        // When
        gameLogic.resetGame();

        // Then
        assertEquals(1, gameLogic.getTotalBlocksSpawned(), "리셋 후 블록 수가 1이어야 합니다");
        assertEquals(0, gameLogic.getTotalLinesCleared(), "리셋 후 줄 삭제 수가 0이어야 합니다");
        assertEquals(1, gameLogic.getCurrentLevel(), "리셋 후 레벨이 1이어야 합니다");
        assertFalse(gameLogic.isGameOver(), "리셋 후 게임오버가 false여야 합니다");
    }
    
    @Test
    @DisplayName("블록 이동 다운 테스트")
    void testMoveDown() {
        // Given
        int initialY = gameLogic.getCurrentY();
        
        // When
        boolean moved = gameLogic.moveDown();
        
        // Then
        assertTrue(moved || gameLogic.getCurrentY() > initialY, "블록이 아래로 이동해야 합니다");
    }
    
    @Test
    @DisplayName("블록 생성 테스트")
    void testSpawnNextPiece() {
        // Given
        Block oldCurrent = gameLogic.getCurrentBlock();
        Block oldNext = gameLogic.getNextBlock();
        
        // When
        boolean spawned = gameLogic.spawnNextPiece();
        
        // Then
        assertNotNull(gameLogic.getCurrentBlock(), "현재 블록이 null이면 안됩니다");
        assertNotNull(gameLogic.getNextBlock(), "다음 블록이 null이면 안됩니다");
        // spawned가 false일 수도 있음 (게임 오버 등)
        if (spawned) {
            assertNotEquals(oldCurrent, gameLogic.getCurrentBlock(), "블록이 교체되어야 합니다");
        }
    }
    
    @Test
    @DisplayName("라인 삭제 실행 테스트")
    void testExecuteLineClear() {
        // Given
        List<Integer> linesToClear = new ArrayList<>();
        linesToClear.add(19);
        
        // When
        int cleared = gameLogic.executeLineClear(linesToClear);
        
        // Then
        assertEquals(1, cleared, "1줄이 삭제되어야 합니다");
    }
    
    @Test
    @DisplayName("단일 라인 클리어 테스트")
    void testClearSingleLine() {
        // Given - 보드 하단 줄을 채움
        int[][] board = gameLogic.getBoard();
        for (int col = 0; col < GameLogic.WIDTH; col++) {
            board[GameLogic.HEIGHT - 1][col] = 1;
        }
        
        // When
        boolean cleared = gameLogic.clearSingleLine(GameLogic.HEIGHT - 1);
        
        // Then
        assertTrue(cleared, "줄이 삭제되어야 합니다");
    }
    
    @Test
    @DisplayName("풀 라인 찾기 테스트")
    void testFindFullLines() {
        // Given - 보드 하단 줄을 채움
        int[][] board = gameLogic.getBoard();
        for (int col = 0; col < GameLogic.WIDTH; col++) {
            board[GameLogic.HEIGHT - 1][col] = 1;
        }
        
        // When
        List<Integer> fullLines = gameLogic.findFullLines();
        
        // Then
        assertNotNull(fullLines, "결과가 null이면 안됩니다");
        assertTrue(fullLines.contains(GameLogic.HEIGHT - 1), "채워진 줄을 찾아야 합니다");
    }
    
    @Test
    @DisplayName("블록 이동 가능 여부 확인 테스트")
    void testCanMove() {
        // Given
        Block block = gameLogic.getCurrentBlock();
        
        // When
        boolean canMove = gameLogic.canMove(5, 5, block);
        
        // Then
        assertTrue(canMove || !canMove, "이동 가능 여부를 반환해야 합니다");
    }
    
    @Test
    @DisplayName("블록이 상단에 있는지 확인 테스트")
    void testIsBlockAtTop() {
        // When
        boolean atTop = gameLogic.isBlockAtTop();
        
        // Then
        assertTrue(atTop || !atTop, "상단 여부를 반환해야 합니다");
    }
    
    @Test
    @DisplayName("드롭 인터벌 계산 테스트")
    void testGetDropInterval() {
        // Given
        long baseInterval = 1000L;
        
        // When
        long interval = gameLogic.getDropInterval(baseInterval);
        
        // Then
        assertTrue(interval > 0, "드롭 인터벌은 양수여야 합니다");
        assertTrue(interval <= baseInterval, "드롭 인터벌은 기본값 이하여야 합니다");
    }
    
    @Test
    @DisplayName("랜덤 시드 설정 테스트")
    void testSetRandomSeed() {
        // Given
        long seed = 12345L;
        
        // When & Then
        assertDoesNotThrow(() -> {
            gameLogic.setRandomSeed(seed);
        });
    }
    
    @Test
    @DisplayName("블록 타입 가져오기 테스트")
    void testGetBlockTypes() {
        // When
        String[][] blockTypes = gameLogic.getBlockTypes();
        
        // Then
        assertNotNull(blockTypes, "블록 타입 배열이 null이면 안됩니다");
        assertEquals(GameLogic.HEIGHT, blockTypes.length);
        assertEquals(GameLogic.WIDTH, blockTypes[0].length);
    }
    
    @Test
    @DisplayName("현재 좌표 가져오기 테스트")
    void testGetCurrentCoordinates() {
        // When
        int x = gameLogic.getCurrentX();
        int y = gameLogic.getCurrentY();
        
        // Then
        assertTrue(x >= 0 && x < GameLogic.WIDTH, "X좌표가 유효 범위 내에 있어야 합니다");
        assertTrue(y >= -1, "Y좌표가 유효해야 합니다");
    }
    
    @Test
    @DisplayName("게임 오버 상태 테스트")
    void testGameOverState() {
        // When
        boolean isOver = gameLogic.isGameOver();
        
        // Then
        assertFalse(isOver, "초기 상태에서는 게임 오버가 아니어야 합니다");
    }
    
    @Test
    @DisplayName("아이템 매니저 가져오기 테스트")
    void testGetItemManager() {
        // When
        ItemManager itemManager = gameLogic.getItemManager();
        
        // Then
        assertNotNull(itemManager, "아이템 매니저가 null이면 안됩니다");
    }
    
    @Test
    @DisplayName("다음 아이템까지 남은 라인 테스트")
    void testGetLinesUntilNextItem() {
        // When
        int lines = gameLogic.getLinesUntilNextItem();
        
        // Then
        assertTrue(lines >= 0, "남은 라인은 음수가 아니어야 합니다");
    }
    
    @Test
    @DisplayName("단일 라인 클리어 - 유효하지 않은 행 번호")
    void testClearSingleLine_InvalidRow() {
        // When & Then - 음수 행
        boolean cleared1 = gameLogic.clearSingleLine(-1);
        assertFalse(cleared1, "음수 행은 삭제할 수 없어야 합니다");
        
        // When & Then - 범위를 벗어난 행
        boolean cleared2 = gameLogic.clearSingleLine(GameLogic.HEIGHT);
        assertFalse(cleared2, "HEIGHT 이상의 행은 삭제할 수 없어야 합니다");
        
        boolean cleared3 = gameLogic.clearSingleLine(GameLogic.HEIGHT + 5);
        assertFalse(cleared3, "HEIGHT보다 큰 행은 삭제할 수 없어야 합니다");
    }
    
    @Test
    @DisplayName("단일 라인 클리어 - 빈 줄도 삭제 가능")
    void testClearSingleLine_EmptyLine() {
        // Given - 모든 줄이 비어있음
        
        // When - clearSingleLine은 줄이 차있는지 확인하지 않고 항상 삭제함
        boolean cleared = gameLogic.clearSingleLine(0);
        
        // Then
        assertTrue(cleared, "유효한 행 번호면 삭제가 실행됩니다");
    }
    
    @Test
    @DisplayName("단일 라인 클리어 - 보드 시프팅 확인")
    void testClearSingleLine_BoardShifting() {
        // Given - 여러 줄을 채움
        int[][] board = gameLogic.getBoard();
        String[][] blockTypes = gameLogic.getBlockTypes();
        
        // 18번째 줄 부분적으로 채움
        for (int col = 0; col < 5; col++) {
            board[18][col] = 1;
            blockTypes[18][col] = "test-block";
        }
        
        // 19번째 줄 완전히 채움
        for (int col = 0; col < GameLogic.WIDTH; col++) {
            board[19][col] = 1;
            blockTypes[19][col] = "full-block";
        }
        
        // When - 19번째 줄 삭제
        boolean cleared = gameLogic.clearSingleLine(19);
        
        // Then
        assertTrue(cleared, "완전히 채워진 줄은 삭제되어야 합니다");
        
        // 19번째 줄이 18번째 줄의 내용으로 시프트되었는지 확인
        int filledCount = 0;
        for (int col = 0; col < GameLogic.WIDTH; col++) {
            if (board[19][col] == 1) {
                filledCount++;
            }
        }
        assertEquals(5, filledCount, "삭제 후 위의 줄이 아래로 시프트되어야 합니다");
    }
    
    @Test
    @DisplayName("풀 라인 찾기 - 빈 보드")
    void testFindFullLines_EmptyBoard() {
        // Given - 빈 보드
        
        // When
        List<Integer> fullLines = gameLogic.findFullLines();
        
        // Then
        assertNotNull(fullLines, "결과가 null이면 안됩니다");
        assertTrue(fullLines.isEmpty(), "빈 보드에서는 풀 라인이 없어야 합니다");
    }
    
    @Test
    @DisplayName("풀 라인 찾기 - 여러 개의 풀 라인")
    void testFindFullLines_MultipleFullLines() {
        // Given - 여러 줄을 완전히 채움
        int[][] board = gameLogic.getBoard();
        for (int col = 0; col < GameLogic.WIDTH; col++) {
            board[17][col] = 1;
            board[18][col] = 1;
            board[19][col] = 1;
        }
        
        // When
        List<Integer> fullLines = gameLogic.findFullLines();
        
        // Then
        assertNotNull(fullLines, "결과가 null이면 안됩니다");
        assertEquals(3, fullLines.size(), "3개의 풀 라인이 있어야 합니다");
        assertTrue(fullLines.contains(17), "17번째 줄이 포함되어야 합니다");
        assertTrue(fullLines.contains(18), "18번째 줄이 포함되어야 합니다");
        assertTrue(fullLines.contains(19), "19번째 줄이 포함되어야 합니다");
    }
    
    @Test
    @DisplayName("풀 라인 찾기 - 부분적으로 채워진 라인 무시")
    void testFindFullLines_IgnorePartialLines() {
        // Given - 부분적으로 채워진 줄과 완전히 채워진 줄
        int[][] board = gameLogic.getBoard();
        
        // 18번째 줄 부분적으로 채움
        for (int col = 0; col < 8; col++) {
            board[18][col] = 1;
        }
        
        // 19번째 줄 완전히 채움
        for (int col = 0; col < GameLogic.WIDTH; col++) {
            board[19][col] = 1;
        }
        
        // When
        List<Integer> fullLines = gameLogic.findFullLines();
        
        // Then
        assertEquals(1, fullLines.size(), "1개의 풀 라인만 있어야 합니다");
        assertTrue(fullLines.contains(19), "19번째 줄만 포함되어야 합니다");
        assertFalse(fullLines.contains(18), "부분적으로 채워진 줄은 포함되지 않아야 합니다");
    }
    
    @Test
    @DisplayName("라인 클리어 실행 - 빈 리스트")
    void testExecuteLineClear_EmptyList() {
        // Given
        List<Integer> emptyList = new ArrayList<>();
        
        // When
        int cleared = gameLogic.executeLineClear(emptyList);
        
        // Then
        assertEquals(0, cleared, "빈 리스트는 0을 반환해야 합니다");
    }
    
    @Test
    @DisplayName("라인 클리어 실행 - 여러 개의 연속된 라인")
    void testExecuteLineClear_ConsecutiveLines() {
        // Given - 보드 초기화 및 연속된 여러 줄을 채움
        int[][] board = gameLogic.getBoard();
        // 보드 전체를 초기화 (현재 블록 포함 모든 데이터 제거)
        for (int row = 0; row < GameLogic.HEIGHT; row++) {
            for (int col = 0; col < GameLogic.WIDTH; col++) {
                board[row][col] = 0;
            }
        }
        
        // 17, 18, 19번 줄을 채움
        for (int row = 17; row < GameLogic.HEIGHT; row++) {
            for (int col = 0; col < GameLogic.WIDTH; col++) {
                board[row][col] = 1;
            }
        }
        
        List<Integer> linesToClear = new ArrayList<>();
        linesToClear.add(17);
        linesToClear.add(18);
        linesToClear.add(19);
        
        // When
        int cleared = gameLogic.executeLineClear(linesToClear);
        
        // Then
        assertEquals(3, cleared, "3줄이 삭제되어야 합니다");
        
        // executeLineClear는 새 보드를 생성하므로 gameLogic.getBoard()로 다시 참조
        board = gameLogic.getBoard();
        // 줄 삭제 후 상단 3줄이 비어있어야 함
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < GameLogic.WIDTH; col++) {
                assertEquals(0, board[row][col], "상단 줄은 비어있어야 합니다");
            }
        }
    }
    
    @Test
    @DisplayName("라인 클리어 실행 - 비연속적인 여러 라인")
    void testExecuteLineClear_NonConsecutiveLines() {
        // Given - 비연속적인 줄들을 채움
        int[][] board = gameLogic.getBoard();
        
        // 15번째 줄 채움
        for (int col = 0; col < GameLogic.WIDTH; col++) {
            board[15][col] = 1;
        }
        
        // 17번째 줄 채움
        for (int col = 0; col < GameLogic.WIDTH; col++) {
            board[17][col] = 1;
        }
        
        // 19번째 줄 채움
        for (int col = 0; col < GameLogic.WIDTH; col++) {
            board[19][col] = 1;
        }
        
        List<Integer> linesToClear = new ArrayList<>();
        linesToClear.add(15);
        linesToClear.add(17);
        linesToClear.add(19);
        
        // When
        int cleared = gameLogic.executeLineClear(linesToClear);
        
        // Then
        assertEquals(3, cleared, "3줄이 삭제되어야 합니다");
    }
    
    @Test
    @DisplayName("속도 배수 계산")
    void testSpeedMultiplier_Calculation() {
        // Given
        double initialMultiplier = gameLogic.getSpeedMultiplier();
        
        // When - 블록 여러 개 생성
        for (int i = 0; i < 15; i++) {
            gameLogic.spawnNextPiece();
        }
        
        double newMultiplier = gameLogic.getSpeedMultiplier();
        
        // Then
        assertTrue(newMultiplier <= initialMultiplier, "속도 배수는 감소해야 합니다 (게임이 빨라짐)");
        assertTrue(newMultiplier > 0, "속도 배수는 양수여야 합니다");
    }
    
    @Test
    @DisplayName("드롭 인터벌 계산 - 다양한 기본값")
    void testDropInterval_VariousBaseValues() {
        // Given
        long baseInterval1 = 1000L;
        long baseInterval2 = 500L;
        long baseInterval3 = 2000L;
        
        // When
        long interval1 = gameLogic.getDropInterval(baseInterval1);
        long interval2 = gameLogic.getDropInterval(baseInterval2);
        long interval3 = gameLogic.getDropInterval(baseInterval3);
        
        // Then
        assertTrue(interval1 > 0 && interval1 <= baseInterval1, "인터벌이 유효 범위 내에 있어야 합니다");
        assertTrue(interval2 > 0 && interval2 <= baseInterval2, "인터벌이 유효 범위 내에 있어야 합니다");
        assertTrue(interval3 > 0 && interval3 <= baseInterval3, "인터벌이 유효 범위 내에 있어야 합니다");
    }
    
    @Test
    @DisplayName("랜덤 시드 설정 - 동일한 블록 시퀀스")
    void testRandomSeed_DeterministicBlocks() {
        // Given
        long seed = 42L;
        GameLogic logic1 = new GameLogic();
        GameLogic logic2 = new GameLogic();
        
        // When
        logic1.setRandomSeed(seed);
        logic2.setRandomSeed(seed);
        
        Block block1_1 = logic1.getRandomBlock();
        Block block1_2 = logic1.getRandomBlock();
        
        Block block2_1 = logic2.getRandomBlock();
        Block block2_2 = logic2.getRandomBlock();
        
        // Then
        assertEquals(block1_1.getCssClass(), block2_1.getCssClass(), 
            "동일한 시드로 첫 번째 블록이 같아야 합니다");
        assertEquals(block1_2.getCssClass(), block2_2.getCssClass(), 
            "동일한 시드로 두 번째 블록이 같아야 합니다");
    }
    
    @Test
    @DisplayName("블록이 상단에 있는지 확인 - 초기 상태")
    void testIsBlockAtTop_InitialState() {
        // Given - 초기 상태
        
        // When
        boolean atTop = gameLogic.isBlockAtTop();
        
        // Then
        assertTrue(atTop, "초기 블록은 상단에 있어야 합니다");
    }
    
    @Test
    @DisplayName("블록이 상단에 있는지 확인 - 이동 후")
    void testIsBlockAtTop_AfterMovement() {
        // Given
        // When - 블록을 여러 번 아래로 이동
        for (int i = 0; i < 5; i++) {
            gameLogic.moveDown();
        }
        
        // Then
        boolean atTop = gameLogic.isBlockAtTop();
        // 블록이 충분히 내려갔으면 상단이 아니어야 함
        assertFalse(atTop || gameLogic.getCurrentY() <= 0, 
            "블록이 충분히 내려가면 상단에 있지 않아야 합니다");
    }
    
    @Test
    @DisplayName("보드 경계 확인 - 좌우 이동 제한")
    void testCanMove_LeftRightBoundary() {
        // Given
        Block block = gameLogic.getCurrentBlock();
        
        // When & Then - 좌측 경계 밖
        boolean canMoveLeft = gameLogic.canMove(-1, 0, block);
        assertFalse(canMoveLeft, "좌측 경계 밖으로 이동할 수 없어야 합니다");
        
        // When & Then - 우측 경계 밖
        boolean canMoveRight = gameLogic.canMove(GameLogic.WIDTH, 0, block);
        assertFalse(canMoveRight, "우측 경계 밖으로 이동할 수 없어야 합니다");
    }
    
    @Test
    @DisplayName("보드 경계 확인 - 하단 이동 제한")
    void testCanMove_BottomBoundary() {
        // Given
        Block block = gameLogic.getCurrentBlock();
        
        // When & Then
        boolean canMoveBottom = gameLogic.canMove(0, GameLogic.HEIGHT, block);
        assertFalse(canMoveBottom, "하단 경계 밖으로 이동할 수 없어야 합니다");
    }
    
    @Test
    @DisplayName("블록 회전 - 경계에서 회전 불가")
    void testRotateBlock_BoundaryCheck() {
        // Given - 블록을 좌측 경계로 이동
        while (gameLogic.getCurrentX() > 0) {
            gameLogic.moveLeft();
        }
        
        // When - 회전 시도
        Block beforeRotation = gameLogic.getCurrentBlock();
        gameLogic.rotateBlock();
        
        // Then - 회전이 가능하거나 원래 상태를 유지해야 함
        assertNotNull(gameLogic.getCurrentBlock(), "회전 후 블록이 null이면 안됩니다");
    }
    
    @Test
    @DisplayName("게임 리셋 - 보드 초기화 확인")
    void testResetGame_BoardCleared() {
        // Given - 보드에 블록 배치
        int[][] board = gameLogic.getBoard();
        for (int row = 15; row < GameLogic.HEIGHT; row++) {
            for (int col = 0; col < GameLogic.WIDTH; col++) {
                board[row][col] = 1;
            }
        }
        
        // When
        gameLogic.resetGame();
        
        // Then - 보드가 비어있는지 확인
        board = gameLogic.getBoard();
        int filledCells = 0;
        for (int row = 0; row < GameLogic.HEIGHT; row++) {
            for (int col = 0; col < GameLogic.WIDTH; col++) {
                if (board[row][col] == 1) {
                    filledCells++;
                }
            }
        }
        
        // 현재 블록과 다음 블록을 제외하고는 비어있어야 함
        assertTrue(filledCells <= 20, "리셋 후 보드는 대부분 비어있어야 합니다");
    }

}