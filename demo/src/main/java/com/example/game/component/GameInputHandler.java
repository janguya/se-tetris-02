package com.example.game.component;

import com.example.settings.GameSettings;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class GameInputHandler {
    
    public interface GameInputCallback {
        void onMoveLeft();
        void onMoveRight();
        void onMoveDown();
        void onRotate();
        void onHardDrop();
        void onPause();
        void onSettings();
        boolean isGameActive(); // 게임이 진행 중인지 확인
        boolean isMenuVisible(); // 메뉴가 보이는지 확인
        boolean isPaused(); // 일시정지 상태인지 확인 (추가)
    }
    
    private final GameInputCallback callback;
    private final GameSettings gameSettings;
    
    public GameInputHandler(GameInputCallback callback) {
        this.callback = callback;
        this.gameSettings = GameSettings.getInstance();
    }
    
    /**
     * 키 입력 이벤트를 처리합니다.
     * @param event 키 이벤트
     * @return 이벤트가 처리되었으면 true, 그렇지 않으면 false
     */
    public boolean handleKeyPressed(KeyEvent event) {
        KeyCode pressedKey = event.getCode();
        
        // ESC 키는 특별 처리 (항상 최우선)
        if (pressedKey == KeyCode.ESCAPE) {
            return handleEscapeKey();
        }
        
        // 메뉴가 열려있으면 다른 게임 입력 무시
        if (callback.isMenuVisible()) {
            return false;
        }
        
        // 게임이 비활성 상태일 때는 일시정지만 허용
        if (!callback.isGameActive()) {
            if (pressedKey == gameSettings.getKeyBinding("PAUSE")) {
                callback.onPause();
                return true;
            }
            return false;
        }
        
        // 게임 진행 중일 때 모든 입력 처리
        return handleGameplayInput(pressedKey) || handleNonGameplayInput(pressedKey);
    }
    
    /**
     * ESC 키 특별 처리
     */
    private boolean handleEscapeKey() {
        // 메뉴가 열려있으면 메뉴 닫기 (Board에서 처리)
        if (callback.isMenuVisible()) {
            callback.onSettings(); // 실제로는 메뉴 닫기 처리
            return true;
        }
        
        // 일시정지 상태이면 재개
        if (callback.isPaused()) {
            callback.onPause(); // 토글이므로 재개됨
            return true;
        }
        
        // 게임 중이면 일시정지
        if (callback.isGameActive()) {
            callback.onPause();
            return true;
        }
        
        // 게임 오버 상태에서는 설정/메뉴 처리
        callback.onSettings();
        return true;
    }
    
    /**
     * 게임플레이 관련 입력 처리 (게임이 진행 중일 때만)
     */
    private boolean handleGameplayInput(KeyCode key) {
        if (key == gameSettings.getKeyBinding("MOVE_LEFT")) {
            callback.onMoveLeft();
            return true;
        }
        
        if (key == gameSettings.getKeyBinding("MOVE_RIGHT")) {
            callback.onMoveRight();
            return true;
        }
        
        if (key == gameSettings.getKeyBinding("MOVE_DOWN")) {
            callback.onMoveDown();
            return true;
        }
        
        if (key == gameSettings.getKeyBinding("ROTATE")) {
            callback.onRotate();
            return true;
        }
        
        if (key == gameSettings.getKeyBinding("HARD_DROP")) {
            callback.onHardDrop();
            return true;
        }
        
        return false;
    }
    
    /**
     * 게임플레이와 무관한 입력 처리 (항상 처리 가능)
     */
    private boolean handleNonGameplayInput(KeyCode key) {
        if (key == gameSettings.getKeyBinding("PAUSE")) {
            callback.onPause();
            return true;
        }
        
        // ESC는 위에서 따로 처리하므로 여기서는 제외
        
        return false;
    }
    
    /**
     * 설정이 변경되었을 때 호출 (필요시 키 바인딩 새로고침)
     */
    public void refreshKeyBindings() {
        // 키 바인딩이 변경되었을 때 필요한 작업이 있다면 여기서 처리
        // 현재는 GameSettings에서 직접 가져오므로 별도 작업 불필요
    }
    
    /**
     * 현재 설정된 키 바인딩 정보를 문자열로 반환 (디버깅용)
     */
    public String getKeyBindingsInfo() {
        StringBuilder info = new StringBuilder("Current Key Bindings:\n");
        info.append("Move Left: ").append(gameSettings.getKeyBinding("MOVE_LEFT")).append("\n");
        info.append("Move Right: ").append(gameSettings.getKeyBinding("MOVE_RIGHT")).append("\n");
        info.append("Move Down: ").append(gameSettings.getKeyBinding("MOVE_DOWN")).append("\n");
        info.append("Rotate: ").append(gameSettings.getKeyBinding("ROTATE")).append("\n");
        info.append("Hard Drop: ").append(gameSettings.getKeyBinding("HARD_DROP")).append("\n");
        info.append("Pause: ").append(gameSettings.getKeyBinding("PAUSE")).append("\n");
        info.append("ESC: Always handles pause/resume/menu");
        return info.toString();
    }
}