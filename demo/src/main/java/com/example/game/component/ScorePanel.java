package com.example.game.component;

import java.util.Map;

import com.example.game.blocks.Block;
import com.example.game.items.LItem;
import com.example.game.items.BombBlock;
import com.example.settings.GameSettings;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

public class ScorePanel {
    // 동적 크기 (화면 크기에 따라 조정됨)
    private int nextBlockCanvasSize;
    private int cellSize;
    private int panelWidth;

    private VBox panel; // 메인 패널
    private Text scoreText; // 점수 텍스트
    private Text levelText; // 레벨 텍스트
    private Text linesText; // 라인 수 텍스트
    private Canvas nextBlockCanvas = new Canvas(100, 100); // 다음 블록 미리보기 캔버스
    private GraphicsContext nextBlockGc; // 다음 블록 그래픽 컨텍스트

    private int score = 0; // 현재 점수
    private int level = 1; // 현재 레벨
    private int linesCleared = 0; // 삭제된 라인 수

    private Text speedText; // 속도 표시 추가

    public ScorePanel() {
        calculateDynamicSizes();
        initializePanel();
        
        // 화면 크기 변경 리스너 등록
        GameSettings.getInstance().addWindowSizeChangeListener(this::onWindowSizeChanged);
    }
    
    // 동적 크기 계산
    private void calculateDynamicSizes() {
        GameSettings settings = GameSettings.getInstance();
        int windowWidth = settings.getWindowWidth();
        int windowHeight = settings.getWindowHeight();
        
        // 창 크기에 비례하여 패널 크기 계산
        panelWidth = Math.max(120, Math.min(200, windowWidth / 4));
        nextBlockCanvasSize = Math.max(60, Math.min(120, panelWidth - 40));
        cellSize = Math.max(10, Math.min(25, nextBlockCanvasSize / 6));
    }
    
    // 화면 크기 변경 콜백
    private void onWindowSizeChanged() {
        calculateDynamicSizes();
        updatePanelSize();
        recreateCanvas();
    }
    
    // 패널 크기 업데이트
    private void updatePanelSize() {
        if (panel != null) {
            panel.setPrefWidth(panelWidth);
        }
    }
    
    // 캔버스 재생성
    private void recreateCanvas() {
        if (nextBlockCanvas != null && panel != null) {
            // 기존 캔버스를 패널에서 제거
            panel.getChildren().remove(nextBlockCanvas);
            
            // 새 캔버스 생성
            nextBlockCanvas = new Canvas(nextBlockCanvasSize, nextBlockCanvasSize);
            nextBlockCanvas.getStyleClass().add("next-block-canvas");
            nextBlockGc = nextBlockCanvas.getGraphicsContext2D();
            
            // 패널에서 "Next Block:" 텍스트 다음에 새 캔버스 추가
            int insertIndex = 2; // "TETRIS", "Next Block:" 다음
            if (insertIndex < panel.getChildren().size()) {
                panel.getChildren().add(insertIndex, nextBlockCanvas);
            }
            
            clearNextBlockCanvas();
        }
    }
    
