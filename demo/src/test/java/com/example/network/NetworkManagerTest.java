package com.example.network;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * NetworkManager 테스트 - NetworkTestApp 및 SimpleNetworkTest 기능 포함
 */
public class NetworkManagerTest {

    private NetworkManager networkManager;
    private NetworkManager clientManager;
    private ConnectionConfig config;
    private TestMessageListener testListener;

    @BeforeEach
    public void setUp() {
        config = ConnectionConfig.createLocalNetworkConfig();
        testListener = new TestMessageListener();
    }

    @AfterEach
    public void tearDown() {
        if (networkManager != null) {
            try {
                networkManager.shutdown();
            } catch (Exception e) {
                // Ignore
            }
        }
        if (clientManager != null) {
            try {
                clientManager.shutdown();
            } catch (Exception e) {
                // Ignore
            }
        }
    }
    
    // ============== 기존 테스트 ==============

    @Test
    @DisplayName("NetworkManager 생성 테스트")
    public void testNetworkManagerCreation() {
        networkManager = new NetworkManager(config, testListener, "Player1");
        assertNotNull(networkManager);
        assertFalse(networkManager.isConnected());
    }

    @Test
    @DisplayName("null config로 생성 시 예외 발생")
    public void testNullConfig() {
        assertThrows(IllegalArgumentException.class, () -> {
            new NetworkManager(null, testListener, "Player1");
        });
    }

    @Test
    @DisplayName("null listener로 생성 시 예외 발생")
    public void testNullListener() {
        assertThrows(IllegalArgumentException.class, () -> {
            new NetworkManager(config, null, "Player1");
        });
    }

    @Test
    @DisplayName("null playerId로 생성 시 예외 발생")
    public void testNullPlayerId() {
        assertThrows(IllegalArgumentException.class, () -> {
            new NetworkManager(config, testListener, null);
        });
    }

    @Test
    @DisplayName("빈 playerId로 생성 시 예외 발생")
    public void testEmptyPlayerId() {
        assertThrows(IllegalArgumentException.class, () -> {
            new NetworkManager(config, testListener, "");
        });
    }

    @Test
    @DisplayName("로컬 IP 주소 가져오기")
    public void testGetLocalIPAddress() {
        String ip = NetworkManager.getLocalIPAddress();
        assertNotNull(ip);
        assertFalse(ip.isEmpty());
        // IP 주소 형식 검증
        assertTrue(ip.matches("\\d+\\.\\d+\\.\\d+\\.\\d+") || ip.equals("127.0.0.1"));
    }

    @Test
    @DisplayName("서버 시작 및 종료")
    public void testServerStartAndShutdown() throws Exception {
        networkManager = new NetworkManager(config, testListener, "Server");
        assertDoesNotThrow(() -> networkManager.startServer());
        assertDoesNotThrow(() -> networkManager.shutdown());
    }

    @Test
    @DisplayName("초기 연결 상태는 false")
    public void testInitialConnectionState() {
        networkManager = new NetworkManager(config, testListener, "Player");
        assertFalse(networkManager.isConnected());
    }

    @Test
    @DisplayName("여러 NetworkManager 인스턴스 생성")
    public void testMultipleInstances() {
        NetworkManager nm1 = new NetworkManager(config, testListener, "Player1");
        NetworkManager nm2 = new NetworkManager(config, testListener, "Player2");
        
        assertNotNull(nm1);
        assertNotNull(nm2);
        assertNotEquals(nm1, nm2);
        
        nm1.shutdown();
        nm2.shutdown();
    }
    
    // ============== NetworkTestApp & SimpleNetworkTest 기능 테스트 ==============
    
