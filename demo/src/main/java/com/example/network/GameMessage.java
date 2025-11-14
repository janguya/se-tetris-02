package com.example.network;

import java.io.Serializable;

public class GameMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private MessageType type;           // 메시지 종류
    private long timestamp;             // 전송 시간 (지연 측정용)
    
    // 게임 상태 전송용
    private int[][] boardState;         // 보드 상태
    private int currentBlockX;          // 현재 블록 X
    private int currentBlockY;          // 현재 블록 Y
    private String blockCssClass;       // 블록 타입
    private int score;                  // 점수
    
    // 공격 전송용
    private int attackLines;            // 공격할 줄 수
    
    // 게임 설정용
    private String gameMode;            // "NORMAL", "ITEM", "TIME_LIMIT"
    
    // 연결 관리용
    private String playerId;            // 플레이어 구분
    
    // 생성자
    public GameMessage(MessageType type) {
        this.type = type;
        this.timestamp = System.currentTimeMillis();
    }
    
    // Getters & Setters
    // 나중에 추가
}