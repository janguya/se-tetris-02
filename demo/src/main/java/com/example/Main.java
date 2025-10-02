package com.example;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) {
        StartMenuView startMenu = new StartMenuView()
            .addMenuItem("GAME", "게임 시작")
            .addMenuItem("SETTINGS", "설정")
            .addMenuItem("SCOREBOARD", "스코어보드")
            .addMenuItem("EXIT", "종료")
            .setOnMenuSelect(this::handleMenuSelection)
            .build();

        Scene scene = new Scene(startMenu.getRoot(), 600, 400);
        scene.getStylesheets().add(getClass().getResource("tetris-menu.css").toExternalForm()); //css
        
        stage.setTitle("Tetris");
        stage.setScene(scene);
        stage.show();

        startMenu.requestFocus(); //키보드 포커스(창 클릭 안 해도 바로 키보드로 이동 가능하도록)
    }

    private void handleMenuSelection(String menuId) {
        System.out.println("Selected: " + menuId); //현재는 그냥 콘솔에 출력
        //여기에서 나중에 각 메뉴에 따른 동작을 구현합니다
        if ("EXIT".equals(menuId)) {
            System.exit(0);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
