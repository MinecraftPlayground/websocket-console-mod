package dev.loat.web_socket_console.http;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class URLParameters {
    private final String query;

    /**
     * @param url The url to get parameters from
     */
    public URLParameters(URI url) {

        var query = url.getQuery();
        this.query = Objects.requireNonNullElse(query, "");
    }

    public Map<String, List<String>> getParameters() {
        var parameters = new HashMap<String, List<String>>();

        if (this.query.isEmpty()) {
            return parameters;
        }

        for (var parameter : this.query.split("&")) {
            var keyValue = parameter.split("=", 2);

            var values = parameters.computeIfAbsent(keyValue[0], key -> new ArrayList<>());

            if (keyValue.length == 1) {
                values.add(null);
            } else {
                values.add(keyValue[1]);
            }
        }

        return parameters;
    }

    public List<String> getParameter(String key) {
        return getParameters().get(key);
    }
}
