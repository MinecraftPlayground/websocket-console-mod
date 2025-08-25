package dev.loat.web_socket_console.web_socket.receive;

import org.java_websocket.WebSocket;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class Parser {
    @FunctionalInterface
    public interface Action {
        void execute(
            String path,
            WebSocket connection,
            Map<String, Set<String>> parameters
        );
    }

    private static final Map<Pattern, Action> actions = new HashMap<>();

    public static void add(
        @NotNull String path,
        Action action
    ) {
        Parser.actions.put(Pattern.compile(path), action);
    }

    public static void remove(
        String path
    ) {
        Parser.actions.remove(Pattern.compile(path));
    }

    public static void parse(
        String path,
        WebSocket connection,
        Map<String, List<String>> parameters
    ) {

    }
}
