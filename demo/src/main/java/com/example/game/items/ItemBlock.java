package com.example.game.items;
import com.example.game.blocks.Block;

/**
 * 아이템 블록 베이스 클래스
 * Block을 상속받아 isItemBlock()을 true로 오버라이드
 */
public class ItemBlock extends Block {
    
    public ItemBlock() {
        super();
    }
    
    @Override
    public boolean isItemBlock() {
        return true;
    }
}