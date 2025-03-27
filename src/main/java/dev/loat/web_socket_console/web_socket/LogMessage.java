package dev.loat.web_socket_console.web_socket;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.message.Message;
import org.json.JSONObject;

public class LogMessage {
    private final long timestamp;
    private final String threadName;
    private final Level logLevel;
    private final String className;
    private final Message message;
    private final long threadId;
    private final ThreadContext.ContextStack stackStraceSource;

    public LogMessage(LogEvent event) {
        this.timestamp = event.getTimeMillis();
        this.threadName = event.getThreadName();
        this.threadId = event.getThreadId();
        this.logLevel = event.getLevel();
        this.className = event.getLoggerName();
        this.message = event.getMessage();
        this.stackStraceSource = event.getContextStack();
    }

    public String toFormattedString() {
        JSONObject data = new JSONObject();
        Object simpleClassName = JSONObject.NULL;

        try {
            simpleClassName = Class.forName(this.className).getSimpleName();
        } catch (ClassNotFoundException ignored) {}

        data.put("timestamp", this.timestamp);
        data.put("threadName", this.threadName);
        data.put("threadId", this.threadId);
        data.put("logLevel", this.logLevel);
        data.put("className", this.className);
        data.put("simpleClassName", simpleClassName);
        data.put("message", this.message.getFormattedMessage());
        data.put("error", this.stackStraceSource);
        return data.toString();
    }
}
