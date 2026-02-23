package org.minjulog.feedserver.infrastructure.repository;

import org.minjulog.feedserver.domain.model.Feed;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface JpaFeedRepository extends JpaRepository<Feed, UUID> {

    @EntityGraph(attributePaths = {"authorUserProfile", "attachments"})
    List<Feed> findByDeletedFalseOrderByCreatedAtDesc();
}
