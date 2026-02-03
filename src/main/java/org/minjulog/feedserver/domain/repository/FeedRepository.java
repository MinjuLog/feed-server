package org.minjulog.feedserver.domain.repository;

import org.minjulog.feedserver.domain.model.Feed;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeedRepository extends JpaRepository<Feed, Long> {

    @EntityGraph(attributePaths = {"authorProfile", "attachments"})
    List<Feed> findByDeletedFalseOrderByCreatedAtDesc();

}
