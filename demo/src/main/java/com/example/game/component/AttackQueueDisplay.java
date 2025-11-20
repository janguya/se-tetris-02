package com.example.game.component;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * 공격 대기열을 시각적으로 표시하는 컴포넌트
 * 상대방으로부터 받을 공격 줄을 미리 보여줌
 */
public class AttackQueueDisplay {
    
    private VBox container;
    private Label titleLabel;
    private Canvas previewCanvas;
    private GraphicsContext gc;
    private Label countLabel;
    
    // 설정
    private static final int PREVIEW_WIDTH = 10; // 10칸
    private static final int PREVIEW_MAX_LINES = 5; // 최대 5줄 표시
    private static final int CELL_SIZE = 15; // 각 셀 크기
    private static final Color ATTACK_BLOCK_COLOR = Color.rgb(120, 120, 120); // 회색
    private static final Color EMPTY_COLOR = Color.rgb(40, 40, 40); // 어두운 배경
    private static final Color BORDER_COLOR = Color.rgb(80, 80, 80);
    
    // 데이터
    private Queue<String[]> attackQueue;
    
    public AttackQueueDisplay(String playerName) {
        this.attackQueue = new LinkedList<>();
        initializeUI(playerName);
    }
    
    private void initializeUI(String playerName) {
        container = new VBox(10);
        container.setAlignment(Pos.CENTER);
        container.setPadding(new Insets(10));
        container.getStyleClass().add("attack-queue-display");
        container.setStyle(
            "-fx-background-color: rgba(30, 30, 30, 0.8);" +
            "-fx-background-radius: 8;" +
            "-fx-border-color: rgba(255, 100, 100, 0.5);" +
            "-fx-border-width: 2;" +
            "-fx-border-radius: 8;"
        );
        
        // 타이틀
        titleLabel = new Label("⚠ 공격 대기");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        titleLabel.setStyle("-fx-text-fill: #ff6b6b;");
        
        // 프리뷰 캔버스
        int canvasWidth = PREVIEW_WIDTH * CELL_SIZE + 2;
        int canvasHeight = PREVIEW_MAX_LINES * CELL_SIZE + 2;
        previewCanvas = new Canvas(canvasWidth, canvasHeight);
        gc = previewCanvas.getGraphicsContext2D();
        
        // 카운트 레이블
        countLabel = new Label("0 줄");
        countLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
        countLabel.setStyle("-fx-text-fill: #ffffff;");
        
        container.getChildren().addAll(titleLabel, previewCanvas, countLabel);
        
        // 초기 그리기
        draw();
    }
    
    /**
     * 공격 줄 추가
     */
    public void addAttackLines(List<String[]> lines) {
        for (String[] line : lines) {
            attackQueue.offer(line);
        }
        updateDisplay();
    }
    
    /**
     * 공격 줄 제거 (실제 보드에 적용됨)
     */
    public String[] pollAttackLine() {
        String[] line = attackQueue.poll();
        updateDisplay();
        return line;
    }
    
    /**
     * 대기 중인 공격 줄 개수
     */
    public int getQueueSize() {
        return attackQueue.size();
    }
    
    /**
     * 실제 대기 중인 공격 줄 개수와 동기화
     * PlayerBoard의 실제 큐 크기와 AttackDisplay의 표시를 동기화함
     */
    public void syncWithActualQueue(int actualQueueSize) {
        // 현재 표시된 크기와 실제 크기가 다르면 조정
        int currentSize = attackQueue.size();
        
        if (actualQueueSize < currentSize) {
            // 실제 큐가 작으면 Display에서 제거
            while (attackQueue.size() > actualQueueSize) {
                attackQueue.poll();
            }
        } else if (actualQueueSize > currentSize) {
            // 실제 큐가 크면 Display에 더미 줄 추가 (모두 attack-block)
            while (attackQueue.size() < actualQueueSize) {
                String[] dummyLine = new String[PREVIEW_WIDTH];
                for (int i = 0; i < PREVIEW_WIDTH; i++) {
                    dummyLine[i] = "attack-block";
                }
                attackQueue.offer(dummyLine);
            }
        }
        
        updateDisplay();
    }
    
    /**
     * 대기열 비우기
     */
    public void clear() {
        attackQueue.clear();
        updateDisplay();
    }
    
    /**
     * 전체 대기열 가져오기
     */
    public Queue<String[]> getQueue() {
        return attackQueue;
    }
    
    /**
     * 표시 업데이트
     */
    private void updateDisplay() {
        countLabel.setText(attackQueue.size() + " 줄");
        draw();
    }
    
    /**
     * 캔버스에 공격 대기열 그리기
     */
    private void draw() {
        // 배경 지우기
        gc.setFill(Color.rgb(20, 20, 20));
        gc.fillRect(0, 0, previewCanvas.getWidth(), previewCanvas.getHeight());
        
        if (attackQueue.isEmpty()) {
            // 빈 상태 표시
            gc.setFill(Color.rgb(80, 80, 80));
            gc.setFont(Font.font("Arial", FontWeight.NORMAL, 10));
            gc.fillText("공격 없음", 10, PREVIEW_MAX_LINES * CELL_SIZE / 2);
            return;
        }
        
        // 대기열에서 최대 PREVIEW_MAX_LINES 줄까지 표시
        List<String[]> linesToShow = new LinkedList<>();
        int count = 0;
        for (String[] line : attackQueue) {
            if (count >= PREVIEW_MAX_LINES) break;
            linesToShow.add(line);
            count++;
        }
        
        // 아래에서 위로 그리기 (가장 먼저 받을 공격이 아래에)
        int yPos = (PREVIEW_MAX_LINES - linesToShow.size()) * CELL_SIZE;
        
        for (String[] line : linesToShow) {
            drawLine(line, yPos);
            yPos += CELL_SIZE;
        }
        
        // 테두리
        gc.setStroke(BORDER_COLOR);
        gc.setLineWidth(1);
        gc.strokeRect(0, 0, previewCanvas.getWidth(), previewCanvas.getHeight());
    }
    
    /**
     * 한 줄 그리기
     */
    private void drawLine(String[] line, int yPos) {
        for (int col = 0; col < PREVIEW_WIDTH && col < line.length; col++) {
            int xPos = col * CELL_SIZE + 1;
            
            if (line[col] != null && line[col].equals("attack-block")) {
                // 공격 블록 (회색)
                gc.setFill(ATTACK_BLOCK_COLOR);
                gc.fillRect(xPos, yPos + 1, CELL_SIZE - 1, CELL_SIZE - 1);
                
                // 블록 테두리
                gc.setStroke(ATTACK_BLOCK_COLOR.brighter());
                gc.strokeRect(xPos, yPos + 1, CELL_SIZE - 1, CELL_SIZE - 1);
            } else {
                // 빈 칸 (어두운 배경)
                gc.setFill(EMPTY_COLOR);
                gc.fillRect(xPos, yPos + 1, CELL_SIZE - 1, CELL_SIZE - 1);
            }
        }
    }
    
    /**
     * UI 컨테이너 반환
     */
    public VBox getContainer() {
        return container;
    }
}
