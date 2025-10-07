package com.example.game.blocks;

public class OBlock extends Block {
    public OBlock() {
        shape = new int[][] { 
            {1, 1}, 
            {1, 1}
        };
        cssClass = "block-o"; // Remove the AWT Color import and usage
    }
}