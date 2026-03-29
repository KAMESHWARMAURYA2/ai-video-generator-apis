package com.example.aivideogenerator.platform.domain;

import java.time.Instant;
import java.util.List;

public record ContentItem(
        String id,
        String prompt,
        List<GenerationOutputType> requestedOutputs,
        List<String> assetUrls,
        String caption,
        List<String> hashtags,
        ContentStatus status,
        String reviewTaskId,
        Instant createdAt,
        Instant updatedAt
) {
}
