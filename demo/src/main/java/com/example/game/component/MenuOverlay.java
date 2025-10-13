package com.example.game.component;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

public class MenuOverlay {
    
    public enum MenuType {
        PAUSE_MENU,
        GAME_OVER_MENU,
        SETTINGS_MENU
    }
    
    private StackPane overlay;
    private VBox menuContainer;
    private Rectangle background;
    
    // 콜백 인터페이스
    public interface MenuCallback {
        void onResume();
        void onRestart();
        void onSettings();
        void onMainMenu();
        void onExit();
    }
    
    public MenuOverlay() {
        createOverlay();
    }
    
    private void createOverlay() {
        overlay = new StackPane();
        overlay.setVisible(false);
        
        // 반투명 배경
        background = new Rectangle();
        background.setFill(Color.color(0, 0, 0, 0.8));
        background.widthProperty().bind(overlay.widthProperty());
        background.heightProperty().bind(overlay.heightProperty());
        
        // 메뉴 컨테이너
        menuContainer = new VBox(15);
        menuContainer.setAlignment(Pos.CENTER);
        menuContainer.setPadding(new Insets(40));
        menuContainer.getStyleClass().add("menu-overlay");
        
        overlay.getChildren().addAll(background, menuContainer);
    }
    
    public void showPauseMenu(MenuCallback callback) {
        showMenu(MenuType.PAUSE_MENU, callback, null);
    }
    
    public void showGameOverMenu(MenuCallback callback, int finalScore) {
        showMenu(MenuType.GAME_OVER_MENU, callback, finalScore);
    }
    
    public void showSettingsMenu(MenuCallback callback) {
        showMenu(MenuType.SETTINGS_MENU, callback, null);
    }
    
    private void showMenu(MenuType menuType, MenuCallback callback, Integer finalScore) {
        menuContainer.getChildren().clear();
        
        // 타이틀
        Text title = createTitle(menuType, finalScore);
        menuContainer.getChildren().add(title);
        
        // 버튼들
        switch (menuType) {
            case PAUSE_MENU:
                addPauseMenuButtons(callback);
                break;
            case GAME_OVER_MENU:
                addGameOverMenuButtons(callback);
                break;
            case SETTINGS_MENU:
                addSettingsMenuButtons(callback);
                break;
        }
        
        overlay.setVisible(true);
        overlay.toFront();
    }
    
    private Text createTitle(MenuType menuType, Integer finalScore) {
        Text title = new Text();
        title.getStyleClass().add("menu-title");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 32));
        title.setFill(Color.WHITE);
        
        switch (menuType) {
            case PAUSE_MENU:
                title.setText("PAUSED");
                break;
            case GAME_OVER_MENU:
                title.setText("GAME OVER");
                if (finalScore != null) {
                    Text scoreText = new Text("Final Score: " + String.format("%,d", finalScore));
                    scoreText.getStyleClass().add("menu-score");
                    scoreText.setFont(Font.font("Arial", FontWeight.NORMAL, 18));
                    scoreText.setFill(Color.LIGHTGRAY);
                    
                    VBox titleContainer = new VBox(10);
                    titleContainer.setAlignment(Pos.CENTER);
                    titleContainer.getChildren().addAll(title, scoreText);
                    
                    menuContainer.getChildren().add(titleContainer);
                    return new Text(); // 빈 텍스트 반환 (이미 컨테이너에 추가됨)
                }
                break;
            case SETTINGS_MENU:
                title.setText("SETTINGS");
                break;
        }
        
        return title;
    }
    
    private void addPauseMenuButtons(MenuCallback callback) {
        Button resumeBtn = createMenuButton("Resume", () -> {
            hide();
            callback.onResume();
        });
        
        Button settingsBtn = createMenuButton("Settings", () -> {
            callback.onSettings();
        });
        
        Button restartBtn = createMenuButton("Restart", () -> {
            hide();
            callback.onRestart();
        });
        
        Button mainMenuBtn = createMenuButton("Main Menu", () -> {
            hide();
            callback.onMainMenu();
        });
        
        menuContainer.getChildren().addAll(resumeBtn, settingsBtn, restartBtn, mainMenuBtn);
    }
    
    private void addGameOverMenuButtons(MenuCallback callback) {
        Button restartBtn = createMenuButton("Play Again", () -> {
            hide();
            callback.onRestart();
        });
        
        Button mainMenuBtn = createMenuButton("Main Menu", () -> {
            hide();
            callback.onMainMenu();
        });
        
        Button exitBtn = createMenuButton("Exit", () -> {
            callback.onExit();
        });
        
        menuContainer.getChildren().addAll(restartBtn, mainMenuBtn, exitBtn);
    }
    
    private void addSettingsMenuButtons(MenuCallback callback) {
        // 설정 옵션들을 여기에 추가할 수 있습니다
        Button colorSchemeBtn = createMenuButton("Color Scheme", () -> {
            // 컬러 스킴 변경 로직
        });
        
        Button controlsBtn = createMenuButton("Controls", () -> {
            // 컨트롤 설정 로직
        });
        
        Button backBtn = createMenuButton("Back", () -> {
            hide();
            callback.onResume();
        });
        
        menuContainer.getChildren().addAll(colorSchemeBtn, controlsBtn, backBtn);
    }
    
    private Button createMenuButton(String text, Runnable action) {
        Button button = new Button(text);
        button.getStyleClass().add("menu-button");
        button.setPrefWidth(200);
        button.setPrefHeight(50);
        button.setOnAction(e -> action.run());
        
        // 호버 효과
        button.setOnMouseEntered(e -> button.setStyle("-fx-background-color: #3bb78f; -fx-text-fill: white;"));
        button.setOnMouseExited(e -> button.setStyle(""));
        
        return button;
    }
    
    public void hide() {
        overlay.setVisible(false);
    }
    
    public StackPane getOverlay() {
        return overlay;
    }
    
    public boolean isVisible() {
        return overlay.isVisible();
    }
}