package com.example.settings;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.example.gameover.ScoreManager;

import com.example.theme.ColorScheme;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ScrollPane;
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
    private ComboBox<GameSettings.Difficulty> difficultyCombo; // 난이도 콤보박스
    private GridPane customColorGrid;
    private GridPane keyBindingGrid; // 키 바인딩 그리드 추가
    private Map<String, ColorPicker> colorPickers;
    private Map<String, Button> keyButtons; // 키 바인딩 버튼들
    private Map<String, KeyCode> pendingKeyBindings; // Apply 버튼 누를 때까지 임시 저장
    private Runnable onSettingsChanged;

    public SettingsDialog(Stage parentStage, Runnable onSettingsChanged) {
        this.settings = GameSettings.getInstance();
        this.onSettingsChanged = onSettingsChanged;
        this.pendingKeyBindings = new HashMap<>();
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
        // 난이도 섹션 추가
        VBox difficultySection = createDifficultySection();

        VBox schemeSection = createColorSchemeSection();
        VBox customSection = createCustomColorsSection();
        VBox keyBindingSection = createKeyBindingSection(); // 키 바인딩 섹션 추가
        VBox scoreSection = createScoreSection();
        HBox buttonBox = createButtonBox();

        // 컨텐츠를 담을 VBox (버튼은 제외)
        VBox contentBox = new VBox(20);
        contentBox.getChildren().addAll(windowSizeSection, difficultySection, schemeSection, customSection,
                keyBindingSection, scoreSection);

        // ScrollPane 추가
        ScrollPane scrollPane = new ScrollPane(contentBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER); // 가로 스크롤바 숨김
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED); // 세로 스크롤바는 필요시 표시
        scrollPane.getStyleClass().add("settings-scroll-pane");
        scrollPane.setPrefViewportHeight(600); // 스크롤 영역 높이 설정

        root.getChildren().addAll(scrollPane, buttonBox);

        Scene scene = new Scene(root, 450, 750); // 다이얼로그 크기
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        dialog.setScene(scene);
    }

    // 난이도 섹션 생성
    private VBox createDifficultySection() {
        VBox section = new VBox(10);

        Label title = new Label("Difficulty:");
        title.getStyleClass().add("settings-section-title");

        difficultyCombo = new ComboBox<>();
        difficultyCombo.getItems().addAll(GameSettings.Difficulty.values());
        difficultyCombo.setValue(settings.getDifficulty());
        difficultyCombo.getStyleClass().add("settings-combo");

        // 표시 텍스트를 간단히 처리
        difficultyCombo.setCellFactory(listView -> new ListCell<GameSettings.Difficulty>() {
            @Override
            protected void updateItem(GameSettings.Difficulty item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.name());
                }
            }
        });

        difficultyCombo.setButtonCell(new ListCell<GameSettings.Difficulty>() {
            @Override
            protected void updateItem(GameSettings.Difficulty item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.name());
                }
            }
        });

        section.getChildren().addAll(title, difficultyCombo);
        return section;
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
        String[] blockNames = { "I Block", "O Block", "J Block", "L Block", "S Block", "T Block", "Z Block" };
        String[] blockTypes = { "block-i", "block-o", "block-j", "block-l", "block-s", "block-t", "block-z" };

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
        String[] actionNames = { "Move Left", "Move Right", "Move Down", "Rotate", "Pause", "Hard drop" };
        String[] actionKeys = { "MOVE_LEFT", "MOVE_RIGHT", "MOVE_DOWN", "ROTATE", "PAUSE", "HARD_DROP" };

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
        cancelButton.setOnAction(e -> {
            // Cancel 버튼 누르면 대기 중인 키 바인딩 변경사항 취소
            pendingKeyBindings.clear();
            dialog.close();
        });

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
        if (difficultyCombo != null) {
            difficultyCombo.setValue(GameSettings.Difficulty.NORMAL);
        }
        colorSchemeCombo.setValue(ColorScheme.NORMAL);
        Map<String, Color> defaultColors = ColorScheme.NORMAL.getColorMap();
        for (Map.Entry<String, ColorPicker> entry : colorPickers.entrySet()) {
            Color defaultColor = defaultColors.get(entry.getKey());
            if (defaultColor != null) {
                entry.getValue().setValue(defaultColor);
            }
        }

        // 키 바인딩 기본값으로 리셋 (실제 설정에는 반영하지 않고 버튼만 변경)
        pendingKeyBindings.clear();
        Map<String, KeyCode> defaultKeyBindings = new HashMap<>();
        defaultKeyBindings.put("MOVE_LEFT", KeyCode.LEFT);
        defaultKeyBindings.put("MOVE_RIGHT", KeyCode.RIGHT);
        defaultKeyBindings.put("MOVE_DOWN", KeyCode.DOWN);
        defaultKeyBindings.put("ROTATE", KeyCode.UP);
        defaultKeyBindings.put("HARD_DROP", KeyCode.SPACE);
        defaultKeyBindings.put("PAUSE", KeyCode.ESCAPE);

        for (Map.Entry<String, KeyCode> entry : defaultKeyBindings.entrySet()) {
            Button button = keyButtons.get(entry.getKey());
            if (button != null) {
                button.setText(entry.getValue().toString());
            }
            pendingKeyBindings.put(entry.getKey(), entry.getValue());
        }

        updateCustomColorVisibility();
    }

    // 설정 적용
    private void applySettings() {
        // 난이도 적용
        if (difficultyCombo != null) {
            GameSettings.Difficulty sel = difficultyCombo.getValue();
            if (sel != null) {
                settings.setDifficulty(sel);
            }
        }
        ColorScheme selectedScheme = colorSchemeCombo.getValue();
        settings.setCurrentColorScheme(selectedScheme);

        if (selectedScheme == ColorScheme.CUSTOM) {
            for (Map.Entry<String, ColorPicker> entry : colorPickers.entrySet()) {
                settings.setCustomColor(entry.getKey(), entry.getValue().getValue());
            }
        }

        // 키 바인딩 적용 (pendingKeyBindings에 저장된 값들을 실제로 저장)
        for (Map.Entry<String, KeyCode> entry : pendingKeyBindings.entrySet()) {
            settings.setKeyBinding(entry.getKey(), entry.getValue());
        }

        // 먼저 다이얼로그를 닫아 UI 리소스(루트 노드 등)가 해제되도록 합니다.
        dialog.close();

        // 설정 변경 콜백은 현재 FX 이벤트 처리 이후에 안전하게 실행되도록 스케줄합니다.
        if (onSettingsChanged != null) {
            javafx.application.Platform.runLater(onSettingsChanged);
        }
    }

    public void show() {
        // 다이얼로그를 열 때마다 pendingKeyBindings 초기화
        pendingKeyBindings.clear();
        // 현재 설정값으로 버튼 텍스트 업데이트
        updateKeyButtonLabels();
        dialog.show();
    }

    // 키 캡처 메서드
    private void captureKey(Button button, String actionKey) {
        String originalText = button.getText();
        button.setText("Press a key...");

        // 버튼의 기본 동작 비활성화
        button.setOnAction(null);

        // 필터를 저장할 배열 (람다에서 참조하기 위해)
        @SuppressWarnings("unchecked")
        final javafx.event.EventHandler<javafx.scene.input.KeyEvent>[] filters = new javafx.event.EventHandler[2];

        // KEY_PRESSED 필터: 실제 키 캡처 처리
        javafx.event.EventHandler<javafx.scene.input.KeyEvent> keyPressedFilter = event -> {
            KeyCode keyCode = event.getCode();

            // 필터 제거
            button.removeEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED, filters[0]);
            button.removeEventFilter(javafx.scene.input.KeyEvent.KEY_RELEASED, filters[1]);

            // ESC는 취소
            if (keyCode == KeyCode.ESCAPE) {
                button.setText(originalText);
                button.setOnAction(e -> captureKey(button, actionKey));
                event.consume();
                return;
            }

            // 중복 키 체크
            if (isKeyAlreadyUsedInPending(keyCode, actionKey)) {
                button.setText(originalText);
                button.setOnAction(e -> captureKey(button, actionKey));
                showAlert("Key Already Used", "This key is already assigned to another action.");
                event.consume();
                return;
            }

            // 임시 저장
            button.setText(keyCode.toString());
            pendingKeyBindings.put(actionKey, keyCode);
            button.setOnAction(e -> captureKey(button, actionKey));
            event.consume();
        };

        // KEY_RELEASED 필터: 모든 릴리즈 이벤트 소비 (스페이스바 버튼 클릭 방지)
        javafx.event.EventHandler<javafx.scene.input.KeyEvent> keyReleasedFilter = event -> {
            event.consume();
        };

        // 필터 배열에 저장
        filters[0] = keyPressedFilter;
        filters[1] = keyReleasedFilter;

        // 필터 등록 (이벤트 캡처 단계에서 가로채기)
        button.addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED, keyPressedFilter);
        button.addEventFilter(javafx.scene.input.KeyEvent.KEY_RELEASED, keyReleasedFilter);

        button.requestFocus();
    }

    // 키 중복 체크 (현재 설정 + 대기 중인 변경사항 확인)
    private boolean isKeyAlreadyUsedInPending(KeyCode keyCode, String currentAction) {
        // 1. 이미 대기 중인 키 바인딩 확인
        for (Map.Entry<String, KeyCode> entry : pendingKeyBindings.entrySet()) {
            if (!entry.getKey().equals(currentAction) && entry.getValue() == keyCode) {
                return true;
            }
        }

        // 2. 현재 저장된 키 바인딩 확인 (단, 대기 중인 변경사항으로 덮어쓰이지 않는 것만)
        Map<String, KeyCode> allBindings = settings.getAllKeyBindings();
        for (Map.Entry<String, KeyCode> entry : allBindings.entrySet()) {
            if (!entry.getKey().equals(currentAction)
                    && !pendingKeyBindings.containsKey(entry.getKey())
                    && entry.getValue() == keyCode) {
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

    // 점수 관리 섹션 생성
    private VBox createScoreSection() {
        VBox section = new VBox(10);

        Label title = new Label("Score Management:");
        title.getStyleClass().add("settings-section-title");

        Button resetScoresButton = new Button("Reset All Scores");
        resetScoresButton.getStyleClass().add("settings-button");
        resetScoresButton.setOnAction(e -> resetScores());

        Label warningLabel = new Label("⚠ This will delete all saved scores permanently!");
        warningLabel.setStyle("-fx-text-fill: #ff6b6b; -fx-font-size: 11px;");

        section.getChildren().addAll(title, resetScoresButton, warningLabel);
        return section;
    }

    // 점수 초기화 메서드
    private void resetScores() {
        // 확인 다이얼로그
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Reset Scores");
        confirmAlert.setHeaderText("Are you sure?");
        confirmAlert.setContentText("점수가 영구적으로 제거됩니다\n되돌릴 수 없습니다!");

        ButtonType yesButton = new ButtonType("초기화");
        ButtonType noButton = new ButtonType("취소", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirmAlert.getButtonTypes().setAll(yesButton, noButton);

        Optional<ButtonType> result = confirmAlert.showAndWait();

        if (result.isPresent() && result.get() == yesButton) {
            // 점수 초기화 실행
            boolean success = ScoreManager.resetScores();

            if (success) {
                // 성공 알림
                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Success");
                successAlert.setHeaderText(null);
                successAlert.setContentText("점수가 성공적으로 초기화 됐습니다!");
                successAlert.showAndWait();
            } else {
                // 실패 알림
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Error");
                errorAlert.setHeaderText("Failed to reset scores");
                errorAlert.setContentText("점수 초기화에 실패했습니다. 다시 시도해주세요.");
                errorAlert.showAndWait();
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