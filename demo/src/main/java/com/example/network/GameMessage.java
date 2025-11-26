package com.example.network;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class GameMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private final MessageType type;
    private final long timestamp;
    private final String senderId;
    private final Map<String, Object> data;

    // 메세지 생성자
    // @param type 메세지 타입
    // @param senderId 보낸 사람 ID
    public GameMessage(MessageType type, String senderId) {
        this.type = type;
        this.senderId = senderId;
        this.timestamp = System.currentTimeMillis();
        this.data = new HashMap<>();
    }
    
    // 데이터 추가
    // @param key 데이터 키
    // @param value 데이터 값
    public GameMessage put(String key, Object value) {
        data.put(key, value);
        return this;
    }

    // 데이터 조회
    // @param key 데이터 키
    // @return 데이터 값
    public Object get(String key) {
        return data.get(key);
    }

    // String 데이터 조회
    // @param key 데이터 키
    // @return String 값
    public String getString(String key) {
        Object value = data.get(key);
        return value != null ? value.toString() : null;
    }

    // int 데이터 조회
    // @param defaultValue 기본값
    // @return int 값
    public int getInt(String key, int defaultValue) {
        Object value = data.get(key);
        if (value instanceof Integer) {
            return (Integer) value;
        }
        return defaultValue;
    }

    // boolean 데이터 조회
    // @param key 데이터 키
    // @param defaultValue 기본값
    // @return boolean 값
    public boolean getBoolean(String key, boolean defaultValue) {
        Object value = data.get(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return defaultValue;
    }

    // 데이터 존재 확인
    // @param key 데이터 키
    // @return 존재 여부
    public boolean has(String key) {
        return data.containsKey(key);
    }

    // Getter 메서드들
    public MessageType getType() {
        return type;
    }
    public long getTimestamp() {
        return timestamp;
    }
    public String getSenderId() {
        return senderId;
    }
    public Map<String, Object> getData() {
        return new HashMap<>(data);
    }

    // 메세지 레이턴시 계산
    // @return 레이턴시 (밀리초)
    public long getLatency() {
        return System.currentTimeMillis() - timestamp;
    }

    // 긴급 메시지 여부 확인
    // @return 긴급 메시지 여부
    public boolean isUrgent() {
        return type.isUrgent();
    }

    @Override
    public String toString() {
        return String.format("GameMessage{type=%s, sender=%s, latency=%dms, data=%s}", 
                            type, senderId, getLatency(), data);
    }

    // Ping 메시지 생성
    public static GameMessage createPing(String senderId) {
        return new GameMessage(MessageType.PING, senderId);
    }

    // Pong 메시지 생성
    public static GameMessage createPong(String senderId) {
        return new GameMessage(MessageType.PONG, senderId);
    }

    // 블록 이동 메시지 생성
    // @param direction "left", "right", "down"

    public static GameMessage createBlockMove(String senderId, String direction, int x, int y) {
        return new GameMessage(MessageType.BLOCK_MOVE, senderId)
                .put("direction", direction)
                .put("x",x)
                .put("y",y);
    }

    // 하드 드롭 메시지 생성
    public static GameMessage createBlockDrop(String senderId, int finalY) {
        return new GameMessage(MessageType.BLOCK_DROP, senderId)
                .put("finalY", finalY);
    }

    // 공격 메시지 생성
    // @param LinesCleared 제거된 줄 수
    // @param attackLines 공격 라인 데이터
    public static GameMessage createAttack(String senderId, int linesCleared, int attackData) {
        return new GameMessage(MessageType.ATTACK, senderId)
                .put("linesCleared", linesCleared)
                .put("attackLines", attackData);
    }

    // 게임 오버 메시지 생성
    public static GameMessage createGameOver(String senderId, int finalScore) {
        return new GameMessage(MessageType.GAME_OVER, senderId)
                .put("finalScore", finalScore);
    }

    // 에러 메시지 생성
    public static GameMessage createError(String senderId, String errorCode, String errorMessage) {
        return new GameMessage(MessageType.ERROR, senderId)
                .put("errorCode", errorCode)
                .put("errorMessage", errorMessage);
    }
}