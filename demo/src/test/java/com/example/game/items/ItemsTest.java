package com.example.game.items;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import com.example.game.blocks.*;

/**
 * Items 패키지의 모든 아이템 블록 테스트
 */
class ItemsTest {

    @Test
    @DisplayName("SingleBlock 생성 및 기본 속성 테스트")
    void testSingleBlockCreation() {
        SingleBlock block = new SingleBlock();

        assertNotNull(block, "SingleBlock이 null이면 안됩니다");
        assertNotNull(block.getCssClass(), "CSS 클래스가 null이면 안됩니다");
        assertEquals("item-single", block.getCssClass(), "CSS 클래스가 item-single이어야 합니다");
        assertEquals(1, block.width(), "SingleBlock의 너비는 1이어야 합니다");
        assertEquals(1, block.height(), "SingleBlock의 높이는 1이어야 합니다");
        assertEquals(1, block.getShape(0, 0), "SingleBlock의 (0,0) 위치는 1이어야 합니다");
    }

    @Test
    @DisplayName("SingleBlock 회전 테스트")
    void testSingleBlockRotation() {
        SingleBlock block = new SingleBlock();

        // 1x1 블록은 회전해도 동일
        block.rotate();
        assertEquals(1, block.width(), "회전 후에도 너비는 1이어야 합니다");
        assertEquals(1, block.height(), "회전 후에도 높이는 1이어야 합니다");
    }

    @Test
    @DisplayName("weightedBlock 생성 및 기본 속성 테스트")
    void testWeightedBlockCreation() {
        weightedBlock block = new weightedBlock();

        assertNotNull(block, "weightedBlock이 null이면 안됩니다");
        assertNotNull(block.getCssClass(), "CSS 클래스가 null이면 안됩니다");
        assertEquals("item-weight", block.getCssClass(), "CSS 클래스가 item-weight이어야 합니다");
        assertEquals(4, block.width(), "weightedBlock의 너비는 4이어야 합니다");
        assertEquals(2, block.height(), "weightedBlock의 높이는 2이어야 합니다");
    }

    @Test
    @DisplayName("weightedBlock 형태 테스트")
    void testWeightedBlockShape() {
        weightedBlock block = new weightedBlock();

        // 첫 번째 줄: 0, 1, 1, 0
        assertEquals(0, block.getShape(0, 0), "(0,0)은 0이어야 합니다");
        assertEquals(1, block.getShape(1, 0), "(1,0)은 1이어야 합니다");
        assertEquals(1, block.getShape(2, 0), "(2,0)은 1이어야 합니다");
        assertEquals(0, block.getShape(3, 0), "(3,0)은 0이어야 합니다");

        // 두 번째 줄: 1, 1, 1, 1
        assertEquals(1, block.getShape(0, 1), "(0,1)은 1이어야 합니다");
        assertEquals(1, block.getShape(1, 1), "(1,1)은 1이어야 합니다");
        assertEquals(1, block.getShape(2, 1), "(2,1)은 1이어야 합니다");
        assertEquals(1, block.getShape(3, 1), "(3,1)은 1이어야 합니다");
    }

    @Test
    @DisplayName("weightedBlock fallToBottom 테스트")
    void testWeightedBlockFallToBottom() {
        weightedBlock block = new weightedBlock();

        // 20x10 보드 생성
        int[][] board = new int[20][10];
        String[][] blockTypes = new String[20][10];

        // 일부 블록 배치 (아래쪽)
        for (int col = 0; col < 10; col++) {
            board[19][col] = 1;
            blockTypes[19][col] = "test-block";
        }

        // fallToBottom 호출 - 예외가 발생하지 않아야 함
        assertDoesNotThrow(() -> {
            int finalRow = block.fallToBottom(board, blockTypes, 0, 3);
            assertTrue(finalRow >= 0, "최종 행 위치는 0 이상이어야 합니다");
        }, "fallToBottom은 예외를 발생시키지 않아야 합니다");
    }

