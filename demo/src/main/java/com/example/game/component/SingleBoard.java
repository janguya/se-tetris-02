package com.example.game.component;

import com.example.Router;
import com.example.gameover.GameOverScene;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

/**
 * 싱글 플레이 게임 보드
 * Board를 상속받아 상단 정보만 추가
 */
public class SingleBoard extends Board {
    
    public interface SingleGameCallback {
        void onGameOver(int score, int linesCleared);
        void onGameEnd();
    }
    
    private final SingleGameCallback callback;
    
    public SingleBoard(SingleGameCallback callback) {
        super(); // Board의 생성자 호출
        this.callback = callback;
        
        // Board의 초기화가 완료된 후 추가 초기화
        initializeSingleBoardUI();
    }
    
    /**
     * 싱글 보드 전용 UI 초기화 (상단 정보 추가)
     */
    private void initializeSingleBoardUI() {
        // 동적 크기 계산
        calculateDynamicSizes();
        
        // UI 초기화
        initializeUI();
        
        // 상단 정보 추가
        VBox topInfo = createTopInfo();
        root.setTop(topInfo);
        root.setMargin(topInfo, new Insets(0, 0, 10, 0));
        
        // 키 입력 처리 설정
        setupKeyHandling();
        
        // 게임 루프 시작
        startGameLoop();
        
        // 초기 보드 그리기
        drawBoard();
    }
    
    /**
     * 상단 정보 영역 생성
     */
    private VBox createTopInfo() {
        VBox topInfo = new VBox(10);
        topInfo.setAlignment(Pos.CENTER);
        topInfo.setPadding(new Insets(10));
        topInfo.getStyleClass().add("game-top-info");
        
        // 난이도 표시
        String difficulty = gameSettings.getDifficulty().toString();
        Label difficultyLabel = new Label("난이도: " + difficulty);
        difficultyLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        difficultyLabel.setStyle("-fx-text-fill: #bbbbbb;");
        topInfo.getChildren().add(difficultyLabel);
        
        // 아이템 모드 표시
        if (gameSettings.isItemModeEnabled()) {
            Label itemLabel = new Label("🎁 아이템 모드");
            itemLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
            itemLabel.setStyle("-fx-text-fill: #ffeb3b;");
            topInfo.getChildren().add(itemLabel);
        }
        
        return topInfo;
    }
    
    /**
     * 게임 오버 처리 오버라이드
     */
    @Override
    protected void gameOver() {
        // 게임 루프 정지
        if (gameLoop != null) {
            gameLoop.stop();
        }
        
        isGameOver = true;
        
        // 최종 점수와 라인 수 가져오기
        int finalScore = scorePanel.getScore();
        int linesCleared = scorePanel.getLinesCleared();
        
        System.out.println("=== Game Over ===");
        System.out.println("Final Score: " + finalScore);
        System.out.println("Lines Cleared: " + linesCleared);
        System.out.println("Difficulty: " + gameSettings.getDifficulty().name());
        System.out.println("Item Mode: " + gameSettings.isItemModeEnabled());
        
        // 콜백 호출
        if (callback != null) {
            callback.onGameOver(finalScore, linesCleared);
        }
        
        // GameOverScene으로 전환
        javafx.application.Platform.runLater(() -> {
            Stage stage = (Stage) mainContainer.getScene().getWindow();
            GameOverScene.show(stage, finalScore);
        });
    }
    
    /**
     * 메인 메뉴로 이동 (콜백 추가)
     */
    private void goToMainMenuWithCallback() {
        cleanup();
        
        if (callback != null) {
            callback.onGameEnd();
        }
        
        Stage stage = (Stage) mainContainer.getScene().getWindow();
        Router router = new Router(stage);
        router.showStartMenu();
    }
    
    /**
     * 게임 재시작 오버라이드
     */
    @Override
    public void restartGame() {
        super.restartGame();
        
        // 상단 정보 업데이트 (난이도나 모드가 변경되었을 수 있음)
        VBox topInfo = createTopInfo();
        root.setTop(topInfo);
        root.setMargin(topInfo, new Insets(0, 0, 10, 0));
        
        mainContainer.requestFocus();
    }
}
