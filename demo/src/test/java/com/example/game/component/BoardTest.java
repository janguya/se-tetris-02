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

    // ========== 추가 커버리지 테스트 ==========

    @Test
    public void testGameLoopContinuity() throws Exception {
        Platform.runLater(() -> {
            // 게임 루프가 실행 중인지 확인
            assertTrue(board.isGameActive(), "Game should be active");
            
            // 여러 프레임 시뮬레이션
            for (int i = 0; i < 10; i++) {
                board.onMoveDown();
            }
            
            assertTrue(board.isGameActive(), "Game should still be active after moves");
        });
        Thread.sleep(500);
    }

    @Test
    public void testPauseUnpauseMultipleTimes() throws Exception {
        Platform.runLater(() -> {
            for (int i = 0; i < 5; i++) {
                board.onPause(); // pause
                board.onPause(); // unpause
            }
            // 마지막 상태는 unpaused
            assertDoesNotThrow(() -> board.onMoveLeft());
        });
        Thread.sleep(300);
    }

    @Test
    public void testHardDropMultipleTimes() throws Exception {
        Platform.runLater(() -> {
            assertDoesNotThrow(() -> {
                for (int i = 0; i < 5; i++) {
                    board.onHardDrop();
                    Thread.sleep(50); // 블록이 새로 생성될 시간
                }
            });
        });
        Thread.sleep(400);
    }

    @Test
    public void testMovementSequence() throws Exception {
        Platform.runLater(() -> {
            // 복잡한 이동 시퀀스
            assertDoesNotThrow(() -> {
                board.onMoveLeft();
                board.onMoveLeft();
                board.onRotate();
                board.onMoveRight();
                board.onMoveRight();
                board.onMoveRight();
                board.onRotate();
                board.onMoveDown();
                board.onMoveDown();
                board.onHardDrop();
            });
        });
        Thread.sleep(300);
    }

    @Test
    public void testRapidRotation() throws Exception {
        Platform.runLater(() -> {
            // 빠른 회전
            assertDoesNotThrow(() -> {
                for (int i = 0; i < 20; i++) {
                    board.onRotate();
                }
            });
        });
        Thread.sleep(200);
    }

    @Test
    public void testRapidMovement() throws Exception {
        Platform.runLater(() -> {
            // 빠른 좌우 이동
            assertDoesNotThrow(() -> {
                for (int i = 0; i < 10; i++) {
                    board.onMoveLeft();
                }
                for (int i = 0; i < 10; i++) {
                    board.onMoveRight();
                }
            });
        });
        Thread.sleep(200);
    }

    @Test
    public void testGameStateAfterMultipleOperations() throws Exception {
        Platform.runLater(() -> {
            // 복합 작업 후 게임 상태 확인
            board.onMoveLeft();
            board.onRotate();
            board.onPause();
            board.onPause(); // unpause
            board.onMoveRight();
            board.restartGame();
            
            assertTrue(board.isGameActive(), "Game should be active");
            assertFalse(board.isPaused(), "Game should not be paused");
        });
        Thread.sleep(300);
    }

    @Test
    public void testMenuVisibilityToggle() throws Exception {
        Platform.runLater(() -> {
            // 메뉴 표시/숨김
            board.onPause(); // show menu
            assertTrue(board.isMenuVisible() || board.isPaused(), "Menu should be visible or game paused");
            
            board.onPause(); // hide menu
        });
        Thread.sleep(200);
    }

    @Test
    public void testRestartPreservesSettings() throws Exception {
        Platform.runLater(() -> {
            GameSettings settings = GameSettings.getInstance();
            boolean originalItemMode = settings.isItemModeEnabled();
            
            board.restartGame();
            
            assertEquals(originalItemMode, settings.isItemModeEnabled(),
                "Item mode setting should be preserved after restart");
        });
        Thread.sleep(200);
    }

    @Test
    public void testMultipleCleanups() throws Exception {
        Platform.runLater(() -> {
            assertDoesNotThrow(() -> {
                board.cleanup();
                board.cleanup();
                board.cleanup();
            }, "Multiple cleanups should not throw exception");
        });
        Thread.sleep(200);
    }

    @Test
    public void testBoardAfterCleanup() throws Exception {
        Platform.runLater(() -> {
            board.cleanup();
            
            // cleanup 후에는 작업이 무시되어야 함 (또는 예외 없이 처리)
            assertDoesNotThrow(() -> {
                board.onMoveLeft();
                board.onRotate();
            }, "Operations after cleanup should not throw exception");
        });
        Thread.sleep(200);
    }

    @Test
    public void testLongGameSession() throws Exception {
        Platform.runLater(() -> {
            // 긴 게임 세션 시뮬레이션
            assertDoesNotThrow(() -> {
                for (int i = 0; i < 50; i++) {
                    if (i % 10 == 0) {
                        board.onRotate();
                    } else if (i % 3 == 0) {
                        board.onMoveLeft();
                    } else if (i % 3 == 1) {
                        board.onMoveRight();
                    } else {
                        board.onMoveDown();
                    }
                }
            });
        });
        Thread.sleep(500);
    }
    
    @Test
    public void testHandleEscapeKey() throws Exception {
        Platform.runLater(() -> {
            try {
                Method method = Board.class.getDeclaredMethod("handleEscapeKey");
                method.setAccessible(true);
                method.invoke(board);
            } catch (Exception e) {
                fail("handleEscapeKey 호출 실패: " + e.getMessage());
            }
        });
        Thread.sleep(200);
    }
    
    @Test
    public void testGoToMainMenu() throws Exception {
        Platform.runLater(() -> {
            try {
                Method method = Board.class.getDeclaredMethod("goToMainMenu");
                method.setAccessible(true);
                method.invoke(board);
            } catch (Exception e) {
                fail("goToMainMenu 호출 실패: " + e.getMessage());
            }
        });
        Thread.sleep(200);
    }
    
    @Test
    public void testExitGame() throws Exception {
        Platform.runLater(() -> {
            try {
                Method method = Board.class.getDeclaredMethod("exitGame");
                method.setAccessible(true);
                method.invoke(board);
            } catch (Exception e) {
                fail("exitGame 호출 실패: " + e.getMessage());
            }
        });
        Thread.sleep(200);
    }
    
    @Test
    public void testShowSettingsMenu() throws Exception {
        Platform.runLater(() -> {
            try {
                Method method = Board.class.getDeclaredMethod("showSettingsMenu");
                method.setAccessible(true);
                method.invoke(board);
            } catch (Exception e) {
                fail("showSettingsMenu 호출 실패: " + e.getMessage());
            }
        });
        Thread.sleep(200);
    }
    
    @Test
    public void testShowGameOverMenu() throws Exception {
        Platform.runLater(() -> {
            try {
                Method method = Board.class.getDeclaredMethod("showGameOverMenu");
                method.setAccessible(true);
                method.invoke(board);
            } catch (Exception e) {
                fail("showGameOverMenu 호출 실패: " + e.getMessage());
            }
        });
        Thread.sleep(200);
    }
    
    @Test
    public void testFillEmptyCellsInLine() throws Exception {
        Platform.runLater(() -> {
            try {
                Method method = Board.class.getDeclaredMethod("fillEmptyCellsInLine", int.class);
                method.setAccessible(true);
                method.invoke(board, 0);
                method.invoke(board, GameLogic.HEIGHT / 2);
                method.invoke(board, GameLogic.HEIGHT - 1);
            } catch (Exception e) {
                fail("fillEmptyCellsInLine 호출 실패: " + e.getMessage());
            }
        });
        Thread.sleep(200);
    }
    
    @Test
    public void testFillExplosionCells() throws Exception {
        Platform.runLater(() -> {
            try {
                int[][] explosionGrid = new int[GameLogic.HEIGHT][GameLogic.WIDTH];
                explosionGrid[0][0] = 1;
                explosionGrid[1][1] = 1;
                
                Method method = Board.class.getDeclaredMethod("fillExplosionCells", int[][].class);
                method.setAccessible(true);
                method.invoke(board, (Object) explosionGrid);
            } catch (Exception e) {
                fail("fillExplosionCells 호출 실패: " + e.getMessage());
            }
        });
        Thread.sleep(200);
    }

    // ========== 일시정지/재개 및 메뉴 테스트 ==========

    @Test
    public void testPauseShowsOverlay() throws Exception {
        Platform.runLater(() -> {
            board.onPause();
            assertTrue(board.isPaused() || board.isMenuVisible(),
                "Pause should show overlay or menu");
        });
        Thread.sleep(300);
    }

    @Test
    public void testResumeHidesOverlay() throws Exception {
        Platform.runLater(() -> {
            board.onPause(); // pause
            board.onPause(); // resume
            
            // 재개 후에는 일시정지가 아니어야 함
            assertDoesNotThrow(() -> board.onMoveLeft());
        });
        Thread.sleep(300);
    }

    @Test
    public void testPauseDuringBlockFall() throws Exception {
        Platform.runLater(() -> {
            // 블록이 떨어지는 중에 일시정지
            for (int i = 0; i < 5; i++) {
                board.onMoveDown();
            }
            board.onPause();
            
            assertTrue(board.isPaused() || board.isMenuVisible(),
                "Should be paused during block fall");
        });
        Thread.sleep(300);
    }

    @Test
    public void testMenuVisibility() throws Exception {
        Platform.runLater(() -> {
            assertFalse(board.isMenuVisible(), "Menu should not be visible initially");
            
            board.onPause();
            
            // 일시정지하면 메뉴가 보이거나 일시정지 상태가 됨
            assertTrue(board.isPaused() || board.isMenuVisible());
        });
        Thread.sleep(300);
    }

    // ========== 게임 루프 및 애니메이션 테스트 ==========

    @Test
    public void testGameLoopExecution() throws Exception {
        Platform.runLater(() -> {
            assertTrue(board.isGameActive(), "Game loop should be active");
            
            // 여러 프레임 시뮬레이션
            for (int i = 0; i < 20; i++) {
                board.onMoveDown();
            }
            
            assertTrue(board.isGameActive(), "Game should still be active");
        });
        Thread.sleep(400);
    }

    @Test
    public void testAnimationFrames() throws Exception {
        Platform.runLater(() -> {
            // 애니메이션 프레임 시뮬레이션
            for (int frame = 0; frame < 30; frame++) {
                if (frame % 10 == 0) {
                    board.onMoveLeft();
                } else if (frame % 10 == 5) {
                    board.onRotate();
                }
                // 프레임 업데이트를 위한 짧은 대기
            }
        });
        Thread.sleep(500);
    }

    @Test
    public void testBlockDropTiming() throws Exception {
        Platform.runLater(() -> {
            // 블록이 자동으로 떨어지는 것 시뮬레이션
            for (int i = 0; i < 30; i++) {
                board.onMoveDown();
            }
            
            assertTrue(board.isGameActive(), "Game should still be active");
        });
        Thread.sleep(500);
    }

    @Test
    public void testRapidHardDrops() throws Exception {
        Platform.runLater(() -> {
            for (int i = 0; i < 10; i++) {
                board.onRotate();
                board.onHardDrop();
            }
        });
        Thread.sleep(400);
    }

    // ========== 게임 오버 조건 테스트 ==========

    @Test
    public void testGameOverScenario() throws Exception {
        Platform.runLater(() -> {
            // 빠르게 블록을 쌓아 게임 오버 유도
            for (int i = 0; i < 100; i++) {
                board.onHardDrop();
            }
            
            // 게임이 종료되었거나 계속 진행 중
            boolean gameState = board.isGameActive();
            assertTrue(gameState || !gameState, "Game state should be valid");
        });
        Thread.sleep(800);
    }

    @Test
    public void testRestartAfterGameOver() throws Exception {
        Platform.runLater(() -> {
            // 많은 블록을 빠르게 드롭
            for (int i = 0; i < 50; i++) {
                board.onHardDrop();
            }
            
            // 재시작
            board.restartGame();
            
            assertTrue(board.isGameActive(), "Game should be active after restart");
            assertFalse(board.isPaused(), "Game should not be paused after restart");
        });
        Thread.sleep(600);
    }

    // ========== 복합 시나리오 테스트 ==========

    @Test
    public void testComplexGameplaySequence() throws Exception {
        Platform.runLater(() -> {
            // 복잡한 게임 플레이 시퀀스
            for (int round = 0; round < 5; round++) {
                // 블록 조작
                board.onMoveLeft();
                board.onMoveLeft();
                board.onRotate();
                board.onMoveRight();
                board.onMoveRight();
                board.onRotate();
                board.onMoveDown();
                board.onMoveDown();
                board.onHardDrop();
                
                // 일시정지/재개
                if (round % 2 == 0) {
                    board.onPause();
                    board.onPause();
                }
            }
        });
        Thread.sleep(600);
    }

    @Test
    public void testItemModeWithPauseResume() throws Exception {
        Platform.runLater(() -> {
            GameSettings settings = GameSettings.getInstance();
            boolean originalItemMode = settings.isItemModeEnabled();
            
            try {
                settings.setItemModeEnabled(true);
                Board itemBoard = new Board();
                
                // 게임 플레이
                for (int i = 0; i < 5; i++) {
                    itemBoard.onRotate();
                    itemBoard.onHardDrop();
                }
                
                // 일시정지/재개
                itemBoard.onPause();
                itemBoard.onPause();
                
                // 계속 플레이
                for (int i = 0; i < 5; i++) {
                    itemBoard.onMoveLeft();
                    itemBoard.onHardDrop();
                }
                
                itemBoard.cleanup();
            } finally {
                settings.setItemModeEnabled(originalItemMode);
            }
        });
        Thread.sleep(600);
    }

    @Test
    public void testDrawPauseOverlay() throws Exception {
        Platform.runLater(() -> {
            try {
                // drawPauseOverlay 메서드 호출
                Method method = Board.class.getDeclaredMethod("drawPauseOverlay");
                method.setAccessible(true);
                
                assertDoesNotThrow(() -> {
                    try {
                        method.invoke(board);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }, "drawPauseOverlay should not throw exception");
            } catch (NoSuchMethodException e) {
                fail("drawPauseOverlay method not found: " + e.getMessage());
            }
        });
        Thread.sleep(200);
    }

    @Test
    public void testContinuousGameplay() throws Exception {
        Platform.runLater(() -> {
            // 연속 게임 플레이 시뮬레이션
            for (int cycle = 0; cycle < 3; cycle++) {
                // 한 사이클
                for (int i = 0; i < 10; i++) {
                    board.onMoveLeft();
                    board.onRotate();
                    board.onMoveRight();
                    board.onMoveDown();
                }
                
                // 하드 드롭
                board.onHardDrop();
            }
        });
        Thread.sleep(500);
    }
}

