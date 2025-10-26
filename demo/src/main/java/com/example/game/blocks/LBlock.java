package com.example.game.blocks;

public class LBlock extends Block {
	
	public LBlock() {
		shape = new int[][] { 
			{1, 1, 1},
			{1, 0, 0}
		};
		cssClass = "block-l";

		itemPositions = new int[shape.length][shape[0].length];
	}
}