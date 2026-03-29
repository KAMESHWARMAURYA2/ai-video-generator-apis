package com.example.aivideogenerator.platform.persistence;

import com.example.aivideogenerator.platform.domain.PublishJobStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "publish_schedules")
public class PublishScheduleEntity {
    @Id
    private String id;
    @Column(nullable = false)
    private String contentItemId;
    @Column(nullable = false)
    private String socialAccountId;
    @Column(nullable = false)
    private String timezone;
    @Column(nullable = false)
    private Instant scheduledAtUtc;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PublishJobStatus status;
    private String externalPostId;
    @Column(nullable = false)
    private int attempts;
    @Column(length = 4000)
    private String lastError;
    @Column(nullable = false)
    private Instant createdAt;
    @Column(nullable = false)
    private Instant updatedAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getContentItemId() { return contentItemId; }
    public void setContentItemId(String contentItemId) { this.contentItemId = contentItemId; }
    public String getSocialAccountId() { return socialAccountId; }
    public void setSocialAccountId(String socialAccountId) { this.socialAccountId = socialAccountId; }
    public String getTimezone() { return timezone; }
    public void setTimezone(String timezone) { this.timezone = timezone; }
    public Instant getScheduledAtUtc() { return scheduledAtUtc; }
    public void setScheduledAtUtc(Instant scheduledAtUtc) { this.scheduledAtUtc = scheduledAtUtc; }
    public PublishJobStatus getStatus() { return status; }
    public void setStatus(PublishJobStatus status) { this.status = status; }
    public String getExternalPostId() { return externalPostId; }
    public void setExternalPostId(String externalPostId) { this.externalPostId = externalPostId; }
    public int getAttempts() { return attempts; }
    public void setAttempts(int attempts) { this.attempts = attempts; }
    public String getLastError() { return lastError; }
    public void setLastError(String lastError) { this.lastError = lastError; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