    @Test
    @DisplayName("LItem 생성 및 기본 속성 테스트")
    void testLItemCreation() {
        Block baseBlock = new IBlock();
        LItem lItem = new LItem(baseBlock);

        assertNotNull(lItem, "LItem이 null이면 안됩니다");
        assertNotNull(lItem.getCssClass(), "CSS 클래스가 null이면 안됩니다");
        assertTrue(lItem.width() > 0, "LItem의 너비는 0보다 커야 합니다");
        assertTrue(lItem.height() > 0, "LItem의 높이는 0보다 커야 합니다");
    }

    @Test
    @DisplayName("LItem 다양한 베이스 블록 테스트")
    void testLItemWithVariousBlocks() {
        Block[] baseBlocks = {
                new IBlock(), new OBlock(), new TBlock(),
                new SBlock(), new ZBlock(), new JBlock(), new LBlock()
        };

        for (Block baseBlock : baseBlocks) {
            LItem lItem = new LItem(baseBlock);
            assertNotNull(lItem, "LItem이 null이면 안됩니다");
            assertEquals(baseBlock.width(), lItem.width(),
                    "LItem의 너비는 베이스 블록과 같아야 합니다");
            assertEquals(baseBlock.height(), lItem.height(),
                    "LItem의 높이는 베이스 블록과 같아야 합니다");
        }
    }

    @Test
    @DisplayName("LItem hasLMarker 테스트")
    void testLItemHasLMarker() {
        Block baseBlock = new IBlock();
        LItem lItem = new LItem(baseBlock);

        // L 마커가 있는 위치를 찾아야 함
        boolean foundLMarker = false;
        for (int row = 0; row < lItem.height(); row++) {
            for (int col = 0; col < lItem.width(); col++) {
                if (lItem.hasLMarker(row, col)) {
                    foundLMarker = true;
                    break;
                }
            }
            if (foundLMarker)
                break;
        }

        assertTrue(foundLMarker, "LItem은 최소 하나의 L 마커를 가져야 합니다");
    }

    @Test
    @DisplayName("LItem getLRow 및 getLCol 테스트")
    void testLItemGetLRowAndCol() {
        Block baseBlock = new TBlock();
        LItem lItem = new LItem(baseBlock);

        int lRow = lItem.getLRow();
        int lCol = lItem.getLCol();

        assertTrue(lRow >= 0 && lRow < lItem.height(),
                "L 마커 행은 유효한 범위 내에 있어야 합니다");
        assertTrue(lCol >= 0 && lCol < lItem.width(),
                "L 마커 열은 유효한 범위 내에 있어야 합니다");
        assertTrue(lItem.hasLMarker(lRow, lCol),
                "getLRow/getLCol이 반환한 위치에 L 마커가 있어야 합니다");
    }

    @Test
    @DisplayName("BombBlock 생성 및 기본 속성 테스트")
    void testBombBlockCreation() {
        Block baseBlock = new OBlock();
        BombBlock bombBlock = new BombBlock(baseBlock);

        assertNotNull(bombBlock, "BombBlock이 null이면 안됩니다");
        assertNotNull(bombBlock.getCssClass(), "CSS 클래스가 null이면 안됩니다");
        assertTrue(bombBlock.width() > 0, "BombBlock의 너비는 0보다 커야 합니다");
        assertTrue(bombBlock.height() > 0, "BombBlock의 높이는 0보다 커야 합니다");
    }

    @Test
    @DisplayName("BombBlock 다양한 베이스 블록 테스트")
    void testBombBlockWithVariousBlocks() {
        Block[] baseBlocks = {
                new IBlock(), new OBlock(), new TBlock(),
                new SBlock(), new ZBlock(), new JBlock(), new LBlock()
        };

        for (Block baseBlock : baseBlocks) {
            BombBlock bombBlock = new BombBlock(baseBlock);
            assertNotNull(bombBlock, "BombBlock이 null이면 안됩니다");
            assertEquals(baseBlock.width(), bombBlock.width(),
                    "BombBlock의 너비는 베이스 블록과 같아야 합니다");
            assertEquals(baseBlock.height(), bombBlock.height(),
                    "BombBlock의 높이는 베이스 블록과 같아야 합니다");
        }
    }

