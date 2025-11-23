package com.example.game.ai;

import com.example.game.blocks.Block;
import com.example.game.component.GameLogic;
import com.example.utils.Logger;

/**
 * 테트리스 AI - 휴리스틱 알고리즘을 사용하여 최적의 수를 찾음
 * Dellacherie 알고리즘 기반
 */
public class TetrisAI {
    
    // Dellacherie 휴리스틱 가중치 (경험적으로 안정적인 값)
    private static final double WEIGHT_AGGREGATE_HEIGHT = -0.510066;
    private static final double WEIGHT_COMPLETE_LINES = 0.760666;
    private static final double WEIGHT_HOLES = -0.35663;
    private static final double WEIGHT_BUMPINESS = -0.184483;
    
    /**
     * 최적의 Move 찾기
     */
    public static Move findBestMove(GameLogic gameLogic) {
        Block currentBlock = gameLogic.getCurrentBlock();
        if (currentBlock == null) {
            return null;
        }
        
        Move bestMove = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        int validMoves = 0;

        // 블록의 원래 상태를 저장하기 위해 초기 형태 기록
        int initialWidth = currentBlock.width();
        int initialHeight = currentBlock.height();

        // 현재 보드에서 진행 중인 블록을 제거한 복사본 준비 (자기 충돌 방지)
        int[][] boardWithoutCurrent = copyBoard(gameLogic.getBoard());
        removeCurrentBlock(boardWithoutCurrent, currentBlock, gameLogic.getCurrentX(), gameLogic.getCurrentY());
        
        // 모든 가능한 회전 상태 시도 (0~3)
        for (int rotation = 0; rotation < 4; rotation++) {
            // 현재 회전 상태에서 모든 가능한 x 위치 시도
            for (int x = 0; x <= GameLogic.WIDTH - currentBlock.width(); x++) {
                // 해당 위치에 블록을 놓을 수 있는지 확인
                int finalY = getFinalY(boardWithoutCurrent, currentBlock, x);
                
                if (finalY == -1) {
                    continue; // 불가능한 위치
                }
                
                validMoves++;

                // 보드 상태 시뮬레이션
                int[][] simulatedBoard = simulateMove(boardWithoutCurrent, currentBlock, x, finalY);

                // 이 수의 점수 평가
                double score = evaluateBoard(simulatedBoard);

                if (score > bestScore) {
                    bestScore = score;
                    bestMove = new Move(x, rotation, finalY, score);
                }
            }
            
            // 다음 회전 상태로
            currentBlock.rotate();
        }
        
        // 블록이 원래 상태로 돌아왔는지 확인
        // 4번 회전하면 원래대로 돌아와야 함
        if (currentBlock.width() != initialWidth || currentBlock.height() != initialHeight) {
                Logger.info("[WARNING] Block rotation did not return to original state!");
                Logger.info("Initial: %dx%d, Final: %dx%d", initialWidth, initialHeight,
                    currentBlock.width(), currentBlock.height());
        }
        
        // 디버그 로그
        if (bestMove != null) {
            Logger.info("AI found %d valid moves, best: %s", validMoves, bestMove);
        } else {
            Logger.info("AI found no valid moves!");
        }
        
        return bestMove;
    }
    
    /**
     * 블록이 특정 x 위치에서 떨어질 때 최종 y 위치 구하기
     */
    private static int getFinalY(int[][] board, Block block, int x) {
        if (x < 0 || x + block.width() > GameLogic.WIDTH) {
            return -1;
        }

        int y = 0;
        if (!canPlaceBlock(board, block, x, y)) {
            return -1;
        }

        while (y < GameLogic.HEIGHT && canPlaceBlock(board, block, x, y + 1)) {
            y++;
        }

        return y;
    }
    
    private static int[][] copyBoard(int[][] board) {
        int[][] copy = new int[GameLogic.HEIGHT][GameLogic.WIDTH];
        for (int row = 0; row < GameLogic.HEIGHT; row++) {
            System.arraycopy(board[row], 0, copy[row], 0, GameLogic.WIDTH);
        }
        return copy;
    }

    private static void removeCurrentBlock(int[][] board, Block block, int currentX, int currentY) {
        if (block == null) {
            return;
        }

        for (int row = 0; row < block.height(); row++) {
            for (int col = 0; col < block.width(); col++) {
                if (block.getShape(col, row) != 1) {
                    continue;
                }
                int boardX = currentX + col;
                int boardY = currentY + row;

                if (boardX >= 0 && boardX < GameLogic.WIDTH &&
                    boardY >= 0 && boardY < GameLogic.HEIGHT) {
                    board[boardY][boardX] = 0;
                }
            }
        }
    }

