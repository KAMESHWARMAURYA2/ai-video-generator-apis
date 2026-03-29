package com.example.aivideogenerator.platform.service;

import com.example.aivideogenerator.platform.api.ReviewActionRequest;
import com.example.aivideogenerator.platform.domain.ContentItem;
import com.example.aivideogenerator.platform.domain.ContentStatus;
import com.example.aivideogenerator.platform.domain.ReviewActionType;
import com.example.aivideogenerator.platform.domain.ReviewTask;
import com.example.aivideogenerator.platform.persistence.ReviewTaskRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class ReviewService {
    private final ReviewTaskRepository reviewTaskRepository;
    private final ContentService contentService;
    private final PlatformMapper mapper;

    public ReviewService(ReviewTaskRepository reviewTaskRepository, ContentService contentService, PlatformMapper mapper) {
        this.reviewTaskRepository = reviewTaskRepository;
        this.contentService = contentService;
        this.mapper = mapper;
    }

    public ReviewTask applyAction(String taskId, ReviewActionRequest request) {
        ReviewTask task = reviewTaskRepository.findById(taskId)
                .map(mapper::toDomain)
                .orElseThrow(() -> new IllegalArgumentException("Review task not found: " + taskId));

        ContentItem content = contentService.getContent(task.contentItemId());
        ContentStatus nextStatus = mapStatus(request.action());
        Instant now = Instant.now();

        String caption = request.editedCaption() != null && !request.editedCaption().isBlank()
                ? request.editedCaption()
                : content.caption();

        ContentItem updatedContent = new ContentItem(content.id(), content.prompt(), content.requestedOutputs(),
                content.assetUrls(), caption, content.hashtags(), nextStatus, content.reviewTaskId(), content.createdAt(), now);

        ReviewTask updatedTask = new ReviewTask(task.id(), task.contentItemId(), nextStatus,
                request.comment(), task.createdAt(), now);

        contentService.update(updatedContent);
        reviewTaskRepository.save(mapper.toEntity(updatedTask));
        return updatedTask;
    }

    private ContentStatus mapStatus(ReviewActionType actionType) {
        return switch (actionType) {
            case APPROVE -> ContentStatus.READY_TO_SCHEDULE;
            case REJECT -> ContentStatus.REJECTED;
            case REQUEST_CHANGES -> ContentStatus.REVIEW_PENDING;
        };
    }
}
