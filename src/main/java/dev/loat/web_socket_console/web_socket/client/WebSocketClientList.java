package dev.loat.web_socket_console.web_socket.client;

import org.java_websocket.WebSocket;

import java.util.HashSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class WebSocketClientList {
    private final HashSet<WebSocketClient> clients = new HashSet<>();
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private final int timeout;

    /**
     * This class represents a list of WebSocket clients.
     *
     * @param timeout The timeout for verification in MS
     */
    public WebSocketClientList(int timeout) {
        this.timeout = timeout;
    }

    /**
     * This function removes a client from the list.
     *
     * @param connection The connection of the client to remove
     */
    public void removeClient(WebSocket connection) {
        this.clients.removeIf((client) -> client.connection == connection);
    }

    /**
     * This function removes a client from the list.
     *
     * @param client The client to remove
     */
    public void removeClient(WebSocketClient client) {
        this.clients.remove(client);
    }

    /**
     * This function adds a client to the list.
     *
     * @param connection The connection of the client to add
     */
    public void addClient(WebSocket connection) {
        var client = new WebSocketClient(connection);
        this.clients.add(client);
        this.executor.schedule(
            () -> {
                if (!client.isVerified()) {
                    this.removeClient(client);
                }
            },
            this.timeout,
            TimeUnit.MILLISECONDS
        );
    }

    /**
     * @return A list of all clients.
     */
    public HashSet<WebSocketClient> getClients() {
        return this.clients;
    }

    /**
     * This function gets a client by the provided connection.
     *
     * @param connection The connection of the client
     * @return The client.
     */
    public WebSocketClient getClient(WebSocket connection) {
        return this.clients.stream().filter(
            (client) -> client.connection == connection
        ).findFirst().orElseThrow();
    }
}
