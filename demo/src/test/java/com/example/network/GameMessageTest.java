package com.example.network;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

/**
 * GameMessage 클래스의 단위 테스트
 */
public class GameMessageTest {
    
    private GameMessage message;
    private static final String TEST_SENDER = "player1";
    
    @BeforeEach
    public void setUp() {
        message = new GameMessage(MessageType.PING, TEST_SENDER);
    }
    
    @Test
    public void testConstructor() {
        assertEquals(MessageType.PING, message.getType(), "타입이 올바르게 설정되어야 함");
        assertEquals(TEST_SENDER, message.getSenderId(), "발신자 ID가 올바르게 설정되어야 함");
        assertTrue(message.getTimestamp() > 0, "타임스탬프가 설정되어야 함");
        assertNotNull(message.getData(), "데이터 맵이 초기화되어야 함");
    }
    
    @Test
    public void testPutAndGet() {
        message.put("key1", "value1");
        assertEquals("value1", message.get("key1"), "put/get이 정상 작동해야 함");
    }
    
    @Test
    public void testPutChaining() {
        GameMessage result = message.put("key1", "value1").put("key2", 123);
        assertEquals(message, result, "put은 체이닝을 지원해야 함");
        assertEquals("value1", message.get("key1"));
        assertEquals(123, message.get("key2"));
    }
    
    @Test
    public void testGetString() {
        message.put("strKey", "testValue");
        assertEquals("testValue", message.getString("strKey"), "getString이 정상 작동해야 함");
    }
    
    @Test
    public void testGetStringWithNull() {
        assertNull(message.getString("nonExistentKey"), "존재하지 않는 키는 null 반환");
    }
    
    @Test
    public void testGetInt() {
        message.put("intKey", 42);
        assertEquals(42, message.getInt("intKey", 0), "getInt가 정상 작동해야 함");
    }
    
    @Test
    public void testGetIntWithDefault() {
        assertEquals(100, message.getInt("nonExistentKey", 100), 
            "존재하지 않는 키는 기본값 반환");
    }
    
    @Test
    public void testGetIntWithWrongType() {
        message.put("wrongType", "notAnInteger");
        assertEquals(50, message.getInt("wrongType", 50), 
            "잘못된 타입은 기본값 반환");
    }
    
    @Test
    public void testGetBoolean() {
        message.put("boolKey", true);
        assertTrue(message.getBoolean("boolKey", false), "getBoolean이 정상 작동해야 함");
    }
    
    @Test
    public void testGetBooleanWithDefault() {
        assertFalse(message.getBoolean("nonExistentKey", false), 
            "존재하지 않는 키는 기본값 반환");
    }
    
    @Test
    public void testGetBooleanWithWrongType() {
        message.put("wrongType", "notABoolean");
        assertTrue(message.getBoolean("wrongType", true), 
            "잘못된 타입은 기본값 반환");
    }
    
    @Test
    public void testHas() {
        message.put("existingKey", "value");
        assertTrue(message.has("existingKey"), "has는 존재하는 키에 대해 true 반환");
        assertFalse(message.has("nonExistentKey"), "has는 존재하지 않는 키에 대해 false 반환");
    }
    
    @Test
    public void testGetData() {
        message.put("key1", "value1");
        message.put("key2", 123);
        
        Map<String, Object> data = message.getData();
        assertEquals(2, data.size(), "getData는 모든 데이터를 반환해야 함");
        assertEquals("value1", data.get("key1"));
        assertEquals(123, data.get("key2"));
        
        // 반환된 맵이 복사본인지 확인
        data.put("key3", "value3");
        assertFalse(message.has("key3"), "getData는 복사본을 반환해야 함");
    }
    
    @Test
    public void testGetLatency() throws InterruptedException {
        long latency1 = message.getLatency();
        assertTrue(latency1 >= 0, "레이턴시는 0 이상이어야 함");
        
        Thread.sleep(50); // 50ms 대기
        
        long latency2 = message.getLatency();
        assertTrue(latency2 >= latency1, "시간이 지나면 레이턴시가 증가해야 함");
        assertTrue(latency2 >= 50, "최소 50ms의 레이턴시가 있어야 함");
    }
    
