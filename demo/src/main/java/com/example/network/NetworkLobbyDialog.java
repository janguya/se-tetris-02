package com.example.network;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;

// 네트워크 로비 다이얼로그
// 서버 생성 또는 클라이언트로 접속하는 UI 제공
public class NetworkLobbyDialog {

    public interface LobbyCallback {
        void onServerCreated(NetworkManager networkManager);
        void onClientConnected(NetworkManager networkManager);
        void onCancelled();
    }
    
    private Stage dialog;
    private NetworkManager networkManager;

    // IP 주소 저장을 위한 Preferences
    private static final String PREF_KEY_LAST_IP = "last_server_ip";
    private static final String PREF_KEY_IP_HISTORY = "ip_history";
    private static final int MAX_IP_HISTORY = 5;
    private final Preferences prefs = Preferences.userNodeForPackage(NetworkLobbyDialog.class);

    // 로비 다이얼로그 표시
    // @param owner 부모 윈도우
    // @param callback 결과 콜백
    public static void show(Stage owner, LobbyCallback callback) {
        NetworkLobbyDialog lobbyDialog = new NetworkLobbyDialog();
        lobbyDialog.showDialog(owner, callback);
    }
    private void showDialog(Stage owner, LobbyCallback callback) {
        dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(owner);
        dialog.setTitle("온라인 대전");
        dialog.setResizable(false);
        
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(30));
        root.getStyleClass().add("dialog-root");
        
        // 타이틀
        Label title = new Label("온라인 대전 로비");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        title.setStyle("-fx-text-fill: black;");
        
        Label subtitle = new Label("같은 Wi-Fi 네트워크에 연결되어 있어야 합니다.");
        subtitle.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
        subtitle.setStyle("-fx-text-fill: gray;");
        
        // 모드 선택 버튼
        VBox modeButtons = new VBox(15);
        modeButtons.setAlignment(Pos.CENTER);
        
        Button serverButton = createModeButton("🏠 방 만들기 (서버)", 
            "다른 플레이어가 접속할 수 있는 방을 만듭니다.");
        serverButton.setOnAction(e -> handleServerMode(callback));
        
        Button clientButton = createModeButton("🔌 방 참가 (클라이언트)", 
            "다른 플레이어가 만든 방에 접속합니다.");
        clientButton.setOnAction(e -> handleClientMode(callback));
        
        modeButtons.getChildren().addAll(serverButton, clientButton);
        
        // 취소 버튼
        Button cancelButton = new Button("취소");
        cancelButton.getStyleClass().add("cancel-button");
        cancelButton.setPrefWidth(100);
        cancelButton.setOnAction(e -> {
            callback.onCancelled();
            dialog.close();
        });
        
        root.getChildren().addAll(title, subtitle, modeButtons, cancelButton);
        
        Scene scene = new Scene(root, 450, 400);
        try {
            scene.getStylesheets().add(
                NetworkLobbyDialog.class.getResource("/styles.css").toExternalForm()
            );
        } catch (Exception e) {
            // 스타일시트 없으면 무시
        }
        
