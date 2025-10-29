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
}
