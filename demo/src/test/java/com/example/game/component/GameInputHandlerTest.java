package com.example.game.component;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.game.component.GameInputHandler.GameInputCallback;
import com.example.settings.GameSettings;

import javafx.application.Platform;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 * GameInputHandler 테스트
 */
public class GameInputHandlerTest {
    
    private GameInputHandler inputHandler;
    private TestGameInputCallback callback;
    private GameSettings gameSettings;
    
    @BeforeAll
    public static void initToolkit() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Already initialized
        }
    }
    
    @BeforeEach
    public void setUp() {
        callback = new TestGameInputCallback();
        inputHandler = new GameInputHandler(callback);
        gameSettings = GameSettings.getInstance();
    }
    
    @Test
    public void testConstructor() {
        assertDoesNotThrow(() -> {
            new GameInputHandler(callback);
        });
    }
    
    @Test
    public void testMoveLeftInput() {
        callback.gameActive = true;
        callback.menuVisible = false;
        
        KeyEvent event = createKeyEvent(gameSettings.getKeyBinding("MOVE_LEFT"));
        boolean handled = inputHandler.handleKeyPressed(event);
        
        assertTrue(handled);
        assertTrue(callback.moveLeftCalled);
    }
    
    @Test
    public void testMoveRightInput() {
        callback.gameActive = true;
        callback.menuVisible = false;
        
        KeyEvent event = createKeyEvent(gameSettings.getKeyBinding("MOVE_RIGHT"));
        boolean handled = inputHandler.handleKeyPressed(event);
        
        assertTrue(handled);
        assertTrue(callback.moveRightCalled);
    }
    
    @Test
    public void testMoveDownInput() {
        callback.gameActive = true;
        callback.menuVisible = false;
        
        KeyEvent event = createKeyEvent(gameSettings.getKeyBinding("MOVE_DOWN"));
        boolean handled = inputHandler.handleKeyPressed(event);
        
        assertTrue(handled);
        assertTrue(callback.moveDownCalled);
    }
    
    @Test
    public void testRotateInput() {
        callback.gameActive = true;
        callback.menuVisible = false;
        
        KeyEvent event = createKeyEvent(gameSettings.getKeyBinding("ROTATE"));
        boolean handled = inputHandler.handleKeyPressed(event);
        
        assertTrue(handled);
        assertTrue(callback.rotateCalled);
    }
    
    @Test
    public void testHardDropInput() {
        callback.gameActive = true;
        callback.menuVisible = false;
        
        KeyEvent event = createKeyEvent(gameSettings.getKeyBinding("HARD_DROP"));
        boolean handled = inputHandler.handleKeyPressed(event);
        
        assertTrue(handled);
        assertTrue(callback.hardDropCalled);
    }
    
    @Test
    public void testPauseInput() {
        callback.gameActive = true;
        callback.menuVisible = false;
        
        KeyEvent event = createKeyEvent(gameSettings.getKeyBinding("PAUSE"));
        boolean handled = inputHandler.handleKeyPressed(event);
        
        assertTrue(handled);
        assertTrue(callback.pauseCalled);
    }
    
    @Test
    public void testEscapeKeyWhenGameActive() {
        callback.gameActive = true;
        callback.menuVisible = false;
        callback.paused = false;
        
        KeyEvent event = createKeyEvent(KeyCode.ESCAPE);
        boolean handled = inputHandler.handleKeyPressed(event);
        
        assertTrue(handled);
        assertTrue(callback.pauseCalled);
    }
    
    @Test
    public void testEscapeKeyWhenMenuVisible() {
        callback.gameActive = true;
        callback.menuVisible = true;
        
        KeyEvent event = createKeyEvent(KeyCode.ESCAPE);
        boolean handled = inputHandler.handleKeyPressed(event);
        
        assertTrue(handled);
        assertTrue(callback.settingsCalled);
    }
    
    @Test
    public void testInputIgnoredWhenMenuVisible() {
        callback.gameActive = true;
        callback.menuVisible = true;
        
        KeyEvent event = createKeyEvent(gameSettings.getKeyBinding("MOVE_LEFT"));
        boolean handled = inputHandler.handleKeyPressed(event);
        
        assertFalse(handled);
        assertFalse(callback.moveLeftCalled);
    }
    
    @Test
    public void testInputIgnoredWhenGameInactive() {
        callback.gameActive = false;
        callback.menuVisible = false;
        
        KeyEvent event = createKeyEvent(gameSettings.getKeyBinding("MOVE_LEFT"));
        boolean handled = inputHandler.handleKeyPressed(event);
        
        assertFalse(handled);
        assertFalse(callback.moveLeftCalled);
    }
    
    @Test
    public void testPauseAllowedWhenGameInactive() {
        callback.gameActive = false;
        callback.menuVisible = false;
        
        // Use ESCAPE key which always works for pause
        KeyEvent event = createKeyEvent(KeyCode.ESCAPE);
        boolean handled = inputHandler.handleKeyPressed(event);
        
        assertTrue(handled);
        // ESCAPE triggers settings when game is not active
        assertTrue(callback.settingsCalled || callback.pauseCalled);
    }
    
    @Test
    public void testRefreshKeyBindings() {
        assertDoesNotThrow(() -> {
            inputHandler.refreshKeyBindings();
        });
    }
    
    @Test
    public void testGetKeyBindingsInfo() {
        String info = inputHandler.getKeyBindingsInfo();
        assertNotNull(info);
        assertTrue(info.contains("Current Key Bindings"));
        assertTrue(info.contains("Move Left"));
        assertTrue(info.contains("Move Right"));
    }
    
    @Test
    public void testEscapeWhenPaused() {
        callback.gameActive = true;
        callback.menuVisible = false;
        callback.paused = true;
        
        KeyEvent event = createKeyEvent(KeyCode.ESCAPE);
        boolean handled = inputHandler.handleKeyPressed(event);
        
        assertTrue(handled);
        assertTrue(callback.pauseCalled);
    }
    
    @Test
    public void testMultipleInputs() {
        callback.gameActive = true;
        callback.menuVisible = false;
        
        inputHandler.handleKeyPressed(createKeyEvent(gameSettings.getKeyBinding("MOVE_LEFT")));
        assertTrue(callback.moveLeftCalled);
        
        callback.reset();
        inputHandler.handleKeyPressed(createKeyEvent(gameSettings.getKeyBinding("ROTATE")));
        assertTrue(callback.rotateCalled);
        
        callback.reset();
        inputHandler.handleKeyPressed(createKeyEvent(gameSettings.getKeyBinding("HARD_DROP")));
        assertTrue(callback.hardDropCalled);
    }
    
    @Test
    public void testUnknownKeyIgnored() {
        callback.gameActive = true;
        callback.menuVisible = false;
        
        KeyEvent event = createKeyEvent(KeyCode.Q); // Not a game key
        boolean handled = inputHandler.handleKeyPressed(event);
        
        assertFalse(handled);
    }
    
    /**
     * 요구사항: 반복된 키 입력과 블럭의 움직임 사이에 발생하는 지연은 1초 미만이어야 함
     */
    @Test
    public void testKeyPressLatencyUnderOneSecond() {
        callback.gameActive = true;
        callback.menuVisible = false;
        
        KeyCode testKey = gameSettings.getKeyBinding("MOVE_RIGHT");
        
        long startTime = System.nanoTime();
        KeyEvent event = createKeyEvent(testKey);
        boolean handled = inputHandler.handleKeyPressed(event);
        long endTime = System.nanoTime();
        
        long latencyNs = endTime - startTime;
        long latencyMs = latencyNs / 1_000_000;
        
        assertTrue(handled, "키 입력이 처리되어야 함");
        assertTrue(callback.moveRightCalled, "블럭 움직임이 호출되어야 함");
        assertTrue(latencyMs < 1000, 
            String.format("키 입력 처리 지연은 1초 미만이어야 함 (실제: %dms)", latencyMs));
        
        // 실제로는 훨씬 더 빨라야 함 (일반적으로 1ms 미만)
        assertTrue(latencyMs < 100, 
            String.format("키 입력 처리는 100ms 미만이어야 함 (실제: %dms)", latencyMs));
    }
    
    /**
     * 여러 종류의 키 입력에 대한 지연 테스트
     */
    @Test
    public void testMultipleKeyTypesLatency() {
        callback.gameActive = true;
        callback.menuVisible = false;
        
        KeyCode[] testKeys = {
            gameSettings.getKeyBinding("MOVE_LEFT"),
            gameSettings.getKeyBinding("MOVE_RIGHT"),
            gameSettings.getKeyBinding("MOVE_DOWN"),
            gameSettings.getKeyBinding("ROTATE"),
            gameSettings.getKeyBinding("HARD_DROP")
        };
        
        for (KeyCode key : testKeys) {
            callback.reset();
            
            long startTime = System.nanoTime();
            KeyEvent event = createKeyEvent(key);
            boolean handled = inputHandler.handleKeyPressed(event);
            long endTime = System.nanoTime();
            
            long latencyMs = (endTime - startTime) / 1_000_000;
            
            assertTrue(handled, "키 " + key + "가 처리되어야 함");
            assertTrue(latencyMs < 1000, 
                String.format("키 %s 처리 지연은 1초 미만이어야 함 (실제: %dms)", 
                    key, latencyMs));
        }
    }
    
    /**
     * 연속된 키 입력의 평균 지연 테스트
     */
    @Test
    public void testAverageLatencyOfRepeatedKeys() throws InterruptedException {
        callback.gameActive = true;
        callback.menuVisible = false;
        
        int repeatCount = 10;
        long totalLatencyNs = 0;
        KeyCode testKey = gameSettings.getKeyBinding("MOVE_LEFT");
        
        for (int i = 0; i < repeatCount; i++) {
            callback.reset();
            
            long startTime = System.nanoTime();
            KeyEvent event = createKeyEvent(testKey);
            inputHandler.handleKeyPressed(event);
            long endTime = System.nanoTime();
            
            totalLatencyNs += (endTime - startTime);
            
            if (i < repeatCount - 1) {
                Thread.sleep(50); // 50ms 간격
            }
        }
        
        long avgLatencyMs = (totalLatencyNs / repeatCount) / 1_000_000;
        
        assertTrue(avgLatencyMs < 1000, 
            String.format("평균 키 입력 처리 지연은 1초 미만이어야 함 (실제: %dms)", avgLatencyMs));
        assertTrue(avgLatencyMs < 10, 
            String.format("평균 키 입력 처리는 10ms 미만이어야 함 (실제: %dms)", avgLatencyMs));
    }
    
    /**
     * 1초에 3번의 키 입력이 모두 다른 동작으로 처리되는지 테스트
     */
    @Test
    public void testThreeDifferentKeysPerSecond() throws InterruptedException {
        callback.gameActive = true;
        callback.menuVisible = false;
        
        KeyCode[] keys = {
            gameSettings.getKeyBinding("MOVE_LEFT"),
            gameSettings.getKeyBinding("ROTATE"),
            gameSettings.getKeyBinding("MOVE_RIGHT")
        };
        
        int successCount = 0;
        
        for (int i = 0; i < keys.length; i++) {
            callback.reset();
            KeyEvent event = createKeyEvent(keys[i]);
            boolean handled = inputHandler.handleKeyPressed(event);
            
            if (handled) {
                successCount++;
            }
            
            if (i < keys.length - 1) {
                Thread.sleep(333); // ~1초에 3번
            }
        }
        
        assertEquals(3, successCount, "1초에 3번의 서로 다른 키 입력이 모두 처리되어야 함");
    }
    
    /**
     * 빠른 연속 입력 시 모든 입력이 처리되는지 테스트
     */
    @Test
    public void testRapidSuccessiveInputs() {
        callback.gameActive = true;
        callback.menuVisible = false;
        
        KeyCode testKey = gameSettings.getKeyBinding("MOVE_DOWN");
        int rapidPressCount = 3;
        int handledCount = 0;
        
        for (int i = 0; i < rapidPressCount; i++) {
            callback.reset();
            KeyEvent event = createKeyEvent(testKey);
            boolean handled = inputHandler.handleKeyPressed(event);
            
            if (handled && callback.moveDownCalled) {
                handledCount++;
            }
        }
        
        assertEquals(rapidPressCount, handledCount,
            "빠른 연속 입력 시 모든 키가 처리되어야 함");
    }
    
    /**
     * 최대 응답 시간이 허용 범위 내인지 테스트
     */
    @Test
    public void testMaximumResponseTime() {
        callback.gameActive = true;
        callback.menuVisible = false;
        
        int testCount = 100;
        long maxLatencyNs = 0;
        KeyCode testKey = gameSettings.getKeyBinding("ROTATE");
        
        for (int i = 0; i < testCount; i++) {
            callback.reset();
            
            long startTime = System.nanoTime();
            KeyEvent event = createKeyEvent(testKey);
            inputHandler.handleKeyPressed(event);
            long endTime = System.nanoTime();
            
            long latency = endTime - startTime;
            maxLatencyNs = Math.max(maxLatencyNs, latency);
        }
        
        long maxLatencyMs = maxLatencyNs / 1_000_000;
        
        assertTrue(maxLatencyMs < 1000,
            String.format("최대 응답 시간은 1초 미만이어야 함 (실제: %dms)", maxLatencyMs));
        assertTrue(maxLatencyMs < 50,
            String.format("최대 응답 시간은 50ms 미만이어야 함 (실제: %dms)", maxLatencyMs));
    }
    
    // Helper method to create KeyEvent
    private KeyEvent createKeyEvent(KeyCode keyCode) {
        return new KeyEvent(
            KeyEvent.KEY_PRESSED,
            "",
            "",
            keyCode,
            false,
            false,
            false,
            false
        );
    }
    
    // Test callback implementation
    private static class TestGameInputCallback implements GameInputCallback {
        boolean moveLeftCalled = false;
        boolean moveRightCalled = false;
        boolean moveDownCalled = false;
        boolean rotateCalled = false;
        boolean hardDropCalled = false;
        boolean pauseCalled = false;
        boolean settingsCalled = false;
        
        boolean gameActive = true;
        boolean menuVisible = false;
        boolean paused = false;
        
        @Override
        public void onMoveLeft() {
            moveLeftCalled = true;
        }
        
        @Override
        public void onMoveRight() {
            moveRightCalled = true;
        }
        
        @Override
        public void onMoveDown() {
            moveDownCalled = true;
        }
        
        @Override
        public void onRotate() {
            rotateCalled = true;
        }
        
        @Override
        public void onHardDrop() {
            hardDropCalled = true;
        }
        
        @Override
        public void onPause() {
            pauseCalled = true;
        }
        
        @Override
        public void onSettings() {
            settingsCalled = true;
        }
        
        @Override
        public boolean isGameActive() {
            return gameActive;
        }
        
        @Override
        public boolean isMenuVisible() {
            return menuVisible;
        }
        
        @Override
        public boolean isPaused() {
            return paused;
        }
        
        void reset() {
            moveLeftCalled = false;
            moveRightCalled = false;
            moveDownCalled = false;
            rotateCalled = false;
            hardDropCalled = false;
            pauseCalled = false;
            settingsCalled = false;
        }
    }
}
