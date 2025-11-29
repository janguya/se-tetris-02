package com.example.game.component;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Animation 클래스의 단위 테스트
 */
public class AnimationTest {
    
    private Animation animation;
    private GraphicsContext gc;
    private Map<String, Color> colorMap;
    private String[][] blockTypes;
    
    @BeforeEach
    public void setUp() {
        animation = new Animation();
        
        // GraphicsContext 생성 (JavaFX 필요)
        try {
            javafx.application.Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Platform already initialized
        }
        
        Canvas canvas = new Canvas(200, 400);
        gc = canvas.getGraphicsContext2D();
        
        // 테스트용 색상 맵
        colorMap = new HashMap<>();
        colorMap.put("block-default", Color.GRAY);
        colorMap.put("block-i", Color.CYAN);
        colorMap.put("block-o", Color.YELLOW);
        colorMap.put("block-t", Color.PURPLE);
        
        // 테스트용 블록 타입 배열
        blockTypes = new String[20][10];
        for (int i = 0; i < 20; i++) {
            for (int j = 0; j < 10; j++) {
                blockTypes[i][j] = "block-default";
            }
        }
    }
    
    @Test
    public void testInitialState() {
        assertFalse(animation.isActive(), "애니메이션은 초기에 비활성화 상태여야 함");
        assertEquals(0, animation.getCurrentPhase(), "초기 페이즈는 0이어야 함");
    }
    
    @Test
    public void testStart() {
        animation.start();
        assertTrue(animation.isActive(), "start 호출 후 애니메이션이 활성화되어야 함");
        assertEquals(0, animation.getCurrentPhase(), "start 직후 페이즈는 0이어야 함");
    }
    
    @Test
    public void testReset() {
        animation.start();
        animation.reset();
        assertFalse(animation.isActive(), "reset 호출 후 애니메이션이 비활성화되어야 함");
        assertEquals(0, animation.getCurrentPhase(), "reset 후 페이즈는 0이어야 함");
    }
    
    @Test
    public void testUpdateProgression() throws InterruptedException {
        animation.start();
        long startTime = System.nanoTime();
        
        // 첫 번째 업데이트 (즉시)
        boolean finished = animation.update(startTime);
        assertFalse(finished, "초기 업데이트에서는 종료되지 않아야 함");
        assertEquals(0, animation.getCurrentPhase());
        
        // 0.08초 후 (첫 번째 깜빡임)
        long time1 = startTime + 80_000_000L;
        finished = animation.update(time1);
        assertFalse(finished, "첫 번째 깜빡임에서는 종료되지 않아야 함");
        assertEquals(1, animation.getCurrentPhase());
        
        // 0.16초 후 (두 번째 깜빡임)
        long time2 = startTime + 160_000_000L;
        finished = animation.update(time2);
        assertFalse(finished, "두 번째 깜빡임에서는 종료되지 않아야 함");
        assertEquals(2, animation.getCurrentPhase());
        
        // 0.24초 후 (세 번째 깜빡임)
        long time3 = startTime + 240_000_000L;
        finished = animation.update(time3);
        assertFalse(finished, "세 번째 깜빡임에서는 종료되지 않아야 함");
        assertEquals(3, animation.getCurrentPhase());
        
        // 0.32초 후 (애니메이션 종료)
        long time4 = startTime + 320_000_000L;
        finished = animation.update(time4);
        assertTrue(finished, "네 번째 페이즈에서 애니메이션이 종료되어야 함");
        assertFalse(animation.isActive(), "종료 후 비활성화 상태여야 함");
    }
    
    @Test
    public void testUpdateWhenInactive() {
        long now = System.nanoTime();
        boolean finished = animation.update(now);
        assertFalse(finished, "비활성 상태에서는 false를 반환해야 함");
    }
    
    @Test
    public void testDrawWithoutStart() {
        List<Integer> lines = new ArrayList<>();
        lines.add(19);
        
        // 애니메이션을 시작하지 않고 draw 호출
        assertDoesNotThrow(() -> {
            animation.draw(gc, lines, blockTypes, colorMap, 20, 10);
        }, "비활성 상태에서도 draw는 예외 없이 실행되어야 함");
    }
    
    @Test
    public void testDrawWithEmptyLines() {
        animation.start();
        List<Integer> emptyLines = new ArrayList<>();
        
        assertDoesNotThrow(() -> {
            animation.draw(gc, emptyLines, blockTypes, colorMap, 20, 10);
        }, "빈 줄 리스트로도 예외 없이 실행되어야 함");
    }
    
