package com.example.settings;

import javafx.scene.paint.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;

import com.example.theme.ColorScheme;

public class GameSettings {
    private static GameSettings instance;
    private Preferences prefs;
    private ColorScheme currentColorScheme;
    private Map<String, Color> customColors;
    
    private GameSettings() {
        prefs = Preferences.userNodeForPackage(GameSettings.class);
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
        
        loadCustomColors();
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
    
    // 설정 저장
    public void saveSettings() {
        prefs.put("colorScheme", currentColorScheme.name());
        
        if (currentColorScheme == ColorScheme.CUSTOM) {
            for (Map.Entry<String, Color> entry : customColors.entrySet()) {
                String hex = String.format("#%02x%02x%02x",
                    (int)(entry.getValue().getRed() * 255),
                    (int)(entry.getValue().getGreen() * 255),
                    (int)(entry.getValue().getBlue() * 255));
                prefs.put("custom_" + entry.getKey(), hex);
            }
        }
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
}