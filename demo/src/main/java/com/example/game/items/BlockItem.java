package com.example.game.items;
import com.example.game.component.GameLogic;

public abstract class BlockItem {

    protected String itemId;
    protected String itemName;
    protected char displayChar;
    protected String description;

    public BlockItem(String itemId, String itemName, char displayChar, String description) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.displayChar = displayChar;
        this.description = description;
    }
    public abstract void activate(GameLogic gameLogic, int x, int y);

    public String getItemId() {
        return itemId;
    }
    public String getItemName() {
        return itemName;
    }
    public char getDisplayChar() {
        return displayChar;
    }
    public String getDescription() {
        return description;
    }

    public abstract int getTypeId();

}
