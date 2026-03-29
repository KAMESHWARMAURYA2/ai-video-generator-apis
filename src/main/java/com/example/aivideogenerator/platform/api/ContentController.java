package com.example.aivideogenerator.platform.api;

import com.example.aivideogenerator.platform.domain.ContentItem;
import com.example.aivideogenerator.platform.domain.ReviewTask;
import com.example.aivideogenerator.platform.service.ContentService;
import com.example.aivideogenerator.platform.service.PublishingService;
import com.example.aivideogenerator.platform.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class ContentController {
    private final ContentService contentService;
    private final ReviewService reviewService;
    private final PublishingService publishingService;

    public ContentController(ContentService contentService,
                             ReviewService reviewService,
                             PublishingService publishingService) {
        this.contentService = contentService;
        this.reviewService = reviewService;
        this.publishingService = publishingService;
    }

    @PostMapping("/content/generate")
    public GenerateContentResponse generate(@Valid @RequestBody GenerateContentRequest request) {
        ContentItem item = contentService.generate(request);
        return new GenerateContentResponse(item.id(), item.reviewTaskId(), item.status());
    }

    @GetMapping("/content/{contentId}")
    public ContentItem getContent(@PathVariable String contentId) {
        return contentService.getContent(contentId);
    }

    @PostMapping("/review/tasks/{taskId}/action")
    public ReviewActionResponse applyReviewAction(@PathVariable String taskId,
                                                  @Valid @RequestBody ReviewActionRequest request) {
        ReviewTask task = reviewService.applyAction(taskId, request);
        return new ReviewActionResponse(task.id(), task.status());
    }

    @PostMapping("/publish/schedules")
    public SchedulePublishResponse schedule(@Valid @RequestBody SchedulePublishRequest request) {
        return new SchedulePublishResponse(publishingService.schedule(request), "SCHEDULED");
    }

    @GetMapping("/publish/schedules/{scheduleId}")
    public Object scheduleStatus(@PathVariable String scheduleId) {
        return publishingService.getSchedule(scheduleId);
    }
}
