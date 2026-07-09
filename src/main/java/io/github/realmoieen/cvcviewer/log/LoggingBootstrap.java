package io.github.realmoieen.cvcviewer.log;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public final class LoggingBootstrap {

    private static boolean initialized = false;

    private LoggingBootstrap() {
    }

    public static synchronized Path init() {
        if (initialized) {
            return logDirectory();
        }
        initialized = true;

        Path logDir = logDirectory();
        try {
            Files.createDirectories(logDir);
            FileHandler fileHandler = new FileHandler(logDir.resolve("cvc-viewer.log").toString(), 1_000_000, 3, true);
            fileHandler.setFormatter(new SimpleFormatter());
            fileHandler.setLevel(Level.ALL);
            Logger.getLogger("io.github.realmoieen.cvcviewer").addHandler(fileHandler);
            Logger.getLogger("io.github.realmoieen.cvcviewer").setLevel(Level.INFO);
        } catch (IOException e) {
            System.err.println("Failed to initialize file logging: " + e.getMessage());
        }
        return logDir;
    }

    public static Path logDirectory() {
        String os = System.getProperty("os.name", "").toLowerCase();
        String home = System.getProperty("user.home");
        if (os.contains("win")) {
            String localAppData = System.getenv("LOCALAPPDATA");
            Path base = localAppData != null ? Path.of(localAppData) : Path.of(home, "AppData", "Local");
            return base.resolve("CVC-Viewer").resolve("logs");
        } else if (os.contains("mac")) {
            return Path.of(home, "Library", "Logs", "CVC-Viewer");
        } else {
            String xdgState = System.getenv("XDG_STATE_HOME");
            Path base = xdgState != null ? Path.of(xdgState) : Path.of(home, ".local", "state");
            return base.resolve("cvc-viewer").resolve("logs");
        }
    }
}
