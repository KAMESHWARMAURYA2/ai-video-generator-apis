package com.example.aivideogenerator.platform.api;

import com.example.aivideogenerator.platform.domain.ReviewActionType;
import jakarta.validation.constraints.NotNull;

public record ReviewActionRequest(
        @NotNull ReviewActionType action,
        String comment,
        String editedCaption
) {
}
