package com.example.game.ai;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.game.component.PlayerBoard;

import java.util.List;

/**
 * AIPlayer 클래스의 단위 테스트
 */
public class AIPlayerTest {
    
    private PlayerBoard playerBoard;
    private AIPlayer aiPlayer;
    
    @BeforeEach
    public void setUp() {
        // JavaFX Platform 초기화
        try {
            javafx.application.Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Platform already initialized
        }
        
        // 실제 PlayerBoard 생성 (테스트용 콜백)
        playerBoard = new PlayerBoard(1, new TestCallback(), false);
        aiPlayer = new AIPlayer(playerBoard);
    }
    
    @Test
    public void testInitialState() {
        assertNotNull(aiPlayer.getState(), "상태는 null이 아니어야 함");
    }
    
    @Test
    public void testReset() {
        // Reset 호출
        aiPlayer.reset();
        
        assertFalse(aiPlayer.isActive(), "reset 후에는 비활성 상태여야 함");
    }
    
    @Test
    public void testGetState() {
        assertNotNull(aiPlayer.getState(), "getState()는 null을 반환하지 않아야 함");
    }
    
    @Test
    public void testNoExceptionDuringUpdate() {
        // 블록 없이도 예외가 발생하지 않아야 함
        assertDoesNotThrow(() -> {
            for (int i = 0; i < 10; i++) {
                aiPlayer.update();
            }
        });
    }
    
    @Test
    public void testUpdateWithBlock() {
        // 블록 생성
        playerBoard.getGameLogic().spawnNextPiece();
        
        // AI 업데이트
        assertDoesNotThrow(() -> {
            for (int i = 0; i < 5; i++) {
                aiPlayer.update();
            }
        });
    }
    
    /**
     * 테스트용 콜백
     */
    private static class TestCallback implements PlayerBoard.LineClearCallback {
        @Override
        public void onLinesCleared(int playerNumber, int linesCleared, List<String[]> clearedLines) {
            // 테스트용 - 아무 작업 안 함
        }
    }
}

