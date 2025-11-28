package com.example.network;

public interface MessageListener {
    //메시지를 받았을 때 호출
    void onMessageReceived(GameMessage message);
    
    // 연결 성공 시 호출
    // @param peerId 연결된 상대방 ID
    void onConnected(String peerId);

    // 연결 끊김 시 호출
    // @param peerId 끊긴 상대방 ID
    // @param reason 끊긴 이유
    void onDisconnected(String peerId, String reason);
    
    // 에러 발생 시 호출
    // @param errorMessage 에러 메시지
    // @param exception 발생한 예외
    void onError(String errorMessage, Exception exception);

    // 레이턴시 업데이트 시 호출
    // @param latency 밀리초 단위 레이턴시
    default void onLatencyUpdate(long latencyMs) {
        // 기본 구현: 로깅만
        if(latencyMs > 200){
            System.out.println("Warning: High latency detected - " + latencyMs + " ms");
        }
    }

}