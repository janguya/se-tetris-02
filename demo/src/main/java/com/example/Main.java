package com.example;

import com.example.component.Board;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import com.example.settings.GameSettings;

public class Main extends Application {

  @Override
    public void start(Stage primaryStage) {
        // 게임 보드
        Board gameBoard = new Board();
        int windowWidth = 400;
        int windowHeight = 600;
        Scene scene = new Scene(gameBoard.getRoot(), windowWidth, windowHeight);

        // 스타일시트 적용
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        
        // 타이틀 및 씬 설정
        primaryStage.setTitle("SeoulTech SE Tetris - JavaFX");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
        
        // 게임 보드에 포커스 설정
        gameBoard.getRoot().requestFocus();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
