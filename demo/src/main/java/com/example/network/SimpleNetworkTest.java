package com.example.network;

import java.util.Scanner;

/**
 * 콘솔 기반 네트워크 통신 테스트
 * JavaFX 없이 간단하게 테스트 가능
 */
public class SimpleNetworkTest implements MessageListener {
    
    private NetworkManager networkManager;
    private String playerId;
    private Scanner scanner;
    
    public SimpleNetworkTest() {
        this.scanner = new Scanner(System.in);
    }
    
    public void start() {
        System.out.println("=================================");
        System.out.println("  P2P 네트워크 통신 테스트");
        System.out.println("=================================\n");
        
        System.out.println("모드를 선택하세요:");
        System.out.println("1. 서버 (방 만들기)");
        System.out.println("2. 클라이언트 (방 참가)");
        System.out.print("\n선택 (1 or 2): ");
        
        String choice = scanner.nextLine().trim();
        
        if (choice.equals("1")) {
            startServer();
        } else if (choice.equals("2")) {
            startClient();
        } else {
            System.out.println("잘못된 선택입니다.");
            System.exit(0);
        }
        
        // 메시지 입력 루프
        startMessageLoop();
    }
    
    /**
     * 서버 시작
     */
    private void startServer() {
        playerId = "Server";
        
        try {
            System.out.println("\n📡 서버 시작 중...");
            
            ConnectionConfig config = ConnectionConfig.createLocalNetworkConfig();
            networkManager = new NetworkManager(config, this, playerId);
            
            networkManager.startServer();
            
            System.out.println("✅ 서버 시작됨!");
            System.out.println("📍 로컬 IP: " + NetworkManager.getLocalIPAddress());
            System.out.println("🔌 포트: " + config.getPort());
            System.out.println("⏳ 클라이언트 연결 대기 중...\n");
            
        } catch (Exception e) {
            System.err.println("❌ 서버 시작 실패: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    /**
     * 클라이언트 시작
     */
    private void startClient() {
        playerId = "Client";
        
        System.out.print("\n서버 IP 주소를 입력하세요: ");
        String serverIp = scanner.nextLine().trim();
        
        if (serverIp.isEmpty()) {
            System.out.println("IP 주소를 입력해야 합니다.");
            System.exit(0);
        }
        
        try {
            System.out.println("\n📡 서버 연결 중...");
            
            ConnectionConfig config = ConnectionConfig.createLocalNetworkConfig();
            networkManager = new NetworkManager(config, this, playerId);
            
            System.out.println("🎯 서버 IP: " + serverIp);
            System.out.println("🔌 포트: " + config.getPort());
            
            networkManager.connectToServer(serverIp);
            
            System.out.println("⏳ 연결 대기 중...\n");
            
        } catch (Exception e) {
            System.err.println("❌ 서버 연결 실패: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    /**
     * 메시지 입력 루프
     */
    private void startMessageLoop() {
        System.out.println("명령어:");
        System.out.println("  - 메시지 입력 후 Enter: 메시지 전송");
        System.out.println("  - 'quit' 또는 'exit': 종료");
        System.out.println("  - 'status': 연결 상태 확인");
        System.out.println("  - 'latency': 현재 레이턴시 확인\n");
        
        while (true) {
            System.out.print("> ");
            String input = scanner.nextLine().trim();
            
            if (input.isEmpty()) {
                continue;
            }
            
            // 명령어 처리
            if (input.equalsIgnoreCase("quit") || input.equalsIgnoreCase("exit")) {
                System.out.println("\n👋 프로그램을 종료합니다...");
                if (networkManager != null) {
                    networkManager.shutdown();
                }
                System.exit(0);
            }
            else if (input.equalsIgnoreCase("status")) {
                showStatus();
            }
            else if (input.equalsIgnoreCase("latency")) {
                showLatency();
            }
            else {
                // 메시지 전송
                sendMessage(input);
            }
        }
    }
    
    /**
     * 메시지 전송
     */
    private void sendMessage(String text) {
        if (networkManager == null || !networkManager.isConnected()) {
            System.out.println("❌ 연결되지 않았습니다. 메시지를 전송할 수 없습니다.");
            return;
        }
        
        GameMessage message = new GameMessage(MessageType.GAME_STATE, playerId);
        message.put("text", text);
        message.put("timestamp", System.currentTimeMillis());
        
        networkManager.sendMessage(message);
        System.out.println("📤 전송: " + text);
    }
    
    /**
     * 연결 상태 확인
     */
    private void showStatus() {
        if (networkManager == null) {
            System.out.println("❌ NetworkManager가 초기화되지 않았습니다.");
            return;
        }
        
        System.out.println("\n=== 연결 상태 ===");
        System.out.println("실행 중: " + networkManager.isRunning());
        System.out.println("연결됨: " + networkManager.isConnected());
        System.out.println("로컬 ID: " + networkManager.getLocalId());
        System.out.println("상대방 ID: " + (networkManager.getPeerId() != null ? networkManager.getPeerId() : "없음"));
        System.out.println("================\n");
    }
    
    /**
     * 레이턴시 확인
     */
    private void showLatency() {
        if (networkManager == null || !networkManager.isConnected()) {
            System.out.println("❌ 연결되지 않았습니다.");
            return;
        }
        
        long latency = networkManager.getCurrentLatency();
        String status = latency < 100 ? "✅ 매우 좋음" : 
                       latency < 200 ? "⚠️ 양호" : 
                       "❌ 높음";
        
        System.out.println("\n현재 레이턴시: " + latency + "ms " + status + "\n");
    }
    
    // ============== MessageListener 구현 ==============
    
    @Override
    public void onMessageReceived(GameMessage message) {
        System.out.println("\n📥 수신: " + message.getString("text"));
        System.out.println("   발신자: " + message.getSenderId());
        System.out.println("   타입: " + message.getType());
        System.out.println("   레이턴시: " + message.getLatency() + "ms\n");
        System.out.print("> ");
    }
    
    @Override
    public void onConnected(String peerId) {
        System.out.println("\n✅ 연결 성공!");
        System.out.println("🤝 상대방: " + peerId);
        System.out.println("이제 메시지를 전송할 수 있습니다.\n");
        System.out.print("> ");
    }
    
    @Override
    public void onDisconnected(String peerId, String reason) {
        System.out.println("\n❌ 연결 끊김");
        System.out.println("   상대방: " + peerId);
        System.out.println("   이유: " + reason + "\n");
        System.out.print("> ");
    }
    
    @Override
    public void onError(String errorMessage, Exception exception) {
        System.err.println("\n⚠️ 에러: " + errorMessage);
        if (exception != null) {
            System.err.println("   상세: " + exception.getMessage());
        }
        System.out.print("\n> ");
    }
    
    @Override
    public void onLatencyUpdate(long latencyMs) {
        // 조용히 업데이트 (200ms 이상일 때만 경고)
        if (latencyMs > 200) {
            System.out.println("\n⚠️ 높은 레이턴시 감지: " + latencyMs + "ms");
            System.out.print("> ");
        }
    }
    
    // ============== Main ==============
    
    public static void main(String[] args) {
        SimpleNetworkTest test = new SimpleNetworkTest();
        test.start();
    }
}