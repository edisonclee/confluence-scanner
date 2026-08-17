package com.edison.scanner.provider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Loads trading symbols from resources/symbols.txt.
 */
public final class SymbolProvider {

    private static final String RESOURCE = "/symbols.txt";

    /**
     * Loads all symbols.
     *
     * @return immutable symbol list
     */
    public List<String> load() {

        InputStream input =
                SymbolProvider.class.getResourceAsStream(
                        RESOURCE);

        if (input == null) {
            throw new IllegalStateException(
                    "Unable to find resource: " + RESOURCE);
        }

        try (BufferedReader reader =
                     new BufferedReader(
                             new InputStreamReader(
                                     input,
                                     StandardCharsets.UTF_8))) {

            return reader.lines()
                    .map(String::trim)
                    .filter(line -> !line.isEmpty())
                    .filter(line -> !line.startsWith("#"))
                    .map(String::toUpperCase)
                    .distinct()
                    .collect(Collectors.toUnmodifiableList());

        } catch (IOException ex) {
        	ex.printStackTrace();

            throw new IllegalStateException(
                    "Unable to read " + RESOURCE,
                    ex);

        }

    }

}