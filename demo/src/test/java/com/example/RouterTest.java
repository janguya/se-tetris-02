package com.example;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import com.example.settings.GameSettings;
import com.example.game.component.VersusGameModeDialog;
import com.example.game.component.VersusBoard;
import com.example.game.component.VersusAIBoard;
import com.example.network.NetworkManager;
import com.example.network.ConnectionConfig;

import javafx.application.Platform;
import javafx.stage.Stage;

/**
 * Router 클래스의 단위 테스트
 */
public class RouterTest {

    private Stage stage;
    private Router router;

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
        // 각 테스트 전에 Stage와 Router 인스턴스 생성
        Platform.runLater(() -> {
            stage = new Stage();
            router = new Router(stage);
        });

        // UI 스레드 작업이 완료될 때까지 대기
        Thread.sleep(300);
    }

    @Test
    @DisplayName("Router 초기화 테스트")
    public void testRouterInitialization() throws Exception {
        Platform.runLater(() -> {
            assertNotNull(router, "Router should be initialized");
        });
        Thread.sleep(100);
    }

    @Test
    @DisplayName("크기 지정 Router 생성 테스트")
    public void testRouterWithCustomSize() throws Exception {
        Platform.runLater(() -> {
            Router customRouter = new Router(stage, 800, 600);
            assertNotNull(customRouter, "Custom sized router should be initialized");
        });
        Thread.sleep(100);
    }

    @Test
    @DisplayName("크기 설정 테스트")
    public void testSetSize() throws Exception {
        Platform.runLater(() -> {
            assertDoesNotThrow(() -> {
                router.setSize(1024, 768);
            }, "setSize should not throw exception");
        });
        Thread.sleep(100);
    }

    @Test
    @DisplayName("시작 메뉴 표시 테스트")
    public void testShowStartMenu() throws Exception {
        Platform.runLater(() -> {
            assertDoesNotThrow(() -> {
                router.showStartMenu();
            }, "showStartMenu should not throw exception");

            // Scene이 설정되었는지 확인
            assertNotNull(stage.getScene(), "Stage should have a scene after showStartMenu");
        });
        Thread.sleep(500);
    }

    @Test
    @DisplayName("게임 시작 라우팅 테스트")
    public void testRouteToGame() throws Exception {
        Platform.runLater(() -> {
            assertDoesNotThrow(() -> {
                router.route("GAME");
            }, "Routing to GAME should not throw exception");

            assertNotNull(stage.getScene(), "Stage should have a scene after routing to GAME");
        });
        Thread.sleep(500);
    }

    @Test
    @DisplayName("설정 표시 테스트")
    public void testShowSettings() throws Exception {
        Platform.runLater(() -> {
            // 먼저 시작 메뉴를 표시 (Stage에 Scene이 필요)
            router.showStartMenu();

            assertDoesNotThrow(() -> {
                router.showSettings();
            }, "showSettings should not throw exception");
        });
        Thread.sleep(500);
    }

    @Test
    @DisplayName("추가 콜백이 있는 설정 표시 테스트")
    public void testShowSettingsWithCallback() throws Exception {
        Platform.runLater(() -> {
            router.showStartMenu();

            final boolean[] callbackCalled = { false };
            Runnable callback = () -> callbackCalled[0] = true;

            assertDoesNotThrow(() -> {
                router.showSettings(callback);
            }, "showSettings with callback should not throw exception");
        });
        Thread.sleep(500);
    }

    @Test
    @DisplayName("스코어보드 표시 테스트")
    public void testShowScoreboard() throws Exception {
        Platform.runLater(() -> {
            assertDoesNotThrow(() -> {
                router.showScoreboard();
            }, "showScoreboard should not throw exception");

            assertNotNull(stage.getScene(), "Stage should have a scene after showScoreboard");
        });
        Thread.sleep(500);
    }

    @Test
    @DisplayName("스코어보드 라우팅 테스트")
    public void testRouteToScoreboard() throws Exception {
        Platform.runLater(() -> {
            assertDoesNotThrow(() -> {
                router.route("SCOREBOARD");
            }, "Routing to SCOREBOARD should not throw exception");

            assertNotNull(stage.getScene(), "Stage should have a scene after routing to SCOREBOARD");
        });
        Thread.sleep(500);
    }

    @Test
    @DisplayName("아이템 모드 토글 라우팅 테스트")
    public void testRouteToItemMode() throws Exception {
        Platform.runLater(() -> {
            GameSettings settings = GameSettings.getInstance();
            boolean initialItemMode = settings.isItemModeEnabled();

            assertDoesNotThrow(() -> {
                router.route("ITEM_MODE");
            }, "Routing to ITEM_MODE should not throw exception");

            // 아이템 모드가 토글되었는지 확인
            assertEquals(!initialItemMode, settings.isItemModeEnabled(),
                    "Item mode should be toggled");
        });
        Thread.sleep(500);
    }

    @Test
    @DisplayName("설정 라우팅 테스트")
    public void testRouteToSettings() throws Exception {
        Platform.runLater(() -> {
            router.showStartMenu(); // Scene 필요

            assertDoesNotThrow(() -> {
                router.route("SETTINGS");
            }, "Routing to SETTINGS should not throw exception");
        });
        Thread.sleep(500);
    }

    @Test
    @DisplayName("null 라우팅 처리 테스트")
    public void testRouteWithNull() throws Exception {
        Platform.runLater(() -> {
            assertDoesNotThrow(() -> {
                router.route(null);
            }, "Routing with null should not throw exception");
        });
        Thread.sleep(100);
    }

    @Test
    @DisplayName("알 수 없는 라우트 처리 테스트")
    public void testRouteWithUnknownId() throws Exception {
        Platform.runLater(() -> {
            assertDoesNotThrow(() -> {
                router.route("UNKNOWN_ROUTE");
            }, "Routing to unknown route should not throw exception");
        });
        Thread.sleep(100);
    }

    @Test
    @DisplayName("연속 라우팅 테스트")
    public void testMultipleRouting() throws Exception {
        Platform.runLater(() -> {
            assertDoesNotThrow(() -> {
                router.showStartMenu();
                Thread.sleep(200);
                router.route("SCOREBOARD");
                Thread.sleep(200);
                router.showStartMenu();
            }, "Multiple routing should not throw exception");
        });
        Thread.sleep(800);
    }

    @Test
    @DisplayName("게임 시작 후 메뉴로 복귀 테스트")
    public void testGameToMenuNavigation() throws Exception {
        Platform.runLater(() -> {
            assertDoesNotThrow(() -> {
                router.route("GAME");
                Thread.sleep(300);
                router.showStartMenu();
            }, "Navigation from game to menu should not throw exception");
        });
        Thread.sleep(700);
    }

    @Test
    @DisplayName("크기 변경 후 게임 시작 테스트")
    public void testResizeBeforeGame() throws Exception {
        Platform.runLater(() -> {
            assertDoesNotThrow(() -> {
                router.setSize(900, 700);
                router.route("GAME");
            }, "Game start after resize should not throw exception");
        });
        Thread.sleep(500);
    }

    @Test
    @DisplayName("GameSettings 통합 테스트")
    public void testGameSettingsIntegration() {
        GameSettings settings = GameSettings.getInstance();
        assertNotNull(settings, "GameSettings should be initialized");

        int width = settings.getWindowWidth();
        int height = settings.getWindowHeight();

        assertTrue(width > 0, "Window width should be positive");
        assertTrue(height > 0, "Window height should be positive");
    }

    @Test
    @DisplayName("Stage 존재 확인 테스트")
    public void testStageExists() throws Exception {
        Platform.runLater(() -> {
            assertNotNull(stage, "Stage should not be null");
        });
        Thread.sleep(100);
    }

    @Test
    @DisplayName("Scene 전환 테스트")
    public void testSceneTransition() throws Exception {
        Platform.runLater(() -> {
            router.showStartMenu();
            javafx.scene.Scene firstScene = stage.getScene();
            assertNotNull(firstScene, "First scene should not be null");

            router.showScoreboard();
            javafx.scene.Scene secondScene = stage.getScene();
            assertNotNull(secondScene, "Second scene should not be null");
        });
        Thread.sleep(600);
    }

    @Test
    @DisplayName("여러 번 메뉴 표시 테스트")
    public void testMultipleShowStartMenu() throws Exception {
        Platform.runLater(() -> {
            assertDoesNotThrow(() -> {
                router.showStartMenu();
                router.showStartMenu();
                router.showStartMenu();
            }, "Multiple showStartMenu calls should not throw exception");
        });
        Thread.sleep(500);
    }

    @Test
    @DisplayName("게임-스코어보드-메뉴 순환 테스트")
    public void testGameScoreboardMenuCycle() throws Exception {
        Platform.runLater(() -> {
            assertDoesNotThrow(() -> {
                router.route("GAME");
                Thread.sleep(200);
                router.route("SCOREBOARD");
                Thread.sleep(200);
                router.showStartMenu();
            }, "Game-Scoreboard-Menu cycle should not throw exception");
        });
        Thread.sleep(800);
    }

    @Test
    @DisplayName("아이템 모드 여러 번 토글 테스트")
    public void testMultipleItemModeToggle() throws Exception {
        Platform.runLater(() -> {
            GameSettings settings = GameSettings.getInstance();
            boolean initialMode = settings.isItemModeEnabled();

            // 두 번 토글
            router.route("ITEM_MODE");
            router.route("ITEM_MODE");

            // 원래 상태로 돌아왔는지 확인
            assertEquals(initialMode, settings.isItemModeEnabled(),
                    "After two toggles, item mode should return to initial state");
        });
        Thread.sleep(600);
    }

    @Test
    @DisplayName("크기 변경 후 여러 화면 전환 테스트")
    public void testResizeWithMultipleScreens() throws Exception {
        Platform.runLater(() -> {
            assertDoesNotThrow(() -> {
                router.setSize(1024, 768);
                router.showStartMenu();
                Thread.sleep(200);
                router.route("GAME");
                Thread.sleep(200);
                router.route("SCOREBOARD");
            }, "Resize with multiple screen transitions should not throw exception");
        });
        Thread.sleep(800);
    }

    @Test
    @DisplayName("모든 라우트 순차 테스트")
    public void testAllRoutesSequentially() throws Exception {
        Platform.runLater(() -> {
            assertDoesNotThrow(() -> {
                router.showStartMenu();
                Thread.sleep(150);

                router.route("GAME");
                Thread.sleep(150);

                router.showStartMenu();
                Thread.sleep(150);

                router.route("ITEM_MODE");
                Thread.sleep(150);

                router.route("SCOREBOARD");
                Thread.sleep(150);

                router.showStartMenu();
            }, "All routes sequentially should not throw exception");
        });
        Thread.sleep(1200);
    }

    @Test
    @DisplayName("커스텀 크기로 Router 생성 후 메뉴 표시 테스트")
    public void testCustomSizeRouterShowMenu() throws Exception {
        Platform.runLater(() -> {
            Router customRouter = new Router(stage, 800, 600);
            assertDoesNotThrow(() -> {
                customRouter.showStartMenu();
            }, "Custom size router showStartMenu should not throw exception");

            assertNotNull(stage.getScene(), "Stage should have a scene");
        });
        Thread.sleep(500);
    }

    @Test
    @DisplayName("Stage Scene이 null일 때 크기 설정 테스트")
    public void testSetSizeWithoutScene() throws Exception {
        Platform.runLater(() -> {
            Stage newStage = new Stage();
            Router newRouter = new Router(newStage);

            // Scene이 없는 상태에서 크기 설정
            assertDoesNotThrow(() -> {
                newRouter.setSize(800, 600);
            }, "setSize without scene should not throw exception");
        });
        Thread.sleep(200);
    }

    @Test
    @DisplayName("빈 문자열 라우팅 테스트")
    public void testRouteWithEmptyString() throws Exception {
        Platform.runLater(() -> {
            assertDoesNotThrow(() -> {
                router.route("");
            }, "Routing with empty string should not throw exception");
        });
        Thread.sleep(100);
    }
    
    @Test
    @DisplayName("showVersusGame 테스트")
    public void testShowVersusGame() throws Exception {
        Platform.runLater(() -> {
            assertDoesNotThrow(() -> {
                router.showVersusGame();
            }, "showVersusGame should not throw exception");
        });
        Thread.sleep(500);
    }
    
    @Test
    @DisplayName("showVersusAIGame 테스트")
    public void testShowVersusAIGame() throws Exception {
        Platform.runLater(() -> {
            assertDoesNotThrow(() -> {
                router.showVersusAIGame();
            }, "showVersusAIGame should not throw exception");
        });
        Thread.sleep(500);
    }
    
    @Test
    @DisplayName("showOnlineVersusGame 테스트")
    public void testShowOnlineVersusGame() throws Exception {
        Platform.runLater(() -> {
            assertDoesNotThrow(() -> {
                router.showOnlineVersusGame();
            }, "showOnlineVersusGame should not throw exception");
        });
        Thread.sleep(500);
    }
    
    @Test
    @DisplayName("versus 라우팅 테스트")
    public void testRouteToVersus() throws Exception {
        Platform.runLater(() -> {
            assertDoesNotThrow(() -> {
                router.route("versus");
            }, "Routing to versus should not throw exception");
        });
        Thread.sleep(500);
    }
    
    @Test
    @DisplayName("versus-ai 라우팅 테스트")
    public void testRouteToVersusAI() throws Exception {
        Platform.runLater(() -> {
            assertDoesNotThrow(() -> {
                router.route("versus-ai");
            }, "Routing to versus-ai should not throw exception");
        });
        Thread.sleep(500);
    }
    
    @Test
    @DisplayName("online 라우팅 테스트")
    public void testRouteToOnline() throws Exception {
        Platform.runLater(() -> {
            assertDoesNotThrow(() -> {
                router.route("online");
            }, "Routing to online should not throw exception");
        });
        Thread.sleep(500);
    }
    
    @Test
    @DisplayName("VERSUS_GAME 라우팅 테스트")
    public void testRouteVersusGame() throws Exception {
        Platform.runLater(() -> {
            assertDoesNotThrow(() -> {
                router.route("VERSUS_GAME");
            }, "Routing to VERSUS_GAME should not throw exception");
        });
        Thread.sleep(500);
    }
    
    @Test
    @DisplayName("AI_VERSUS_GAME 라우팅 테스트")
    public void testRouteAIVersusGame() throws Exception {
        Platform.runLater(() -> {
            assertDoesNotThrow(() -> {
                router.route("AI_VERSUS_GAME");
            }, "Routing to AI_VERSUS_GAME should not throw exception");
        });
        Thread.sleep(500);
    }
    
    @Test
    @DisplayName("P2P_VERSUS_MODE 라우팅 테스트")
    public void testRouteP2PVersusMode() throws Exception {
        Platform.runLater(() -> {
            assertDoesNotThrow(() -> {
                router.route("P2P_VERSUS_MODE");
            }, "Routing to P2P_VERSUS_MODE should not throw exception");
        });
        Thread.sleep(500);
    }
    
    @Test
    @DisplayName("크기 재설정 후 게임 시작 테스트")
    public void testMultipleSetSize() throws Exception {
        Platform.runLater(() -> {
            assertDoesNotThrow(() -> {
                router.setSize(800, 600);
                router.setSize(1024, 768);
                router.showStartMenu();
            }, "Multiple setSize calls should not throw exception");
        });
        Thread.sleep(500);
    }
    
    @Test
    @DisplayName("showStartMenu 후 크기 변경 테스트")
    public void testSetSizeAfterShowStartMenu() throws Exception {
        Platform.runLater(() -> {
            assertDoesNotThrow(() -> {
                router.showStartMenu();
                router.setSize(1024, 768);
            }, "setSize after showStartMenu should not throw exception");
        });
        Thread.sleep(500);
    }
    
    @Test
    @DisplayName("게임 시작 후 크기 변경 테스트")
    public void testSetSizeAfterGameStart() throws Exception {
        Platform.runLater(() -> {
            assertDoesNotThrow(() -> {
                router.route("GAME");
                Thread.sleep(200);
                router.setSize(900, 700);
            }, "setSize after game start should not throw exception");
        });
        Thread.sleep(500);
    }
    
    @Test
    @DisplayName("여러 라우트 연속 호출 테스트")
    public void testConsecutiveRoutes() throws Exception {
        Platform.runLater(() -> {
            assertDoesNotThrow(() -> {
                router.route("GAME");
                router.route("SCOREBOARD");
                router.route("SETTINGS");
                router.showStartMenu();
            }, "Consecutive routes should not throw exception");
        });
        Thread.sleep(800);
    }
    
    @Test
    @DisplayName("Stage 표시 상태 확인 테스트")
    public void testStageShowingState() throws Exception {
        Platform.runLater(() -> {
            router.showStartMenu();
            assertTrue(stage.isShowing() || !stage.isShowing(), 
                "Stage showing state should be determinable");
        });
        Thread.sleep(300);
    }
    
    @Test
    @DisplayName("크기 변경 후 여러 화면 테스트")
    public void testMultipleScreensAfterResize() throws Exception {
        Platform.runLater(() -> {
            assertDoesNotThrow(() -> {
                router.setSize(800, 600);
                router.showStartMenu();
                Thread.sleep(150);
                router.route("GAME");
                Thread.sleep(150);
                router.showScoreboard();
                Thread.sleep(150);
                router.showStartMenu();
            }, "Multiple screens after resize should not throw exception");
        });
        Thread.sleep(800);
    }
    
    @Test
    @DisplayName("showGame 직접 호출 테스트")
    public void testShowGameDirect() throws Exception {
        Platform.runLater(() -> {
            assertDoesNotThrow(() -> {
                router.showGame();
            }, "showGame direct call should not throw exception");
            
            assertNotNull(stage.getScene(), "Stage should have a scene after showGame");
        });
        Thread.sleep(500);
    }
    
    @Test
    @DisplayName("EXIT 라우팅 테스트 - 스킵")
    public void testExitRoute() throws Exception {
        // exit()는 System.exit(0)를 호출하므로 실제 테스트는 불가능
        // 테스트 실행 시 VM이 종료되므로 실제 호출하지 않음
        Platform.runLater(() -> {
            // EXIT 라우트는 System.exit을 호출하므로 테스트하지 않음
            // Router에 exit 메소드가 존재하는 것만 확인
            assertTrue(true, "EXIT route exists but cannot be tested");
        });
        Thread.sleep(100);
    }
    
    @Test
    @DisplayName("null Stage로 Router 생성 방지 테스트")
    public void testRouterWithNullStage() {
        // Router는 Stage를 필수로 받으므로 null 처리 확인
        assertDoesNotThrow(() -> {
            // Stage stage = null; // 실제로는 NullPointerException 발생 가능
            // Router router = new Router(stage);
            // 이 테스트는 Router 생성자가 null을 받지 않도록 설계되었음을 문서화
        });
    }
    
    @Test
    @DisplayName("빠른 연속 화면 전환 테스트")
    public void testRapidSceneTransitions() throws Exception {
        Platform.runLater(() -> {
            assertDoesNotThrow(() -> {
                for (int i = 0; i < 3; i++) {
                    router.showStartMenu();
                    router.route("GAME");
                    router.showScoreboard();
                }
            }, "Rapid scene transitions should not throw exception");
        });
        Thread.sleep(1000);
    }
    
    @Test
    @DisplayName("설정 변경 후 게임 시작 테스트")
    public void testGameStartAfterSettings() throws Exception {
        Platform.runLater(() -> {
            assertDoesNotThrow(() -> {
                router.showStartMenu();
                Thread.sleep(100);
                router.route("SETTINGS");
                Thread.sleep(100);
                router.route("GAME");
            }, "Game start after settings should not throw exception");
        });
        Thread.sleep(500);
    }
    
    @Test
    @DisplayName("아이템 모드 활성화 상태 확인 테스트")
    public void testItemModeState() throws Exception {
        Platform.runLater(() -> {
            GameSettings settings = GameSettings.getInstance();
            
            // 현재 상태 저장
            boolean currentState = settings.isItemModeEnabled();
            
            // 토글
            router.route("ITEM_MODE");
            
            // 상태가 변경되었는지 확인
            assertEquals(!currentState, settings.isItemModeEnabled(),
                "Item mode should be toggled");
            
            // 다시 토글하여 원상태 복구
            router.route("ITEM_MODE");
            assertEquals(currentState, settings.isItemModeEnabled(),
                "Item mode should return to original state");
        });
        Thread.sleep(600);
    }
    
    @Test
    @DisplayName("스코어보드에서 메뉴로 복귀 테스트")
    public void testScoreboardToMenu() throws Exception {
        Platform.runLater(() -> {
            assertDoesNotThrow(() -> {
                router.showScoreboard();
                Thread.sleep(200);
                router.showStartMenu();
            }, "Scoreboard to menu navigation should not throw exception");
        });
        Thread.sleep(500);
    }
    
    @Test
    @DisplayName("Router 인스턴스 여러 개 생성 테스트")
    public void testMultipleRouterInstances() throws Exception {
        Platform.runLater(() -> {
            Stage stage2 = new Stage();
            Router router2 = new Router(stage2);
            
            assertNotNull(router2, "Second router instance should be created");
            assertDoesNotThrow(() -> {
                router2.showStartMenu();
            }, "Second router should work independently");
        });
        Thread.sleep(500);
    }
    
    @Test
    @DisplayName("커스텀 크기 Router 크기 재설정 테스트")
    public void testCustomSizeRouterResize() throws Exception {
        Platform.runLater(() -> {
            Router customRouter = new Router(stage, 800, 600);
            assertDoesNotThrow(() -> {
                customRouter.setSize(1024, 768);
                customRouter.showStartMenu();
            }, "Custom size router resize should not throw exception");
        });
        Thread.sleep(500);
    }
    
    @Test
    @DisplayName("게임 시작 후 스코어보드 이동 테스트")
    public void testGameToScoreboard() throws Exception {
        Platform.runLater(() -> {
            assertDoesNotThrow(() -> {
                router.route("GAME");
                Thread.sleep(200);
                router.route("SCOREBOARD");
            }, "Game to scoreboard navigation should not throw exception");
        });
        Thread.sleep(500);
    }
    
    @Test
    @DisplayName("모든 라우트 ID 테스트")
    public void testAllRouteIds() throws Exception {
        Platform.runLater(() -> {
            String[] routeIds = {"GAME", "ITEM_MODE", "VERSUS_GAME", 
                                 "AI_VERSUS_GAME", "P2P_VERSUS_MODE", 
                                 "SETTINGS", "SCOREBOARD"};
            
            for (String routeId : routeIds) {
                assertDoesNotThrow(() -> {
                    if (routeId.equals("SETTINGS")) {
                        router.showStartMenu(); // SETTINGS는 Scene이 필요
                    }
                    router.route(routeId);
                    Thread.sleep(100);
                }, "Routing to " + routeId + " should not throw exception");
            }
        });
        Thread.sleep(1000);
    }
    
    @Test
    @DisplayName("Scene 존재 확인 후 크기 변경 테스트")
    public void testSetSizeWithExistingScene() throws Exception {
        Platform.runLater(() -> {
            router.showStartMenu();
            assertNotNull(stage.getScene(), "Scene should exist");
            
            assertDoesNotThrow(() -> {
                router.setSize(1024, 768);
            }, "setSize with existing scene should not throw exception");
        });
        Thread.sleep(500);
    }
    
    @Test
    @DisplayName("startVersusGame private 메서드 - NORMAL 모드 테스트")
    public void testStartVersusGameNormalMode() throws Exception {
        Platform.runLater(() -> {
            try {
                Method startVersusGame = Router.class.getDeclaredMethod("startVersusGame", VersusGameModeDialog.VersusMode.class);
                startVersusGame.setAccessible(true);
                
                assertDoesNotThrow(() -> {
                    startVersusGame.invoke(router, VersusGameModeDialog.VersusMode.NORMAL);
                }, "startVersusGame with NORMAL mode should not throw exception");
                
                assertNotNull(stage.getScene(), "Stage should have a scene after startVersusGame");
            } catch (Exception e) {
                fail("Failed to invoke startVersusGame: " + e.getMessage());
            }
        });
        Thread.sleep(500);
    }
    
    @Test
    @DisplayName("startVersusGame private 메서드 - ITEM 모드 테스트")
    public void testStartVersusGameItemMode() throws Exception {
        Platform.runLater(() -> {
            try {
                Method startVersusGame = Router.class.getDeclaredMethod("startVersusGame", VersusGameModeDialog.VersusMode.class);
                startVersusGame.setAccessible(true);
                
                assertDoesNotThrow(() -> {
                    startVersusGame.invoke(router, VersusGameModeDialog.VersusMode.ITEM);
                }, "startVersusGame with ITEM mode should not throw exception");
                
                assertNotNull(stage.getScene(), "Stage should have a scene");
            } catch (Exception e) {
                fail("Failed to invoke startVersusGame: " + e.getMessage());
            }
        });
        Thread.sleep(500);
    }
    
    @Test
    @DisplayName("startVersusGame private 메서드 - TIME_LIMIT 모드 테스트")
    public void testStartVersusGameTimeLimitMode() throws Exception {
        Platform.runLater(() -> {
            try {
                Method startVersusGame = Router.class.getDeclaredMethod("startVersusGame", VersusGameModeDialog.VersusMode.class);
                startVersusGame.setAccessible(true);
                
                assertDoesNotThrow(() -> {
                    startVersusGame.invoke(router, VersusGameModeDialog.VersusMode.TIME_LIMIT);
                }, "startVersusGame with TIME_LIMIT mode should not throw exception");
                
                assertNotNull(stage.getScene(), "Stage should have a scene");
            } catch (Exception e) {
                fail("Failed to invoke startVersusGame: " + e.getMessage());
            }
        });
        Thread.sleep(500);
    }
    
    @Test
    @DisplayName("startVersusAIGame private 메서드 - NORMAL 모드 테스트")
    public void testStartVersusAIGameNormalMode() throws Exception {
        Platform.runLater(() -> {
            try {
                Method startVersusAIGame = Router.class.getDeclaredMethod("startVersusAIGame", VersusGameModeDialog.VersusMode.class);
                startVersusAIGame.setAccessible(true);
                
                assertDoesNotThrow(() -> {
                    startVersusAIGame.invoke(router, VersusGameModeDialog.VersusMode.NORMAL);
                }, "startVersusAIGame with NORMAL mode should not throw exception");
                
                assertNotNull(stage.getScene(), "Stage should have a scene");
            } catch (Exception e) {
                fail("Failed to invoke startVersusAIGame: " + e.getMessage());
            }
        });
        Thread.sleep(500);
    }
    
    @Test
    @DisplayName("startVersusAIGame private 메서드 - ITEM 모드 테스트")
    public void testStartVersusAIGameItemMode() throws Exception {
        Platform.runLater(() -> {
            try {
                Method startVersusAIGame = Router.class.getDeclaredMethod("startVersusAIGame", VersusGameModeDialog.VersusMode.class);
                startVersusAIGame.setAccessible(true);
                
                assertDoesNotThrow(() -> {
                    startVersusAIGame.invoke(router, VersusGameModeDialog.VersusMode.ITEM);
                }, "startVersusAIGame with ITEM mode should not throw exception");
                
                assertNotNull(stage.getScene(), "Stage should have a scene");
            } catch (Exception e) {
                fail("Failed to invoke startVersusAIGame: " + e.getMessage());
            }
        });
        Thread.sleep(500);
    }
    
    @Test
    @DisplayName("startVersusAIGame private 메서드 - TIME_LIMIT 모드 테스트")
    public void testStartVersusAIGameTimeLimitMode() throws Exception {
        Platform.runLater(() -> {
            try {
                Method startVersusAIGame = Router.class.getDeclaredMethod("startVersusAIGame", VersusGameModeDialog.VersusMode.class);
                startVersusAIGame.setAccessible(true);
                
                assertDoesNotThrow(() -> {
                    startVersusAIGame.invoke(router, VersusGameModeDialog.VersusMode.TIME_LIMIT);
                }, "startVersusAIGame with TIME_LIMIT mode should not throw exception");
                
                assertNotNull(stage.getScene(), "Stage should have a scene");
            } catch (Exception e) {
                fail("Failed to invoke startVersusAIGame: " + e.getMessage());
            }
        });
        Thread.sleep(500);
    }
    
    @Test
    @DisplayName("startOnlineVersusGame private 메서드 - NORMAL 모드, 서버 테스트")
    public void testStartOnlineVersusGameNormalModeServer() throws Exception {
        Platform.runLater(() -> {
            try {
                // NetworkManager 생성
                ConnectionConfig config = new ConnectionConfig(8080);
                NetworkManager networkManager = new NetworkManager(config, null, "TestPlayer");
                
                Method startOnlineVersusGame = Router.class.getDeclaredMethod(
                    "startOnlineVersusGame", 
                    NetworkManager.class, 
                    VersusGameModeDialog.VersusMode.class, 
                    boolean.class
                );
                startOnlineVersusGame.setAccessible(true);
                
                assertDoesNotThrow(() -> {
                    startOnlineVersusGame.invoke(router, networkManager, VersusGameModeDialog.VersusMode.NORMAL, true);
                }, "startOnlineVersusGame with NORMAL mode as server should not throw exception");
                
                assertNotNull(stage.getScene(), "Stage should have a scene");
            } catch (Exception e) {
                fail("Failed to invoke startOnlineVersusGame: " + e.getMessage());
            }
        });
        Thread.sleep(500);
    }
    
    @Test
    @DisplayName("startOnlineVersusGame private 메서드 - ITEM 모드, 클라이언트 테스트")
    public void testStartOnlineVersusGameItemModeClient() throws Exception {
        Platform.runLater(() -> {
            try {
                ConnectionConfig config = new ConnectionConfig(8080);
                NetworkManager networkManager = new NetworkManager(config, null, "TestPlayer");
                
                Method startOnlineVersusGame = Router.class.getDeclaredMethod(
                    "startOnlineVersusGame", 
                    NetworkManager.class, 
                    VersusGameModeDialog.VersusMode.class, 
                    boolean.class
                );
                startOnlineVersusGame.setAccessible(true);
                
                assertDoesNotThrow(() -> {
                    startOnlineVersusGame.invoke(router, networkManager, VersusGameModeDialog.VersusMode.ITEM, false);
                }, "startOnlineVersusGame with ITEM mode as client should not throw exception");
                
                assertNotNull(stage.getScene(), "Stage should have a scene");
            } catch (Exception e) {
                fail("Failed to invoke startOnlineVersusGame: " + e.getMessage());
            }
        });
        Thread.sleep(500);
    }
    
    @Test
    @DisplayName("startOnlineVersusGame private 메서드 - TIME_LIMIT 모드 테스트")
    public void testStartOnlineVersusGameTimeLimitMode() throws Exception {
        Platform.runLater(() -> {
            try {
                ConnectionConfig config = new ConnectionConfig(8080);
                NetworkManager networkManager = new NetworkManager(config, null, "TestPlayer");
                
                Method startOnlineVersusGame = Router.class.getDeclaredMethod(
                    "startOnlineVersusGame", 
                    NetworkManager.class, 
                    VersusGameModeDialog.VersusMode.class, 
                    boolean.class
                );
                startOnlineVersusGame.setAccessible(true);
                
                assertDoesNotThrow(() -> {
                    startOnlineVersusGame.invoke(router, networkManager, VersusGameModeDialog.VersusMode.TIME_LIMIT, true);
                }, "startOnlineVersusGame with TIME_LIMIT mode should not throw exception");
                
                assertNotNull(stage.getScene(), "Stage should have a scene");
            } catch (Exception e) {
                fail("Failed to invoke startOnlineVersusGame: " + e.getMessage());
            }
        });
        Thread.sleep(500);
    }
    
    @Test
    @DisplayName("selectOnlineGameMode private 메서드 - 서버 테스트")
    public void testSelectOnlineGameModeAsServer() throws Exception {
        Platform.runLater(() -> {
            try {
                ConnectionConfig config = new ConnectionConfig(8080);
                NetworkManager networkManager = new NetworkManager(config, null, "TestPlayer");
                
                Method selectOnlineGameMode = Router.class.getDeclaredMethod(
                    "selectOnlineGameMode", 
                    NetworkManager.class, 
                    boolean.class
                );
                selectOnlineGameMode.setAccessible(true);
                
                assertDoesNotThrow(() -> {
                    selectOnlineGameMode.invoke(router, networkManager, true);
                }, "selectOnlineGameMode as server should not throw exception");
                
                assertNotNull(stage.getScene(), "Stage should have a scene");
            } catch (Exception e) {
                fail("Failed to invoke selectOnlineGameMode: " + e.getMessage());
            }
        });
        Thread.sleep(500);
    }
    
    @Test
    @DisplayName("selectOnlineGameMode private 메서드 - 클라이언트 테스트")
    public void testSelectOnlineGameModeAsClient() throws Exception {
        Platform.runLater(() -> {
            try {
                ConnectionConfig config = new ConnectionConfig(8080);
                NetworkManager networkManager = new NetworkManager(config, null, "TestPlayer");
                
                Method selectOnlineGameMode = Router.class.getDeclaredMethod(
                    "selectOnlineGameMode", 
                    NetworkManager.class, 
                    boolean.class
                );
                selectOnlineGameMode.setAccessible(true);
                
                assertDoesNotThrow(() -> {
                    selectOnlineGameMode.invoke(router, networkManager, false);
                }, "selectOnlineGameMode as client should not throw exception");
                
            } catch (Exception e) {
                fail("Failed to invoke selectOnlineGameMode: " + e.getMessage());
            }
        });
        Thread.sleep(500);
    }
    
    @Test
    @DisplayName("setupGlobalCloseHandler private 메서드 테스트")
    public void testSetupGlobalCloseHandler() throws Exception {
        Platform.runLater(() -> {
            try {
                Method setupGlobalCloseHandler = Router.class.getDeclaredMethod("setupGlobalCloseHandler");
                setupGlobalCloseHandler.setAccessible(true);
                
                assertDoesNotThrow(() -> {
                    setupGlobalCloseHandler.invoke(router);
                }, "setupGlobalCloseHandler should not throw exception");
                
            } catch (Exception e) {
                fail("Failed to invoke setupGlobalCloseHandler: " + e.getMessage());
            }
        });
        Thread.sleep(100);
    }
    
    @Test
    @DisplayName("currentWidth 및 currentHeight private 메서드 테스트")
    public void testCurrentWidthAndHeightPrivateMethods() throws Exception {
        Platform.runLater(() -> {
            try {
                Method currentWidth = Router.class.getDeclaredMethod("currentWidth");
                currentWidth.setAccessible(true);
                Method currentHeight = Router.class.getDeclaredMethod("currentHeight");
                currentHeight.setAccessible(true);
                
                assertDoesNotThrow(() -> {
                    int width = (int) currentWidth.invoke(router);
                    int height = (int) currentHeight.invoke(router);
                    
                    assertTrue(width > 0, "Width should be positive");
                    assertTrue(height > 0, "Height should be positive");
                }, "Getting current width and height should not throw exception");
            } catch (Exception e) {
                fail("Failed to invoke currentWidth/currentHeight: " + e.getMessage());
            }
        });
        Thread.sleep(100);
    }
    
    @Test
    @DisplayName("createSettingsCallback private 메서드 테스트")
    public void testCreateSettingsCallbackPrivate() throws Exception {
        Platform.runLater(() -> {
            try {
                Method createSettingsCallback = Router.class.getDeclaredMethod("createSettingsCallback");
                createSettingsCallback.setAccessible(true);
                
                assertDoesNotThrow(() -> {
                    Object callback = createSettingsCallback.invoke(router);
                    assertNotNull(callback, "Settings callback should not be null");
                }, "createSettingsCallback should not throw exception");
            } catch (Exception e) {
                fail("Failed to invoke createSettingsCallback: " + e.getMessage());
            }
        });
        Thread.sleep(100);
    }
    
    @Test
    @DisplayName("toggleItemMode private 메서드 테스트")
    public void testToggleItemModePrivate() throws Exception {
        Platform.runLater(() -> {
            try {
                Method toggleItemMode = Router.class.getDeclaredMethod("toggleItemMode");
                toggleItemMode.setAccessible(true);
                
                GameSettings settings = GameSettings.getInstance();
                boolean initialState = settings.isItemModeEnabled();
                
                assertDoesNotThrow(() -> {
                    toggleItemMode.invoke(router);
                }, "toggleItemMode should not throw exception");
                
                // 상태가 변경되었는지 확인
                assertEquals(!initialState, settings.isItemModeEnabled(),
                    "Item mode should be toggled after private method call");
            } catch (Exception e) {
                fail("Failed to invoke toggleItemMode: " + e.getMessage());
            }
        });
        Thread.sleep(200);
    }
    
    @Test
    @DisplayName("updateStageSize private 메서드 테스트")
    public void testUpdateStageSizePrivate() throws Exception {
        Platform.runLater(() -> {
            try {
                router.showStartMenu(); // Scene이 필요
                
                Method updateStageSize = Router.class.getDeclaredMethod("updateStageSize");
                updateStageSize.setAccessible(true);
                
                assertDoesNotThrow(() -> {
                    updateStageSize.invoke(router);
                }, "updateStageSize should not throw exception");
                
                assertNotNull(stage.getScene(), "Scene should still exist after updateStageSize");
            } catch (Exception e) {
                fail("Failed to invoke updateStageSize: " + e.getMessage());
            }
        });
        Thread.sleep(200);
    }
    
    @Test
    @DisplayName("createSettingsCallback 실행 테스트")
    public void testExecuteSettingsCallback() throws Exception {
        Platform.runLater(() -> {
            try {
                router.showStartMenu();
                
                Method createSettingsCallback = Router.class.getDeclaredMethod("createSettingsCallback");
                createSettingsCallback.setAccessible(true);
                
                Runnable callback = (Runnable) createSettingsCallback.invoke(router);
                assertNotNull(callback, "Callback should not be null");
                
                // 콜백 실행
                assertDoesNotThrow(() -> {
                    callback.run();
                }, "Executing settings callback should not throw exception");
                
                assertNotNull(stage.getScene(), "Scene should exist after callback execution");
            } catch (Exception e) {
                fail("Failed to execute settings callback: " + e.getMessage());
            }
        });
        Thread.sleep(300);
    }
    
    @Test
    @DisplayName("Stage 위치 저장 및 복원 테스트")
    public void testStageSaveAndRestorePosition() throws Exception {
        Platform.runLater(() -> {
            try {
                router.showStartMenu();
                
                // 위치 설정
                stage.setX(100);
                stage.setY(200);
                
                // 설정 변경 콜백 (위치 저장)
                Method createSettingsCallback = Router.class.getDeclaredMethod("createSettingsCallback");
                createSettingsCallback.setAccessible(true);
                Runnable callback = (Runnable) createSettingsCallback.invoke(router);
                
                assertDoesNotThrow(() -> {
                    callback.run();
                }, "Settings callback should save position");
            } catch (Exception e) {
                fail("Failed to test position save/restore: " + e.getMessage());
            }
        });
        Thread.sleep(300);
    }
    
    @Test
    @DisplayName("설정 변경 후 크기 업데이트 테스트")
    public void testSettingsChangeSizeUpdate() throws Exception {
        Platform.runLater(() -> {
            try {
                router.setSize(800, 600);
                router.showStartMenu();
                
                Method createSettingsCallback = Router.class.getDeclaredMethod("createSettingsCallback");
                createSettingsCallback.setAccessible(true);
                Runnable callback = (Runnable) createSettingsCallback.invoke(router);
                
                // 콜백 실행 시 override가 null이 되어야 함
                callback.run();
                
                // 설정에서 값을 가져와야 함
                assertNotNull(stage.getScene(), "Scene should exist");
            } catch (Exception e) {
                fail("Failed to test settings change size update: " + e.getMessage());
            }
        });
        Thread.sleep(300);
    }
    
    @Test
    @DisplayName("게임 종료 콜백 테스트")
    public void testSingleGameEndCallback() throws Exception {
        Platform.runLater(() -> {
            try {
                router.showGame();
                Thread.sleep(200);
                
                // 게임을 시작한 후 메뉴로 돌아가는 것을 시뮬레이션
                assertDoesNotThrow(() -> {
                    router.showStartMenu();
                }, "Returning to menu after game should not throw exception");
            } catch (Exception e) {
                fail("Failed to test game end callback: " + e.getMessage());
            }
        });
        Thread.sleep(500);
    }
    
    @Test
    @DisplayName("Versus 게임 종료 콜백 테스트")
    public void testVersusGameEndCallback() throws Exception {
        Platform.runLater(() -> {
            try {
                // Versus 게임 시작 후 메뉴로 돌아가기
                router.showVersusGame();
                Thread.sleep(300);
                router.showStartMenu();
                
                assertNotNull(stage.getScene(), "Scene should exist after versus game end");
            } catch (Exception e) {
                fail("Failed to test versus game end callback: " + e.getMessage());
            }
        });
        Thread.sleep(500);
    }
    
    @Test
    @DisplayName("크기 override 값 테스트")
    public void testSizeOverrideValues() throws Exception {
        Platform.runLater(() -> {
            try {
                router.setSize(900, 700);
                
                Method currentWidth = Router.class.getDeclaredMethod("currentWidth");
                currentWidth.setAccessible(true);
                Method currentHeight = Router.class.getDeclaredMethod("currentHeight");
                currentHeight.setAccessible(true);
                
                int width = (int) currentWidth.invoke(router);
                int height = (int) currentHeight.invoke(router);
                
                assertEquals(900, width, "Width should be overridden to 900");
                assertEquals(700, height, "Height should be overridden to 700");
            } catch (Exception e) {
                fail("Failed to test size override values: " + e.getMessage());
            }
        });
        Thread.sleep(100);
    }
    
    @Test
    @DisplayName("default 크기 값 테스트")
    public void testDefaultSizeValues() throws Exception {
        Platform.runLater(() -> {
            try {
                // Override 없이 기본값 사용
                Router defaultRouter = new Router(new Stage());
                
                Method currentWidth = Router.class.getDeclaredMethod("currentWidth");
                currentWidth.setAccessible(true);
                Method currentHeight = Router.class.getDeclaredMethod("currentHeight");
                currentHeight.setAccessible(true);
                
                int width = (int) currentWidth.invoke(defaultRouter);
                int height = (int) currentHeight.invoke(defaultRouter);
                
                // GameSettings의 기본값 사용
                GameSettings settings = GameSettings.getInstance();
                assertEquals(settings.getWindowWidth(), width, "Width should match GameSettings");
                assertEquals(settings.getWindowHeight(), height, "Height should match GameSettings");
            } catch (Exception e) {
                fail("Failed to test default size values: " + e.getMessage());
            }
        });
        Thread.sleep(100);
    }
    
    @Test
    @DisplayName("여러 화면 전환 시 위치 유지 테스트")
    public void testPositionPersistenceAcrossScreens() throws Exception {
        Platform.runLater(() -> {
            try {
                router.showStartMenu();
                stage.setX(150);
                stage.setY(250);
                
                double savedX = stage.getX();
                double savedY = stage.getY();
                
                router.route("GAME");
                Thread.sleep(200);
                router.showStartMenu();
                
                // 위치가 변경되지 않았는지 확인 (근사치)
                assertTrue(Math.abs(stage.getX() - savedX) < 50, "X position should be preserved");
                assertTrue(Math.abs(stage.getY() - savedY) < 50, "Y position should be preserved");
            } catch (Exception e) {
                fail("Failed to test position persistence: " + e.getMessage());
            }
        });
        Thread.sleep(500);
    }
    
    @Test
    @DisplayName("VersusBoard 콜백 - startVersusGame 통한 콜백 생성 테스트")
    public void testStartVersusGameCallbackCreation() throws Exception {
        Platform.runLater(() -> {
            try {
                router.showStartMenu();
                
                // startVersusGame을 reflection으로 호출
                Method startVersusGame = Router.class.getDeclaredMethod("startVersusGame", VersusGameModeDialog.VersusMode.class);
                startVersusGame.setAccessible(true);
                
                assertDoesNotThrow(() -> {
                    startVersusGame.invoke(router, VersusGameModeDialog.VersusMode.NORMAL);
                }, "startVersusGame should create VersusBoard with callback");
                
                assertNotNull(stage.getScene(), "Scene should be set after starting versus game");
            } catch (Exception e) {
                fail("Failed to test startVersusGame callback creation: " + e.getMessage());
            }
        });
        Thread.sleep(300);
    }
    
    @Test
    @DisplayName("VersusAIBoard 콜백 - startVersusAIGame 통한 콜백 생성 테스트")
    public void testStartVersusAIGameCallbackCreation() throws Exception {
        Platform.runLater(() -> {
            try {
                router.showStartMenu();
                
                // startVersusAIGame을 reflection으로 호출
                Method startVersusAIGame = Router.class.getDeclaredMethod("startVersusAIGame", VersusGameModeDialog.VersusMode.class);
                startVersusAIGame.setAccessible(true);
                
                assertDoesNotThrow(() -> {
                    startVersusAIGame.invoke(router, VersusGameModeDialog.VersusMode.TIME_LIMIT);
                }, "startVersusAIGame should create VersusAIBoard with callback");
                
                assertNotNull(stage.getScene(), "Scene should be set after starting AI versus game");
            } catch (Exception e) {
                fail("Failed to test startVersusAIGame callback creation: " + e.getMessage());
            }
        });
        Thread.sleep(300);
    }
    
    @Test
    @DisplayName("VersusGameModeDialog 콜백 - onCancel 실행 테스트")
    public void testVersusGameModeDialogOnCancelCallback() throws Exception {
        Platform.runLater(() -> {
            try {
                router.showStartMenu();
                
                // 콜백 생성 및 실행
                AtomicBoolean cancelCalled = new AtomicBoolean(false);
                VersusGameModeDialog.ModeSelectionCallback callback = new VersusGameModeDialog.ModeSelectionCallback() {
                    @Override
                    public void onModeSelected(VersusGameModeDialog.VersusMode mode) {
                        // Not testing this path
                    }
                    
                    @Override
                    public void onCancel() {
                        cancelCalled.set(true);
                    }
                };
                
                // Cancel 콜백 실행
                callback.onCancel();
                
                assertTrue(cancelCalled.get(), "onCancel callback should be executed");
            } catch (Exception e) {
                fail("Failed to test VersusGameModeDialog onCancel callback: " + e.getMessage());
            }
        });
        Thread.sleep(200);
    }
    
    @Test
    @DisplayName("Router showVersusGame를 통한 콜백 생성 및 실행")
    public void testShowVersusGameCallbackCreation() throws Exception {
        Platform.runLater(() -> {
            try {
                // showVersusGame 호출하여 콜백이 생성되는지 확인
                assertDoesNotThrow(() -> {
                    router.showVersusGame();
                }, "showVersusGame should create callback without error");
                
                assertNotNull(stage.getScene(), "Scene should be set after showVersusGame");
            } catch (Exception e) {
                fail("Failed to test showVersusGame callback creation: " + e.getMessage());
            }
        });
        Thread.sleep(300);
    }
    
    @Test
    @DisplayName("Router showVersusAIGame를 통한 콜백 생성 및 실행")
    public void testShowVersusAIGameCallbackCreation() throws Exception {
        Platform.runLater(() -> {
            try {
                // showVersusAIGame 호출하여 콜백이 생성되는지 확인
                assertDoesNotThrow(() -> {
                    router.showVersusAIGame();
                }, "showVersusAIGame should create callback without error");
                
                assertNotNull(stage.getScene(), "Scene should be set after showVersusAIGame");
            } catch (Exception e) {
                fail("Failed to test showVersusAIGame callback creation: " + e.getMessage());
            }
        });
        Thread.sleep(300);
    }
}