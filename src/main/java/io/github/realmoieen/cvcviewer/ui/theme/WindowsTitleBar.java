package io.github.realmoieen.cvcviewer.ui.theme;

import com.sun.javafx.stage.WindowHelper;
import com.sun.javafx.tk.TKStage;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.ptr.IntByReference;
import javafx.stage.Stage;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Sets the DWMWA_USE_IMMERSIVE_DARK_MODE attribute on a Stage's native window so the OS-drawn
 * title bar follows the app's theme - AtlantaFX/JavaFX CSS only styles window content, not the
 * chrome, which Windows renders itself via DWM and defaults to light regardless.
 * <p>
 * Windows-only; every entry point degrades to a silent no-op on failure (missing native library,
 * unsupported Windows build, JavaFX internals changing between versions, etc). Reflectively
 * reaching into com.sun.javafx/com.sun.glass works here without --add-opens only because this
 * project deliberately runs JavaFX on the classpath rather than the module path - see build.gradle.
 */
final class WindowsTitleBar {

    private static final Logger LOGGER = Logger.getLogger(WindowsTitleBar.class.getName());
    // Windows 11 / Windows 10 20H1+ (build 18985+)
    private static final int DWMWA_USE_IMMERSIVE_DARK_MODE = 20;
    // Earlier Windows 10 1809-1909 builds used this undocumented attribute number instead
    private static final int DWMWA_USE_IMMERSIVE_DARK_MODE_LEGACY = 19;

    private WindowsTitleBar() {
    }

    interface Dwmapi extends com.sun.jna.Library {
        Dwmapi INSTANCE = Native.load("dwmapi", Dwmapi.class);

        int DwmSetWindowAttribute(WinDef.HWND hwnd, int dwAttribute, IntByReference pvAttribute, int cbAttribute);
    }

    static void setDark(Stage stage, boolean dark) {
        try {
            long handle = nativeHandle(stage);
            if (handle == 0) {
                return;
            }
            WinDef.HWND hwnd = new WinDef.HWND(Pointer.createConstant(handle));
            IntByReference value = new IntByReference(dark ? 1 : 0);
            int result = Dwmapi.INSTANCE.DwmSetWindowAttribute(hwnd, DWMWA_USE_IMMERSIVE_DARK_MODE, value, 4);
            if (result != 0) {
                Dwmapi.INSTANCE.DwmSetWindowAttribute(hwnd, DWMWA_USE_IMMERSIVE_DARK_MODE_LEGACY, value, 4);
            }
        } catch (Throwable t) {
            LOGGER.log(Level.FINE, "Failed to set Windows title bar theme", t);
        }
    }

    private static long nativeHandle(Stage stage) {
        try {
            TKStage peer = WindowHelper.getPeer(stage);
            return peer == null ? 0 : peer.getRawHandle();
        } catch (Throwable t) {
            return 0;
        }
    }
}
