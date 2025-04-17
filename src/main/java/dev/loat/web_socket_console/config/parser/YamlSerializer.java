package dev.loat.web_socket_console.config.parser;

import dev.loat.web_socket_console.config.parser.annotation.Comment;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

import java.io.FileWriter;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
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
        DumperOptions options = new DumperOptions();
        options.setIndent(2);
        options.setPrettyFlow(true);
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

        Representer representer = new Representer(options);
        representer.addClassTag(configClass, Tag.MAP);

        Yaml yaml = new Yaml(representer, options);

        String tempFile = filePath + ".tmp";
        try (FileWriter writer = new FileWriter(tempFile)) {
            yaml.dump(config, writer);
        }

        Map<String, Object> yamlData;
        try (InputStream inputStream = Files.newInputStream(Paths.get(tempFile))) {
            yamlData = new Yaml().load(inputStream);
        }

        List<String> modifiedLines = new ArrayList<>();
        Field[] fields = configClass.getDeclaredFields();

        for (Field field : fields) {
            Comment comment = field.getAnnotation(Comment.class);
            String fieldName = field.getName();

            if (comment != null) {
                String[] commentLines = comment.value().split("\n");
                for (String commentLine : commentLines) {
                    String trimmedLine = commentLine.trim();
                    if (!trimmedLine.isEmpty()) {
                        modifiedLines.add("# " + trimmedLine);
                    } else {
                        modifiedLines.add("#");
                    }
                }
            }

            if (yamlData != null && yamlData.containsKey(fieldName)) {
                Object value = yamlData.get(fieldName);
                String yamlValue = yaml.dump(value).trim();
                if (yamlValue.contains("\n")) {
                    yamlValue = yamlValue.substring(0, yamlValue.indexOf('\n'));
                }
                modifiedLines.add(fieldName + ": " + yamlValue);
            } else {
                modifiedLines.add(fieldName + ": null");
            }
        }

        Files.write(Paths.get(filePath), modifiedLines);
        Files.deleteIfExists(Paths.get(tempFile));
    }

    /**
     * Parses a YAML file and returns a configuration object.
     *
     * @return The configuration object reflecting the YAML file structure.
     * @throws Exception If an error occurs while reading the file or casting.
     */
    public ConfigClass parse() throws Exception {
        try (InputStream inputStream = Files.newInputStream(Paths.get(filePath))) {
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