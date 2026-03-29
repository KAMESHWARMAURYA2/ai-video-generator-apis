package com.example.aivideogenerator.platform.persistence;

import com.example.aivideogenerator.platform.domain.PublishJobStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface PublishScheduleRepository extends JpaRepository<PublishScheduleEntity, String> {
    List<PublishScheduleEntity> findByStatusAndScheduledAtUtcLessThanEqual(PublishJobStatus status, Instant dueAt);
}
