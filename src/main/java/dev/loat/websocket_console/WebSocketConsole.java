package dev.loat.websocket_console;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.java_websocket.server.WebSocketServer;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.appender.AbstractAppender;

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

            var ctx = (LoggerContext)LogManager.getContext(false);
            var logger = ctx.getConfiguration().getLoggers().get("");
            var webSocketLogAppender = new WebSocketLogAppender();
            webSocketLogAppender.start();
            logger.addAppender(webSocketLogAppender, Level.DEBUG, null);
            ctx.updateLoggers();
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
            // LOGGER.info("WebSocket server started.");
        }

        public void broadcastToClients(String message) {
            synchronized (connections) {
                for (WebSocket conn : connections) {
                    conn.send(message);
                }
            }
        }
    }

    private static class WebSocketLogAppender extends AbstractAppender {
        protected WebSocketLogAppender() {
            super("WebSocketLogAppender", null, PatternLayout.createDefaultLayout(), false);
        }

        @Override
        public void append(LogEvent event) {
            WebSocketConsole.broadcastMessage(event.getMessage().getFormattedMessage());
        }
    }
}

