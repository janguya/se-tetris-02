package com.example.game.component;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * SingleBoard 클래스의 단위 테스트
 */
public class SingleBoardTest {
    
    private SingleBoard singleBoard;
    private boolean gameOverCalled;
    private boolean gameEndCalled;
    
    @BeforeEach
    public void setUp() {
        // JavaFX Platform 초기화
        try {
            javafx.application.Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Platform already initialized
        }
        
        gameOverCalled = false;
        gameEndCalled = false;
        
        singleBoard = new SingleBoard(new SingleBoard.SingleGameCallback() {
            @Override
            public void onGameOver(int score, int linesCleared) {
                gameOverCalled = true;
            }
            
            @Override
            public void onGameEnd() {
                gameEndCalled = true;
            }
        });
    }
    
    @Test
    public void testSingleBoardCreation() {
        assertNotNull(singleBoard, "SingleBoard가 생성되어야 함");
    }
    
    @Test
    public void testCallbackNotCalledInitially() {
        assertFalse(gameOverCalled, "초기에는 게임 오버 콜백이 호출되지 않아야 함");
        assertFalse(gameEndCalled, "초기에는 게임 종료 콜백이 호출되지 않아야 함");
    }
    
    @Test
    public void testOnMoveLeft() {
        assertDoesNotThrow(() -> {
            singleBoard.onMoveLeft();
        });
    }
    
    @Test
    public void testOnMoveRight() {
        assertDoesNotThrow(() -> {
            singleBoard.onMoveRight();
        });
    }
    
    @Test
    public void testOnMoveDown() {
        assertDoesNotThrow(() -> {
            singleBoard.onMoveDown();
        });
    }
    
    @Test
    public void testOnRotate() {
        assertDoesNotThrow(() -> {
            singleBoard.onRotate();
        });
    }
    
    @Test
    public void testOnHardDrop() {
        assertDoesNotThrow(() -> {
            singleBoard.onHardDrop();
        });
    }
    
    @Test
    public void testRestartGame() {
        assertDoesNotThrow(() -> {
            singleBoard.restartGame();
        });
    }
    
    @Test
    public void testMultipleOperations() {
        assertDoesNotThrow(() -> {
            singleBoard.onMoveLeft();
            singleBoard.onMoveRight();
            singleBoard.onRotate();
            singleBoard.onMoveDown();
        });
    }
}
