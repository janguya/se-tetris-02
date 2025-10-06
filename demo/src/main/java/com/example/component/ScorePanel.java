package com.example.component;

import java.util.Map;

import com.example.blocks.Block;
import com.example.settings.GameSettings;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

public class ScorePanel {
    private static final int NEXT_BLOCK_CANVAS_SIZE = 100;
    private static final int CELL_SIZE = 20;

    private VBox panel; // 메인 패널
    private Text scoreText; // 점수 텍스트
    private Text levelText; // 레벨 텍스트
    private Text linesText; // 라인 수 텍스트
    private Canvas nextBlockCanvas = new Canvas(100, 100); // 다음 블록 미리보기 캔버스
    private GraphicsContext nextBlockGc; // 다음 블록 그래픽 컨텍스트

    private int score = 0; // 현재 점수
    private int level = 1; // 현재 레벨
    private int linesCleared = 0; // 삭제된 라인 수

    public ScorePanel() {
        initializePanel();
    }
    
    // 패널 초기화
    private void initializePanel() {
        // 메인 패널 설정
        panel = new VBox(20);
        panel.getStyleClass().add("side-panel");
        panel.setPadding(new Insets(20));
        panel.setAlignment(Pos.TOP_CENTER);
        panel.setPrefWidth(150);
        
        createPanelContent();
    }
    
    // 패널 내용 생성
    private void createPanelContent() {
        // 타이틀
        Text titleText = new Text("TETRIS");
        titleText.getStyleClass().add("title-text");
        
        // 다음 블록 부분
        Text nextBlockTitle = new Text("Next Block:");
        nextBlockTitle.getStyleClass().add("next-block-title");

        // 다음 블록 캔버스 설정
        nextBlockCanvas = new Canvas(NEXT_BLOCK_CANVAS_SIZE, NEXT_BLOCK_CANVAS_SIZE);
        nextBlockCanvas.getStyleClass().add("next-block-canvas");
        nextBlockGc = nextBlockCanvas.getGraphicsContext2D();
        
        // 초기화
        clearNextBlockCanvas();

        // 점수 부분
        scoreText = new Text("Score: 0");
        scoreText.getStyleClass().add("score-text");

        // 레벨 부분
        levelText = new Text("Level: 1");
        levelText.getStyleClass().add("level-text");

        // 라인 수 부분
        linesText = new Text("Lines: 0");
        linesText.getStyleClass().add("lines-text");

        // 컨트롤 설명 부분
        Text controlsTitle = new Text("Controls:");
        controlsTitle.getStyleClass().add("controls-title");
        
        Text controls = new Text("↑ Rotate\n← → Move\n↓ Drop\nESC Settings");
        controls.getStyleClass().add("controls-text");
        
        // 패널에 모든 요소 추가
        panel.getChildren().addAll(
            titleText, 
            nextBlockTitle,
            nextBlockCanvas, 
            scoreText, 
            levelText, 
            linesText, 
            controlsTitle, 
            controls
        );
    }

    // 다음 블록 업데이트
    public void updateNextBlock(Block nextBlock) {
        if (nextBlock == null) {
            clearNextBlockCanvas();
            return;
        }
        
        clearNextBlockCanvas();
        
        // 다음 블록 그리기
        GameSettings gameSettings = GameSettings.getInstance();
        Map<String, Color> colorMap = gameSettings.getCurrentColors();
        Color blockColor = colorMap.get(nextBlock.getCssClass());

        // 중앙 위치 계산
        double centerX = (NEXT_BLOCK_CANVAS_SIZE - nextBlock.width() * CELL_SIZE) / 2.0;
        double centerY = (NEXT_BLOCK_CANVAS_SIZE - nextBlock.height() * CELL_SIZE) / 2.0;

        // 다음 블록 그리기
        for (int i = 0; i < nextBlock.width(); i++) {
            for (int j = 0; j < nextBlock.height(); j++) {
                if (nextBlock.getShape(i, j) == 1) {
                    double drawX = centerX + i * CELL_SIZE;
                    double drawY = centerY + j * CELL_SIZE;
                    drawNextBlockCell(drawX, drawY, blockColor);
                }
            }
        }
    }
    
    // 다음 블록 캔버스 초기화
     private void clearNextBlockCanvas() {
        nextBlockGc.setFill(Color.web("#0f1419"));
        nextBlockGc.fillRect(0, 0, NEXT_BLOCK_CANVAS_SIZE, NEXT_BLOCK_CANVAS_SIZE);
        
        // Add border
        nextBlockGc.setStroke(Color.web("#16213e"));
        nextBlockGc.setLineWidth(2);
        nextBlockGc.strokeRect(1, 1, NEXT_BLOCK_CANVAS_SIZE - 2, NEXT_BLOCK_CANVAS_SIZE - 2);
    }

    // 다음 블록 그리기
    private void drawNextBlockCell(double x, double y, Color color) {
        // Main cell
        nextBlockGc.setFill(color);
        nextBlockGc.fillRect(x + 1, y + 1, CELL_SIZE - 2, CELL_SIZE - 2);
        
        // Highlight effect (smaller for next block)
        nextBlockGc.setFill(color.brighter());
        nextBlockGc.fillRect(x + 2, y + 2, CELL_SIZE - 4, 2);
        nextBlockGc.fillRect(x + 2, y + 2, 2, CELL_SIZE - 4);
        
        // Shadow effect (smaller for next block)
        nextBlockGc.setFill(color.darker());
        nextBlockGc.fillRect(x + 2, y + CELL_SIZE - 4, CELL_SIZE - 4, 2);
        nextBlockGc.fillRect(x + CELL_SIZE - 4, y + 2, 2, CELL_SIZE - 4);
    }
    
    // 업데이트된 색상 적용
    public void updateColors(Block nextBlock) {
        updateNextBlock(nextBlock);
    }

    // 점수, 레벨, 라인 수 업데이트 메서드
    public void addScore(int points) {
        score += points;
        updateScoreDisplay();
    }
    
    public void addLines(int lines) {
        linesCleared += lines;
        updateLevel();
        updateLinesDisplay();
    }
    
    private void updateLevel() {
        int newLevel = (linesCleared / 10) + 1;
        if (newLevel != level) {
            level = newLevel;
            updateLevelDisplay();
        }
    }
    
    private void updateScoreDisplay() {
        scoreText.setText("Score: " + score);
    }
    
    private void updateLevelDisplay() {
        levelText.setText("Level: " + level);
    }
    
    private void updateLinesDisplay() {
        linesText.setText("Lines: " + linesCleared);
    }
    
    // 점수, 레벨, 라인 수 초기화
    public void resetScore() {
        score = 0;
        level = 1;
        linesCleared = 0;
        updateScoreDisplay();
        updateLevelDisplay();
        updateLinesDisplay();
    }
    
    // Getters
    public VBox getPanel() {
        return panel;
    }
    
    public int getScore() {
        return score;
    }
    
    public int getLevel() {
        return level;
    }
    
    public int getLinesCleared() {
        return linesCleared;
    }

    public Canvas getNextBlockCanvas() {
        return nextBlockCanvas;
    }

    // 한 번에 지운 라인 수에 따른 점수 계산
    public void calculateLineScore(int linesCount) {
        int baseScore = 0;
        switch (linesCount) {
            case 1:
                baseScore = 100;
                break;
            case 2:
                baseScore = 300;
                break;
            case 3:
                baseScore = 500;
                break;
            case 4:
                baseScore = 800;
                break;
        }
        
        // 레벨에 따른 점수 배율 적용
        addScore(baseScore * level);
        addLines(linesCount);
    }
}