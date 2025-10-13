package com.example;

import com.example.settings.GameSettings;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) {
        // Router를 설정 크기 없이 생성 (GameSettings 값을 바로 사용)
        Router router = new Router(stage);
        router.showStartMenu();
    }

    public static void main(String[] args) {
        launch(args);
    }
}