package dev.loat.web_socket_console.web_socket;

import dev.loat.web_socket_console.logging.Logger;
import dev.loat.web_socket_console.web_socket.client.WebSocketClientList;
import net.minecraft.server.MinecraftServer;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class WebSocketConsoleServer extends WebSocketServer {
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private final Set<WebSocket> clients = Collections.synchronizedSet(new HashSet<>());
    private final MinecraftServer serverInstance;
    private final int port;
    private final String logLevel;

    /**
     * This class represents a WebSocket server for the minecraft server console.
     *
     * @param minecraftServerInstance The instance of the Minecraft server
     * @param port The port for the WebSocket server
     */
    public WebSocketConsoleServer(
        MinecraftServer minecraftServerInstance,
        int port,
        String logLevel
    ) {
        super(new InetSocketAddress(port));
        this.serverInstance = minecraftServerInstance;
        this.port = port;
        this.logLevel = logLevel;
    }

    @Override
    public void onOpen(
        WebSocket connection,
        ClientHandshake clientHandshake
    ) {
        this.clients.add(connection);

        Logger.info("Client connected: {}", connection.getRemoteSocketAddress());
    }

    @Override
    public void onClose(
        WebSocket connection,
        int code,
        String reason,
        boolean remote
    ) {
        clients.remove(connection);

        Logger.info(
            "Client disconnected: {} with code {}",
            connection.getRemoteSocketAddress(),
            code
        );
    }

    @Override
    public void onMessage(
        WebSocket connection,
        String message
    ) {
//        Logger.info(message);

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
        Logger.info("Started WebSocket server on *:{} with log level {}", this.port, this.logLevel);
    }

    public void broadcastToClients(LogMessage message) {
        synchronized (this.clients) {
            for (WebSocket connection : this.clients) {
                connection.send(message.toFormattedString());
            }
        }
    }
}
