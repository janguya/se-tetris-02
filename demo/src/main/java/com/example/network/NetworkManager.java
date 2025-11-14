package com.example.network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class NetworkManager {
    private Socket socket;
    private ServerSocket serverSocket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    
    private MessageListener listener;
    private Thread receiveThread;
    private boolean isConnected = false;
    
    private String localIP;  // 내 IP (서버용)
    
    //서버 시작 (호스트)
    public void startHost(int port) throws IOException {
        // TODO: 나중에 구현
        // ServerSocket 생성
        // 클라이언트 접속 대기
        // 연결되면 스트림 초기화
        // 수신 스레드 시작
    }
    
    //클라이언트 연결 (게스트)
    public void connectToHost(String ip, int port) throws IOException {
        // TODO: 나중에 구현
        // Socket으로 접속
        // 스트림 초기화
        // 수신 스레드 시작
    }
    
    //메시지 전송
    public void sendMessage(GameMessage message) throws IOException {
        // TODO: 나중에 구현
        // out.writeObject(message)
    }
    
    //내 IP 주소 가져오기 (서버용)
    public String getLocalIP() throws UnknownHostException {
        // TODO: 나중에 구현
        return InetAddress.getLocalHost().getHostAddress();
    }
    
    //연결 종료
    public void disconnect() {
        // TODO: 나중에 구현
        // 소켓 닫기
        // 스레드 종료
    }
    
    //리스너 등록
    public void setMessageListener(MessageListener listener) {
        this.listener = listener;
    }
    
    //연결 상태 확인
    public boolean isConnected() {
        return isConnected;
    }
    
    // Private 메서드들
    private void initStreams() throws IOException {
        // ObjectOutputStream 먼저!
        out = new ObjectOutputStream(socket.getOutputStream());
        out.flush();
        in = new ObjectInputStream(socket.getInputStream());
    }
    
    private void startReceiving() {
        receiveThread = new Thread(() -> {
            while (isConnected) {
                try {
                    GameMessage msg = (GameMessage) in.readObject();
                    if (listener != null) {
                        listener.onMessageReceived(msg);
                    }
                } catch (Exception e) {
                    if (isConnected && listener != null) {
                        listener.onConnectionLost(e);
                    }
                    break;
                }
            }
        });
        receiveThread.start();
    }
}