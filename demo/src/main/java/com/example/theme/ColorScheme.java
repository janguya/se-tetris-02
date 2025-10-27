package com.example.theme;

import java.util.HashMap;
import java.util.Map;

import javafx.scene.paint.Color;

public enum ColorScheme {
    NORMAL("Normal"),
    COLOR_BLIND("Color Blind Friendly"), // 색맹 친화적
    // PROTANOPIA("Red-Blind (Protanopia)"), // 적색맹 친화적
    // DEUTERANOPIA("Green-Blind (Deuteranopia)"), // 녹색맹 친화적
    // TRITANOPIA("Blue-Blind (Tritanopia)"), // 청색맹 친화적
    // HIGH_CONTRAST("High Contrast"), // 고대비
    CUSTOM("Custom"); // 사용자 정의

    private final String displayName;
    
    ColorScheme(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public Map<String, Color> getColorMap() {
        Map<String, Color> colors = new HashMap<>();
        
        switch (this) {
            case NORMAL:
                colors.put("block-i", Color.web("#00ffff")); // Cyan
                colors.put("block-o", Color.web("#ffff00")); // Yellow
                colors.put("block-j", Color.web("#0000ff")); // Blue
                colors.put("block-l", Color.web("#ff8c00")); // Orange
                colors.put("block-s", Color.web("#00ff00")); // Green
                colors.put("block-t", Color.web("#800080")); // Purple
                colors.put("block-z", Color.web("#ff0000")); // Red
                break;
                
            // case PROTANOPIA: // Red-blind friendly
            //     colors.put("block-i", Color.web("#0077be")); // Blue
            //     colors.put("block-o", Color.web("#ffd700")); // Gold
            //     colors.put("block-j", Color.web("#004d7a")); // Dark Blue
            //     colors.put("block-l", Color.web("#ff8c00")); // Orange
            //     colors.put("block-s", Color.web("#228b22")); // Forest Green
            //     colors.put("block-t", Color.web("#9932cc")); // Dark Orchid
            //     colors.put("block-z", Color.web("#8b4513")); // Saddle Brown
            //     break;
                
            // case DEUTERANOPIA: // Green-blind friendly
            //     colors.put("block-i", Color.web("#00bfff")); // Deep Sky Blue
            //     colors.put("block-o", Color.web("#ffd700")); // Gold
            //     colors.put("block-j", Color.web("#0000cd")); // Medium Blue
            //     colors.put("block-l", Color.web("#ff4500")); // Orange Red
            //     colors.put("block-s", Color.web("#4169e1")); // Royal Blue
            //     colors.put("block-t", Color.web("#8b008b")); // Dark Magenta
            //     colors.put("block-z", Color.web("#dc143c")); // Crimson
            //     break;
                
            // case TRITANOPIA: // Blue-blind friendly
            //     colors.put("block-i", Color.web("#00ff7f")); // Spring Green
            //     colors.put("block-o", Color.web("#ffd700")); // Gold
            //     colors.put("block-j", Color.web("#ff1493")); // Deep Pink
            //     colors.put("block-l", Color.web("#ff8c00")); // Orange
            //     colors.put("block-s", Color.web("#32cd32")); // Lime Green
            //     colors.put("block-t", Color.web("#ff69b4")); // Hot Pink
            //     colors.put("block-z", Color.web("#dc143c")); // Crimson
            //     break;
                
            // case HIGH_CONTRAST:
            //     colors.put("block-i", Color.web("#ffffff")); // White
            //     colors.put("block-o", Color.web("#ffff00")); // Yellow
            //     colors.put("block-j", Color.web("#00ffff")); // Cyan
            //     colors.put("block-l", Color.web("#ff00ff")); // Magenta
            //     colors.put("block-s", Color.web("#00ff00")); // Lime
            //     colors.put("block-t", Color.web("#ff8000")); // Orange
            //     colors.put("block-z", Color.web("#ff0000")); // Red
            //     break;

            case COLOR_BLIND:
                colors.put("block-i", Color.web("#FF8C00")); // Orange
                colors.put("block-o", Color.web("#00BFFE")); // Sky blue
                colors.put("block-j", Color.web("#FFFF00")); // Yellow
                colors.put("block-l", Color.web("#0077BE")); // Blue
                colors.put("block-s", Color.web("#10A674")); // Bluish green
                colors.put("block-t", Color.web("#E34234")); // Vermillion
                colors.put("block-z", Color.web("#953553")); // Reddish purple
                break;

            default: // COLOR_BLIND will be handled separately
                return getNormalColors();
        }
        
        // 모든 테마에 공통으로 적용되는 색상-item
        colors.put("item-single", Color.web("#ffd700")); // Gold - 싱글 블록
        colors.put("item-weight", Color.web("#8b4513")); // Brown - 무게 블록
        colors.put("item-lmarker", Color.web("#ff00ff")); // Magenta - L 마커
        colors.put("item-sand", Color.web("#f4a460")); // Sandy Brown - 모래 블록
        
        return colors;
    }
    
    private Map<String, Color> getNormalColors() {
        return NORMAL.getColorMap();
    }
}