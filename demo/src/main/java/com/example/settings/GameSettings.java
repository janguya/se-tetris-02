package com.example.settings;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import com.example.theme.ColorScheme;

import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;

public class GameSettings {
    private static GameSettings instance;
    private static final String SETTINGS_FILE = "tetris_settings.json";
    // Allow tests to override the application directory with system property
    // 'tetris.appdir'
    private static final String APP_DIR = (System.getProperty("tetris.appdir") != null)
            ? System.getProperty("tetris.appdir")
            : System.getProperty("user.home") + File.separator + ".tetris";
    private static final String SETTINGS_PATH = APP_DIR + File.separator + SETTINGS_FILE;

    // 전역 난이도 설정 (기본 MEDIUM)
    private Difficulty currentDifficulty = Difficulty.NORMAL;
    private ColorScheme currentColorScheme;
    private Map<String, Color> customColors;
    private WindowSize currentWindowSize;
    private Map<String, KeyCode> keyBindings;
    private List<Runnable> windowSizeChangeListeners;
    private boolean itemModeEnabled; // 아이템 모드 설정 추가

    private GameSettings() {
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
        try {
            // 디렉토리가 없으면 생성
            File appDir = new File(APP_DIR);
            if (!appDir.exists()) {
                appDir.mkdirs();
            }

            File settingsFile = new File(SETTINGS_PATH);
            if (!settingsFile.exists()) {
                // 파일이 없으면 기본값 사용
                loadDefaultSettings();
                return;
            }

            // JSON 파일 읽기
            String content = new String(Files.readAllBytes(Paths.get(SETTINGS_PATH)));
            JSONObject json = new JSONObject(content);

            // 색상 테마 로드
            String schemeName = json.optString("colorScheme", ColorScheme.NORMAL.name());
            try {
                currentColorScheme = ColorScheme.valueOf(schemeName);
            } catch (IllegalArgumentException e) {
                currentColorScheme = ColorScheme.NORMAL;
            }

            // 창 크기 설정 로드
            String windowSizeName = json.optString("windowSize", WindowSize.MEDIUM.name());
            try {
                currentWindowSize = WindowSize.valueOf(windowSizeName);
            } catch (IllegalArgumentException e) {
                currentWindowSize = WindowSize.MEDIUM;
            }

            // 난이도 설정 로드
            String difficultyName = json.optString("difficulty", Difficulty.NORMAL.name());
            try {
                currentDifficulty = Difficulty.valueOf(difficultyName);
            } catch (IllegalArgumentException e) {
                currentDifficulty = Difficulty.NORMAL;
            }

            // 아이템 모드 설정 로드
            itemModeEnabled = json.optBoolean("itemModeEnabled", false);

            // 커스텀 색상 로드
            loadCustomColorsFromJson(json);

            // 키 바인딩 로드
            loadKeyBindingsFromJson(json);

            System.out.println("✓ Settings loaded from: " + SETTINGS_PATH);

        } catch (Exception e) {
            System.err.println("Failed to load settings, using defaults: " + e.getMessage());
            loadDefaultSettings();
        }
    }

    // 기본 설정 로드
    private void loadDefaultSettings() {
        currentColorScheme = ColorScheme.NORMAL;
        currentWindowSize = WindowSize.MEDIUM;
        currentDifficulty = Difficulty.NORMAL;
        itemModeEnabled = false;
        loadDefaultCustomColors();
        loadDefaultKeyBindings();
    }

    // 커스텀 색상 불러오기
    private void loadCustomColorsFromJson(JSONObject json) {
        customColors = new HashMap<>();
        String[] blockTypes = { "block-i", "block-o", "block-j", "block-l", "block-s", "block-t", "block-z" };

        JSONObject customColorsJson = json.optJSONObject("customColors");
        if (customColorsJson == null) {
            loadDefaultCustomColors();
            return;
        }

        for (String blockType : blockTypes) {
            String colorHex = customColorsJson.optString(blockType, "#ffffff");
            try {
                customColors.put(blockType, Color.web(colorHex));
            } catch (IllegalArgumentException e) {
                customColors.put(blockType, Color.WHITE);
            }
        }
    }

    // 기본 커스텀 색상 로드
    private void loadDefaultCustomColors() {
        customColors = new HashMap<>();
        String[] blockTypes = { "block-i", "block-o", "block-j", "block-l", "block-s", "block-t", "block-z" };
        for (String blockType : blockTypes) {
            customColors.put(blockType, Color.WHITE);
        }
    }