    @Test
    @DisplayName("BombBlock hasBMarker 테스트")
    void testBombBlockHasBMarker() {
        Block baseBlock = new TBlock();
        BombBlock bombBlock = new BombBlock(baseBlock);

        // B 마커가 있는 위치를 찾아야 함
        boolean foundBMarker = false;
        for (int row = 0; row < bombBlock.height(); row++) {
            for (int col = 0; col < bombBlock.width(); col++) {
                if (bombBlock.hasBMarker(row, col)) {
                    foundBMarker = true;
                    break;
                }
            }
            if (foundBMarker)
                break;
        }

        assertTrue(foundBMarker, "BombBlock은 최소 하나의 B 마커를 가져야 합니다");
    }

    @Test
    @DisplayName("BombBlock getBRow 및 getBCol 테스트")
    void testBombBlockGetBRowAndCol() {
        Block baseBlock = new SBlock();
        BombBlock bombBlock = new BombBlock(baseBlock);

        int bRow = bombBlock.getBRow();
        int bCol = bombBlock.getBCol();

        assertTrue(bRow >= 0 && bRow < bombBlock.height(),
                "B 마커 행은 유효한 범위 내에 있어야 합니다");
        assertTrue(bCol >= 0 && bCol < bombBlock.width(),
                "B 마커 열은 유효한 범위 내에 있어야 합니다");
        assertTrue(bombBlock.hasBMarker(bRow, bCol),
                "getBRow/getBCol이 반환한 위치에 B 마커가 있어야 합니다");
    }

    @Test
    @DisplayName("BombBlock explode 테스트")
    void testBombBlockExplode() {
        Block baseBlock = new OBlock();
        BombBlock bombBlock = new BombBlock(baseBlock);

        // 20x10 보드 생성
        int[][] board = new int[20][10];
        String[][] blockTypes = new String[20][10];

        // 보드를 블록으로 채움
        for (int row = 15; row < 20; row++) {
            for (int col = 0; col < 10; col++) {
                board[row][col] = 1;
                blockTypes[row][col] = "test-block";
            }
        }

        // explode 호출 - 예외가 발생하지 않아야 함
        assertDoesNotThrow(() -> {
            bombBlock.explode(board, blockTypes, 17, 5);
        }, "explode는 예외를 발생시키지 않아야 합니다");
    }

    @Test
    @DisplayName("SandBlock 생성 테스트")
    void testSandBlockCreation() {
        Block baseBlock = new ZBlock();

        assertDoesNotThrow(() -> {
            SandBlock sandBlock = new SandBlock(baseBlock);
            assertNotNull(sandBlock, "SandBlock이 null이면 안됩니다");
            assertTrue(sandBlock.width() > 0, "SandBlock의 너비는 0보다 커야 합니다");
            assertTrue(sandBlock.height() > 0, "SandBlock의 높이는 0보다 커야 합니다");
        }, "SandBlock 생성은 예외를 발생시키지 않아야 합니다");
    }

    @Test
    @DisplayName("ItemManager 생성 테스트")
    void testItemManagerCreation() {
        assertDoesNotThrow(() -> {
            ItemManager itemManager = new ItemManager(true);
            assertNotNull(itemManager, "ItemManager가 null이면 안됩니다");
        }, "ItemManager 생성은 예외를 발생시키지 않아야 합니다");
    }

    @Test
    @DisplayName("ItemManager 아이템 모드 비활성화 테스트")
    void testItemManagerDisabled() {
        ItemManager itemManager = new ItemManager(false);

        // 아이템 모드가 비활성화되면 아이템 생성 안됨
        assertFalse(itemManager.shouldSpawnItem(10),
                "아이템 모드가 비활성화되면 shouldSpawnItem은 false를 반환해야 합니다");
        assertFalse(itemManager.shouldSpawnItem(20),
                "아이템 모드가 비활성화되면 shouldSpawnItem은 false를 반환해야 합니다");
    }

