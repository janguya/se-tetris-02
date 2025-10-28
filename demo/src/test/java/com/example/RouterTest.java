// package com.example;

// import com.example.settings.GameSettings;

// import javafx.stage.Stage;

// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.DisplayName;

// import static org.junit.jupiter.api.Assertions.*;

// class RouterTest {
    
//     @Test
//     @DisplayName("Router 클래스 기본 테스트")
//     void testRouterBasics() {
//         // 클래스 존재 확인
//         assertDoesNotThrow(() -> {
//             Class.forName("com.example.Router");
//         });
        
//         // Router 클래스 로딩 성공 확인
//         Class<?> routerClass = Router.class;
//         assertNotNull(routerClass);
//         assertEquals("com.example.Router", routerClass.getName());
//     }

//     @Test
//     @DisplayName("Router GameSettings 의존성 테스트")
//     void testGameSettingsDependency() {
//         GameSettings settings = GameSettings.getInstance();
//         assertNotNull(settings);
//         assertTrue(settings.getWindowWidth() > 0);
//         assertTrue(settings.getWindowHeight() > 0);
//     }
    
//     @Test
//     @DisplayName("Router 메서드 존재 확인")
//     void testRouterMethods() throws Exception {
//         Stage stage = new Stage();
//         Router router = new Router(stage);
        
//         // public 메서드들 존재 확인
//         assertNotNull(router.getClass().getMethod("setSize", Integer.class, Integer.class));
//         assertNotNull(router.getClass().getMethod("route", String.class));
//         assertNotNull(router.getClass().getMethod("showGame"));
//         assertNotNull(router.getClass().getMethod("showSettings"));
//         assertNotNull(router.getClass().getMethod("showSettings", Runnable.class));
//         assertNotNull(router.getClass().getMethod("showScoreboard"));
//         assertNotNull(router.getClass().getMethod("showStartMenu"));

//         // private 메서드들 존재 확인
//         assertNotNull(router.getClass().getDeclaredMethod("currentWidth"));
//         assertNotNull(router.getClass().getDeclaredMethod("currentHeight"));
//         assertNotNull(router.getClass().getDeclaredMethod("createSettingsCallback"));
//         assertNotNull(router.getClass().getDeclaredMethod("updateStageSize"));
//     }
    
//     @Test
//     @DisplayName("Router 필드 존재 확인")
//     void testRouterFields() throws Exception {
//         Stage stage = new Stage();
//         Router router = new Router(stage);

//         assertNotNull(router.getClass().getDeclaredField("stage"));
//         assertNotNull(router.getClass().getDeclaredField("overrideWidth"));
//         assertNotNull(router.getClass().getDeclaredField("overrideHeight"));
//         assertNotNull(router.getClass().getDeclaredField("savedX"));
//         assertNotNull(router.getClass().getDeclaredField("savedY"));
//     }
// }