package com.example.aivideogenerator.platform.api;

import com.example.aivideogenerator.platform.domain.ContentStatus;

public record ReviewActionResponse(
        String taskId,
        ContentStatus status
) {
}
