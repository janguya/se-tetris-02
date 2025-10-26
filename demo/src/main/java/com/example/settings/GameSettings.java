package com.example.settings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

import com.example.theme.ColorScheme;

import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;

// 화면 크기 열거형 추가
enum WindowSize {
    SMALL("작은 화면", 400, 520),
    MEDIUM("중간 화면", 480, 640),
    LARGE("큰 화면", 560, 720),
    EXTRA_LARGE("매우 큰 화면", 640, 800);
    
    private final String displayName;
    private final int width;
    private final int height;
    
    WindowSize(String displayName, int width, int height) {
        this.displayName = displayName;
        this.width = width;
        this.height = height;
    }
    
    public String getDisplayName() { return displayName; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
}

public class GameSettings {
    private static GameSettings instance;
    private Preferences prefs;
    private ColorScheme currentColorScheme;
    private Map<String, Color> customColors;
    private WindowSize currentWindowSize;
    private Map<String, KeyCode> keyBindings;
    private List<Runnable> windowSizeChangeListeners;
    
    private GameSettings() {
        prefs = Preferences.userNodeForPackage(GameSettings.class);
        windowSizeChangeListeners = new ArrayList<>();
        loadSettings();
    }
    
    // 싱글톤 인스턴스 반환
    public static GameSettings getInstance() {
        if (instance == null) {
            instance = new GameSettings();
        }
        return instance;
    }
    
    // 설정 불러오기
    private void loadSettings() {
        String schemeName = prefs.get("colorScheme", ColorScheme.NORMAL.name());
        try {
            currentColorScheme = ColorScheme.valueOf(schemeName);
        } catch (IllegalArgumentException e) {
            currentColorScheme = ColorScheme.NORMAL;
        }
        
        // 창 크기 설정 불러오기
        String windowSizeName = prefs.get("windowSize", WindowSize.MEDIUM.name());
        try {
            currentWindowSize = WindowSize.valueOf(windowSizeName);
        } catch (IllegalArgumentException e) {
            currentWindowSize = WindowSize.MEDIUM;
        }
        
        loadCustomColors();
        loadKeyBindings();
    }
    
    // 커스텀 색상 불러오기
    private void loadCustomColors() {
        customColors = new HashMap<>();
        String[] blockTypes = {"block-i", "block-o", "block-j", "block-l", "block-s", "block-t", "block-z"};
        
        for (String blockType : blockTypes) {
            String colorHex = prefs.get("custom_" + blockType, "#ffffff");
            try {
                customColors.put(blockType, Color.web(colorHex));
            } catch (IllegalArgumentException e) {
                customColors.put(blockType, Color.WHITE);
            }
        }
    }
    
    // 키 바인딩 불러오기
    private void loadKeyBindings() {
        keyBindings = new HashMap<>();
        // 기본 키 설정
        keyBindings.put("MOVE_LEFT", KeyCode.valueOf(prefs.get("key_move_left", "LEFT")));
        keyBindings.put("MOVE_RIGHT", KeyCode.valueOf(prefs.get("key_move_right", "RIGHT")));
        keyBindings.put("MOVE_DOWN", KeyCode.valueOf(prefs.get("key_move_down", "DOWN")));
        keyBindings.put("ROTATE", KeyCode.valueOf(prefs.get("key_rotate", "UP")));
        keyBindings.put("PAUSE", KeyCode.valueOf(prefs.get("key_pause", "SPACE")));
        keyBindings.put("SETTINGS", KeyCode.valueOf(prefs.get("key_settings", "ESCAPE")));
    }
    
