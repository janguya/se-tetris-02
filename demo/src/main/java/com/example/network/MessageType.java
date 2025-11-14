package com.example.network;

public enum MessageType {
    // 연결 단계
    CONNECT,        // 클라이언트 접속
    READY,          // 준비 완료
    
    // 게임 설정
    MODE_SELECT,    // 서버가 모드 선택 (일반/아이템/시간제한)
    START_GAME,     // 게임 시작 신호
    
    // 게임 진행
    GAME_STATE,     // 게임 상태 (보드, 블록 위치 등)
    ATTACK,         // 공격 (방해 줄)
    
    // 종료
    GAME_OVER,      // 게임 오버
    BACK_TO_LOBBY,  // 대기실로 복귀
    
    // 네트워크 관리
    PING,           // 핑 측정용
    PONG,           // 핑 응답
    DISCONNECT      // 연결 종료
}