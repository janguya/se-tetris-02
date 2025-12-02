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
    
    @Test
    public void testOnPause() {
        assertDoesNotThrow(() -> {
            singleBoard.onPause();
        });
    }
    
    @Test
    public void testMultipleRestarts() {
        assertDoesNotThrow(() -> {
            singleBoard.restartGame();
            singleBoard.restartGame();
            singleBoard.restartGame();
        });
    }
    
    @Test
    public void testPauseMultipleTimes() {
        assertDoesNotThrow(() -> {
            singleBoard.onPause();
            singleBoard.onPause();
            singleBoard.onPause();
        });
    }
    
    @Test
    public void testOperationsAfterRestart() {
        assertDoesNotThrow(() -> {
            singleBoard.restartGame();
            singleBoard.onMoveLeft();
            singleBoard.onRotate();
            singleBoard.onMoveDown();
        });
    }
    
    @Test
    public void testHardDropMultipleTimes() {
        assertDoesNotThrow(() -> {
            singleBoard.onHardDrop();
            Thread.sleep(100);
            singleBoard.onHardDrop();
            Thread.sleep(100);
            singleBoard.onHardDrop();
        });
    }
    
    @Test
    public void testRotateMultipleTimes() {
        assertDoesNotThrow(() -> {
            singleBoard.onRotate();
            singleBoard.onRotate();
            singleBoard.onRotate();
            singleBoard.onRotate();
        });
    }
    
    @Test
    public void testMovementSequence() {
        assertDoesNotThrow(() -> {
            singleBoard.onMoveLeft();
            singleBoard.onMoveLeft();
            singleBoard.onMoveRight();
            singleBoard.onMoveDown();
        });
    }
    
    @Test
    public void testRestartDuringPlay() {
        assertDoesNotThrow(() -> {
            singleBoard.onMoveDown();
            singleBoard.restartGame();
            singleBoard.onMoveLeft();
        });
    }
    
    @Test
    public void testMoveAndRotate() {
        assertDoesNotThrow(() -> {
            singleBoard.onMoveLeft();
            singleBoard.onRotate();
            singleBoard.onMoveRight();
            singleBoard.onRotate();
        });
    }
    
    @Test
    public void testPauseAfterOperations() {
        assertDoesNotThrow(() -> {
            singleBoard.onMoveDown();
            singleBoard.onRotate();
            singleBoard.onPause();
        });
    }
    
    @Test
    public void testOperationsAfterPause() {
        assertDoesNotThrow(() -> {
            singleBoard.onPause();
            singleBoard.onMoveLeft();
            singleBoard.onRotate();
        });
    }
}