    @Test
    public void testDrawWithNullLines() {
        animation.start();
        
        assertDoesNotThrow(() -> {
            animation.draw(gc, null, blockTypes, colorMap, 20, 10);
        }, "null 줄 리스트로도 예외 없이 실행되어야 함");
    }
    
    @Test
    public void testDrawWithValidLines() {
        animation.start();
        List<Integer> lines = new ArrayList<>();
        lines.add(18);
        lines.add(19);
        
        assertDoesNotThrow(() -> {
            animation.draw(gc, lines, blockTypes, colorMap, 20, 10);
        }, "정상적인 줄 리스트로 draw 실행되어야 함");
    }
    
    @Test
    public void testDrawWithCyanColor() {
        animation.start();
        List<Integer> lines = new ArrayList<>();
        lines.add(19);
        
        assertDoesNotThrow(() -> {
            animation.draw(gc, lines, blockTypes, colorMap, 20, 10, true);
        }, "하늘색 옵션으로 draw 실행되어야 함");
    }
    
    @Test
    public void testDrawExplosionWithoutStart() {
        int[][] explosionCells = {{19, 5}, {18, 5}};
        
        assertDoesNotThrow(() -> {
            animation.drawExplosion(gc, explosionCells, blockTypes, colorMap, 20);
        }, "비활성 상태에서도 drawExplosion은 예외 없이 실행되어야 함");
    }
    
    @Test
    public void testDrawExplosionWithEmptyCells() {
        animation.start();
        int[][] emptyCells = {};
        
        assertDoesNotThrow(() -> {
            animation.drawExplosion(gc, emptyCells, blockTypes, colorMap, 20);
        }, "빈 셀 배열로도 예외 없이 실행되어야 함");
    }
    
    @Test
    public void testDrawExplosionWithNullCells() {
        animation.start();
        
        assertDoesNotThrow(() -> {
            animation.drawExplosion(gc, null, blockTypes, colorMap, 20);
        }, "null 셀 배열로도 예외 없이 실행되어야 함");
    }
    
    @Test
    public void testDrawExplosionWithValidCells() {
        animation.start();
        int[][] explosionCells = {{19, 4}, {19, 5}, {18, 5}};
        
        assertDoesNotThrow(() -> {
            animation.drawExplosion(gc, explosionCells, blockTypes, colorMap, 20);
        }, "정상적인 셀 배열로 drawExplosion 실행되어야 함");
    }
    
    @Test
    public void testDrawWithNullBlockType() {
        animation.start();
        List<Integer> lines = new ArrayList<>();
        lines.add(19);
        
        // blockTypes에 null 값 설정
        blockTypes[19][5] = null;
        
        assertDoesNotThrow(() -> {
            animation.draw(gc, lines, blockTypes, colorMap, 20, 10);
        }, "null 블록 타입이 있어도 예외 없이 실행되어야 함");
    }
    
    @Test
    public void testMultipleStartCalls() {
        animation.start();
        long firstPhase = animation.getCurrentPhase();
        
        animation.start(); // 두 번째 start 호출
        long secondPhase = animation.getCurrentPhase();
        
        assertEquals(0, firstPhase, "첫 번째 start 후 페이즈는 0");
        assertEquals(0, secondPhase, "두 번째 start 후에도 페이즈는 0으로 리셋");
        assertTrue(animation.isActive(), "여러 번 start 호출해도 활성 상태 유지");
    }
    
    @Test
    public void testPhaseTransitions() {
        animation.start();
        long startTime = System.nanoTime();
        
        // 각 페이즈별로 확인
        for (int expectedPhase = 0; expectedPhase < 4; expectedPhase++) {
            long currentTime = startTime + (expectedPhase * 80_000_000L);
            animation.update(currentTime);
            
            if (expectedPhase < 4) {
                assertEquals(expectedPhase, animation.getCurrentPhase(), 
                    "페이즈 " + expectedPhase + "가 올바르게 설정되어야 함");
            }
        }
    }
    
    @Test
    public void testAnimationCompleteCycle() {
        animation.start();
        assertTrue(animation.isActive(), "시작 시 활성화");
        
        long now = System.nanoTime();
        
        // 충분한 시간이 지난 후 (애니메이션 종료)
        long endTime = now + 400_000_000L; // 0.4초
        boolean finished = animation.update(endTime);
        
        assertTrue(finished, "충분한 시간 후 애니메이션 종료");
        assertFalse(animation.isActive(), "종료 후 비활성화");
        assertEquals(0, animation.getCurrentPhase(), "종료 후 페이즈 리셋");
    }
}
