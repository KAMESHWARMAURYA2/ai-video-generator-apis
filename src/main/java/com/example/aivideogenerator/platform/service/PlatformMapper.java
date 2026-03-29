package com.example.aivideogenerator.platform.service;

import com.example.aivideogenerator.platform.domain.ContentItem;
import com.example.aivideogenerator.platform.domain.GenerationOutputType;
import com.example.aivideogenerator.platform.domain.PublishSchedule;
import com.example.aivideogenerator.platform.domain.ReviewTask;
import com.example.aivideogenerator.platform.persistence.ContentEntity;
import com.example.aivideogenerator.platform.persistence.PublishScheduleEntity;
import com.example.aivideogenerator.platform.persistence.ReviewTaskEntity;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PlatformMapper {
    private final ObjectMapper objectMapper;

    public PlatformMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public ContentEntity toEntity(ContentItem item) {
        ContentEntity entity = new ContentEntity();
        entity.setId(item.id());
        entity.setPrompt(item.prompt());
        entity.setRequestedOutputsJson(write(item.requestedOutputs()));
        entity.setAssetUrlsJson(write(item.assetUrls()));
        entity.setCaption(item.caption());
        entity.setHashtagsJson(write(item.hashtags()));
        entity.setStatus(item.status());
        entity.setReviewTaskId(item.reviewTaskId());
        entity.setCreatedAt(item.createdAt());
        entity.setUpdatedAt(item.updatedAt());
        return entity;
    }

    public ContentItem toDomain(ContentEntity entity) {
        return new ContentItem(
                entity.getId(),
                entity.getPrompt(),
                read(entity.getRequestedOutputsJson(), new TypeReference<List<GenerationOutputType>>() {}),
                read(entity.getAssetUrlsJson(), new TypeReference<List<String>>() {}),
                entity.getCaption(),
                read(entity.getHashtagsJson(), new TypeReference<List<String>>() {}),
                entity.getStatus(),
                entity.getReviewTaskId(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public ReviewTaskEntity toEntity(ReviewTask task) {
        ReviewTaskEntity entity = new ReviewTaskEntity();
        entity.setId(task.id());
        entity.setContentItemId(task.contentItemId());
        entity.setStatus(task.status());
        entity.setReviewerComment(task.reviewerComment());
        entity.setCreatedAt(task.createdAt());
        entity.setUpdatedAt(task.updatedAt());
        return entity;
    }

    public ReviewTask toDomain(ReviewTaskEntity entity) {
        return new ReviewTask(
                entity.getId(),
                entity.getContentItemId(),
                entity.getStatus(),
                entity.getReviewerComment(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public PublishScheduleEntity toEntity(PublishSchedule schedule) {
        PublishScheduleEntity entity = new PublishScheduleEntity();
        entity.setId(schedule.id());
        entity.setContentItemId(schedule.contentItemId());
        entity.setSocialAccountId(schedule.socialAccountId());
        entity.setTimezone(schedule.timezone());
        entity.setScheduledAtUtc(schedule.scheduledAtUtc());
        entity.setStatus(schedule.status());
        entity.setExternalPostId(schedule.externalPostId());
        entity.setAttempts(schedule.attempts());
        entity.setLastError(schedule.lastError());
        entity.setCreatedAt(schedule.createdAt());
        entity.setUpdatedAt(schedule.updatedAt());
        return entity;
    }

    public PublishSchedule toDomain(PublishScheduleEntity entity) {
        return new PublishSchedule(
                entity.getId(),
                entity.getContentItemId(),
                entity.getSocialAccountId(),
                entity.getTimezone(),
                entity.getScheduledAtUtc(),
                entity.getStatus(),
                entity.getExternalPostId(),
                entity.getAttempts(),
                entity.getLastError(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private String write(Object value) {
        try {
            return objectMapper.writeValueAsString(value == null ? List.of() : value);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize value", e);
        }
    }

    private <T> T read(String json, TypeReference<T> typeReference) {
        try {
            if (json == null || json.isBlank()) {
                return objectMapper.readValue("[]", typeReference);
            }
            return objectMapper.readValue(json, typeReference);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to deserialize value", e);
        }
    }
}