    // 설정 저장
    public void saveSettings() {
        prefs.put("colorScheme", currentColorScheme.name());
        prefs.put("windowSize", currentWindowSize.name());
        
        if (currentColorScheme == ColorScheme.CUSTOM) {
            for (Map.Entry<String, Color> entry : customColors.entrySet()) {
                String hex = String.format("#%02x%02x%02x",
                    (int)(entry.getValue().getRed() * 255),
                    (int)(entry.getValue().getGreen() * 255),
                    (int)(entry.getValue().getBlue() * 255));
                prefs.put("custom_" + entry.getKey(), hex);
            }
        }
        
        // 키 바인딩 저장
        prefs.put("key_move_left", keyBindings.get("MOVE_LEFT").name());
        prefs.put("key_move_right", keyBindings.get("MOVE_RIGHT").name());
        prefs.put("key_move_down", keyBindings.get("MOVE_DOWN").name());
        prefs.put("key_rotate", keyBindings.get("ROTATE").name());
        prefs.put("key_pause", keyBindings.get("PAUSE").name());
        prefs.put("key_settings", keyBindings.get("SETTINGS").name());
    }
    
    // 현재 색상 테마 및 커스텀 색상 접근자
    public ColorScheme getCurrentColorScheme() {
        return currentColorScheme;
    }
    
    // 현재 색상 테마 설정
    public void setCurrentColorScheme(ColorScheme scheme) {
        this.currentColorScheme = scheme;
        saveSettings();
    }
    
    // 현재 색상 맵 반환
    public Map<String, Color> getCurrentColors() {
        if (currentColorScheme == ColorScheme.CUSTOM) {
            return new HashMap<>(customColors);
        }
        return currentColorScheme.getColorMap();
    }
    
    // 커스텀 색상 설정
    public void setCustomColor(String blockType, Color color) {
        customColors.put(blockType, color);
        if (currentColorScheme == ColorScheme.CUSTOM) {
            saveSettings();
        }
    }
    
    // 특정 블록 타입의 커스텀 색상 반환
    public Color getCustomColor(String blockType) {
        return customColors.getOrDefault(blockType, Color.WHITE);
    }
    
    // 창 크기 관련 메소드 추가
    public WindowSize getCurrentWindowSize() {
        return currentWindowSize;
    }
    
    public void setCurrentWindowSize(WindowSize windowSize) {
        this.currentWindowSize = windowSize;
        saveSettings();
        // 모든 리스너들에게 창 크기 변경 알림
        notifyWindowSizeChangeListeners();
    }
    
    public int getWindowWidth() {
        return currentWindowSize.getWidth();
    }
    
    public int getWindowHeight() {
        return currentWindowSize.getHeight();
    }
    
    // 키 바인딩 관련 메서드
    public KeyCode getKeyBinding(String action) {
        return keyBindings.get(action);
    }
    
    public void setKeyBinding(String action, KeyCode keyCode) {
        keyBindings.put(action, keyCode);
        saveSettings();
    }
    
    public Map<String, KeyCode> getAllKeyBindings() {
        return new HashMap<>(keyBindings);
    }
    
    public void resetKeyBindingsToDefault() {
        keyBindings.put("MOVE_LEFT", KeyCode.LEFT);
        keyBindings.put("MOVE_RIGHT", KeyCode.RIGHT);
        keyBindings.put("MOVE_DOWN", KeyCode.DOWN);
        keyBindings.put("ROTATE", KeyCode.UP);
        keyBindings.put("PAUSE", KeyCode.SPACE);
        keyBindings.put("SETTINGS", KeyCode.ESCAPE);
        saveSettings();
    }
    
    // 창 크기 변경 리스너 관리
    public void addWindowSizeChangeListener(Runnable listener) {
        if (!windowSizeChangeListeners.contains(listener)) {
            windowSizeChangeListeners.add(listener);
        }
    }
    
    public void removeWindowSizeChangeListener(Runnable listener) {
        windowSizeChangeListeners.remove(listener);
    }
    
    private void notifyWindowSizeChangeListeners() {
        for (Runnable listener : windowSizeChangeListeners) {
            try {
                listener.run();
            } catch (Exception e) {
                System.err.println("Error in window size change listener: " + e.getMessage());
            }
        }
    }
}