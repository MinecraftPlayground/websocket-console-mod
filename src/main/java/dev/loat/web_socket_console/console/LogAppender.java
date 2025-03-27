package dev.loat.web_socket_console.console;

import dev.loat.web_socket_console.web_socket.LogMessage;
import dev.loat.web_socket_console.web_socket.WebSocketConsoleServer;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.layout.PatternLayout;

public class LogAppender extends AbstractAppender {
    private final WebSocketConsoleServer webSocketServer;

    public LogAppender(
        WebSocketConsoleServer webSocketServer
    ) {
        super(
            "WebSocketLogAppender",
            null,
            PatternLayout.createDefaultLayout(),
            false,
            null
        );
        this.webSocketServer = webSocketServer;
    }

    @Override
    public void append(LogEvent event) {
        this.webSocketServer.broadcastToClients(new LogMessage(event));
    }
}
