package com.example.aivideogenerator.platform.api;

import com.example.aivideogenerator.platform.domain.ContentStatus;

public record GenerateContentResponse(
        String contentItemId,
        String reviewTaskId,
        ContentStatus status
) {
}
