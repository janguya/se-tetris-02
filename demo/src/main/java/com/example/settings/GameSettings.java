package com.example.settings;

import javafx.scene.paint.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;

public class GameSettings {
    private static GameSettings instance;
    private Preferences prefs;
    private ColorScheme currentColorScheme;
    private Map<String, Color> customColors;
    
    private GameSettings() {
        prefs = Preferences.userNodeForPackage(GameSettings.class);
        loadSettings();
    }
    
    public static GameSettings getInstance() {
        if (instance == null) {
            instance = new GameSettings();
        }
        return instance;
    }
    
    private void loadSettings() {
        String schemeName = prefs.get("colorScheme", ColorScheme.NORMAL.name());
        try {
            currentColorScheme = ColorScheme.valueOf(schemeName);
        } catch (IllegalArgumentException e) {
            currentColorScheme = ColorScheme.NORMAL;
        }
        
        loadCustomColors();
    }
    
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
    
    public ColorScheme getCurrentColorScheme() {
        return currentColorScheme;
    }
    
    public void setCurrentColorScheme(ColorScheme scheme) {
        this.currentColorScheme = scheme;
        saveSettings();
    }
    
    public Map<String, Color> getCurrentColors() {
        if (currentColorScheme == ColorScheme.CUSTOM) {
            return new HashMap<>(customColors);
        }
        return currentColorScheme.getColorMap();
    }
    
    public void setCustomColor(String blockType, Color color) {
        customColors.put(blockType, color);
        if (currentColorScheme == ColorScheme.CUSTOM) {
            saveSettings();
        }
    }
    
    public Color getCustomColor(String blockType) {
        return customColors.getOrDefault(blockType, Color.WHITE);
    }
}