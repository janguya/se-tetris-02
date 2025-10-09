package com.example.gameover;

import com.example.Router; 
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.*;
import javafx.stage.Stage;
import javafx.scene.paint.Color;

import java.util.*;

public class GameOverScene {

    private static final int MAX_SCORES = 10; // 상위 10개만 표시

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

        // 메인으로 돌아가는 버튼 추가
        Button mainMenuButton = new Button("메인으로");
        mainMenuButton.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        mainMenuButton.setPrefSize(120, 40);
        mainMenuButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-background-radius: 5;");
        
        mainMenuButton.setOnAction(e -> {
            Router router = new Router(stage);
            router.showStartMenu();
        });

        root.getChildren().addAll(gameOverText, scoreBoardLabel, scoreListView, mainMenuButton);
        return new Scene(root, width, height);
    }
    
    // 기존 메소드 호환성 유지
    public static Scene create(Stage stage, List<ScoreEntry> scores, ScoreEntry currentPlayer) {
        return create(stage, scores, currentPlayer, 400, 500);
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