    @Test
    @DisplayName("서버-클라이언트 연결 테스트")
    public void testServerClientConnection() throws Exception {
        CountDownLatch serverConnectedLatch = new CountDownLatch(1);
        CountDownLatch clientConnectedLatch = new CountDownLatch(1);
        
        AtomicReference<String> serverPeerId = new AtomicReference<>();
        AtomicReference<String> clientPeerId = new AtomicReference<>();
        
        // 서버 리스너
        MessageListener serverListener = new MessageListener() {
            @Override
            public void onMessageReceived(GameMessage message) {}
            @Override
            public void onConnected(String peerId) {
                serverPeerId.set(peerId);
                serverConnectedLatch.countDown();
            }
            @Override
            public void onDisconnected(String peerId, String reason) {}
            @Override
            public void onError(String errorMessage, Exception exception) {}
        };
        
        // 클라이언트 리스너
        MessageListener clientListener = new MessageListener() {
            @Override
            public void onMessageReceived(GameMessage message) {}
            @Override
            public void onConnected(String peerId) {
                clientPeerId.set(peerId);
                clientConnectedLatch.countDown();
            }
            @Override
            public void onDisconnected(String peerId, String reason) {}
            @Override
            public void onError(String errorMessage, Exception exception) {}
        };
        
        // 서버 시작
        networkManager = new NetworkManager(config, serverListener, "Server");
        networkManager.startServer();
        
        // 서버 시작 대기
        Thread.sleep(1000);
        
        // 클라이언트 연결
        clientManager = new NetworkManager(config, clientListener, "Client");
        clientManager.connectToServer("127.0.0.1");
        
        // 연결 대기 (최대 5초)
        assertTrue(serverConnectedLatch.await(5, TimeUnit.SECONDS), 
            "서버가 클라이언트 연결을 받지 못했습니다");
        assertTrue(clientConnectedLatch.await(5, TimeUnit.SECONDS), 
            "클라이언트가 서버에 연결하지 못했습니다");
        
        // Handshake 메시지 대기
        Thread.sleep(1000);
        
        // 연결 확인 (peerId는 첫 메시지 수신 후 설정됨)
        // 서버와 클라이언트가 서로 연결됨을 확인
        assertNotNull(serverPeerId.get(), "서버가 클라이언트 ID를 받지 못했습니다");
        assertNotNull(clientPeerId.get(), "클라이언트가 서버 ID를 받지 못했습니다");
    }
    
    @Test
    @DisplayName("메시지 송수신 테스트")
    public void testMessageExchange() throws Exception {
        CountDownLatch messageLatch = new CountDownLatch(2);
        AtomicReference<GameMessage> serverReceivedMsg = new AtomicReference<>();
        AtomicReference<GameMessage> clientReceivedMsg = new AtomicReference<>();
        
        // 서버 리스너
        MessageListener serverListener = new MessageListener() {
            @Override
            public void onMessageReceived(GameMessage message) {
                serverReceivedMsg.set(message);
                messageLatch.countDown();
            }
            @Override
            public void onConnected(String peerId) {}
            @Override
            public void onDisconnected(String peerId, String reason) {}
            @Override
            public void onError(String errorMessage, Exception exception) {}
        };
        
        // 클라이언트 리스너
        MessageListener clientListener = new MessageListener() {
            @Override
            public void onMessageReceived(GameMessage message) {
                clientReceivedMsg.set(message);
                messageLatch.countDown();
            }
            @Override
            public void onConnected(String peerId) {}
            @Override
            public void onDisconnected(String peerId, String reason) {}
            @Override
            public void onError(String errorMessage, Exception exception) {}
        };
        
        // 서버 시작
        networkManager = new NetworkManager(config, serverListener, "Server");
        networkManager.startServer();
        Thread.sleep(500);
        
        // 클라이언트 연결
        clientManager = new NetworkManager(config, clientListener, "Client");
        clientManager.connectToServer("127.0.0.1");
        Thread.sleep(1000);
        
        // 메시지 전송 - 클라이언트 -> 서버
        GameMessage clientMessage = new GameMessage(MessageType.GAME_STATE, "Client");
        clientMessage.put("text", "Hello Server!");
        clientMessage.put("timestamp", System.currentTimeMillis());
        clientManager.sendMessage(clientMessage);
        
        // 메시지 전송 - 서버 -> 클라이언트
        GameMessage serverMessage = new GameMessage(MessageType.GAME_STATE, "Server");
        serverMessage.put("text", "Hello Client!");
        serverMessage.put("timestamp", System.currentTimeMillis());
        networkManager.sendMessage(serverMessage);
        
        // 메시지 수신 대기
        assertTrue(messageLatch.await(5, TimeUnit.SECONDS), 
            "메시지 송수신이 완료되지 않았습니다");
        
        // 메시지 검증
        assertNotNull(serverReceivedMsg.get(), "서버가 메시지를 받지 못했습니다");
        assertNotNull(clientReceivedMsg.get(), "클라이언트가 메시지를 받지 못했습니다");
        assertEquals("Hello Server!", serverReceivedMsg.get().getString("text"));
        assertEquals("Hello Client!", clientReceivedMsg.get().getString("text"));
    }
    
