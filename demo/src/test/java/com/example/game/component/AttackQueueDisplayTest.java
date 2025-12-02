package com.example.game.component;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javafx.scene.layout.VBox;

/**
 * AttackQueueDisplay 클래스의 단위 테스트
 */
public class AttackQueueDisplayTest {
    
    private AttackQueueDisplay display;
    
    @BeforeEach
    public void setUp() {
        // JavaFX Platform 초기화
        try {
            javafx.application.Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Platform already initialized
        }
        
        display = new AttackQueueDisplay("Player 1");
    }
    
    @Test
    public void testInitialState() {
        assertEquals(0, display.getQueueSize(), "초기 큐 크기는 0이어야 함");
        assertNotNull(display.getContainer(), "컨테이너는 null이 아니어야 함");
        assertTrue(display.getContainer() instanceof VBox, "컨테이너는 VBox 타입이어야 함");
    }
    
    @Test
    public void testAddSingleAttackLine() {
        List<String[]> lines = new ArrayList<>();
        String[] line = createAttackLine(10);
        lines.add(line);
        
        display.addAttackLines(lines);
        
        assertEquals(1, display.getQueueSize(), "공격 줄 추가 후 큐 크기는 1이어야 함");
    }
    
    @Test
    public void testAddMultipleAttackLines() {
        List<String[]> lines = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            lines.add(createAttackLine(10));
        }
        
        display.addAttackLines(lines);
        
        assertEquals(3, display.getQueueSize(), "3줄 추가 후 큐 크기는 3이어야 함");
    }
    
    @Test
    public void testPollAttackLine() {
        List<String[]> lines = new ArrayList<>();
        String[] line1 = createAttackLine(10);
        String[] line2 = createAttackLine(10);
        lines.add(line1);
        lines.add(line2);
        
        display.addAttackLines(lines);
        assertEquals(2, display.getQueueSize());
        
        String[] polled = display.pollAttackLine();
        assertNotNull(polled, "poll된 줄은 null이 아니어야 함");
        assertEquals(1, display.getQueueSize(), "poll 후 큐 크기는 1이어야 함");
        
        String[] polled2 = display.pollAttackLine();
        assertNotNull(polled2, "두 번째 poll된 줄도 null이 아니어야 함");
        assertEquals(0, display.getQueueSize(), "모두 poll 후 큐 크기는 0이어야 함");
    }
    
    @Test
    public void testPollFromEmptyQueue() {
        String[] polled = display.pollAttackLine();
        assertNull(polled, "빈 큐에서 poll하면 null을 반환해야 함");
        assertEquals(0, display.getQueueSize(), "큐 크기는 여전히 0이어야 함");
    }
    
    @Test
    public void testClear() {
        List<String[]> lines = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            lines.add(createAttackLine(10));
        }
        
        display.addAttackLines(lines);
        assertEquals(5, display.getQueueSize());
        
        display.clear();
        assertEquals(0, display.getQueueSize(), "clear 후 큐 크기는 0이어야 함");
    }
    
    @Test
    public void testGetQueue() {
        List<String[]> lines = new ArrayList<>();
        lines.add(createAttackLine(10));
        
        display.addAttackLines(lines);
        
        Queue<String[]> queue = display.getQueue();
        assertNotNull(queue, "큐는 null이 아니어야 함");
        assertEquals(1, queue.size(), "큐 크기는 1이어야 함");
    }
    
    @Test
    public void testSyncWithActualQueue_Decrease() {
        // 5줄 추가
        List<String[]> lines = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            lines.add(createAttackLine(10));
        }
        display.addAttackLines(lines);
        assertEquals(5, display.getQueueSize());
        
        // 실제 큐는 2줄로 동기화
        display.syncWithActualQueue(2);
        assertEquals(2, display.getQueueSize(), "동기화 후 큐 크기는 2여야 함");
    }
    
    @Test
    public void testSyncWithActualQueue_Increase() {
        // 2줄 추가
        List<String[]> lines = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            lines.add(createAttackLine(10));
        }
        display.addAttackLines(lines);
        assertEquals(2, display.getQueueSize());
        
        // 실제 큐는 5줄로 동기화
        display.syncWithActualQueue(5);
        assertEquals(5, display.getQueueSize(), "동기화 후 큐 크기는 5여야 함");
    }
    
    @Test
    public void testSyncWithActualQueue_SameSize() {
        // 3줄 추가
        List<String[]> lines = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            lines.add(createAttackLine(10));
        }
        display.addAttackLines(lines);
        assertEquals(3, display.getQueueSize());
        
        // 같은 크기로 동기화
        display.syncWithActualQueue(3);
        assertEquals(3, display.getQueueSize(), "동일한 크기로 동기화해도 변화 없어야 함");
    }
    
    @Test
    public void testSyncWithActualQueue_ToZero() {
        // 3줄 추가
        List<String[]> lines = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            lines.add(createAttackLine(10));
        }
        display.addAttackLines(lines);
        assertEquals(3, display.getQueueSize());
        
        // 0으로 동기화
        display.syncWithActualQueue(0);
        assertEquals(0, display.getQueueSize(), "0으로 동기화하면 큐가 비워져야 함");
    }
    
    @Test
    public void testSyncWithActualQueue_FromZero() {
        assertEquals(0, display.getQueueSize());
        
        // 빈 큐에서 3으로 동기화
        display.syncWithActualQueue(3);
        assertEquals(3, display.getQueueSize(), "빈 큐에서 3으로 동기화하면 더미 줄 추가되어야 함");
    }
    
    @Test
    public void testAddEmptyList() {
        List<String[]> emptyLines = new ArrayList<>();
        
        display.addAttackLines(emptyLines);
        assertEquals(0, display.getQueueSize(), "빈 리스트 추가 시 큐 크기는 0이어야 함");
    }
    
    @Test
    public void testMultipleOperations() {
        // 2줄 추가
        List<String[]> lines1 = new ArrayList<>();
        lines1.add(createAttackLine(10));
        lines1.add(createAttackLine(10));
        display.addAttackLines(lines1);
        assertEquals(2, display.getQueueSize());
        
        // 1줄 poll
        display.pollAttackLine();
        assertEquals(1, display.getQueueSize());
        
        // 3줄 더 추가
        List<String[]> lines2 = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            lines2.add(createAttackLine(10));
        }
        display.addAttackLines(lines2);
        assertEquals(4, display.getQueueSize());
        
        // clear
        display.clear();
        assertEquals(0, display.getQueueSize());
    }
    
    @Test
    public void testContainerNotNull() {
        VBox container = display.getContainer();
        assertNotNull(container, "컨테이너는 null이 아니어야 함");
        assertFalse(container.getChildren().isEmpty(), "컨테이너는 자식 요소를 가져야 함");
    }
    
    /**
     * 테스트용 공격 줄 생성
     */
    private String[] createAttackLine(int width) {
        String[] line = new String[width];
        for (int i = 0; i < width; i++) {
            if (i % 2 == 0) {
                line[i] = "attack-block";
            } else {
                line[i] = null;
            }
        }
        return line;
    }
}
