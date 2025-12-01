package com.example.network;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * ConnectionConfig 클래스의 단위 테스트
 */
public class ConnectionConfigTest {
    
    @Test
    public void testDefaultConstructor() {
        ConnectionConfig config = new ConnectionConfig();
        
        assertEquals(ConnectionConfig.DEFAULT_PORT, config.getPort());
        assertEquals(ConnectionConfig.DEFAULT_TIMEOUT, config.getConnectionTimeout());
        assertEquals(ConnectionConfig.DEFAULT_TIMEOUT, config.getReadTimeout());
        assertEquals(ConnectionConfig.DEFAULT_BUFFER_SIZE, config.getBufferSize());
        assertEquals(ConnectionConfig.DEFAULT_PING_INTERVAL, config.getPingInterval());
        assertEquals(ConnectionConfig.MAX_LATENCY_MS, config.getMaxLatency());
        assertTrue(config.isAutoReconnect());
        assertEquals(3, config.getMaxReconnectAttempts());
    }
    
    @Test
    public void testPortConstructor() {
        ConnectionConfig config = new ConnectionConfig(30000);
        assertEquals(30000, config.getPort());
    }
    
    @Test
    public void testSetPortValid() {
        ConnectionConfig config = new ConnectionConfig();
        config.setPort(8080);
        assertEquals(8080, config.getPort());
    }
    
    @Test
    public void testSetPortInvalidLow() {
        ConnectionConfig config = new ConnectionConfig();
        assertThrows(IllegalArgumentException.class, () -> {
            config.setPort(1023);
        });
    }
    
    @Test
    public void testSetPortInvalidHigh() {
        ConnectionConfig config = new ConnectionConfig();
        assertThrows(IllegalArgumentException.class, () -> {
            config.setPort(65536);
        });
    }
    
    @Test
    public void testSetConnectionTimeoutValid() {
        ConnectionConfig config = new ConnectionConfig();
        config.setConnectionTimeout(10000);
        assertEquals(10000, config.getConnectionTimeout());
    }
    
    @Test
    public void testSetConnectionTimeoutInvalid() {
        ConnectionConfig config = new ConnectionConfig();
        assertThrows(IllegalArgumentException.class, () -> {
            config.setConnectionTimeout(-1);
        });
    }
    
    @Test
    public void testSetReadTimeoutValid() {
        ConnectionConfig config = new ConnectionConfig();
        config.setReadTimeout(8000);
        assertEquals(8000, config.getReadTimeout());
    }
    
    @Test
    public void testSetReadTimeoutInvalid() {
        ConnectionConfig config = new ConnectionConfig();
        assertThrows(IllegalArgumentException.class, () -> {
            config.setReadTimeout(-100);
        });
    }
    
    @Test
    public void testSetBufferSizeValid() {
        ConnectionConfig config = new ConnectionConfig();
        config.setBufferSize(16384);
        assertEquals(16384, config.getBufferSize());
    }
    
    @Test
    public void testSetBufferSizeInvalid() {
        ConnectionConfig config = new ConnectionConfig();
        assertThrows(IllegalArgumentException.class, () -> {
            config.setBufferSize(512);
        });
    }
    
    @Test
    public void testSetPingIntervalValid() {
        ConnectionConfig config = new ConnectionConfig();
        config.setPingInterval(2000);
        assertEquals(2000, config.getPingInterval());
    }
    
    @Test
    public void testSetPingIntervalInvalid() {
        ConnectionConfig config = new ConnectionConfig();
        assertThrows(IllegalArgumentException.class, () -> {
            config.setPingInterval(50);
        });
    }
    
    @Test
    public void testSetMaxLatencyValid() {
        ConnectionConfig config = new ConnectionConfig();
        config.setMaxLatency(300);
        assertEquals(300, config.getMaxLatency());
    }
    
    @Test
    public void testSetMaxLatencyInvalid() {
        ConnectionConfig config = new ConnectionConfig();
        assertThrows(IllegalArgumentException.class, () -> {
            config.setMaxLatency(30);
        });
    }
    
    @Test
    public void testSetAutoReconnect() {
        ConnectionConfig config = new ConnectionConfig();
        config.setAutoReconnect(false);
        assertFalse(config.isAutoReconnect());
    }
    
    @Test
    public void testSetMaxReconnectAttemptsValid() {
        ConnectionConfig config = new ConnectionConfig();
        config.setMaxReconnectAttempts(10);
        assertEquals(10, config.getMaxReconnectAttempts());
    }
    
