package com.example.game.blocks;

public class TBlock extends Block {
	
	public TBlock() {
		shape = new int[][] { 
			{0, 1, 0},
			{1, 1, 1}
		};
		cssClass = "block-t";
		itemPositions = new int[shape.length][shape[0].length];
	}
}