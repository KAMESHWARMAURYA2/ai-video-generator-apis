package com.example.aivideogenerator.platform.persistence;

import com.example.aivideogenerator.platform.domain.ContentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "review_tasks")
public class ReviewTaskEntity {
    @Id
    private String id;
    @Column(nullable = false)
    private String contentItemId;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContentStatus status;
    @Column(length = 4000)
    private String reviewerComment;
    @Column(nullable = false)
    private Instant createdAt;
    @Column(nullable = false)
    private Instant updatedAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getContentItemId() { return contentItemId; }
    public void setContentItemId(String contentItemId) { this.contentItemId = contentItemId; }
    public ContentStatus getStatus() { return status; }
    public void setStatus(ContentStatus status) { this.status = status; }
    public String getReviewerComment() { return reviewerComment; }
    public void setReviewerComment(String reviewerComment) { this.reviewerComment = reviewerComment; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
