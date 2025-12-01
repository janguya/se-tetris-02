package com.example.utils;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * Logger 유틸리티 클래스 테스트
 */
public class LoggerTest {
    
    @Test
    @DisplayName("info 메서드 테스트")
    public void testInfo() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outputStream));
        
        try {
            Logger.info("Test message");
            String output = outputStream.toString();
            assertTrue(output.contains("[INFO]"));
            assertTrue(output.contains("Test message"));
        } finally {
            System.setOut(originalOut);
        }
    }
    
    @Test
    @DisplayName("info 포맷팅 메서드 테스트")
    public void testInfoWithFormatting() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outputStream));
        
        try {
            Logger.info("Score: %d, Player: %s", 1000, "Alice");
            String output = outputStream.toString();
            assertTrue(output.contains("[INFO]"));
            assertTrue(output.contains("Score: 1000"));
            assertTrue(output.contains("Player: Alice"));
        } finally {
            System.setOut(originalOut);
        }
    }
    
    @Test
    @DisplayName("error 메서드 테스트")
    public void testError() {
        ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
        PrintStream originalErr = System.err;
        System.setErr(new PrintStream(errorStream));
        
        try {
            Logger.error("Error occurred");
            String output = errorStream.toString();
            assertTrue(output.contains("[ERROR]"));
            assertTrue(output.contains("Error occurred"));
        } finally {
            System.setErr(originalErr);
        }
    }
    
    @Test
    @DisplayName("error with exception 테스트")
    public void testErrorWithException() {
        ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
        PrintStream originalErr = System.err;
        System.setErr(new PrintStream(errorStream));
        
        try {
            Exception testException = new RuntimeException("Test exception");
            Logger.error("An error happened", testException);
            String output = errorStream.toString();
            assertTrue(output.contains("[ERROR]"));
            assertTrue(output.contains("An error happened"));
            // Stack trace는 DEBUG_MODE일 때만 출력되므로 확인
            assertTrue(output.contains("Test exception"));
        } finally {
            System.setErr(originalErr);
        }
    }
    
    @Test
    @DisplayName("info 메서드 null 메시지 테스트")
    public void testInfoWithNullMessage() {
        assertDoesNotThrow(() -> Logger.info((String) null));
    }
    
    @Test
    @DisplayName("info 포맷팅 null 파라미터 테스트")
    public void testInfoFormattingWithNullParams() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outputStream));
        
        try {
            Logger.info("Value: %s", (Object) null);
            String output = outputStream.toString();
            assertTrue(output.contains("[INFO]"));
            assertTrue(output.contains("null"));
        } finally {
            System.setOut(originalOut);
        }
    }
    
    @Test
    @DisplayName("error null 메시지 테스트")
    public void testErrorWithNullMessage() {
        assertDoesNotThrow(() -> Logger.error((String) null));
    }
    
    @Test
    @DisplayName("error null exception 테스트")
    public void testErrorWithNullException() {
        ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
        PrintStream originalErr = System.err;
        System.setErr(new PrintStream(errorStream));
        
        try {
            // Logger.error(message, null)은 NullPointerException 발생 가능
            assertThrows(NullPointerException.class, () -> Logger.error("Message", null));
        } finally {
            System.setErr(originalErr);
        }
    }
    
    @Test
    @DisplayName("info 빈 문자열 테스트")
    public void testInfoWithEmptyString() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outputStream));
        
        try {
            Logger.info("");
            String output = outputStream.toString();
            assertTrue(output.contains("[INFO]"));
        } finally {
            System.setOut(originalOut);
        }
    }
    
    @Test
    @DisplayName("error 빈 문자열 테스트")
    public void testErrorWithEmptyString() {
        ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
        PrintStream originalErr = System.err;
        System.setErr(new PrintStream(errorStream));
        
        try {
            Logger.error("");
            String output = errorStream.toString();
            assertTrue(output.contains("[ERROR]"));
        } finally {
            System.setErr(originalErr);
        }
    }
    
    @Test
    @DisplayName("여러 info 호출 테스트")
    public void testMultipleInfoCalls() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outputStream));
        
        try {
            Logger.info("First message");
            Logger.info("Second message");
            Logger.info("Third message");
            
            String output = outputStream.toString();
            String[] lines = output.split(System.lineSeparator());
            assertTrue(lines.length >= 3);
        } finally {
            System.setOut(originalOut);
        }
    }
    
    @Test
    @DisplayName("info와 error 혼합 테스트")
    public void testMixedInfoAndError() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        PrintStream originalErr = System.err;
        
        System.setOut(new PrintStream(outputStream));
        System.setErr(new PrintStream(errorStream));
        
        try {
            Logger.info("Info message");
            Logger.error("Error message");
            
            String infoOutput = outputStream.toString();
            String errorOutput = errorStream.toString();
            
            assertTrue(infoOutput.contains("[INFO]"));
            assertTrue(infoOutput.contains("Info message"));
            assertTrue(errorOutput.contains("[ERROR]"));
            assertTrue(errorOutput.contains("Error message"));
        } finally {
            System.setOut(originalOut);
            System.setErr(originalErr);
        }
    }
    
    @Test
    @DisplayName("포맷팅 특수문자 테스트")
    public void testFormattingWithSpecialCharacters() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outputStream));
        
        try {
            Logger.info("Special: %d%%", 100);
            String output = outputStream.toString();
            assertTrue(output.contains("Special: 100%"));
        } finally {
            System.setOut(originalOut);
        }
    }
}
