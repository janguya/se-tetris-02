package com.example.gameover;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

//점수 데이터를 파일에 영구 저장하고 불러오는 매니저 클래스
public class ScoreManager {
    private static final String SCORE_FILE = "tetris_scores.json";
    private static final String APP_DIR = System.getProperty("user.home") + File.separator + ".tetris";
    private static final String SCORE_PATH = APP_DIR + File.separator + SCORE_FILE;
    
    // 점수 목록을 파일에서 로드
    // 파일이 없으면 빈 리스트 반환
    public static List<GameOverScene.ScoreEntry> loadScores() {
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
            JSONArray jsonArray = new JSONArray(content);
            
            // JSON을 ScoreEntry로 변환
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                String name = obj.getString("name");
                int score = obj.getInt("score");
                scores.add(new GameOverScene.ScoreEntry(name, score));
            }
            
            System.out.println("✓ Loaded " + scores.size() + " scores from: " + SCORE_PATH);
            
        } catch (Exception e) {
            System.err.println("Failed to load scores: " + e.getMessage());
            e.printStackTrace();
        }
        
        return scores;
    }
    
    //점수 목록을 파일에 저장
    public static void saveScores(List<GameOverScene.ScoreEntry> scores) {
        try {
            // 디렉토리가 없으면 생성
            File appDir = new File(APP_DIR);
            if (!appDir.exists()) {
                appDir.mkdirs();
            }
            
            // ScoreEntry를 JSON으로 변환
            JSONArray jsonArray = new JSONArray();
            for (GameOverScene.ScoreEntry entry : scores) {
                JSONObject obj = new JSONObject();
                obj.put("name", entry.getName());
                obj.put("score", entry.getScore());
                jsonArray.put(obj);
            }
            
            // 파일에 쓰기 (들여쓰기 2칸으로 포맷)
            Files.write(Paths.get(SCORE_PATH), jsonArray.toString(2).getBytes());
            
            System.out.println("✓ Saved " + scores.size() + " scores to: " + SCORE_PATH);
            
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

    //점수 파일이 존재하는지 확인
    public static boolean scoreFileExists() {
        return new File(SCORE_PATH).exists();
    }

    //저장된 점수 개수 반환
    public static int getScoreCount() {
        return loadScores().size();
    }
}
