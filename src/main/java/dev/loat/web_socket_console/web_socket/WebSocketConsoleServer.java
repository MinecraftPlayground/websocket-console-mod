package dev.loat.web_socket_console.web_socket;

import dev.loat.web_socket_console.logging.Logger;
import net.minecraft.server.MinecraftServer;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class WebSocketConsoleServer extends WebSocketServer {
    private final Set<WebSocket> connections = Collections.synchronizedSet(new HashSet<>());
    private final MinecraftServer serverInstance;
    private final int port;

    /**
     * This class represents a WebSocket server for the minecraft server console.
     *
     * @param minecraftServerInstance The instance of the Minecraft server
     * @param port The port for the WebSocket server
     */
    public WebSocketConsoleServer(
        MinecraftServer minecraftServerInstance,
        int port
    ) {
        super(new InetSocketAddress(port));
        this.serverInstance = minecraftServerInstance;
        this.port = port;
    }

    @Override
    public void onOpen(
        WebSocket connection,
        ClientHandshake clientHandshake
    ) {
        connections.add(connection);

        Logger.info("New client connected: {}", connection.getRemoteSocketAddress());
    }

    @Override
    public void onClose(
        WebSocket connection,
        int code,
        String reason,
        boolean remote
    ) {
        connections.remove(connection);

        Logger.info(
            "Existing client disconnected: {} with code {}",
            connection.getRemoteSocketAddress(),
            code
        );
    }

    @Override
    public void onMessage(
        WebSocket connection,
        String message
    ) {
        if (this.serverInstance != null) {
            serverInstance.execute(() -> serverInstance.getCommandManager().executeWithPrefix(
                serverInstance.getCommandSource(),
                message
            ));
        }
    }

    @Override
    public void onError(
        WebSocket webSocket,
        Exception exception
    ) {
        Logger.error(exception.toString());
    }

    @Override
    public void onStart() {
        Logger.info("Started WebSocket server on *:{}", this.port);
    }

    public void broadcastToClients(LogMessage message) {
        synchronized (this.connections) {
            for (WebSocket connection : this.connections) {
                connection.send(message.toFormattedString());
            }
        }
    }
}
