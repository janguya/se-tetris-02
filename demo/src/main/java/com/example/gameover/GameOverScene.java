package com.example.gameover;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.example.Router;
import com.example.settings.GameSettings;
import com.example.settings.GameSettings.Difficulty;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.application.Platform;

public class GameOverScene {

    static {
        // 구독: 스코어 리셋 시 캐시 무효화를 요청
        try {
            com.example.gameover.ScoreManager.addResetListener(() -> {
                // Try to run on FX thread; if toolkit not initialized (e.g. during unit tests),
                // fall back to directly clearing the cache.
                try {
                    Platform.runLater(() -> LEADERBOARD = null);
                } catch (IllegalStateException ise) {
                    // Toolkit not initialized — clear directly
                    LEADERBOARD = null;
                }
            });
        } catch (Throwable t) {
            // ignore if ScoreManager not available at class load time
        }
    }

    private static final int MAX_SCORES = 10; // 상위 10개만 표시

    private static List<ScoreEntry> LEADERBOARD = null;

    // (per-score fields are stored in ScoreEntry)

    // 리더보드 초기화 (파일에서 로드)
    private static void initializeLeaderboard() {
        if (LEADERBOARD == null) {
            boolean isItem = com.example.settings.GameSettings.getInstance().isItemModeEnabled();
            LEADERBOARD = ScoreManager.loadScores(isItem);
            System.out.println("Leaderboard initialized (mode=" + (isItem ? "item" : "normal") + ") with "
                    + LEADERBOARD.size() + " entries");
        }
    }

    public static void show(Stage stage, int finalScore) {

        // 리더보드 초기화
        initializeLeaderboard();

        // top10 미만이거나 최하위보다 크면 등록
        boolean qualifies = qualifies(finalScore);

        ScoreEntry currentPlayer = null;
        if (qualifies) {
            // 애니메이션 도중 게임 종료되면 버그 발생 가능성 방지
            javafx.application.Platform.runLater(() -> {
                String name = askName(stage, finalScore);
                if (name != null) {
                    if (name.trim().isEmpty())
                        name = "Player";
                    boolean isItemMode = GameSettings.getInstance().isItemModeEnabled();
                    Difficulty diff = GameSettings.getInstance().getDifficulty();
                    ScoreEntry added = addScore(name.trim(), finalScore, isItemMode, diff);
                    // 파일에 저장 (모드별로 분리)
                    ScoreManager.saveScores(LEADERBOARD, isItemMode);
                    // 정렬 후 화면 생성, 현재 플레이어를 하이라이트
                    GameSettings settings = GameSettings.getInstance();
                    Scene scene = create(stage, LEADERBOARD, added, settings.getWindowWidth(),
                            settings.getWindowHeight());
                    stage.setScene(scene);
                    stage.show();
                } else {
                    // 취소한 경우 일반 화면으로
                    Router router = new Router(stage);
                    router.showStartMenu();
                }
            });
        } else {
            // 정렬 후 화면 생성
            Scene scene = create(stage, LEADERBOARD, currentPlayer, 400, 500);
            stage.setScene(scene);
            stage.show();
        }
    }

    // 외부에서 리더보드 캐시를 초기화(예: 파일 삭제 후 메모리상의 목록 제거)
    public static void clearLeaderboard() {
        LEADERBOARD = null;
    }

