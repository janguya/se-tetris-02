package com.example.settings;

public enum WindowSize {
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
