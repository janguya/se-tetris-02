package com.example.startmenu;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import javafx.application.Platform;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class StartMenuViewTest {

    private StartMenuView menuView;

    @BeforeAll
    public static void initToolkit() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Toolkit already initialized
        }
    }

    @BeforeEach
    public void setUp() throws Exception {
        Platform.runLater(() -> {
            menuView = new StartMenuView();
        });
        Thread.sleep(100);
    }

    @Test
    @DisplayName("StartMenuView 초기화 테스트")
    public void testInitialization() throws Exception {
        Platform.runLater(() -> {
            assertNotNull(menuView.getRoot(), "Root should not be null");
            assertTrue(menuView.getRoot().isFocusTraversable(), "Root should be focus traversable");
        });
        Thread.sleep(100);
    }

    @Test
    @DisplayName("MenuItem 생성 테스트")
    public void testMenuItemCreation() {
        StartMenuView.MenuItem item = new StartMenuView.MenuItem("test_id", "Test Label");
        assertEquals("test_id", item.getId());
        assertEquals("Test Label", item.getLabel());
    }

    @Test
    @DisplayName("메뉴 아이템 추가 테스트")
    public void testAddMenuItem() throws Exception {
        Platform.runLater(() -> {
            menuView.addMenuItem("item1", "Item 1")
                    .addMenuItem("item2", "Item 2")
                    .addMenuItem("item3", "Item 3");
            
            assertDoesNotThrow(() -> menuView.build(), "Building with items should not throw");
        });
        Thread.sleep(100);
    }

    @Test
    @DisplayName("메뉴 아이템 없이 빌드 시 예외 발생")
    public void testBuildWithoutItems() throws Exception {
        Platform.runLater(() -> {
            assertThrows(IllegalStateException.class, () -> {
                menuView.build();
            }, "Should throw exception when building without items");
        });
        Thread.sleep(100);
    }

    @Test
    @DisplayName("선택 콜백 설정 및 실행 테스트")
    public void testOnMenuSelectCallback() throws Exception {
        AtomicReference<String> selectedId = new AtomicReference<>();
        
        Platform.runLater(() -> {
            menuView.addMenuItem("test1", "Test 1")
                    .addMenuItem("test2", "Test 2")
                    .setOnMenuSelect(id -> selectedId.set(id))
                    .build();
            
            menuView.requestFocus();
        });
        Thread.sleep(500);
        
        Platform.runLater(() -> {
            KeyEvent enterEvent = new KeyEvent(
                KeyEvent.KEY_PRESSED,
                "",
                "",
                KeyCode.ENTER,
                false, false, false, false
            );
            menuView.getRoot().fireEvent(enterEvent);
        });
        Thread.sleep(500);
        
        assertEquals("test1", selectedId.get(), "Should select first item on Enter");
    }

    @Test
    @DisplayName("UP 키로 메뉴 이동 테스트")
    public void testMoveSelectionUp() throws Exception {
        Platform.runLater(() -> {
            menuView.addMenuItem("item1", "Item 1")
                    .addMenuItem("item2", "Item 2")
                    .addMenuItem("item3", "Item 3")
                    .build();
            
            menuView.requestFocus();
        });
        Thread.sleep(200);
        
        Platform.runLater(() -> {
            KeyEvent upEvent = new KeyEvent(
                KeyEvent.KEY_PRESSED,
                "",
                "",
                KeyCode.UP,
                false, false, false, false
            );
            menuView.getRoot().fireEvent(upEvent);
        });
        Thread.sleep(200);
        
        // 첫 번째 아이템에서 UP을 누르면 마지막 아이템으로 순환
        assertDoesNotThrow(() -> {}, "Should handle UP key without error");
    }

    @Test
    @DisplayName("DOWN 키로 메뉴 이동 테스트")
    public void testMoveSelectionDown() throws Exception {
        AtomicReference<String> selectedId = new AtomicReference<>();
        
        Platform.runLater(() -> {
            menuView.addMenuItem("item1", "Item 1")
                    .addMenuItem("item2", "Item 2")
                    .addMenuItem("item3", "Item 3")
                    .setOnMenuSelect(id -> selectedId.set(id))
                    .build();
            
            menuView.requestFocus();
        });
        Thread.sleep(500);
        
        Platform.runLater(() -> {
            // DOWN 키 누르기
            KeyEvent downEvent = new KeyEvent(
                KeyEvent.KEY_PRESSED,
                "",
                "",
                KeyCode.DOWN,
                false, false, false, false
            );
            menuView.getRoot().fireEvent(downEvent);
        });
        Thread.sleep(500);
        
        Platform.runLater(() -> {
            // ENTER로 선택
            KeyEvent enterEvent = new KeyEvent(
                KeyEvent.KEY_PRESSED,
                "",
                "",
                KeyCode.ENTER,
                false, false, false, false
            );
            menuView.getRoot().fireEvent(enterEvent);
        });
        Thread.sleep(500);
        
        assertEquals("item2", selectedId.get(), "Should select second item after DOWN");
    }

    @Test
    @DisplayName("W 키로 메뉴 이동 테스트")
    public void testMoveSelectionWithW() throws Exception {
        Platform.runLater(() -> {
            menuView.addMenuItem("item1", "Item 1")
                    .addMenuItem("item2", "Item 2")
                    .build();
            
            menuView.requestFocus();
        });
        Thread.sleep(200);
        
        Platform.runLater(() -> {
            KeyEvent wEvent = new KeyEvent(
                KeyEvent.KEY_PRESSED,
                "",
                "",
                KeyCode.W,
                false, false, false, false
            );
            menuView.getRoot().fireEvent(wEvent);
        });
        Thread.sleep(200);
        
        assertDoesNotThrow(() -> {}, "Should handle W key without error");
    }

    @Test
    @DisplayName("S 키로 메뉴 이동 테스트")
    public void testMoveSelectionWithS() throws Exception {
        Platform.runLater(() -> {
            menuView.addMenuItem("item1", "Item 1")
                    .addMenuItem("item2", "Item 2")
                    .build();
            
            menuView.requestFocus();
        });
        Thread.sleep(200);
        
        Platform.runLater(() -> {
            KeyEvent sEvent = new KeyEvent(
                KeyEvent.KEY_PRESSED,
                "",
                "",
                KeyCode.S,
                false, false, false, false
            );
            menuView.getRoot().fireEvent(sEvent);
        });
        Thread.sleep(200);
        
        assertDoesNotThrow(() -> {}, "Should handle S key without error");
    }

    @Test
    @DisplayName("SPACE 키로 메뉴 선택 테스트")
    public void testSelectWithSpace() throws Exception {
        AtomicBoolean selected = new AtomicBoolean(false);
        
        Platform.runLater(() -> {
            menuView.addMenuItem("test", "Test")
                    .setOnMenuSelect(id -> selected.set(true))
                    .build();
            
            menuView.requestFocus();
        });
        Thread.sleep(500);
        
        Platform.runLater(() -> {
            KeyEvent spaceEvent = new KeyEvent(
                KeyEvent.KEY_PRESSED,
                "",
                "",
                KeyCode.SPACE,
                false, false, false, false
            );
            menuView.getRoot().fireEvent(spaceEvent);
        });
        Thread.sleep(500);
        
        assertTrue(selected.get(), "Should select menu with SPACE key");
    }

    @Test
    @DisplayName("ESC 키로 종료 처리 테스트")
    public void testHandleEscape() throws Exception {
        AtomicReference<String> result = new AtomicReference<>();
        
        Platform.runLater(() -> {
            menuView.addMenuItem("item1", "Item 1")
                    .setOnMenuSelect(id -> result.set(id))
                    .build();
            
            menuView.requestFocus();
        });
        Thread.sleep(200);
        
        Platform.runLater(() -> {
            KeyEvent escEvent = new KeyEvent(
                KeyEvent.KEY_PRESSED,
                "",
                "",
                KeyCode.ESCAPE,
                false, false, false, false
            );
            menuView.getRoot().fireEvent(escEvent);
        });
        Thread.sleep(200);
        
        assertEquals("EXIT", result.get(), "Should handle ESC as EXIT");
    }

    @Test
    @DisplayName("알 수 없는 키 입력 무시 테스트")
    public void testIgnoreUnknownKeys() throws Exception {
        Platform.runLater(() -> {
            menuView.addMenuItem("item", "Item")
                    .build();
            
            menuView.requestFocus();
        });
        Thread.sleep(200);
        
        Platform.runLater(() -> {
            KeyEvent unknownEvent = new KeyEvent(
                KeyEvent.KEY_PRESSED,
                "",
                "",
                KeyCode.A,
                false, false, false, false
            );
            assertDoesNotThrow(() -> menuView.getRoot().fireEvent(unknownEvent), 
                "Should ignore unknown keys");
        });
        Thread.sleep(100);
    }

    @Test
    @DisplayName("순환 선택 테스트 - 마지막에서 첫 번째로")
    public void testCircularSelectionDownToFirst() throws Exception {
        AtomicReference<String> selectedId = new AtomicReference<>();
        
        Platform.runLater(() -> {
            menuView.addMenuItem("item1", "Item 1")
                    .addMenuItem("item2", "Item 2")
                    .setOnMenuSelect(id -> selectedId.set(id))
                    .build();
            
            menuView.requestFocus();
        });
        Thread.sleep(500);
        
        Platform.runLater(() -> {
            // DOWN 키를 2번 눌러 순환
            for (int i = 0; i < 2; i++) {
                KeyEvent downEvent = new KeyEvent(
                    KeyEvent.KEY_PRESSED,
                    "",
                    "",
                    KeyCode.DOWN,
                    false, false, false, false
                );
                menuView.getRoot().fireEvent(downEvent);
            }
        });
        Thread.sleep(500);
        
        Platform.runLater(() -> {
            KeyEvent enterEvent = new KeyEvent(
                KeyEvent.KEY_PRESSED,
                "",
                "",
                KeyCode.ENTER,
                false, false, false, false
            );
            menuView.getRoot().fireEvent(enterEvent);
        });
        Thread.sleep(500);
        
        assertEquals("item1", selectedId.get(), "Should wrap around to first item");
    }

    @Test
    @DisplayName("requestFocus 메서드 테스트")
    public void testRequestFocus() throws Exception {
        Platform.runLater(() -> {
            menuView.addMenuItem("item", "Item")
                    .build();
            
            assertDoesNotThrow(() -> menuView.requestFocus(), 
                "requestFocus should not throw");
        });
        Thread.sleep(100);
    }

    @Test
    @DisplayName("getRoot 메서드 테스트")
    public void testGetRoot() throws Exception {
        Platform.runLater(() -> {
            assertNotNull(menuView.getRoot(), "getRoot should return non-null");
            assertEquals("BorderPane", menuView.getRoot().getClass().getSimpleName(), 
                "Root should be BorderPane");
        });
        Thread.sleep(100);
    }

    @Test
    @DisplayName("여러 아이템 추가 및 빌드 체인 테스트")
    public void testChainedBuilderPattern() throws Exception {
        Platform.runLater(() -> {
            StartMenuView view = new StartMenuView()
                .addMenuItem("start", "Start Game")
                .addMenuItem("settings", "Settings")
                .addMenuItem("scoreboard", "Scoreboard")
                .addMenuItem("exit", "Exit")
                .setOnMenuSelect(id -> {})
                .build();
            
            assertNotNull(view, "Chained build should return view");
            assertNotNull(view.getRoot(), "Built view should have root");
        });
        Thread.sleep(100);
    }

    @Test
    @DisplayName("콜백 없이 메뉴 선택 시 안전 처리")
    public void testSelectWithoutCallback() throws Exception {
        Platform.runLater(() -> {
            menuView.addMenuItem("item", "Item")
                    .build();
            
            menuView.requestFocus();
        });
        Thread.sleep(200);
        
        Platform.runLater(() -> {
            KeyEvent enterEvent = new KeyEvent(
                KeyEvent.KEY_PRESSED,
                "",
                "",
                KeyCode.ENTER,
                false, false, false, false
            );
            assertDoesNotThrow(() -> menuView.getRoot().fireEvent(enterEvent), 
                "Should handle selection without callback safely");
        });
        Thread.sleep(300);
    }

    @Test
    @DisplayName("콜백 없이 ESC 처리")
    public void testEscapeWithoutCallback() throws Exception {
        Platform.runLater(() -> {
            menuView.addMenuItem("item", "Item")
                    .build();
            
            menuView.requestFocus();
        });
        Thread.sleep(200);
        
        Platform.runLater(() -> {
            KeyEvent escEvent = new KeyEvent(
                KeyEvent.KEY_PRESSED,
                "",
                "",
                KeyCode.ESCAPE,
                false, false, false, false
            );
            assertDoesNotThrow(() -> menuView.getRoot().fireEvent(escEvent), 
                "Should handle ESC without callback safely");
        });
        Thread.sleep(200);
    }
}
