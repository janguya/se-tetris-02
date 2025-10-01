package com.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.example.GameOverScene;

import java.util.List;

import java.io.IOException;

/**
 * JavaFX App
 */
public class App extends Application {

    private static Scene scene;

    @Override
    public void start(Stage stage) {
        List<GameOverScene.ScoreEntry> scores = new java.util.ArrayList<>(List.of(
            new GameOverScene.ScoreEntry("Alice", 1200),
            new GameOverScene.ScoreEntry("Bob", 950),
            new GameOverScene.ScoreEntry("You", 1000),
            new GameOverScene.ScoreEntry("Charlie", 850)
        ));

        GameOverScene.ScoreEntry current = new GameOverScene.ScoreEntry("You", 1000);

        Scene scene = GameOverScene.create(stage, scores, current);

        stage.setScene(scene);
        stage.setTitle("Tetris - Game Over");
        stage.show();
    }

    static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch();
    }

}