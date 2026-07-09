package io.github.realmoieen.cvcviewer.service.update;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

public final class ReleaseNotesRenderer {

    private static final Parser MARKDOWN_PARSER = Parser.builder().build();
    private static final HtmlRenderer HTML_RENDERER = HtmlRenderer.builder().build();

    private ReleaseNotesRenderer() {
    }

    public static String toHtml(String markdown) {
        Node document = MARKDOWN_PARSER.parse(markdown);
        String body = HTML_RENDERER.render(document);
        return "<html><head><style>"
                + "body { font-family: -apple-system, Segoe UI, Helvetica, Arial, sans-serif; "
                + "font-size: 13px; color: #1f2328; margin: 8px; }"
                + "h1, h2, h3 { border-bottom: 1px solid #d0d7de; padding-bottom: 4px; }"
                + "code { background: #f6f8fa; padding: 1px 4px; border-radius: 4px; font-size: 12px; }"
                + "pre code { display: block; padding: 8px; overflow-x: auto; }"
                + "a { color: #0969da; }"
                + "</style></head><body>" + body + "</body></html>";
    }
}
