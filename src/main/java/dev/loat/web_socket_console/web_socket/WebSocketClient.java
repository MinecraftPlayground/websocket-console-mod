package dev.loat.web_socket_console.web_socket;

import org.java_websocket.WebSocket;


import java.util.List;

public class WebSocketClient {
    public final WebSocket connection;
    private final List<String> logLevels;

    public WebSocketClient(
        WebSocket connection,
        List<String> logLevels
    ) {
        this.connection = connection;
        this.logLevels = logLevels;
    }
}
