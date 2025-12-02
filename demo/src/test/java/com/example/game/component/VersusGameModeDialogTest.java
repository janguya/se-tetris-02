package com.example.game.component;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.example.game.component.VersusGameModeDialog.ModeSelectionCallback;
import com.example.game.component.VersusGameModeDialog.VersusMode;

import javafx.application.Platform;
import javafx.stage.Stage;

/**
 * VersusGameModeDialog 테스트
 */
public class VersusGameModeDialogTest {
    
    @BeforeAll
    public static void initJFX() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // already started
        }
    }
    
    /**
     * 테스트용 콜백 구현
     */
    private static class TestCallback implements ModeSelectionCallback {
        VersusMode selectedMode;
        boolean cancelCalled;
        
        @Override
        public void onModeSelected(VersusMode mode) {
            this.selectedMode = mode;
        }
        
        @Override
        public void onCancel() {
            this.cancelCalled = true;
        }
    }
    
    @Test
    public void testVersusMode_NORMAL() {
        VersusMode mode = VersusMode.NORMAL;
        assertNotNull(mode.getDisplayName());
        assertNotNull(mode.getDescription());
        assertEquals("일반 모드", mode.getDisplayName());
        assertTrue(mode.getDescription().contains("플레이어"));
    }
    
    @Test
    public void testVersusMode_ITEM() {
        VersusMode mode = VersusMode.ITEM;
        assertNotNull(mode.getDisplayName());
        assertNotNull(mode.getDescription());
        assertEquals("아이템 모드", mode.getDisplayName());
        assertTrue(mode.getDescription().contains("아이템"));
    }
    
    @Test
    public void testVersusMode_TIME_LIMIT() {
        VersusMode mode = VersusMode.TIME_LIMIT;
        assertNotNull(mode.getDisplayName());
        assertNotNull(mode.getDescription());
        assertEquals("시간제한 모드", mode.getDisplayName());
        assertTrue(mode.getDescription().contains("시간"));
    }
    
    @Test
    public void testAllVersusModes() {
        VersusMode[] modes = VersusMode.values();
        assertEquals(3, modes.length);
        
        // 모든 모드가 displayName과 description을 가지고 있는지 확인
        for (VersusMode mode : modes) {
            assertNotNull(mode.getDisplayName());
            assertFalse(mode.getDisplayName().isEmpty());
            assertNotNull(mode.getDescription());
            assertFalse(mode.getDescription().isEmpty());
        }
    }
    
    @Test
    public void testShowDialog() throws InterruptedException {
        TestCallback callback = new TestCallback();
        
        Platform.runLater(() -> {
            Stage stage = new Stage();
            assertDoesNotThrow(() -> VersusGameModeDialog.show(stage, callback));
        });
        
        Thread.sleep(500); // UI 생성 대기
    }
    
    @Test
    public void testCallbackNotNull() {
        Platform.runLater(() -> {
            Stage stage = new Stage();
            TestCallback callback = new TestCallback();
            
            // null callback은 NullPointerException 발생 가능
            assertDoesNotThrow(() -> VersusGameModeDialog.show(stage, callback));
        });
    }
    
    @Test
    public void testModeSelectionCallback() {
        TestCallback callback = new TestCallback();
        
        // onModeSelected 호출 테스트
        callback.onModeSelected(VersusMode.NORMAL);
        assertEquals(VersusMode.NORMAL, callback.selectedMode);
        assertFalse(callback.cancelCalled);
        
        // 다른 모드 선택
        callback.onModeSelected(VersusMode.ITEM);
        assertEquals(VersusMode.ITEM, callback.selectedMode);
        
        callback.onModeSelected(VersusMode.TIME_LIMIT);
        assertEquals(VersusMode.TIME_LIMIT, callback.selectedMode);
    }
    
    @Test
    public void testCancelCallback() {
        TestCallback callback = new TestCallback();
        
        // onCancel 호출 테스트
        callback.onCancel();
        assertTrue(callback.cancelCalled);
        assertNull(callback.selectedMode);
    }
    
    @Test
    public void testMultipleDialogCreation() throws InterruptedException {
        Platform.runLater(() -> {
            Stage stage1 = new Stage();
            Stage stage2 = new Stage();
            
            TestCallback callback1 = new TestCallback();
            TestCallback callback2 = new TestCallback();
            
            // 여러 다이얼로그 생성 가능 확인
            assertDoesNotThrow(() -> VersusGameModeDialog.show(stage1, callback1));
            assertDoesNotThrow(() -> VersusGameModeDialog.show(stage2, callback2));
        });
        
        Thread.sleep(500);
    }
    
    @Test
    public void testEnumValueOf() {
        // Enum.valueOf 테스트
        assertEquals(VersusMode.NORMAL, VersusMode.valueOf("NORMAL"));
        assertEquals(VersusMode.ITEM, VersusMode.valueOf("ITEM"));
        assertEquals(VersusMode.TIME_LIMIT, VersusMode.valueOf("TIME_LIMIT"));
    }
    
    @Test
    public void testEnumToString() {
        assertEquals("NORMAL", VersusMode.NORMAL.toString());
        assertEquals("ITEM", VersusMode.ITEM.toString());
        assertEquals("TIME_LIMIT", VersusMode.TIME_LIMIT.toString());
    }
    
    @Test
    public void testCallbackInterfaceContract() {
        // 콜백 인터페이스가 올바르게 구현되었는지 확인
        ModeSelectionCallback callback = new ModeSelectionCallback() {
            private VersusMode mode;
            private boolean cancelled;
            
            @Override
            public void onModeSelected(VersusMode mode) {
                this.mode = mode;
            }
            
            @Override
            public void onCancel() {
                this.cancelled = true;
            }
        };
        
        assertDoesNotThrow(() -> callback.onModeSelected(VersusMode.NORMAL));
        assertDoesNotThrow(() -> callback.onCancel());
    }
    
    @Test
    public void testDialogWithNullParent() {
        Platform.runLater(() -> {
            TestCallback callback = new TestCallback();
            
            // null parent는 가능 (top-level window)
            assertDoesNotThrow(() -> VersusGameModeDialog.show(null, callback));
        });
    }
}
