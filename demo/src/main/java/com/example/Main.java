package com.example;


import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    int windowWidth = 480;
    int windowHeight = 640; // TODO : 설정에서 불러오기

    @Override
    public void start(Stage stage) {
        Router router = new Router(stage);
        router.setSize(windowWidth, windowHeight);
        router.showStartMenu();
    }

    public static void main(String[] args) {
        launch(args);
    }
}