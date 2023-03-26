package com.woop.Squad4J.util;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.InvalidJsonException;
import com.jayway.jsonpath.JsonPath;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * A utility class providing methods to access fields from the config.json configuration file. Provides methods for
 * using JSON paths to access these fields.
 * <p>
 * See <a href="https://github.com/json-path/JsonPath#operators">this page</a> for more information on JSON paths.
 *
 * @author Robert Engle
 */
public class ConfigLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigLoader.class);
    private static Object document;

    static {
        PathMatchingResourcePatternResolver loader = new PathMatchingResourcePatternResolver();

        String json = "{}";
        InputStream is = null;
        try {
            Resource resource = loader.getResources("classpath*:config.json")[0];
            if (!resource.exists()) {
                throw new RuntimeException();
            }
            is = resource.getInputStream();
        } catch (Exception e) {
            System.out.println("config.json does not exist by the path 'src/main/resources/config.json'. Exiting.");
            e.printStackTrace();
            System.exit(1);
        }
        try {
            json = IOUtils.toString(is, StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }

        try {
            document = Configuration.defaultConfiguration().jsonProvider().parse(json);
        } catch (InvalidJsonException e) {
            System.out.println("config.json is not valid JSON.");
            System.out.println("Exiting");
            e.printStackTrace();
            System.exit(1);
        }
    }

    private ConfigLoader() {
        throw new UnsupportedOperationException("You cannot instantiate this class");
    }

    /**
     * Gets an {@link Object} from the configuration file given the path.
     * <p>
     * See <a href="https://github.com/json-path/JsonPath#operators">this page</a> for more information on JSON paths.
     *
     * @param path the JSON path to get the field for
     * @return the value at the given path
     */
    public static Object get(String path) {
        return JsonPath.read(document, path);
    }

    /**
     * Gets the value of the path from the configuration file, casting it to the given class T
     * <p>
     * See <a href="https://github.com/json-path/JsonPath#operators">this page</a> for more information on JSON paths.
     *
     * @param path the JSON path to get the field for
     * @param <T>  the type of the class
     * @return the value of the given key, casted accordingly
     */
    @SuppressWarnings("unchecked")
    public static <T> T get(String path, Class<T> clazz) {
        return (T) JsonPath.read(document, path);
    }

}