    private static boolean canPlaceBlock(int[][] board, Block block, int x, int y) {
        for (int row = 0; row < block.height(); row++) {
            for (int col = 0; col < block.width(); col++) {
                if (block.getShape(col, row) != 1) {
                    continue;
                }

                int boardX = x + col;
                int boardY = y + row;

                if (boardX < 0 || boardX >= GameLogic.WIDTH) {
                    return false;
                }

                if (boardY >= GameLogic.HEIGHT) {
                    return false;
                }

                if (boardY < -2) {
                    return false;
                }

                if (boardY >= 0 && board[boardY][boardX] == 1) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 블록을 놓았을 때의 보드 상태 시뮬레이션
     */
    private static int[][] simulateMove(int[][] board, Block block, int x, int y) {
        int[][] simBoard = copyBoard(board);
        
        // 블록 놓기 - shape는 [row][col] 형태이므로 주의
        for (int row = 0; row < block.height(); row++) {
            for (int col = 0; col < block.width(); col++) {
                if (block.getShape(col, row) == 1) {
                    int boardX = x + col;
                    int boardY = y + row;
                    
                    if (boardX >= 0 && boardX < GameLogic.WIDTH && 
                        boardY >= 0 && boardY < GameLogic.HEIGHT) {
                        simBoard[boardY][boardX] = 1;
                    }
                }
            }
        }
        
        // 줄 삭제는 하지 않고 블록을 놓은 상태 그대로 반환
        // (평가 함수에서 countClearedLines를 통해 삭제할 줄 수를 계산)
        return simBoard;
    }
    
    /**
     * 보드 상태 평가 (간단하고 효과적인 알고리즘)
     */
    private static double evaluateBoard(int[][] board) {
        double score = 0;
        
        // 1. Aggregate Height (전체 높이 합 - 낮을수록 좋음)
        int aggregateHeight = getAggregateHeight(board);
        score += WEIGHT_AGGREGATE_HEIGHT * aggregateHeight;
        
        // 2. Complete Lines (완성된 줄 수 - 많을수록 좋음)
        int completeLines = countClearedLines(board);
        score += WEIGHT_COMPLETE_LINES * completeLines;
        
        // 3. Holes (구멍 수 - 적을수록 좋음)
        int holes = countHoles(board);
        score += WEIGHT_HOLES * holes;
        
        // 4. Bumpiness (울퉁불퉁함 - 적을수록 좋음)
        int bumpiness = getBumpiness(board);
        score += WEIGHT_BUMPINESS * bumpiness;
        
        // 디버그: 줄 삭제가 가능한 경우 로그 출력
        if (completeLines > 0) {
                Logger.info("[AI] Found move that clears %d lines! Score: %.2f", completeLines, score);
                Logger.info("    Height: %d, Holes: %d, Bumpiness: %d",
                    aggregateHeight, holes, bumpiness);
        }
        
        return score;
    }
    
    /**
     * 각 열의 높이 합 계산
     */
    private static int getAggregateHeight(int[][] board) {
        int total = 0;
        for (int col = 0; col < GameLogic.WIDTH; col++) {
            for (int row = 0; row < GameLogic.HEIGHT; row++) {
                if (board[row][col] == 1) {
                    total += (GameLogic.HEIGHT - row);
                    break;
                }
            }
        }
        return total;
    }
    
    /**
     * 인접한 열의 높이 차이 합 계산
     */
    private static int getBumpiness(int[][] board) {
        int bumpiness = 0;
        int[] heights = new int[GameLogic.WIDTH];
        
        // 각 열의 높이 계산
        for (int col = 0; col < GameLogic.WIDTH; col++) {
            for (int row = 0; row < GameLogic.HEIGHT; row++) {
                if (board[row][col] == 1) {
                    heights[col] = GameLogic.HEIGHT - row;
                    break;
                }
            }
        }
        
        // 인접한 열의 높이 차이 합
        for (int col = 0; col < GameLogic.WIDTH - 1; col++) {
            bumpiness += Math.abs(heights[col] - heights[col + 1]);
        }
        
        return bumpiness;
    }
    
    private static int countClearedLines(int[][] board) {
        int count = 0;
        for (int row = 0; row < GameLogic.HEIGHT; row++) {
            boolean full = true;
            for (int col = 0; col < GameLogic.WIDTH; col++) {
                if (board[row][col] == 0) {
                    full = false;
                    break;
                }
            }
            if (full) count++;
        }
        return count;
    }
    
    private static int countHoles(int[][] board) {
        int holes = 0;
        for (int col = 0; col < GameLogic.WIDTH; col++) {
            boolean blockFound = false;
            for (int row = 0; row < GameLogic.HEIGHT; row++) {
                if (board[row][col] == 1) {
                    blockFound = true;
                } else if (blockFound && board[row][col] == 0) {
                    holes++;
                }
            }
        }
        return holes;
    }
    
    /**
     * Move 클래스 - AI가 선택한 수
     */
    public static class Move {
        public final int x;
        public final int rotation;
        public final int finalY;
        public final double score;
        
        public Move(int x, int rotation, int finalY, double score) {
            this.x = x;
            this.rotation = rotation;
            this.finalY = finalY;
            this.score = score;
        }
        
        @Override
        public String toString() {
            return String.format("Move(x=%d, rotation=%d, finalY=%d, score=%.2f)", 
                x, rotation, finalY, score);
        }
    }
}
