package com.example.network;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

/**
 * GameMessage 추가 테스트
 */
public class GameMessageExtraTest {

    @Test
    @DisplayName("GameMessage 기본 생성")
    public void testBasicCreation() {
        GameMessage msg = new GameMessage(MessageType.GAME_START, "Player1");
        assertNotNull(msg);
        assertEquals(MessageType.GAME_START, msg.getType());
        assertEquals("Player1", msg.getSenderId());
    }

    @Test
    @DisplayName("put/get 메서드 테스트")
    public void testPutGet() {
        GameMessage msg = new GameMessage(MessageType.BLOCK_MOVE, "Player1");
        msg.put("x", 5);
        msg.put("y", 10);
        msg.put("direction", "left");
        
        assertEquals(5, msg.get("x"));
        assertEquals(10, msg.get("y"));
        assertEquals("left", msg.get("direction"));
    }

    @Test
    @DisplayName("getString 메서드 테스트")
    public void testGetString() {
        GameMessage msg = new GameMessage(MessageType.ATTACK, "Player1");
        msg.put("attackType", "double");
        
        assertEquals("double", msg.getString("attackType"));
        assertNull(msg.getString("nonexistent"));
    }

    @Test
    @DisplayName("getInt 메서드 테스트")
    public void testGetInt() {
        GameMessage msg = new GameMessage(MessageType.SCORE_UPDATE, "Player1");
        msg.put("score", 1000);
        msg.put("level", 5);
        
        assertEquals(1000, msg.getInt("score", 0));
        assertEquals(5, msg.getInt("level", 1));
        assertEquals(99, msg.getInt("nonexistent", 99));
    }

    @Test
    @DisplayName("getBoolean 메서드 테스트")
    public void testGetBoolean() {
        GameMessage msg = new GameMessage(MessageType.GAME_READY, "Player1");
        msg.put("isReady", true);
        msg.put("hasPowerup", false);
        
        assertTrue(msg.getBoolean("isReady", false));
        assertFalse(msg.getBoolean("hasPowerup", true));
        assertTrue(msg.getBoolean("nonexistent", true));
    }

    @Test
    @DisplayName("has 메서드 테스트")
    public void testHas() {
        GameMessage msg = new GameMessage(MessageType.BLOCK_DROP, "Player1");
        msg.put("finalY", 20);
        
        assertTrue(msg.has("finalY"));
        assertFalse(msg.has("nonexistent"));
    }

    @Test
    @DisplayName("timestamp 검증")
    public void testTimestamp() throws Exception {
        long before = System.currentTimeMillis();
        GameMessage msg = new GameMessage(MessageType.PING, "Player1");
        long after = System.currentTimeMillis();
        
        assertTrue(msg.getTimestamp() >= before);
        assertTrue(msg.getTimestamp() <= after);
    }

    @Test
    @DisplayName("getLatency 테스트")
    public void testLatency() throws Exception {
        GameMessage msg = new GameMessage(MessageType.PONG, "Player1");
        Thread.sleep(50);
        
        long latency = msg.getLatency();
        assertTrue(latency >= 50, "Latency should be at least 50ms");
    }

    @Test
    @DisplayName("isUrgent 테스트")
    public void testIsUrgent() {
        GameMessage ping = new GameMessage(MessageType.PING, "Player1");
        assertTrue(ping.isUrgent());
        
        GameMessage move = new GameMessage(MessageType.BLOCK_MOVE, "Player1");
        assertTrue(move.isUrgent());
        
        GameMessage gameOver = new GameMessage(MessageType.GAME_OVER, "Player1");
        assertFalse(gameOver.isUrgent());
    }

    @Test
    @DisplayName("toString 테스트")
    public void testToString() {
        GameMessage msg = new GameMessage(MessageType.ATTACK, "Player1");
        msg.put("damage", 10);
        
        String str = msg.toString();
        assertTrue(str.contains("ATTACK"));
        assertTrue(str.contains("Player1"));
    }

    @Test
    @DisplayName("getData 불변성 테스트")
    public void testGetDataImmutability() {
        GameMessage msg = new GameMessage(MessageType.GAME_STATE, "Player1");
        msg.put("score", 500);
        msg.put("level", 3);
        
        Map<String, Object> data = msg.getData();
        assertEquals(2, data.size());
        assertEquals(500, data.get("score"));
        assertEquals(3, data.get("level"));
        
        // getData는 복사본을 반환해야 함
        data.put("newKey", "newValue");
        assertFalse(msg.has("newKey"));
    }

