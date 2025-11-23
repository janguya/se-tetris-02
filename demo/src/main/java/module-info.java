module com.example.tetris {
    requires transitive javafx.controls;
    requires transitive javafx.fxml;
    requires java.prefs;

    opens com.example to javafx.fxml;
    opens com.example.startmenu to javafx.fxml;
    opens com.example.settings to javafx.fxml;
    opens com.example.gameover to javafx.fxml;
    opens com.example.game.component to javafx.fxml;

    requires com.google.gson;
    requires org.json;

    exports com.example;
    exports com.example.startmenu;
    exports com.example.settings;
    exports com.example.gameover;
    exports com.example.game.blocks;
    exports com.example.game.component;
    exports com.example.theme;
    exports com.example.network;
}
