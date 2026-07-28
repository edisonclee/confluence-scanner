package com.edison.scanner.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Loads the application configuration from the
 * application.properties file.
 *
 * <p>
 * This class provides read-only access to all configuration
 * values used throughout the application.
 * </p>
 */
public class ApplicationConfig {

    private final Properties properties;

    /**
     * Creates a new configuration instance and loads
     * application.properties from the classpath.
     *
     * @throws IllegalStateException
     *         if the configuration file cannot be loaded.
     */
    public ApplicationConfig() {

        properties = new Properties();

        try (InputStream input =
                getClass().getClassLoader()
                        .getResourceAsStream("application.properties")) {

            if (input == null) {
                throw new IllegalStateException(
                        "application.properties was not found.");
            }

            properties.load(input);

        } catch (IOException e) {

            throw new IllegalStateException(
                    "Unable to load application.properties.",
                    e);

        }

    }

    /**
     * Returns a configuration value.
     *
     * @param key
     *         Configuration key.
     *
     * @return configuration value.
     */
    public String get(String key) {

        return properties.getProperty(key);

    }

}