        dialog.setScene(scene);
        dialog.centerOnScreen();
        dialog.show();
    }

    // 모드 선택 버튼 생성
    private Button createModeButton(String title, String description) {
        VBox buttonContent = new VBox(5);
        buttonContent.setAlignment(Pos.CENTER);
        
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        titleLabel.setStyle("-fx-text-fill: black;");
        
        Label descLabel = new Label(description);
        descLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 11));
        descLabel.setStyle("-fx-text-fill: black;");
        descLabel.setWrapText(true);
        descLabel.setMaxWidth(280);
        
        buttonContent.getChildren().addAll(titleLabel, descLabel);
        
        Button button = new Button();
        button.setGraphic(buttonContent);
        button.getStyleClass().add("mode-button");
        button.setPrefWidth(300);
        button.setPrefHeight(80);
        
        return button;
    }

    // 서버 모드 처리
    private void handleServerMode(LobbyCallback callback) {
        dialog.close();
        
        // 로딩 다이얼로그 표시
        Stage loadingDialog = showLoadingDialog("서버 시작 중...");
        
        CompletableFuture.runAsync(() -> {
            try {
                // 플레이어 ID 생성
                String playerId = "Player_" + System.currentTimeMillis();
                
                // NetworkManager 생성
                ConnectionConfig config = ConnectionConfig.createLocalNetworkConfig();
                MessageListener tempListener = new MessageListener() {
                    @Override
                    public void onMessageReceived(GameMessage message) {}
                    @Override
                    public void onConnected(String peerId) {}
                    @Override
                    public void onDisconnected(String peerId, String reason) {}
                    @Override
                    public void onError(String errorMessage, Exception exception) {}
                };
                
                networkManager = new NetworkManager(config, tempListener, playerId);
                networkManager.startServer();
                
                // 서버 정보 표시
                String localIP = NetworkManager.getLocalIPAddress();
                int port = config.getPort();
                
                Platform.runLater(() -> {
                    loadingDialog.close();
                    showServerInfoDialog(localIP, port, callback);
                });
                
            } catch (IOException e) {
                Platform.runLater(() -> {
                    loadingDialog.close();
                    showErrorDialog("서버 시작 실패", e.getMessage());
                    callback.onCancelled();
                });
            }
        });
    }

    // 클라이언트 모드 처리
    private void handleClientMode(LobbyCallback callback) {
        dialog.close();
        
        // IP 입력 다이얼로그
        TextInputDialog ipDialog = new TextInputDialog("192.168.0.1");
        ipDialog.setTitle("서버 연결");
        ipDialog.setHeaderText("서버 IP 주소 입력");
        ipDialog.setContentText("서버 IP:");
        
        ipDialog.showAndWait().ifPresent(serverIp -> {
            if (serverIp.isEmpty()) {
                callback.onCancelled();
                return;
            }
            
            // 로딩 다이얼로그 표시
            Stage loadingDialog = showLoadingDialog("서버 연결 중...");
            
            CompletableFuture.runAsync(() -> {
                try {
                    // 플레이어 ID 생성
                    String playerId = "Player_" + System.currentTimeMillis();
                    
                    // NetworkManager 생성
                    ConnectionConfig config = ConnectionConfig.createLocalNetworkConfig();
                    MessageListener tempListener = new MessageListener() {
                        @Override
                        public void onMessageReceived(GameMessage message) {}
                        @Override
                        public void onConnected(String peerId) {}
                        @Override
                        public void onDisconnected(String peerId, String reason) {}
                        @Override
                        public void onError(String errorMessage, Exception exception) {}
                    };
                    
                    networkManager = new NetworkManager(config, tempListener, playerId);
                    networkManager.connectToServer(serverIp);
                    
                    // 연결 대기 (최대 5초)
                    int attempts = 0;
                    while (!networkManager.isConnected() && attempts < 50) {
                        Thread.sleep(100);
                        attempts++;
                    }
                    
                    if (networkManager.isConnected()) {
                        Platform.runLater(() -> {
                            loadingDialog.close();
                            showSuccessDialog("연결 성공!", "서버에 연결되었습니다.");
                            callback.onClientConnected(networkManager);
                        });
                    } else {
                        throw new IOException("연결 타임아웃");
                    }
                    
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        loadingDialog.close();
                        showErrorDialog("연결 실패", e.getMessage());
                        callback.onCancelled();
                    });
                }
            });
        });
    }

    // 서버 정보 다이얼로그
    private void showServerInfoDialog(String ip, int port, LobbyCallback callback) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("서버 시작됨");
        alert.setHeaderText("서버가 시작되었습니다!");
        
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        
        Label ipLabel = new Label("📡 서버 IP: " + ip);
        ipLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        
        Label portLabel = new Label("🔌 포트: " + port);
        portLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
        
        Label infoLabel = new Label("클라이언트가 이 IP로 접속할 수 있습니다.");
        infoLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 11));
        infoLabel.setStyle("-fx-text-fill: gray;");
        
        Label waitLabel = new Label("⏳ 클라이언트 연결 대기 중...");
        waitLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        waitLabel.setStyle("-fx-text-fill: orange;");
        
        content.getChildren().addAll(ipLabel, portLabel, infoLabel, waitLabel);
        alert.getDialogPane().setContent(content);
        
        // 비동기로 표시 (닫지 않음)
        alert.show();
        
        // 연결 대기
        CompletableFuture.runAsync(() -> {
            while (!networkManager.isConnected()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    break;
                }
            }
            
            Platform.runLater(() -> {
                alert.close();
                showSuccessDialog("클라이언트 연결됨!", "게임을 시작합니다.");
                callback.onServerCreated(networkManager);
            });
        });
    }

    // 로딩 다이얼로그 표시
    private Stage showLoadingDialog(String message) {
        Stage loading = new Stage();
        loading.initModality(Modality.APPLICATION_MODAL);
        loading.setTitle("처리 중...");
        loading.setResizable(false);
        
        VBox content = new VBox(20);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(40));
        
        ProgressIndicator progress = new ProgressIndicator();
        progress.setPrefSize(60, 60);
        
        Label label = new Label(message);
        label.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        
        content.getChildren().addAll(progress, label);
        
        Scene scene = new Scene(content, 300, 150);
        loading.setScene(scene);
        loading.show();
        
        return loading;
    }

    // 성공 다이얼로그 표시
    private void showSuccessDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // 에러 다이얼로그 표시
    private void showErrorDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private String getLastConnectedIP() {
        String ip = prefs.get(PREF_KEY_LAST_IP, "192.168.0.1");
        System.out.println(">>> Loaded last IP: " + ip);
        return ip;
    }

    // 접속한 IP 주소 저장
    private void saveConnectedIP(String ip) {
        if (ip == null || ip.isEmpty()) {
            return;
        }
        
        // 유효한 IP 형식인지 간단히 체크
        if (!ip.matches("^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$")) {
            System.out.println(">>> Invalid IP format, not saving: " + ip);
            return;
        }
        
        // 최근 IP로 저장
        prefs.put(PREF_KEY_LAST_IP, ip);
        
        System.out.println(">>> Saved IP to preferences: " + ip);
    }
    
    // 저장된 IP 기록 삭제 (설정 초기화용)
    public static void clearIPHistory() {
        Preferences prefs = Preferences.userNodeForPackage(NetworkLobbyDialog.class);
        prefs.remove(PREF_KEY_LAST_IP);
        prefs.remove(PREF_KEY_IP_HISTORY);
        System.out.println(">>> Cleared IP history");
    }
}