    @Test
    public void testIsUrgent() {
        GameMessage urgentMsg = new GameMessage(MessageType.PING, TEST_SENDER);
        assertTrue(urgentMsg.isUrgent(), "PING은 긴급 메시지");
        
        GameMessage normalMsg = new GameMessage(MessageType.LOBBY_CREATE, TEST_SENDER);
        assertFalse(normalMsg.isUrgent(), "LOBBY_CREATE는 긴급하지 않음");
    }
    
    @Test
    public void testToString() {
        message.put("testKey", "testValue");
        String str = message.toString();
        
        assertNotNull(str, "toString은 null이 아니어야 함");
        assertTrue(str.contains("PING"), "타입 정보 포함");
        assertTrue(str.contains(TEST_SENDER), "발신자 정보 포함");
        assertTrue(str.contains("testKey"), "데이터 정보 포함");
    }
    
    @Test
    public void testCreatePing() {
        GameMessage ping = GameMessage.createPing(TEST_SENDER);
        assertEquals(MessageType.PING, ping.getType());
        assertEquals(TEST_SENDER, ping.getSenderId());
    }
    
    @Test
    public void testCreatePong() {
        GameMessage pong = GameMessage.createPong(TEST_SENDER);
        assertEquals(MessageType.PONG, pong.getType());
        assertEquals(TEST_SENDER, pong.getSenderId());
    }
    
    @Test
    public void testCreateBlockMove() {
        GameMessage move = GameMessage.createBlockMove(TEST_SENDER, "left", 5, 10);
        assertEquals(MessageType.BLOCK_MOVE, move.getType());
        assertEquals("left", move.getString("direction"));
        assertEquals(5, move.getInt("x", 0));
        assertEquals(10, move.getInt("y", 0));
    }
    
    @Test
    public void testCreateBlockDrop() {
        GameMessage drop = GameMessage.createBlockDrop(TEST_SENDER, 18);
        assertEquals(MessageType.BLOCK_DROP, drop.getType());
        assertEquals(18, drop.getInt("finalY", 0));
    }
    
    @Test
    public void testCreateAttack() {
        GameMessage attack = GameMessage.createAttack(TEST_SENDER, 3, 999);
        assertEquals(MessageType.ATTACK, attack.getType());
        assertEquals(3, attack.getInt("linesCleared", 0));
        assertEquals(999, attack.getInt("attackLines", 0));
    }
    
    @Test
    public void testCreateGameOver() {
        GameMessage gameOver = GameMessage.createGameOver(TEST_SENDER, 5000);
        assertEquals(MessageType.GAME_OVER, gameOver.getType());
        assertEquals(5000, gameOver.getInt("finalScore", 0));
    }
    
    @Test
    public void testCreateError() {
        GameMessage error = GameMessage.createError(TEST_SENDER, "ERR001", "Test error");
        assertEquals(MessageType.ERROR, error.getType());
        assertEquals("ERR001", error.getString("errorCode"));
        assertEquals("Test error", error.getString("errorMessage"));
    }
    
    @Test
    public void testMultipleDataTypes() {
        message.put("string", "value")
               .put("int", 42)
               .put("boolean", true)
               .put("double", 3.14);
        
        assertEquals("value", message.getString("string"));
        assertEquals(42, message.getInt("int", 0));
        assertTrue(message.getBoolean("boolean", false));
        assertEquals(3.14, (Double) message.get("double"), 0.001);
    }
    
    @Test
    public void testTimestampIsSet() {
        long before = System.currentTimeMillis();
        GameMessage msg = new GameMessage(MessageType.GAME_START, TEST_SENDER);
        long after = System.currentTimeMillis();
        
        assertTrue(msg.getTimestamp() >= before && msg.getTimestamp() <= after,
            "타임스탬프가 생성 시간에 설정되어야 함");
    }
}
