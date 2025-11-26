package com.example.game.component;

import com.example.Router;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

/**
 * 대전 모드 전용 게임 오버 화면
 * 게임 화면을 정지시키고 그 위에 결과를 표시
 */
public class VersusGameOverScene {
    
    public enum GameResult {
        PLAYER1_WIN,
        PLAYER2_WIN,
        DRAW
    }
    
    private StackPane gameContainer; // 게임 컨테이너 참조
    private VBox resultOverlay;
    private Stage stage;
    private Runnable onRestart;
    
    public VersusGameOverScene(Stage stage, StackPane gameContainer, Runnable onRestart) {
        this.stage = stage;
        this.gameContainer = gameContainer;
        this.onRestart = onRestart;
    }
    
    /**
     * 게임 오버 화면 표시 - 게임 화면 위에 직접 그리기
     */
    public void show(GameResult result, int player1Score, int player2Score) {
        System.out.println("[VersusGameOverScene] show() called - Result: " + result + ", P1: " + player1Score + ", P2: " + player2Score);
        
        // 게임 화면은 그대로 두고 위에 반투명 배경 + 결과 표시
        resultOverlay = createResultOverlay(result, player1Score, player2Score);
        
        // 게임 컨테이너 맨 위에 추가
        gameContainer.getChildren().add(resultOverlay);
        resultOverlay.toFront();
        
        System.out.println("[VersusGameOverScene] Result overlay added. Children count: " + gameContainer.getChildren().size());
    }
    
    /**
     * 게임 오버 화면 숨기기
     */
    public void hide() {
        if (resultOverlay != null && gameContainer != null) {
            gameContainer.getChildren().remove(resultOverlay);
        }
    }
    
    /**
     * 결과 오버레이 생성 - 전체 화면을 덮는 반투명 배경 + 결과
     */
    private VBox createResultOverlay(GameResult result, int player1Score, int player2Score) {
        // 결과 컨텐츠
        VBox contentBox = createContentBox(result, player1Score, player2Score);
        
        // 전체 화면을 덮는 반투명 오버레이 (StackPane 사용)
        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.85);");
        overlay.getChildren().add(contentBox);
        StackPane.setAlignment(contentBox, Pos.CENTER);
        
        // 명시적으로 크기 바인딩
        overlay.prefWidthProperty().bind(gameContainer.widthProperty());
        overlay.prefHeightProperty().bind(gameContainer.heightProperty());
        
        // VBox로 감싸기
        VBox wrapper = new VBox(overlay);
        wrapper.setAlignment(Pos.CENTER);
        VBox.setVgrow(overlay, javafx.scene.layout.Priority.ALWAYS);
        