    @Test
    @DisplayName("createPing 팩토리 메서드")
    public void testCreatePing() {
        GameMessage ping = GameMessage.createPing("Player1");
        assertNotNull(ping);
        assertEquals(MessageType.PING, ping.getType());
        assertEquals("Player1", ping.getSenderId());
    }

    @Test
    @DisplayName("createPong 팩토리 메서드")
    public void testCreatePong() {
        GameMessage pong = GameMessage.createPong("Player2");
        assertNotNull(pong);
        assertEquals(MessageType.PONG, pong.getType());
        assertEquals("Player2", pong.getSenderId());
    }

    @Test
    @DisplayName("createBlockMove 팩토리 메서드")
    public void testCreateBlockMove() {
        GameMessage move = GameMessage.createBlockMove("Player1", "left", 3, 5);
        assertNotNull(move);
        assertEquals(MessageType.BLOCK_MOVE, move.getType());
        assertEquals("left", move.getString("direction"));
        assertEquals(3, move.getInt("x", 0));
        assertEquals(5, move.getInt("y", 0));
    }

    @Test
    @DisplayName("createBlockDrop 팩토리 메서드")
    public void testCreateBlockDrop() {
        GameMessage drop = GameMessage.createBlockDrop("Player1", 20);
        assertNotNull(drop);
        assertEquals(MessageType.BLOCK_DROP, drop.getType());
        assertEquals(20, drop.getInt("finalY", 0));
    }

    @Test
    @DisplayName("createAttack 팩토리 메서드")
    public void testCreateAttack() {
        GameMessage attack = GameMessage.createAttack("Player1", 2, 123);
        assertNotNull(attack);
        assertEquals(MessageType.ATTACK, attack.getType());
        assertEquals(2, attack.getInt("linesCleared", 0));
        assertEquals(123, attack.getInt("attackLines", 0));
    }

    @Test
    @DisplayName("createGameOver 팩토리 메서드")
    public void testCreateGameOver() {
        GameMessage gameOver = GameMessage.createGameOver("Player1", 5000);
        assertNotNull(gameOver);
        assertEquals(MessageType.GAME_OVER, gameOver.getType());
        assertEquals(5000, gameOver.getInt("finalScore", 0));
    }

    @Test
    @DisplayName("createError 팩토리 메서드")
    public void testCreateError() {
        GameMessage error = GameMessage.createError("Player1", "ERR_001", "Connection failed");
        assertNotNull(error);
        assertEquals(MessageType.ERROR, error.getType());
        assertEquals("ERR_001", error.getString("errorCode"));
        assertEquals("Connection failed", error.getString("errorMessage"));
    }

    @Test
    @DisplayName("체이닝 테스트")
    public void testChaining() {
        GameMessage msg = new GameMessage(MessageType.ATTACK, "Player1")
                .put("lines", 2)
                .put("combo", 3);
        
        assertEquals(2, msg.getInt("lines", 0));
        assertEquals(3, msg.getInt("combo", 0));
    }

    @Test
    @DisplayName("다양한 타입의 데이터 저장")
    public void testVariousDataTypes() {
        GameMessage msg = new GameMessage(MessageType.GAME_STATE, "Player1");
        msg.put("intValue", 42);
        msg.put("stringValue", "test");
        msg.put("boolValue", true);
        msg.put("doubleValue", 3.14);
        
        assertEquals(42, msg.get("intValue"));
        assertEquals("test", msg.get("stringValue"));
        assertEquals(true, msg.get("boolValue"));
        assertEquals(3.14, msg.get("doubleValue"));
    }

    @Test
    @DisplayName("빈 SenderId 테스트")
    public void testEmptySenderId() {
        GameMessage msg = new GameMessage(MessageType.GAME_START, "");
        assertNotNull(msg);
        assertEquals("", msg.getSenderId());
    }

    @Test
    @DisplayName("null 값 put 테스트")
    public void testPutNullValue() {
        GameMessage msg = new GameMessage(MessageType.GAME_STATE, "Player1");
        msg.put("nullValue", null);
        
        assertTrue(msg.has("nullValue"));
        assertNull(msg.get("nullValue"));
    }

    @Test
    @DisplayName("동일한 키에 여러 번 put")
    public void testOverwriteValue() {
        GameMessage msg = new GameMessage(MessageType.SCORE_UPDATE, "Player1");
        msg.put("score", 100);
        msg.put("score", 200);
        
        assertEquals(200, msg.getInt("score", 0));
    }
}
