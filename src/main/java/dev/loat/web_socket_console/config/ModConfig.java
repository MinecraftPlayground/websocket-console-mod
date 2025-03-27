package dev.loat.web_socket_console.config;

import dev.loat.web_socket_console.config.files.WebSocketConsoleConfig;

public class ModConfig {
    public static final WebSocketConsoleConfig WEB_SOCKET_CONFIG = WebSocketConsoleConfig.createAndLoad();

    public void reload() {
        ModConfig.WEB_SOCKET_CONFIG.load();
    }
}
