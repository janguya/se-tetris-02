package com.example.game.blocks;

public abstract class Block {
    protected int[][] shape;
    protected String cssClass; // Changed from Color to CSS class
    
    public Block() {
        shape = new int[][]{ 
            {1, 1}, 
            {1, 1}
        };
        cssClass = "block-default";
    }
    
    public int getShape(int x, int y) {
        return shape[y][x];
    }
    
    public String getCssClass() {
        return cssClass;
    }
    
    public void setShape(int[][] newShape) {
        this.shape = newShape;
    }
    
    public void rotate() {
        int rows = shape.length;
        int cols = shape[0].length;
        int[][] rotated = new int[cols][rows];
        
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                rotated[j][rows - 1 - i] = shape[i][j];
            }
        }
        
        shape = rotated;
    }
    
    public int height() {
        return shape.length;
    }
    
    public int width() {
        if(shape.length > 0)
            return shape[0].length;
        return 0;
    }
    
    /**
     * 아이템 블록 여부 확인
     * 기본적으로 false, 아이템 블록들은 이를 오버라이드
     */
    public boolean isItemBlock() {
        return false;
    }
}