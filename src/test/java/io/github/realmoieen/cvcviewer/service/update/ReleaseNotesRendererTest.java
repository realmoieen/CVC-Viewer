package io.github.realmoieen.cvcviewer.service.update;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReleaseNotesRendererTest {

    @Test
    void rendersGitHubStyleReleaseNotesAsHtml() {
        String markdown = "## What's Changed\n"
                + "* Fixed a bug by @someone in https://github.com/realmoieen/CVC-Viewer/pull/1\n"
                + "* **Important**: read the [migration guide](https://example.com/migrate)\n"
                + "\n"
                + "```\n"
                + "some code\n"
                + "```\n"
                + "\n"
                + "**Full Changelog**: https://github.com/realmoieen/CVC-Viewer/compare/v3.0...v3.1";

        String html = ReleaseNotesRenderer.toHtml(markdown);

        assertTrue(html.startsWith("<html>"));
        assertTrue(html.contains("<h2>What's Changed</h2>"));
        assertTrue(html.contains("<li>"));
        assertTrue(html.contains("<strong>Important</strong>"));
        assertTrue(html.contains("<a href=\"https://example.com/migrate\">migration guide</a>"));
        assertTrue(html.contains("<pre><code>some code"));
        assertFalse(html.contains("##"), "raw markdown syntax should not leak into the rendered output");
    }

    @Test
    void handlesEmptyReleaseNotes() {
        String html = ReleaseNotesRenderer.toHtml("");

        assertTrue(html.startsWith("<html>"));
        assertTrue(html.contains("<body></body>"));
    }
}
