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
        
        // UI 초기화ghk
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
    VBox topInfo = new VBox(0);
    topInfo.setAlignment(Pos.CENTER);
    topInfo.setPadding(new Insets(0));
    topInfo.getStyleClass().add("game-top-info");
    
    // BorderPane으로 레이아웃 구성
    javafx.scene.layout.BorderPane topLayout = new javafx.scene.layout.BorderPane();
    topLayout.setPadding(new Insets(5, 10, 5, 10));
    topInfo.getChildren().add(topLayout);
    
    // 중앙: 난이도와 아이템 모드
    javafx.scene.layout.HBox centerContainer = new javafx.scene.layout.HBox(15);
    centerContainer.setAlignment(Pos.CENTER);
    
    // 난이도 표시
    String difficulty = gameSettings.getDifficulty().toString();
    Label difficultyLabel = new Label("⚡ " + difficulty);
    difficultyLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
    difficultyLabel.getStyleClass().add("difficulty-label");
    
    // 난이도별 색상 적용
    switch (gameSettings.getDifficulty()) {
        case EASY:
            difficultyLabel.setStyle("-fx-text-fill: #00ff88;"); // 녹색
            break;
        case NORMAL:
            difficultyLabel.setStyle("-fx-text-fill: #ffeb3b;"); // 노란색
            break;
        case HARD:
            difficultyLabel.setStyle("-fx-text-fill: #ff9800;"); // 주황색
            break;
    }
    
    centerContainer.getChildren().add(difficultyLabel);
    
    // 아이템 모드 표시
    if (gameSettings.isItemModeEnabled()) {
        Label itemLabel = new Label("🎁 ITEM MODE");
        itemLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        itemLabel.getStyleClass().add("item-mode-label");
        centerContainer.getChildren().add(itemLabel);
    }
    
    // 중앙에 배치
    topLayout.setLeft(centerContainer);
    
    // 오른쪽: 종료 버튼
    javafx.scene.control.Button exitButton = createExitButton();
    topLayout.setRight(exitButton);
    javafx.scene.layout.BorderPane.setAlignment(exitButton, Pos.CENTER_RIGHT);

    return topInfo;
}

/**
 * 종료 버튼 생성
 */
private javafx.scene.control.Button createExitButton() {
    javafx.scene.control.Button exitButton = new javafx.scene.control.Button("✕");
    exitButton.getStyleClass().add("exit-button");
    
    // 클릭 시 확인 다이얼로그 표시
    exitButton.setOnAction(e -> {
        showExitConfirmDialog();
    });
    
    return exitButton;
}

/**
 * 종료 확인 다이얼로그 표시
 */
private void showExitConfirmDialog() {
    // 게임 일시정지
    boolean wasPaused = isPaused;
    if (!wasPaused) {
        isPaused = true;
        if (gameLoop != null) {
            gameLoop.stop();
        }
    }
    
    // 확인 다이얼로그 생성
    javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
        javafx.scene.control.Alert.AlertType.CONFIRMATION
    );
    alert.setTitle("게임 종료");
    alert.setHeaderText("정말 게임을 종료하시겠습니까?");
    alert.setContentText("현재 진행 중인 게임이 저장되지 않습니다.");
    
    // 다이얼로그 스타일 설정
    javafx.scene.control.DialogPane dialogPane = alert.getDialogPane();
    dialogPane.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
    dialogPane.getStyleClass().add("exit-dialog");
    
    // 버튼 텍스트 한글로 변경
    javafx.scene.control.ButtonType confirmButton = new javafx.scene.control.ButtonType(
        "종료", 
        javafx.scene.control.ButtonBar.ButtonData.OK_DONE
    );
    javafx.scene.control.ButtonType cancelButton = new javafx.scene.control.ButtonType(
        "취소", 
        javafx.scene.control.ButtonBar.ButtonData.CANCEL_CLOSE
    );
    
    alert.getButtonTypes().setAll(confirmButton, cancelButton);
    
    // 다이얼로그 표시 및 응답 처리
    alert.showAndWait().ifPresent(response -> {
        if (response == confirmButton) {
            // 게임 종료
            exitGame();
        } else {
            // 게임 재개
            if (!wasPaused) {
                isPaused = false;
                startGameLoop();
                mainContainer.requestFocus();
            }
        }
    });
}
/**
 * 게임 완전 종료
 */
private void exitGame() {
    cleanup();
    Stage stage = (Stage) mainContainer.getScene().getWindow();
    stage.close();
    System.exit(0);
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
