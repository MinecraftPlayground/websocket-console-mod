package dev.loat.web_socket_console.config.files;

import dev.loat.web_socket_console.config.parser.annotation.Comment;

class LogLevel {
    public static String DEBUG = "DEBUG";
    public static String INFO = "INFO";
    public static String WARN = "WARN";
    public static String ERROR = "ERROR";
}

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
    public String logLevel = LogLevel.INFO;
}