        return wrapper;
    }
    
    /**
     * 컨텐츠 박스 생성
     */
    private VBox createContentBox(GameResult result, int player1Score, int player2Score) {
        VBox box = new VBox(30);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(50));
        box.setMaxWidth(600);
        box.setMaxHeight(500);
        box.setStyle("-fx-background-color: #0a0e27; " +
                    "-fx-background-radius: 15; " +
                    "-fx-border-color: #1e3a8a; " +
                    "-fx-border-width: 3; " +
                    "-fx-border-radius: 15; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.8), 30, 0, 0, 0);");
        
        // 결과 레이블
        Label resultLabel = createResultLabel(result);
        
        // 점수 표시
        VBox scoresBox = createScoresBox(player1Score, player2Score, result);
        
        // 버튼들
        HBox buttonsBox = createButtonsBox();
        
        box.getChildren().addAll(resultLabel, scoresBox, buttonsBox);
        return box;
    }
    
    /**
     * 결과 레이블 생성
     */
    private Label createResultLabel(GameResult result) {
        String text;
        String color;
        String glowColor;
        
        switch (result) {
            case PLAYER1_WIN:
                text = "Player 1 Wins!";
                color = "#00d4ff";
                glowColor = "rgba(0,212,255,0.8)";
                break;
            case PLAYER2_WIN:
                text = "Player 2 Wins!";
                color = "#ff6b6b";
                glowColor = "rgba(255,107,107,0.8)";
                break;
            case DRAW:
                text = "Draw!";
                color = "#ffd700";
                glowColor = "rgba(255,215,0,0.8)";
                break;
            default:
                text = "Game Over";
                color = "#ffffff";
                glowColor = "rgba(255,255,255,0.8)";
        }
        
        Label label = new Label(text);
        label.setFont(Font.font("Arial", FontWeight.BOLD, 48));
        label.setStyle("-fx-text-fill: " + color + ";" +
                      "-fx-effect: dropshadow(gaussian, " + glowColor + ", 20, 0, 0, 0);");
        return label;
    }
    
    /**
     * 점수 박스 생성
     */
    private VBox createScoresBox(int player1Score, int player2Score, GameResult result) {
        VBox box = new VBox(15);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(20, 0, 20, 0));
        
        // 구분선 (상단)
        Rectangle divider1 = new Rectangle(400, 2);
        divider1.setFill(Color.web("#1e3a8a"));
        
        // Player 1 점수
        Label player1Label = new Label("Player 1: " + player1Score);
        player1Label.setFont(Font.font("Courier New", FontWeight.BOLD, 28));
        String player1Style = "-fx-text-fill: #00d4ff;";
        if (result == GameResult.PLAYER1_WIN) {
            player1Style += " -fx-effect: dropshadow(gaussian, rgba(0,212,255,0.6), 15, 0, 0, 0);";
        }
        player1Label.setStyle(player1Style);
        
        // VS 레이블
        Label vsLabel = new Label("VS");
        vsLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        vsLabel.setStyle("-fx-text-fill: #666666;");
        
        // Player 2 점수
        Label player2Label = new Label("Player 2: " + player2Score);
        player2Label.setFont(Font.font("Courier New", FontWeight.BOLD, 28));
        String player2Style = "-fx-text-fill: #ff6b6b;";
        if (result == GameResult.PLAYER2_WIN) {
            player2Style += " -fx-effect: dropshadow(gaussian, rgba(255,107,107,0.6), 15, 0, 0, 0);";
        }
        player2Label.setStyle(player2Style);
        
        // 구분선 (하단)
        Rectangle divider2 = new Rectangle(400, 2);
        divider2.setFill(Color.web("#1e3a8a"));
        
        box.getChildren().addAll(divider1, player1Label, vsLabel, player2Label, divider2);
        return box;
    }
    
    /**
     * 버튼 박스 생성
     */
    private HBox createButtonsBox() {
        HBox box = new HBox(20);
        box.setAlignment(Pos.CENTER);
        
        Button restartButton = new Button("다시 하기");
        restartButton.getStyleClass().add("versus-button");
        restartButton.setPrefWidth(180);
        restartButton.setPrefHeight(50);
        restartButton.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        restartButton.setStyle("-fx-background-color: #00d4ff; " +
                              "-fx-text-fill: #0a0e27; " +
                              "-fx-background-radius: 8; " +
                              "-fx-cursor: hand;");
        restartButton.setOnMouseEntered(e -> 
            restartButton.setStyle("-fx-background-color: #00b8e6; " +
                                  "-fx-text-fill: #0a0e27; " +
                                  "-fx-background-radius: 8; " +
                                  "-fx-cursor: hand;"));
        restartButton.setOnMouseExited(e -> 
            restartButton.setStyle("-fx-background-color: #00d4ff; " +
                                  "-fx-text-fill: #0a0e27; " +
                                  "-fx-background-radius: 8; " +
                                  "-fx-cursor: hand;"));
        restartButton.setOnAction(e -> {
            hide();
            if (onRestart != null) {
                onRestart.run();
            }
        });
        
        Button menuButton = new Button("메인 메뉴");
        menuButton.getStyleClass().add("versus-button");
        menuButton.setPrefWidth(180);
        menuButton.setPrefHeight(50);
        menuButton.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        menuButton.setStyle("-fx-background-color: #1e3a8a; " +
                           "-fx-text-fill: #ffffff; " +
                           "-fx-background-radius: 8; " +
                           "-fx-cursor: hand;");
        menuButton.setOnMouseEntered(e -> 
            menuButton.setStyle("-fx-background-color: #2d4ba8; " +
                               "-fx-text-fill: #ffffff; " +
                               "-fx-background-radius: 8; " +
                               "-fx-cursor: hand;"));
        menuButton.setOnMouseExited(e -> 
            menuButton.setStyle("-fx-background-color: #1e3a8a; " +
                               "-fx-text-fill: #ffffff; " +
                               "-fx-background-radius: 8; " +
                               "-fx-cursor: hand;"));
        menuButton.setOnAction(e -> {
            Router router = new Router(stage);
            router.showStartMenu();
        });
        
        box.getChildren().addAll(restartButton, menuButton);
        return box;
    }
    
}
