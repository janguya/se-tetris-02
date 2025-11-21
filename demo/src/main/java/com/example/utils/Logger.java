package com.example.utils;

/**
 * 간단한 로깅 유틸리티
 * 개발 모드에서만 INFO 로그를 출력하고, ERROR는 항상 출력됩니다.
 */
public class Logger {

    // 개발 모드 플래그 - false로 설정하면 INFO 로그가 비활성화됩니다
    private static final boolean DEBUG_MODE = false;

    // INFO 레벨 로그 (일반 정보 - 개발 모드에서만 출력)
    public static void info(String message) {
        if (DEBUG_MODE) {
            System.out.println("[INFO] " + message);
        }
    }

    // INFO 레벨 로그 - 포맷팅 지원
    public static void info(String format, Object... args) {
        if (DEBUG_MODE) {
            System.out.println("[INFO] " + String.format(format, args));
        }
    }

    // ERROR 레벨 로그 (에러는 항상 출력)
    public static void error(String message) {
        System.err.println("[ERROR] " + message);
    }

    // ERROR 레벨 로그 with exception
    public static void error(String message, Throwable t) {
        System.err.println("[ERROR] " + message);
        if (DEBUG_MODE) {
            t.printStackTrace();
        }
    }
}
