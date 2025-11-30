package com.example;

import java.util.List;

import com.example.game.component.OnlineVersusBoard;
import com.example.game.component.SingleBoard;
import com.example.game.component.VersusAIBoard;
import com.example.game.component.VersusBoard;
import com.example.game.component.VersusGameModeDialog;
import com.example.gameover.GameOverScene;
import com.example.gameover.ScoreManager;
import com.example.network.NetworkLobbyDialog;
import com.example.network.NetworkManager;
import com.example.settings.GameSettings;
import com.example.settings.SettingsDialog;
import com.example.startmenu.StartMenuView;

import javafx.scene.Scene;
import javafx.stage.Stage;

public class Router {
    private final Stage stage;
    private Integer overrideWidth = null;
    private Integer overrideHeight = null;
    private Double savedX = null; // 창 위치 저장
    private Double savedY = null;
    private OnlineVersusBoard currentOnlineBoard = null; // 현재 온라인 게임 보드 추적

    public Router(Stage stage) {
        this.stage = stage;
    }

    public Router(Stage stage, Integer width, Integer height) {
        this.stage = stage;
        this.overrideWidth = width;
        this.overrideHeight = height;
    }

    // x로 창 껐을 때 정리 작업
    private void setupGlobalCloseHandler() {
        stage.setOnCloseRequest(event -> {
            System.out.println("Window close requested - cleaning up...");
            
            // 온라인 게임 보드가 활성화되어 있으면 정리
            if (currentOnlineBoard != null) {
                System.out.println("Cleaning up OnlineVersusBoard...");
                currentOnlineBoard.cleanup();
                currentOnlineBoard = null;
            }
            
            // 기본 종료 허용
            System.out.println("Window closing...");
        });
    }

    public void setSize(Integer width, Integer height) {
        this.overrideWidth = width;
        this.overrideHeight = height;
        updateStageSize();
    }

    private int currentWidth() {
        if (overrideWidth != null)
            return overrideWidth;
        return GameSettings.getInstance().getWindowWidth();
    }

    private int currentHeight() {
        if (overrideHeight != null)
            return overrideHeight;
        return GameSettings.getInstance().getWindowHeight();
    }

    // 설정 변경 콜백 생성
    private Runnable createSettingsCallback() {
        return () -> {
            System.out.println("Settings changed - clearing override and updating size");

            // 현재 창 위치 저장 (화면이 표시되어 있다면)
            if (stage.isShowing()) {
                savedX = stage.getX();
                savedY = stage.getY();
            }

            // override 값 제거하여 GameSettings 값 사용
            this.overrideWidth = null;
            this.overrideHeight = null;

            // 크기 업데이트 후 시작 메뉴로 돌아가기
            showStartMenu();
        };
    }

    // 스테이지 크기 업데이트
    private void updateStageSize() {
        System.out.println("Updating stage size to: " + currentWidth() + "x" + currentHeight());

        // Avoid reusing the same root Node in a new Scene (causes
        // "is already set as root of another scene"), so don't create
        // a new Scene from the existing root. Instead, resize the
        // existing stage directly.
        if (stage.getScene() != null) {
            // Set explicit stage width/height based on settings. This
            // preserves the current Scene and its root Node.
            stage.setWidth(currentWidth());
            stage.setHeight(currentHeight());

            // Ensure the window respects the new size
            stage.sizeToScene();

            // Restore saved position if available
            if (savedX != null && savedY != null) {
                stage.setX(savedX);
                stage.setY(savedY);
            } else if (!stage.isShowing()) {
                stage.centerOnScreen();
            }
        }
    }

    public void route(String menuId) {
        if (menuId == null)
            return;
        switch (menuId) {
            case "GAME":
                showGame();
                break;
            case "ITEM_MODE":
                toggleItemMode();
                break;
            case "VERSUS_GAME":
                showVersusGame();
                break;
            case "AI_VERSUS_GAME":
                showVersusAIGame();
                break;
            case "P2P_VERSUS_MODE":
                showOnlineVersusGame();
                break;
            case "SETTINGS":
                showSettings();
                break;
            case "SCOREBOARD":
                showScoreboard();
                break;
            case "EXIT":
                exit();
                break;
            default:
                // unknown
        }
    }

