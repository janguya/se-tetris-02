package com.example.network;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * 네트워크 통신 테스트 애플리케이션
 * 서버/클라이언트 모드를 선택하여 기본 통신을 테스트
 */
public class NetworkTestApp extends Application implements MessageListener {
    
    private NetworkManager networkManager;
    private TextArea logArea;
    private TextField messageField;
    private Button sendButton;
    private Label statusLabel;
    private Label latencyLabel;
    
    private String playerId;
    private boolean isServer;
    
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("P2P Network Test");
        
        // 초기 선택 다이얼로그
        showModeSelectionDialog(primaryStage);
    }
    
    /**
     * 서버/클라이언트 선택 다이얼로그
     */
    private void showModeSelectionDialog(Stage primaryStage) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("네트워크 모드 선택");
        dialog.setHeaderText("서버 또는 클라이언트를 선택하세요");
        
        // 버튼 타입
        ButtonType serverButton = new ButtonType("서버 (방 만들기)", ButtonBar.ButtonData.OK_DONE);
        ButtonType clientButton = new ButtonType("클라이언트 (방 참가)", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("취소", ButtonBar.ButtonData.CANCEL_CLOSE);
        
        dialog.getDialogPane().getButtonTypes().addAll(serverButton, clientButton, cancelButton);
        
        // 다이얼로그 내용
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        
        Label infoLabel = new Label("같은 Wi-Fi 네트워크에 연결되어 있어야 합니다.");
        infoLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: gray;");
        
        content.getChildren().add(infoLabel);
        dialog.getDialogPane().setContent(content);
        
        // 버튼 처리
        dialog.showAndWait().ifPresent(response -> {
            if (response == serverButton) {
                startAsServer(primaryStage);
            } else if (response == clientButton) {
                showClientConnectDialog(primaryStage);
            } else {
                Platform.exit();
            }
        });
    }
    
    /**
     * 서버로 시작
     */
    private void startAsServer(Stage primaryStage) {
        isServer = true;
        playerId = "Server";
        
        try {
            // NetworkManager 생성
            ConnectionConfig config = ConnectionConfig.createLocalNetworkConfig();
            networkManager = new NetworkManager(config, this, playerId);
            
            // 서버 시작
            networkManager.startServer();
            
            // UI 표시
            showMainUI(primaryStage, "서버 모드 - 연결 대기 중...");
            
            log("✅ 서버 시작됨!");
            log("📡 로컬 IP: " + NetworkManager.getLocalIPAddress());
            log("🔌 포트: " + config.getPort());
            log("⏳ 클라이언트 연결 대기 중...");
            
        } catch (Exception e) {
            showError("서버 시작 실패", e);
        }
    }
    
    /**
     * 클라이언트 연결 다이얼로그
     */
    private void showClientConnectDialog(Stage primaryStage) {
        TextInputDialog dialog = new TextInputDialog("192.168.0.1");
        dialog.setTitle("서버 연결");
        dialog.setHeaderText("서버 IP 주소 입력");
        dialog.setContentText("서버 IP:");
        
        dialog.showAndWait().ifPresent(ip -> {
            if (!ip.isEmpty()) {
                startAsClient(primaryStage, ip);
            } else {
                Platform.exit();
            }
        });
    }
    
    /**
     * 클라이언트로 시작
     */
    private void startAsClient(Stage primaryStage, String serverIp) {
        isServer = false;
        playerId = "Client";
        
        try {
            // NetworkManager 생성
            ConnectionConfig config = ConnectionConfig.createLocalNetworkConfig();
            networkManager = new NetworkManager(config, this, playerId);
            
            // UI 표시
            showMainUI(primaryStage, "클라이언트 모드 - 연결 중...");
            
            log("📡 서버 연결 시도 중...");
            log("🎯 서버 IP: " + serverIp);
            log("🔌 포트: " + config.getPort());
            
            // 서버에 연결
            networkManager.connectToServer(serverIp);
            
        } catch (Exception e) {
            showError("서버 연결 실패", e);
        }
    }
    
    /**
     * 메인 UI 표시
     */
    private void showMainUI(Stage primaryStage, String title) {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));
        
        // 상단: 상태 표시
        VBox topBox = new VBox(5);
        topBox.setPadding(new Insets(5));
        
        statusLabel = new Label(title);
        statusLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        
        latencyLabel = new Label("레이턴시: -");
        latencyLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: gray;");
        
        topBox.getChildren().addAll(statusLabel, latencyLabel);
        root.setTop(topBox);
        
        // 중앙: 로그 영역
        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setWrapText(true);
        logArea.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 12px;");
        root.setCenter(logArea);
        
        // 하단: 메시지 전송
        HBox bottomBox = new HBox(10);
        bottomBox.setPadding(new Insets(5));
        bottomBox.setAlignment(Pos.CENTER);
        
        messageField = new TextField();
        messageField.setPromptText("메시지 입력...");
        messageField.setPrefWidth(400);
        messageField.setOnAction(e -> sendTestMessage());
        
        sendButton = new Button("전송");
        sendButton.setOnAction(e -> sendTestMessage());
        sendButton.setDisable(true); // 연결 전에는 비활성화
        
        Button disconnectButton = new Button("연결 종료");
        disconnectButton.setOnAction(e -> disconnect());
        
        bottomBox.getChildren().addAll(messageField, sendButton, disconnectButton);
        root.setBottom(bottomBox);
        
        // Scene 설정
        Scene scene = new Scene(root, 700, 500);
        primaryStage.setScene(scene);
        primaryStage.show();
        
        // 종료 시 정리
        primaryStage.setOnCloseRequest(e -> {
            if (networkManager != null) {
                networkManager.shutdown();
            }
        });
    }
    
    /**
     * 테스트 메시지 전송
     */
    private void sendTestMessage() {
        String message = messageField.getText().trim();
        if (message.isEmpty()) {
            return;
        }
        
        // 메시지 전송
        GameMessage gameMessage = new GameMessage(MessageType.GAME_STATE, playerId);
        gameMessage.put("text", message);
        gameMessage.put("timestamp", System.currentTimeMillis());
        
        networkManager.sendMessage(gameMessage);
        
        log("📤 전송: " + message);
        messageField.clear();
    }
    
    /**
     * 연결 종료
     */
    private void disconnect() {
        if (networkManager != null) {
            networkManager.disconnect("사용자가 연결 종료");
        }
    }
    
    /**
     * 로그 출력
     */
    private void log(String message) {
        Platform.runLater(() -> {
            String timestamp = String.format("[%tT] ", System.currentTimeMillis());
            logArea.appendText(timestamp + message + "\n");
        });
    }
    
    /**
     * 에러 다이얼로그
     */
    private void showError(String title, Exception e) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("에러");
            alert.setHeaderText(title);
            alert.setContentText(e.getMessage());
            alert.showAndWait();
            Platform.exit();
        });
    }
    
    // ============== MessageListener 구현 ==============
    
    @Override
    public void onMessageReceived(GameMessage message) {
        log("📥 수신: " + message.getString("text"));
        log("   타입: " + message.getType());
        log("   발신자: " + message.getSenderId());
        log("   레이턴시: " + message.getLatency() + "ms");
    }
    
    @Override
    public void onConnected(String peerId) {
        log("✅ 연결 성공!");
        log("🤝 상대방: " + peerId);
        
        Platform.runLater(() -> {
            statusLabel.setText("연결됨 - " + peerId);
            statusLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: green;");
            sendButton.setDisable(false);
        });
    }
    
    @Override
    public void onDisconnected(String peerId, String reason) {
        log("❌ 연결 끊김");
        log("   이유: " + reason);
        
        Platform.runLater(() -> {
            statusLabel.setText("연결 끊김");
            statusLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: red;");
            sendButton.setDisable(true);
        });
    }
    
    @Override
    public void onError(String errorMessage, Exception exception) {
        log("⚠️ 에러: " + errorMessage);
        if (exception != null) {
            log("   상세: " + exception.getMessage());
        }
    }
    
    @Override
    public void onLatencyUpdate(long latencyMs) {
        Platform.runLater(() -> {
            String color = latencyMs < 100 ? "green" : latencyMs < 200 ? "orange" : "red";
            latencyLabel.setText("레이턴시: " + latencyMs + "ms");
            latencyLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: " + color + ";");
        });
    }
    
    // ============== Main ==============
    
    public static void main(String[] args) {
        launch(args);
    }
}







