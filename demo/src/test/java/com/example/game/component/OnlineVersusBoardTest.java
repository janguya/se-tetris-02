package com.example.game.component;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.network.ConnectionConfig;
import com.example.network.GameMessage;
import com.example.network.MessageType;
import com.example.network.NetworkManager;

import javafx.application.Platform;
import javafx.stage.Stage;

public class OnlineVersusBoardTest {

    private OnlineVersusBoard onlineVersusBoard;
    private NetworkManager mockNetworkManager;

    @BeforeEach
    public void setUp() throws Exception {
        Platform.runLater(() -> {
            // NetworkManager 목 객체 생성
            ConnectionConfig config = new ConnectionConfig(8080);
            mockNetworkManager = new NetworkManager(config, null, "TestPlayer");
            
            // OnlineVersusBoard 생성 (서버 모드)
            onlineVersusBoard = new OnlineVersusBoard(
                VersusGameModeDialog.VersusMode.NORMAL,
                mockNetworkManager,
                true  // isServer
            );
        });
        Thread.sleep(500);
    }

    @AfterEach
    public void tearDown() throws Exception {
        Platform.runLater(() -> {
            if (onlineVersusBoard != null) {
                onlineVersusBoard.cleanup();
            }
            if (mockNetworkManager != null) {
                mockNetworkManager.disconnect("Test cleanup");
            }
        });
        Thread.sleep(200);
    }

    @Test
    public void testOnlineVersusBoardInitialization() throws Exception {
        Platform.runLater(() -> {
            assertNotNull(onlineVersusBoard, "OnlineVersusBoard should be initialized");
            assertNotNull(onlineVersusBoard.getRoot(), "Root should not be null");
        });
        Thread.sleep(200);
    }

    @Test
    public void testRootContainerIsStackPane() throws Exception {
        Platform.runLater(() -> {
            assertInstanceOf(javafx.scene.layout.StackPane.class, onlineVersusBoard.getRoot(),
                "Root should be StackPane");
        });
        Thread.sleep(200);
    }

    @Test
    public void testServerMode() throws Exception {
        Platform.runLater(() -> {
            OnlineVersusBoard serverBoard = new OnlineVersusBoard(
                VersusGameModeDialog.VersusMode.NORMAL,
                mockNetworkManager,
                true
            );
            assertNotNull(serverBoard, "Server board should be initialized");
            serverBoard.cleanup();
        });
        Thread.sleep(300);
    }

    @Test
    public void testClientMode() throws Exception {
        Platform.runLater(() -> {
            OnlineVersusBoard clientBoard = new OnlineVersusBoard(
                VersusGameModeDialog.VersusMode.NORMAL,
                mockNetworkManager,
                false
            );
            assertNotNull(clientBoard, "Client board should be initialized");
            clientBoard.cleanup();
        });
        Thread.sleep(300);
    }

    @Test
    public void testNormalMode() throws Exception {
        Platform.runLater(() -> {
            OnlineVersusBoard normalBoard = new OnlineVersusBoard(
                VersusGameModeDialog.VersusMode.NORMAL,
                mockNetworkManager,
                true
            );
            assertNotNull(normalBoard, "Normal mode board should be initialized");
            normalBoard.cleanup();
        });
        Thread.sleep(300);
    }

    @Test
    public void testItemMode() throws Exception {
        Platform.runLater(() -> {
            OnlineVersusBoard itemBoard = new OnlineVersusBoard(
                VersusGameModeDialog.VersusMode.ITEM,
                mockNetworkManager,
                true
            );
            assertNotNull(itemBoard, "Item mode board should be initialized");
            itemBoard.cleanup();
        });
        Thread.sleep(300);
    }

    @Test
    public void testTimeLimitMode() throws Exception {
        Platform.runLater(() -> {
            OnlineVersusBoard timeLimitBoard = new OnlineVersusBoard(
                VersusGameModeDialog.VersusMode.TIME_LIMIT,
                mockNetworkManager,
                true
            );
            assertNotNull(timeLimitBoard, "Time limit mode board should be initialized");
            timeLimitBoard.cleanup();
        });
        Thread.sleep(300);
    }

    @Test
    public void testCleanup() throws Exception {
        Platform.runLater(() -> {
            assertDoesNotThrow(() -> {
                onlineVersusBoard.cleanup();
            }, "cleanup should not throw exception");
        });
        Thread.sleep(200);
    }

    @Test
    public void testMultipleCleanups() throws Exception {
        Platform.runLater(() -> {
            assertDoesNotThrow(() -> {
                onlineVersusBoard.cleanup();
                onlineVersusBoard.cleanup();
                onlineVersusBoard.cleanup();
            }, "Multiple cleanups should not throw exception");
        });
        Thread.sleep(200);
    }

    @Test
    public void testOnConnected() throws Exception {
        Platform.runLater(() -> {
            assertDoesNotThrow(() -> {
                onlineVersusBoard.onConnected("RemotePlayer");
            }, "onConnected should not throw exception");
        });
        Thread.sleep(200);
    }

    @Test
    public void testOnDisconnected() throws Exception {
        Platform.runLater(() -> {
            assertDoesNotThrow(() -> {
                onlineVersusBoard.onDisconnected("RemotePlayer", "Test disconnect");
            }, "onDisconnected should not throw exception");
        });
        Thread.sleep(200);
    }

    @Test
    public void testOnError() throws Exception {
        Platform.runLater(() -> {
            assertDoesNotThrow(() -> {
                onlineVersusBoard.onError("Test error", new Exception("Test exception"));
            }, "onError should not throw exception");
        });
        Thread.sleep(200);
    }

    @Test
    public void testOnLatencyUpdate() throws Exception {
        Platform.runLater(() -> {
            assertDoesNotThrow(() -> {
                onlineVersusBoard.onLatencyUpdate(50L);
            }, "onLatencyUpdate should not throw exception");
        });
        Thread.sleep(200);
    }

    @Test
    public void testOnMessageReceived() throws Exception {
        Platform.runLater(() -> {
            assertDoesNotThrow(() -> {
                GameMessage message = new GameMessage(MessageType.GAME_STATE, "TestPlayer");
                onlineVersusBoard.onMessageReceived(message);
            }, "onMessageReceived should not throw exception");
        });
        Thread.sleep(200);
    }

    @Test
    public void testRootFocusable() throws Exception {
        Platform.runLater(() -> {
            assertTrue(onlineVersusBoard.getRoot().isFocusTraversable(),
                "Root should be focusable");
        });
        Thread.sleep(200);
    }

    @Test
    public void testNetworkManagerIntegration() throws Exception {
        Platform.runLater(() -> {
            assertNotNull(mockNetworkManager, "NetworkManager should be initialized");
        });
        Thread.sleep(200);
    }
}
