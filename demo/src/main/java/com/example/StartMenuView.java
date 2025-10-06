package com.example;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.util.Duration;

public class StartMenuView {


    // 메뉴 아이템 데이터 클래스
    public static class MenuItem {
        private final String id;
        private final String label;

        public MenuItem(String id, String label) {
            this.id = id;
            this.label = label;
        }

        public String getId() { return id; }
        public String getLabel() { return label; }
    }

    private final BorderPane root = new BorderPane();
    private final VBox menuBox = new VBox(15);
    private final Label titleLabel = new Label("TETRIS");
    private final Label hintLabel = new Label("↑/↓: 이동   Enter: 선택   Esc: 종료");
    
    private final List<MenuItem> menuItems = new ArrayList<>();
    private final List<Label> menuLabels = new ArrayList<>();
    private int selectedIndex = 0;
    private Consumer<String> onSelectCallback;
    
    // 빌더 패턴으로 체이닝 가능하도록
    public StartMenuView() {
        initializeUI();
        setupKeyboardInput();
    }

    // 빌더 패턴: 메뉴 아이템 추가
    public StartMenuView addMenuItem(String id, String label) {
        MenuItem item = new MenuItem(id, label);
        menuItems.add(item);
        
        Label itemLabel = new Label(label);
        itemLabel.getStyleClass().add("menu-item");
        itemLabel.setFont(Font.font("Consolas", 20));
        menuLabels.add(itemLabel);
        
        return this;
    }

    // 빌더 패턴: 선택 콜백 설정
    public StartMenuView setOnMenuSelect(Consumer<String> callback) {
        this.onSelectCallback = callback;
        return this;
    }

    // 빌더 패턴: 최종 빌드
    public StartMenuView build() {
        if (menuItems.isEmpty()) {
            throw new IllegalStateException("메뉴 아이템이 하나 이상 필요합니다.");
        }
        refreshMenuDisplay();
        playTitleAnimation();
        return this;
    }

    private void initializeUI() {
        // 타이틀 스타일
        titleLabel.getStyleClass().add("title");
        titleLabel.setFont(Font.font("Consolas", 48));
        
        // 힌트 스타일
        hintLabel.getStyleClass().add("hint");
        hintLabel.setOpacity(0.7);
        
        // 메뉴 박스 설정
        menuBox.setAlignment(Pos.CENTER);
        menuBox.setPadding(new Insets(20, 0, 0, 0));
        
        // 중앙 컨테이너
        VBox centerContainer = new VBox(30, titleLabel, menuBox);
        centerContainer.setAlignment(Pos.CENTER);
        
        // 레이아웃 구성
        root.setCenter(centerContainer);
        root.setBottom(hintLabel);
        root.setPadding(new Insets(40, 20, 20, 20));
        
        BorderPane.setAlignment(hintLabel, Pos.CENTER);
        BorderPane.setMargin(hintLabel, new Insets(0, 0, 15, 0));
        
        // 포커스 가능하도록 설정
        root.setFocusTraversable(true);
    }

    private void setupKeyboardInput() {
        root.setOnKeyPressed(event -> {
            KeyCode code = event.getCode();
            
            switch (code) {
                case UP:
                case W:
                    moveSelection(-1);
                    break;
                case DOWN:
                case S:
                    moveSelection(1);
                    break;
                case ENTER:
                case SPACE:
                    selectCurrentMenu();
                    break;
                case ESCAPE:
                    handleEscape();
                    break;
                default:
                    // 다른 키는 무시 (하단 힌트가 항상 표시됨)
                    break;
            }
            
            event.consume();
        });
    }

    private void moveSelection(int delta) {
        // 이전 선택 해제 애니메이션
        animateDeselect(menuLabels.get(selectedIndex));
        
        // 인덱스 이동 (순환)
        int size = menuItems.size();
        selectedIndex = (selectedIndex + delta + size) % size;
        
        // 새 선택 강조 애니메이션
        animateSelect(menuLabels.get(selectedIndex));
        
        refreshMenuDisplay();
    }

    private void selectCurrentMenu() {
        if (onSelectCallback != null && selectedIndex < menuItems.size()) {
            MenuItem selected = menuItems.get(selectedIndex);
            
            // 선택 애니메이션 후 콜백 실행
            playSelectAnimation(menuLabels.get(selectedIndex), () -> {
                onSelectCallback.accept(selected.getId());
            });
        }
    }

    private void handleEscape() {
        if (onSelectCallback != null) {
            onSelectCallback.accept("EXIT");
        }
    }

    private void refreshMenuDisplay() {
        menuBox.getChildren().clear();
        
        for (int i = 0; i < menuLabels.size(); i++) {
            Label label = menuLabels.get(i);
            boolean isSelected = (i == selectedIndex);
            
            // CSS 클래스로 스타일 관리
            label.getStyleClass().removeAll("selected", "unselected");
            label.getStyleClass().add(isSelected ? "selected" : "unselected");
            
            // 텍스트 앞에 선택 표시
            String prefix = isSelected ? "▶ " : "  ";
            label.setText(prefix + menuItems.get(i).getLabel());
            
            menuBox.getChildren().add(label);
        }
    }

    // 애니메이션: 타이틀 등장
    private void playTitleAnimation() {
        FadeTransition fade = new FadeTransition(Duration.millis(800), titleLabel);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);
        
        TranslateTransition slide = new TranslateTransition(Duration.millis(800), titleLabel);
        slide.setFromY(-30);
        slide.setToY(0);
        
        fade.play();
        slide.play();
    }

    // 애니메이션: 메뉴 선택
    private void animateSelect(Label label) {
        TranslateTransition transition = new TranslateTransition(Duration.millis(150), label);
        transition.setFromX(-10);
        transition.setToX(0);
        transition.play();
    }

    // 애니메이션: 메뉴 선택 해제
    private void animateDeselect(Label label) {
        TranslateTransition transition = new TranslateTransition(Duration.millis(150), label);
        transition.setFromX(0);
        transition.setToX(-10);
        transition.setToX(0);
        transition.play();
    }

    // 애니메이션: 메뉴 확정 선택
    private void playSelectAnimation(Label label, Runnable onComplete) {
        FadeTransition fade = new FadeTransition(Duration.millis(200), label);
        fade.setFromValue(1.0);
        fade.setToValue(0.3);
        fade.setCycleCount(2);
        fade.setAutoReverse(true);
        fade.setOnFinished(e -> {
            if (onComplete != null) {
                onComplete.run();
            }
        });
        fade.play();
    }

    public Parent getRoot() {
        return root;
    }

    public void requestFocus() {
        root.requestFocus();
    }
}
