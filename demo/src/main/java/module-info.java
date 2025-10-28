module com.example {
    requires java.prefs;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.base;
    
    requires org.json;
    
    exports com.example;
    exports com.example.game.blocks;
    exports com.example.game.component;
    exports com.example.gameover;
    exports com.example.settings;
    exports com.example.startmenu;
    exports com.example.theme;
    
    opens com.example to javafx.fxml;
    opens com.example.game.component to javafx.fxml;
    opens com.example.gameover to javafx.fxml;
}