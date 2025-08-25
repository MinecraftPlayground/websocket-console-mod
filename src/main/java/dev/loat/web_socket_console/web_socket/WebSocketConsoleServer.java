package dev.loat.web_socket_console.web_socket;

import dev.loat.web_socket_console.logging.Logger;
import dev.loat.web_socket_console.web_socket.send.LogMessage;
import io.javalin.plugin.bundled.CorsPluginConfig;
import io.javalin.websocket.WsConnectContext;
import net.minecraft.server.MinecraftServer;
import io.javalin.Javalin;
import org.json.JSONObject;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class WebSocketConsoleServer {
    private final Set<WsConnectContext> clients = Collections.synchronizedSet(new HashSet<>());
    private final Javalin app;
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
        /*
        localhost:8080/log?logLevel=debug -> DEBUG Channel
        localhost:8080/log?logLevel=info -> INFO Channel
        localhost:8080/log?logLevel=warn -> WARN Channel
        localhost:8080/log?logLevel=error -> ERROR Channel

        localhost:8080/log -> localhost:8080/log?logLevel=debug&logLevel=info&logLevel=warn&logLevel=error -> All Channel

        localhost:8080/log?channel=warn&channel=error -> WARN, ERROR Channel

        */
        this.serverInstance = minecraftServerInstance;
        this.port = port;
        this.logLevel = logLevel;
        this.app = Javalin.create(config -> {
            config.showJavalinBanner = false;
            config.jetty.defaultPort = this.port;
            config.bundledPlugins.enableCors(cors -> {
                cors.addRule(CorsPluginConfig.CorsRule::anyHost);
            });
        });


        this.app.ws("/logs", ws -> {
            ws.onConnect(openEvent -> {
                clients.add(openEvent);

                openEvent.enableAutomaticPings();

                Logger.info(
                    "Client connected: {}",
                    openEvent.host()
                );
            });

            ws.onClose(closeEvent -> {
                clients.removeIf((client) -> client.session == closeEvent.session);

                Logger.info(
                    "Client disconnected: {} with code {} {}",
                    closeEvent.host(),
                    closeEvent.status(),
                    closeEvent.reason()
                );
            });

            ws.onError(errorEvent -> {
                if (errorEvent.error() != null) {
                    Logger.error(errorEvent.error().getMessage());
                }
                clients.removeIf(client -> client.session == errorEvent.session);
            });
        });

        this.app.post("/command", event -> {
            String body = event.body();
            if (body.trim().isEmpty()) {
                event.status(400).result("Missing command in body");
                return;
            }

            var message = new JSONObject(body);

            if (this.serverInstance != null) {
                Logger.info(message.toString());
//                serverInstance.execute(() -> serverInstance.getCommandManager().executeWithPrefix(
//                    serverInstance.getCommandSource(),
//                    message
//                ));
            }

            event.status(200).result("Done!");
        });

        Logger.info("Started server on *:{} with log level {}", this.port, this.logLevel);
    }

    public void start() {
        this.app.start();
    }

    public void stop() {
        this.app.stop();
    }

    public void broadcastToClients(LogMessage message) {
        synchronized (this.clients) {
            for (WsConnectContext client : this.clients) {
                client.send(message.toFormattedString());
            }
        }
    }
}
