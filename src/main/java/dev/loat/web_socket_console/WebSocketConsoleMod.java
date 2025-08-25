package dev.loat.web_socket_console;

import dev.loat.web_socket_console.config.files.WebSocketConsoleConfigFile;
import dev.loat.web_socket_console.config.parser.YamlSerializer;
import dev.loat.web_socket_console.console.LogAppender;
import dev.loat.web_socket_console.logging.Logger;
import dev.loat.web_socket_console.web_socket.WebSocketConsoleServer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LoggerContext;

import org.apache.logging.log4j.LogManager;

import java.io.File;

public class WebSocketConsoleMod implements ModInitializer {
    private static WebSocketConsoleServer webSocketConsoleServer;
    private static MinecraftServer serverInstance;

    private static WebSocketConsoleConfigFile modConfig;

    @Override
    public void onInitialize() {
        Logger.setLoggerClass(WebSocketConsoleMod.class);

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            var path = FabricLoader.getInstance().getConfigDir().resolve("web_socket_console/config.yaml");
            var config = new YamlSerializer<>(
                path.toString(),
                WebSocketConsoleConfigFile.class
            );

            try {
                if(!new File(path.toString()).exists()) {
                    config.serialize(new WebSocketConsoleConfigFile());
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            try {
                WebSocketConsoleMod.modConfig = config.parse();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            WebSocketConsoleMod.serverInstance = server;
            WebSocketConsoleMod.webSocketConsoleServer = new WebSocketConsoleServer(
                WebSocketConsoleMod.serverInstance,
                WebSocketConsoleMod.modConfig.port,
                WebSocketConsoleMod.modConfig.logLevel
            );
            Logger.info("Starting WebSocket server");
            WebSocketConsoleMod.webSocketConsoleServer.start();

            var ctx = (LoggerContext)LogManager.getContext(false);
            var logger = ctx.getConfiguration().getLoggers().get("");
            var webSocketLogAppender = new LogAppender(WebSocketConsoleMod.webSocketConsoleServer);
            webSocketLogAppender.start();
            logger.addAppender(
                webSocketLogAppender,
                Level.valueOf(WebSocketConsoleMod.modConfig.logLevel),
                null
            );
            ctx.updateLoggers();
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            if (webSocketConsoleServer != null) {
                webSocketConsoleServer.stop();

                Logger.info("Stopping WebSocket server");
            }
        });
    }
}

