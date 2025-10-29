package com.example.gameover;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import com.example.settings.GameSettings;

//점수 데이터를 파일에 영구 저장하고 불러오는 매니저 클래스
public class ScoreManager {
    private static final String SCORE_FILE = "tetris_scores.json";
    // Allow tests to override the application directory with system property
    // 'tetris.appdir'
    private static final String APP_DIR = (System.getProperty("tetris.appdir") != null)
            ? System.getProperty("tetris.appdir")
            : System.getProperty("user.home") + File.separator + ".tetris";
    private static final String SCORE_PATH = APP_DIR + File.separator + SCORE_FILE;

    // 기존 호출을 위한 편의 메소드: 현재 모드에 해당하는 점수 목록 로드
    public static List<GameOverScene.ScoreEntry> loadScores() {
        boolean isItemMode = GameSettings.getInstance().isItemModeEnabled();
        return loadScores(isItemMode);
    }

    // 모드(아이템 모드 여부)에 따라 별도의 최상위 점수 목록을 로드
    public static List<GameOverScene.ScoreEntry> loadScores(boolean isItemMode) {
        List<GameOverScene.ScoreEntry> scores = new ArrayList<>();

        try {
            // 디렉토리가 없으면 생성
            File appDir = new File(APP_DIR);
            if (!appDir.exists()) {
                appDir.mkdirs();
            }

            File scoreFile = new File(SCORE_PATH);
            if (!scoreFile.exists()) {
                System.out.println("Score file not found, starting with empty leaderboard");
                return scores;
            }

            // JSON 파일 읽기
            String content = new String(Files.readAllBytes(Paths.get(SCORE_PATH)));
            JSONObject root = new JSONObject(content);

            String key = isItemMode ? "item" : "normal";
            JSONArray jsonArray = root.optJSONArray(key);

            System.out.println("Debug: Loaded JSON content: " + content);
            System.out.println("Debug: Loaded JSON root: " + root);
            System.out.println("Debug: Loaded JSON array: " + "key" + key + "jsonArray" + jsonArray);

            if (jsonArray == null) {
                // missing key => empty
                return scores;
            }

            // JSON을 ScoreEntry로 변환
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                String name = obj.optString("name", "Player");
                int score = obj.optInt("score", 0);
                boolean entryIsItemMode = obj.optBoolean("isItemMode", isItemMode);
                String diffName = obj.optString("difficulty", GameSettings.Difficulty.NORMAL.name());
                GameSettings.Difficulty difficulty = GameSettings.Difficulty.NORMAL;
                try {
                    difficulty = GameSettings.Difficulty.valueOf(diffName);
                } catch (Exception ex) {
                    // fallback to NORMAL
                }
                GameOverScene.ScoreEntry entry = new GameOverScene.ScoreEntry(name, score, entryIsItemMode, difficulty);
                scores.add(entry);
            }

            System.out.println("✓ Loaded " + scores.size() + " scores (mode=" + (isItemMode ? "item" : "normal")
                    + ") from: " + SCORE_PATH);

        } catch (Exception e) {
            System.err.println("Failed to load scores: " + e.getMessage());
            e.printStackTrace();
        }

