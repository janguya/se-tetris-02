package com.example;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import com.example.settings.GameSettings;

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
}