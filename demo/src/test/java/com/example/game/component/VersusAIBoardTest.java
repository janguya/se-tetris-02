package com.example.game.component;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.settings.GameSettings;

import javafx.application.Platform;
import javafx.stage.Stage;

public class VersusAIBoardTest {

    private VersusAIBoard versusAIBoard;
    private Stage testStage;
    private VersusAIBoard.VersusGameCallback testCallback;
    
    // Callback 상태 추적
    private int winnerPlayer = -1;
    private int player1Score = -1;
    private int player2Score = -1;
    private boolean gameEnded = false;

    @BeforeEach
    public void setUp() throws Exception {
        Platform.runLater(() -> {
            testStage = new Stage();
            
            // Callback 초기화
            winnerPlayer = -1;
            player1Score = -1;
            player2Score = -1;
            gameEnded = false;
            
            testCallback = new VersusAIBoard.VersusGameCallback() {
                @Override
                public void onPlayerWin(int winner, int p1Score, int p2Score) {
                    winnerPlayer = winner;
                    player1Score = p1Score;
                    player2Score = p2Score;
                }
                
                @Override
                public void onGameEnd() {
                    gameEnded = true;
                }
            };
            
            versusAIBoard = new VersusAIBoard(testStage, 
                VersusGameModeDialog.VersusMode.NORMAL, testCallback);
        });
        Thread.sleep(500);
    }

    @AfterEach
    public void tearDown() throws Exception {
        Platform.runLater(() -> {
            if (versusAIBoard != null) {
                versusAIBoard.cleanup();
            }
        });
        Thread.sleep(200);
    }

    @Test
    public void testVersusAIBoardInitialization() throws Exception {
        Platform.runLater(() -> {
            assertNotNull(versusAIBoard, "VersusAIBoard should be initialized");
            assertNotNull(versusAIBoard.getRoot(), "Root should not be null");
        });
        Thread.sleep(200);
    }

    @Test
    public void testRootContainerIsStackPane() throws Exception {
        Platform.runLater(() -> {
            assertInstanceOf(javafx.scene.layout.StackPane.class, versusAIBoard.getRoot(),
                "Root should be StackPane");
        });
        Thread.sleep(200);
    }

    @Test
    public void testRestartGame() throws Exception {
        Platform.runLater(() -> {
            assertDoesNotThrow(() -> {
                versusAIBoard.restartGame();
            }, "restartGame should not throw exception");
        });
        Thread.sleep(200);
    }

    @Test
    public void testMultipleRestarts() throws Exception {
        Platform.runLater(() -> {
            assertDoesNotThrow(() -> {
                versusAIBoard.restartGame();
                versusAIBoard.restartGame();
                versusAIBoard.restartGame();
            }, "Multiple restarts should not throw exception");
        });
        Thread.sleep(300);
    }

    @Test
    public void testCleanup() throws Exception {
        Platform.runLater(() -> {
            assertDoesNotThrow(() -> {
                versusAIBoard.cleanup();
            }, "cleanup should not throw exception");
        });
        Thread.sleep(200);
    }

    @Test
    public void testCleanupAfterRestart() throws Exception {
        Platform.runLater(() -> {
            versusAIBoard.restartGame();
            assertDoesNotThrow(() -> {
                versusAIBoard.cleanup();
            }, "cleanup after restart should not throw exception");
        });
        Thread.sleep(300);
    }

    @Test
    public void testNormalMode() throws Exception {
        Platform.runLater(() -> {
            VersusAIBoard normalBoard = new VersusAIBoard(testStage, 
                VersusGameModeDialog.VersusMode.NORMAL, testCallback);
            assertNotNull(normalBoard, "Normal mode board should be initialized");
            normalBoard.cleanup();
        });
        Thread.sleep(300);
    }

    @Test
    public void testItemMode() throws Exception {
        Platform.runLater(() -> {
            VersusAIBoard itemBoard = new VersusAIBoard(testStage, 
                VersusGameModeDialog.VersusMode.ITEM, testCallback);
            assertNotNull(itemBoard, "Item mode board should be initialized");
            itemBoard.cleanup();
        });
        Thread.sleep(300);
    }

    @Test
    public void testTimeLimitMode() throws Exception {
        Platform.runLater(() -> {
            VersusAIBoard timeLimitBoard = new VersusAIBoard(testStage, 
                VersusGameModeDialog.VersusMode.TIME_LIMIT, testCallback);
            assertNotNull(timeLimitBoard, "Time limit mode board should be initialized");
            timeLimitBoard.cleanup();
        });
        Thread.sleep(300);
    }

    @Test
    public void testGameSettingsIntegration() throws Exception {
        Platform.runLater(() -> {
            assertNotNull(GameSettings.getInstance(), "GameSettings should be available");
        });
        Thread.sleep(200);
    }

    @Test
    public void testRestartAfterCleanup() throws Exception {
        Platform.runLater(() -> {
            versusAIBoard.cleanup();
            VersusAIBoard newBoard = new VersusAIBoard(testStage, 
                VersusGameModeDialog.VersusMode.NORMAL, testCallback);
            assertNotNull(newBoard, "New board after cleanup should be initialized");
            newBoard.cleanup();
        });
        Thread.sleep(300);
    }

    @Test
    public void testMultipleCleanups() throws Exception {
        Platform.runLater(() -> {
            assertDoesNotThrow(() -> {
                versusAIBoard.cleanup();
                versusAIBoard.cleanup();
                versusAIBoard.cleanup();
            }, "Multiple cleanups should not throw exception");
        });
        Thread.sleep(200);
    }

    @Test
    public void testRestartDifferentMode() throws Exception {
        Platform.runLater(() -> {
            VersusAIBoard board1 = new VersusAIBoard(testStage, 
                VersusGameModeDialog.VersusMode.NORMAL, testCallback);
            board1.restartGame();
            board1.cleanup();
            
            VersusAIBoard board2 = new VersusAIBoard(testStage, 
                VersusGameModeDialog.VersusMode.TIME_LIMIT, testCallback);
            board2.restartGame();
            board2.cleanup();
            
            VersusAIBoard board3 = new VersusAIBoard(testStage, 
                VersusGameModeDialog.VersusMode.ITEM, testCallback);
            board3.restartGame();
            board3.cleanup();
        });
        Thread.sleep(500);
    }

    @Test
    public void testCallbackIntegration() throws Exception {
        Platform.runLater(() -> {
            assertNotNull(testCallback, "Callback should be initialized");
        });
        Thread.sleep(200);
    }

    @Test
    public void testRootFocusable() throws Exception {
        Platform.runLater(() -> {
            assertTrue(versusAIBoard.getRoot().isFocusTraversable(),
                "Root should be focusable");
        });
        Thread.sleep(200);
    }
}
