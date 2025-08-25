package dev.loat.web_socket_console.console;

public class LogLevel {
    public static String fromString(String level) {
        return switch (level.toLowerCase()) {
            case "debug" -> Level.DEBUG;
            case "info" -> Level.INFO;
            case "warn" -> Level.WARN;
            case "error" -> Level.ERROR;
            default -> Level.DEBUG;
        };
    }
}
