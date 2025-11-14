package com.example.game.component;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class VersusGameModeDialog {
    
    public enum VersusMode {
        NORMAL("일반 모드", "2명의 플레이어가 각자 테트리스를 진행하고,\n삭제한 줄이 상대방에게 전송됩니다."),
        ITEM("아이템 모드", "대전 모드에 아이템이 추가된 모드입니다."),
        TIME_LIMIT("시간제한 모드", "정해진 시간 내에 더 높은 점수를 얻는 모드입니다.");
        
        private final String displayName;
        private final String description;
        
        VersusMode(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
    }
    
    public interface ModeSelectionCallback {
        void onModeSelected(VersusMode mode);
        void onCancel();
    }
    
    public static void show(Stage parentStage, ModeSelectionCallback callback) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(parentStage);
        dialog.setTitle("대전 모드 선택");
        dialog.setResizable(false);
        
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(30));
        root.getStyleClass().add("dialog-root");
        
        Label title = new Label("대전 모드를 선택하세요");
        title.getStyleClass().add("dialog-title");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        VBox modeButtons = new VBox(15);
        modeButtons.setAlignment(Pos.CENTER);
        
        // 각 모드별 버튼 생성
        for (VersusMode mode : VersusMode.values()) {
            VBox modeContainer = new VBox(5);
            modeContainer.setAlignment(Pos.CENTER);
            
            Button modeButton = new Button(mode.getDisplayName());
            modeButton.getStyleClass().add("mode-button");
            modeButton.setPrefWidth(200);
            modeButton.setPrefHeight(50);
            modeButton.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
            modeButton.setOnAction(e -> {
                callback.onModeSelected(mode);
                dialog.close();
            });
            
            Label description = new Label(mode.getDescription());
            description.getStyleClass().add("mode-description");
            description.setWrapText(true);
            description.setMaxWidth(300);
            description.setStyle("-fx-text-fill: gray; -fx-font-size: 11px;");
            
            modeContainer.getChildren().addAll(modeButton, description);
            modeButtons.getChildren().add(modeContainer);
        }
        
        Button cancelButton = new Button("취소");
        cancelButton.getStyleClass().add("cancel-button");
        cancelButton.setPrefWidth(100);
        cancelButton.setOnAction(e -> {
            callback.onCancel();
            dialog.close();
        });
        
        root.getChildren().addAll(title, modeButtons, cancelButton);
        
        Scene scene = new Scene(root, 400, 500);
        try {
            scene.getStylesheets().add(VersusGameModeDialog.class.getResource("/styles.css").toExternalForm());
        } catch (Exception e) {
            // ignore missing stylesheet
        }
        
        dialog.setScene(scene);
        dialog.centerOnScreen();
        dialog.show();
    }
}
