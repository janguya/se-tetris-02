package com.example.game.component;

import java.util.List;
import java.util.Map;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * 줄 삭제 애니메이션을 관리하는 클래스
 * Board 클래스에서 애니메이션 로직을 분리하여 코드를 깔끔하게 유지
 */
public class Animation {
    
    // 애니메이션 설정 - 빠르고 강렬하게!
    private static final long BLINK_DURATION = 80_000_000L; // 0.08초 (나노초)
    private static final int BLINK_COUNT = 2; // 2번 깜빡임
    
    // 애니메이션 상태
    private long animationStartTime = 0;
    private int animationPhase = 0;
    private boolean isActive = false;
    
    /**
     * 애니메이션 시작
     */
    public void start() {
        animationStartTime = System.nanoTime();
        animationPhase = 0;
        isActive = true;
    }
    
    /**
     * 애니메이션 업데이트 및 종료 여부 반환
     * @param now 현재 시간 (나노초)
     * @return 애니메이션이 종료되었으면 true
     */
    public boolean update(long now) {
        if (!isActive) {
            return false;
        }
        
        long elapsed = now - animationStartTime;
        int newPhase = (int)(elapsed / BLINK_DURATION);
        
        if (newPhase != animationPhase) {
            animationPhase = newPhase;
        }
        
        // 애니메이션 종료 체크 (2번 깜빡임 = 4 페이즈)
        if (animationPhase >= (BLINK_COUNT * 2)) {
            reset();
            return true; // 애니메이션 종료
        }
        
        return false; // 애니메이션 진행 중
    }
    
    /**
     * 줄 삭제 애니메이션 그리기
     * @param gc GraphicsContext
     * @param lines 삭제될 줄 번호 리스트
     * @param blockTypes 블록 타입 배열
     * @param colorMap 색상 맵
     * @param cellSize 셀 크기
     * @param boardWidth 보드 너비 (셀 개수)
     */
    public void draw(GraphicsContext gc, List<Integer> lines, String[][] blockTypes, 
                     Map<String, Color> colorMap, int cellSize, int boardWidth) {
        if (!isActive || lines == null || lines.isEmpty()) {
            return;
        }
        
        for (int row : lines) {
            for (int col = 0; col < boardWidth; col++) {
                String cssClass = blockTypes[row][col];
                Color blockColor;
                
                // null 체크: cssClass가 null이면 기본 색상 사용
                if (cssClass == null) {
                    blockColor = colorMap.getOrDefault("block-default", Color.GRAY);
                } else {
                    blockColor = colorMap.getOrDefault(cssClass, colorMap.get("block-default"));
                }
                
                double x = col * cellSize;
                double y = row * cellSize;
                
                // 애니메이션 페이즈에 따른 효과
                if (animationPhase % 2 == 1) {
                    // 홀수 페이즈: 강렬한 흰색 플래시
                    drawFlashEffect(gc, x, y, cellSize);
                } else {
                    // 짝수 페이즈: 약간 밝게
                    drawBrightEffect(gc, x, y, cellSize, blockColor);
                }
            }
            
            // 줄 전체에 빛나는 효과 (홀수 페이즈)
            if (animationPhase % 2 == 1) {
                drawLineHighlight(gc, row, cellSize, boardWidth);
            }
        }
    }
    
    /**
     * 플래시 효과 그리기 (강렬한 흰색)
     */
    private void drawFlashEffect(GraphicsContext gc, double x, double y, int cellSize) {
        // 흰색 셀
        gc.setFill(Color.WHITE);
        gc.fillRect(x + 1, y + 1, cellSize - 2, cellSize - 2);
        
        // 강렬한 노란 빛 오버레이
        gc.setFill(Color.YELLOW.deriveColor(0, 1, 1, 0.6));
        gc.fillRect(x + 1, y + 1, cellSize - 2, cellSize - 2);
        
        // 하이라이트 효과
        gc.setFill(Color.WHITE);
        gc.fillRect(x + 2, y + 2, cellSize - 4, 3);
        gc.fillRect(x + 2, y + 2, 3, cellSize - 4);
    }
    
    /**
     * 밝게 빛나는 효과 그리기
     */
    private void drawBrightEffect(GraphicsContext gc, double x, double y, int cellSize, Color baseColor) {
        // null 체크: baseColor가 null이면 기본 회색 사용
        if (baseColor == null) {
            baseColor = Color.GRAY;
        }
        
        Color brightColor = baseColor.brighter();
        
        // 메인 셀
        gc.setFill(brightColor);
        gc.fillRect(x + 1, y + 1, cellSize - 2, cellSize - 2);
        
        // 하이라이트
        gc.setFill(brightColor.brighter());
        gc.fillRect(x + 2, y + 2, cellSize - 4, 3);
        gc.fillRect(x + 2, y + 2, 3, cellSize - 4);
        
        // 그림자
        gc.setFill(brightColor.darker());
        gc.fillRect(x + 2, y + cellSize - 5, cellSize - 4, 3);
        gc.fillRect(x + cellSize - 5, y + 2, 3, cellSize - 4);
    }
    
    /**
     * 줄 전체 하이라이트 효과
     */
    private void drawLineHighlight(GraphicsContext gc, int row, int cellSize, int boardWidth) {
        gc.setFill(Color.GOLD.deriveColor(0, 1, 1, 0.3));
        gc.fillRect(0, row * cellSize, boardWidth * cellSize, cellSize);
    }
    
    /**
     * 애니메이션 초기화
     */
    public void reset() {
        animationStartTime = 0;
        animationPhase = 0;
        isActive = false;
    }
    
    /**
     * 애니메이션 활성화 상태 확인
     */
    public boolean isActive() {
        return isActive;
    }
    
    /**
     * 현재 페이즈 가져오기 (디버깅용)
     */
    public int getCurrentPhase() {
        return animationPhase;
    }
}