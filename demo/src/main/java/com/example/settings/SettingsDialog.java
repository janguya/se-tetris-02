package com.example.settings;

import java.util.HashMap;
import java.util.Map;

import com.example.theme.ColorScheme;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class SettingsDialog {
    private Stage dialog;
    private GameSettings settings;
    private ComboBox<ColorScheme> colorSchemeCombo;
    private ComboBox<WindowSize> windowSizeCombo; // 창 크기 콤보박스 추가
    private GridPane customColorGrid;
    private GridPane keyBindingGrid; // 키 바인딩 그리드 추가
    private Map<String, ColorPicker> colorPickers;
    private Map<String, Button> keyButtons; // 키 바인딩 버튼들
    private Runnable onSettingsChanged;
    
    public SettingsDialog(Stage parentStage, Runnable onSettingsChanged) {
        this.settings = GameSettings.getInstance();
        this.onSettingsChanged = onSettingsChanged;
        createDialog(parentStage);
    }
    
    private void createDialog(Stage parentStage) {
        dialog = new Stage();
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initOwner(parentStage);
        dialog.setTitle("Game Settings");
        dialog.setResizable(false);
        
        VBox root = new VBox(20);
        root.setPadding(new Insets(20));
        root.getStyleClass().add("settings-dialog");
        
        // 창 크기 섹션 추가
        VBox windowSizeSection = createWindowSizeSection();
        
        VBox schemeSection = createColorSchemeSection();
        VBox customSection = createCustomColorsSection();
        VBox keyBindingSection = createKeyBindingSection(); // 키 바인딩 섹션 추가
        HBox buttonBox = createButtonBox();
        
        root.getChildren().addAll(windowSizeSection, schemeSection, customSection, keyBindingSection, buttonBox);
        
        Scene scene = new Scene(root, 450, 750); // 다이얼로그 크기 증가
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        dialog.setScene(scene);
    }
    
    // 창 크기 섹션 생성
    private VBox createWindowSizeSection() {
        VBox section = new VBox(10);
        
        Label title = new Label("Window Size:");
        title.getStyleClass().add("settings-section-title");
        
        windowSizeCombo = new ComboBox<>();
        windowSizeCombo.getItems().addAll(WindowSize.values());
        windowSizeCombo.setValue(settings.getCurrentWindowSize());
        windowSizeCombo.getStyleClass().add("settings-combo");
        
        // 창 크기 변경 시 즉시 적용 (실시간 미리보기)
        windowSizeCombo.setOnAction(e -> {
            WindowSize selectedSize = windowSizeCombo.getValue();
            if (selectedSize != null) {
                settings.setCurrentWindowSize(selectedSize);
            }
        });
        
        windowSizeCombo.setCellFactory(listView -> new ListCell<WindowSize>() {
            @Override
            protected void updateItem(WindowSize item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getDisplayName() + " (" + item.getWidth() + "x" + item.getHeight() + ")");
                }
            }
        });
        
        windowSizeCombo.setButtonCell(new ListCell<WindowSize>() {
            @Override
            protected void updateItem(WindowSize item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getDisplayName() + " (" + item.getWidth() + "x" + item.getHeight() + ")");
                }
            }
        });
        
        section.getChildren().addAll(title, windowSizeCombo);
        return section;
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
    
    // 키 바인딩 섹션 생성
    private VBox createKeyBindingSection() {
        VBox section = new VBox(10);
        
        Label title = new Label("Key Bindings:");
        title.getStyleClass().add("settings-section-title");
        
        keyBindingGrid = new GridPane();
        keyBindingGrid.setHgap(10);
        keyBindingGrid.setVgap(8);
        keyBindingGrid.setAlignment(Pos.CENTER_LEFT);
        
        // 키 바인딩 버튼들 생성
        keyButtons = new HashMap<>();
        String[] actionNames = {"Move Left", "Move Right", "Move Down", "Rotate", "Pause", "Hard drop"};
        String[] actionKeys = {"MOVE_LEFT", "MOVE_RIGHT", "MOVE_DOWN", "ROTATE", "PAUSE", "HARD_DROP"};

        for (int i = 0; i < actionNames.length; i++) {
            Label label = new Label(actionNames[i] + ":");
            label.getStyleClass().add("settings-color-label");
            
            Button keyButton = new Button(settings.getKeyBinding(actionKeys[i]).toString());
            keyButton.getStyleClass().add("settings-key-button");
            keyButton.setPrefWidth(100);
            
            final String actionKey = actionKeys[i];
            keyButton.setOnAction(e -> captureKey(keyButton, actionKey));
            
            keyButtons.put(actionKeys[i], keyButton);
            
            keyBindingGrid.add(label, 0, i);
            keyBindingGrid.add(keyButton, 1, i);
        }
        
        section.getChildren().addAll(title, keyBindingGrid);
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
        windowSizeCombo.setValue(WindowSize.MEDIUM);
        colorSchemeCombo.setValue(ColorScheme.NORMAL);
        Map<String, Color> defaultColors = ColorScheme.NORMAL.getColorMap();
        for (Map.Entry<String, ColorPicker> entry : colorPickers.entrySet()) {
            Color defaultColor = defaultColors.get(entry.getKey());
            if (defaultColor != null) {
                entry.getValue().setValue(defaultColor);
            }
        }
        
        // 키 바인딩 기본값으로 리셋
        settings.resetKeyBindingsToDefault();
        updateKeyButtonLabels();
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
    
    // 키 캡처 메서드
    private void captureKey(Button button, String actionKey) {
        button.setText("Press a key...");
        button.setOnKeyPressed(event -> {
            KeyCode keyCode = event.getCode();
            // ESC는 설정 키로 예약되어 있어서 중복 방지
            if (keyCode == KeyCode.ESCAPE && !actionKey.equals("SETTINGS")) {
                button.setText(settings.getKeyBinding(actionKey).toString());
                button.setOnKeyPressed(null);
                return;
            }
            
            // 중복 키 체크
            if (isKeyAlreadyUsed(keyCode, actionKey)) {
                button.setText(settings.getKeyBinding(actionKey).toString());
                button.setOnKeyPressed(null);
                showAlert("Key Already Used", "This key is already assigned to another action.");
                return;
            }
            
            button.setText(keyCode.toString());
            settings.setKeyBinding(actionKey, keyCode);
            button.setOnKeyPressed(null);
            event.consume();
        });
        button.requestFocus();
    }
    
    // 키 중복 체크
    private boolean isKeyAlreadyUsed(KeyCode keyCode, String currentAction) {
        Map<String, KeyCode> allBindings = settings.getAllKeyBindings();
        for (Map.Entry<String, KeyCode> entry : allBindings.entrySet()) {
            if (!entry.getKey().equals(currentAction) && entry.getValue() == keyCode) {
                return true;
            }
        }
        return false;
    }
    
    // 키 버튼 라벨 업데이트
    private void updateKeyButtonLabels() {
        Map<String, KeyCode> allBindings = settings.getAllKeyBindings();
        for (Map.Entry<String, KeyCode> entry : allBindings.entrySet()) {
            Button button = keyButtons.get(entry.getKey());
            if (button != null) {
                button.setText(entry.getValue().toString());
            }
        }
    }
    
    // 알림 다이얼로그 표시
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}