    @Test
    @DisplayName("ItemManager 아이템 생성 조건 테스트")
    void testItemManagerShouldSpawnItem() {
        ItemManager itemManager = new ItemManager(true);

        // 10줄 미만에서는 아이템 생성 안됨
        assertFalse(itemManager.shouldSpawnItem(5),
                "5줄에서는 아이템을 생성하지 않아야 합니다");
        assertFalse(itemManager.shouldSpawnItem(9),
                "9줄에서는 아이템을 생성하지 않아야 합니다");

        // 10줄에서 아이템 생성
        assertTrue(itemManager.shouldSpawnItem(10),
                "10줄에서는 아이템을 생성해야 합니다");

        // 10줄 이후 19줄까지는 아이템 생성 안됨
        assertFalse(itemManager.shouldSpawnItem(15),
                "15줄에서는 아이템을 생성하지 않아야 합니다");

        // 20줄에서 다시 아이템 생성
        assertTrue(itemManager.shouldSpawnItem(20),
                "20줄에서는 아이템을 생성해야 합니다");
    }

    @Test
    @DisplayName("ItemManager 랜덤 아이템 생성 테스트")
    void testItemManagerSpawnRandomItem() {
        ItemManager itemManager = new ItemManager(true);

        // 여러 번 호출하여 다양한 아이템이 생성되는지 확인
        for (int i = 0; i < 10; i++) {
            Block item = itemManager.spawnRandomItem();
            assertNotNull(item, "생성된 아이템은 null이 아니어야 합니다");
            assertTrue(item instanceof Block, "생성된 아이템은 Block 타입이어야 합니다");
        }
    }

    @Test
    @DisplayName("ItemManager 비활성화 시 아이템 생성 안됨 테스트")
    void testItemManagerSpawnRandomItemWhenDisabled() {
        ItemManager itemManager = new ItemManager(false);

        Block item = itemManager.spawnRandomItem();
        assertNull(item, "아이템 모드가 비활성화되면 null을 반환해야 합니다");
    }

    @Test
    @DisplayName("모든 아이템 블록 회전 테스트")
    void testAllItemBlocksRotation() {
        // SingleBlock
        SingleBlock single = new SingleBlock();
        assertDoesNotThrow(() -> single.rotate(),
                "SingleBlock 회전은 예외를 발생시키지 않아야 합니다");

        // weightedBlock
        weightedBlock weighted = new weightedBlock();
        assertDoesNotThrow(() -> weighted.rotate(),
                "weightedBlock 회전은 예외를 발생시키지 않아야 합니다");

        // LItem
        LItem lItem = new LItem(new TBlock());
        assertDoesNotThrow(() -> lItem.rotate(),
                "LItem 회전은 예외를 발생시키지 않아야 합니다");

        // BombBlock
        BombBlock bomb = new BombBlock(new JBlock());
        assertDoesNotThrow(() -> bomb.rotate(),
                "BombBlock 회전은 예외를 발생시키지 않아야 합니다");
    }

    @Test
    @DisplayName("ItemBlock 추상 클래스 테스트")
    void testItemBlockSubclasses() {
        // SingleBlock과 weightedBlock이 ItemBlock을 상속하는지 확인
        SingleBlock single = new SingleBlock();
        assertTrue(single instanceof ItemBlock,
                "SingleBlock은 ItemBlock을 상속해야 합니다");

        weightedBlock weighted = new weightedBlock();
        assertTrue(weighted instanceof ItemBlock,
                "weightedBlock은 ItemBlock을 상속해야 합니다");
    }

    @Test
    @DisplayName("LItem과 BombBlock이 Block을 상속하는지 테스트")
    void testLItemAndBombBlockInheritance() {
        LItem lItem = new LItem(new IBlock());
        assertTrue(lItem instanceof Block,
                "LItem은 Block을 상속해야 합니다");

        BombBlock bomb = new BombBlock(new OBlock());
        assertTrue(bomb instanceof Block,
                "BombBlock은 Block을 상속해야 합니다");
    }
}