    private void toggleItemMode() {
        GameSettings settings = GameSettings.getInstance();
        settings.setItemModeEnabled(!settings.isItemModeEnabled());
        // 메뉴 새로고침
        showStartMenu();
    }

public void showGame() {
    SingleBoard gameBoard = new SingleBoard(new SingleBoard.SingleGameCallback() {
        @Override
        public void onGameOver(int score, int linesCleared) {
            System.out.println("=== 게임 종료 ===");
            System.out.println("최종 점수: " + score);
            System.out.println("삭제한 줄: " + linesCleared);
            // GameOverScene이 이미 표시되므로 여기서는 로그만 출력
        }
        
        @Override
        public void onGameEnd() {
            // 게임 종료 시 시작 메뉴로 (GameOverScene에서 메인 메뉴 버튼 클릭 시)
            showStartMenu();
        }
    });
    
    Scene gameScene = new Scene(gameBoard.getRoot(), currentWidth(), currentHeight());
    try {
        gameScene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
    } catch (Exception e) {
        // ignore missing stylesheet
    }
    
    stage.setScene(gameScene);
    stage.setResizable(false);
    stage.sizeToScene();

    // 저장된 위치가 있으면 사용, 없으면 중앙 정렬
    if (savedX != null && savedY != null) {
        stage.setX(savedX);
        stage.setY(savedY);
    } else {
        stage.centerOnScreen();
    }

    stage.show();
    gameBoard.getRoot().requestFocus();
}

    // 대전 모드
    public void showVersusGame() {
        // 대전 모드 선택 다이얼로그 표시
        VersusGameModeDialog.show(stage, new VersusGameModeDialog.ModeSelectionCallback() {
            @Override
            public void onModeSelected(VersusGameModeDialog.VersusMode mode) {
                startVersusGame(mode);
            }
            
            @Override
            public void onCancel() {
                // 시작 메뉴로 돌아가기
                showStartMenu();
            }
        });
    }

    /**
     * 대전 게임 시작
     */
    private void startVersusGame(VersusGameModeDialog.VersusMode mode) {
        VersusBoard versusBoard = new VersusBoard(stage, mode, new VersusBoard.VersusGameCallback() {
            @Override
            public void onPlayerWin(int winnerPlayer, int player1Score, int player2Score) {
                // 승리 메시지 출력 후 시작 메뉴로
                System.out.println("=== 대전 모드 게임 종료 ===");
                System.out.println("승자: Player " + winnerPlayer);
                System.out.println("Player 1 점수: " + player1Score);
                System.out.println("Player 2 점수: " + player2Score);
                
                // 대전 모드는 스코어보드에 기록하지 않음
                showStartMenu();
            }
            
            @Override
            public void onGameEnd() {
                // 게임 종료 시 시작 메뉴로
                showStartMenu();
            }
        });
        
        // 대전 모드 Scene 생성
        Scene gameScene = new Scene(versusBoard.getRoot(), currentWidth()*2, currentHeight()*1.3); // TODO : 크기 조정
        try {
            gameScene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        } catch (Exception e) {
            // ignore missing stylesheet
        }
        
        stage.setScene(gameScene);
        stage.setResizable(false);
        stage.sizeToScene();
        
        // 저장된 위치가 있으면 사용, 없으면 중앙 정렬
        if (savedX != null && savedY != null) {
            stage.setX(savedX);
            stage.setY(savedY);
        } else {
            stage.centerOnScreen();
        }
        
        stage.show();
        versusBoard.getRoot().requestFocus(); // 키 입력을 위해 포커스 설정
    }

    /**
     * AI 대전 모드
     */
    public void showVersusAIGame() {
        // 대전 모드 선택 다이얼로그 표시
        VersusGameModeDialog.show(stage, new VersusGameModeDialog.ModeSelectionCallback() {
            @Override
            public void onModeSelected(VersusGameModeDialog.VersusMode mode) {
                startVersusAIGame(mode);
            }
            
            @Override
            public void onCancel() {
                // 시작 메뉴로 돌아가기
                showStartMenu();
            }
        });
    }

