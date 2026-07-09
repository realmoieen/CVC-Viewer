package io.github.realmoieen.cvcviewer.service.settings;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AppSettingsTest {

    @Test
    void notPausedByDefault() {
        AppSettings settings = new AppSettings();
        assertFalse(settings.isUpdateNotificationsPaused());
    }

    @Test
    void pausedWhenResumeTimeIsInTheFuture() {
        AppSettings settings = new AppSettings();
        settings.setUpdateNotificationsPausedUntilEpochMilli(
                System.currentTimeMillis() + Duration.ofDays(7).toMillis());
        assertTrue(settings.isUpdateNotificationsPaused());
    }

    @Test
    void notPausedOnceResumeTimeHasPassed() {
        AppSettings settings = new AppSettings();
        settings.setUpdateNotificationsPausedUntilEpochMilli(
                System.currentTimeMillis() - Duration.ofDays(1).toMillis());
        assertFalse(settings.isUpdateNotificationsPaused());
    }
}
