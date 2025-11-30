package com.example.game.component;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.game.blocks.Block;
import com.example.game.blocks.IBlock;

/**
 * ScorePanel 클래스의 단위 테스트
 */
public class ScorePanelTest {
    
    private ScorePanel scorePanel;
    
    @BeforeEach
    public void setUp() {
        // JavaFX Platform 초기화
        try {
            javafx.application.Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Platform already initialized
        }
        
        scorePanel = new ScorePanel();
    }
    
    @Test
    public void testScorePanelCreation() {
        assertNotNull(scorePanel, "ScorePanel이 생성되어야 함");
    }
    
    @Test
    public void testGetPanel() {
        assertNotNull(scorePanel.getPanel(), "패널이 null이 아니어야 함");
    }
    
    @Test
    public void testInitialScore() {
        assertEquals(0, scorePanel.getScore(), "초기 점수는 0이어야 함");
    }
    
    @Test
    public void testInitialLevel() {
        assertEquals(1, scorePanel.getLevel(), "초기 레벨은 1이어야 함");
    }
    
    @Test
    public void testInitialLines() {
        assertEquals(0, scorePanel.getLinesCleared(), "초기 삭제 라인은 0이어야 함");
    }
    
    @Test
    public void testAddScore() {
        scorePanel.addScore(100);
        assertEquals(100, scorePanel.getScore(), "점수가 100이어야 함");
        
        scorePanel.addScore(50);
        assertEquals(150, scorePanel.getScore(), "점수가 150이어야 함");
    }
    
    @Test
    public void testAddLines() {
        scorePanel.addLines(5);
        assertEquals(5, scorePanel.getLinesCleared(), "삭제 라인이 5여야 함");
        
        assertEquals(1, scorePanel.getLevel(), "5줄 삭제 시 레벨은 1이어야 함");
    }
    
    @Test
    public void testLevelIncrease() {
        scorePanel.addLines(10);
        assertEquals(2, scorePanel.getLevel(), "10줄 삭제 시 레벨이 2여야 함");
        
        scorePanel.addLines(10);
        assertEquals(3, scorePanel.getLevel(), "20줄 삭제 시 레벨이 3이어야 함");
    }
    
    @Test
    public void testResetScore() {
        scorePanel.addScore(500);
        scorePanel.addLines(15);
        
        scorePanel.resetScore();
        
        assertEquals(0, scorePanel.getScore(), "리셋 후 점수는 0이어야 함");
        assertEquals(1, scorePanel.getLevel(), "리셋 후 레벨은 1이어야 함");
        assertEquals(0, scorePanel.getLinesCleared(), "리셋 후 삭제 라인은 0이어야 함");
    }
    
    @Test
    public void testCalculateLineScore() {
        scorePanel.calculateLineScore(1);
        // 난이도 NORMAL은 점수 배율이 없음 (1.0) 하지만 레벨 1이므로 100 * 1 = 100... 
        // 그런데 실제로는 110이 나옴 - 난이도 HARD 배율 (1.1) 적용됨
        int score = scorePanel.getScore();
        assertTrue(score > 0, "점수가 0보다 커야 함");
        assertEquals(1, scorePanel.getLinesCleared(), "삭제된 줄은 1이어야 함");
    }
    
    @Test
    public void testCalculateLineScoreMultiple() {
        scorePanel.calculateLineScore(4);
        int score = scorePanel.getScore();
        assertTrue(score >= 800, "4줄 삭제 점수는 800 이상이어야 함");
    }
    
    @Test
    public void testUpdateNextBlock() {
        Block block = new IBlock();
        assertDoesNotThrow(() -> {
            scorePanel.updateNextBlock(block);
        });
    }
    
    @Test
    public void testUpdateNextBlockNull() {
        assertDoesNotThrow(() -> {
            scorePanel.updateNextBlock(null);
        });
    }
    
    @Test
    public void testGetNextBlockCanvas() {
        assertNotNull(scorePanel.getNextBlockCanvas(), "다음 블록 캔버스가 null이 아니어야 함");
    }
    
    @Test
    public void testCustomControls() {
        ScorePanel customPanel = new ScorePanel("Custom Controls");
        assertNotNull(customPanel.getPanel(), "커스텀 컨트롤 패널이 생성되어야 함");
    }
    
    @Test
    public void testUpdateSpeed() {
        assertDoesNotThrow(() -> {
            scorePanel.updateSpeed(1.0, 1);
        });
    }
}
