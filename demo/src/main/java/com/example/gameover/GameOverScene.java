package com.example.gameover;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.example.Router;

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

public class GameOverScene {

    private static final int MAX_SCORES = 10; // 상위 10개만 표시

    private static final List<ScoreEntry> LEADERBOARD = new ArrayList<>();

    public static void show(Stage stage, int finalScore) {
        // top10 미만이거나 최하위보다 크면 등록
        boolean qualifies = qualifies(finalScore);

        ScoreEntry currentPlayer = null;
        if (qualifies) {
            String name = askName(stage, finalScore);
            if(name == null){
                currentPlayer = null; // 취소 눌렀을 때
            }else{
                if(name.trim().isEmpty()) {
                    name = "Player"; // 빈 이름 방지
                }
            currentPlayer = addScore(name.trim(), finalScore); // 보드에 추가하고 참조 반환
            }
        }

        //정렬 후 화면 생성
        Scene scene = create(stage, LEADERBOARD, currentPlayer, 400, 500);
        stage.setScene(scene);
        stage.show();
    }

    public static Scene create(Stage stage, List<ScoreEntry> scores, ScoreEntry currentPlayer, int width, int height) {
        VBox root = new VBox(20);
        root.setAlignment(Pos.TOP_CENTER);
        root.setPadding(new Insets(40, 20, 40, 20));

        Text gameOverText = new Text("게임 종료");
        gameOverText.setFont(Font.font("Arial", FontWeight.BOLD, 36));

        Label scoreBoardLabel = new Label("🏆 스코어 보드");
        scoreBoardLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));

        ListView<HBox> scoreListView = new ListView<>();
        // 화면 크기에 비례한 리스트 높이 계산
        int listHeight = Math.min((26 * MAX_SCORES) + 20, height - 200);
        scoreListView.setPrefHeight(listHeight);
        scoreListView.setMaxHeight(listHeight);

        scores.sort(Comparator.comparingInt(ScoreEntry::getScore).reversed());

        for (int i = 0; i < Math.min(scores.size(), MAX_SCORES); i++) {
            ScoreEntry entry = scores.get(i);
            String text = String.format("%2d. %s - %d점", i + 1, entry.getName(), entry.getScore());

            Label label = new Label(text);
            label.setFont(Font.font("Arial", 16));

            HBox row = new HBox(label);
            row.setPadding(new Insets(5));

            if (entry.equals(currentPlayer)) {
                row.setStyle("-fx-background-color: #ffd700;");
                label.setFont(Font.font("Arial", FontWeight.BOLD, 16));
            }

            scoreListView.getItems().add(row);
        }
        HBox buttons = new HBox(12);
        buttons.setAlignment(Pos.CENTER);

        // 메인으로 돌아가는 버튼 추가
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

        root.getChildren().addAll(gameOverText, scoreBoardLabel, scoreListView, buttons);
        return new Scene(root, width, height);
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

    //등록 자격
    private static boolean qualifies(int score) {
        if (LEADERBOARD.size() < MAX_SCORES) {
            return true;
        }
        int min = LEADERBOARD.stream().mapToInt(ScoreEntry::getScore).min().orElse(Integer.MIN_VALUE);
        return score > min;
    }

    // 등록
    private static ScoreEntry addScore(String name, int score) {
        ScoreEntry entry = new ScoreEntry(name, score);
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

        public ScoreEntry(String name, int score) {
            this.name = name;
            this.score = score;
        }

        public String getName() {
            return name;
        }

        public int getScore() {
            return score;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            ScoreEntry that = (ScoreEntry) obj;
            return score == that.score && Objects.equals(name, that.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, score);
        }
    }
}


