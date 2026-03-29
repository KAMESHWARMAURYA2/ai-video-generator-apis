package com.example.aivideogenerator.platform.domain;

import java.time.Instant;

public record PublishSchedule(
        String id,
        String contentItemId,
        String socialAccountId,
        String timezone,
        Instant scheduledAtUtc,
        PublishJobStatus status,
        String externalPostId,
        int attempts,
        String lastError,
        Instant createdAt,
        Instant updatedAt
) {
}