    // 패널 초기화
    private void initializePanel() {
        // 메인 패널 설정
        panel = new VBox(20);
        panel.getStyleClass().add("side-panel");
        panel.setPadding(new Insets(20));
        panel.setAlignment(Pos.TOP_CENTER);
        panel.setPrefWidth(panelWidth);
        
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
        nextBlockCanvas = new Canvas(nextBlockCanvasSize, nextBlockCanvasSize);
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

        // 속도 부분 (새로 추가)
        speedText = new Text("Speed: x1.0");
        speedText.getStyleClass().add("speed-text");

        // 컨트롤 설명 부분
        Text controlsTitle = new Text("Controls:");
        controlsTitle.getStyleClass().add("controls-title");
        
        Text controls = new Text("↑ Rotate\n← → Move\n↓ Drop\nSPACE Pause\nESC Settings");
        controls.getStyleClass().add("controls-text");
        
        // 패널에 모든 요소 추가
        panel.getChildren().addAll(
            titleText, 
            nextBlockTitle, 
            nextBlockCanvas,
            scoreText, 
            levelText, 
            linesText,
            speedText, // 속도 표시 추가
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
        double centerX = (nextBlockCanvasSize - nextBlock.width() * cellSize) / 2.0;
        double centerY = (nextBlockCanvasSize - nextBlock.height() * cellSize) / 2.0;

        // LItem인지 확인
        boolean isLItem = nextBlock instanceof LItem;
        LItem lItem = isLItem ? (LItem) nextBlock : null;
        
        // BombBlock인지 확인
        boolean isBombBlock = nextBlock instanceof BombBlock;
        BombBlock bombBlock = isBombBlock ? (BombBlock) nextBlock : null;

        // 다음 블록 그리기
        for (int i = 0; i < nextBlock.width(); i++) {
            for (int j = 0; j < nextBlock.height(); j++) {
                if (nextBlock.getShape(i, j) == 1) {
                    double drawX = centerX + i * cellSize;
                    double drawY = centerY + j * cellSize;
                    
                    // L 마커가 있는 셀인지 확인
                    if (isLItem && lItem.hasLMarker(j, i)) {
                        // L 마커 셀은 특별한 색상으로 그리고 "L" 텍스트 추가
                        Color lMarkerColor = colorMap.get("item-lmarker");
                        drawLMarkerNextBlockCell(drawX, drawY, lMarkerColor);
                    }
                    // B 마커가 있는 셀인지 확인
                    else if (isBombBlock && bombBlock.hasBMarker(j, i)) {
                        // B 마커 셀은 검은색으로 그리고 "B" 텍스트 추가
                        Color bMarkerColor = colorMap.get("item-bmarker");
                        drawBMarkerNextBlockCell(drawX, drawY, bMarkerColor);
                    }
                    else {
                        // 일반 셀 그리기
                        drawNextBlockCell(drawX, drawY, blockColor);
                    }
                }
            }
        }
    }
    
    // 다음 블록 캔버스 초기화
     private void clearNextBlockCanvas() {
        nextBlockGc.setFill(Color.web("#0f1419"));
        nextBlockGc.fillRect(0, 0, nextBlockCanvasSize, nextBlockCanvasSize);
        
        // Add border
        nextBlockGc.setStroke(Color.web("#16213e"));
        nextBlockGc.setLineWidth(2);
        nextBlockGc.strokeRect(1, 1, nextBlockCanvasSize - 2, nextBlockCanvasSize - 2);
    }

    // 다음 블록 그리기
    private void drawNextBlockCell(double x, double y, Color color) {
        // Main cell
        nextBlockGc.setFill(color);
        nextBlockGc.fillRect(x + 1, y + 1, cellSize - 2, cellSize - 2);
        
        // Highlight effect (smaller for next block)
        nextBlockGc.setFill(color.brighter());
        nextBlockGc.fillRect(x + 2, y + 2, cellSize - 4, 2);
        nextBlockGc.fillRect(x + 2, y + 2, 2, cellSize - 4);
        
        // Shadow effect (smaller for next block)
        nextBlockGc.setFill(color.darker());
        nextBlockGc.fillRect(x + 2, y + cellSize - 4, cellSize - 4, 2);
        nextBlockGc.fillRect(x + cellSize - 4, y + 2, 2, cellSize - 4);
    }
    
    // L 마커 다음 블록 셀 그리기
    private void drawLMarkerNextBlockCell(double x, double y, Color color) {
        // Main cell (L 마커 색상)
        nextBlockGc.setFill(color);
        nextBlockGc.fillRect(x + 1, y + 1, cellSize - 2, cellSize - 2);
        
        // Highlight effect
        nextBlockGc.setFill(color.brighter());
        nextBlockGc.fillRect(x + 2, y + 2, cellSize - 4, 2);
        nextBlockGc.fillRect(x + 2, y + 2, 2, cellSize - 4);
        
        // Shadow effect
        nextBlockGc.setFill(color.darker());
        nextBlockGc.fillRect(x + 2, y + cellSize - 4, cellSize - 4, 2);
        nextBlockGc.fillRect(x + cellSize - 4, y + 2, 2, cellSize - 4);
        
        // "L" 텍스트 그리기
        nextBlockGc.setFill(Color.WHITE);
        nextBlockGc.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, cellSize * 0.5));
        
        // 텍스트 중앙 정렬을 위한 계산
        javafx.scene.text.Text tempText = new javafx.scene.text.Text("L");
        tempText.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, cellSize * 0.5));
        double textWidth = tempText.getBoundsInLocal().getWidth();
        double textHeight = tempText.getBoundsInLocal().getHeight();
        
        double textX = x + (cellSize - textWidth) / 2;
        double textY = y + (cellSize + textHeight) / 2 - 1;
        
        nextBlockGc.fillText("L", textX, textY);
    }
    
    // B 마커 다음 블록 셀 그리기
    private void drawBMarkerNextBlockCell(double x, double y, Color color) {
        // Main cell (B 마커 색상 - 검은색)
        nextBlockGc.setFill(color);
        nextBlockGc.fillRect(x + 1, y + 1, cellSize - 2, cellSize - 2);
        
        // Highlight effect
        nextBlockGc.setFill(color.brighter());
        nextBlockGc.fillRect(x + 2, y + 2, cellSize - 4, 2);
        nextBlockGc.fillRect(x + 2, y + 2, 2, cellSize - 4);
        
        // Shadow effect
        nextBlockGc.setFill(color.darker());
        nextBlockGc.fillRect(x + 2, y + cellSize - 4, cellSize - 4, 2);
        nextBlockGc.fillRect(x + cellSize - 4, y + 2, 2, cellSize - 4);
        
        // "B" 텍스트 그리기 (흰색)
        nextBlockGc.setFill(Color.WHITE);
        nextBlockGc.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, cellSize * 0.5));
        
        // 텍스트 중앙 정렬을 위한 계산
        javafx.scene.text.Text tempText = new javafx.scene.text.Text("B");
        tempText.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, cellSize * 0.5));
        double textWidth = tempText.getBoundsInLocal().getWidth();
        double textHeight = tempText.getBoundsInLocal().getHeight();
        
        double textX = x + (cellSize - textWidth) / 2;
        double textY = y + (cellSize + textHeight) / 2 - 1;
        
        nextBlockGc.fillText("B", textX, textY);
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
    
    // 리소스 정리
    public void cleanup() {
        GameSettings.getInstance().removeWindowSizeChangeListener(this::onWindowSizeChanged);
    }
    // 속도 업데이트 메서드 추가
    public void updateSpeed(double speedMultiplier, int speedLevel) {
        speedText.setText("Speed: x" + String.format("%.1f", 1.0 / speedMultiplier));
    }
}