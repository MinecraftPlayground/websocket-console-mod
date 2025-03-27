package dev.loat.web_socket_console.config.files;

import blue.endless.jankson.Comment;
import io.wispforest.owo.config.annotation.Config;
import org.apache.logging.log4j.Level;

@Config(
    wrapperName = "WebSocketConsoleConfig",
    name = "web_socket_console/config"
)
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
    public String logLevel = Level.DEBUG.name();
}
