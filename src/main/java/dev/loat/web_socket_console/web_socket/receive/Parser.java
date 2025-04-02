package dev.loat.web_socket_console.web_socket.receive;

import dev.loat.web_socket_console.logging.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONString;

import java.util.*;
import java.util.function.Consumer;

public class Parser {
    private static final Map<String, List<Consumer<JSONObject>>> listeners = new HashMap<>();

    /**
     * This function parses the message.
     *
     * @param message The message to parse
     */
    public static void parse(String message) {
        JSONObject messageObject;
        String type;
        JSONObject payload;

        try {
            messageObject = new JSONObject(message);
        } catch (JSONException error) {
            Logger.error("Failed to parse message: {}", error.getMessage());
            return;
        }

        try {
            type = messageObject.getString("type");
        } catch (JSONException error) {
            Logger.error("Failed to parse type: {}", error.getMessage());
            return;
        }

        try {
            payload = messageObject.getJSONObject("payload");
        } catch (JSONException error) {
            Logger.error("Failed to parse payload: {}", error.getMessage());
            return;
        }

        Parser.listeners.getOrDefault(type, new ArrayList<>()).forEach((callback) -> {
            callback.accept(payload);
        });
    }

    /**
     * This function allows to add listeners to specific message types.
     *
     * @param type The type of the message
     * @param callback The function to call when receiving the message
     */
    public static void addListener(
        String type,
        Consumer<JSONObject> callback
    ) {
        var callbacks = Parser.listeners.computeIfAbsent(type, (empty) -> new ArrayList<>());

        callbacks.add(callback);
    }
}
