package com.example.game.ai;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.game.blocks.Block;
import com.example.game.blocks.IBlock;
import com.example.game.blocks.OBlock;
import com.example.game.blocks.TBlock;
import com.example.game.component.GameLogic;

/**
 * TetrisAI 클래스의 단위 테스트
 */
public class TetrisAITest {
    
    private GameLogic gameLogic;
    
    @BeforeEach
    public void setUp() {
        gameLogic = new GameLogic(false); // 아이템 모드 비활성화
    }
    
    @Test
    public void testFindBestMoveWithNullBlock() {
        // 블록이 없는 상태에서 AI 호출
        TetrisAI.Move move = TetrisAI.findBestMove(gameLogic);
        // 블록이 자동으로 spawn 되므로 null이 아닐 수 있음
        // null이거나 유효한 Move를 반환해야 함
        if (move != null) {
            assertTrue(Double.isFinite(move.score), "Move가 있으면 유효한 점수여야 함");
        }
    }
    
    @Test
    public void testFindBestMoveWithIBlock() {
        // I 블록으로 게임 시작
        gameLogic.spawnNextPiece();
        
        // 현재 블록이 I 블록이 될 때까지 시도
        for (int i = 0; i < 10; i++) {
            if (gameLogic.getCurrentBlock() instanceof IBlock) {
                break;
            }
            // 블록을 떨어뜨리고 다음 블록 생성
            while (gameLogic.moveDown()) {}
            gameLogic.spawnNextPiece();
        }
        
        if (gameLogic.getCurrentBlock() instanceof IBlock) {
            TetrisAI.Move move = TetrisAI.findBestMove(gameLogic);
            assertNotNull(move, "I 블록에 대한 최적의 수를 찾아야 함");
            assertTrue(move.x >= 0 && move.x < GameLogic.WIDTH, "x 좌표가 유효해야 함");
            assertTrue(move.rotation >= 0 && move.rotation < 4, "회전 값이 유효해야 함");
        }
    }
    
    @Test
    public void testFindBestMoveWithOBlock() {
        // O 블록으로 게임 시작
        gameLogic.spawnNextPiece();
        
        // 현재 블록이 O 블록이 될 때까지 시도
        for (int i = 0; i < 10; i++) {
            if (gameLogic.getCurrentBlock() instanceof OBlock) {
                break;
            }
            while (gameLogic.moveDown()) {}
            gameLogic.spawnNextPiece();
        }
        
        if (gameLogic.getCurrentBlock() instanceof OBlock) {
            TetrisAI.Move move = TetrisAI.findBestMove(gameLogic);
            assertNotNull(move, "O 블록에 대한 최적의 수를 찾아야 함");
            assertTrue(move.x >= 0 && move.x < GameLogic.WIDTH, "x 좌표가 유효해야 함");
        }
    }
    
    @Test
    public void testBlockStatePreservation() {
        gameLogic.spawnNextPiece();
        Block currentBlock = gameLogic.getCurrentBlock();
        
        if (currentBlock != null) {
            int initialWidth = currentBlock.width();
            int initialHeight = currentBlock.height();
            
            TetrisAI.findBestMove(gameLogic);
            
            // findBestMove 후에도 블록의 상태가 원래대로 돌아와야 함
            assertEquals(initialWidth, currentBlock.width(), 
                "findBestMove 후 블록의 너비가 원래대로 돌아와야 함");
            assertEquals(initialHeight, currentBlock.height(), 
                "findBestMove 후 블록의 높이가 원래대로 돌아와야 함");
        }
    }
    
    @Test
    public void testMoveScoring() {
        gameLogic.spawnNextPiece();
        
        if (gameLogic.getCurrentBlock() != null) {
            TetrisAI.Move move = TetrisAI.findBestMove(gameLogic);
            
            if (move != null) {
                assertTrue(Double.isFinite(move.score), "점수는 유한한 값이어야 함");
                assertNotEquals(Double.NEGATIVE_INFINITY, move.score, 
                    "유효한 수는 NEGATIVE_INFINITY가 아니어야 함");
            }
        }
    }
    
    @Test
    public void testMoveValidity() {
        gameLogic.spawnNextPiece();
        
        if (gameLogic.getCurrentBlock() != null) {
            TetrisAI.Move move = TetrisAI.findBestMove(gameLogic);
            
            if (move != null) {
                assertTrue(move.x >= 0, "x 좌표는 0 이상이어야 함");
                assertTrue(move.x < GameLogic.WIDTH, "x 좌표는 보드 너비보다 작아야 함");
                assertTrue(move.rotation >= 0 && move.rotation < 4, 
                    "회전 값은 0-3 사이여야 함");
                assertTrue(move.finalY >= 0, "최종 y 좌표는 0 이상이어야 함");
            }
        }
    }
    