    public static Scene create(Stage stage, List<ScoreEntry> scores, ScoreEntry currentPlayer, int width, int height) {
        VBox root = new VBox(20);
        root.setAlignment(Pos.TOP_CENTER);
        root.setPadding(new Insets(20));

        Text gameOverText = new Text("게임 종료");
        gameOverText.setFont(Font.font("Arial", FontWeight.BOLD, 36));

        Label scoreBoardLabel = new Label("🏆 스코어 보드");
        scoreBoardLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));

        // load both modes
        List<ScoreEntry> normalList = ScoreManager.loadScores(false);
        List<ScoreEntry> itemList = ScoreManager.loadScores(true);

        int listHeight = Math.max((int) (height * 0.6), Math.min((26 * MAX_SCORES) + 20, height - 160));

        // 좌우 나란히 배치할 리더보드
        VBox leftPanel = buildScoreBoardPanel("일반 모드", normalList, currentPlayer, listHeight);
        VBox rightPanel = buildScoreBoardPanel("아이템 모드", itemList, currentPlayer, listHeight);

        // HBox로 좌우 분할 (1:1 비율)
        HBox leaderboardContainer = new HBox(10);
        leaderboardContainer.setAlignment(Pos.CENTER);
        HBox.setHgrow(leftPanel, javafx.scene.layout.Priority.ALWAYS);
        HBox.setHgrow(rightPanel, javafx.scene.layout.Priority.ALWAYS);
        leaderboardContainer.getChildren().addAll(leftPanel, rightPanel);
        VBox.setVgrow(leaderboardContainer, javafx.scene.layout.Priority.ALWAYS);

        HBox buttons = new HBox(14);
        buttons.setAlignment(Pos.CENTER);

        Button mainMenuButton = new Button("메인으로");
        mainMenuButton.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        mainMenuButton.setPrefSize(120, 40);
        mainMenuButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-background-radius: 5;");
        mainMenuButton.setOnAction(e -> {
            Router router = new Router(stage);
            router.showStartMenu();
        });

        Button quitButton = new Button("종료");
        quitButton.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        quitButton.setPrefSize(120, 40);
        quitButton.setStyle("-fx-background-color: #D32F2F; -fx-text-fill: white; -fx-background-radius: 5;");
        quitButton.setOnAction(e -> stage.close());

        buttons.getChildren().addAll(mainMenuButton, quitButton);

        root.getChildren().addAll(gameOverText, scoreBoardLabel, leaderboardContainer, buttons);
        Scene scene = new Scene(root, width, height);
        return scene;
    }

    // 스코어보드 패널 생성 (제목 + 리스트)
    private static VBox buildScoreBoardPanel(String title, List<ScoreEntry> scores, ScoreEntry currentPlayer,
            int listHeight) {
        VBox panel = new VBox(10);
        panel.setAlignment(Pos.TOP_CENTER);
        panel.setPadding(new Insets(10));
        panel.setStyle(
                "-fx-border-color: #cccccc; -fx-border-width: 1; -fx-border-radius: 5; -fx-background-radius: 5;");

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        titleLabel.setStyle("-fx-text-fill: #333333;");

        ListView<HBox> listView = buildScoreListView(scores, currentPlayer, listHeight);
        VBox.setVgrow(listView, javafx.scene.layout.Priority.ALWAYS);

        panel.getChildren().addAll(titleLabel, listView);
        return panel;
    }

    private static ListView<HBox> buildScoreListView(List<ScoreEntry> scores, ScoreEntry currentPlayer,
            int listHeight) {
        ListView<HBox> view = new ListView<>();
        view.setPrefHeight(listHeight);
        view.setMaxHeight(listHeight);
        VBox.setVgrow(view, javafx.scene.layout.Priority.ALWAYS);
        // set fixed cell size so empty rows match actual row height
        view.setFixedCellSize(40);

        scores.sort(Comparator.comparingInt(ScoreEntry::getScore).reversed());
        // if empty, ensure placeholder fills the view
        VBox placeholderBox = new VBox();
        placeholderBox.setAlignment(Pos.CENTER);
        Label placeholder = new Label("기록이 없습니다");
        placeholder.setStyle("-fx-text-fill: #9e9e9e; -fx-font-size: 18px; -fx-alignment: center;");
        placeholder.setWrapText(true);
        placeholderBox.getChildren().add(placeholder);
        // bind placeholder to fill entire ListView height
        placeholderBox.minHeightProperty().bind(view.heightProperty());
        placeholderBox.prefHeightProperty().bind(view.heightProperty());
        view.setPlaceholder(placeholderBox);

        for (int i = 0; i < Math.min(scores.size(), MAX_SCORES); i++) {
            ScoreEntry entry = scores.get(i);

            Label left = new Label(String.format("%2d. %s", i + 1, entry.getName()));
            left.setFont(Font.font("Arial", 16));

            Label right = new Label(String.format("%d", entry.getScore()));
            right.setFont(Font.font("Arial", FontWeight.BOLD, 16));

            HBox row = new HBox();
            HBox.setHgrow(left, javafx.scene.layout.Priority.ALWAYS);
            left.setMaxWidth(Double.MAX_VALUE);
            row.getChildren().addAll(left, right);
            row.setPadding(new Insets(8, 12, 8, 12));
            row.setSpacing(10);
            row.setStyle("-fx-border-color: transparent transparent #e0e0e0 transparent; -fx-border-width: 0 0 1 0;");

            if (entry.equals(currentPlayer)) {
                row.setStyle(row.getStyle() + " -fx-background-color: linear-gradient(to right, #fff8dc, #ffd700);");
            }

            view.getItems().add(row);
        }
        return view;
    }

    // 기존 메소드 호환성 유지
    public static Scene create(Stage stage, List<ScoreEntry> scores, ScoreEntry currentPlayer) {
        return create(stage, scores, currentPlayer, 400, 500);
    }

    // 이름 입력
    private static String askName(Stage stage, int score) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("이름 입력");
        dialog.setHeaderText("축하합니다! 새로운 기록을 세우셨습니다: " + score + "점");
        dialog.setContentText("이름을 입력하세요:");

        Optional<String> result = dialog.showAndWait();
        return result.orElse(null);
    }

    // 등록 자격
    private static boolean qualifies(int score) {
        if (LEADERBOARD.size() < MAX_SCORES) {
            return true;
        }
        int min = LEADERBOARD.stream().mapToInt(ScoreEntry::getScore).min().orElse(Integer.MIN_VALUE);
        return score > min;
    }

    // 등록
    private static ScoreEntry addScore(String name, int score, boolean isItemMode,
            Difficulty difficulty) {
        ScoreEntry entry = new ScoreEntry(name, score, isItemMode,
                difficulty == null ? Difficulty.NORMAL : difficulty);
        LEADERBOARD.add(entry);
        LEADERBOARD.sort(Comparator.comparingInt(ScoreEntry::getScore).reversed());
        // 상위 N개만 유지
        if (LEADERBOARD.size() > MAX_SCORES) {
            LEADERBOARD.remove(LEADERBOARD.size() - 1);
        }
        return entry;
    }

    // ScoreEntry 내부 클래스
    public static class ScoreEntry {
        private String name;
        private int score;
        private boolean isItemMode;
        private Difficulty difficulty;

        public ScoreEntry(String name, int score) {
            this(name, score, false, Difficulty.NORMAL);
        }

        public ScoreEntry(String name, int score, boolean isItemMode, Difficulty difficulty) {
            this.name = name;
            this.score = score;
            this.isItemMode = isItemMode;
            this.difficulty = difficulty == null ? Difficulty.NORMAL : difficulty;
        }

        public String getName() {
            return name;
        }

        public int getScore() {
            return score;
        }

        public boolean isItemMode() {
            return isItemMode;
        }

        public Difficulty getDifficulty() {
            return difficulty;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null || getClass() != obj.getClass())
                return false;
            ScoreEntry that = (ScoreEntry) obj;
            return score == that.score && Objects.equals(name, that.name)
                    && isItemMode == that.isItemMode && difficulty == that.difficulty;
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, score, isItemMode, difficulty);
        }
    }
}
