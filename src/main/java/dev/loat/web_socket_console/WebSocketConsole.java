package dev.loat.web_socket_console;

import dev.loat.web_socket_console.config.ModConfig;
import dev.loat.web_socket_console.console.LogAppender;
import dev.loat.web_socket_console.logging.Logger;
import dev.loat.web_socket_console.web_socket.WebSocketConsoleServer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LoggerContext;

import org.apache.logging.log4j.LogManager;

public class WebSocketConsole implements ModInitializer {
    private static WebSocketConsoleServer webSocketConsoleServer;
    private static MinecraftServer serverInstance;

    @Override
    public void onInitialize() {
        Logger.setLoggerClass(WebSocketConsole.class);

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            int port = ModConfig.WEB_SOCKET_CONFIG.port();
            Level logLevel = Level.valueOf(ModConfig.WEB_SOCKET_CONFIG.logLevel());

            WebSocketConsole.serverInstance = server;
            WebSocketConsole.webSocketConsoleServer = new WebSocketConsoleServer(
                WebSocketConsole.serverInstance,
                port
            );
            Logger.info("Starting WebSocket server");
            WebSocketConsole.webSocketConsoleServer.start();

            var ctx = (LoggerContext)LogManager.getContext(false);
            var logger = ctx.getConfiguration().getLoggers().get("");
            var webSocketLogAppender = new LogAppender(WebSocketConsole.webSocketConsoleServer);
            webSocketLogAppender.start();
            logger.addAppender(webSocketLogAppender, logLevel, null);
            ctx.updateLoggers();
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            if (webSocketConsoleServer != null) {
                try {
                    webSocketConsoleServer.stop();
                } catch (InterruptedException error) {
                    throw new RuntimeException(error);
                }

                Logger.info("Stopping WebSocket server");
            }
        });
    }
}

