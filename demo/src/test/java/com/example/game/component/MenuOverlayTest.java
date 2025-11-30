package com.example.game.component;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.game.component.MenuOverlay.MenuCallback;
import com.example.settings.GameSettings.Difficulty;

import javafx.application.Platform;

/**
 * MenuOverlay 테스트
 */
public class MenuOverlayTest {
    
    private MenuOverlay menuOverlay;
    private TestMenuCallback callback;
    
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
        Platform.runLater(() -> {
            menuOverlay = new MenuOverlay();
            callback = new TestMenuCallback();
        });
        
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    @Test
    public void testConstructor() {
        assertDoesNotThrow(() -> {
            Platform.runLater(() -> {
                new MenuOverlay();
            });
        });
    }
    
    @Test
    public void testInitiallyNotVisible() {
        Platform.runLater(() -> {
            assertFalse(menuOverlay.isVisible());
        });
        
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    @Test
    public void testShowPauseMenu() {
        Platform.runLater(() -> {
            menuOverlay.showPauseMenu(callback);
            assertTrue(menuOverlay.isVisible());
        });
        
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    @Test
    public void testShowGameOverMenu() {
        Platform.runLater(() -> {
            menuOverlay.showGameOverMenu(callback, 1000, false, Difficulty.NORMAL);
            assertTrue(menuOverlay.isVisible());
        });
        
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    @Test
    public void testShowGameOverMenuWithItemMode() {
        Platform.runLater(() -> {
            menuOverlay.showGameOverMenu(callback, 5000, true, Difficulty.HARD);
            assertTrue(menuOverlay.isVisible());
        });
        
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    @Test
    public void testShowSettingsMenu() {
        Platform.runLater(() -> {
            menuOverlay.showSettingsMenu(callback);
            assertTrue(menuOverlay.isVisible());
        });
        
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    @Test
    public void testHide() {
        Platform.runLater(() -> {
            menuOverlay.showPauseMenu(callback);
            assertTrue(menuOverlay.isVisible());
            
            menuOverlay.hide();
            assertFalse(menuOverlay.isVisible());
        });
        
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    @Test
    public void testGetOverlay() {
        Platform.runLater(() -> {
            assertNotNull(menuOverlay.getOverlay());
        });
        
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    @Test
    public void testMultipleShowHideCycles() {
        Platform.runLater(() -> {
            for (int i = 0; i < 3; i++) {
                menuOverlay.showPauseMenu(callback);
                assertTrue(menuOverlay.isVisible());
                
                menuOverlay.hide();
                assertFalse(menuOverlay.isVisible());
            }
        });
        
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    @Test
    public void testSwitchBetweenMenuTypes() {
        Platform.runLater(() -> {
            menuOverlay.showPauseMenu(callback);
            assertTrue(menuOverlay.isVisible());
            
            menuOverlay.showGameOverMenu(callback, 1000, false, Difficulty.NORMAL);
            assertTrue(menuOverlay.isVisible());
            
            menuOverlay.showSettingsMenu(callback);
            assertTrue(menuOverlay.isVisible());
        });
        
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    @Test
    public void testGameOverMenuWithDifferentScores() {
        Platform.runLater(() -> {
            assertDoesNotThrow(() -> {
                menuOverlay.showGameOverMenu(callback, 0, false, Difficulty.EASY);
                menuOverlay.hide();
                
                menuOverlay.showGameOverMenu(callback, 999999, true, Difficulty.HARD);
                menuOverlay.hide();
                
                menuOverlay.showGameOverMenu(callback, 12345, false, Difficulty.NORMAL);
            });
        });
        
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    @Test
    public void testAllDifficultyLevels() {
        Platform.runLater(() -> {
            for (Difficulty difficulty : Difficulty.values()) {
                assertDoesNotThrow(() -> {
                    menuOverlay.showGameOverMenu(callback, 1000, false, difficulty);
                    menuOverlay.hide();
                });
            }
        });
        
        try {
            Thread.sleep(150);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    // Test callback implementation
    private static class TestMenuCallback implements MenuCallback {
        boolean resumeCalled = false;
        boolean restartCalled = false;
        boolean settingsCalled = false;
        boolean mainMenuCalled = false;
        boolean exitCalled = false;
        
        @Override
        public void onResume() {
            resumeCalled = true;
        }
        
        @Override
        public void onRestart() {
            restartCalled = true;
        }
        
        @Override
        public void onSettings() {
            settingsCalled = true;
        }
        
        @Override
        public void onMainMenu() {
            mainMenuCalled = true;
        }
        
        @Override
        public void onExit() {
            exitCalled = true;
        }
    }
}
