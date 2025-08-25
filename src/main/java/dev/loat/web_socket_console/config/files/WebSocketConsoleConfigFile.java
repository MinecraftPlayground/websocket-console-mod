package dev.loat.web_socket_console.config.files;

import dev.loat.web_socket_console.config.parser.annotation.Comment;
import dev.loat.web_socket_console.console.Level;

public class WebSocketConsoleConfigFile {
    @Comment("The Port to use for the WebSocket server.")
    public int port = 8080;

    @Comment("""
    The LogLevel to use for the WebSocket server.
    
    - "DEBUG"
    - "INFO"
    - "WARN"
    - "ERROR"
    """)
    public String logLevel = Level.INFO;
}
