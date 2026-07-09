package io.github.realmoieen.cvcviewer.app;

/**
 * Entry point kept separate from {@link CvcViewerApp} on purpose: the JDK launcher
 * refuses to run "java -jar" against a manifest Main-Class that directly extends
 * javafx.application.Application when JavaFX is supplied on the classpath rather
 * than the module path ("JavaFX runtime components are missing"). Routing through
 * a plain main method here avoids that check.
 */
public final class Launcher {

    private Launcher() {
    }

    public static void main(String[] args) {
        CvcViewerApp.main(args);
    }
}
