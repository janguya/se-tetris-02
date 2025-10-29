package com.example.game.blocks;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

class BlockTest {
    @Test
    @DisplayName("모든 블록 타입 생성 테스트")
    void testAllBlockTypes() {
        Block[] blocks = {
                new IBlock(), new OBlock(), new TBlock(),
                new SBlock(), new ZBlock(), new JBlock(), new LBlock()
        };

        for (Block block : blocks) {
            assertNotNull(block, "블록이 null이면 안됩니다");
            assertNotNull(block.getCssClass(), "CSS 클래스가 null이면 안됩니다");
            assertTrue(block.width() > 0, "블록 너비가 0보다 커야 합니다");
            assertTrue(block.height() > 0, "블록 높이가 0보다 커야 합니다");
        }
    }
}