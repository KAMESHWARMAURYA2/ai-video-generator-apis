package com.example.aivideogenerator.platform.api;

import java.util.List;

public record SchedulePublishResponse(
        List<String> scheduleIds,
        String status
) {
}
