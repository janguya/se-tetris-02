package com.example;


import com.example.component.Board;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    private Stage primaryStage;

    int windowWidth = 400;
    int windowHeight = 600; // TODO : 설정에서 불러오기

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        StartMenuView startMenu = new StartMenuView()
            .addMenuItem("GAME", "게임 시작")
            .addMenuItem("SETTINGS", "설정")
            .addMenuItem("SCOREBOARD", "스코어보드")
            .addMenuItem("EXIT", "종료")
            .setOnMenuSelect(this::handleMenuSelection)
            .build();

    Scene scene = new Scene(startMenu.getRoot(), windowWidth, windowHeight);
        scene.getStylesheets().add(getClass().getResource("tetris-menu.css").toExternalForm()); //css
        
        stage.setTitle("Tetris");
        stage.setScene(scene);
        stage.show();

        startMenu.requestFocus(); // 키보드 포커스(창 클릭 안 해도 바로 키보드로 이동 가능하도록)
    }

    private void handleMenuSelection(String menuId) {
        System.out.println("Selected: " + menuId); // 콘솔에 출력

        if ("GAME".equals(menuId)) {
            // 전환: 시작 메뉴 -> 게임 보드
            Board gameBoard = new Board();
            Scene gameScene = new Scene(gameBoard.getRoot(), windowWidth, windowHeight);
            // 스타일시트(게임 전용)
            try {
                gameScene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
            } catch (Exception e) {
                // 리소스가 없으면 무시
            }
            primaryStage.setScene(gameScene);
            primaryStage.setResizable(false);
            primaryStage.show();

            // 게임 보드에 포커스 설정
            gameBoard.getRoot().requestFocus();
        }

        if("SETTINGS".equals(menuId)) {
            // TODO : 설정 다이얼로그 표시
        }

        // if ("SCOREBOARD".equals(menuId)) {
        //     ScoreboardView scoreboardView = new ScoreboardView(primaryStage);
        //     scoreboardView.show();
        // }

        if ("EXIT".equals(menuId)) {
            System.exit(0);
            return;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}