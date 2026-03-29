package com.example.aivideogenerator.platform.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record SchedulePublishRequest(
        @NotBlank String contentItemId,
        @NotEmpty List<TargetSchedule> targets
) {
    public record TargetSchedule(
            @NotBlank String socialAccountId,
            @NotBlank String scheduledAt,
            @NotBlank String timezone
    ) {
    }
}