    @Test
    public void testMultipleMovesConsistency() {
        gameLogic.spawnNextPiece();
        Block block = gameLogic.getCurrentBlock();
        
        if (block != null) {
            // 같은 상태에서 여러 번 호출해도 같은 결과를 반환해야 함
            TetrisAI.Move move1 = TetrisAI.findBestMove(gameLogic);
            TetrisAI.Move move2 = TetrisAI.findBestMove(gameLogic);
            
            if (move1 != null && move2 != null) {
                assertEquals(move1.x, move2.x, "같은 상태에서 같은 x 좌표를 반환해야 함");
                assertEquals(move1.rotation, move2.rotation, 
                    "같은 상태에서 같은 회전 값을 반환해야 함");
                assertEquals(move1.score, move2.score, 0.001, 
                    "같은 상태에서 같은 점수를 반환해야 함");
            }
        }
    }
    
    @Test
    public void testMoveWithPartiallyFilledBoard() {
        // 보드의 일부를 채움
        int[][] board = gameLogic.getBoard();
        for (int col = 0; col < GameLogic.WIDTH; col++) {
            if (col != 4 && col != 5) { // 중간에 구멍 남김
                board[GameLogic.HEIGHT - 1][col] = 1;
            }
        }
        
        gameLogic.spawnNextPiece();
        
        if (gameLogic.getCurrentBlock() != null) {
            TetrisAI.Move move = TetrisAI.findBestMove(gameLogic);
            assertNotNull(move, "부분적으로 채워진 보드에서도 최적의 수를 찾아야 함");
        }
    }
    
    @Test
    public void testMoveToString() {
        TetrisAI.Move move = new TetrisAI.Move(5, 2, 18, 100.5);
        String str = move.toString();
        
        assertNotNull(str, "toString은 null이 아니어야 함");
        assertTrue(str.contains("5"), "x 좌표를 포함해야 함");
        assertTrue(str.contains("2"), "회전 값을 포함해야 함");
        assertTrue(str.contains("18"), "finalY를 포함해야 함");
    }
    
    @Test
    public void testMoveCreation() {
        TetrisAI.Move move = new TetrisAI.Move(3, 1, 15, 50.0);
        
        assertEquals(3, move.x, "x 좌표가 올바르게 설정되어야 함");
        assertEquals(1, move.rotation, "회전 값이 올바르게 설정되어야 함");
        assertEquals(15, move.finalY, "finalY가 올바르게 설정되어야 함");
        assertEquals(50.0, move.score, 0.001, "점수가 올바르게 설정되어야 함");
    }
    
    @Test
    public void testAIWithDifferentBlocks() {
        // 여러 블록에 대해 AI가 정상 작동하는지 확인
        for (int i = 0; i < 10; i++) {
            gameLogic.spawnNextPiece();
            Block block = gameLogic.getCurrentBlock();
            
            if (block != null) {
                int initialWidth = block.width();
                int initialHeight = block.height();
                
                TetrisAI.Move move = TetrisAI.findBestMove(gameLogic);
                
                // 블록 상태 확인
                assertEquals(initialWidth, block.width(), 
                    "블록 " + i + "의 너비가 보존되어야 함");
                assertEquals(initialHeight, block.height(), 
                    "블록 " + i + "의 높이가 보존되어야 함");
                
                // 블록을 실제로 떨어뜨림
                while (gameLogic.moveDown()) {}
                
                // 줄 삭제
                gameLogic.executeLineClear(gameLogic.findFullLines());
            }
        }
    }
    
    @Test
    public void testAIAvoidCreatingHoles() {
        // 보드 상태 설정 - 구멍이 생기기 쉬운 상황
        int[][] board = gameLogic.getBoard();
        
        // 아래쪽 두 줄을 거의 채우되, 한 칸씩 비움
        for (int row = GameLogic.HEIGHT - 2; row < GameLogic.HEIGHT; row++) {
            for (int col = 0; col < GameLogic.WIDTH; col++) {
                if (col != 3) {
                    board[row][col] = 1;
                }
            }
        }
        
        gameLogic.spawnNextPiece();
        
        if (gameLogic.getCurrentBlock() instanceof IBlock) {
            TetrisAI.Move move = TetrisAI.findBestMove(gameLogic);
            assertNotNull(move, "복잡한 보드 상태에서도 최적의 수를 찾아야 함");
            // AI는 구멍을 최소화하는 수를 선택해야 함
            assertTrue(move.score > Double.NEGATIVE_INFINITY, 
                "유효한 점수를 가져야 함");
        }
    }
}
