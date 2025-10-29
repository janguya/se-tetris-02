package com.example.game.items;

/**
 * 싱글 블록 - 1x1 크기의 아이템 블록
 */
public class SingleBlock extends ItemBlock {
    
    public SingleBlock() {
        super();
        // 1x1 블록
        shape = new int[][] {
            {1}
        };
        //cssClass = "item-single";
    }
    
    /**
     * 독립 아이템 블록은 회전 불가
     */
    @Override
    public void rotate() {
        // 1x1 블록은 회전해도 동일하므로 아무것도 하지 않음
    }
}
