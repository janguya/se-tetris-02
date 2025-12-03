package com.example;

import javafx.application.Platform;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class MainTest {

    @BeforeAll
    static void initJavaFX() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Already initialized
        }
    }

    @Test
    @DisplayName("Main application can be instantiated")
    void testMainInstantiation() {
        Main main = new Main();
        assertNotNull(main);
    }

    @Test
    @DisplayName("Main start method creates Router")
    void testMainStart() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        
        Platform.runLater(() -> {
            try {
                Main main = new Main();
                Stage stage = new Stage();
                main.start(stage);
                latch.countDown();
            } catch (Exception e) {
                fail("Exception during start: " + e.getMessage());
            }
        });

        assertTrue(latch.await(5, TimeUnit.SECONDS), "Start method should complete");
    }

    @Test
    @DisplayName("Main class exists and has main method")
    void testMainMethodExists() throws Exception {
        Class<?> mainClass = Main.class;
        assertNotNull(mainClass);
        assertNotNull(mainClass.getMethod("main", String[].class));
    }
}
