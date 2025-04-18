package dev.loat.web_socket_console.web_socket.send;

import org.apache.logging.log4j.core.LogEvent;

public class Message {
    private final LogEvent event;

    public Message(LogEvent event) {
        this.event = event;
    }

    public String toJson() {
        
        return "";
    }
}
