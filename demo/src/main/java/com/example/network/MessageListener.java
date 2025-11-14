package com.example.network;

public interface MessageListener {
    //메시지를 받았을 때 호출
    void onMessageReceived(GameMessage message);
    
    //연결이 끊어졌을 때 호출
    void onConnectionLost(Exception e);
    
    //연결 상태가 나빠졌을 때 (지연) 호출
    void onNetworkDelay(long pingMs);
}