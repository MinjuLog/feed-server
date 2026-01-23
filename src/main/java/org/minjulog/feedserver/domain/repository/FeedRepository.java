package org.minjulog.feedserver.domain.repository;

import java.util.List;

import org.minjulog.feedserver.domain.model.Feed;
import org.springframework.data.jpa.repository.*;

public interface FeedRepository extends JpaRepository<Feed, Long> {

    @EntityGraph(attributePaths = {"authorProfile", "attachments"})
    List<Feed> findByDeletedFalseOrderByCreatedAtDesc();

}
