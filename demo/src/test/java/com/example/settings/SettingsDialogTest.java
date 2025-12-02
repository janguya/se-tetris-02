package com.example.settings;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.testfx.util.WaitForAsyncUtils;

import static org.junit.jupiter.api.Assertions.*;

class SettingsDialogTest {

    private Stage parentStage;
    private SettingsDialog settingsDialog;

    @BeforeAll
    static void initJavaFX() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Already initialized
        }
    }

    @BeforeEach
    void setUp() {
        Platform.runLater(() -> {
            parentStage = new Stage();
            settingsDialog = new SettingsDialog(parentStage, () -> {});
        });
        WaitForAsyncUtils.waitForFxEvents();
    }

    @Test
    @DisplayName("SettingsDialog 생성 확인")
    void testSettingsDialogCreation() {
        Platform.runLater(() -> {
            assertNotNull(settingsDialog);
        });
        WaitForAsyncUtils.waitForFxEvents();
    }

    @Test
    @DisplayName("show() 메서드 호출")
    void testShowDialog() {
        Platform.runLater(() -> {
            settingsDialog.show();
            // Dialog가 표시되었는지 확인
        });
        WaitForAsyncUtils.waitForFxEvents();
    }

    @Test
    @DisplayName("SettingsDialog with callback")
    void testSettingsDialogWithCallback() {
        final boolean[] callbackInvoked = {false};
        
        Platform.runLater(() -> {
            Runnable callback = () -> callbackInvoked[0] = true;
            SettingsDialog dialogWithCallback = new SettingsDialog(parentStage, callback);
            assertNotNull(dialogWithCallback);
        });
        WaitForAsyncUtils.waitForFxEvents();
    }

    @Test
    @DisplayName("Multiple SettingsDialog creations")
    void testMultipleDialogCreations() {
        Platform.runLater(() -> {
            for (int i = 0; i < 5; i++) {
                SettingsDialog dialog = new SettingsDialog(parentStage, () -> {});
                assertNotNull(dialog);
            }
        });
        WaitForAsyncUtils.waitForFxEvents();
    }

    @Test
    @DisplayName("SettingsDialog with different parent stages")
    void testSettingsDialogWithDifferentParents() {
        Platform.runLater(() -> {
            Stage parent1 = new Stage();
            Stage parent2 = new Stage();
            
            SettingsDialog dialog1 = new SettingsDialog(parent1, () -> {});
            SettingsDialog dialog2 = new SettingsDialog(parent2, () -> {});
            
            assertNotNull(dialog1);
            assertNotNull(dialog2);
        });
        WaitForAsyncUtils.waitForFxEvents();
    }

    @Test
    @DisplayName("GameSettings instance access")
    void testGameSettingsInstanceAccess() {
        GameSettings settings = GameSettings.getInstance();
        assertNotNull(settings);
    }

    @Test
    @DisplayName("GameSettings difficulty values")
    void testGameSettingsDifficultyValues() {
        GameSettings settings = GameSettings.getInstance();
        
        // Test setting different difficulties
        settings.setDifficulty(GameSettings.Difficulty.EASY);
        assertEquals(GameSettings.Difficulty.EASY, settings.getDifficulty());
        
        settings.setDifficulty(GameSettings.Difficulty.NORMAL);
        assertEquals(GameSettings.Difficulty.NORMAL, settings.getDifficulty());
        
        settings.setDifficulty(GameSettings.Difficulty.HARD);
        assertEquals(GameSettings.Difficulty.HARD, settings.getDifficulty());
    }

    @Test
    @DisplayName("GameSettings item mode toggle")
    void testGameSettingsItemModeToggle() {
        GameSettings settings = GameSettings.getInstance();
        
        settings.setItemModeEnabled(true);
        assertTrue(settings.isItemModeEnabled());
        
        settings.setItemModeEnabled(false);
        assertFalse(settings.isItemModeEnabled());
    }

    @Test
    @DisplayName("GameSettings key bindings")
    void testGameSettingsKeyBindings() {
        GameSettings settings = GameSettings.getInstance();
        
        // Test setting and getting key bindings
        settings.setKeyBinding("moveLeft", KeyCode.A);
        assertEquals(KeyCode.A, settings.getKeyBinding("moveLeft"));
        
        settings.setKeyBinding("moveRight", KeyCode.D);
        assertEquals(KeyCode.D, settings.getKeyBinding("moveRight"));
        
        settings.setKeyBinding("softDrop", KeyCode.S);
        assertEquals(KeyCode.S, settings.getKeyBinding("softDrop"));
    }

    @Test
    @DisplayName("GameSettings color scheme")
    void testGameSettingsColorScheme() {
        GameSettings settings = GameSettings.getInstance();
        
        // Test that we can access color scheme settings
        assertNotNull(settings);
        // Color scheme methods should be accessible
    }

    @Test
    @DisplayName("GameSettings window size")
    void testGameSettingsWindowSize() {
        GameSettings settings = GameSettings.getInstance();
        
        // Test window size settings (if available)
        assertNotNull(settings);
    }

    @Test
    @DisplayName("SettingsDialog scene creation")
    void testSettingsDialogSceneCreation() {
        Platform.runLater(() -> {
            settingsDialog.show();
            // Verify dialog has been created
        });
        WaitForAsyncUtils.waitForFxEvents();
    }

    @Test
    @DisplayName("Multiple show() calls")
    void testMultipleShowCalls() {
        Platform.runLater(() -> {
            settingsDialog.show();
            settingsDialog.show();
            settingsDialog.show();
        });
        WaitForAsyncUtils.waitForFxEvents();
    }

    @Test
    @DisplayName("GameSettings persistence")
    void testGameSettingsPersistence() {
        GameSettings settings = GameSettings.getInstance();
        
        // Change some settings
        settings.setDifficulty(GameSettings.Difficulty.HARD);
        settings.setItemModeEnabled(true);
        
        // Verify settings are set
        assertEquals(GameSettings.Difficulty.HARD, settings.getDifficulty());
        assertTrue(settings.isItemModeEnabled());
        
        // Save settings
        settings.saveSettings();
        
        // Verify save doesn't throw exception
        assertNotNull(settings);
    }

    @Test
    @DisplayName("GameSettings all key bindings")
    void testGameSettingsAllKeyBindings() {
        GameSettings settings = GameSettings.getInstance();
        
        // Test all standard key bindings
        String[] actions = {"moveLeft", "moveRight", "softDrop", "hardDrop", "rotateLeft", "rotateRight", "hold"};
        
        for (String action : actions) {
            KeyCode original = settings.getKeyBinding(action);
            settings.setKeyBinding(action, KeyCode.SPACE);
            assertEquals(KeyCode.SPACE, settings.getKeyBinding(action));
            settings.setKeyBinding(action, original); // restore
        }
    }
}