    @Test
    public void testSetMaxReconnectAttemptsInvalid() {
        ConnectionConfig config = new ConnectionConfig();
        assertThrows(IllegalArgumentException.class, () -> {
            config.setMaxReconnectAttempts(0);
        });
    }
    
    @Test
    public void testIsValidWithDefaultConfig() {
        ConnectionConfig config = new ConnectionConfig();
        assertTrue(config.isValid());
    }
    
    @Test
    public void testIsValidWithInvalidPort() {
        ConnectionConfig config = new ConnectionConfig();
        try {
            config.setPort(1000);
        } catch (IllegalArgumentException e) {
            // Expected
        }
        // 예외 발생으로 포트가 변경되지 않음
        assertTrue(config.isValid());
    }
    
    @Test
    public void testToString() {
        ConnectionConfig config = new ConnectionConfig();
        String str = config.toString();
        
        assertNotNull(str);
        assertTrue(str.contains("ConnectionConfig"));
        assertTrue(str.contains("port="));
        assertTrue(str.contains("timeout="));
    }
    
    @Test
    public void testCreateLocalNetworkConfig() {
        ConnectionConfig config = ConnectionConfig.createLocalNetworkConfig();
        
        assertNotNull(config);
        assertEquals(3000, config.getConnectionTimeout());
        assertEquals(3000, config.getReadTimeout());
        assertEquals(500, config.getPingInterval());
        assertEquals(100, config.getMaxLatency());
        assertTrue(config.isAutoReconnect());
        assertEquals(5, config.getMaxReconnectAttempts());
        assertTrue(config.isValid());
    }
    
    // ============== Branch Coverage 테스트 ==============
    
    @Test
    public void testIsValidWithPortBoundaries() {
        ConnectionConfig config = new ConnectionConfig();
        
        // 유효한 최소 포트
        config.setPort(1024);
        assertTrue(config.isValid());
        
        // 유효한 최대 포트
        config.setPort(65535);
        assertTrue(config.isValid());
    }
    
    @Test
    public void testIsValidWithConnectionTimeoutBoundary() {
        ConnectionConfig config = new ConnectionConfig();
        
        // 0은 유효함 (무한 대기)
        config.setConnectionTimeout(0);
        assertTrue(config.isValid());
    }
    
    @Test
    public void testIsValidWithReadTimeoutBoundary() {
        ConnectionConfig config = new ConnectionConfig();
        
        // 0은 유효함 (무한 대기)
        config.setReadTimeout(0);
        assertTrue(config.isValid());
    }
    
    @Test
    public void testIsValidWithBufferSizeBoundary() {
        ConnectionConfig config = new ConnectionConfig();
        
        // 최소 버퍼 크기
        config.setBufferSize(1024);
        assertTrue(config.isValid());
    }
    
    @Test
    public void testIsValidWithPingIntervalBoundary() {
        ConnectionConfig config = new ConnectionConfig();
        
        // 최소 핑 간격
        config.setPingInterval(100);
        assertTrue(config.isValid());
    }
    
    @Test
    public void testIsValidWithMaxLatencyBoundary() {
        ConnectionConfig config = new ConnectionConfig();
        
        // 최소 레이턴시
        config.setMaxLatency(50);
        assertTrue(config.isValid());
    }
    
    @Test
    public void testIsValidWithMaxReconnectAttemptsBoundary() {
        ConnectionConfig config = new ConnectionConfig();
        
        // 최소 재연결 시도
        config.setMaxReconnectAttempts(1);
        assertTrue(config.isValid());
    }
    
    @Test
    public void testSettersReturnValues() {
        ConnectionConfig config = new ConnectionConfig();
        
        // setMaxReconnectAttempts는 체이닝을 위해 this를 반환
        ConnectionConfig returned = config.setMaxReconnectAttempts(5);
        assertSame(config, returned, "setMaxReconnectAttempts는 this를 반환해야 함");
    }
    
    @Test
    public void testPortBoundaryEdgeCases() {
        ConnectionConfig config = new ConnectionConfig();
        
        // 1024는 유효
        assertDoesNotThrow(() -> config.setPort(1024));
        
        // 65535는 유효
        assertDoesNotThrow(() -> config.setPort(65535));
    }
    
    @Test
    public void testTimeoutZeroIsValid() {
        ConnectionConfig config = new ConnectionConfig();
        
        // 0 타임아웃은 무한 대기를 의미하므로 유효
        assertDoesNotThrow(() -> config.setConnectionTimeout(0));
        assertDoesNotThrow(() -> config.setReadTimeout(0));
        
        assertTrue(config.isValid());
    }
}
