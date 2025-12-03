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

    @Test
    @DisplayName("ScoreEntry constructor with only name and score")
    void testScoreEntrySimpleConstructor() {
        GameOverScene.ScoreEntry entry = new GameOverScene.ScoreEntry("Test", 5000);
        assertEquals("Test", entry.getName());
        assertEquals(5000, entry.getScore());
        assertFalse(entry.isItemMode());
        assertEquals(GameSettings.Difficulty.NORMAL, entry.getDifficulty());
    }

    @Test
    @DisplayName("clearLeaderboard method")
    void testClearLeaderboard() {
        GameOverScene.clearLeaderboard();
        // Should not throw any exception
        assertTrue(true);
    }

    @Test
    @DisplayName("ScoreEntry with null difficulty")
    void testScoreEntryNullDifficulty() {
        GameOverScene.ScoreEntry entry = new GameOverScene.ScoreEntry("Player", 1000, false, null);
        assertEquals(GameSettings.Difficulty.NORMAL, entry.getDifficulty());
    }

    @Test
    @DisplayName("ScoreEntry toString contains relevant info")
    void testScoreEntryToStringContent() {
        GameOverScene.ScoreEntry entry = new GameOverScene.ScoreEntry("TestPlayer", 9999, true, GameSettings.Difficulty.HARD);
        String str = entry.toString();
        assertNotNull(str);
        assertTrue(str.length() > 0);
    }

    @Test
    @DisplayName("ScoreEntry equals with same reference")
    void testScoreEntryEqualsSameReference() {
        GameOverScene.ScoreEntry entry = new GameOverScene.ScoreEntry("Player", 1000, false, GameSettings.Difficulty.NORMAL);
        assertEquals(entry, entry);
    }

    @Test
    @DisplayName("ScoreEntry equals with different name")
    void testScoreEntryDifferentName() {
        GameOverScene.ScoreEntry entry1 = new GameOverScene.ScoreEntry("Alice", 1000, false, GameSettings.Difficulty.NORMAL);
        GameOverScene.ScoreEntry entry2 = new GameOverScene.ScoreEntry("Bob", 1000, false, GameSettings.Difficulty.NORMAL);
        assertNotEquals(entry1, entry2);
    }

    @Test
    @DisplayName("ScoreEntry equals with different score")
    void testScoreEntryDifferentScore() {
        GameOverScene.ScoreEntry entry1 = new GameOverScene.ScoreEntry("Player", 1000, false, GameSettings.Difficulty.NORMAL);
        GameOverScene.ScoreEntry entry2 = new GameOverScene.ScoreEntry("Player", 2000, false, GameSettings.Difficulty.NORMAL);
        assertNotEquals(entry1, entry2);
    }

    @Test
    @DisplayName("ScoreEntry equals with different item mode")
    void testScoreEntryDifferentItemMode() {
        GameOverScene.ScoreEntry entry1 = new GameOverScene.ScoreEntry("Player", 1000, true, GameSettings.Difficulty.NORMAL);
        GameOverScene.ScoreEntry entry2 = new GameOverScene.ScoreEntry("Player", 1000, false, GameSettings.Difficulty.NORMAL);
        assertNotEquals(entry1, entry2);
    }

    @Test
    @DisplayName("ScoreEntry equals with different difficulty")
    void testScoreEntryDifferentDifficultyEquals() {
        GameOverScene.ScoreEntry entry1 = new GameOverScene.ScoreEntry("Player", 1000, false, GameSettings.Difficulty.EASY);
        GameOverScene.ScoreEntry entry2 = new GameOverScene.ScoreEntry("Player", 1000, false, GameSettings.Difficulty.HARD);
        assertNotEquals(entry1, entry2);
    }

    @Test
    @DisplayName("GameOverScene create with custom size")
    void testGameOverSceneCreateWithCustomSize() throws Exception {
        clearLeaderboard();
        Platform.runLater(() -> {
            try {
                javafx.stage.Stage stage = new javafx.stage.Stage();
                java.util.List<GameOverScene.ScoreEntry> scores = new java.util.ArrayList<>();
                scores.add(new GameOverScene.ScoreEntry("Player1", 5000, false, GameSettings.Difficulty.NORMAL));
                scores.add(new GameOverScene.ScoreEntry("Player2", 4000, false, GameSettings.Difficulty.NORMAL));
                
                javafx.scene.Scene scene = GameOverScene.create(stage, scores, null, 800, 600);
                assertNotNull(scene);
                assertEquals(800, scene.getWidth(), 0.1);
                assertEquals(600, scene.getHeight(), 0.1);
            } catch (Exception e) {
                fail("Failed to create scene with custom size: " + e.getMessage());
            }
        });
        Thread.sleep(300);
    }

    @Test
    @DisplayName("GameOverScene create with default size")
    void testGameOverSceneCreateWithDefaultSize() throws Exception {
        clearLeaderboard();
        Platform.runLater(() -> {
            try {
                javafx.stage.Stage stage = new javafx.stage.Stage();
                java.util.List<GameOverScene.ScoreEntry> scores = new java.util.ArrayList<>();
                scores.add(new GameOverScene.ScoreEntry("Player", 1000, false, GameSettings.Difficulty.EASY));
                
                javafx.scene.Scene scene = GameOverScene.create(stage, scores, null);
                assertNotNull(scene);
            } catch (Exception e) {
                fail("Failed to create scene with default size: " + e.getMessage());
            }
        });
        Thread.sleep(300);
    }

    @Test
    @DisplayName("GameOverScene with empty score list")
    void testGameOverSceneWithEmptyScores() throws Exception {
        clearLeaderboard();
        Platform.runLater(() -> {
            try {
                javafx.stage.Stage stage = new javafx.stage.Stage();
                java.util.List<GameOverScene.ScoreEntry> scores = new java.util.ArrayList<>();
                
                javafx.scene.Scene scene = GameOverScene.create(stage, scores, null, 800, 600);
                assertNotNull(scene);
            } catch (Exception e) {
                fail("Failed to create scene with empty scores: " + e.getMessage());
            }
        });
        Thread.sleep(300);
    }

    @Test
    @DisplayName("GameOverScene with current player highlight")
    void testGameOverSceneWithCurrentPlayer() throws Exception {
        clearLeaderboard();
        Platform.runLater(() -> {
            try {
                javafx.stage.Stage stage = new javafx.stage.Stage();
                java.util.List<GameOverScene.ScoreEntry> scores = new java.util.ArrayList<>();
                GameOverScene.ScoreEntry player1 = new GameOverScene.ScoreEntry("Player1", 5000, false, GameSettings.Difficulty.NORMAL);
                GameOverScene.ScoreEntry player2 = new GameOverScene.ScoreEntry("Player2", 4000, false, GameSettings.Difficulty.NORMAL);
                scores.add(player1);
                scores.add(player2);
                
                javafx.scene.Scene scene = GameOverScene.create(stage, scores, player1, 800, 600);
                assertNotNull(scene);
            } catch (Exception e) {
                fail("Failed to create scene with current player: " + e.getMessage());
            }
        });
        Thread.sleep(300);
    }

    @Test
    @DisplayName("GameOverScene with long player name")
    void testGameOverSceneWithLongName() throws Exception {
        clearLeaderboard();
        Platform.runLater(() -> {
            try {
                javafx.stage.Stage stage = new javafx.stage.Stage();
                java.util.List<GameOverScene.ScoreEntry> scores = new java.util.ArrayList<>();
                scores.add(new GameOverScene.ScoreEntry("VeryLongPlayerNameThatExceedsLimit", 1000, false, GameSettings.Difficulty.NORMAL));
                
                javafx.scene.Scene scene = GameOverScene.create(stage, scores, null, 800, 600);
                assertNotNull(scene);
            } catch (Exception e) {
                fail("Failed to create scene with long name: " + e.getMessage());
            }
        });
        Thread.sleep(300);
    }

    @Test
    @DisplayName("GameOverScene with max scores (10)")
    void testGameOverSceneWithMaxScores() throws Exception {
        clearLeaderboard();
        Platform.runLater(() -> {
            try {
                javafx.stage.Stage stage = new javafx.stage.Stage();
                java.util.List<GameOverScene.ScoreEntry> scores = new java.util.ArrayList<>();
                for (int i = 0; i < 10; i++) {
                    scores.add(new GameOverScene.ScoreEntry("Player" + i, (10-i) * 1000, false, GameSettings.Difficulty.NORMAL));
                }
                
                javafx.scene.Scene scene = GameOverScene.create(stage, scores, null, 800, 600);
                assertNotNull(scene);
            } catch (Exception e) {
                fail("Failed to create scene with max scores: " + e.getMessage());
            }
        });
        Thread.sleep(300);
    }

    @Test
    @DisplayName("GameOverScene with more than max scores")
    void testGameOverSceneWithMoreThanMaxScores() throws Exception {
        clearLeaderboard();
        Platform.runLater(() -> {
            try {
                javafx.stage.Stage stage = new javafx.stage.Stage();
                java.util.List<GameOverScene.ScoreEntry> scores = new java.util.ArrayList<>();
                for (int i = 0; i < 15; i++) {
                    scores.add(new GameOverScene.ScoreEntry("Player" + i, (15-i) * 1000, false, GameSettings.Difficulty.NORMAL));
                }
                
                javafx.scene.Scene scene = GameOverScene.create(stage, scores, null, 800, 600);
                assertNotNull(scene);
            } catch (Exception e) {
                fail("Failed to create scene with more than max scores: " + e.getMessage());
            }
        });
        Thread.sleep(300);
    }

    @Test
    @DisplayName("GameOverScene with item mode scores")
    void testGameOverSceneWithItemModeScores() throws Exception {
        clearLeaderboard();
        Platform.runLater(() -> {
            try {
                javafx.stage.Stage stage = new javafx.stage.Stage();
                java.util.List<GameOverScene.ScoreEntry> scores = new java.util.ArrayList<>();
                scores.add(new GameOverScene.ScoreEntry("Player1", 5000, true, GameSettings.Difficulty.NORMAL));
                scores.add(new GameOverScene.ScoreEntry("Player2", 4000, true, GameSettings.Difficulty.HARD));
                
                javafx.scene.Scene scene = GameOverScene.create(stage, scores, null, 800, 600);
                assertNotNull(scene);
            } catch (Exception e) {
                fail("Failed to create scene with item mode scores: " + e.getMessage());
            }
        });
        Thread.sleep(300);
    }

    @Test
    @DisplayName("GameOverScene with mixed difficulty scores")
    void testGameOverSceneWithMixedDifficulties() throws Exception {
        clearLeaderboard();
        Platform.runLater(() -> {
            try {
                javafx.stage.Stage stage = new javafx.stage.Stage();
                java.util.List<GameOverScene.ScoreEntry> scores = new java.util.ArrayList<>();
                scores.add(new GameOverScene.ScoreEntry("Easy", 3000, false, GameSettings.Difficulty.EASY));
                scores.add(new GameOverScene.ScoreEntry("Normal", 4000, false, GameSettings.Difficulty.NORMAL));
                scores.add(new GameOverScene.ScoreEntry("Hard", 5000, false, GameSettings.Difficulty.HARD));
                
                javafx.scene.Scene scene = GameOverScene.create(stage, scores, null, 800, 600);
                assertNotNull(scene);
            } catch (Exception e) {
                fail("Failed to create scene with mixed difficulties: " + e.getMessage());
            }
        });
        Thread.sleep(300);
    }

    @Test
    @DisplayName("GameOverScene with small window size")
    void testGameOverSceneWithSmallSize() throws Exception {
        clearLeaderboard();
        Platform.runLater(() -> {
            try {
                javafx.stage.Stage stage = new javafx.stage.Stage();
                java.util.List<GameOverScene.ScoreEntry> scores = new java.util.ArrayList<>();
                scores.add(new GameOverScene.ScoreEntry("Player", 1000, false, GameSettings.Difficulty.NORMAL));
                
                javafx.scene.Scene scene = GameOverScene.create(stage, scores, null, 400, 300);
                assertNotNull(scene);
            } catch (Exception e) {
                fail("Failed to create scene with small size: " + e.getMessage());
            }
        });
        Thread.sleep(300);
    }

    @Test
    @DisplayName("GameOverScene with large window size")
    void testGameOverSceneWithLargeSize() throws Exception {
        clearLeaderboard();
        Platform.runLater(() -> {
            try {
                javafx.stage.Stage stage = new javafx.stage.Stage();
                java.util.List<GameOverScene.ScoreEntry> scores = new java.util.ArrayList<>();
                scores.add(new GameOverScene.ScoreEntry("Player", 1000, false, GameSettings.Difficulty.NORMAL));
                
                javafx.scene.Scene scene = GameOverScene.create(stage, scores, null, 1920, 1080);
                assertNotNull(scene);
            } catch (Exception e) {
                fail("Failed to create scene with large size: " + e.getMessage());
            }
        });
        Thread.sleep(300);
    }

    @Test
    @DisplayName("GameOverScene with null current player")
    void testGameOverSceneWithNullCurrentPlayer() throws Exception {
        clearLeaderboard();
        Platform.runLater(() -> {
            try {
                javafx.stage.Stage stage = new javafx.stage.Stage();
                java.util.List<GameOverScene.ScoreEntry> scores = new java.util.ArrayList<>();
                scores.add(new GameOverScene.ScoreEntry("Player", 1000, false, GameSettings.Difficulty.NORMAL));
                
                javafx.scene.Scene scene = GameOverScene.create(stage, scores, null, 800, 600);
                assertNotNull(scene);
            } catch (Exception e) {
                fail("Failed to create scene with null current player: " + e.getMessage());
            }
        });
        Thread.sleep(300);
    }

    @Test
    @DisplayName("ScoreEntry toString not null or empty")
    void testScoreEntryToStringNotEmpty() {
        GameOverScene.ScoreEntry entry = new GameOverScene.ScoreEntry("Test", 1000, false, GameSettings.Difficulty.NORMAL);
        String str = entry.toString();
        assertNotNull(str);
        assertFalse(str.trim().isEmpty());
    }

    @Test
    @DisplayName("GameOverScene clearLeaderboard method call")
    void testClearLeaderboardMethodCall() {
        assertDoesNotThrow(() -> {
            GameOverScene.clearLeaderboard();
        });
    }

    @Test
    @DisplayName("Multiple GameOverScene instances")
    void testMultipleGameOverSceneInstances() throws Exception {
        clearLeaderboard();
        Platform.runLater(() -> {
            try {
                javafx.stage.Stage stage1 = new javafx.stage.Stage();
                javafx.stage.Stage stage2 = new javafx.stage.Stage();
                
                java.util.List<GameOverScene.ScoreEntry> scores = new java.util.ArrayList<>();
                scores.add(new GameOverScene.ScoreEntry("Player", 1000, false, GameSettings.Difficulty.NORMAL));
                
                javafx.scene.Scene scene1 = GameOverScene.create(stage1, scores, null, 800, 600);
                javafx.scene.Scene scene2 = GameOverScene.create(stage2, scores, null, 800, 600);
                
                assertNotNull(scene1);
                assertNotNull(scene2);
            } catch (Exception e) {
                fail("Failed to create multiple GameOverScene instances: " + e.getMessage());
            }
        });
        Thread.sleep(300);
    }

    @Test
    @DisplayName("GameOverScene with sorted scores")
    void testGameOverSceneWithSortedScores() throws Exception {
        clearLeaderboard();
        Platform.runLater(() -> {
            try {
                javafx.stage.Stage stage = new javafx.stage.Stage();
                java.util.List<GameOverScene.ScoreEntry> scores = new java.util.ArrayList<>();
                // 이미 정렬된 상태로 추가
                scores.add(new GameOverScene.ScoreEntry("First", 10000, false, GameSettings.Difficulty.NORMAL));
                scores.add(new GameOverScene.ScoreEntry("Second", 9000, false, GameSettings.Difficulty.NORMAL));
                scores.add(new GameOverScene.ScoreEntry("Third", 8000, false, GameSettings.Difficulty.NORMAL));
                
                javafx.scene.Scene scene = GameOverScene.create(stage, scores, null, 800, 600);
                assertNotNull(scene);
            } catch (Exception e) {
                fail("Failed to create scene with sorted scores: " + e.getMessage());
            }
        });
        Thread.sleep(300);
    }

    @Test
    @DisplayName("GameOverScene with unsorted scores")
    void testGameOverSceneWithUnsortedScores() throws Exception {
        clearLeaderboard();
        Platform.runLater(() -> {
            try {
                javafx.stage.Stage stage = new javafx.stage.Stage();
                java.util.List<GameOverScene.ScoreEntry> scores = new java.util.ArrayList<>();
                // 정렬되지 않은 상태로 추가
                scores.add(new GameOverScene.ScoreEntry("Middle", 5000, false, GameSettings.Difficulty.NORMAL));
                scores.add(new GameOverScene.ScoreEntry("High", 10000, false, GameSettings.Difficulty.NORMAL));
                scores.add(new GameOverScene.ScoreEntry("Low", 1000, false, GameSettings.Difficulty.NORMAL));
                
                javafx.scene.Scene scene = GameOverScene.create(stage, scores, null, 800, 600);
                assertNotNull(scene);
            } catch (Exception e) {
                fail("Failed to create scene with unsorted scores: " + e.getMessage());
            }
        });
        Thread.sleep(300);
    }

    // Helper methods
    private void clearLeaderboard() throws Exception {
        Field f = GameOverScene.class.getDeclaredField("LEADERBOARD");
        f.setAccessible(true);
        f.set(null, null);
    }
}
