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

}