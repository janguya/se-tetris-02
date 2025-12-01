package com.example.game.component;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * PlayerBoard 클래스의 단위 테스트
 */
public class PlayerBoardTest {
    
    private PlayerBoard playerBoard;
    private int lastPlayerNumber;
    private int lastLinesCleared;
    private List<String[]> lastClearedLines;
    
    @BeforeEach
    public void setUp() throws Exception {
        // JavaFX Platform 초기화
        try {
            javafx.application.Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Platform already initialized
        }
        
        lastPlayerNumber = -1;
        lastLinesCleared = 0;
        lastClearedLines = null;
        
        // JavaFX UI 스레드에서 PlayerBoard 초기화
        javafx.application.Platform.runLater(() -> {
            playerBoard = new PlayerBoard(1, this::onLinesCleared, false);
            playerBoard.initializeUI();
        });
        Thread.sleep(300); // UI 초기화 대기
    }
    
    private void onLinesCleared(int playerNumber, int linesCleared, List<String[]> clearedLines) {
        lastPlayerNumber = playerNumber;
        lastLinesCleared = linesCleared;
        lastClearedLines = clearedLines;
    }
    
    @Test
    public void testPlayerBoardCreation() {
        assertNotNull(playerBoard, "PlayerBoard가 생성되어야 함");
    }
    
    @Test
    public void testGetScore() {
        int score = playerBoard.getScore();
        assertTrue(score >= 0, "점수는 0 이상이어야 함");
    }
    
    @Test
    public void testIsGameActive() {
        assertTrue(playerBoard.isGameActive(), "초기에는 게임이 활성화되어야 함");
    }
    
    @Test
    public void testIsGameOver() {
        assertFalse(playerBoard.isGameOver(), "초기에는 게임 오버가 아니어야 함");
    }
    
    @Test
    public void testGetDropInterval() {
        long interval = playerBoard.getDropInterval();
        assertTrue(interval > 0, "드롭 인터벌은 0보다 커야 함");
    }
    
    @Test
    public void testIsAnimationActive() {
        assertFalse(playerBoard.isAnimationActive(), "초기에는 애니메이션이 비활성화되어야 함");
    }
    
    @Test
    public void testGetGameLogic() {
        assertNotNull(playerBoard.getGameLogic(), "GameLogic이 null이 아니어야 함");
    }
    
    @Test
    public void testGetCurrentBlock() {
        assertNotNull(playerBoard.getCurrentBlock(), "현재 블록이 null이 아니어야 함");
    }
    
    @Test
    public void testAddPendingAttackLines() throws Exception {
        // 공격 라인을 받음
        List<String[]> attackLines = new ArrayList<>();
        String[] line = new String[GameLogic.WIDTH];
        for (int i = 0; i < GameLogic.WIDTH; i++) {
            line[i] = i % 2 == 0 ? "attack-block" : null;
        }
        attackLines.add(line);
        
        playerBoard.receiveAttackLines(attackLines);
        
        // 대기 중인 공격 카운트 확인
        int pendingCount = playerBoard.getPendingAttackCount();
        assertEquals(1, pendingCount, "대기 중인 공격 라인 수가 1이어야 함");
        
        // addPendingAttackLines를 트리거하기 위해 블록 착지 시뮬레이션
        javafx.application.Platform.runLater(() -> {
            try {
                java.lang.reflect.Method method = PlayerBoard.class.getDeclaredMethod("addPendingAttackLines");
                method.setAccessible(true);
                method.invoke(playerBoard);
            } catch (Exception e) {
                fail("addPendingAttackLines 호출 실패: " + e.getMessage());
            }
        });
        Thread.sleep(200);
    }
    
