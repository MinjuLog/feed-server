package org.minjulog.feedserver.domain.feed;

import java.util.List;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface FeedRepository extends JpaRepository<Feed, Long> {

    @EntityGraph(attributePaths = {"authorProfile", "attachments"})
    List<Feed> findByDeletedFalseOrderByCreatedAtDesc();

}
