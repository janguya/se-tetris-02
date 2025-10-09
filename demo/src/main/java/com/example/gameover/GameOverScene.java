package com.example.gameover;

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

    public static Scene create(Stage stage, List<ScoreEntry> scores, ScoreEntry currentPlayer) {
        VBox root = new VBox(20);
        root.setAlignment(Pos.TOP_CENTER);
        root.setPadding(new Insets(40, 20, 40, 20));

        Text gameOverText = new Text("게임 종료");
        gameOverText.setFont(Font.font("Arial", FontWeight.BOLD, 36));

        Label scoreBoardLabel = new Label("🏆 스코어 보드");
        scoreBoardLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));

        ListView<HBox> scoreListView = new ListView<>();
        // 각 항목의 높이(16px 폰트 + 상하 패딩 10px) * 최대 항목 수(10개) + 여유 공간(20px)
        scoreListView.setPrefHeight((26 * MAX_SCORES) + 20);
        scoreListView.setMaxHeight((26 * MAX_SCORES) + 20);

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

        root.getChildren().addAll(gameOverText, scoreBoardLabel, scoreListView);
        // 전체 Scene 크기 조정
        // 게임오버 텍스트(36px) + 스코어보드 라벨(24px) + 리스트뷰 높이 + 패딩(상하 80px) + 요소간 간격(40px)
        return new Scene(root, 400, 500);
    }

    public static class ScoreEntry {
        private final String name;
        private final int score;

        public ScoreEntry(String name, int score) {
            this.name = name;
            this.score = score;
        }

        public String getName() { return name; }

        public int getScore() { return score; }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof ScoreEntry) {
                ScoreEntry other = (ScoreEntry) obj;
                return name.equals(other.name) && score == other.score;
            }
            return false;
        }
    }
}


