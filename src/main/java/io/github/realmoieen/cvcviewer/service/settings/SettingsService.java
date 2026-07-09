package io.github.realmoieen.cvcviewer.service.settings;

import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class SettingsService {

    private static final Logger LOGGER = Logger.getLogger(SettingsService.class.getName());
    private static final String THEME_KEY = "theme";
    private static final String UPDATE_PAUSED_UNTIL_KEY = "updateNotificationsPausedUntil";

    private static AppSettings cached;

    private SettingsService() {
    }

    public static synchronized AppSettings load() {
        if (cached == null) {
            cached = readFromDisk();
        }
        return cached;
    }

    public static synchronized void save(AppSettings settings) {
        cached = settings;
        writeToDisk(settings);
    }

    private static AppSettings readFromDisk() {
        AppSettings settings = new AppSettings();
        Path file = settingsFile();
        if (!Files.isRegularFile(file)) {
            return settings;
        }
        try {
            JSONObject json = new JSONObject(Files.readString(file));
            String theme = json.optString(THEME_KEY, ThemePreference.SYSTEM.name());
            try {
                settings.setTheme(ThemePreference.valueOf(theme));
            } catch (IllegalArgumentException e) {
                settings.setTheme(ThemePreference.SYSTEM);
            }
            if (json.has(UPDATE_PAUSED_UNTIL_KEY) && !json.isNull(UPDATE_PAUSED_UNTIL_KEY)) {
                settings.setUpdateNotificationsPausedUntilEpochMilli(json.getLong(UPDATE_PAUSED_UNTIL_KEY));
            }
        } catch (IOException | RuntimeException e) {
            LOGGER.log(Level.WARNING, "Failed to read settings file, using defaults: " + file, e);
        }
        return settings;
    }

    private static void writeToDisk(AppSettings settings) {
        Path file = settingsFile();
        JSONObject json = new JSONObject();
        json.put(THEME_KEY, settings.getTheme().name());
        if (settings.getUpdateNotificationsPausedUntilEpochMilli() != null) {
            json.put(UPDATE_PAUSED_UNTIL_KEY, settings.getUpdateNotificationsPausedUntilEpochMilli());
        }
        try {
            Files.createDirectories(file.getParent());
            Files.writeString(file, json.toString(2));
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to write settings file: " + file, e);
        }
    }

    private static Path settingsFile() {
        return settingsDirectory().resolve("settings.json");
    }

    private static Path settingsDirectory() {
        String os = System.getProperty("os.name", "").toLowerCase();
        String home = System.getProperty("user.home");
        if (os.contains("win")) {
            String localAppData = System.getenv("LOCALAPPDATA");
            Path base = localAppData != null ? Path.of(localAppData) : Path.of(home, "AppData", "Local");
            return base.resolve("CVC-Viewer");
        } else if (os.contains("mac")) {
            return Path.of(home, "Library", "Application Support", "CVC-Viewer");
        } else {
            String xdgConfig = System.getenv("XDG_CONFIG_HOME");
            Path base = xdgConfig != null ? Path.of(xdgConfig) : Path.of(home, ".config");
            return base.resolve("cvc-viewer");
        }
    }
}
