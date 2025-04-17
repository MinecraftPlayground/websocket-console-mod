package dev.loat.web_socket_console.web_socket.client;

import org.java_websocket.WebSocket;

public class WebSocketClient {

    public final WebSocket connection;
    private boolean isVerified;

    public WebSocketClient(WebSocket connection) {
        this.connection = connection;
        this.isVerified = false;
    }

    public WebSocketClient(
        WebSocket connection,
        boolean verified
    ) {
        this.connection = connection;
        this.isVerified = verified;
    }

    /**
     * @return If the client is already verified.
     */
    public boolean isVerified() {
        return this.isVerified;
    }


    void verify() {
        this.isVerified = true;
    }
}
