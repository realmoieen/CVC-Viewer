package io.github.realmoieen.cvcviewer.service.errorreport;

import io.github.realmoieen.cvcviewer.info.AppInfo;
import io.github.realmoieen.cvcviewer.log.LoggingBootstrap;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public final class GitHubIssueReporter {

    private static final String REPO_URL = "https://github.com/realmoieen/CVC-Viewer";
    private static final int MAX_STACK_TRACE_LINES = 40;

    private GitHubIssueReporter() {
    }

    public static String buildIssueUrl(Throwable throwable) {
        String title = "Bug: " + throwable.getClass().getSimpleName() + (throwable.getMessage() != null ? ": " + throwable.getMessage() : "");
        String body = buildBody(throwable);
        return REPO_URL + "/issues/new"
                + "?title=" + encode(truncate(title, 250))
                + "&body=" + encode(body)
                + "&labels=" + encode("bug");
    }

    private static String buildBody(Throwable throwable) {
        StringBuilder sb = new StringBuilder();
        sb.append("### Environment\n");
        sb.append("- CVC-Viewer version: ").append(AppInfo.APP_VERSION).append("\n");
        sb.append("- OS: ").append(System.getProperty("os.name")).append(" ")
                .append(System.getProperty("os.version")).append(" (")
                .append(System.getProperty("os.arch")).append(")\n");
        sb.append("- Java runtime: ").append(System.getProperty("java.version")).append("\n\n");

        sb.append("### Stack trace\n```\n");
        sb.append(truncatedStackTrace(throwable));
        sb.append("\n(see log file at ").append(LoggingBootstrap.logDirectory()).append(" for full trace)\n");
        sb.append("```\n\n");

        sb.append("### Steps to reproduce\n<!-- fill in -->\n");
        return sb.toString();
    }

    private static String truncatedStackTrace(Throwable throwable) {
        StringWriter sw = new StringWriter();
        throwable.printStackTrace(new PrintWriter(sw));
        String[] lines = sw.toString().split("\n", MAX_STACK_TRACE_LINES + 1);
        if (lines.length <= MAX_STACK_TRACE_LINES) {
            return sw.toString();
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < MAX_STACK_TRACE_LINES; i++) {
            sb.append(lines[i]).append("\n");
        }
        sb.append("... (truncated)");
        return sb.toString();
    }

    private static String truncate(String s, int maxLength) {
        return s.length() <= maxLength ? s : s.substring(0, maxLength);
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
