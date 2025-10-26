package com.example.game.blocks;

public class IBlock extends Block {
    public IBlock() {
        shape = new int[][]{ 
            {1, 1, 1, 1}
        };
        cssClass = "block-i"; // Use CSS class instead of Color.CYAN

        itemPositions = new int[shape.length][shape[0].length];
    }
}