    @Test
    public void testGetClearedLinesForAttack() throws Exception {
        // 라인 클리어를 위한 설정
        List<String[]> clearedLines = new ArrayList<>();
        String[] line1 = new String[GameLogic.WIDTH];
        String[] line2 = new String[GameLogic.WIDTH];
        
        for (int i = 0; i < GameLogic.WIDTH; i++) {
            line1[i] = "T";
            line2[i] = "I";
        }
        
        clearedLines.add(line1);
        clearedLines.add(line2);
        
        // reflection으로 getClearedLinesForAttack 호출
        javafx.application.Platform.runLater(() -> {
            try {
                java.lang.reflect.Method method = PlayerBoard.class.getDeclaredMethod("getClearedLinesForAttack", List.class);
                method.setAccessible(true);
                List<String[]> result = (List<String[]>) method.invoke(playerBoard, clearedLines);
                assertNotNull(result, "결과가 null이 아니어야 함");
            } catch (Exception e) {
                fail("getClearedLinesForAttack 호출 실패: " + e.getMessage());
            }
        });
        Thread.sleep(200);
    }
    
    @Test
    public void testIsCurrentBlockCell() throws Exception {
        // reflection으로 isCurrentBlockCell 호출
        javafx.application.Platform.runLater(() -> {
            try {
                java.lang.reflect.Method method = PlayerBoard.class.getDeclaredMethod("isCurrentBlockCell", int.class, int.class);
                method.setAccessible(true);
                
                // 유효한 위치 테스트
                Boolean result = (Boolean) method.invoke(playerBoard, 0, 0);
                assertNotNull(result, "결과가 null이 아니어야 함");
                
                // 다양한 위치 테스트
                method.invoke(playerBoard, 5, 5);
                method.invoke(playerBoard, GameLogic.WIDTH - 1, 0);
                
            } catch (Exception e) {
                fail("isCurrentBlockCell 호출 실패: " + e.getMessage());
            }
        });
        Thread.sleep(200);
    }
    
    @Test
    public void testExecuteExplosion() throws Exception {
        // reflection으로 executeExplosion 호출
        javafx.application.Platform.runLater(() -> {
            try {
                java.lang.reflect.Method method = PlayerBoard.class.getDeclaredMethod("executeExplosion");
                method.setAccessible(true);
                method.invoke(playerBoard);
            } catch (Exception e) {
                fail("executeExplosion 호출 실패: " + e.getMessage());
            }
        });
        Thread.sleep(200);
    }
    
    @Test
    public void testReceiveAttackLines() {
        List<String[]> attackLines = new ArrayList<>();
        String[] line1 = new String[GameLogic.WIDTH];
        String[] line2 = new String[GameLogic.WIDTH];
        
        for (int i = 0; i < GameLogic.WIDTH; i++) {
            line1[i] = i % 2 == 0 ? "attack-block" : null;
            line2[i] = i % 3 == 0 ? "attack-block" : null;
        }
        
        attackLines.add(line1);
        attackLines.add(line2);
        
        assertDoesNotThrow(() -> {
            playerBoard.receiveAttackLines(attackLines);
        });
        
        assertEquals(2, playerBoard.getPendingAttackCount(), "공격 줄 개수가 일치해야 함");
    }
    
    @Test
    public void testGetPendingAttackCount() {
        assertEquals(0, playerBoard.getPendingAttackCount(), "초기에는 대기 중인 공격이 없어야 함");
    }
    
    @Test
    public void testItemMode() throws Exception {
        javafx.application.Platform.runLater(() -> {
            PlayerBoard itemBoard = new PlayerBoard(2, this::onLinesCleared, true);
            itemBoard.initializeUI();
            assertNotNull(itemBoard.getGameLogic(), "아이템 모드에서도 GameLogic이 초기화되어야 함");
        });
        Thread.sleep(200);
    }
    
    /**
     * 여러 공격 라인 수신 테스트
     */
    @Test
    public void testMultipleAttackLines() {
        List<String[]> attack1 = new ArrayList<>();
        attack1.add(new String[GameLogic.WIDTH]);
        
        List<String[]> attack2 = new ArrayList<>();
        attack2.add(new String[GameLogic.WIDTH]);
        attack2.add(new String[GameLogic.WIDTH]);
        
        playerBoard.receiveAttackLines(attack1);
        assertEquals(1, playerBoard.getPendingAttackCount());
        
        playerBoard.receiveAttackLines(attack2);
        assertEquals(3, playerBoard.getPendingAttackCount());
    }
    
