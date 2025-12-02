package com.example.game.component;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.network.ConnectionConfig;
import com.example.network.GameMessage;
import com.example.network.MessageType;
import com.example.network.MessageListener;
import com.example.network.NetworkManager;

import javafx.application.Platform;
import javafx.stage.Stage;

public class OnlineVersusBoardTest {

    private OnlineVersusBoard onlineVersusBoard;
    private NetworkManager mockNetworkManager;
    private Stage mockStage;

    // 더미 MessageListener 구현
    private static class DummyMessageListener implements MessageListener {
        @Override
        public void onMessageReceived(GameMessage message) {}
        
        @Override
        public void onConnected(String peerId) {}
        
        @Override
        public void onDisconnected(String peerId, String reason) {}
        
        @Override
        public void onError(String errorMessage, Exception exception) {}
        
        @Override
        public void onLatencyUpdate(long latencyMs) {}
    }

    @BeforeAll
    public static void initToolkit() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Toolkit already initialized
        }
    }

    @BeforeEach
    public void setUp() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                // Stage 초기화
                mockStage = new Stage();
                
                // NetworkManager 목 객체 생성 - DummyMessageListener 사용
                ConnectionConfig config = new ConnectionConfig(8080);
                mockNetworkManager = new NetworkManager(config, new DummyMessageListener(), "TestPlayer");
                
                // OnlineVersusBoard 생성 (서버 모드)
                onlineVersusBoard = new OnlineVersusBoard(
                    mockStage,
                    VersusGameModeDialog.VersusMode.NORMAL,
                    mockNetworkManager,
                    true  // isServer
                );
            } finally {
                latch.countDown();
            }
        });
        latch.await(5, TimeUnit.SECONDS);
    }

    @AfterEach
    public void tearDown() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                if (onlineVersusBoard != null) {
                    onlineVersusBoard.cleanup();
                }
                if (mockNetworkManager != null) {
                    mockNetworkManager.disconnect("Test cleanup");
                }
            } finally {
                latch.countDown();
            }
        });
        latch.await(2, TimeUnit.SECONDS);
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
                mockStage,
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
                mockStage,
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
                mockStage,
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
                mockStage,
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
                mockStage,
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

    // ========== 메시지 처리 테스트 ==========
    
    @Test
    public void testOnMessageReceivedGameStart() throws Exception {
        Platform.runLater(() -> {
            // GAME_START 메시지 생성
            GameMessage message = new GameMessage(MessageType.GAME_START, "Server");
            message.put("mode", "NORMAL");
            message.put("player1Seed", 12345L);
            message.put("player2Seed", 67890L);
            
            assertDoesNotThrow(() -> {
                onlineVersusBoard.onMessageReceived(message);
            }, "GAME_START message should be handled");
        });
        Thread.sleep(300);
    }

    @Test
    public void testOnMessageReceivedPlayerReady() throws Exception {
        Platform.runLater(() -> {
            GameMessage message = new GameMessage(MessageType.PLAYER_READY, "RemotePlayer");
            
            assertDoesNotThrow(() -> {
                onlineVersusBoard.onMessageReceived(message);
            }, "PLAYER_READY message should be handled");
        });
        Thread.sleep(200);
    }

    @Test
    public void testOnMessageReceivedGameReady() throws Exception {
        Platform.runLater(() -> {
            GameMessage message = new GameMessage(MessageType.GAME_READY, "Server");
            
            assertDoesNotThrow(() -> {
                onlineVersusBoard.onMessageReceived(message);
            }, "GAME_READY message should be handled");
        });
        Thread.sleep(200);
    }

    @Test
    public void testOnMessageReceivedBlockMoveLeft() throws Exception {
        Platform.runLater(() -> {
            GameMessage message = new GameMessage(MessageType.BLOCK_MOVE, "RemotePlayer");
            message.put("direction", "left");
            
            assertDoesNotThrow(() -> {
                onlineVersusBoard.onMessageReceived(message);
            }, "BLOCK_MOVE left should be handled");
        });
        Thread.sleep(200);
    }

    @Test
    public void testOnMessageReceivedBlockMoveRight() throws Exception {
        Platform.runLater(() -> {
            GameMessage message = new GameMessage(MessageType.BLOCK_MOVE, "RemotePlayer");
            message.put("direction", "right");
            
            assertDoesNotThrow(() -> {
                onlineVersusBoard.onMessageReceived(message);
            }, "BLOCK_MOVE right should be handled");
        });
        Thread.sleep(200);
    }

    @Test
    public void testOnMessageReceivedBlockMoveDown() throws Exception {
        Platform.runLater(() -> {
            GameMessage message = new GameMessage(MessageType.BLOCK_MOVE, "RemotePlayer");
            message.put("direction", "down");
            
            assertDoesNotThrow(() -> {
                onlineVersusBoard.onMessageReceived(message);
            }, "BLOCK_MOVE down should be handled");
        });
        Thread.sleep(200);
    }

    @Test
    public void testOnMessageReceivedBlockRotate() throws Exception {
        Platform.runLater(() -> {
            GameMessage message = new GameMessage(MessageType.BLOCK_ROTATE, "RemotePlayer");
            
            assertDoesNotThrow(() -> {
                onlineVersusBoard.onMessageReceived(message);
            }, "BLOCK_ROTATE should be handled");
        });
        Thread.sleep(200);
    }

    @Test
    public void testOnMessageReceivedBlockDrop() throws Exception {
        Platform.runLater(() -> {
            GameMessage message = new GameMessage(MessageType.BLOCK_DROP, "RemotePlayer");
            
            assertDoesNotThrow(() -> {
                onlineVersusBoard.onMessageReceived(message);
            }, "BLOCK_DROP should be handled");
        });
        Thread.sleep(200);
    }

    @Test
    public void testOnMessageReceivedAttack() throws Exception {
        Platform.runLater(() -> {
            GameMessage message = new GameMessage(MessageType.ATTACK, "RemotePlayer");
            message.put("linesCleared", 2);
            message.put("attackData", "1111111111;1111111111;");
            
            assertDoesNotThrow(() -> {
                onlineVersusBoard.onMessageReceived(message);
            }, "ATTACK message should be handled");
        });
        Thread.sleep(200);
    }

    @Test
    public void testOnMessageReceivedGameOver() throws Exception {
        Platform.runLater(() -> {
            GameMessage message = new GameMessage(MessageType.GAME_OVER, "RemotePlayer");
            
            assertDoesNotThrow(() -> {
                onlineVersusBoard.onMessageReceived(message);
            }, "GAME_OVER message should be handled");
        });
        Thread.sleep(300);
    }

    // ========== 게임 상태 테스트 ==========
    
    @Test
    public void testGameModeNormal() throws Exception {
        Platform.runLater(() -> {
            OnlineVersusBoard board = new OnlineVersusBoard(
                mockStage,
                VersusGameModeDialog.VersusMode.NORMAL,
                mockNetworkManager,
                true
            );
            assertNotNull(board, "Normal mode board should be created");
            board.cleanup();
        });
        Thread.sleep(200);
    }

    @Test
    public void testGameModeItem() throws Exception {
        Platform.runLater(() -> {
            OnlineVersusBoard board = new OnlineVersusBoard(
                mockStage,
                VersusGameModeDialog.VersusMode.ITEM,
                mockNetworkManager,
                true
            );
            assertNotNull(board, "Item mode board should be created");
            board.cleanup();
        });
        Thread.sleep(200);
    }

    @Test
    public void testGameModeTimeLimit() throws Exception {
        Platform.runLater(() -> {
            OnlineVersusBoard board = new OnlineVersusBoard(
                mockStage,
                VersusGameModeDialog.VersusMode.TIME_LIMIT,
                mockNetworkManager,
                true
            );
            assertNotNull(board, "Time limit mode board should be created");
            board.cleanup();
        });
        Thread.sleep(200);
    }

    @Test
    public void testServerClientConnection() throws Exception {
        Platform.runLater(() -> {
            // 연결 시뮬레이션
            onlineVersusBoard.onConnected("RemotePlayer");
            
            assertDoesNotThrow(() -> {
                // 메시지 전송 테스트
                GameMessage msg = new GameMessage(MessageType.GAME_STATE, "TestPlayer");
                onlineVersusBoard.onMessageReceived(msg);
            });
        });
        Thread.sleep(200);
    }

    @Test
    public void testLatencyUpdate() throws Exception {
        Platform.runLater(() -> {
            assertDoesNotThrow(() -> {
                onlineVersusBoard.onLatencyUpdate(25L);
                onlineVersusBoard.onLatencyUpdate(50L);
                onlineVersusBoard.onLatencyUpdate(100L);
                onlineVersusBoard.onLatencyUpdate(150L);
            }, "Multiple latency updates should work");
        });
        Thread.sleep(200);
    }

    @Test
    public void testDisconnectionHandling() throws Exception {
        Platform.runLater(() -> {
            // 연결 후 연결 해제
            onlineVersusBoard.onConnected("RemotePlayer");
            onlineVersusBoard.onDisconnected("RemotePlayer", "Connection lost");
            
            // 재연결
            onlineVersusBoard.onConnected("RemotePlayer2");
        });
        Thread.sleep(200);
    }

    @Test
    public void testErrorHandling() throws Exception {
        Platform.runLater(() -> {
            assertDoesNotThrow(() -> {
                onlineVersusBoard.onError("Network error", new Exception("Test exception"));
                onlineVersusBoard.onError("Timeout", new Exception("Connection timeout"));
            }, "Error handling should not crash");
        });
        Thread.sleep(200);
    }

    @Test
    public void testMultipleMessageTypes() throws Exception {
        Platform.runLater(() -> {
            // 여러 메시지 타입 연속 처리
            GameMessage msg1 = new GameMessage(MessageType.PLAYER_READY, "P1");
            GameMessage msg2 = new GameMessage(MessageType.BLOCK_MOVE, "P1");
            msg2.put("direction", "left");
            GameMessage msg3 = new GameMessage(MessageType.BLOCK_ROTATE, "P1");
            
            assertDoesNotThrow(() -> {
                onlineVersusBoard.onMessageReceived(msg1);
                onlineVersusBoard.onMessageReceived(msg2);
                onlineVersusBoard.onMessageReceived(msg3);
            });
        });
        Thread.sleep(300);
    }

    @Test
    public void testGameFlowSequence() throws Exception {
        Platform.runLater(() -> {
            // 게임 시작 플로우 시뮬레이션
            onlineVersusBoard.onConnected("RemotePlayer");
            
            GameMessage startMsg = new GameMessage(MessageType.GAME_START, "Server");
            startMsg.put("mode", "NORMAL");
            startMsg.put("player1Seed", 123L);
            startMsg.put("player2Seed", 456L);
            onlineVersusBoard.onMessageReceived(startMsg);
            
            GameMessage readyMsg = new GameMessage(MessageType.PLAYER_READY, "RemotePlayer");
            onlineVersusBoard.onMessageReceived(readyMsg);
        });
        Thread.sleep(300);
    }

    @Test
    public void testAttackMessageHandling() throws Exception {
        Platform.runLater(() -> {
            // 공격 메시지 여러 번
            for (int i = 2; i <= 4; i++) {
                GameMessage attackMsg = new GameMessage(MessageType.ATTACK, "RemotePlayer");
                attackMsg.put("linesCleared", i);
                attackMsg.put("attackData", "1111111111;".repeat(i));
                
                assertDoesNotThrow(() -> {
                    onlineVersusBoard.onMessageReceived(attackMsg);
                });
            }
        });
        Thread.sleep(300);
    }

    @Test
    public void testConnectionStateTransitions() throws Exception {
        Platform.runLater(() -> {
            // 연결 → 연결 해제 → 재연결
            onlineVersusBoard.onConnected("Player1");
            onlineVersusBoard.onDisconnected("Player1", "User quit");
            onlineVersusBoard.onConnected("Player2");
            onlineVersusBoard.onDisconnected("Player2", "Network error");
        });
        Thread.sleep(300);
    }

    @Test
    public void testMultipleGameModes() throws Exception {
        Platform.runLater(() -> {
            OnlineVersusBoard board1 = new OnlineVersusBoard(
                mockStage, VersusGameModeDialog.VersusMode.NORMAL, mockNetworkManager, true);
            OnlineVersusBoard board2 = new OnlineVersusBoard(
                mockStage, VersusGameModeDialog.VersusMode.ITEM, mockNetworkManager, false);
            OnlineVersusBoard board3 = new OnlineVersusBoard(
                mockStage, VersusGameModeDialog.VersusMode.TIME_LIMIT, mockNetworkManager, true);
            
            assertNotNull(board1);
            assertNotNull(board2);
            assertNotNull(board3);
            
            board1.cleanup();
            board2.cleanup();
            board3.cleanup();
        });
        Thread.sleep(300);
    }

    // ========== UI 및 실제 메서드 실행 테스트 ==========

    @Test
    public void testInitializeUICreatesComponents() throws Exception {
        Platform.runLater(() -> {
            // getRoot()를 통해 UI가 생성되었는지 확인
            assertNotNull(onlineVersusBoard.getRoot(), "Root should be created");
            assertTrue(onlineVersusBoard.getRoot() instanceof javafx.scene.layout.StackPane,
                "Root should be StackPane");
        });
        Thread.sleep(200);
    }

    @Test
    public void testReadyButtonFunctionality() throws Exception {
        Platform.runLater(() -> {
            // 연결 후 준비 버튼이 활성화되어야 함
            onlineVersusBoard.onConnected("TestPlayer");
            
            // Ready 상태 변경 시뮬레이션
            assertDoesNotThrow(() -> {
                // onReadyButtonClick 간접 호출 (메시지를 통해)
                GameMessage readyMsg = new GameMessage(MessageType.PLAYER_READY, "TestPlayer");
                onlineVersusBoard.onMessageReceived(readyMsg);
            });
        });
        Thread.sleep(300);
    }

    @Test
    public void testGameStartWithSeeds() throws Exception {
        Platform.runLater(() -> {
            // 게임 시작 메시지 with seeds
            GameMessage startMsg = new GameMessage(MessageType.GAME_START, "Server");
            startMsg.put("mode", "NORMAL");
            startMsg.put("player1Seed", 111111L);
            startMsg.put("player2Seed", 222222L);
            
            assertDoesNotThrow(() -> {
                onlineVersusBoard.onMessageReceived(startMsg);
            }, "Game start with seeds should work");
        });
        Thread.sleep(400);
    }

    @Test
    public void testSerializeDeserializeAttackLines() throws Exception {
        Platform.runLater(() -> {
            // 공격 메시지 생성 및 처리
            GameMessage attackMsg = new GameMessage(MessageType.ATTACK, "RemotePlayer");
            attackMsg.put("linesCleared", 3);
            
            // 공격 데이터 직렬화 형식 테스트
            String serializedData = "1111111111;1010101010;1111111111;";
            attackMsg.put("attackData", serializedData);
            
            assertDoesNotThrow(() -> {
                onlineVersusBoard.onMessageReceived(attackMsg);
            }, "Attack message with serialized data should work");
        });
        Thread.sleep(300);
    }

    @Test
    public void testTogglePauseBeforeGameStart() throws Exception {
        Platform.runLater(() -> {
            // 게임 시작 전 pause 시도
            assertDoesNotThrow(() -> {
                // ESC 키 이벤트 시뮬레이션은 어렵지만, 메서드는 호출 가능
                onlineVersusBoard.getRoot().requestFocus();
            }, "Focus request should not throw");
        });
        Thread.sleep(200);
    }

    @Test
    public void testMultipleLatencyUpdates() throws Exception {
        Platform.runLater(() -> {
            // 레이턴시 변화 시뮬레이션
            for (int i = 10; i <= 200; i += 20) {
                long latency = i;
                assertDoesNotThrow(() -> {
                    onlineVersusBoard.onLatencyUpdate(latency);
                });
            }
        });
        Thread.sleep(300);
    }

    @Test
    public void testGameMessageSequence() throws Exception {
        Platform.runLater(() -> {
            // 실제 게임 진행 시퀀스 시뮬레이션
            
            // 1. 연결
            onlineVersusBoard.onConnected("Opponent");
            
            // 2. 양쪽 준비
            GameMessage localReady = new GameMessage(MessageType.PLAYER_READY, "TestPlayer");
            onlineVersusBoard.onMessageReceived(localReady);
            
            GameMessage remoteReady = new GameMessage(MessageType.PLAYER_READY, "Opponent");
            onlineVersusBoard.onMessageReceived(remoteReady);
            
            // 3. 게임 시작
            GameMessage start = new GameMessage(MessageType.GAME_START, "Server");
            start.put("mode", "NORMAL");
            start.put("player1Seed", 12345L);
            start.put("player2Seed", 67890L);
            onlineVersusBoard.onMessageReceived(start);
            
            // 4. 게임 액션들
            GameMessage move1 = new GameMessage(MessageType.BLOCK_MOVE, "Opponent");
            move1.put("direction", "left");
            onlineVersusBoard.onMessageReceived(move1);
            
            GameMessage rotate = new GameMessage(MessageType.BLOCK_ROTATE, "Opponent");
            onlineVersusBoard.onMessageReceived(rotate);
            
            GameMessage drop = new GameMessage(MessageType.BLOCK_DROP, "Opponent");
            onlineVersusBoard.onMessageReceived(drop);
        });
        Thread.sleep(500);
    }

    @Test
    public void testLargeAttack() throws Exception {
        Platform.runLater(() -> {
            // 큰 공격 (4줄)
            GameMessage attackMsg = new GameMessage(MessageType.ATTACK, "Opponent");
            attackMsg.put("linesCleared", 4);
            
            StringBuilder attackData = new StringBuilder();
            for (int i = 0; i < 4; i++) {
                attackData.append("1111111111;");
            }
            attackMsg.put("attackData", attackData.toString());
            
            assertDoesNotThrow(() -> {
                onlineVersusBoard.onMessageReceived(attackMsg);
            }, "Large attack should be handled");
        });
        Thread.sleep(300);
    }

    @Test
    public void testGameOverMessage() throws Exception {
        Platform.runLater(() -> {
            // 게임 시작
            GameMessage start = new GameMessage(MessageType.GAME_START, "Server");
            start.put("mode", "NORMAL");
            start.put("player1Seed", 111L);
            start.put("player2Seed", 222L);
            onlineVersusBoard.onMessageReceived(start);
            
            // 게임 오버
            GameMessage gameOver = new GameMessage(MessageType.GAME_OVER, "Opponent");
            assertDoesNotThrow(() -> {
                onlineVersusBoard.onMessageReceived(gameOver);
            }, "Game over message should be handled");
        });
        Thread.sleep(400);
    }

    @Test
    public void testItemModeInitialization() throws Exception {
        Platform.runLater(() -> {
            OnlineVersusBoard itemBoard = new OnlineVersusBoard(
                mockStage,
                VersusGameModeDialog.VersusMode.ITEM,
                mockNetworkManager,
                true
            );
            
            assertNotNull(itemBoard.getRoot(), "Item mode board should be initialized");
            
            // 아이템 모드 게임 시작
            GameMessage start = new GameMessage(MessageType.GAME_START, "Server");
            start.put("mode", "ITEM");
            start.put("player1Seed", 333L);
            start.put("player2Seed", 444L);
            
            assertDoesNotThrow(() -> {
                itemBoard.onMessageReceived(start);
            }, "Item mode game start should work");
            
            itemBoard.cleanup();
        });
        Thread.sleep(400);
    }

    @Test
    public void testTimeLimitModeInitialization() throws Exception {
        Platform.runLater(() -> {
            OnlineVersusBoard timeLimitBoard = new OnlineVersusBoard(
                mockStage,
                VersusGameModeDialog.VersusMode.TIME_LIMIT,
                mockNetworkManager,
                false
            );
            
            assertNotNull(timeLimitBoard.getRoot(), "Time limit board should be initialized");
            
            // 시간 제한 모드 게임 시작
            GameMessage start = new GameMessage(MessageType.GAME_START, "Server");
            start.put("mode", "TIME_LIMIT");
            start.put("player1Seed", 555L);
            start.put("player2Seed", 666L);
            
            assertDoesNotThrow(() -> {
                timeLimitBoard.onMessageReceived(start);
            }, "Time limit mode game start should work");
            
            timeLimitBoard.cleanup();
        });
        Thread.sleep(400);
    }

    @Test
    public void testMultipleErrors() throws Exception {
        Platform.runLater(() -> {
            // 여러 종류의 에러 시뮬레이션
            onlineVersusBoard.onError("Connection timeout", new java.io.IOException("Timeout"));
            onlineVersusBoard.onError("Parse error", new java.lang.IllegalArgumentException("Invalid data"));
            onlineVersusBoard.onError("Network error", new Exception("Network unreachable"));
            
            // 에러 후에도 정상 동작해야 함
            assertDoesNotThrow(() -> {
                onlineVersusBoard.onConnected("Player");
            }, "Should work after errors");
        });
        Thread.sleep(300);
    }

    @Test
    public void testReconnectionAfterDisconnect() throws Exception {
        Platform.runLater(() -> {
            // 연결 → 게임 시작 → 연결 끊김 → 재연결
            onlineVersusBoard.onConnected("Player1");
            
            GameMessage start = new GameMessage(MessageType.GAME_START, "Server");
            start.put("mode", "NORMAL");
            start.put("player1Seed", 777L);
            start.put("player2Seed", 888L);
            onlineVersusBoard.onMessageReceived(start);
            
            onlineVersusBoard.onDisconnected("Player1", "Connection lost");
            
            // 재연결
            onlineVersusBoard.onConnected("Player1");
        });
        Thread.sleep(400);
    }

    @Test
    public void testAllBlockMovements() throws Exception {
        Platform.runLater(() -> {
            // 게임 시작
            GameMessage start = new GameMessage(MessageType.GAME_START, "Server");
            start.put("mode", "NORMAL");
            start.put("player1Seed", 999L);
            start.put("player2Seed", 1111L);
            onlineVersusBoard.onMessageReceived(start);
            
            // 모든 방향 이동
            String[] directions = {"left", "right", "down"};
            for (String dir : directions) {
                GameMessage move = new GameMessage(MessageType.BLOCK_MOVE, "Opponent");
                move.put("direction", dir);
                onlineVersusBoard.onMessageReceived(move);
            }
            
            // 회전
            GameMessage rotate = new GameMessage(MessageType.BLOCK_ROTATE, "Opponent");
            onlineVersusBoard.onMessageReceived(rotate);
            
            // 하드 드롭
            GameMessage drop = new GameMessage(MessageType.BLOCK_DROP, "Opponent");
            onlineVersusBoard.onMessageReceived(drop);
        });
        Thread.sleep(400);
    }

    @Test
    public void testServerVsClientBehavior() throws Exception {
        Platform.runLater(() -> {
            // 서버 보드
            OnlineVersusBoard serverBoard = new OnlineVersusBoard(
                mockStage,
                VersusGameModeDialog.VersusMode.NORMAL,
                mockNetworkManager,
                true
            );
            
            // 클라이언트 보드
            ConnectionConfig clientConfig = new ConnectionConfig(8081);
            NetworkManager clientNetwork = new NetworkManager(clientConfig, null, "Client");
            OnlineVersusBoard clientBoard = new OnlineVersusBoard(
                mockStage,
                VersusGameModeDialog.VersusMode.NORMAL,
                clientNetwork,
                false
            );
            
            assertNotNull(serverBoard.getRoot());
            assertNotNull(clientBoard.getRoot());
            
            // 양쪽에 게임 시작
            GameMessage start = new GameMessage(MessageType.GAME_START, "Server");
            start.put("mode", "NORMAL");
            start.put("player1Seed", 1234L);
            start.put("player2Seed", 5678L);
            
            serverBoard.onMessageReceived(start);
            clientBoard.onMessageReceived(start);
            
            serverBoard.cleanup();
            clientBoard.cleanup();
            clientNetwork.disconnect("Test end");
        });
        Thread.sleep(500);
    }

    @Test
    public void testContinuousAttacks() throws Exception {
        Platform.runLater(() -> {
            // 연속 공격 시뮬레이션
            for (int i = 0; i < 5; i++) {
                GameMessage attack = new GameMessage(MessageType.ATTACK, "Opponent");
                attack.put("linesCleared", 2 + (i % 3)); // 2, 3, 4줄 교대
                attack.put("attackData", "1111111111;1111111111;");
                
                onlineVersusBoard.onMessageReceived(attack);
            }
        });
        Thread.sleep(400);
    }

    @Test
    public void testGameReadyMessage() throws Exception {
        Platform.runLater(() -> {
            // 클라이언트가 게임 준비 완료 메시지를 받을 때
            OnlineVersusBoard clientBoard = new OnlineVersusBoard(
                mockStage,
                VersusGameModeDialog.VersusMode.NORMAL,
                mockNetworkManager,
                false
            );
            
            GameMessage readyMsg = new GameMessage(MessageType.GAME_READY, "Server");
            assertDoesNotThrow(() -> {
                clientBoard.onMessageReceived(readyMsg);
            }, "GAME_READY message should be processed");
            
            clientBoard.cleanup();
        });
        Thread.sleep(300);
    }

    @Test
    public void testMixedMessageTypes() throws Exception {
        Platform.runLater(() -> {
            // 다양한 메시지 타입 랜덤 순서로 전송
            GameMessage[] messages = {
                new GameMessage(MessageType.PLAYER_READY, "P1"),
                new GameMessage(MessageType.GAME_STATE, "P1"),
                new GameMessage(MessageType.ATTACK, "P1"),
                new GameMessage(MessageType.BLOCK_ROTATE, "P1"),
                new GameMessage(MessageType.GAME_OVER, "P1")
            };
            
            // ATTACK 메시지에 데이터 추가
            messages[2].put("linesCleared", 3);
            messages[2].put("attackData", "111;111;111;");
            
            for (GameMessage msg : messages) {
                assertDoesNotThrow(() -> {
                    onlineVersusBoard.onMessageReceived(msg);
                });
            }
        });
        Thread.sleep(400);
    }

    @Test
    public void testReadyButtonServerSide() throws Exception {
        Platform.runLater(() -> {
            OnlineVersusBoard serverBoard = new OnlineVersusBoard(
                mockStage,
                VersusGameModeDialog.VersusMode.NORMAL,
                mockNetworkManager,
                true
            );
            serverBoard.onConnected("Client");
            
            GameMessage ready = new GameMessage(MessageType.PLAYER_READY, "Server");
            serverBoard.onMessageReceived(ready);
            
            serverBoard.cleanup();
        });
        Thread.sleep(300);
    }

    @Test
    public void testReadyButtonClientSide() throws Exception {
        Platform.runLater(() -> {
            OnlineVersusBoard clientBoard = new OnlineVersusBoard(
                mockStage,
                VersusGameModeDialog.VersusMode.NORMAL,
                mockNetworkManager,
                false
            );
            clientBoard.onConnected("Server");
            
            GameMessage ready = new GameMessage(MessageType.PLAYER_READY, "Client");
            clientBoard.onMessageReceived(ready);
            
            clientBoard.cleanup();
        });
        Thread.sleep(300);
    }

    @Test
    public void testGameStartBothPlayersReady() throws Exception {
        Platform.runLater(() -> {
            onlineVersusBoard.onConnected("Opponent");
            
            GameMessage localReady = new GameMessage(MessageType.PLAYER_READY, "TestPlayer");
            onlineVersusBoard.onMessageReceived(localReady);
            
            GameMessage remoteReady = new GameMessage(MessageType.PLAYER_READY, "Opponent");
            onlineVersusBoard.onMessageReceived(remoteReady);
            
            GameMessage start = new GameMessage(MessageType.GAME_START, "Server");
            start.put("mode", "NORMAL");
            start.put("player1Seed", 11111L);
            start.put("player2Seed", 22222L);
            onlineVersusBoard.onMessageReceived(start);
        });
        Thread.sleep(400);
    }

    @Test
    public void testSynchronizedBlockMovement() throws Exception {
        Platform.runLater(() -> {
            GameMessage start = new GameMessage(MessageType.GAME_START, "Server");
            start.put("mode", "NORMAL");
            start.put("player1Seed", 33333L);
            start.put("player2Seed", 44444L);
            onlineVersusBoard.onMessageReceived(start);
            
            // 동기화된 블록 이동
            for (int i = 0; i < 5; i++) {
                GameMessage move = new GameMessage(MessageType.BLOCK_MOVE, "Opponent");
                move.put("direction", "left");
                onlineVersusBoard.onMessageReceived(move);
                
                GameMessage rotate = new GameMessage(MessageType.BLOCK_ROTATE, "Opponent");
                onlineVersusBoard.onMessageReceived(rotate);
            }
        });
        Thread.sleep(500);
    }

    @Test
    public void testAttackQueueProcessing() throws Exception {
        Platform.runLater(() -> {
            // 여러 공격을 큐에 추가
            for (int i = 0; i < 3; i++) {
                GameMessage attack = new GameMessage(MessageType.ATTACK, "Opponent");
                attack.put("linesCleared", 2);
                attack.put("attackData", "1111111111;1111111111;");
                onlineVersusBoard.onMessageReceived(attack);
            }
        });
        Thread.sleep(400);
    }

    @Test
    public void testLatencyUpdateRange() throws Exception {
        Platform.runLater(() -> {
            // 다양한 레이턴시 값
            long[] latencies = {5L, 15L, 30L, 50L, 100L, 150L, 200L, 300L};
            for (long latency : latencies) {
                onlineVersusBoard.onLatencyUpdate(latency);
            }
        });
        Thread.sleep(300);
    }

    @Test
    public void testOpponentBoardSynchronization() throws Exception {
        Platform.runLater(() -> {
            GameMessage start = new GameMessage(MessageType.GAME_START, "Server");
            start.put("mode", "NORMAL");
            start.put("player1Seed", 55555L);
            start.put("player2Seed", 66666L);
            onlineVersusBoard.onMessageReceived(start);
            
            // 상대방 액션 시뮬레이션
            GameMessage[] actions = {
                createMoveMessage("left"),
                createMoveMessage("right"),
                new GameMessage(MessageType.BLOCK_ROTATE, "Opponent"),
                createMoveMessage("down"),
                new GameMessage(MessageType.BLOCK_DROP, "Opponent")
            };
            
            for (GameMessage action : actions) {
                onlineVersusBoard.onMessageReceived(action);
            }
        });
        Thread.sleep(500);
    }

    private GameMessage createMoveMessage(String direction) {
        GameMessage msg = new GameMessage(MessageType.BLOCK_MOVE, "Opponent");
        msg.put("direction", direction);
        return msg;
    }

    @Test
    public void testGameEndConditions() throws Exception {
        Platform.runLater(() -> {
            GameMessage start = new GameMessage(MessageType.GAME_START, "Server");
            start.put("mode", "NORMAL");
            start.put("player1Seed", 77777L);
            start.put("player2Seed", 88888L);
            onlineVersusBoard.onMessageReceived(start);
            
            // 게임 오버 메시지
            GameMessage gameOver = new GameMessage(MessageType.GAME_OVER, "Opponent");
            onlineVersusBoard.onMessageReceived(gameOver);
        });
        Thread.sleep(400);
    }

    @Test
    public void testItemModeGameStart() throws Exception {
        Platform.runLater(() -> {
            OnlineVersusBoard itemBoard = new OnlineVersusBoard(
                mockStage,
                VersusGameModeDialog.VersusMode.ITEM,
                mockNetworkManager,
                true
            );
            
            GameMessage start = new GameMessage(MessageType.GAME_START, "Server");
            start.put("mode", "ITEM");
            start.put("player1Seed", 99999L);
            start.put("player2Seed", 10101L);
            
            itemBoard.onMessageReceived(start);
            itemBoard.cleanup();
        });
        Thread.sleep(400);
    }

    @Test
    public void testTimeLimitModeGameStart() throws Exception {
        Platform.runLater(() -> {
            OnlineVersusBoard timeLimitBoard = new OnlineVersusBoard(
                mockStage,
                VersusGameModeDialog.VersusMode.TIME_LIMIT,
                mockNetworkManager,
                true
            );
            
            GameMessage start = new GameMessage(MessageType.GAME_START, "Server");
            start.put("mode", "TIME_LIMIT");
            start.put("player1Seed", 12121L);
            start.put("player2Seed", 13131L);
            
            timeLimitBoard.onMessageReceived(start);
            timeLimitBoard.cleanup();
        });
        Thread.sleep(400);
    }

    @Test
    public void testNetworkDisconnectionDuringGame() throws Exception {
        Platform.runLater(() -> {
            onlineVersusBoard.onConnected("Player");
            
            GameMessage start = new GameMessage(MessageType.GAME_START, "Server");
            start.put("mode", "NORMAL");
            start.put("player1Seed", 14141L);
            start.put("player2Seed", 15151L);
            onlineVersusBoard.onMessageReceived(start);
            
            // 게임 중 연결 끊김
            onlineVersusBoard.onDisconnected("Player", "Connection lost during game");
        });
        Thread.sleep(400);
    }

    @Test
    public void testMultipleAttacksInSequence() throws Exception {
        Platform.runLater(() -> {
            // 다양한 크기의 연속 공격
            for (int lines = 2; lines <= 4; lines++) {
                GameMessage attack = new GameMessage(MessageType.ATTACK, "Opponent");
                attack.put("linesCleared", lines);
                StringBuilder data = new StringBuilder();
                for (int i = 0; i < lines; i++) {
                    data.append("1111111111;");
                }
                attack.put("attackData", data.toString());
                onlineVersusBoard.onMessageReceived(attack);
            }
        });
        Thread.sleep(400);
    }

    @Test
    public void testGameStateMessage() throws Exception {
        Platform.runLater(() -> {
            GameMessage state = new GameMessage(MessageType.GAME_STATE, "Opponent");
            state.put("score", 1000);
            state.put("level", 5);
            
            assertDoesNotThrow(() -> {
                onlineVersusBoard.onMessageReceived(state);
            });
        });
        Thread.sleep(200);
    }

    @Test
    public void testCompleteGameFlow() throws Exception {
        Platform.runLater(() -> {
            // 1. 연결
            onlineVersusBoard.onConnected("Player2");
            
            // 2. 준비
            GameMessage ready1 = new GameMessage(MessageType.PLAYER_READY, "TestPlayer");
            onlineVersusBoard.onMessageReceived(ready1);
            
            GameMessage ready2 = new GameMessage(MessageType.PLAYER_READY, "Player2");
            onlineVersusBoard.onMessageReceived(ready2);
            
            // 3. 게임 시작
            GameMessage start = new GameMessage(MessageType.GAME_START, "Server");
            start.put("mode", "NORMAL");
            start.put("player1Seed", 16161L);
            start.put("player2Seed", 17171L);
            onlineVersusBoard.onMessageReceived(start);
            
            // 4. 게임 플레이
            GameMessage move = new GameMessage(MessageType.BLOCK_MOVE, "Player2");
            move.put("direction", "left");
            onlineVersusBoard.onMessageReceived(move);
            
            GameMessage rotate = new GameMessage(MessageType.BLOCK_ROTATE, "Player2");
            onlineVersusBoard.onMessageReceived(rotate);
            
            GameMessage drop = new GameMessage(MessageType.BLOCK_DROP, "Player2");
            onlineVersusBoard.onMessageReceived(drop);
            
            // 5. 공격
            GameMessage attack = new GameMessage(MessageType.ATTACK, "Player2");
            attack.put("linesCleared", 3);
            attack.put("attackData", "111;111;111;");
            onlineVersusBoard.onMessageReceived(attack);
            
            // 6. 게임 종료
            GameMessage gameOver = new GameMessage(MessageType.GAME_OVER, "Player2");
            onlineVersusBoard.onMessageReceived(gameOver);
        });
        Thread.sleep(600);
    }

    @Test
    public void testServerBroadcastsGameStart() throws Exception {
        Platform.runLater(() -> {
            OnlineVersusBoard serverBoard = new OnlineVersusBoard(
                mockStage,
                VersusGameModeDialog.VersusMode.NORMAL,
                mockNetworkManager,
                true
            );
            
            serverBoard.onConnected("Client");
            
            // 서버가 게임 시작을 브로드캐스트
            GameMessage start = new GameMessage(MessageType.GAME_START, "Server");
            start.put("mode", "NORMAL");
            start.put("player1Seed", 18181L);
            start.put("player2Seed", 19191L);
            
            assertDoesNotThrow(() -> {
                serverBoard.onMessageReceived(start);
            });
            
            serverBoard.cleanup();
        });
        Thread.sleep(400);
    }

    @Test
    public void testClientReceivesGameStart() throws Exception {
        Platform.runLater(() -> {
            OnlineVersusBoard clientBoard = new OnlineVersusBoard(
                mockStage,
                VersusGameModeDialog.VersusMode.NORMAL,
                mockNetworkManager,
                false
            );
            
            clientBoard.onConnected("Server");
            
            GameMessage ready = new GameMessage(MessageType.GAME_READY, "Server");
            clientBoard.onMessageReceived(ready);
            
            GameMessage start = new GameMessage(MessageType.GAME_START, "Server");
            start.put("mode", "NORMAL");
            start.put("player1Seed", 20202L);
            start.put("player2Seed", 21212L);
            clientBoard.onMessageReceived(start);
            
            clientBoard.cleanup();
        });
        Thread.sleep(400);
    }

    @Test
    public void testHighLatencyScenario() throws Exception {
        Platform.runLater(() -> {
            onlineVersusBoard.onLatencyUpdate(500L);
            onlineVersusBoard.onLatencyUpdate(1000L);
            onlineVersusBoard.onLatencyUpdate(750L);
            onlineVersusBoard.onLatencyUpdate(250L);
        });
        Thread.sleep(300);
    }

    @Test
    public void testLowLatencyScenario() throws Exception {
        Platform.runLater(() -> {
            onlineVersusBoard.onLatencyUpdate(5L);
            onlineVersusBoard.onLatencyUpdate(10L);
            onlineVersusBoard.onLatencyUpdate(8L);
            onlineVersusBoard.onLatencyUpdate(12L);
        });
        Thread.sleep(200);
    }

    @Test
    public void testRapidBlockMovements() throws Exception {
        Platform.runLater(() -> {
            GameMessage start = new GameMessage(MessageType.GAME_START, "Server");
            start.put("mode", "NORMAL");
            start.put("player1Seed", 22222L);
            start.put("player2Seed", 23232L);
            onlineVersusBoard.onMessageReceived(start);
            
            // 빠른 연속 이동
            for (int i = 0; i < 10; i++) {
                GameMessage move = new GameMessage(MessageType.BLOCK_MOVE, "Opponent");
                move.put("direction", i % 2 == 0 ? "left" : "right");
                onlineVersusBoard.onMessageReceived(move);
            }
        });
        Thread.sleep(400);
    }

    @Test
    public void testAllGameModesWithMessages() throws Exception {
        Platform.runLater(() -> {
            VersusGameModeDialog.VersusMode[] modes = {
                VersusGameModeDialog.VersusMode.NORMAL,
                VersusGameModeDialog.VersusMode.ITEM,
                VersusGameModeDialog.VersusMode.TIME_LIMIT
            };
            
            for (VersusGameModeDialog.VersusMode mode : modes) {
                OnlineVersusBoard board = new OnlineVersusBoard(
                    mockStage,
                    mode,
                    mockNetworkManager,
                    true
                );
                
                GameMessage start = new GameMessage(MessageType.GAME_START, "Server");
                start.put("mode", mode.toString());
                start.put("player1Seed", 24242L);
                start.put("player2Seed", 25252L);
                
                board.onMessageReceived(start);
                board.cleanup();
            }
        });
        Thread.sleep(600);
    }

    @Test
    public void testRecoveryAfterError() throws Exception {
        Platform.runLater(() -> {
            onlineVersusBoard.onError("Test error", new Exception("Test"));
            
            // 에러 후 정상 메시지 처리
            onlineVersusBoard.onConnected("Player");
            
            GameMessage msg = new GameMessage(MessageType.PLAYER_READY, "Player");
            assertDoesNotThrow(() -> {
                onlineVersusBoard.onMessageReceived(msg);
            });
        });
        Thread.sleep(300);
    }

    @Test
    public void testMultipleGameSessions() throws Exception {
        Platform.runLater(() -> {
            for (int session = 0; session < 3; session++) {
                onlineVersusBoard.onConnected("Player" + session);
                
                GameMessage start = new GameMessage(MessageType.GAME_START, "Server");
                start.put("mode", "NORMAL");
                start.put("player1Seed", (long)(26262 + session * 1000));
                start.put("player2Seed", (long)(27272 + session * 1000));
                onlineVersusBoard.onMessageReceived(start);
                
                GameMessage gameOver = new GameMessage(MessageType.GAME_OVER, "Player" + session);
                onlineVersusBoard.onMessageReceived(gameOver);
                
                onlineVersusBoard.onDisconnected("Player" + session, "Session end");
            }
        });
        Thread.sleep(600);
    }
}
