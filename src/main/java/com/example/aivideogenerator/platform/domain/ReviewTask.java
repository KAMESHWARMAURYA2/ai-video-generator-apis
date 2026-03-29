package com.example.aivideogenerator.platform.domain;

import java.time.Instant;

public record ReviewTask(
        String id,
        String contentItemId,
        ContentStatus status,
        String reviewerComment,
        Instant createdAt,
        Instant updatedAt
) {
}
