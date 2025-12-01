package com.example.gameover;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.example.settings.GameSettings;

public class ScoreManagerTest {

    @TempDir
    Path tempDir;

    @BeforeEach
    public void setup() {
        // point ScoreManager to a temp directory so we don't touch user's home
        System.setProperty("tetris.appdir", tempDir.toString());
        // ensure singleton settings start from a clean state for itemMode flag
        GameSettings.getInstance().setItemModeEnabled(false);
    }

    @Test
    public void saveAndLoad_normalMode() throws Exception {
        List<GameOverScene.ScoreEntry> list = new ArrayList<>();
        list.add(new GameOverScene.ScoreEntry("Alice", 1000, false, GameSettings.Difficulty.NORMAL));
        list.add(new GameOverScene.ScoreEntry("Bob", 800, false, GameSettings.Difficulty.EASY));

        // explicitly save under normal mode
        ScoreManager.saveScores(list, false);

        List<GameOverScene.ScoreEntry> loaded = ScoreManager.loadScores(false);
        assertEquals(2, loaded.size(), "Should load two entries for normal mode");
        assertEquals("Alice", loaded.get(0).getName());
        assertEquals(1000, loaded.get(0).getScore());
        // write debug info to stderr so IDE test runner is likely to show it
        System.err.println("scorePath=" + ScoreManager.getScorePath());
        System.err.println("savedFile=" + Files.readString(Path.of(ScoreManager.getScorePath())));
    }

    @Test
    public void saveAndLoad_itemMode() throws Exception {
        List<GameOverScene.ScoreEntry> list = new ArrayList<>();
        list.add(new GameOverScene.ScoreEntry("Carol", 1500, true, GameSettings.Difficulty.HARD));

        // explicitly save under item mode
        ScoreManager.saveScores(list, true);

        List<GameOverScene.ScoreEntry> loaded = ScoreManager.loadScores(true);
        assertEquals(1, loaded.size(), "Should load one entry for item mode");
        assertEquals("Carol", loaded.get(0).getName());
        assertTrue(loaded.get(0).isItemMode());
        assertEquals(GameSettings.Difficulty.HARD, loaded.get(0).getDifficulty());
        System.err.println("scorePath=" + ScoreManager.getScorePath());
        System.err.println("savedFile=" + Files.readString(Path.of(ScoreManager.getScorePath())));
    }

    @Test
    public void resetScores_createsEmptySkeletonAndClearsCache() throws Exception {
        // create some data first
        List<GameOverScene.ScoreEntry> list = new ArrayList<>();
        list.add(new GameOverScene.ScoreEntry("Dave", 500, false, GameSettings.Difficulty.NORMAL));
        ScoreManager.saveScores(list, false);

        // file should exist
        assertTrue(Files.exists(Path.of(ScoreManager.getScorePath())));

        // reset
        boolean ok = ScoreManager.resetScores();
        assertTrue(ok, "resetScores should return true on success");

        // file should exist (we write skeleton); loads should be empty for both modes
        List<GameOverScene.ScoreEntry> normal = ScoreManager.loadScores(false);
        List<GameOverScene.ScoreEntry> item = ScoreManager.loadScores(true);
        assertEquals(0, normal.size(), "normal mode should be empty after reset");
        assertEquals(0, item.size(), "item mode should be empty after reset");
        System.err.println("scorePath=" + ScoreManager.getScorePath());
        System.err.println("savedFile=" + Files.readString(Path.of(ScoreManager.getScorePath())));
    }
    
    @Test
    public void loadScores_nonExistentFile_returnsEmptyList() {
        // Given - 파일이 존재하지 않음
        
        // When
        List<GameOverScene.ScoreEntry> scores = ScoreManager.loadScores(false);
        
        // Then
        assertNotNull(scores, "결과가 null이면 안됩니다");
        assertTrue(scores.isEmpty(), "파일이 없으면 빈 리스트를 반환해야 합니다");
    }
    