    @Test
    @DisplayName("연결 상태 확인 테스트")
    public void testConnectionStatus() throws Exception {
        CountDownLatch connectedLatch = new CountDownLatch(1);
        
        MessageListener serverListener = new MessageListener() {
            @Override
            public void onMessageReceived(GameMessage message) {}
            @Override
            public void onConnected(String peerId) {
                connectedLatch.countDown();
            }
            @Override
            public void onDisconnected(String peerId, String reason) {}
            @Override
            public void onError(String errorMessage, Exception exception) {}
        };
        
        // 서버 시작
        networkManager = new NetworkManager(config, serverListener, "Server");
        assertFalse(networkManager.isConnected(), "연결 전에는 isConnected가 false여야 합니다");
        
        networkManager.startServer();
        Thread.sleep(500);
        
        // 클라이언트 연결
        clientManager = new NetworkManager(config, testListener, "Client");
        clientManager.connectToServer("127.0.0.1");
        
        // 연결 대기
        assertTrue(connectedLatch.await(5, TimeUnit.SECONDS));
        
        // 연결 상태 확인
        assertTrue(networkManager.isConnected(), "연결 후 서버의 isConnected가 true여야 합니다");
        assertTrue(clientManager.isConnected(), "연결 후 클라이언트의 isConnected가 true여야 합니다");
    }
    
    @Test
    @DisplayName("레이턴시 업데이트 테스트")
    public void testLatencyUpdate() throws Exception {
        CountDownLatch latencyLatch = new CountDownLatch(1);
        AtomicLong latencyValue = new AtomicLong(-1);
        
        MessageListener clientListener = new MessageListener() {
            @Override
            public void onMessageReceived(GameMessage message) {}
            @Override
            public void onConnected(String peerId) {}
            @Override
            public void onDisconnected(String peerId, String reason) {}
            @Override
            public void onError(String errorMessage, Exception exception) {}
            @Override
            public void onLatencyUpdate(long latencyMs) {
                latencyValue.set(latencyMs);
                latencyLatch.countDown();
            }
        };
        
        // 서버 시작
        networkManager = new NetworkManager(config, testListener, "Server");
        networkManager.startServer();
        Thread.sleep(500);
        
        // 클라이언트 연결
        clientManager = new NetworkManager(config, clientListener, "Client");
        clientManager.connectToServer("127.0.0.1");
        
        // 레이턴시 업데이트 대기 (ping 간격 고려)
        assertTrue(latencyLatch.await(10, TimeUnit.SECONDS), 
            "레이턴시 업데이트를 받지 못했습니다");
        
        // 레이턴시 값 검증
        assertTrue(latencyValue.get() >= 0, "레이턴시 값이 유효하지 않습니다");
        assertTrue(latencyValue.get() < 1000, "로컬 연결의 레이턴시가 너무 높습니다");
    }
    
    @Test
    @DisplayName("연결 해제 테스트")
    public void testDisconnection() throws Exception {
        CountDownLatch disconnectedLatch = new CountDownLatch(1);
        AtomicReference<String> disconnectReason = new AtomicReference<>();
        
        MessageListener serverListener = new MessageListener() {
            @Override
            public void onMessageReceived(GameMessage message) {}
            @Override
            public void onConnected(String peerId) {}
            @Override
            public void onDisconnected(String peerId, String reason) {
                disconnectReason.set(reason);
                disconnectedLatch.countDown();
            }
            @Override
            public void onError(String errorMessage, Exception exception) {}
        };
        
        // 서버 시작
        networkManager = new NetworkManager(config, serverListener, "Server");
        networkManager.startServer();
        Thread.sleep(500);
        
        // 클라이언트 연결
        clientManager = new NetworkManager(config, testListener, "Client");
        clientManager.connectToServer("127.0.0.1");
        Thread.sleep(1000);
        
        // 클라이언트 연결 해제
        clientManager.disconnect("사용자 요청");
        
        // 연결 해제 대기
        assertTrue(disconnectedLatch.await(5, TimeUnit.SECONDS), 
            "연결 해제 알림을 받지 못했습니다");
        
        // 연결 해제 사유 확인
        assertNotNull(disconnectReason.get());
        assertFalse(clientManager.isConnected(), "연결 해제 후 isConnected가 false여야 합니다");
    }
    
