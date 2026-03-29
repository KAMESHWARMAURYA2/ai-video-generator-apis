package com.example.aivideogenerator.platform.persistence;

import com.example.aivideogenerator.platform.domain.ContentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "content_items")
public class ContentEntity {
    @Id
    private String id;
    @Column(nullable = false, length = 2000)
    private String prompt;
    @Lob
    private String requestedOutputsJson;
    @Lob
    private String assetUrlsJson;
    @Column(length = 4000)
    private String caption;
    @Lob
    private String hashtagsJson;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContentStatus status;
    @Column(nullable = false)
    private String reviewTaskId;
    @Column(nullable = false)
    private Instant createdAt;
    @Column(nullable = false)
    private Instant updatedAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getPrompt() { return prompt; }
    public void setPrompt(String prompt) { this.prompt = prompt; }
    public String getRequestedOutputsJson() { return requestedOutputsJson; }
    public void setRequestedOutputsJson(String requestedOutputsJson) { this.requestedOutputsJson = requestedOutputsJson; }
    public String getAssetUrlsJson() { return assetUrlsJson; }
    public void setAssetUrlsJson(String assetUrlsJson) { this.assetUrlsJson = assetUrlsJson; }
    public String getCaption() { return caption; }
    public void setCaption(String caption) { this.caption = caption; }
    public String getHashtagsJson() { return hashtagsJson; }
    public void setHashtagsJson(String hashtagsJson) { this.hashtagsJson = hashtagsJson; }
    public ContentStatus getStatus() { return status; }
    public void setStatus(ContentStatus status) { this.status = status; }
    public String getReviewTaskId() { return reviewTaskId; }
    public void setReviewTaskId(String reviewTaskId) { this.reviewTaskId = reviewTaskId; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