    /**
     * AI 대전 게임 시작
     */
    private void startVersusAIGame(VersusGameModeDialog.VersusMode mode) {
        VersusAIBoard versusAIBoard = new VersusAIBoard(stage, mode, new VersusAIBoard.VersusGameCallback() {
            @Override
            public void onPlayerWin(int winnerPlayer, int player1Score, int player2Score) {
                System.out.println("=== AI 대전 모드 게임 종료 ===");
                System.out.println("승자: " + (winnerPlayer == 1 ? "Player" : "AI"));
                System.out.println("Player 점수: " + player1Score);
                System.out.println("AI 점수: " + player2Score);
                
                showStartMenu();
            }
            
            @Override
            public void onGameEnd() {
                showStartMenu();
            }
        });
        
        // AI 대전 모드 Scene 생성
        Scene gameScene = new Scene(versusAIBoard.getRoot(), currentWidth()*2, currentHeight()*1.3);
        try {
            gameScene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        } catch (Exception e) {
            // ignore missing stylesheet
        }
        
        stage.setScene(gameScene);
        stage.setResizable(false);
        stage.sizeToScene();
        
        if (savedX != null && savedY != null) {
            stage.setX(savedX);
            stage.setY(savedY);
        } else {
            stage.centerOnScreen();
        }
        
        stage.show();
        versusAIBoard.getRoot().requestFocus();
    }

    // 매개변수 없는 showSettings (메뉴에서 호출)
    public void showSettings() {
        SettingsDialog settingsDialog = new SettingsDialog(stage, createSettingsCallback());
        settingsDialog.show();
    }

    // 매개변수 있는 showSettings (Board.java에서 호출)
    public void showSettings(Runnable additionalCallback) {
        // 기본 콜백과 추가 콜백을 조합
        Runnable combinedCallback = () -> {
            createSettingsCallback().run(); // 기본 크기 변경 콜백
            if (additionalCallback != null) {
                additionalCallback.run(); // 추가 콜백 (Board의 onSettingsChanged)
            }
        };

        SettingsDialog settingsDialog = new SettingsDialog(stage, combinedCallback);
        settingsDialog.show();
    }

    public void showScoreboard() {
        System.out.println("\n=== Router.showScoreboard() ===");

        // ⭐ JSON 파일에서 실제 점수 불러오기 ⭐
        List<GameOverScene.ScoreEntry> scores = ScoreManager.loadScores();

        System.out.println("Loaded " + scores.size() + " scores from file");

        // 점수가 없으면 안내 메시지용 더미 데이터 추가
        if (scores.isEmpty()) {
            System.out.println("No scores found, showing empty scoreboard");
        }

        // 현재 플레이어는 null (스코어보드 보기만 하는 경우)
        Scene scoreScene = GameOverScene.create(stage, scores, null, currentWidth(), currentHeight());
        stage.setScene(scoreScene);
        stage.setResizable(false);
        stage.sizeToScene();

        // 저장된 위치가 있으면 사용, 없으면 중앙 정렬
        if (savedX != null && savedY != null) {
            stage.setX(savedX);
            stage.setY(savedY);
        } else {
            stage.centerOnScreen();
        }

        stage.show();
    }