    @Test
    @DisplayName("다중 메시지 송수신 테스트")
    public void testMultipleMessages() throws Exception {
        int messageCount = 10;
        CountDownLatch messageLatch = new CountDownLatch(messageCount);
        List<GameMessage> receivedMessages = new ArrayList<>();
        
        MessageListener serverListener = new MessageListener() {
            @Override
            public void onMessageReceived(GameMessage message) {
                synchronized (receivedMessages) {
                    receivedMessages.add(message);
                }
                messageLatch.countDown();
            }
            @Override
            public void onConnected(String peerId) {}
            @Override
            public void onDisconnected(String peerId, String reason) {}
            @Override
            public void onError(String errorMessage, Exception exception) {}
        };
        
        // 서버 시작
        networkManager = new NetworkManager(config, serverListener, "Server");
        networkManager.startServer();
        Thread.sleep(500);
        
        // 클라이언트 연결
        clientManager = new NetworkManager(config, testListener, "Client");
        clientManager.connectToServer("127.0.0.1");
        Thread.sleep(1000);
        
        // 다중 메시지 전송
        for (int i = 0; i < messageCount; i++) {
            GameMessage message = new GameMessage(MessageType.GAME_STATE, "Client");
            message.put("text", "Message " + i);
            message.put("index", i);
            clientManager.sendMessage(message);
            Thread.sleep(50); // 메시지 간 간격
        }
        
        // 모든 메시지 수신 대기
        assertTrue(messageLatch.await(10, TimeUnit.SECONDS), 
            "모든 메시지를 받지 못했습니다");
        
        // 메시지 개수 확인
        assertEquals(messageCount, receivedMessages.size(), 
            "받은 메시지 개수가 일치하지 않습니다");
    }
    
    @Test
    @DisplayName("에러 처리 테스트 - 잘못된 IP")
    public void testInvalidServerIP() throws Exception {
        CountDownLatch errorLatch = new CountDownLatch(1);
        AtomicBoolean errorOccurred = new AtomicBoolean(false);
        
        MessageListener clientListener = new MessageListener() {
            @Override
            public void onMessageReceived(GameMessage message) {}
            @Override
            public void onConnected(String peerId) {}
            @Override
            public void onDisconnected(String peerId, String reason) {}
            @Override
            public void onError(String errorMessage, Exception exception) {
                errorOccurred.set(true);
                errorLatch.countDown();
            }
        };
        
        clientManager = new NetworkManager(config, clientListener, "Client");
        
        // 잘못된 IP로 연결 시도
        assertDoesNotThrow(() -> clientManager.connectToServer("999.999.999.999"));
        
        // 에러 발생 대기
        assertTrue(errorLatch.await(10, TimeUnit.SECONDS), 
            "에러가 발생하지 않았습니다");
        assertTrue(errorOccurred.get(), "에러 핸들러가 호출되지 않았습니다");
    }
    
    @Test
    @DisplayName("서버 시작 전 클라이언트 연결 시도")
    public void testConnectWithoutServer() throws Exception {
        CountDownLatch errorLatch = new CountDownLatch(1);
        AtomicBoolean errorOccurred = new AtomicBoolean(false);
        
        MessageListener clientListener = new MessageListener() {
            @Override
            public void onMessageReceived(GameMessage message) {}
            @Override
            public void onConnected(String peerId) {}
            @Override
            public void onDisconnected(String peerId, String reason) {}
            @Override
            public void onError(String errorMessage, Exception exception) {
                errorOccurred.set(true);
                errorLatch.countDown();
            }
        };
        
        clientManager = new NetworkManager(config, clientListener, "Client");
        
        // 서버 없이 연결 시도
        clientManager.connectToServer("127.0.0.1");
        
        // 에러 발생 대기
        assertTrue(errorLatch.await(10, TimeUnit.SECONDS), 
            "에러가 발생하지 않았습니다");
        assertTrue(errorOccurred.get(), "에러 핸들러가 호출되지 않았습니다");
        assertFalse(clientManager.isConnected(), "연결이 되지 않아야 합니다");
    }
    
