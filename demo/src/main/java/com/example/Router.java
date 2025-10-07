package com.example;

import com.example.game.component.Board;
import com.example.gameover.GameOverScene;
import com.example.settings.SettingsDialog;
import com.example.startmenu.StartMenuView;

import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class Router {
    private final Stage stage;
    private Integer overrideWidth = null;
    private Integer overrideHeight = null;
    private Runnable onSettingsChanged = null;

    private static final int DEFAULT_WIDTH = 480;
    private static final int DEFAULT_HEIGHT = 640;

    public Router(Stage stage) {
        this(stage, null, null);
    }

    public Router(Stage stage, Integer width, Integer height) {
        this.stage = stage;
        this.overrideWidth = width;
        this.overrideHeight = height;
    }

    public void setSize(Integer width, Integer height) {
        this.overrideWidth = width;
        this.overrideHeight = height;
    }

    private int currentWidth() {
        if (overrideWidth != null) return overrideWidth;
        double w = stage.getWidth();
        if (w > 1) return (int) Math.round(w);
        return DEFAULT_WIDTH;
    }

    private int currentHeight() {
        if (overrideHeight != null) return overrideHeight;
        double h = stage.getHeight();
        if (h > 1) return (int) Math.round(h);
        return DEFAULT_HEIGHT;
    }

    public void route(String menuId) {
        if (menuId == null) return;
        switch (menuId) {
            case "GAME":
                showGame();
                break;
            case "SETTINGS":
                showSettings(onSettingsChanged);
                break;
            case "SCOREBOARD":
                showScoreboard();
                break;
            case "EXIT":
                exit();
                break;
            default:
                // unknown
        }
    }

    public void showGame() {
        Board gameBoard = new Board();
    Scene gameScene = new Scene(gameBoard.getRoot(), currentWidth(), currentHeight());
        try {
            gameScene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        } catch (Exception e) {
            // ignore missing stylesheet
        }
        stage.setScene(gameScene);
        stage.setResizable(false);
        stage.show();
        gameBoard.getRoot().requestFocus();
    }

    public void showSettings(Runnable onSettingsChanged) {
        SettingsDialog settingsDialog =  new SettingsDialog(stage, onSettingsChanged);
        settingsDialog.show();
    }

    public void showScoreboard() {
        List<GameOverScene.ScoreEntry> scores = new ArrayList<>();
        // TODO : 실제 점수 불러오기 아래는 더미 데이터
        scores.add(new GameOverScene.ScoreEntry("Alice", 1200));
        scores.add(new GameOverScene.ScoreEntry("Bob", 950));
        scores.add(new GameOverScene.ScoreEntry("Charlie", 850));
        scores.add(new GameOverScene.ScoreEntry("You", 1000));
        scores.add(new GameOverScene.ScoreEntry("Dave", 750));
        scores.add(new GameOverScene.ScoreEntry("Eve", 600));
        scores.add(new GameOverScene.ScoreEntry("Frank", 500));
        scores.add(new GameOverScene.ScoreEntry("Grace", 400));
        scores.add(new GameOverScene.ScoreEntry("Heidi", 300));
        scores.add(new GameOverScene.ScoreEntry("Ivan", 200));
        scores.add(new GameOverScene.ScoreEntry("Judy", 100));

        GameOverScene.ScoreEntry current = new GameOverScene.ScoreEntry("You", 1000);
        Scene scoreScene = GameOverScene.create(stage, scores, current);
        stage.setScene(scoreScene);
        stage.setResizable(false);
        stage.show();
    }

    // Create and show the start menu scene
    public void showStartMenu() {
        StartMenuView startMenu = new StartMenuView()
            .addMenuItem("GAME", "게임 시작")
            .addMenuItem("SETTINGS", "설정")
            .addMenuItem("SCOREBOARD", "스코어보드")
            .addMenuItem("EXIT", "종료")
            .setOnMenuSelect(this::route)
            .build();

    Scene scene = new Scene(startMenu.getRoot(), currentWidth(), currentHeight());
        try {
            scene.getStylesheets().add(getClass().getResource("tetris-menu.css").toExternalForm());
        } catch (Exception e) {
            // ignore
        }
        stage.setTitle("Tetris");
        stage.setScene(scene);
        stage.show();
        startMenu.requestFocus();
    }

    public void exit() {
        System.exit(0);
    }
}
