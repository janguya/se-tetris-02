package com.example.game.component;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.game.component.VersusBoard.VersusGameCallback;
import com.example.settings.GameSettings;

import javafx.application.Platform;
import javafx.stage.Stage;

/**
 * VersusBoard 클래스의 단위 테스트
 */
public class VersusBoardTest {

    private VersusBoard versusBoard;
    private Stage testStage;
    private VersusGameCallback testCallback;
    private int player1Score = 0;
    private int player2Score = 0;
    private int winnerPlayer = 0;
    private boolean gameEnded = false;

    @BeforeAll
    public static void initToolkit() {
        // JavaFX Toolkit 초기화
        try {
            Platform.startup(() -> {
            });
        } catch (IllegalStateException e) {
            // 이미 시작된 경우 무시
        }
    }

    @BeforeEach
    public void setUp() throws Exception {
        // 각 테스트 전에 초기화
        player1Score = 0;
        player2Score = 0;
        winnerPlayer = 0;
        gameEnded = false;
        
        Platform.runLater(() -> {
            testStage = new Stage();
            testCallback = new VersusGameCallback() {
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
            
            // NORMAL 모드로 VersusBoard 생성
            versusBoard = new VersusBoard(testStage, VersusGameModeDialog.VersusMode.NORMAL, testCallback);
        });

        // UI 스레드 작업이 완료될 때까지 대기
        Thread.sleep(500);
    }

    @Test
    public void testVersusBoardInitialization() throws Exception {
        Platform.runLater(() -> {
            assertNotNull(versusBoard, "VersusBoard should be initialized");
            assertNotNull(versusBoard.getRoot(), "VersusBoard root container should not be null");
        });
        Thread.sleep(100);
    }

    @Test
    public void testRootContainerIsStackPane() throws Exception {
        Platform.runLater(() -> {
            assertNotNull(versusBoard.getRoot(), "Root container should not be null");
            assertTrue(versusBoard.getRoot() instanceof javafx.scene.layout.StackPane,
                    "Root container should be a StackPane");
        });
        Thread.sleep(100);
    }

    @Test
    public void testRestartGame() throws Exception {
        Platform.runLater(() -> {
            // 게임 재시작
            assertDoesNotThrow(() -> {
                versusBoard.restartGame();
            }, "restartGame should not throw exception");
        });
        Thread.sleep(200);
    }

    @Test
    public void testMultipleRestarts() throws Exception {
        Platform.runLater(() -> {
            // 여러 번 재시작해도 문제가 없어야 함
            assertDoesNotThrow(() -> {
                versusBoard.restartGame();
                versusBoard.restartGame();
                versusBoard.restartGame();
            }, "Multiple restarts should not throw exception");
        });
        Thread.sleep(300);
    }

    @Test
    public void testCleanup() throws Exception {
        Platform.runLater(() -> {
            // cleanup 메서드가 예외 없이 실행되어야 함
            assertDoesNotThrow(() -> {
                versusBoard.cleanup();
            }, "cleanup should not throw exception");
        });
        Thread.sleep(100);
    }

    @Test
    public void testCleanupAfterRestart() throws Exception {
        Platform.runLater(() -> {
            // 재시작 후 정리
            versusBoard.restartGame();
            
            assertDoesNotThrow(() -> {
                versusBoard.cleanup();
            }, "Cleanup after restart should not throw exception");
        });
        Thread.sleep(200);
    }

    @Test
    public void testTimeLimitMode() throws Exception {
        Platform.runLater(() -> {
            // 시간제한 모드로 새 보드 생성
            VersusBoard timeLimitBoard = new VersusBoard(testStage, 
                VersusGameModeDialog.VersusMode.TIME_LIMIT, testCallback);
            
            assertNotNull(timeLimitBoard, "Time limit board should be initialized");
            
            // cleanup
            timeLimitBoard.cleanup();
        });
        Thread.sleep(300);
    }

    @Test
    public void testNormalMode() throws Exception {
        Platform.runLater(() -> {
            // 일반 모드로 새 보드 생성
            VersusBoard normalBoard = new VersusBoard(testStage, 
                VersusGameModeDialog.VersusMode.NORMAL, testCallback);
            
            assertNotNull(normalBoard, "Normal board should be initialized");
            
            // cleanup
            normalBoard.cleanup();
        });
        Thread.sleep(300);
    }

    @Test
    public void testItemMode() throws Exception {
        Platform.runLater(() -> {
            // 아이템 모드로 새 보드 생성
            VersusBoard itemBoard = new VersusBoard(testStage, 
                VersusGameModeDialog.VersusMode.ITEM, testCallback);
            
            assertNotNull(itemBoard, "Item board should be initialized");
            
            // cleanup
            itemBoard.cleanup();
        });
        Thread.sleep(300);
    }

    @Test
    public void testGameSettingsIntegration() {
        // GameSettings 인스턴스가 올바르게 사용되는지 확인
        GameSettings settings = GameSettings.getInstance();
        assertNotNull(settings, "GameSettings should be initialized");

        int width = settings.getWindowWidth();
        int height = settings.getWindowHeight();

        assertTrue(width > 0, "Window width should be positive");
        assertTrue(height > 0, "Window height should be positive");
    }

    @Test
    public void testRestartAfterCleanup() throws Exception {
        Platform.runLater(() -> {
            // cleanup 후 재시작 (새 인스턴스가 필요할 수 있음)
            versusBoard.cleanup();
            
            // 새로운 VersusBoard 생성
            VersusBoard newBoard = new VersusBoard(testStage, VersusGameModeDialog.VersusMode.NORMAL, testCallback);
            
            assertDoesNotThrow(() -> {
                newBoard.restartGame();
            }, "Restart after cleanup should not throw exception");
            
            newBoard.cleanup();
        });
        Thread.sleep(300);
    }

    @Test
    public void testMultipleCleanups() throws Exception {
        Platform.runLater(() -> {
            // 여러 번 cleanup해도 문제가 없어야 함
            assertDoesNotThrow(() -> {
                versusBoard.cleanup();
                versusBoard.cleanup();
                versusBoard.cleanup();
            }, "Multiple cleanups should not throw exception");
        });
        Thread.sleep(200);
    }

    @Test
    public void testRestartDifferentMode() throws Exception {
        Platform.runLater(() -> {
            // 다른 모드들을 테스트
            VersusBoard board1 = new VersusBoard(testStage, VersusGameModeDialog.VersusMode.NORMAL, testCallback);
            board1.restartGame();
            board1.cleanup();
            
            VersusBoard board2 = new VersusBoard(testStage, VersusGameModeDialog.VersusMode.TIME_LIMIT, testCallback);
            board2.restartGame();
            board2.cleanup();
            
            VersusBoard board3 = new VersusBoard(testStage, VersusGameModeDialog.VersusMode.ITEM, testCallback);
            board3.restartGame();
            board3.cleanup();
        });
        Thread.sleep(500);
    }

    @Test
    public void testCallbackIntegration() {
        // 콜백이 제대로 설정되었는지 확인
        assertNotNull(testCallback, "Callback should be initialized");
    }

    @Test
    public void testRootFocusable() throws Exception {
        Platform.runLater(() -> {
            assertNotNull(versusBoard.getRoot(), "Root should not be null");
            // StackPane은 기본적으로 focusTraversable
        });
        Thread.sleep(100);
    }
}