    // 키 바인딩 불러오기
    private void loadKeyBindingsFromJson(JSONObject json) {
        keyBindings = new HashMap<>();
        JSONObject keyBindingsJson = json.optJSONObject("keyBindings");

        if (keyBindingsJson == null) {
            loadDefaultKeyBindings();
            return;
        }

        try {
            keyBindings.put("MOVE_LEFT", KeyCode.valueOf(keyBindingsJson.optString("MOVE_LEFT", "LEFT")));
            keyBindings.put("MOVE_RIGHT", KeyCode.valueOf(keyBindingsJson.optString("MOVE_RIGHT", "RIGHT")));
            keyBindings.put("MOVE_DOWN", KeyCode.valueOf(keyBindingsJson.optString("MOVE_DOWN", "DOWN")));
            keyBindings.put("ROTATE", KeyCode.valueOf(keyBindingsJson.optString("ROTATE", "UP")));
            keyBindings.put("PAUSE", KeyCode.valueOf(keyBindingsJson.optString("PAUSE", "ESCAPE")));
            keyBindings.put("HARD_DROP", KeyCode.valueOf(keyBindingsJson.optString("HARD_DROP", "SPACE")));
        } catch (Exception e) {
            System.err.println("Failed to load key bindings: " + e.getMessage());
            loadDefaultKeyBindings();
        }
    }

    // 기본 키 바인딩 로드
    private void loadDefaultKeyBindings() {
        keyBindings = new HashMap<>();
        keyBindings.put("MOVE_LEFT", KeyCode.LEFT);
        keyBindings.put("MOVE_RIGHT", KeyCode.RIGHT);
        keyBindings.put("MOVE_DOWN", KeyCode.DOWN);
        keyBindings.put("ROTATE", KeyCode.UP);
        keyBindings.put("PAUSE", KeyCode.ESCAPE);
        keyBindings.put("HARD_DROP", KeyCode.SPACE);
    }

    // 설정 저장
    public void saveSettings() {
        try {
            // 디렉토리가 없으면 생성
            File appDir = new File(APP_DIR);
            if (!appDir.exists()) {
                appDir.mkdirs();
            }

            // JSON 객체 생성
            JSONObject json = new JSONObject();

            // 기본 설정 저장
            json.put("colorScheme", currentColorScheme.name());
            json.put("windowSize", currentWindowSize.name());
            json.put("difficulty", currentDifficulty.name());
            json.put("itemModeEnabled", itemModeEnabled);

            // 커스텀 색상 저장
            if (currentColorScheme == ColorScheme.CUSTOM) {
                JSONObject customColorsJson = new JSONObject();
                for (Map.Entry<String, Color> entry : customColors.entrySet()) {
                    String hex = String.format("#%02x%02x%02x",
                            (int) (entry.getValue().getRed() * 255),
                            (int) (entry.getValue().getGreen() * 255),
                            (int) (entry.getValue().getBlue() * 255));
                    customColorsJson.put(entry.getKey(), hex);
                }
                json.put("customColors", customColorsJson);
            }

            // 키 바인딩 저장
            JSONObject keyBindingsJson = new JSONObject();
            keyBindingsJson.put("MOVE_LEFT", keyBindings.get("MOVE_LEFT").name());
            keyBindingsJson.put("MOVE_RIGHT", keyBindings.get("MOVE_RIGHT").name());
            keyBindingsJson.put("MOVE_DOWN", keyBindings.get("MOVE_DOWN").name());
            keyBindingsJson.put("ROTATE", keyBindings.get("ROTATE").name());
            keyBindingsJson.put("PAUSE", keyBindings.get("PAUSE").name());
            keyBindingsJson.put("HARD_DROP", keyBindings.get("HARD_DROP").name());
            json.put("keyBindings", keyBindingsJson);

            // 파일에 저장 (포맷팅된 JSON)
            Files.write(Paths.get(SETTINGS_PATH), json.toString(2).getBytes());

            System.out.println("✓ Settings saved to: " + SETTINGS_PATH);

        } catch (Exception e) {
            System.err.println("Failed to save settings: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // 전역 난이도 접근자
    public enum Difficulty {
        EASY,
        NORMAL,
        HARD
    }

    public Difficulty getDifficulty() {
        return currentDifficulty;
    }

    public void setDifficulty(Difficulty difficulty) {
        if (difficulty == null)
            return;
        this.currentDifficulty = difficulty;
        saveSettings();
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
        keyBindings.put("HARD_DROP", KeyCode.SPACE);
        keyBindings.put("PAUSE", KeyCode.ESCAPE);
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

    // 아이템 모드 관련 메서드
    public boolean isItemModeEnabled() {
        return itemModeEnabled;
    }

    public void setItemModeEnabled(boolean enabled) {
        this.itemModeEnabled = enabled;
        saveSettings();
    }
}