package com.example.gameover;

import com.example.settings.GameSettings;
import javafx.application.Platform;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class GameOverSceneTest {

    @BeforeAll
    static void initJavaFX() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Already initialized
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        clearLeaderboard();
    }

    @Test
    @DisplayName("ScoreEntry basics")
    void testScoreEntryBasics() {
        GameOverScene.ScoreEntry s1 = new GameOverScene.ScoreEntry("A", 100, true, GameSettings.Difficulty.NORMAL);
        GameOverScene.ScoreEntry s2 = new GameOverScene.ScoreEntry("A", 100, true, GameSettings.Difficulty.NORMAL);
        GameOverScene.ScoreEntry s3 = new GameOverScene.ScoreEntry("B", 200, false, GameSettings.Difficulty.HARD);

        assertEquals("A", s1.getName());
        assertEquals(100, s1.getScore());
        assertEquals(s1, s2);
        assertNotEquals(s1, s3);
        assertEquals(s1.hashCode(), s2.hashCode());
    }

    @Test
    @DisplayName("ScoreEntry toString")
    void testScoreEntryToString() {
        GameOverScene.ScoreEntry s1 = new GameOverScene.ScoreEntry("Player", 1234, false, GameSettings.Difficulty.EASY);
        String toString = s1.toString();
        assertNotNull(toString);
        assertFalse(toString.isEmpty());
    }

    @Test
    @DisplayName("ScoreEntry equals different types")
    void testScoreEntryEqualsDifferentTypes() {
        GameOverScene.ScoreEntry s1 = new GameOverScene.ScoreEntry("A", 100, true, GameSettings.Difficulty.NORMAL);
        assertNotEquals(s1, null);
        assertNotEquals(s1, "string");
        assertNotEquals(s1, new Object());
    }

    @Test
    @DisplayName("ScoreEntry with different difficulties")
    void testScoreEntryDifferentDifficulties() {
        GameOverScene.ScoreEntry easy = new GameOverScene.ScoreEntry("Player", 1000, false, GameSettings.Difficulty.EASY);
        GameOverScene.ScoreEntry normal = new GameOverScene.ScoreEntry("Player", 1000, false, GameSettings.Difficulty.NORMAL);
        GameOverScene.ScoreEntry hard = new GameOverScene.ScoreEntry("Player", 1000, false, GameSettings.Difficulty.HARD);

        assertNotNull(easy);
        assertNotNull(normal);
        assertNotNull(hard);
    }

    @Test
    @DisplayName("ScoreEntry with item mode")
    void testScoreEntryWithItemMode() {
        GameOverScene.ScoreEntry withItem = new GameOverScene.ScoreEntry("Player", 5000, true, GameSettings.Difficulty.NORMAL);
        GameOverScene.ScoreEntry withoutItem = new GameOverScene.ScoreEntry("Player", 5000, false, GameSettings.Difficulty.NORMAL);

        assertTrue(withItem.isItemMode());
        assertFalse(withoutItem.isItemMode());
    }

    @Test
    @DisplayName("ScoreEntry with high score")
    void testScoreEntryHighScore() {
        GameOverScene.ScoreEntry highScore = new GameOverScene.ScoreEntry("Champion", 99999, true, GameSettings.Difficulty.HARD);
        assertEquals(99999, highScore.getScore());
        assertEquals("Champion", highScore.getName());
    }

    @Test
    @DisplayName("ScoreEntry with zero score")
    void testScoreEntryZeroScore() {
        GameOverScene.ScoreEntry zeroScore = new GameOverScene.ScoreEntry("Beginner", 0, false, GameSettings.Difficulty.EASY);
        assertEquals(0, zeroScore.getScore());
    }

    @Test
    @DisplayName("ScoreEntry difficulty access")
    void testScoreEntryDifficultyAccess() {
        GameOverScene.ScoreEntry entry = new GameOverScene.ScoreEntry("Test", 1000, false, GameSettings.Difficulty.NORMAL);
        assertEquals(GameSettings.Difficulty.NORMAL, entry.getDifficulty());
    }

    @Test
    @DisplayName("Multiple ScoreEntry creations")
    void testMultipleScoreEntryCreations() {
        for (int i = 0; i < 10; i++) {
            GameOverScene.ScoreEntry entry = new GameOverScene.ScoreEntry("Player" + i, i * 1000, i % 2 == 0, GameSettings.Difficulty.NORMAL);
            assertNotNull(entry);
            assertEquals(i * 1000, entry.getScore());
        }
    }

    // Helper methods
    private void clearLeaderboard() throws Exception {
        Field f = GameOverScene.class.getDeclaredField("LEADERBOARD");
        f.setAccessible(true);
        f.set(null, null);
    }
}
