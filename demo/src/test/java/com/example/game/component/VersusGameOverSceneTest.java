package com.example.game.component;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.game.component.VersusGameOverScene.GameResult;

import javafx.application.Platform;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * VersusGameOverScene 테스트
 */
public class VersusGameOverSceneTest {
    
    private VersusGameOverScene gameOverScene;
    private StackPane gameContainer;
    private Stage mockStage;
    private boolean restartCalled;
    
    @BeforeAll
    public static void initToolkit() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Already initialized
        }
    }
    
    @BeforeEach
    public void setUp() {
        Platform.runLater(() -> {
            gameContainer = new StackPane();
            gameContainer.setPrefSize(800, 600);
            mockStage = new Stage();
            restartCalled = false;
            
            Runnable onRestart = () -> restartCalled = true;
            gameOverScene = new VersusGameOverScene(mockStage, gameContainer, onRestart);
        });
        
        // Wait for JavaFX thread
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    @Test
    public void testConstructor() {
        assertDoesNotThrow(() -> {
            Platform.runLater(() -> {
                StackPane container = new StackPane();
                Stage stage = new Stage();
                new VersusGameOverScene(stage, container, () -> {});
            });
        });
    }
    
    @Test
    public void testShowPlayer1Win() {
        Platform.runLater(() -> {
            int initialChildren = gameContainer.getChildren().size();
            gameOverScene.show(GameResult.PLAYER1_WIN, 1000, 500);
            
            // Overlay should be added
            assertTrue(gameContainer.getChildren().size() > initialChildren);
        });
        
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    @Test
    public void testShowPlayer2Win() {
        Platform.runLater(() -> {
            int initialChildren = gameContainer.getChildren().size();
            gameOverScene.show(GameResult.PLAYER2_WIN, 500, 1000);
            
            assertTrue(gameContainer.getChildren().size() > initialChildren);
        });
        
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    @Test
    public void testShowDraw() {
        Platform.runLater(() -> {
            int initialChildren = gameContainer.getChildren().size();
            gameOverScene.show(GameResult.DRAW, 1000, 1000);
            
            assertTrue(gameContainer.getChildren().size() > initialChildren);
        });
        
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    @Test
    public void testHide() {
        Platform.runLater(() -> {
            gameOverScene.show(GameResult.PLAYER1_WIN, 1000, 500);
            int childrenAfterShow = gameContainer.getChildren().size();
            
            gameOverScene.hide();
            
            // Overlay should be removed
            assertTrue(gameContainer.getChildren().size() < childrenAfterShow);
        });
        
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    @Test
    public void testShowWithDifferentScores() {
        Platform.runLater(() -> {
            assertDoesNotThrow(() -> {
                gameOverScene.show(GameResult.PLAYER1_WIN, 0, 0);
                gameOverScene.hide();
                
                gameOverScene.show(GameResult.PLAYER2_WIN, 999999, 888888);
                gameOverScene.hide();
                
                gameOverScene.show(GameResult.DRAW, 12345, 54321);
            });
        });
        
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    @Test
    public void testMultipleShowHideCycles() {
        Platform.runLater(() -> {
            assertDoesNotThrow(() -> {
                for (int i = 0; i < 3; i++) {
                    gameOverScene.show(GameResult.PLAYER1_WIN, 1000, 500);
                    gameOverScene.hide();
                }
            });
        });
        
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    @Test
    public void testShowDoesNotThrow() {
        assertDoesNotThrow(() -> {
            Platform.runLater(() -> {
                gameOverScene.show(GameResult.PLAYER1_WIN, 1000, 500);
            });
            Thread.sleep(100);
        });
    }
    
    @Test
    public void testHideDoesNotThrow() {
        assertDoesNotThrow(() -> {
            Platform.runLater(() -> {
                gameOverScene.show(GameResult.PLAYER1_WIN, 1000, 500);
                gameOverScene.hide();
            });
            Thread.sleep(100);
        });
    }
    
    @Test
    public void testAllGameResults() {
        Platform.runLater(() -> {
            for (GameResult result : GameResult.values()) {
                assertDoesNotThrow(() -> {
                    gameOverScene.show(result, 1000, 500);
                    gameOverScene.hide();
                });
            }
        });
        
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
