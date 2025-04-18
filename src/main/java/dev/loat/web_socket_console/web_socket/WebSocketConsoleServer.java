package dev.loat.web_socket_console.web_socket;

import dev.loat.web_socket_console.http.URLParameters;
import dev.loat.web_socket_console.logging.Logger;
import dev.loat.web_socket_console.web_socket.send.LogMessage;
import net.minecraft.server.MinecraftServer;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

public class WebSocketConsoleServer extends WebSocketServer {
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
        Map<String, List<String>> parameters;

        Logger.info("http://localhost" + clientHandshake.getResourceDescriptor());
        try {
            var uri = new URI("http://localhost" + clientHandshake.getResourceDescriptor());
            uri.getPath();
            parameters = new URLParameters(uri).getParameters();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

//        try {
//            new URI(connection.getRemoteSocketAddress().getHostString() + connection.getResourceDescriptor()).toURL().getQuery();
//        } catch (URISyntaxException e) {
//            throw new RuntimeException(e);
//        }


        /*
        localhost:8080/log?channel=debug -> DEBUG Channel
        localhost:8080/log?channel=info -> INFO Channel
        localhost:8080/log?channel=warn -> WARN Channel
        localhost:8080/log?channel=error -> ERROR Channel

        localhost:8080/log -> localhost:8080/log?channel=debug&channel=info&channel=warn&channel=error -> All Channel

        localhost:8080/log?channel=warn&channel=error -> WARN, ERROR Channel

        */

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
        Logger.debug(message);

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
