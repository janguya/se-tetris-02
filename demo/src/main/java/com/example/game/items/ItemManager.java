package com.example.game.items;

import java.util.Random;
import com.example.game.blocks.Block;
import com.example.game.blocks.IBlock;
import com.example.game.blocks.JBlock;
import com.example.game.blocks.LBlock;
import com.example.game.blocks.OBlock;
import com.example.game.blocks.SBlock;
import com.example.game.blocks.TBlock;
import com.example.game.blocks.ZBlock;
import com.example.utils.Logger;

/**
 * 아이템 생성 및 관리 클래스
 * - 10줄마다 아이템 블록 생성
 * - 랜덤으로 아이템 종류 결정
 */
public class ItemManager {
    private static final int LINES_FOR_ITEM = 10; // 10줄마다 아이템 생성
    private int lastItemSpawnLines = 0; // 마지막 아이템 생성 시점의 줄 수
    private Random random;
    private boolean itemModeEnabled; // 아이템 모드 활성화 여부

    public ItemManager(boolean itemModeEnabled) {
        this.random = new Random();
        this.itemModeEnabled = itemModeEnabled;
        this.lastItemSpawnLines = 0;
    }

    /**
     * 아이템 생성 여부 확인
     * 
     * @param totalLinesCleared 총 삭제된 줄 수
     * @return 아이템을 생성해야 하면 true
     */
    public boolean shouldSpawnItem(int totalLinesCleared) {
        if (!itemModeEnabled) {
            Logger.info("ItemManager: Item mode is DISABLED");
            return false;
        }

        // 10줄마다 아이템 생성
        if (totalLinesCleared >= lastItemSpawnLines + LINES_FOR_ITEM) {
            lastItemSpawnLines = totalLinesCleared;
            Logger.info("ItemManager: Item should spawn! Total lines: %d, Last spawn: %d", totalLinesCleared,
                    lastItemSpawnLines);
            return true;
        }

        Logger.info("ItemManager: Not yet. Total: %d, Last spawn: %d, Need: %d",
                totalLinesCleared, lastItemSpawnLines, lastItemSpawnLines + LINES_FOR_ITEM);
        return false;
    }

    /**
     * 랜덤 아이템 블록 생성
     * 
     * @return 생성된 아이템 블록
     */
    public Block spawnRandomItem() {
        if (!itemModeEnabled) {
            return null;
        }

        int itemType = random.nextInt(5); // 0: SingleBlock, 1: weightedBlock, 2: LItem, 3: SandBlock, 4: BombBlock
        Block itemBlock;
        switch (itemType) {
            case 0:
                itemBlock = new SingleBlock();
                Logger.info("Item Manager: Spawned SingleBlock item");
                break;
            case 1:
                itemBlock = new weightedBlock();
                Logger.info("Item Manager: Spawned WeightedBlock item");
                break;
            case 2:
                // 랜덤 블록 생성하여 L 아이템으로 래핑
                Block baseBlock = getRandomNormalBlock();
                itemBlock = new LItem(baseBlock);
                Logger.info("Item Manager: Spawned LItem with base block: %s", baseBlock.getClass().getSimpleName());
                break;
            case 3:
                // 랜덤 블록 생성하여 Sand 아이템으로 래핑
                Block sandBase = getRandomNormalBlock();
                itemBlock = new SandBlock(sandBase);
                Logger.info("Item Manager: Spawned SandBlock with base block: %s",
                        sandBase.getClass().getSimpleName());
                break;
            case 4:
                // 랜덤 블록 생성하여 Bomb 아이템으로 래핑
                Block bombBase = getRandomNormalBlock();
                itemBlock = new BombBlock(bombBase);
                Logger.info("Item Manager: Spawned BombBlock with base block: %s",
                        bombBase.getClass().getSimpleName());
                break;
            default:
                // 기본값도 BombBlock
                Block defaultBase = getRandomNormalBlock();
                itemBlock = new BombBlock(defaultBase);
                Logger.info("Item Manager: Spawned BombBlock (default) with base block: %s",
                        defaultBase.getClass().getSimpleName());
                break;
        }

        return itemBlock;
    }

    /**
     * 랜덤 일반 블록 생성 (L 아이템용)
     */
    private Block getRandomNormalBlock() {
        int blockType = random.nextInt(7);
        switch (blockType) {
            case 0:
                return new IBlock();
            case 1:
                return new JBlock();
            case 2:
                return new LBlock();
            case 3:
                return new ZBlock();
            case 4:
                return new SBlock();
            case 5:
                return new TBlock();
            case 6:
                return new OBlock();
            default:
                return new IBlock();
        }
    }

    /**
     * 아이템 모드 활성화 여부 확인
     */
    public boolean isItemModeEnabled() {
        return itemModeEnabled;
    }

    /**
     * 아이템 모드 설정
     */
    public void setItemModeEnabled(boolean enabled) {
        this.itemModeEnabled = enabled;
        if (!enabled) {
            // 아이템 모드 비활성화 시 카운터 리셋
            lastItemSpawnLines = 0;
        }
    }

    /**
     * 아이템 매니저 리셋 (게임 재시작 시)
     */
    public void reset() {
        lastItemSpawnLines = 0;
    }

    /**
     * 다음 아이템까지 남은 줄 수
     */
    public int getLinesUntilNextItem(int totalLinesCleared) {
        if (!itemModeEnabled) {
            return -1;
        }
        int remaining = LINES_FOR_ITEM - (totalLinesCleared - lastItemSpawnLines);
        return Math.max(0, remaining);
    }
}
