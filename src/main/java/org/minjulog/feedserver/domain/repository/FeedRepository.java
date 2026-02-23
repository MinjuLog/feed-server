package org.minjulog.feedserver.domain.repository;

import lombok.RequiredArgsConstructor;
import org.minjulog.feedserver.domain.model.Feed;
import org.minjulog.feedserver.infrastructure.repository.JpaFeedRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class FeedRepository {
    private final JpaFeedRepository jpa;

    public Feed save(Feed feed) { return jpa.save(feed); }
    public Optional<Feed> findById(UUID id) { return jpa.findById(id); }
    public List<Feed> findByDeletedFalseOrderByCreatedAtDesc() { return jpa.findByDeletedFalseOrderByCreatedAtDesc(); }
}
