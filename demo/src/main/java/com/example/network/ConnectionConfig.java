package com.example.network;


public class ConnectionConfig {
     // 기본 설정값
    public static final int DEFAULT_PORT = 25566;
    public static final int DEFAULT_TIMEOUT = 5000; // 5초
    public static final int DEFAULT_BUFFER_SIZE = 8192; // 8KB
    public static final int DEFAULT_PING_INTERVAL = 1000; // 1초
    public static final int MAX_LATENCY_MS = 200; // 200ms (요구사항)
    
    private int port;
    private int connectionTimeout;
    private int readTimeout;
    private int bufferSize;
    private int pingInterval;
    private int maxLatency;
    private boolean autoReconnect;
    private int maxReconnectAttempts;

    // 기본 설정으로 생성
    public ConnectionConfig() {
        this.port = DEFAULT_PORT;
        this.connectionTimeout = DEFAULT_TIMEOUT;
        this.readTimeout = DEFAULT_TIMEOUT;
        this.bufferSize = DEFAULT_BUFFER_SIZE;
        this.pingInterval = DEFAULT_PING_INTERVAL;
        this.maxLatency = MAX_LATENCY_MS;
        this.autoReconnect = true;
        this.maxReconnectAttempts = 3;
    }

    // 커스텀 포트로 생성
    // @param port 포트 번호
    public ConnectionConfig(int port) {
        this();
        this.port = port;
    }

    // Getters & Setters
    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        if(port < 1024 || port > 65535) {
            throw new IllegalArgumentException("포트 번호는 1024에서 65535 사이여야 합니다.");
        }
        this.port = port;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        if(connectionTimeout < 0) {
            throw new IllegalArgumentException("타임아웃은 음수일 수 없습니다.");
        }
        this.connectionTimeout = connectionTimeout;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(int readTimeout) {
        if(readTimeout < 0) {
            throw new IllegalArgumentException("타임아웃은 음수일 수 없습니다.");
        }
        this.readTimeout = readTimeout;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public void setBufferSize(int bufferSize) {
        if(bufferSize < 1024) {
            throw new IllegalArgumentException("버퍼 크기는 최소 1024 바이트여야 합니다.");
        }
        this.bufferSize = bufferSize;
    }

    public int getPingInterval() {
        return pingInterval;
    }

    public void setPingInterval(int pingInterval) {
        if(pingInterval < 100) {
            throw new IllegalArgumentException("핑 간격은 최소 100ms여야 합니다.");
        }
        this.pingInterval = pingInterval;
    }

    public int getMaxLatency() {
        return maxLatency;
    }

    public void setMaxLatency(int maxLatency) {
        if(maxLatency < 50) {
            throw new IllegalArgumentException("최대 레이턴시는 최소 50ms여야 합니다.");
        }
        this.maxLatency = maxLatency;
    }

    public boolean isAutoReconnect() {
        return autoReconnect;
    }

    public void setAutoReconnect(boolean autoReconnect) {
        this.autoReconnect = autoReconnect;
    }

    public int getMaxReconnectAttempts() {
        return maxReconnectAttempts;
    }

    public ConnectionConfig setMaxReconnectAttempts(int maxReconnectAttempts) {
        if(maxReconnectAttempts < 1) {
            throw new IllegalArgumentException("재연결 시도 횟수는 최소 1회여야 합니다.");
        }
        this.maxReconnectAttempts = maxReconnectAttempts;
        return this;
    }

    // 설정 유효성 검증
    // @return 유효하면 true
    public boolean isValid() {
        return port >= 1024 && port <= 65535 &&
               connectionTimeout >= 0 &&
               readTimeout >= 0 &&
               bufferSize >= 1024 &&
               pingInterval >= 100 &&
               maxLatency >= 50 &&
               maxReconnectAttempts >= 1;
    }

    @Override
    public String toString() {
         return String.format("ConnectionConfig{port=%d, timeout=%dms, bufferSize=%d, " +
                           "pingInterval=%dms, maxLatency=%dms, autoReconnect=%s}",
                           port, connectionTimeout, bufferSize, 
                           pingInterval, maxLatency, autoReconnect);
    }

    // 같은 와이파이 네트워크 최적화 설정 생성
    public static ConnectionConfig createLocalNetworkConfig() {
        ConnectionConfig config = new ConnectionConfig();
        config.setConnectionTimeout(3000);  // ✅ 개별 호출
        config.setReadTimeout(3000);
        config.setPingInterval(500);
        config.setMaxLatency(100);
        config.setAutoReconnect(true);
        config.setMaxReconnectAttempts(5);
        return config;
    }
}