    @Test
    public void saveScores_emptyList() throws Exception {
        // Given
        List<GameOverScene.ScoreEntry> emptyList = new ArrayList<>();
        
        // When
        ScoreManager.saveScores(emptyList, false);
        
        // Then
        List<GameOverScene.ScoreEntry> loaded = ScoreManager.loadScores(false);
        assertTrue(loaded.isEmpty(), "빈 리스트를 저장하면 빈 리스트가 로드되어야 합니다");
    }
    
    @Test
    public void saveScores_moreThan10Entries_savesOnlyTop10() throws Exception {
        // Given - 15개의 점수 항목
        List<GameOverScene.ScoreEntry> list = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            list.add(new GameOverScene.ScoreEntry("Player" + i, 1000 - (i * 50), false, GameSettings.Difficulty.NORMAL));
        }
        
        // When
        ScoreManager.saveScores(list, false);
        
        // Then
        List<GameOverScene.ScoreEntry> loaded = ScoreManager.loadScores(false);
        assertEquals(10, loaded.size(), "최대 10개만 저장되어야 합니다");
        assertEquals(1000, loaded.get(0).getScore(), "가장 높은 점수가 첫 번째여야 합니다");
        assertEquals(550, loaded.get(9).getScore(), "10번째로 높은 점수가 마지막이어야 합니다");
    }
    
    @Test
    public void saveScores_bothModes_preservesBothData() throws Exception {
        // Given - 두 모드에 각각 데이터 저장
        List<GameOverScene.ScoreEntry> normalScores = new ArrayList<>();
        normalScores.add(new GameOverScene.ScoreEntry("NormalPlayer", 800, false, GameSettings.Difficulty.NORMAL));
        
        List<GameOverScene.ScoreEntry> itemScores = new ArrayList<>();
        itemScores.add(new GameOverScene.ScoreEntry("ItemPlayer", 1200, true, GameSettings.Difficulty.HARD));
        
        // When
        ScoreManager.saveScores(normalScores, false);
        ScoreManager.saveScores(itemScores, true);
        
        // Then - 두 모드 데이터가 모두 유지됨
        List<GameOverScene.ScoreEntry> loadedNormal = ScoreManager.loadScores(false);
        List<GameOverScene.ScoreEntry> loadedItem = ScoreManager.loadScores(true);
        
        assertEquals(1, loadedNormal.size(), "노말 모드 데이터가 유지되어야 합니다");
        assertEquals(1, loadedItem.size(), "아이템 모드 데이터가 유지되어야 합니다");
        assertEquals("NormalPlayer", loadedNormal.get(0).getName());
        assertEquals("ItemPlayer", loadedItem.get(0).getName());
    }
    
    @Test
    public void scoreFileExists_afterSaving() throws Exception {
        // Given - 이전 테스트에서 파일이 생성될 수 있으므로 확인만 함
        
        // When
        List<GameOverScene.ScoreEntry> list = new ArrayList<>();
        list.add(new GameOverScene.ScoreEntry("Test", 100, false, GameSettings.Difficulty.EASY));
        ScoreManager.saveScores(list, false);
        
        // Then
        assertTrue(ScoreManager.scoreFileExists(), "저장 후 파일이 존재해야 합니다");
    }
    
    @Test
    public void getScoreCount_returnsCorrectCount() throws Exception {
        // Given
        List<GameOverScene.ScoreEntry> list = new ArrayList<>();
        list.add(new GameOverScene.ScoreEntry("Player1", 500, false, GameSettings.Difficulty.NORMAL));
        list.add(new GameOverScene.ScoreEntry("Player2", 600, false, GameSettings.Difficulty.NORMAL));
        list.add(new GameOverScene.ScoreEntry("Player3", 700, false, GameSettings.Difficulty.NORMAL));
        
        // When
        GameSettings.getInstance().setItemModeEnabled(false);
        ScoreManager.saveScores(list, false);
        
        // Then
        int count = ScoreManager.getScoreCount();
        assertEquals(3, count, "점수 개수가 정확해야 합니다");
    }
    
    @Test
    public void loadScores_withoutExplicitMode_usesGameSettings() throws Exception {
        // Given
        List<GameOverScene.ScoreEntry> normalList = new ArrayList<>();
        normalList.add(new GameOverScene.ScoreEntry("Normal", 300, false, GameSettings.Difficulty.NORMAL));
        
        List<GameOverScene.ScoreEntry> itemList = new ArrayList<>();
        itemList.add(new GameOverScene.ScoreEntry("Item", 400, true, GameSettings.Difficulty.HARD));
        
        ScoreManager.saveScores(normalList, false);
        ScoreManager.saveScores(itemList, true);
        
        // When - 아이템 모드 비활성화
        GameSettings.getInstance().setItemModeEnabled(false);
        List<GameOverScene.ScoreEntry> loaded1 = ScoreManager.loadScores();
        
        // Then
        assertEquals(1, loaded1.size());
        assertEquals("Normal", loaded1.get(0).getName());
        
        // When - 아이템 모드 활성화
        GameSettings.getInstance().setItemModeEnabled(true);
        List<GameOverScene.ScoreEntry> loaded2 = ScoreManager.loadScores();
        
        // Then
        assertEquals(1, loaded2.size());
        assertEquals("Item", loaded2.get(0).getName());
    }
    
    @Test
    public void saveScores_sortsByScoreDescending() throws Exception {
        // Given - 정렬되지 않은 점수 리스트
        List<GameOverScene.ScoreEntry> list = new ArrayList<>();
        list.add(new GameOverScene.ScoreEntry("Low", 100, false, GameSettings.Difficulty.NORMAL));
        list.add(new GameOverScene.ScoreEntry("High", 900, false, GameSettings.Difficulty.NORMAL));
        list.add(new GameOverScene.ScoreEntry("Medium", 500, false, GameSettings.Difficulty.NORMAL));
        
        // When
        ScoreManager.saveScores(list, false);
        
        // Then
        List<GameOverScene.ScoreEntry> loaded = ScoreManager.loadScores(false);
        assertEquals(900, loaded.get(0).getScore(), "가장 높은 점수가 첫 번째여야 합니다");
        assertEquals(500, loaded.get(1).getScore(), "중간 점수가 두 번째여야 합니다");
        assertEquals(100, loaded.get(2).getScore(), "가장 낮은 점수가 마지막이어야 합니다");
    }
    
    @Test
    public void resetScores_nonExistentFile_returnsTrue() {
        // Given - 파일이 없는 상태
        assertFalse(ScoreManager.scoreFileExists());
        
        // When
        boolean result = ScoreManager.resetScores();
        
        // Then
        assertTrue(result, "파일이 없어도 true를 반환해야 합니다");
    }
    
    @Test
    public void addResetListener_getsNotified() throws Exception {
        // Given
        final boolean[] notified = {false};
        Runnable listener = () -> notified[0] = true;
        ScoreManager.addResetListener(listener);
        
        // When
        ScoreManager.resetScores();
        
        // Then
        assertTrue(notified[0], "리스너가 호출되어야 합니다");
        
        // Cleanup
        ScoreManager.removeResetListener(listener);
    }
    
    @Test
    public void removeResetListener_stopsNotifications() throws Exception {
        // Given
        final int[] callCount = {0};
        Runnable listener = () -> callCount[0]++;
        ScoreManager.addResetListener(listener);
        
        // When - 리스너 제거
        ScoreManager.removeResetListener(listener);
        ScoreManager.resetScores();
        
        // Then
        assertEquals(0, callCount[0], "리스너가 제거되면 호출되지 않아야 합니다");
    }
    
    @Test
    public void getScorePath_returnsValidPath() {
        // When
        String path = ScoreManager.getScorePath();
        
        // Then
        assertNotNull(path, "경로가 null이면 안됩니다");
        assertTrue(path.endsWith("tetris_scores.json"), "올바른 파일 이름이어야 합니다");
    }
}
