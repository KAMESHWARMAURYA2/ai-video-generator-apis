package com.example.aivideogenerator.platform.service;

import com.example.aivideogenerator.platform.api.SchedulePublishRequest;
import com.example.aivideogenerator.platform.domain.ContentItem;
import com.example.aivideogenerator.platform.domain.ContentStatus;
import com.example.aivideogenerator.platform.domain.PublishJobStatus;
import com.example.aivideogenerator.platform.domain.PublishSchedule;
import com.example.aivideogenerator.platform.persistence.PublishScheduleRepository;
import com.example.aivideogenerator.platform.provider.PublisherProvider;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class PublishingService {
    private final PublishScheduleRepository publishScheduleRepository;
    private final ContentService contentService;
    private final PublisherProvider publisherProvider;
    private final PlatformMapper mapper;

    public PublishingService(PublishScheduleRepository publishScheduleRepository,
                             ContentService contentService,
                             PublisherProvider publisherProvider,
                             PlatformMapper mapper) {
        this.publishScheduleRepository = publishScheduleRepository;
        this.contentService = contentService;
        this.publisherProvider = publisherProvider;
        this.mapper = mapper;
    }

    public List<String> schedule(SchedulePublishRequest request) {
        ContentItem content = contentService.getContent(request.contentItemId());
        if (content.status() != ContentStatus.READY_TO_SCHEDULE) {
            throw new IllegalStateException("Content must be APPROVED before scheduling. Current status: " + content.status());
        }

        List<String> scheduleIds = new ArrayList<>();
        Instant now = Instant.now();
        for (SchedulePublishRequest.TargetSchedule target : request.targets()) {
            String scheduleId = "sch_" + UUID.randomUUID();
            Instant scheduledAtUtc = LocalDateTime.parse(target.scheduledAt())
                    .atZone(ZoneId.of(target.timezone()))
                    .toInstant();

            PublishSchedule schedule = new PublishSchedule(scheduleId, request.contentItemId(), target.socialAccountId(),
                    target.timezone(), scheduledAtUtc, PublishJobStatus.SCHEDULED, null, 0, null, now, now);
            publishScheduleRepository.save(mapper.toEntity(schedule));
            scheduleIds.add(scheduleId);
        }

        contentService.update(new ContentItem(content.id(), content.prompt(), content.requestedOutputs(), content.assetUrls(),
                content.caption(), content.hashtags(), ContentStatus.SCHEDULED, content.reviewTaskId(), content.createdAt(), now));

        return scheduleIds;
    }

    public PublishSchedule getSchedule(String scheduleId) {
        return publishScheduleRepository.findById(scheduleId)
                .map(mapper::toDomain)
                .orElseThrow(() -> new IllegalArgumentException("Schedule not found: " + scheduleId));
    }

    @Scheduled(fixedDelay = 5000)
    public void processDueSchedules() {
        List<PublishSchedule> due = publishScheduleRepository
                .findByStatusAndScheduledAtUtcLessThanEqual(PublishJobStatus.SCHEDULED, Instant.now())
                .stream().map(mapper::toDomain).toList();
        due.forEach(this::executePublish);
    }

    private void executePublish(PublishSchedule schedule) {
        ContentItem content = contentService.getContent(schedule.contentItemId());
        try {
            String assetUrl = content.assetUrls().isEmpty() ? null : content.assetUrls().get(0);
            String externalPostId = publisherProvider.publishPost(schedule.socialAccountId(), content.caption(), assetUrl);
            PublishSchedule posted = new PublishSchedule(schedule.id(), schedule.contentItemId(), schedule.socialAccountId(),
                    schedule.timezone(), schedule.scheduledAtUtc(), PublishJobStatus.POSTED, externalPostId,
                    schedule.attempts() + 1, null, schedule.createdAt(), Instant.now());
            publishScheduleRepository.save(mapper.toEntity(posted));
            contentService.update(new ContentItem(content.id(), content.prompt(), content.requestedOutputs(), content.assetUrls(),
                    content.caption(), content.hashtags(), ContentStatus.POSTED, content.reviewTaskId(), content.createdAt(), Instant.now()));
        } catch (Exception e) {
            PublishSchedule failed = new PublishSchedule(schedule.id(), schedule.contentItemId(), schedule.socialAccountId(),
                    schedule.timezone(), schedule.scheduledAtUtc(), PublishJobStatus.FAILED, null,
                    schedule.attempts() + 1, e.getMessage(), schedule.createdAt(), Instant.now());
            publishScheduleRepository.save(mapper.toEntity(failed));
        }
    }
}