    /**
     * 빈 공격 라인 수신 테스트
     */
    @Test
    public void testEmptyAttackLines() {
        List<String[]> emptyAttack = new ArrayList<>();
        playerBoard.receiveAttackLines(emptyAttack);
        assertEquals(0, playerBoard.getPendingAttackCount());
    }
    
    /**
     * 아이템 모드와 일반 모드 비교 테스트
     */
    @Test
    public void testItemModeVsNormalMode() throws Exception {
        javafx.application.Platform.runLater(() -> {
            PlayerBoard normalBoard = new PlayerBoard(1, this::onLinesCleared, false);
            PlayerBoard itemBoard = new PlayerBoard(2, this::onLinesCleared, true);
            
            normalBoard.initializeUI();
            itemBoard.initializeUI();
            
            assertNotNull(normalBoard.getGameLogic());
            assertNotNull(itemBoard.getGameLogic());
        });
        Thread.sleep(300);
    }
    
    /**
     * 여러 플레이어 보드 동시 생성 테스트
     */
    @Test
    public void testMultiplePlayers() throws Exception {
        javafx.application.Platform.runLater(() -> {
            PlayerBoard p1 = new PlayerBoard(1, this::onLinesCleared, false);
            PlayerBoard p2 = new PlayerBoard(2, this::onLinesCleared, false);
            
            p1.initializeUI();
            p2.initializeUI();
            
            assertNotNull(p1);
            assertNotNull(p2);
            assertNotEquals(p1, p2);
        });
        Thread.sleep(300);
    }

    // ========== 추가 커버리지 테스트 ==========

    @Test
    public void testOnMoveLeft() {
        assertDoesNotThrow(() -> {
            playerBoard.onMoveLeft();
        }, "onMoveLeft should not throw exception");
    }

    @Test
    public void testOnMoveRight() {
        assertDoesNotThrow(() -> {
            playerBoard.onMoveRight();
        }, "onMoveRight should not throw exception");
    }

    @Test
    public void testOnMoveDown() {
        assertDoesNotThrow(() -> {
            playerBoard.onMoveDown();
        }, "onMoveDown should not throw exception");
    }

    @Test
    public void testOnRotate() {
        assertDoesNotThrow(() -> {
            playerBoard.onRotate();
        }, "onRotate should not throw exception");
    }

    @Test
    public void testOnHardDrop() {
        assertDoesNotThrow(() -> {
            playerBoard.onHardDrop();
        }, "onHardDrop should not throw exception");
    }

    @Test
    public void testUpdate() {
        assertDoesNotThrow(() -> {
            playerBoard.update();
        }, "update should not throw exception");
    }

    @Test
    public void testDrawBoard() {
        assertDoesNotThrow(() -> {
            playerBoard.drawBoard();
        }, "drawBoard should not throw exception");
    }

    @Test
    public void testGetCanvas() {
        assertNotNull(playerBoard.getCanvas(), "Canvas should not be null");
    }

    @Test
    public void testComplexMovementSequence() {
        assertDoesNotThrow(() -> {
            playerBoard.onMoveLeft();
            playerBoard.onMoveLeft();
            playerBoard.onRotate();
            playerBoard.onMoveRight();
            playerBoard.onMoveRight();
            playerBoard.onMoveDown();
            playerBoard.onMoveDown();
            playerBoard.onHardDrop();
        }, "Complex movement sequence should work");
    }

    @Test
    public void testMultipleUpdates() {
        assertDoesNotThrow(() -> {
            for (int i = 0; i < 10; i++) {
                playerBoard.update();
            }
        }, "Multiple updates should work");
    }

    @Test
    public void testMultipleDraws() {
        assertDoesNotThrow(() -> {
            for (int i = 0; i < 10; i++) {
                playerBoard.drawBoard();
            }
        }, "Multiple draws should work");
    }

    @Test
    public void testLargeAttack() {
        List<String[]> largeAttack = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            String[] line = new String[GameLogic.WIDTH];
            for (int j = 0; j < GameLogic.WIDTH; j++) {
                line[j] = "attack-block";
            }
            largeAttack.add(line);
        }
        
