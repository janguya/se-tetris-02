package com.example;

import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.*;
import javafx.stage.Stage;
import javafx.scene.paint.Color;

import java.util.*;

public class GameOverScene {

    public static Scene create(Stage stage, List<ScoreEntry> scores, ScoreEntry currentPlayer) {
        VBox root = new VBox(20);
        root.setAlignment(Pos.TOP_CENTER);
        root.setPadding(new Insets(40, 20, 40, 20));

        Text gameOverText = new Text("게임 종료");
        gameOverText.setFont(Font.font("Arial", FontWeight.BOLD, 36));

        Label scoreBoardLabel = new Label("🏆 스코어 보드");
        scoreBoardLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));

        ListView<HBox> scoreListView = new ListView<>();
        scoreListView.setPrefHeight(400);

        scores.sort(Comparator.comparingInt(ScoreEntry::getScore).reversed());

        for (int i = 0; i < scores.size(); i++) {
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
        return new Scene(root, 400, 600);
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
            if (obj instanceof ScoreEntry other) {
                return name.equals(other.name) && score == other.score;
            }
            return false;
        }
    }
}

