package com.example.network;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * MessageType 열거형의 단위 테스트
 */
public class MessageTypeTest {
    
    @Test
    public void testIsUrgentForUrgentMessages() {
        assertTrue(MessageType.PING.isUrgent(), "PING은 긴급 메시지");
        assertTrue(MessageType.PONG.isUrgent(), "PONG은 긴급 메시지");
        assertTrue(MessageType.BLOCK_MOVE.isUrgent(), "BLOCK_MOVE는 긴급 메시지");
        assertTrue(MessageType.BLOCK_ROTATE.isUrgent(), "BLOCK_ROTATE는 긴급 메시지");
        assertTrue(MessageType.BLOCK_DROP.isUrgent(), "BLOCK_DROP은 긴급 메시지");
        assertTrue(MessageType.ATTACK.isUrgent(), "ATTACK은 긴급 메시지");
    }
    
    @Test
    public void testIsUrgentForNonUrgentMessages() {
        assertFalse(MessageType.CONNECT_REQUEST.isUrgent(), "CONNECT_REQUEST는 긴급하지 않음");
        assertFalse(MessageType.LOBBY_CREATE.isUrgent(), "LOBBY_CREATE는 긴급하지 않음");
        assertFalse(MessageType.GAME_START.isUrgent(), "GAME_START는 긴급하지 않음");
        assertFalse(MessageType.GAME_OVER.isUrgent(), "GAME_OVER는 긴급하지 않음");
        assertFalse(MessageType.ERROR.isUrgent(), "ERROR는 긴급하지 않음");
    }
    
    @Test
    public void testIsGameplayMessageForGameplayMessages() {
        assertTrue(MessageType.BLOCK_SPAWN.isGameplayMessage(), "BLOCK_SPAWN은 게임플레이 메시지");
        assertTrue(MessageType.BLOCK_MOVE.isGameplayMessage(), "BLOCK_MOVE는 게임플레이 메시지");
        assertTrue(MessageType.BLOCK_ROTATE.isGameplayMessage(), "BLOCK_ROTATE는 게임플레이 메시지");
        assertTrue(MessageType.BLOCK_DROP.isGameplayMessage(), "BLOCK_DROP은 게임플레이 메시지");
        assertTrue(MessageType.BLOCK_LAND.isGameplayMessage(), "BLOCK_LAND는 게임플레이 메시지");
        assertTrue(MessageType.LINE_CLEAR.isGameplayMessage(), "LINE_CLEAR는 게임플레이 메시지");
        assertTrue(MessageType.ATTACK.isGameplayMessage(), "ATTACK은 게임플레이 메시지");
        assertTrue(MessageType.SCORE_UPDATE.isGameplayMessage(), "SCORE_UPDATE는 게임플레이 메시지");
    }
    
    @Test
    public void testIsGameplayMessageForNonGameplayMessages() {
        assertFalse(MessageType.PING.isGameplayMessage(), "PING은 게임플레이 메시지가 아님");
        assertFalse(MessageType.CONNECT_REQUEST.isGameplayMessage(), "CONNECT_REQUEST는 게임플레이 메시지가 아님");
        assertFalse(MessageType.LOBBY_CREATE.isGameplayMessage(), "LOBBY_CREATE는 게임플레이 메시지가 아님");
        assertFalse(MessageType.GAME_START.isGameplayMessage(), "GAME_START는 게임플레이 메시지가 아님");
        assertFalse(MessageType.GAME_OVER.isGameplayMessage(), "GAME_OVER는 게임플레이 메시지가 아님");
    }
    
    @Test
    public void testAllMessageTypesHaveUniqueName() {
        MessageType[] types = MessageType.values();
        for (int i = 0; i < types.length; i++) {
            for (int j = i + 1; j < types.length; j++) {
                assertNotEquals(types[i], types[j], 
                    "모든 MessageType은 고유해야 함");
            }
        }
    }
    
    @Test
    public void testMessageTypeCount() {
        MessageType[] types = MessageType.values();
        assertTrue(types.length > 0, "최소 하나 이상의 MessageType이 있어야 함");
        // MessageType 개수는 변경될 수 있으므로 최소 개수만 확인
        assertTrue(types.length >= 27, "MessageType은 최소 27개 이상이어야 함");
    }
    
    @Test
    public void testValueOf() {
        assertEquals(MessageType.PING, MessageType.valueOf("PING"));
        assertEquals(MessageType.BLOCK_MOVE, MessageType.valueOf("BLOCK_MOVE"));
        assertEquals(MessageType.GAME_OVER, MessageType.valueOf("GAME_OVER"));
    }
    
    @Test
    public void testValueOfInvalidName() {
        assertThrows(IllegalArgumentException.class, () -> {
            MessageType.valueOf("INVALID_MESSAGE_TYPE");
        }, "존재하지 않는 MessageType에 대해 예외 발생");
    }
}
