package com.example.blocks;

public class IBlock extends Block {
    public IBlock() {
        shape = new int[][]{ 
            {1, 1, 1, 1}
        };
        cssClass = "block-i"; // Use CSS class instead of Color.CYAN
    }
}
