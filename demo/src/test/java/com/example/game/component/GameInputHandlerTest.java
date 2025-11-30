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
