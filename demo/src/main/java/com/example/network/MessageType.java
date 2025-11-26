package com.example.network;

public enum MessageType {
    // 연결 관련
    PING,                   // 연결 확인 (keepalive)
    PONG,                   // PING 응답
    CONNECT_REQUEST,        // 연결 요청
    CONNECT_RESPONSE,       // 연결 응답
    DISCONNECT,             // 연결 종료
    
    // 로비 관련
    LOBBY_CREATE,           // 방 생성
    LOBBY_JOIN,             // 방 참가
    LOBBY_LEAVE,            // 방 나가기
    LOBBY_LIST,             // 방 목록 요청
    LOBBY_INFO,             // 방 정보
    
    // 게임 시작/종료
    GAME_START,             // 게임 시작
    PLAYER_READY,
    GAME_READY,             // 준비 완료
    GAME_OVER,              // 게임 종료
    GAME_PAUSE,             // 일시정지
    GAME_RESUME,            // 재개
    
    // 게임 상태 동기화
    GAME_STATE,             // 전체 게임 상태
    BLOCK_SPAWN,            // 블록 생성
    BLOCK_MOVE,             // 블록 이동 (left, right, down)
    BLOCK_ROTATE,           // 블록 회전
    BLOCK_DROP,             // 하드 드롭
    BLOCK_LAND,             // 블록 착지
    
    // 게임 이벤트
    LINE_CLEAR,             // 줄 삭제
    ATTACK,                 // 공격 (상대방에게 줄 보내기)
    ATTACK_RECEIVED,        // 공격 받음 확인
    SCORE_UPDATE,           // 점수 업데이트
    
    // 에러 처리
    ERROR,                  // 에러 메시지
    SYNC_REQUEST,           // 동기화 요청 (연결이 불안정할 때)
    SYNC_RESPONSE;          // 동기화 응답
    
    //메시지가 즉시 전송되어야 하는지 확인
    //@return 긴급 메시지 여부
    public boolean isUrgent() {
        return this == PING || this == PONG || 
               this == BLOCK_MOVE || this == BLOCK_ROTATE || 
               this == BLOCK_DROP || this == ATTACK;
    }
    
    //메시지가 게임 플레이 중에만 유효한지 확인
    //@return 게임 중 메시지 여부
    public boolean isGameplayMessage() {
        return this == BLOCK_SPAWN || this == BLOCK_MOVE || 
               this == BLOCK_ROTATE || this == BLOCK_DROP || 
               this == BLOCK_LAND || this == LINE_CLEAR || 
               this == ATTACK || this == SCORE_UPDATE;
    }
}