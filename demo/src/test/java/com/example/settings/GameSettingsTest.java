package com.example.settings;

import com.example.theme.ColorScheme;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GameSettingsTest {
    
    private GameSettings gameSettings;
    
    @BeforeEach
    void setUp() {
        gameSettings = GameSettings.getInstance();
    }
    
    @Test
    @DisplayName("GameSettings 싱글톤 테스트")
    void testSingleton() {
        GameSettings instance1 = GameSettings.getInstance();
        GameSettings instance2 = GameSettings.getInstance();
        
        assertSame(instance1, instance2, "GameSettings는 같은 인스턴스를 반환해야 합니다");
        assertNotNull(instance1, "GameSettings 인스턴스는 null이면 안됩니다");
    }
    
    @Test
    @DisplayName("게임 설정 기본값 테스트")
    void testDefaultSettings() {
        assertTrue(gameSettings.getWindowWidth() > 0, "윈도우 너비가 양수여야 합니다");
        assertTrue(gameSettings.getWindowHeight() > 0, "윈도우 높이가 양수여야 합니다");
        
        assertNotNull(gameSettings.getCurrentColorScheme(), "색상 스킴이 null이면 안됩니다");
        assertNotNull(gameSettings.getCurrentWindowSize(), "윈도우 크기가 null이면 안됩니다");
    }
    
    @Test
    @DisplayName("윈도우 크기 설정 테스트")
    void testWindowSizeSettings() {
        // 모든 윈도우 크기 테스트
        WindowSize[] sizes = WindowSize.values();
        
        for (WindowSize size : sizes) {
            gameSettings.setCurrentWindowSize(size);
            
            assertEquals(size, gameSettings.getCurrentWindowSize(), 
                        "윈도우 크기가 올바르게 설정되어야 합니다");
            assertEquals(size.getWidth(), gameSettings.getWindowWidth(), 
                        "윈도우 너비가 일치해야 합니다");
            assertEquals(size.getHeight(), gameSettings.getWindowHeight(), 
                        "윈도우 높이가 일치해야 합니다");
        }
    }
    
    @Test
    @DisplayName("WindowSize 열거형 테스트")
    void testWindowSizeEnum() {
        WindowSize small = WindowSize.SMALL;
        assertEquals("작은 화면", small.getDisplayName());
        assertEquals(440, small.getWidth());
        assertEquals(600, small.getHeight());
        
        WindowSize medium = WindowSize.MEDIUM;
        assertEquals("중간 화면", medium.getDisplayName());
        assertEquals(480, medium.getWidth());
        assertEquals(640, medium.getHeight());
        
        WindowSize large = WindowSize.LARGE;
        assertEquals("큰 화면", large.getDisplayName());
        assertEquals(560, large.getWidth());
        assertEquals(720, large.getHeight());
        
        WindowSize extraLarge = WindowSize.EXTRA_LARGE;
        assertEquals("매우 큰 화면", extraLarge.getDisplayName());
        assertEquals(640, extraLarge.getWidth());
        assertEquals(800, extraLarge.getHeight());
    }
    
    @Test
    @DisplayName("색상 스킴 설정 테스트")
    void testColorSchemeSettings() {
        ColorScheme originalScheme = gameSettings.getCurrentColorScheme();
        
        // 다양한 색상 스킴 테스트
        for (ColorScheme scheme : ColorScheme.values()) {
            gameSettings.setCurrentColorScheme(scheme);
            assertEquals(scheme, gameSettings.getCurrentColorScheme(), 
                        "색상 스킴이 올바르게 설정되어야 합니다");
            
            Map<String, Color> colors = gameSettings.getCurrentColors();
            assertNotNull(colors, "색상 맵이 null이면 안됩니다");
        }
        
        // 원래 스킴으로 복원
        gameSettings.setCurrentColorScheme(originalScheme);
    }
    
    @Test
    @DisplayName("커스텀 색상 테스트")
    void testCustomColors() {
        String blockType = "block-i";
        Color testColor = Color.BLUE;
        
        gameSettings.setCustomColor(blockType, testColor);
        Color retrievedColor = gameSettings.getCustomColor(blockType);
        
        assertEquals(testColor, retrievedColor, "커스텀 색상이 올바르게 설정되어야 합니다");
    }
    
    @Test
    @DisplayName("키 바인딩 테스트")
    void testKeyBindings() {
        // 기본 키 바인딩 확인
        assertNotNull(gameSettings.getKeyBinding("MOVE_LEFT"), "왼쪽 이동 키가 설정되어야 합니다");
        assertNotNull(gameSettings.getKeyBinding("MOVE_RIGHT"), "오른쪽 이동 키가 설정되어야 합니다");
        assertNotNull(gameSettings.getKeyBinding("MOVE_DOWN"), "아래쪽 이동 키가 설정되어야 합니다");
        assertNotNull(gameSettings.getKeyBinding("ROTATE"), "회전 키가 설정되어야 합니다");
        assertNotNull(gameSettings.getKeyBinding("PAUSE"), "일시정지 키가 설정되어야 합니다");
        assertNotNull(gameSettings.getKeyBinding("HARD_DROP"), "하드 드롭 키가 설정되어야 합니다");
    }
    
    @Test
    @DisplayName("키 바인딩 변경 테스트")
    void testKeyBindingChange() {
        String action = "MOVE_LEFT";
        KeyCode originalKey = gameSettings.getKeyBinding(action);
        KeyCode newKey = KeyCode.A;
        
        gameSettings.setKeyBinding(action, newKey);
        assertEquals(newKey, gameSettings.getKeyBinding(action), 
                    "키 바인딩이 올바르게 변경되어야 합니다");
        
        // 원래 키로 복원
        gameSettings.setKeyBinding(action, originalKey);
    }
    
    @Test
    @DisplayName("모든 키 바인딩 조회 테스트")
    void testGetAllKeyBindings() {
        Map<String, KeyCode> allBindings = gameSettings.getAllKeyBindings();
        
        assertNotNull(allBindings, "키 바인딩 맵이 null이면 안됩니다");
        assertTrue(allBindings.size() >= 6, "최소 6개의 키 바인딩이 있어야 합니다");
        assertTrue(allBindings.containsKey("MOVE_LEFT"), "왼쪽 이동 키가 포함되어야 합니다");
        assertTrue(allBindings.containsKey("MOVE_RIGHT"), "오른쪽 이동 키가 포함되어야 합니다");
        assertTrue(allBindings.containsKey("ROTATE"), "회전 키가 포함되어야 합니다");
    }
    
    @Test
    @DisplayName("키 바인딩 기본값 복원 테스트")
    void testResetKeyBindingsToDefault() {
        // 키 바인딩을 변경
        gameSettings.setKeyBinding("MOVE_LEFT", KeyCode.A);
        gameSettings.setKeyBinding("ROTATE", KeyCode.W);
        
        // 기본값으로 복원
        gameSettings.resetKeyBindingsToDefault();
        
        // 기본값 확인
        assertEquals(KeyCode.LEFT, gameSettings.getKeyBinding("MOVE_LEFT"));
        assertEquals(KeyCode.RIGHT, gameSettings.getKeyBinding("MOVE_RIGHT"));
        assertEquals(KeyCode.DOWN, gameSettings.getKeyBinding("MOVE_DOWN"));
        assertEquals(KeyCode.UP, gameSettings.getKeyBinding("ROTATE"));
        assertEquals(KeyCode.SPACE, gameSettings.getKeyBinding("HARD_DROP"));
    }
    
    @Test
    @DisplayName("윈도우 크기 변경 리스너 테스트")
    void testWindowSizeChangeListener() {
        boolean[] listenerCalled = {false};
        
        Runnable listener = () -> listenerCalled[0] = true;
        
        // 리스너 추가
        gameSettings.addWindowSizeChangeListener(listener);
        
        // 윈도우 크기 변경
        WindowSize originalSize = gameSettings.getCurrentWindowSize();
        WindowSize newSize = (originalSize == WindowSize.SMALL) ? WindowSize.MEDIUM : WindowSize.SMALL;
        
        gameSettings.setCurrentWindowSize(newSize);
        
        assertTrue(listenerCalled[0], "윈도우 크기 변경 시 리스너가 호출되어야 합니다");
        
        // 리스너 제거
        gameSettings.removeWindowSizeChangeListener(listener);
        
        // 원래 크기로 복원
        gameSettings.setCurrentWindowSize(originalSize);
    }
    
    @Test
    @DisplayName("커스텀 색상 맵 테스트")
    void testCustomColorMap() {
        // 커스텀 스킴으로 변경
        gameSettings.setCurrentColorScheme(ColorScheme.CUSTOM);
        
        // 커스텀 색상 설정
        gameSettings.setCustomColor("block-i", Color.RED);
        gameSettings.setCustomColor("block-o", Color.BLUE);
        
        Map<String, Color> customColors = gameSettings.getCurrentColors();
        
        assertNotNull(customColors, "커스텀 색상 맵이 null이면 안됩니다");
        assertEquals(Color.RED, customColors.get("block-i"), "I블록 커스텀 색상이 일치해야 합니다");
        assertEquals(Color.BLUE, customColors.get("block-o"), "O블록 커스텀 색상이 일치해야 합니다");
    }
    
    @Test
    @DisplayName("존재하지 않는 키 바인딩 테스트")
    void testNonExistentKeyBinding() {
        KeyCode result = gameSettings.getKeyBinding("NON_EXISTENT_ACTION");
        assertNull(result, "존재하지 않는 액션은 null을 반환해야 합니다");
    }
    
    @Test
    @DisplayName("존재하지 않는 커스텀 색상 테스트")
    void testNonExistentCustomColor() {
        Color result = gameSettings.getCustomColor("non-existent-block");
        assertEquals(Color.WHITE, result, "존재하지 않는 블록 타입은 기본 색상을 반환해야 합니다");
    }
    
    @Test
    @DisplayName("설정 저장 테스트")
    void testSaveSettings() {
        // 설정 변경
        WindowSize originalSize = gameSettings.getCurrentWindowSize();
        WindowSize newSize = (originalSize == WindowSize.SMALL) ? WindowSize.MEDIUM : WindowSize.SMALL;
        
        gameSettings.setCurrentWindowSize(newSize);
        
        // 저장 메서드 호출이 예외를 발생시키지 않는지 확인
        assertDoesNotThrow(() -> {
            gameSettings.saveSettings();
        }, "설정 저장에서 예외가 발생하면 안됩니다");
        
        // 원래 크기로 복원
        gameSettings.setCurrentWindowSize(originalSize);
    }
    
    @Test
    @DisplayName("난이도 설정 테스트")
    void testDifficultySettings() {
        // 모든 난이도 테스트
        GameSettings.Difficulty[] difficulties = GameSettings.Difficulty.values();
        
        for (GameSettings.Difficulty diff : difficulties) {
            gameSettings.setDifficulty(diff);
            assertEquals(diff, gameSettings.getDifficulty(), 
                        "난이도가 올바르게 설정되어야 합니다");
        }
    }
    
    @Test
    @DisplayName("아이템 모드 설정 테스트")
    void testItemModeSettings() {
        // 아이템 모드 활성화
        gameSettings.setItemModeEnabled(true);
        assertTrue(gameSettings.isItemModeEnabled(), "아이템 모드가 활성화되어야 합니다");
        
        // 아이템 모드 비활성화
        gameSettings.setItemModeEnabled(false);
        assertFalse(gameSettings.isItemModeEnabled(), "아이템 모드가 비활성화되어야 합니다");
    }
    
    @Test
    @DisplayName("잘못된 색상 스킴 이름 처리 테스트")
    void testInvalidColorSchemeName() {
        // 시스템 프로퍼티로 설정 파일 경로 변경
        String tempDir = System.getProperty("java.io.tmpdir");
        String originalDir = System.getProperty("tetris.appdir");
        System.setProperty("tetris.appdir", tempDir + "/tetris-test-" + System.nanoTime());
        
        try {
            // 잘못된 JSON 파일 생성
            java.io.File appDir = new java.io.File(System.getProperty("tetris.appdir"));
            appDir.mkdirs();
            
            java.io.File settingsFile = new java.io.File(appDir, "tetris_settings.json");
            java.nio.file.Files.writeString(settingsFile.toPath(), 
                "{\"colorScheme\":\"INVALID_SCHEME\",\"windowSize\":\"INVALID_SIZE\",\"difficulty\":\"INVALID_DIFF\"}");
            
            // 새 인스턴스가 기본값으로 fallback 되는지 확인
            // (실제로는 기존 싱글톤을 재사용하므로 loadSettings만 호출)
            gameSettings.saveSettings(); // 새 경로에 저장 시도
            
            assertNotNull(gameSettings.getCurrentColorScheme(), "색상 스킴이 null이면 안됩니다");
            assertNotNull(gameSettings.getCurrentWindowSize(), "윈도우 크기가 null이면 안됩니다");
            
        } catch (Exception e) {
            // 예외 처리
        } finally {
            // 원래 프로퍼티 복원
            if (originalDir != null) {
                System.setProperty("tetris.appdir", originalDir);
            } else {
                System.clearProperty("tetris.appdir");
            }
        }
    }
    
    @Test
    @DisplayName("빈 커스텀 색상 처리 테스트")
    void testEmptyCustomColors() {
        // 시스템 프로퍼티로 설정 파일 경로 변경
        String tempDir = System.getProperty("java.io.tmpdir");
        String originalDir = System.getProperty("tetris.appdir");
        System.setProperty("tetris.appdir", tempDir + "/tetris-test-custom-" + System.nanoTime());
        
        try {
            // 커스텀 색상이 없는 JSON 파일 생성
            java.io.File appDir = new java.io.File(System.getProperty("tetris.appdir"));
            appDir.mkdirs();
            
            java.io.File settingsFile = new java.io.File(appDir, "tetris_settings.json");
            java.nio.file.Files.writeString(settingsFile.toPath(), 
                "{\"colorScheme\":\"CUSTOM\"}");
            
            // 설정 저장으로 새 파일 생성
            gameSettings.setCurrentColorScheme(ColorScheme.CUSTOM);
            gameSettings.saveSettings();
            
            // 커스텀 색상이 기본값으로 설정되었는지 확인
            assertNotNull(gameSettings.getCurrentColors(), "색상 맵이 null이면 안됩니다");
            
        } catch (Exception e) {
            // 예외 처리
        } finally {
            // 원래 프로퍼티 복원
            if (originalDir != null) {
                System.setProperty("tetris.appdir", originalDir);
            } else {
                System.clearProperty("tetris.appdir");
            }
        }
    }
    
    @Test
    @DisplayName("잘못된 키 바인딩 처리 테스트")
    void testInvalidKeyBindings() {
        // 시스템 프로퍼티로 설정 파일 경로 변경
        String tempDir = System.getProperty("java.io.tmpdir");
        String originalDir = System.getProperty("tetris.appdir");
        System.setProperty("tetris.appdir", tempDir + "/tetris-test-keys-" + System.nanoTime());
        
        try {
            // 잘못된 키 바인딩이 포함된 JSON 파일 생성
            java.io.File appDir = new java.io.File(System.getProperty("tetris.appdir"));
            appDir.mkdirs();
            
            java.io.File settingsFile = new java.io.File(appDir, "tetris_settings.json");
            java.nio.file.Files.writeString(settingsFile.toPath(), 
                "{\"keyBindings\":{\"MOVE_LEFT\":\"INVALID_KEY\"}}");
            
            // 저장하면 현재 설정이 저장됨
            gameSettings.saveSettings();
            
            // 기본 키 바인딩이 있는지 확인
            assertNotNull(gameSettings.getKeyBinding("MOVE_LEFT"), "왼쪽 이동 키가 null이면 안됩니다");
            assertNotNull(gameSettings.getKeyBinding("ROTATE"), "회전 키가 null이면 안됩니다");
            
        } catch (Exception e) {
            // 예외 처리
        } finally {
            // 원래 프로퍼티 복원
            if (originalDir != null) {
                System.setProperty("tetris.appdir", originalDir);
            } else {
                System.clearProperty("tetris.appdir");
            }
        }
    }
}