package com.example.game.blocks;

public class ZBlock extends Block {
    	public ZBlock() {
		shape = new int[][] { 
			{1, 1, 0},
			{0, 1, 1}
		};
		cssClass = "block-z";
	}
}