    public void showStartMenu() {

        // 온라인 게임 보드가 있으면 정리
        if (currentOnlineBoard != null) {
            System.out.println("Cleaning up OnlineVersusBoard before showing menu...");
            currentOnlineBoard.cleanup();
            currentOnlineBoard = null;
    }

        GameSettings settings = GameSettings.getInstance();
        String itemModeLabel = settings.isItemModeEnabled() ? "아이템 모드: ON" : "아이템 모드: OFF";

        StartMenuView startMenu = new StartMenuView()
                .addMenuItem("GAME", "게임 시작")
                .addMenuItem("ITEM_MODE", itemModeLabel)
                .addMenuItem("VERSUS_GAME", "대전 모드")
                // .addMenuItem("AI_VERSUS_GAME", "AI 대전 모드") // AI 대전 모드는 일단 숨김
                .addMenuItem("P2P_VERSUS_MODE", "P2P 대전 모드")
                .addMenuItem("SETTINGS", "설정")
                .addMenuItem("SCOREBOARD", "스코어보드")
                .addMenuItem("EXIT", "종료")
                .setOnMenuSelect(this::route)
                .build();

        Scene scene = new Scene(startMenu.getRoot(), currentWidth(), currentHeight());
        try {
            scene.getStylesheets().add(getClass().getResource("/tetris-menu.css").toExternalForm());
        } catch (Exception e) {
            // ignore
        }
        stage.setTitle("Tetris");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.sizeToScene();

        // 저장된 위치가 있으면 사용, 없으면 중앙 정렬
        if (savedX != null && savedY != null) {
            stage.setX(savedX);
            stage.setY(savedY);
        } else {
            stage.centerOnScreen();
        }

        stage.show();
        startMenu.requestFocus();
    }

    public void exit() {
        System.exit(0);
    }

    // P2P 대전 모드
    public void showOnlineVersusGame() {
    // 로비 다이얼로그 표시
        NetworkLobbyDialog.show(stage, new NetworkLobbyDialog.LobbyCallback() {
            @Override
            public void onServerCreated(NetworkManager networkManager) {
                // 서버 생성 성공 - 게임 모드 선택
                selectOnlineGameMode(networkManager, true);
            }
        
            @Override
            public void onClientConnected(NetworkManager networkManager) {
                // 클라이언트 연결 성공 - 게임 모드 선택
                selectOnlineGameMode(networkManager, false);
            }
        
            @Override
            public void onCancelled() {
                // 취소 - 시작 메뉴로
                showStartMenu();
            }
        });
    }

    // 온라인 대전 게임 모드 선택
    private void selectOnlineGameMode(NetworkManager networkManager, boolean isServer) {
        if (isServer) {
            VersusGameModeDialog.show(stage, new VersusGameModeDialog.ModeSelectionCallback() {
           
                @Override
                public void onModeSelected(VersusGameModeDialog.VersusMode mode) {
                    startOnlineVersusGame(networkManager, mode, isServer);
                }
        
                @Override
                public void onCancel() {
                    // 취소 - 연결 종료 후 메뉴로
                    networkManager.shutdown();
                    showStartMenu();
                }
            });
        } else {
        // 클라이언트는 서버의 모드 선택 대기
        // null로 시작 (OnlineVersusBoard에서 GAME_START 메시지 받으면 모드 설정)
        startOnlineVersusGame(networkManager, null, isServer);
        }
    }

    // 온라인 대전 게임 시작
    private void startOnlineVersusGame(NetworkManager networkManager, 
                                  VersusGameModeDialog.VersusMode mode, 
                                  boolean isServer) {

        // 이전 온라인 보드가 있으면 정리
        if (currentOnlineBoard != null) {
            currentOnlineBoard.cleanup();
        }
        
        // 새 온라인 보드 생성 및 추적
        currentOnlineBoard = new OnlineVersusBoard(stage, mode, networkManager, isServer);
    
        Scene gameScene = new Scene(currentOnlineBoard.getRoot(), currentWidth()*2, currentHeight()*1.3);
        try {
            gameScene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        } catch (Exception e) {
            // ignore missing stylesheet
        }
    
        stage.setScene(gameScene);
        stage.setResizable(false);
        stage.sizeToScene();
    
        if (savedX != null && savedY != null) {
            stage.setX(savedX);
            stage.setY(savedY);
        } else {
            stage.centerOnScreen();
        }
    
        // 창 닫을 때 정리
        stage.setOnCloseRequest(e -> {
            currentOnlineBoard.cleanup();
        });
    
        stage.show();
        currentOnlineBoard.getRoot().requestFocus();
    }
}
