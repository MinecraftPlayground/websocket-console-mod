package dev.loat.websocket_console;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import org.java_websocket.server.WebSocketServer;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.WebSocket;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class WebSocketConsole implements ModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketConsole.class);
    private static WebSocketConsoleServer webSocketServer;
    private static MinecraftServer serverInstance;

    @Override
    public void onInitialize() {

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            serverInstance = server;
            webSocketServer = new WebSocketConsoleServer(new InetSocketAddress(8080));
            webSocketServer.start();
            LOGGER.info("Starting WebSocket server on port 8080 ...");


            System.setOut(new PrintStream(new ConsoleOutputStream(System.out), true));
            System.setErr(new PrintStream(new ConsoleOutputStream(System.err), true));
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            if (webSocketServer != null) {
                try {
                    webSocketServer.stop();
                } catch (InterruptedException error) {
                    throw new RuntimeException(error);
                }
                LOGGER.info("WebSocket server stopped");
            }
        });
    }

    public static void broadcastMessage(String message) {
        if (serverInstance != null) {
            serverInstance.execute(() -> {
                Text text = Text.of(message);
                serverInstance.getPlayerManager().broadcast(text, false);
            });
        }
        if (webSocketServer != null) {
            webSocketServer.broadcastToClients(message);
        }
    }

    private static class WebSocketConsoleServer extends WebSocketServer {
        private final Set<WebSocket> connections = Collections.synchronizedSet(new HashSet<>());

        public WebSocketConsoleServer(InetSocketAddress address) {
            super(address);
        }

        @Override
        public void onOpen(WebSocket conn, ClientHandshake handshake) {
            connections.add(conn);
            conn.send("Connected to Minecraft WebSocket Console.");
        }

        @Override
        public void onClose(WebSocket conn, int code, String reason, boolean remote) {
            connections.remove(conn);
        }

        @Override
        public void onMessage(WebSocket conn, String message) {
            if (serverInstance != null) {
                serverInstance.execute(() -> serverInstance.getCommandManager().executeWithPrefix(
                    serverInstance.getCommandSource(),
                    message
                ));
            }
        }

        @Override
        public void onError(WebSocket conn, Exception exception) {
            LOGGER.error(exception.getMessage());
        }

        @Override
        public void onStart() {
            LOGGER.info("WebSocket server started.");
        }

        public void broadcastToClients(String message) {
            synchronized (connections) {
                for (WebSocket conn : connections) {
                    conn.send(message);
                }
            }
        }
    }

    private static class ConsoleOutputStream extends OutputStream {
        private final PrintStream original;

        public ConsoleOutputStream(PrintStream original) {
            this.original = original;
        }

        @Override
        public void write(int b) {
            original.write(b);
            if (b == '\n') {
                return;
            }
            WebSocketConsole.broadcastMessage(String.valueOf((char) b));
        }

        @Override
        public void write(byte @NotNull [] bytes, int off, int len) {
            String message = new String(bytes, off, len);
            original.write(bytes, off, len);
            WebSocketConsole.broadcastMessage(message);
        }
    }
}

