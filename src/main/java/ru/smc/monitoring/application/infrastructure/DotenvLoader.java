package ru.smc.monitoring.application.infrastructure;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class DotenvLoader {

    private static final Path ENV_PATH = Path.of(".env");

    private DotenvLoader() {
    }

    public static void load() {
        if (!Files.exists(ENV_PATH)) {
            return;
        }

        try {
            List<String> lines = Files.readAllLines(ENV_PATH);
            for (String line : lines) {
                loadLine(line);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to load .env file", exception);
        }
    }

    private static void loadLine(String line) {
        String trimmed = line.trim();
        if (trimmed.isBlank() || trimmed.startsWith("#")) {
            return;
        }

        int separatorIndex = trimmed.indexOf('=');
        if (separatorIndex < 1) {
            return;
        }

        String key = trimmed.substring(0, separatorIndex).trim();
        String value = unquote(trimmed.substring(separatorIndex + 1).trim());
        if (System.getenv(key) == null && System.getProperty(key) == null) {
            System.setProperty(key, value);
        }
    }

    private static String unquote(String value) {
        if (value.length() >= 2) {
            char first = value.charAt(0);
            char last = value.charAt(value.length() - 1);
            if ((first == '"' && last == '"') || (first == '\'' && last == '\'')) {
                return value.substring(1, value.length() - 1);
            }
        }

        return value;
    }
}
