package com.example.game.component;

import com.example.game.blocks.Block;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

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
    
    // @Test
    // @DisplayName("새 블록 생성 테스트")
    // void testSpawnNewBlock() {
    //     // Given
    //     Block initialNextBlock = gameLogic.getNextBlock();
    //     int initialBlockCount = gameLogic.getTotalBlocksSpawned();
        
    //     // When
    //     boolean spawned = gameLogic.spawnNextPiece();
        
    //     // Then
    //     assertTrue(spawned, "블록 생성이 실패했습니다");
    //     assertEquals(initialBlockCount + 1, gameLogic.getTotalBlocksSpawned(), 
    //                 "블록 생성 카운트가 증가하지 않았습니다");
    //     assertNotEquals(initialNextBlock, gameLogic.getNextBlock(), 
    //                    "다음 블록이 새로 생성되지 않았습니다");
    // }
    
    // @Test
    // @DisplayName("속도 계산 테스트")
    // void testSpeedCalculation() {
    //     // Given & When
    //     double initialSpeedMultiplier = gameLogic.getSpeedMultiplier();
        
    //     // Then
    //     assertEquals(1.0, initialSpeedMultiplier, 0.001, 
    //                 "초기 속도 배수는 1.0이어야 합니다");
        
    //     // Given - 블록을 여러 개 생성하여 속도 레벨 증가시키기
    //     for (int i = 0; i < 10; i++) {
    //         gameLogic.spawnNextPiece();
    //     }
        
    //     // When
    //     double newSpeedMultiplier = gameLogic.getSpeedMultiplier();
        
    //     // Then
    //     assertTrue(newSpeedMultiplier < initialSpeedMultiplier, 
    //               "블록 수가 증가하면 속도가 빨라져야 합니다 (배수가 작아져야 함)");
    // }
    
    @Test
    @DisplayName("줄 삭제 테스트 준비")
    void testClearLinesSetup() {
        // Given & When
        int clearedLines = gameLogic.clearLines();
        
        // Then
        assertEquals(0, clearedLines, "초기 상태에서는 삭제할 줄이 없어야 합니다");
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
    @DisplayName("랜덤 블록 생성 테스트")
    void testRandomBlockGeneration() {
        // Given & When - 여러 블록 생성
        boolean hasVariation = false;
        String firstBlockType = gameLogic.getRandomBlock().getCssClass();
        
        for (int i = 0; i < 20; i++) {
            Block randomBlock = gameLogic.getRandomBlock();
            assertNotNull(randomBlock, "랜덤 블록이 null이면 안됩니다");
            
            if (!randomBlock.getCssClass().equals(firstBlockType)) {
                hasVariation = true;
            }
        }
        
        // Then
        assertTrue(hasVariation, "랜덤 블록 생성에 다양성이 있어야 합니다");
    }
    
    // @Test
    // @DisplayName("드롭 간격 계산 테스트")
    // void testDropIntervalCalculation() {
    //     // Given
    //     long baseInterval = 1000L; // 1초
        
    //     // When
    //     long dropInterval = gameLogic.getDropInterval(baseInterval);
        
    //     // Then
    //     assertEquals(baseInterval, dropInterval, "초기 드롭 간격은 기본값과 같아야 합니다");
        
    //     // Given - 속도 레벨 증가시키기
    //     for (int i = 0; i < 10; i++) {
    //         gameLogic.spawnNextPiece();
    //     }
        
    //     // When
    //     long fasterDropInterval = gameLogic.getDropInterval(baseInterval);
        
    //     // Then
    //     assertTrue(fasterDropInterval < baseInterval, 
    //               "속도가 증가하면 드롭 간격이 짧아져야 합니다");
    // }
}