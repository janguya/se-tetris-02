package com.example.game.blocks;
import com.example.game.items.BlockItem;

public abstract class Block {
    protected int[][] shape;
    protected String cssClass; // Changed from Color to CSS class

    protected int[][] itemPositions;
    protected BlockItem embeddedItem;
    
    public Block() {
        shape = new int[][]{ 
            {1, 1}, 
            {1, 1}
        };
        cssClass = "block-default";

        itemPositions = new int[shape.length][shape[0].length];
        embeddedItem = null;
    }
    
    public int getShape(int x, int y) {
        return shape[y][x];
    }
    
    public String getCssClass() {
        return cssClass;
    }
    //특정 위치에 아이템이 있는지 확인
    public boolean hasItemAt(int x, int y) {
        if (itemPositions == null) return false;
        if (y >= itemPositions.length || x >= itemPositions[0].length) return false;
        return itemPositions[y][x] > 0;
    }
    //특정 위치의 아이템 유형 반환
    public int getItemTypeAt(int x, int y) {
        if (itemPositions == null) return 0;
        if (y >= itemPositions.length || x >= itemPositions[0].length) return 0;
        return itemPositions[y][x];
    }
    //임베디드 아이템 정
    public void setEmbeddedItem(BlockItem item, int x, int y) {
        if (itemPositions == null) {
            itemPositions = new int[shape.length][shape[0].length];
        }
        this.embeddedItem = item;
        if (y < itemPositions.length && x < itemPositions[0].length) {
            itemPositions[y][x] = item.getTypeId();
        }
    }
    //임베디드 아이템환
    public BlockItem getEmbeddedItem() {
        return embeddedItem;
    }
    
    //아이템이 있는지 확인
    public boolean hasEmbeddedItem() {
        return embeddedItem != null;
    }
    
    //아이템 위치 배열 반환
    public int[][] getItemPositions() {
        return itemPositions;
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

         if (itemPositions != null) {
            // itemPositions의 실제 크기 사용
            int itemRows = itemPositions.length;
            int itemCols = itemPositions[0].length;
            int[][] rotatedItems = new int[itemCols][itemRows];
    
            for (int i = 0; i < itemRows; i++) {
                for (int j = 0; j < itemCols; j++) {
                    rotatedItems[j][itemRows - 1 - i] = itemPositions[i][j];
                }
            }
            itemPositions = rotatedItems;
        }
    }
    
    public int height() {
        return shape.length;
    }
    
    public int width() {
        if(shape.length > 0)
            return shape[0].length;
        return 0;
    }
    //이 블록이 아이템 블록인지 확인
    public boolean isItemBlock() {
        return false;
    }
}