    @Test
    @DisplayName("LocalId 및 PeerId 확인")
    public void testLocalIdAndPeerId() throws Exception {
        CountDownLatch connectedLatch = new CountDownLatch(1);
        
        MessageListener serverListener = new MessageListener() {
            @Override
            public void onMessageReceived(GameMessage message) {}
            @Override
            public void onConnected(String peerId) {
                connectedLatch.countDown();
            }
            @Override
            public void onDisconnected(String peerId, String reason) {}
            @Override
            public void onError(String errorMessage, Exception exception) {}
        };
        
        // 서버 시작
        networkManager = new NetworkManager(config, serverListener, "Server");
        assertEquals("Server", networkManager.getLocalId());
        assertNull(networkManager.getPeerId(), "연결 전에는 PeerId가 null이어야 합니다");
        
        networkManager.startServer();
        Thread.sleep(1000);
        
        // 클라이언트 연결
        clientManager = new NetworkManager(config, testListener, "Client");
        assertEquals("Client", clientManager.getLocalId());
        clientManager.connectToServer("127.0.0.1");
        
        assertTrue(connectedLatch.await(5, TimeUnit.SECONDS));
        
        // 메시지를 전송하여 peerId 설정
        GameMessage testMsg = new GameMessage(MessageType.GAME_STATE, "Client");
        testMsg.put("test", "handshake");
        clientManager.sendMessage(testMsg);
        Thread.sleep(500);
        
        // PeerId 확인 (메시지 수신 후 설정됨)
        assertNotNull(networkManager.getPeerId(), "서버가 클라이언트 ID를 받아야 합니다");
        // 서버에서 클라이언트로 메시지도 전송
        GameMessage serverMsg = new GameMessage(MessageType.GAME_STATE, "Server");
        serverMsg.put("test", "response");
        networkManager.sendMessage(serverMsg);
        Thread.sleep(500);
        assertNotNull(clientManager.getPeerId(), "클라이언트가 서버 ID를 받아야 합니다");
    }
    
    @Test
    @DisplayName("메시지 레이턴시 측정")
    public void testMessageLatency() throws Exception {
        CountDownLatch messageLatch = new CountDownLatch(1);
        AtomicReference<GameMessage> receivedMessage = new AtomicReference<>();
        
        MessageListener serverListener = new MessageListener() {
            @Override
            public void onMessageReceived(GameMessage message) {
                receivedMessage.set(message);
                messageLatch.countDown();
            }
            @Override
            public void onConnected(String peerId) {}
            @Override
            public void onDisconnected(String peerId, String reason) {}
            @Override
            public void onError(String errorMessage, Exception exception) {}
        };
        
        // 서버 시작
        networkManager = new NetworkManager(config, serverListener, "Server");
        networkManager.startServer();
        Thread.sleep(500);
        
        // 클라이언트 연결
        clientManager = new NetworkManager(config, testListener, "Client");
        clientManager.connectToServer("127.0.0.1");
        Thread.sleep(1000);
        
        // 메시지 전송
        long sendTime = System.currentTimeMillis();
        GameMessage message = new GameMessage(MessageType.GAME_STATE, "Client");
        message.put("text", "Latency test");
        message.put("timestamp", sendTime);
        clientManager.sendMessage(message);
        
        // 메시지 수신 대기
        assertTrue(messageLatch.await(5, TimeUnit.SECONDS));
        
        // 레이턴시 확인
        GameMessage received = receivedMessage.get();
        assertNotNull(received);
        long latency = received.getLatency();
        assertTrue(latency >= 0, "레이턴시가 음수가 될 수 없습니다");
        assertTrue(latency < 500, "로컬 연결의 메시지 레이턴시가 너무 높습니다");
    }
    
    // ============== 테스트 헬퍼 클래스 ==============
    
    /**
     * 테스트용 MessageListener 구현
     */
    private static class TestMessageListener implements MessageListener {
        @Override
        public void onMessageReceived(GameMessage message) {}
        @Override
        public void onConnected(String peerId) {}
        @Override
        public void onDisconnected(String peerId, String reason) {}
        @Override
        public void onError(String errorMessage, Exception exception) {}
    }
}

