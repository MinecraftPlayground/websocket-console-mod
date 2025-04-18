package dev.loat.web_socket_console.config.parser;

import dev.loat.web_socket_console.config.parser.annotation.Comment;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class YamlSerializer<ConfigClass> {
    private final String filePath;
    private final Class<ConfigClass> configClass;

    public YamlSerializer(String filePath, Class<ConfigClass> configClass) {
        this.filePath = filePath;
        this.configClass = configClass;
    }

    /**
     * Serializes a configuration object to a YAML file with comments.
     *
     * @param config The configuration object to serialize.
     * @throws Exception If an error occurs while writing the file.
     */
    public void serialize(ConfigClass config) throws Exception {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            Files.createDirectories(path.getParent());
            Files.createFile(path);
        }

        DumperOptions options = new DumperOptions();
        options.setIndent(2);
        options.setPrettyFlow(true);
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

        Representer representer = new Representer(options);
        representer.addClassTag(configClass, Tag.MAP);

        Yaml yaml = new Yaml(representer, options);

        String yamlString = yaml.dump(config);
        String[] lines = yamlString.split("\n");

        // Kommentare für Felder mit @Comment-Annotation sammeln
        Field[] fields = configClass.getDeclaredFields();
        Map<String, List<String>> commentsMap = new HashMap<>();
        for (Field field : fields) {
            Comment comment = field.getAnnotation(Comment.class);
            if (comment != null) {
                String[] commentLines = comment.value().split("\n");
                List<String> formattedComments = new ArrayList<>();
                for (String commentLine : commentLines) {
                    String trimmed = commentLine.trim();
                    if (!trimmed.isEmpty()) {
                        formattedComments.add("# " + trimmed);
                    } else {
                        formattedComments.add("#");
                    }
                }
                commentsMap.put(field.getName(), formattedComments);
            }
        }

        // YAML-Zeilen mit Kommentaren anreichern
        List<String> modifiedLines = new ArrayList<>();
        for (String line : lines) {
            for (String fieldName : commentsMap.keySet()) {
                if (line.startsWith(fieldName + ":")) {
                    modifiedLines.addAll(commentsMap.get(fieldName));
                    break;
                }
            }
            modifiedLines.add(line);
        }

        // Ergebnis in die Zieldatei schreiben
        Files.write(path, modifiedLines);
    }

    /**
     * Parses a YAML file and returns a configuration object.
     *
     * @return The configuration object reflecting the YAML file structure.
     * @throws Exception If an error occurs while reading the file or casting.
     */
    public ConfigClass parse() throws Exception {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            // Erstelle die Ordnerstruktur und Datei, falls sie nicht existieren
            Files.createDirectories(path.getParent());
            Files.createFile(path);
            throw new IllegalStateException("YAML file was newly created and is empty: " + filePath);
        }

        try (InputStream inputStream = Files.newInputStream(path)) {
            LoaderOptions loaderOptions = new LoaderOptions();
            loaderOptions.setAllowDuplicateKeys(false);
            Constructor constructor = new Constructor(configClass, loaderOptions);
            Yaml yaml = new Yaml(constructor);
            Object result = yaml.load(inputStream);
            if (result == null) {
                throw new IllegalStateException("No data found in YAML file: " + filePath);
            }
            return configClass.cast(result);
        }
    }
}
