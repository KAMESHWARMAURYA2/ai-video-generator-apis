package com.example.aivideogenerator.platform.service;

import com.example.aivideogenerator.platform.api.GenerateContentRequest;
import com.example.aivideogenerator.platform.domain.ContentItem;
import com.example.aivideogenerator.platform.domain.ContentStatus;
import com.example.aivideogenerator.platform.domain.GenerationOutputType;
import com.example.aivideogenerator.platform.domain.ReviewTask;
import com.example.aivideogenerator.platform.persistence.ContentRepository;
import com.example.aivideogenerator.platform.persistence.ReviewTaskRepository;
import com.example.aivideogenerator.platform.provider.CaptionGenProvider;
import com.example.aivideogenerator.platform.provider.ImageGenProvider;
import com.example.aivideogenerator.platform.provider.VideoGenProvider;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ContentService {
    private final ContentRepository contentRepository;
    private final ReviewTaskRepository reviewTaskRepository;
    private final PlatformMapper mapper;
    private final ImageGenProvider imageProvider;
    private final VideoGenProvider videoProvider;
    private final CaptionGenProvider captionProvider;

    public ContentService(ContentRepository contentRepository,
                          ReviewTaskRepository reviewTaskRepository,
                          PlatformMapper mapper,
                          ImageGenProvider imageProvider,
                          VideoGenProvider videoProvider,
                          CaptionGenProvider captionProvider) {
        this.contentRepository = contentRepository;
        this.reviewTaskRepository = reviewTaskRepository;
        this.mapper = mapper;
        this.imageProvider = imageProvider;
        this.videoProvider = videoProvider;
        this.captionProvider = captionProvider;
    }

    public ContentItem generate(GenerateContentRequest request) {
        Instant now = Instant.now();
        String contentId = "cnt_" + UUID.randomUUID();
        String reviewTaskId = "rvw_" + UUID.randomUUID();

        List<String> assets = new ArrayList<>();
        String caption = null;
        List<String> hashtags = List.of();

        for (GenerationOutputType output : request.outputs()) {
            switch (output) {
                case IMAGE -> assets.add(imageProvider.generateImage(request.prompt()));
                case VIDEO -> assets.add(videoProvider.generateVideo(request.prompt()));
                case CAPTION -> {
                    CaptionGenProvider.CaptionResult result = captionProvider.generateCaption(request.prompt());
                    caption = result.caption();
                    hashtags = result.hashtags();
                }
            }
        }

        ContentItem item = new ContentItem(contentId, request.prompt(), request.outputs(), assets, caption, hashtags,
                ContentStatus.REVIEW_PENDING, reviewTaskId, now, now);
        ReviewTask reviewTask = new ReviewTask(reviewTaskId, contentId, ContentStatus.REVIEW_PENDING, null, now, now);

        contentRepository.save(mapper.toEntity(item));
        reviewTaskRepository.save(mapper.toEntity(reviewTask));
        return item;
    }

    public ContentItem getContent(String contentId) {
        return contentRepository.findById(contentId)
                .map(mapper::toDomain)
                .orElseThrow(() -> new IllegalArgumentException("Content item not found: " + contentId));
    }

    public void update(ContentItem item) {
        contentRepository.save(mapper.toEntity(item));
    }
}
