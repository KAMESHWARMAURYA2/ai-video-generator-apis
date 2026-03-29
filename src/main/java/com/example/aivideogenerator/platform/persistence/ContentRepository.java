package com.example.aivideogenerator.platform.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ContentRepository extends JpaRepository<ContentEntity, String> {
}
