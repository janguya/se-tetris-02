package com.example.game.blocks;

public class JBlock extends Block {
    public JBlock() {
        shape = new int[][]{ 
            {1, 1, 1},
            {0, 0, 1}
        };
        cssClass = "block-j";

        itemPositions = new int[shape.length][shape[0].length];
    }
}