        return scores;
    }

    // 점수 목록을 파일에 저장 (기본: 현재 설정된 모드로 저장)
    public static void saveScores(List<GameOverScene.ScoreEntry> scores) {
        boolean isItemMode = GameSettings.getInstance().isItemModeEnabled();
        saveScores(scores, isItemMode);
    }

    // 모드(아이템 모드 여부)에 따라 별도로 최상위 점수 목록을 파일에 저장
    public static void saveScores(List<GameOverScene.ScoreEntry> scores, boolean isItemMode) {
        try {
            // 디렉토리가 없으면 생성
            File appDir = new File(APP_DIR);
            if (!appDir.exists()) {
                appDir.mkdirs();
            }

            JSONObject root = new JSONObject();
            // 기존 파일이 있으면 로드해서 다른 모드 데이터를 보존
            File scoreFile = new File(SCORE_PATH);
            if (scoreFile.exists()) {
                try {
                    String existing = new String(Files.readAllBytes(Paths.get(SCORE_PATH)));
                    root = new JSONObject(existing);
                } catch (Exception ex) {
                    // ignore and overwrite
                    root = new JSONObject();
                }
            }

            String key = isItemMode ? "item" : "normal";

            // 제한: 상위 10개만 저장 (scores는 이미 정렬되어 있다고 가정하지만 안전을 위해 정렬)
            scores.sort((a, b) -> Integer.compare(b.getScore(), a.getScore()));
            JSONArray jsonArray = new JSONArray();
            int limit = Math.min(scores.size(), 10);
            for (int i = 0; i < limit; i++) {
                GameOverScene.ScoreEntry entry = scores.get(i);
                JSONObject obj = new JSONObject();
                obj.put("name", entry.getName());
                obj.put("score", entry.getScore());
                obj.put("isItemMode", entry.isItemMode());
                obj.put("difficulty", entry.getDifficulty() == null ? GameSettings.Difficulty.NORMAL.name()
                        : entry.getDifficulty().name());
                jsonArray.put(obj);
            }

            // 다른 키는 보존
            root.put(key, jsonArray);

            // 파일에 쓰기 (들여쓰기 2칸으로 포맷)
            Files.write(Paths.get(SCORE_PATH), root.toString(2).getBytes());

            System.out.println("✓ Saved " + limit + " scores (mode=" + key + ") to: " + SCORE_PATH);

        } catch (Exception e) {
            System.err.println("Failed to save scores: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // 저장 파일 경로 반환 (디버깅용)
    public static String getScorePath() {
        return SCORE_PATH;
    }

    public static boolean resetScores() {
        System.out.println("\n=== ScoreManager.resetScores() ===");

        File scoreFile = new File(SCORE_PATH);

        if (!scoreFile.exists()) {
            System.out.println("Score file does not exist, nothing to delete");
            return true;
        }

        try {
            boolean deleted = scoreFile.delete();
            if (deleted) {
                System.out.println("✓ Score file deleted successfully: " + SCORE_PATH);
                // Clear any in-memory cached leaderboard so scores don't get resurrected
                try {
                    GameOverScene.clearLeaderboard();
                } catch (Throwable t) {
                    // ignore if GameOverScene not available
                }
                // notify subscribers that a reset happened
                try {
                    notifyResetListeners();
                } catch (Throwable t) {
                    // ignore
                }
                // Also write an empty root with both mode keys to ensure consistent file shape
                try {
                    JSONObject empty = new JSONObject();
                    empty.put("normal", new JSONArray());
                    empty.put("item", new JSONArray());
                    Files.write(Paths.get(SCORE_PATH), empty.toString(2).getBytes());
                } catch (Exception ex) {
                    // If writing fails, it's non-fatal; file was deleted already
                }
                return true;
            } else {
                System.err.println("✗ Failed to delete score file: " + SCORE_PATH);
                return false;
            }
        } catch (SecurityException e) {
            System.err.println("✗ Security error deleting score file: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // 점수 파일이 존재하는지 확인
    public static boolean scoreFileExists() {
        return new File(SCORE_PATH).exists();
    }

    // 저장된 점수 개수 반환
    public static int getScoreCount() {
        return loadScores().size();
    }

    // reset 이벤트 리스너 관리 (reset 후 구독자에게 알림)
    private static final java.util.List<Runnable> resetListeners = new java.util.ArrayList<>();

    public static void addResetListener(Runnable r) {
        synchronized (resetListeners) {
            if (r != null && !resetListeners.contains(r))
                resetListeners.add(r);
        }
    }

    public static void removeResetListener(Runnable r) {
        synchronized (resetListeners) {
            resetListeners.remove(r);
        }
    }

    private static void notifyResetListeners() {
        java.util.List<Runnable> copy;
        synchronized (resetListeners) {
            copy = new java.util.ArrayList<>(resetListeners);
        }
        for (Runnable r : copy) {
            try {
                r.run();
            } catch (Throwable t) {
                System.err.println("Error in reset listener: " + t.getMessage());
                t.printStackTrace();
            }
        }
    }
}
