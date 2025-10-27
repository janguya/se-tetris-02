package com.example;

import com.example.game.component.Board;
import com.example.gameover.GameOverScene;
import com.example.settings.GameSettings;
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
    private Double savedX = null; // 창 위치 저장
    private Double savedY = null;

    public Router(Stage stage) {
        this.stage = stage;
    }

    public Router(Stage stage, Integer width, Integer height) {
        this.stage = stage;
        this.overrideWidth = width;
        this.overrideHeight = height;
    }

    public void setSize(Integer width, Integer height) {
        this.overrideWidth = width;
        this.overrideHeight = height;
        updateStageSize();
    }

    private int currentWidth() {
        if (overrideWidth != null) return overrideWidth;
        return GameSettings.getInstance().getWindowWidth();
    }

    private int currentHeight() {
        if (overrideHeight != null) return overrideHeight;
        return GameSettings.getInstance().getWindowHeight();
    }
    
    // 설정 변경 콜백 생성
    private Runnable createSettingsCallback() {
        return () -> {
            System.out.println("Settings changed - clearing override and updating size");
            
            // 현재 창 위치 저장 (화면이 표시되어 있다면)
            if (stage.isShowing()) {
                savedX = stage.getX();
                savedY = stage.getY();
            }
            
            // override 값 제거하여 GameSettings 값 사용
            this.overrideWidth = null;
            this.overrideHeight = null;
            
            // 크기 업데이트
            updateStageSize();
        };
    }
    
    // 스테이지 크기 업데이트 
    private void updateStageSize() {
        System.out.println("Updating stage size to: " + currentWidth() + "x" + currentHeight());
        
        if (stage.getScene() != null) {
            Scene currentScene = stage.getScene();
            Scene newScene = new Scene(currentScene.getRoot(), currentWidth(), currentHeight());
            
            // 기존 스타일시트가 있다면 복사
            newScene.getStylesheets().addAll(currentScene.getStylesheets());
            
            stage.setScene(newScene);
            stage.sizeToScene();
            
            // 저장된 위치로 복원 (있다면)
            if (savedX != null && savedY != null) {
                stage.setX(savedX);
                stage.setY(savedY);
            } else if (!stage.isShowing()) {
                stage.centerOnScreen();
            }
        }
    }

    public void route(String menuId) {
        if (menuId == null) return;
        switch (menuId) {
            case "GAME":
                showGame();
                break;
            case "SETTINGS":
                showSettings();
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
        stage.sizeToScene();
        
        // 저장된 위치가 있으면 사용, 없으면 중앙 정렬
        if (savedX != null && savedY != null) {
            stage.setX(savedX);
            stage.setY(savedY);
        } else {
            stage.centerOnScreen();
        }
        
        stage.show();
        gameBoard.getRoot().requestFocus();
    }

    // 매개변수 없는 showSettings (메뉴에서 호출)
    public void showSettings() {
        SettingsDialog settingsDialog = new SettingsDialog(stage, createSettingsCallback());
        settingsDialog.show();
    }

    // 매개변수 있는 showSettings (Board.java에서 호출)
    public void showSettings(Runnable additionalCallback) {
        // 기본 콜백과 추가 콜백을 조합
        Runnable combinedCallback = () -> {
            createSettingsCallback().run(); // 기본 크기 변경 콜백
            if (additionalCallback != null) {
                additionalCallback.run(); // 추가 콜백 (Board의 onSettingsChanged)
            }
        };
        
        SettingsDialog settingsDialog = new SettingsDialog(stage, combinedCallback);
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
        Scene scoreScene = GameOverScene.create(stage, scores, current, currentWidth(), currentHeight());
        stage.setScene(scoreScene);
        stage.setResizable(false);
        stage.sizeToScene();
        
        // 저장된 위치가 있으면 사용, 없으면 중앙 정렬
        if (savedX != null && savedY != null) {
            stage.setX(savedX);
            stage.setY(savedY);
        } else {
            stage.centerOnScreen();
        }
        
        stage.show();
    }

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
            scene.getStylesheets().add(getClass().getResource("/tetris-menu.css").toExternalForm());
        } catch (Exception e) {
            // ignore
        }
        stage.setTitle("Tetris");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.sizeToScene();
        
        // 저장된 위치가 있으면 사용, 없으면 중앙 정렬
        if (savedX != null && savedY != null) {
            stage.setX(savedX);
            stage.setY(savedY);
        } else {
            stage.centerOnScreen();
        }
        
        stage.show();
        startMenu.requestFocus();
    }

    public void exit() {
        System.exit(0);
    }
}
