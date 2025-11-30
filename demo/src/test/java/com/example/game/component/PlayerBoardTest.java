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
    public void setUp() {
        // JavaFX Platform 초기화
        try {
            javafx.application.Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Platform already initialized
        }
        
        lastPlayerNumber = -1;
        lastLinesCleared = 0;
        lastClearedLines = null;
        
        playerBoard = new PlayerBoard(1, this::onLinesCleared, false);
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
    public void testItemMode() {
        PlayerBoard itemBoard = new PlayerBoard(2, this::onLinesCleared, true);
        assertNotNull(itemBoard.getGameLogic(), "아이템 모드에서도 GameLogic이 초기화되어야 함");
    }
}

