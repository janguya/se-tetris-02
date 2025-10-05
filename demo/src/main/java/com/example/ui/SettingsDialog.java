package com.example.ui;

import com.example.settings.ColorScheme;
import com.example.settings.GameSettings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;

public class SettingsDialog {
    private Stage dialog; // 다이얼로그 스테이지
    private GameSettings settings; // 게임 설정 인스턴스
    private ComboBox<ColorScheme> colorSchemeCombo; // 색상 스킴 콤보박스
    private GridPane customColorGrid; // 커스텀 색상 그리드
    private Map<String, ColorPicker> colorPickers; // 블록별 컬러 피커 맵
    private Runnable onSettingsChanged; // 설정 변경 콜백
    
    // 생성자
    public SettingsDialog(Stage parentStage, Runnable onSettingsChanged) {
        this.settings = GameSettings.getInstance();
        this.onSettingsChanged = onSettingsChanged;
        createDialog(parentStage);
    }
    
    // 다이얼로그 생성
    private void createDialog(Stage parentStage) {
        dialog = new Stage();
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initOwner(parentStage);
        dialog.setTitle("Game Settings");
        dialog.setResizable(false);
        
        // 메인 레이아웃
        VBox root = new VBox(20);
        root.setPadding(new Insets(20));
        root.getStyleClass().add("settings-dialog");
        
        // 색상 스킴 섹션
        VBox schemeSection = createColorSchemeSection();
        
        // 커스텀 색상 섹션
        VBox customSection = createCustomColorsSection();
        
        // 버튼 박스
        HBox buttonBox = createButtonBox();
        
        // 레이아웃에 섹션 추가
        root.getChildren().addAll(schemeSection, customSection, buttonBox);
        
        // 씬 설정
        Scene scene = new Scene(root, 400, 500);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        dialog.setScene(scene);
    }
    
    // 색상 스킴 섹션 생성
    private VBox createColorSchemeSection() {
        VBox section = new VBox(10);
        
        Label title = new Label("Color Scheme:");
        title.getStyleClass().add("settings-section-title");
        
        colorSchemeCombo = new ComboBox<>();
        colorSchemeCombo.getItems().addAll(ColorScheme.values());
        colorSchemeCombo.setValue(settings.getCurrentColorScheme());
        colorSchemeCombo.getStyleClass().add("settings-combo");
        
        // 콤보박스 커스텀 셀 팩토리 및 버튼 셀
        colorSchemeCombo.setCellFactory(listView -> new ListCell<ColorScheme>() {
            @Override
            protected void updateItem(ColorScheme item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getDisplayName());
                }
            }
        });
        
        // 버튼 셀도 동일하게 설정
        colorSchemeCombo.setButtonCell(new ListCell<ColorScheme>() {
            @Override
            protected void updateItem(ColorScheme item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getDisplayName());
                }
            }
        });
        
        colorSchemeCombo.setOnAction(e -> updateCustomColorVisibility());
        
        section.getChildren().addAll(title, colorSchemeCombo);
        return section;
    }
    
    // 커스텀 색상 섹션 생성
    private VBox createCustomColorsSection() {
        VBox section = new VBox(10);
        
        Label title = new Label("Custom Colors:");
        title.getStyleClass().add("settings-section-title");
        
        customColorGrid = new GridPane();
        customColorGrid.setHgap(10);
        customColorGrid.setVgap(8);
        customColorGrid.setAlignment(Pos.CENTER_LEFT);
        
        // 블록별 컬러 피커 생성
        colorPickers = new HashMap<>();
        String[] blockNames = {"I Block", "O Block", "J Block", "L Block", "S Block", "T Block", "Z Block"};
        String[] blockTypes = {"block-i", "block-o", "block-j", "block-l", "block-s", "block-t", "block-z"};
        
        for (int i = 0; i < blockNames.length; i++) {
            Label label = new Label(blockNames[i] + ":");
            label.getStyleClass().add("settings-color-label");
            
            ColorPicker picker = new ColorPicker();
            picker.setValue(settings.getCustomColor(blockTypes[i]));
            picker.getStyleClass().add("settings-color-picker");
            colorPickers.put(blockTypes[i], picker);
            
            customColorGrid.add(label, 0, i);
            customColorGrid.add(picker, 1, i);
        }
        
        section.getChildren().addAll(title, customColorGrid);
        updateCustomColorVisibility();
        return section;
    }
    
    // 버튼 박스 생성
    private HBox createButtonBox() {
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        
        Button resetButton = new Button("Reset to Default");
        resetButton.getStyleClass().add("settings-button");
        resetButton.setOnAction(e -> resetToDefault());
        
        Button cancelButton = new Button("Cancel");
        cancelButton.getStyleClass().add("settings-button");
        cancelButton.setOnAction(e -> dialog.close());
        
        Button applyButton = new Button("Apply");
        applyButton.getStyleClass().add("settings-button-primary");
        applyButton.setOnAction(e -> applySettings());
        
        buttonBox.getChildren().addAll(resetButton, cancelButton, applyButton);
        return buttonBox;
    }
    
    // 커스텀 색상 섹션 표시 여부 업데이트
    private void updateCustomColorVisibility() {
        boolean isCustom = colorSchemeCombo.getValue() == ColorScheme.CUSTOM;
        customColorGrid.setVisible(isCustom);
        customColorGrid.setManaged(isCustom);
    }
    
    // 기본값으로 리셋
    private void resetToDefault() {
        colorSchemeCombo.setValue(ColorScheme.NORMAL);
        Map<String, Color> defaultColors = ColorScheme.NORMAL.getColorMap();
        for (Map.Entry<String, ColorPicker> entry : colorPickers.entrySet()) {
            Color defaultColor = defaultColors.get(entry.getKey());
            if (defaultColor != null) {
                entry.getValue().setValue(defaultColor);
            }
        }
        updateCustomColorVisibility();
    }
    
    // 설정 적용
    private void applySettings() {
        ColorScheme selectedScheme = colorSchemeCombo.getValue();
        settings.setCurrentColorScheme(selectedScheme);
        
        if (selectedScheme == ColorScheme.CUSTOM) {
            for (Map.Entry<String, ColorPicker> entry : colorPickers.entrySet()) {
                settings.setCustomColor(entry.getKey(), entry.getValue().getValue());
            }
        }
        
        if (onSettingsChanged != null) {
            onSettingsChanged.run();
        }
        
        dialog.close();
    }
    
    public void show() {
        dialog.show();
    }
}