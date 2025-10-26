package com.example.game.blocks;

public class SBlock extends Block {

	public SBlock() {
		shape = new int[][] { 
			{0, 1, 1},
			{1, 1, 0}
		};
		cssClass = "block-s";
		itemPositions = new int[shape.length][shape[0].length];
	}
}