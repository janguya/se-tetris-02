package com.example.network;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class NetworkManager {

    private final ConnectionConfig config;
    private MessageListener listener;
    private final String localId;
    
    // 연결 상태
    private Socket socket;
    private ServerSocket serverSocket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private final AtomicBoolean connected = new AtomicBoolean(false);
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicBoolean handshakeComplete = new AtomicBoolean(false);
    
    // 스레드 풀
    private final ExecutorService executorService;
    private ScheduledExecutorService pingScheduler;
    
    // 레이턴시 추적
    private final AtomicLong lastPingTime = new AtomicLong(0);
    private final AtomicLong currentLatency = new AtomicLong(0);
    
    // 상대방 정보
    private String peerId;
    
    // Network 생성자
    // @param config 연결 설정
    // @param listener 메시지 리스너
    // @param localId 로컬 사용자 ID
    public NetworkManager(ConnectionConfig config, MessageListener listener, String localId) {
        if(config == null || listener == null || localId == null || localId.isEmpty()) {
            throw new IllegalArgumentException("Invalid arguments for NetworkManager");
        }
        this.config = config;
        this.listener = listener;
        this.localId = localId;
        this.executorService = Executors.newCachedThreadPool();

        System.out.println("NetworkManager created for player: " + localId);
        System.out.println("Configuration: " + config);

        // JVM 종료 시 자동으로 리소스 정리
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("JVM shutting down - cleaning up NetworkManager...");
            shutdown();
        }));
    }

    // ============== 서버 모드 (호스트) ==============

    // 서버로 시작 (방 생성)
    //@throws IOException 서버 시작 실패
    public void startServer() throws IOException {
        if (running.get()) {
            throw new IllegalStateException("NetworkManager is already running");
        }

        System.out.println("Starting server on port " + config.getPort() + "...");

        serverSocket = new ServerSocket();
        serverSocket.setReuseAddress(true); // 포트 재사용 허용
        serverSocket.bind(new InetSocketAddress(config.getPort()));
        serverSocket.setSoTimeout(0); // 무한 대기
        running.set(true);
        
        System.out.println("Server started. Waiting for connections...");
        System.out.println("Local IP: "+ getLocalIPAddress());

        // 클라이언트 연결 대기 (별도 스레드)
        executorService.submit(this::acceptClient);
    }

    // 클라이언트 연결 수락
    private void acceptClient() {
        try {
            System.out.println("Waiting for client connection...");
            socket = serverSocket.accept();
            
            System.out.println("Client connected from " + socket.getInetAddress().getHostAddress());

            // 스트림 초기화
            initializeStreams();

            // 메시지 수신 시작 (핸드셰이크 대기를 위해 먼저 시작)
            startReceiving();
            
            // ===== CONNECT_REQUEST 대기 =====
            System.out.println(">>> Server: Waiting for CONNECT_REQUEST from client...");
            int timeout = 0;
            while (!handshakeComplete.get() && timeout < 50) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.err.println(">>> Client: Handshake wait interrupted");
                    break;
                }
                timeout++;
            }
            
            
            if (!handshakeComplete.get()) {
                System.err.println(">>> Client: Handshake failed - no CONNECT_RESPONSE from server");
                running.set(false);
                throw new IOException("서버가 응답하지 않습니다. 방이 존재하지 않을 수 있습니다.");
            }
            
            System.out.println(">>> Server: Handshake complete!");
            // ===== 핸드셰이크 완료 =====
            
            // 연결 완료
            connected.set(true);
            
            // 핑 시작
            startPingScheduler();
            
            // 리스너 알림
            listener.onConnected(peerId != null ? peerId : "Unknown");
 

        } catch (IOException e) {
            if (running.get()) {
                System.err.println("Error accepting client: " + e.getMessage());
                listener.onError("Failed to accept client connection", e);
            }
        }
    }

    // ============== 클라이언트 모드 (참가자) ==============

    // 서버에 연결 (방 참가)
    // @param hostAddress 호스트 IP 주소
    // @throws IOException 연결 실패
    public void connectToServer(String hostAddress) throws IOException {
        if (running.get()) {
            throw new IllegalStateException("Already running");
        }
        
        System.out.println("Connecting to server at " + hostAddress + ":" + config.getPort() + "...");
        
        running.set(true);
        
        // 연결 시도 (별도 스레드)
        executorService.submit(() -> {
            try {
                socket = new Socket();
                socket.connect(
                    new InetSocketAddress(hostAddress, config.getPort()),
                    config.getConnectionTimeout()
                );
                
                System.out.println("Connected to server!");
                
                // 스트림 초기화
                initializeStreams();
                
                // 메시지 수신 시작 (먼저 시작해야 응답을 받을 수 있음)
                startReceiving();
                
                // ===== 핸드셰이크 시작: CONNECT_REQUEST 전송 =====
                System.out.println(">>> Client: Sending CONNECT_REQUEST...");
                GameMessage request = new GameMessage(MessageType.CONNECT_REQUEST, localId);
                request.put("clientId", localId);
                request.put("version", "1.0");
                sendMessageDirect(request);
                
                // CONNECT_RESPONSE 대기 (최대 5초)
                System.out.println(">>> Client: Waiting for CONNECT_RESPONSE...");
                int timeout = 0;
                while (!handshakeComplete.get() && timeout < 50) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        System.err.println(">>> Client: Handshake wait interrupted");
                        break;
                    }
                    timeout++;
                }
                
                if (!handshakeComplete.get()) {
                    System.err.println(">>> Client: Handshake failed - no CONNECT_RESPONSE from server");
                    running.set(false);
                    throw new IOException("서버가 응답하지 않습니다. 방이 존재하지 않을 수 있습니다.");
                }
                
                System.out.println(">>> Client: Handshake complete!");
                // ===== 핸드셰이크 완료 =====
                
                // 연결 완료
                connected.set(true);
                
                // 핑 시작
                startPingScheduler();
                
                // 리스너 알림
                listener.onConnected(peerId != null ? peerId : hostAddress);
                
            } catch (Exception e) {
                System.err.println("Failed to connect: " + e.getMessage());
                listener.onError("Connection failed", e);
                running.set(false);
                connected.set(false);
                handshakeComplete.set(false);
            }
        });
    }

    // ============== 공통 기능 ==============
    
    // 스트림 초기화
    private void initializeStreams() throws IOException {
        // 출력 스트림 먼저 생성 (중요!)
        out = new ObjectOutputStream(socket.getOutputStream());
        out.flush();
        
        // 입력 스트림 생성
        in = new ObjectInputStream(socket.getInputStream());
        
        System.out.println("Streams initialized successfully");
    }

    // 메시지 수신 시작
    private void startReceiving() {
        executorService.submit(() -> {
            System.out.println("Started receiving messages...");
            
            while (running.get()) {
                try {
                    // 메시지 수신
                    Object obj = in.readObject();
                    
                    if (obj instanceof GameMessage) {
                        GameMessage message = (GameMessage) obj;

                        // 핸드셰이크 메시지 처리
                        if (message.getType() == MessageType.CONNECT_REQUEST) {
                            handleConnectRequest(message);
                            continue;
                        } else if (message.getType() == MessageType.CONNECT_RESPONSE) {
                            handleConnectResponse(message);
                            continue;
                        }
                        
                        // PONG 메시지는 레이턴시 계산
                        if (message.getType() == MessageType.PONG) {
                            handlePong(message);
                        }
                        // PING 메시지는 자동 응답
                        else if (message.getType() == MessageType.PING) {
                            handlePing(message);
                        }
                        // 나머지는 리스너에 전달
                        else {
                            listener.onMessageReceived(message);
                        }
                        
                        // 상대방 ID 저장
                        if (peerId == null && message.getSenderId() != null) {
                            peerId = message.getSenderId();
                        }
                    }
                    
                } catch (EOFException e) {
                    // 연결 종료
                    System.out.println("Connection closed by peer");
                    break;
                    
                } catch (SocketException e) {
                    if (running.get()) {
                        System.err.println("Socket error: " + e.getMessage());
                    }
                    break;
                    
                } catch (Exception e) {
                    if (running.get()) {
                        System.err.println("Error receiving message: " + e.getMessage());
                        e.printStackTrace();
                    }
                    break;
                }
            }
            
            // 수신 종료 - 연결 끊김 처리
            if (running.get()) {
                disconnect("Connection lost");
            }
        });
    }

    // 서버: CONNECT_REQUEST 처리
    private void handleConnectRequest(GameMessage request) {
        System.out.println(">>> Server: Received CONNECT_REQUEST from " + request.getSenderId());
        
        // 클라이언트 정보 저장
        peerId = request.getSenderId();
        String clientVersion = request.getString("version");
        
        System.out.println(">>> Server: Client version: " + clientVersion);
        
        // CONNECT_RESPONSE 전송
        GameMessage response = new GameMessage(MessageType.CONNECT_RESPONSE, localId);
        response.put("serverId", localId);
        response.put("version", "1.0");
        response.put("status", "accepted");
        
        sendMessageDirect(response);
        
        System.out.println(">>> Server: Sent CONNECT_RESPONSE");
        
        // 핸드셰이크 완료
        handshakeComplete.set(true);
    }

    // 클라이언트: CONNECT_RESPONSE 처리
    private void handleConnectResponse(GameMessage response) {
        System.out.println(">>> Client: Received CONNECT_RESPONSE from " + response.getSenderId());
        
        String status = response.getString("status");
        
        if ("accepted".equals(status)) {
            peerId = response.getSenderId();
            String serverVersion = response.getString("version");
            
            System.out.println(">>> Client: Server version: " + serverVersion);
            System.out.println(">>> Client: Connection accepted!");
            
            // 핸드셰이크 완료
            handshakeComplete.set(true);
        } else {
            System.err.println(">>> Client: Connection rejected by server");
            disconnect("Connection rejected");
        }
    }

    // 직접 메시지 전송 (핸드셰이크용)
    private void sendMessageDirect(GameMessage message) {
        try {
            synchronized (out) {
                out.writeObject(message);
                out.flush();
                out.reset();
            }
        } catch (IOException e) {
            System.err.println("Failed to send message: " + e.getMessage());
        }
    }

    // 메시지 전송
    // @param message 전송할 메시지
    public void sendMessage(GameMessage message) {
        if (!connected.get()) {
            System.err.println("Cannot send message: not connected");
            return;
        }
        
        executorService.submit(() -> {
            try {
                synchronized (out) {
                    out.writeObject(message);
                    out.flush();
                    out.reset(); // 메모리 누수 방지
                }
                
                // 긴급 메시지는 로그
                if (message.isUrgent() && message.getType() != MessageType.PING && message.getType() != MessageType.PONG) {
                    System.out.println("Sent urgent message: " + message.getType());
                }
                
            } catch (IOException e) {
                System.err.println("Failed to send message: " + e.getMessage());
                listener.onError("Failed to send message", e);
                
                // 전송 실패 시 연결 체크
                if (!socket.isConnected() || socket.isClosed()) {
                    disconnect("Connection lost during send");
                }
            }
        });
    }
    
    // PING/PONG 스케줄러 시작
    private void startPingScheduler() {
        pingScheduler = Executors.newSingleThreadScheduledExecutor();
        
        pingScheduler.scheduleAtFixedRate(() -> {
            if (connected.get()) {
                sendPing();
            }
        }, 0, config.getPingInterval(), TimeUnit.MILLISECONDS);
        
        System.out.println("Ping scheduler started (interval: " + config.getPingInterval() + "ms)");
    }
    
    // PING 전송
    private void sendPing() {
        lastPingTime.set(System.currentTimeMillis());
        GameMessage ping = GameMessage.createPing(localId);
        sendMessage(ping);
    }
    
    // PING 수신 처리 (자동 PONG 응답)
    private void handlePing(GameMessage ping) {
        GameMessage pong = GameMessage.createPong(localId);
        sendMessage(pong);
    }

    // PONG 수신 처리 (레이턴시 계산)
    private void handlePong(GameMessage pong) {
        long latency = System.currentTimeMillis() - lastPingTime.get();
        currentLatency.set(latency);
        
        // 레이턴시 경고
        if (latency > config.getMaxLatency()) {
            System.out.println("⚠️ High latency: " + latency + "ms (max: " + config.getMaxLatency() + "ms)");
        }
        
        // 리스너에 레이턴시 알림
        listener.onLatencyUpdate(latency);
    }
    
    // 연결 종료
    // @param reason 종료 이유
    public void disconnect(String reason) {
        if (!running.get()) {
            return;
        }
        
        System.out.println("Disconnecting: " + reason);
        
        running.set(false);
        connected.set(false);
        handshakeComplete.set(false);
        
        // 핑 스케줄러 중지
        if (pingScheduler != null) {
            pingScheduler.shutdown();
        }
        
        // 소켓 닫기
        closeResources();
        
        // 리스너 알림
        listener.onDisconnected(peerId != null ? peerId : "Unknown", reason);
    }
    
    // 리소스 정리
    private void closeResources() {
        try {
            if (out != null) out.close();
        } catch (IOException e) { /* ignore */ }
        
        try {
            if (in != null) in.close();
        } catch (IOException e) { /* ignore */ }
        
        try {
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) { /* ignore */ }
        
        try {
            if (serverSocket != null && !serverSocket.isClosed()) serverSocket.close();
        } catch (IOException e) { /* ignore */ }
    }

    // 완전 종료 (스레드 풀 포함)
    public void shutdown() {
        disconnect("Shutdown requested");
        
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }
        
        System.out.println("NetworkManager shutdown complete");
    }
    
    // ============== 상태 조회 ==============
    
    public boolean isConnected() {
        return connected.get() && handshakeComplete.get();
    }
    
    public boolean isRunning() {
        return running.get();
    }
    
    public long getCurrentLatency() {
        return currentLatency.get();
    }

    // 리스너 교체 메서드 추가
    public void setListener(MessageListener newListener) {
        if (newListener == null) {
            throw new IllegalArgumentException("Listener cannot be null");
        }
        this.listener = newListener;
        System.out.println(">>> NetworkManager: Listener replaced");
    
        // 이미 연결되어 있으면 새 리스너에게 알림
        if (connected.get() && peerId != null) {
            listener.onConnected(peerId);
        }
    }
    
    public String getPeerId() {
        return peerId;
    }
    
    public String getLocalId() {
        return localId;
    }
    
    // 로컬 IP 주소 조회
    // @return 로컬 IP 주소
    public static String getLocalIPAddress() {
        try {
            // 모든 네트워크 인터페이스 조회
            for (NetworkInterface ni : java.util.Collections.list(NetworkInterface.getNetworkInterfaces())) {
                for (InetAddress addr : java.util.Collections.list(ni.getInetAddresses())) {
                    // IPv4이고 루프백이 아닌 주소
                    if (!addr.isLoopbackAddress() && addr instanceof Inet4Address) {
                        String ip = addr.getHostAddress();
                        // 192.168.x.x 또는 10.x.x.x 형태의 로컬 네트워크 주소
                        if (ip.startsWith("192.168.") || ip.startsWith("10.")) {
                            return ip;
                        }
                    }
                }
            }
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            return "Unknown";
        }
    }
}