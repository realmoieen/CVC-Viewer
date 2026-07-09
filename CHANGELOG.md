# Changelog

## 4.0 — The Swing → JavaFX Modernization Release

CVC-Viewer has been rebuilt from the ground up on JavaFX, with a modernized architecture, a redesigned distribution model, and a set of quality-of-life features that didn't exist before: theme switching, pausable update notifications, manual update checks, and a proper GitHub Pages website. This is a major version bump because it's a full UI-toolkit rewrite and a Java baseline bump (8 → 17), not an incremental update.

### Highlights

#### New JavaFX UI, new look
The entire UI has been rewritten from Swing to **JavaFX**, styled with **[AtlantaFX](https://github.com/mkpaz/atlantafx)**. The old hand-positioned Swing layout is replaced with FXML views and controllers, and the app ships a brand-new icon set and logo across the app, installers, and README.

#### Light, dark, and system theme support
A new **View → Theme** menu lets you switch between Light, Dark, and System Default. Your choice is saved to app settings and restored on next launch. On Windows, the native title bar now correctly follows dark mode too — previously it stayed light even when the app content was dark.

#### Smarter update notifications
- The **Update Notifier** dialog now renders GitHub release notes as properly formatted markdown instead of raw text.
- You can now **pause update notifications** for 1, 7, or 14 days directly from the notifier dialog.
- A new **Help → Check for Updates...** menu item lets you manually check any time — it shows a success message if you're already on the latest version, or the Update Notifier dialog if a newer release is available.

#### Two ways to install: installer or portable
Every platform (Windows, macOS, Linux) now offers two download options:
- **Installer** (MSI / DMG / DEB) — bundles its own trimmed Java runtime, no prerequisites.
- **Portable** (ZIP / tar.gz) — a much smaller download with no bundled JRE, for machines that already have a system **Java 17+** installed.

#### New website
CVC-Viewer now has a dedicated site at **[realmoieen.github.io/CVC-Viewer](https://realmoieen.github.io/CVC-Viewer/)**, hosted via GitHub Pages directly from this repo. The About dialog and installer metadata now link here instead of the raw GitHub repo URL.

#### Detail view resizer
The Details tab now has a draggable divider between the certificate table and the selected-row detail pane, so you can resize the split to your liking.

### Bug Fixes

- **Save / Copy to File**: the file-format selection dialog now appears *before* the save dialog (previously it appeared after, which was backwards and confusing).
- **Missing dialog icons**: the Update Notifier, About, and file-chooser dialogs — including the one shown on startup — now correctly display the app icon instead of a blank/default one.
- **Windows dark title bar**: the native window title bar now switches to dark mode along with the rest of the app (previously stayed light regardless of theme).
- **Extension formatter**: fixed an issue in certificate extension formatting.
- Fixed a broken release workflow that was missing portable-artifact publishing for one or more platforms.

### Under the Hood

- Migrated the full UI layer from Swing to JavaFX 21 + AtlantaFX 2.0.1 (deliberately pinned to stay compatible with the Java 17 baseline).
- Extracted certificate-formatting logic (public key decoding, authorization bitflags, extensions) out of the old monolithic Swing view class into a pure, independently unit-tested `core/format` layer.
- Added a settings persistence layer (JSON, per-OS config directory) for theme preference and update-notification pause state.
- Added native Win32 integration (via JNA) for the dark title bar fix.
- Added markdown-to-HTML rendering (via commonmark) for release notes, displayed through a JavaFX WebView.
- Rebuilt distribution: self-contained jpackage installers for all three OSes, plus new portable no-JRE builds (Launch4j-wrapped EXE on Windows, app bundle on macOS, shell launcher on Linux).
- Audited and fixed all GitHub Actions release workflows (per-OS reusable workflows, correct artifact naming, aggregated release publishing).
- Bumped baseline from Java 8 to **Java 17** across the board (installers already bundled their own runtime; this is an intentional breaking change for anyone still building/running from source on an older JDK).

### Upgrade Notes

- If you use the **portable** build, you now need a system-installed **Java 17 or newer** on `PATH` (or `JAVA_HOME` set). The installer builds are unaffected — they bundle their own runtime as always.
- Existing theme/update-pause preferences are new in this release; nothing to migrate from prior versions.

---

## 3.0 and earlier

See [GitHub Releases](https://github.com/realmoieen/CVC-Viewer/releases) for notes on prior versions.
