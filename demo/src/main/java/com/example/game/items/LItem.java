package com.example.game.items;
import com.example.game.component.GameLogic;


public class LItem extends BlockItem {

    public static final int TYPE_ID = 1;

    public LItem() {
        super("L_ITEM", "줄 삭제", 'L', "줄 삭제");
    }

    @Override
    public void activate(GameLogic gameLogic, int x, int y) {
        // L 아이템이 활성화될 때의 동작 구현
        System.out.println(x + ", " + y + ")에서 L 활성화.");

        int [][] board = gameLogic.getBoard();
        String[][] blockTypes = gameLogic.getBlockTypes();

        for (int row = y; row>0; row--){
            System.arraycopy(board[row - 1], 0, board[row], 0,GameLogic.WIDTH);
            System.arraycopy(blockTypes[row - 1], 0, blockTypes[row], 0,GameLogic.WIDTH);
        }
        for (int col = 0; col < GameLogic.WIDTH; col++) {
            board[0][col] = 0; // 최상단 행을 빈 칸으로 설정
            blockTypes[0][col] = null;
        }
        System.out.println("줄" + y + "삭제");
    }

    @Override
    public int getTypeId() {
        return TYPE_ID;
    }


}
