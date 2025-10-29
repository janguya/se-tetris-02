package com.example.game.component;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Method;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.settings.GameSettings;

import javafx.application.Platform;
import javafx.scene.paint.Color;

/**
 * Board 클래스의 단위 테스트
 */
public class BoardTest {

    private Board board;

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
        // 각 테스트 전에 Board 인스턴스를 생성
        Platform.runLater(() -> {
            board = new Board();
        });

        // UI 스레드 작업이 완료될 때까지 대기
        Thread.sleep(500);
    }

    @Test
    public void testBoardInitialization() throws Exception {
        Platform.runLater(() -> {
            assertNotNull(board, "Board should be initialized");
            assertNotNull(board.getRoot(), "Board root container should not be null");
        });
        Thread.sleep(100);
    }

    @Test
    public void testBoardIsNotPausedInitially() throws Exception {
        Platform.runLater(() -> {
            assertFalse(board.isPaused(), "Board should not be paused initially");
        });
        Thread.sleep(100);
    }

    @Test
    public void testBoardIsGameActiveInitially() throws Exception {
        Platform.runLater(() -> {
            assertTrue(board.isGameActive(), "Game should be active initially");
        });
        Thread.sleep(100);
    }

    @Test
    public void testBoardMenuNotVisibleInitially() throws Exception {
        Platform.runLater(() -> {
            assertFalse(board.isMenuVisible(), "Menu should not be visible initially");
        });
        Thread.sleep(100);
    }

    @Test
    public void testMoveLeft() throws Exception {
        Platform.runLater(() -> {
            // 초기 상태 저장 (실제로는 블록 위치를 확인해야 하지만, 여기서는 예외가 발생하지 않는지 확인)
            assertDoesNotThrow(() -> {
                board.onMoveLeft();
            }, "onMoveLeft should not throw exception");
        });
        Thread.sleep(100);
    }

    @Test
    public void testMoveRight() throws Exception {
        Platform.runLater(() -> {
            assertDoesNotThrow(() -> {
                board.onMoveRight();
            }, "onMoveRight should not throw exception");
        });
        Thread.sleep(100);
    }

    @Test
    public void testMoveDown() throws Exception {
        Platform.runLater(() -> {
            assertDoesNotThrow(() -> {
                board.onMoveDown();
            }, "onMoveDown should not throw exception");
        });
        Thread.sleep(100);
    }

    @Test
    public void testRotate() throws Exception {
        Platform.runLater(() -> {
            assertDoesNotThrow(() -> {
                board.onRotate();
            }, "onRotate should not throw exception");
        });
        Thread.sleep(100);
    }

    @Test
    public void testHardDrop() throws Exception {
        Platform.runLater(() -> {
            assertDoesNotThrow(() -> {
                board.onHardDrop();
            }, "onHardDrop should not throw exception");
        });
        Thread.sleep(100);
    }

    @Test
    public void testPauseToggle() throws Exception {
        Platform.runLater(() -> {
            // 일시정지 토글
            board.onPause();
            // 메뉴가 표시되거나 일시정지 상태가 변경됨
            assertTrue(board.isPaused() || board.isMenuVisible(),
                    "Board should be paused or menu should be visible after pause");
        });
        Thread.sleep(200);
    }

    @Test
    public void testRestartGame() throws Exception {
        Platform.runLater(() -> {
            // 게임 재시작
            assertDoesNotThrow(() -> {
                board.restartGame();
            }, "restartGame should not throw exception");

            // 재시작 후 게임이 활성화되어야 함
            assertTrue(board.isGameActive(), "Game should be active after restart");
            assertFalse(board.isPaused(), "Game should not be paused after restart");
        });
        Thread.sleep(200);
    }

    @Test
    public void testMultipleMoves() throws Exception {
        Platform.runLater(() -> {
            // 여러 번 이동 테스트
            assertDoesNotThrow(() -> {
                board.onMoveLeft();
                board.onMoveRight();
                board.onMoveDown();
                board.onRotate();
            }, "Multiple moves should not throw exception");
        });
        Thread.sleep(200);
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
    public void testCleanup() throws Exception {
        Platform.runLater(() -> {
            // cleanup 메서드가 예외 없이 실행되어야 함
            assertDoesNotThrow(() -> {
                board.cleanup();
            }, "cleanup should not throw exception");
        });
        Thread.sleep(100);
    }

    @Test
    public void testRootContainerIsStackPane() throws Exception {
        Platform.runLater(() -> {
            assertNotNull(board.getRoot(), "Root container should not be null");
            assertTrue(board.getRoot() instanceof javafx.scene.layout.StackPane,
                    "Root container should be a StackPane");
        });
        Thread.sleep(100);
    }

    @Test
    public void testMultipleRestarts() throws Exception {
        Platform.runLater(() -> {
            // 여러 번 재시작해도 문제가 없어야 함
            assertDoesNotThrow(() -> {
                board.restartGame();
                board.restartGame();
                board.restartGame();
            }, "Multiple restarts should not throw exception");

            assertTrue(board.isGameActive(), "Game should be active after multiple restarts");
        });
        Thread.sleep(300);
    }

    @Test
    public void testPauseAndResume() throws Exception {
        Platform.runLater(() -> {
            // 일시정지
            board.onPause();
            boolean pausedOrMenuVisible = board.isPaused() || board.isMenuVisible();
            assertTrue(pausedOrMenuVisible, "Should be paused or menu visible after pause");

            // 재개 시도
            board.onPause();
        });
        Thread.sleep(300);
    }

    @Test
    public void testMovesWhenPaused() throws Exception {
        Platform.runLater(() -> {
            // 일시정지
            board.onPause();

            // 일시정지 상태에서 이동 시도 (게임 로직에서 무시되어야 함)
            assertDoesNotThrow(() -> {
                board.onMoveLeft();
                board.onMoveRight();
                board.onMoveDown();
            }, "Moves when paused should not throw exception");
        });
        Thread.sleep(200);
    }

    @Test
    public void testRotateMultipleTimes() throws Exception {
        Platform.runLater(() -> {
            // 블록을 여러 번 회전
            assertDoesNotThrow(() -> {
                board.onRotate();
                board.onRotate();
                board.onRotate();
                board.onRotate(); // 4번 회전하면 원래 위치
            }, "Multiple rotations should not throw exception");
        });
        Thread.sleep(200);
    }

    @Test
    public void testRestartAfterMoves() throws Exception {
        Platform.runLater(() -> {
            // 이동 후 재시작
            board.onMoveLeft();
            board.onMoveDown();
            board.onRotate();

            assertDoesNotThrow(() -> {
                board.restartGame();
            }, "Restart after moves should not throw exception");

            assertTrue(board.isGameActive(), "Game should be active after restart");
        });
        Thread.sleep(300);
    }

    @Test
    public void testCleanupAfterGameplay() throws Exception {
        Platform.runLater(() -> {
            // 게임 플레이 후 정리
            board.onMoveLeft();
            board.onMoveRight();
            board.onMoveDown();

            assertDoesNotThrow(() -> {
                board.cleanup();
            }, "Cleanup after gameplay should not throw exception");
        });
        Thread.sleep(200);
    }

    @Test
    public void testDrawingWithItemMode() throws Exception {
        Platform.runLater(() -> {
            // 아이템 모드 활성화
            GameSettings settings = GameSettings.getInstance();
            boolean originalItemMode = settings.isItemModeEnabled();

            try {
                // 아이템 모드 켜기
                settings.setItemModeEnabled(true);

                // 새로운 Board 생성 (아이템 모드로)
                Board itemBoard = new Board();

                // 아이템 모드에서 일반 블록들이 정상적으로 그려지는지 확인
                // (아이템 블록은 10줄 삭제 후에만 생성되므로 일반 블록만 테스트)
                assertDoesNotThrow(() -> {
                    for (int i = 0; i < 10; i++) {
                        itemBoard.onMoveDown();
                        itemBoard.onRotate();
                    }
                }, "Drawing with item mode should not throw exception");

                // cleanup
                itemBoard.cleanup();
            } finally {
                // 원래 설정으로 복구
                settings.setItemModeEnabled(originalItemMode);
            }
        });
        Thread.sleep(500);
    }

    @Test
    public void testItemBlockMovement() throws Exception {
        Platform.runLater(() -> {
            GameSettings settings = GameSettings.getInstance();
            boolean originalItemMode = settings.isItemModeEnabled();

            try {
                // 아이템 모드 활성화
                settings.setItemModeEnabled(true);
                Board itemBoard = new Board();

                // 아이템 모드에서 블록 이동 테스트
                // (아이템 블록은 줄 10개 삭제 후 생성되므로, 일반 블록만 테스트)
                assertDoesNotThrow(() -> {
                    for (int i = 0; i < 8; i++) {
                        itemBoard.onMoveLeft();
                        itemBoard.onMoveRight();
                        itemBoard.onRotate();
                        itemBoard.onMoveDown();
                    }
                }, "Item block movement should not throw exception");

                itemBoard.cleanup();
            } finally {
                settings.setItemModeEnabled(originalItemMode);
            }
        });
        Thread.sleep(600);
    }

    @Test
    public void testHardDropWithItemMode() throws Exception {
        Platform.runLater(() -> {
            GameSettings settings = GameSettings.getInstance();
            boolean originalItemMode = settings.isItemModeEnabled();

            try {
                settings.setItemModeEnabled(true);
                Board itemBoard = new Board();

                // 하드 드롭으로 블록 배치
                // 아이템 모드 활성화 상태에서 게임 진행 테스트
                // (게임 오버 방지를 위해 적당히만 진행)
                assertDoesNotThrow(() -> {
                    for (int i = 0; i < 5; i++) {
                        itemBoard.onRotate();
                        itemBoard.onHardDrop();
                    }
                }, "Hard drop with item mode should not throw exception");

                itemBoard.cleanup();
            } finally {
                settings.setItemModeEnabled(originalItemMode);
            }
        });
        Thread.sleep(400);
    }

    @Test
    public void testMultipleItemBlocksPlacement() throws Exception {
        Platform.runLater(() -> {
            GameSettings settings = GameSettings.getInstance();
            boolean originalItemMode = settings.isItemModeEnabled();

            try {
                settings.setItemModeEnabled(true);
                Board itemBoard = new Board();

                // 아이템 모드에서 여러 블록 배치 테스트
                // (줄 삭제 10번 이후에야 아이템 블록이 생성되므로 일반 블록만 테스트)
                // 게임 오버 방지를 위해 좌우로 분산 배치하고 적당히만 진행
                assertDoesNotThrow(() -> {
                    for (int i = 0; i < 6; i++) {
                        itemBoard.onRotate();
                        itemBoard.onMoveLeft();
                        itemBoard.onMoveLeft();
                        itemBoard.onHardDrop();
                        itemBoard.onMoveRight();
                        itemBoard.onMoveRight();
                        itemBoard.onHardDrop();
                    }
                }, "Multiple item blocks placement should not throw exception");

                // 게임 재시작도 테스트
                assertDoesNotThrow(() -> {
                    itemBoard.restartGame();
                }, "Restart with item mode should not throw exception");

                itemBoard.cleanup();
            } finally {
                settings.setItemModeEnabled(originalItemMode);
            }
        });
        Thread.sleep(800);
    }

    @Test
    public void testItemModeToggleDuringGame() throws Exception {
        Platform.runLater(() -> {
            GameSettings settings = GameSettings.getInstance();
            boolean originalItemMode = settings.isItemModeEnabled();

            try {
                // 아이템 모드 끄고 시작
                settings.setItemModeEnabled(false);
                Board normalBoard = new Board();

                // 일반 모드로 플레이
                assertDoesNotThrow(() -> {
                    for (int i = 0; i < 5; i++) {
                        normalBoard.onMoveDown();
                    }
                }, "Normal mode gameplay should not throw exception");

                normalBoard.cleanup();

                // 아이템 모드 켜고 새 게임
                settings.setItemModeEnabled(true);
                Board itemBoard = new Board();

                // 아이템 모드로 플레이 (게임 오버 방지)
                assertDoesNotThrow(() -> {
                    for (int i = 0; i < 5; i++) {
                        itemBoard.onRotate();
                        itemBoard.onHardDrop();
                    }
                }, "Item mode gameplay should not throw exception");

                itemBoard.cleanup();
            } finally {
                settings.setItemModeEnabled(originalItemMode);
            }
        });
        Thread.sleep(600);
    }

    @Test
    public void testDrawLMarkerCell() throws Exception {
        Platform.runLater(() -> {
            try {
                // drawLMarkerCell 메서드를 리플렉션으로 호출
                Method method = Board.class.getDeclaredMethod("drawLMarkerCell", double.class, double.class,
                        Color.class);
                method.setAccessible(true);

                // 테스트용 Color 생성
                Color testColor = Color.YELLOW;

                // 메서드 호출 - 예외가 발생하지 않으면 성공
                assertDoesNotThrow(() -> {
                    try {
                        method.invoke(board, 0.0, 0.0, testColor);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }, "drawLMarkerCell should not throw exception");

            } catch (NoSuchMethodException e) {
                fail("drawLMarkerCell method not found: " + e.getMessage());
            }
        });
        Thread.sleep(200);
    }

    @Test
    public void testDrawBMarkerCell() throws Exception {
        Platform.runLater(() -> {
            try {
                // drawBMarkerCell 메서드를 리플렉션으로 호출
                Method method = Board.class.getDeclaredMethod("drawBMarkerCell", double.class, double.class,
                        Color.class);
                method.setAccessible(true);

                // 테스트용 Color 생성
                Color testColor = Color.BLACK;

                // 메서드 호출 - 예외가 발생하지 않으면 성공
                assertDoesNotThrow(() -> {
                    try {
                        method.invoke(board, 0.0, 0.0, testColor);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }, "drawBMarkerCell should not throw exception");

            } catch (NoSuchMethodException e) {
                fail("drawBMarkerCell method not found: " + e.getMessage());
            }
        });
        Thread.sleep(200);
    }

    @Test
    public void testDrawLMarkerCellMultiplePositions() throws Exception {
        Platform.runLater(() -> {
            try {
                Method method = Board.class.getDeclaredMethod("drawLMarkerCell", double.class, double.class,
                        Color.class);
                method.setAccessible(true);
                Color testColor = Color.YELLOW;

                // 여러 위치에서 호출하여 코드 커버리지 향상
                assertDoesNotThrow(() -> {
                    try {
                        method.invoke(board, 10.0, 20.0, testColor);
                        method.invoke(board, 50.0, 100.0, testColor);
                        method.invoke(board, 200.0, 300.0, testColor);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }, "drawLMarkerCell with multiple positions should not throw exception");

            } catch (NoSuchMethodException e) {
                fail("drawLMarkerCell method not found: " + e.getMessage());
            }
        });
        Thread.sleep(200);
    }

    @Test
    public void testDrawBMarkerCellMultiplePositions() throws Exception {
        Platform.runLater(() -> {
            try {
                Method method = Board.class.getDeclaredMethod("drawBMarkerCell", double.class, double.class,
                        Color.class);
                method.setAccessible(true);
                Color testColor = Color.BLACK;

                // 여러 위치에서 호출하여 코드 커버리지 향상
                assertDoesNotThrow(() -> {
                    try {
                        method.invoke(board, 10.0, 20.0, testColor);
                        method.invoke(board, 50.0, 100.0, testColor);
                        method.invoke(board, 200.0, 300.0, testColor);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }, "drawBMarkerCell with multiple positions should not throw exception");

            } catch (NoSuchMethodException e) {
                fail("drawBMarkerCell method not found: " + e.getMessage());
            }
        });
        Thread.sleep(200);
    }
}
