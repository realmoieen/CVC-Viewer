package io.github.realmoieen.cvcviewer.ui.theme;

import atlantafx.base.theme.PrimerDark;
import atlantafx.base.theme.PrimerLight;
import io.github.realmoieen.cvcviewer.service.settings.ThemePreference;
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class ThemeManager {

    private static final Logger LOGGER = Logger.getLogger(ThemeManager.class.getName());
    private static final boolean WINDOWS = System.getProperty("os.name", "").toLowerCase().contains("win");

    private static volatile boolean darkModeActive = false;

    private ThemeManager() {
    }

    public static void apply(ThemePreference preference) {
        boolean dark = preference == ThemePreference.DARK
                || (preference == ThemePreference.SYSTEM && isSystemDarkMode());
        darkModeActive = dark;

        Application.setUserAgentStylesheet(dark
                ? new PrimerDark().getUserAgentStylesheet()
                : new PrimerLight().getUserAgentStylesheet());

        if (WINDOWS) {
            for (Window window : Window.getWindows()) {
                if (window instanceof Stage stage) {
                    WindowsTitleBar.setDark(stage, dark);
                }
            }
        }
    }

    /**
     * Applies the currently active theme's title bar color to a single stage - call this once,
     * right after {@code stage.show()}, for every new window (the loop in {@link #apply} only
     * reaches windows that already existed at the time the theme was changed).
     */
    public static void applyTitleBar(Stage stage) {
        if (WINDOWS) {
            WindowsTitleBar.setDark(stage, darkModeActive);
        }
    }

    private static boolean isSystemDarkMode() {
        String os = System.getProperty("os.name", "").toLowerCase();
        try {
            if (os.contains("win")) {
                return isWindowsDarkMode();
            } else if (os.contains("mac")) {
                return isMacDarkMode();
            } else {
                return isLinuxDarkMode();
            }
        } catch (Exception e) {
            LOGGER.log(Level.FINE, "System theme detection failed, defaulting to light", e);
            return false;
        }
    }

    private static boolean isWindowsDarkMode() throws IOException {
        String output = runCommand("reg", "query",
                "HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize",
                "/v", "AppsUseLightTheme");
        // Registry value 0x0 = dark mode, 0x1 = light mode
        return output.contains("0x0");
    }

    private static boolean isMacDarkMode() throws IOException {
        String output = runCommand("defaults", "read", "-g", "AppleInterfaceStyle");
        return output.trim().equalsIgnoreCase("Dark");
    }

    private static boolean isLinuxDarkMode() throws IOException {
        String output = runCommand("gsettings", "get", "org.gnome.desktop.interface", "color-scheme");
        return output.toLowerCase().contains("dark");
    }

    private static String runCommand(String... command) throws IOException {
        Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        try {
            if (!process.waitFor(2, TimeUnit.SECONDS)) {
                process.destroyForcibly();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return output;
    }
}