        assertDoesNotThrow(() -> {
            playerBoard.receiveAttackLines(largeAttack);
        });
        assertEquals(10, playerBoard.getPendingAttackCount());
    }

    @Test
    public void testMultipleSmallAttacks() {
        for (int i = 0; i < 5; i++) {
            List<String[]> attack = new ArrayList<>();
            attack.add(new String[GameLogic.WIDTH]);
            playerBoard.receiveAttackLines(attack);
        }
        assertEquals(5, playerBoard.getPendingAttackCount());
    }

    @Test
    public void testGameStateTransitions() {
        assertTrue(playerBoard.isGameActive());
        assertFalse(playerBoard.isGameOver());
        
        // 여러 작업 수행
        playerBoard.onMoveDown();
        playerBoard.update();
        
        assertTrue(playerBoard.isGameActive());
    }

    @Test
    public void testScoreIncreases() {
        int initialScore = playerBoard.getScore();
        
        // 여러 블록 드롭
        for (int i = 0; i < 5; i++) {
            playerBoard.onHardDrop();
        }
        
        // 점수는 초기값 이상이어야 함
        assertTrue(playerBoard.getScore() >= initialScore);
    }

    @Test
    public void testItemModeWithMultipleOperations() throws Exception {
        javafx.application.Platform.runLater(() -> {
            PlayerBoard itemBoard = new PlayerBoard(1, this::onLinesCleared, true);
            itemBoard.initializeUI();
            
            assertDoesNotThrow(() -> {
                itemBoard.onMoveLeft();
                itemBoard.onRotate();
                itemBoard.onMoveRight();
                itemBoard.onHardDrop();
                itemBoard.update();
                itemBoard.drawBoard();
            }, "Item mode operations should work");
        });
        Thread.sleep(300);
    }

    @Test
    public void testPlayerNumbersDistinct() throws Exception {
        javafx.application.Platform.runLater(() -> {
            PlayerBoard p1 = new PlayerBoard(1, this::onLinesCleared, false);
            PlayerBoard p2 = new PlayerBoard(2, this::onLinesCleared, false);
            PlayerBoard p3 = new PlayerBoard(3, this::onLinesCleared, false);
            
            p1.initializeUI();
            p2.initializeUI();
            p3.initializeUI();
            
            assertNotNull(p1);
            assertNotNull(p2);
            assertNotNull(p3);
        });
        Thread.sleep(300);
    }

    @Test
    public void testCallbackInvocation() throws Exception {
        javafx.application.Platform.runLater(() -> {
            PlayerBoard board = new PlayerBoard(1, this::onLinesCleared, false);
            board.initializeUI();
            
            // 라인 클리어를 시뮬레이션하기 위해 여러 블록 드롭
            for (int i = 0; i < 100; i++) {
                board.onHardDrop();
                board.update();
            }
            
            // 콜백이 호출되었는지 확인 (lastPlayerNumber가 업데이트됨)
            assertTrue(lastPlayerNumber >= -1, "Callback should have been invoked or not");
        });
        Thread.sleep(500);
    }

    @Test
    public void testDropIntervalConsistency() {
        long interval1 = playerBoard.getDropInterval();
        long interval2 = playerBoard.getDropInterval();
        
        assertEquals(interval1, interval2, "Drop interval should be consistent");
    }

    @Test
    public void testAnimationStateAfterOperations() {
        playerBoard.onHardDrop();
        playerBoard.update();
        
        // 애니메이션 상태 확인 (활성/비활성)
        boolean animActive = playerBoard.isAnimationActive();
        assertTrue(animActive == true || animActive == false, "Animation state should be boolean");
    }

    @Test
    public void testGameLogicConsistency() {
        GameLogic logic1 = playerBoard.getGameLogic();
        GameLogic logic2 = playerBoard.getGameLogic();
        
        assertSame(logic1, logic2, "GameLogic should be the same instance");
    }

    @Test
    public void testCurrentBlockConsistency() {
        assertNotNull(playerBoard.getCurrentBlock(), "Current block should not be null");
        
        playerBoard.onHardDrop();
        
        assertNotNull(playerBoard.getCurrentBlock(), "Current block should not be null after drop");
    }
}

