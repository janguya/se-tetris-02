package com.example.game.ai;

import com.example.game.ai.TetrisAI.Move;
import com.example.game.blocks.Block;
import com.example.game.component.PlayerBoard;

/**
 * AI 플레이어 - 사람처럼 블록을 조작하는 AI
 * 입력 딜레이와 애니메이션을 추가하여 자연스럽게 보이도록 함
 */
public class AIPlayer {
    
    private final PlayerBoard board;
    private Move currentMove;
    private AIState state;
    private long lastActionTime;
    
    // AI 설정
    private static final long THINK_DELAY = 300; // 생각하는 시간 (ms)
    private static final long MOVE_DELAY = 50;   // 이동 간 딜레이 (ms)
    private static final long ROTATE_DELAY = 80; // 회전 딜레이 (ms)
    private static final long DROP_DELAY = 100;  // 드롭 전 대기 (ms)
    
    private int currentX;
    private int currentRotation;
    private int targetX;
    private int targetRotation;
    
    private enum AIState {
        THINKING,    // AI가 최적의 수를 계산 중
        ROTATING,    // 목표 회전까지 회전 중
        MOVING,      // 목표 x 위치로 이동 중
        DROPPING,    // 하드 드롭 준비
        IDLE         // 다음 블록 대기
    }
    
    public AIPlayer(PlayerBoard board) {
        this.board = board;
        this.state = AIState.IDLE;
        this.lastActionTime = System.currentTimeMillis();
    }
    
    /**
     * AI 업데이트 - 매 프레임마다 호출
     */
    public void update() {
        if (board.isGameOver()) {
            return;
        }
        
        long currentTime = System.currentTimeMillis();
        
        switch (state) {
            case IDLE:
                // 새 블록이 생성되면 다음 수 계산 시작
                if (board.getCurrentBlock() != null) {
                    state = AIState.THINKING;
                    lastActionTime = currentTime;
                }
                break;
                
            case THINKING:
                // 생각하는 시간 경과 후 최적의 수 계산
                if (currentTime - lastActionTime >= THINK_DELAY) {
                    calculateBestMove();
                    if (currentMove != null) {
                        initializeMove();
                        state = AIState.ROTATING;
                        lastActionTime = currentTime;
                    } else {
                        state = AIState.IDLE;
                    }
                }
                break;
                
            case ROTATING:
                // 목표 회전까지 회전
                if (currentTime - lastActionTime >= ROTATE_DELAY) {
                    if (currentRotation < targetRotation) {
                        board.onRotate();
                        currentRotation++;
                        lastActionTime = currentTime;
                    } else {
                        state = AIState.MOVING;
                        lastActionTime = currentTime;
                    }
                }
                break;
                
            case MOVING:
                // 목표 x 위치로 이동
                if (currentTime - lastActionTime >= MOVE_DELAY) {
                    if (currentX < targetX) {
                        board.onMoveRight();
                        currentX++;
                        lastActionTime = currentTime;
                    } else if (currentX > targetX) {
                        board.onMoveLeft();
                        currentX--;
                        lastActionTime = currentTime;
                    } else {
                        state = AIState.DROPPING;
                        lastActionTime = currentTime;
                    }
                }
                break;
                
            case DROPPING:
                // 잠시 대기 후 하드 드롭
                if (currentTime - lastActionTime >= DROP_DELAY) {
                    board.onHardDrop();
                    state = AIState.IDLE;
                    currentMove = null;
                }
                break;
        }
    }
    
    /**
     * 최적의 수 계산
     */
    private void calculateBestMove() {
        // findBestMove는 블록을 4번 회전시키므로, 호출 전후 블록 상태가 동일해야 함
        // 하지만 안전을 위해 현재 블록의 초기 크기를 저장
        Block block = board.getCurrentBlock();
        if (block == null) {
            currentMove = null;
            return;
        }
        
        int initialWidth = block.width();
        int initialHeight = block.height();
        
        currentMove = TetrisAI.findBestMove(board.getGameLogic());
        
        if (currentMove != null) {
            System.out.println("[AI] Best move: " + currentMove);
            
            // 블록이 원래 상태로 돌아왔는지 확인
            if (block.width() != initialWidth || block.height() != initialHeight) {
                System.err.println("[ERROR] Block state changed after findBestMove!");
                System.err.println("Expected: " + initialWidth + "x" + initialHeight + 
                                 ", Got: " + block.width() + "x" + block.height());
                // 블록을 원래 상태로 복구 시도
                while (block.width() != initialWidth || block.height() != initialHeight) {
                    block.rotate();
                }
            }
        }
    }
    
    /**
     * 이동 초기화
     */
    private void initializeMove() {
        currentX = board.getGameLogic().getCurrentX();
        currentRotation = 0;
        targetX = currentMove.x;
        targetRotation = currentMove.rotation;
    }
    
    /**
     * AI 상태 리셋
     */
    public void reset() {
        state = AIState.IDLE;
        currentMove = null;
        lastActionTime = System.currentTimeMillis();
    }
    
    /**
     * AI 활성화 여부
     */
    public boolean isActive() {
        return state != AIState.IDLE;
    }
    
    /**
     * 현재 상태
     */
    public AIState getState() {
        return state;
    }
}
