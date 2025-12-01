package com.example.network;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * MessageType 추가 테스트
 */
public class MessageTypeExtraTest {

    @Test
    @DisplayName("isUrgent 메서드 - urgent 타입")
    public void testIsUrgentTrue() {
        assertTrue(MessageType.PING.isUrgent());
        assertTrue(MessageType.PONG.isUrgent());
        assertTrue(MessageType.BLOCK_MOVE.isUrgent());
        assertTrue(MessageType.BLOCK_ROTATE.isUrgent());
        assertTrue(MessageType.BLOCK_DROP.isUrgent());
        assertTrue(MessageType.ATTACK.isUrgent());
    }

    @Test
    @DisplayName("isUrgent 메서드 - non-urgent 타입")
    public void testIsUrgentFalse() {
        assertFalse(MessageType.GAME_START.isUrgent());
        assertFalse(MessageType.GAME_OVER.isUrgent());
        assertFalse(MessageType.LOBBY_CREATE.isUrgent());
        assertFalse(MessageType.CONNECT_REQUEST.isUrgent());
        assertFalse(MessageType.GAME_STATE.isUrgent());
    }

    @Test
    @DisplayName("isGameplayMessage 메서드 - gameplay 타입")
    public void testIsGameplayMessageTrue() {
        assertTrue(MessageType.BLOCK_SPAWN.isGameplayMessage());
        assertTrue(MessageType.BLOCK_MOVE.isGameplayMessage());
        assertTrue(MessageType.BLOCK_ROTATE.isGameplayMessage());
        assertTrue(MessageType.BLOCK_DROP.isGameplayMessage());
        assertTrue(MessageType.BLOCK_LAND.isGameplayMessage());
        assertTrue(MessageType.LINE_CLEAR.isGameplayMessage());
        assertTrue(MessageType.ATTACK.isGameplayMessage());
        assertTrue(MessageType.SCORE_UPDATE.isGameplayMessage());
    }

    @Test
    @DisplayName("isGameplayMessage 메서드 - non-gameplay 타입")
    public void testIsGameplayMessageFalse() {
        assertFalse(MessageType.GAME_START.isGameplayMessage());
        assertFalse(MessageType.PING.isGameplayMessage());
        assertFalse(MessageType.LOBBY_CREATE.isGameplayMessage());
        assertFalse(MessageType.CONNECT_REQUEST.isGameplayMessage());
        assertFalse(MessageType.DISCONNECT.isGameplayMessage());
    }

    @Test
    @DisplayName("연결 관련 MessageType")
    public void testConnectionTypes() {
        assertNotNull(MessageType.PING);
        assertNotNull(MessageType.PONG);
        assertNotNull(MessageType.CONNECT_REQUEST);
        assertNotNull(MessageType.CONNECT_RESPONSE);
        assertNotNull(MessageType.DISCONNECT);
    }

    @Test
    @DisplayName("로비 관련 MessageType")
    public void testLobbyTypes() {
        assertNotNull(MessageType.LOBBY_CREATE);
        assertNotNull(MessageType.LOBBY_JOIN);
        assertNotNull(MessageType.LOBBY_LEAVE);
        assertNotNull(MessageType.LOBBY_LIST);
        assertNotNull(MessageType.LOBBY_INFO);
    }

    @Test
    @DisplayName("게임 시작/종료 MessageType")
    public void testGameControlTypes() {
        assertNotNull(MessageType.GAME_START);
        assertNotNull(MessageType.PLAYER_READY);
        assertNotNull(MessageType.GAME_READY);
        assertNotNull(MessageType.GAME_OVER);
        assertNotNull(MessageType.GAME_PAUSE);
        assertNotNull(MessageType.GAME_RESUME);
    }

    @Test
    @DisplayName("게임 상태 동기화 MessageType")
    public void testGameStateTypes() {
        assertNotNull(MessageType.GAME_STATE);
        assertNotNull(MessageType.BLOCK_SPAWN);
        assertNotNull(MessageType.BLOCK_MOVE);
        assertNotNull(MessageType.BLOCK_ROTATE);
        assertNotNull(MessageType.BLOCK_DROP);
        assertNotNull(MessageType.BLOCK_LAND);
    }

    @Test
    @DisplayName("게임 이벤트 MessageType")
    public void testGameEventTypes() {
        assertNotNull(MessageType.LINE_CLEAR);
        assertNotNull(MessageType.ATTACK);
        assertNotNull(MessageType.ATTACK_RECEIVED);
        assertNotNull(MessageType.SCORE_UPDATE);
    }

    @Test
    @DisplayName("에러 처리 MessageType")
    public void testErrorTypes() {
        assertNotNull(MessageType.ERROR);
        assertNotNull(MessageType.SYNC_REQUEST);
        assertNotNull(MessageType.SYNC_RESPONSE);
    }

    @Test
    @DisplayName("모든 MessageType enum 값 존재 확인")
    public void testAllEnumValues() {
        MessageType[] types = MessageType.values();
        assertTrue(types.length > 0);
        
        // 최소한 기본 타입들이 포함되어야 함
        boolean hasPing = false;
        boolean hasGameStart = false;
        boolean hasBlockMove = false;
        
        for (MessageType type : types) {
            if (type == MessageType.PING) hasPing = true;
            if (type == MessageType.GAME_START) hasGameStart = true;
            if (type == MessageType.BLOCK_MOVE) hasBlockMove = true;
        }
        
        assertTrue(hasPing);
        assertTrue(hasGameStart);
        assertTrue(hasBlockMove);
    }

    @Test
    @DisplayName("MessageType valueOf 테스트")
    public void testValueOf() {
        assertEquals(MessageType.PING, MessageType.valueOf("PING"));
        assertEquals(MessageType.GAME_START, MessageType.valueOf("GAME_START"));
        assertEquals(MessageType.BLOCK_MOVE, MessageType.valueOf("BLOCK_MOVE"));
    }

    @Test
    @DisplayName("urgent와 gameplay 속성 조합 테스트")
    public void testUrgentAndGameplay() {
        // BLOCK_MOVE는 urgent이면서 gameplay
        assertTrue(MessageType.BLOCK_MOVE.isUrgent());
        assertTrue(MessageType.BLOCK_MOVE.isGameplayMessage());
        
        // BLOCK_SPAWN은 gameplay지만 urgent 아님
        assertFalse(MessageType.BLOCK_SPAWN.isUrgent());
        assertTrue(MessageType.BLOCK_SPAWN.isGameplayMessage());
        
        // PING은 urgent지만 gameplay 아님
        assertTrue(MessageType.PING.isUrgent());
        assertFalse(MessageType.PING.isGameplayMessage());
    }
}
