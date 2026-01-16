package org.minjulog.feedserver.domain.feed;

import java.util.List;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface FeedRepository extends JpaRepository<Feed, Long> {

    @Modifying
    @Query("update Feed f set f.likeCount = f.likeCount + 1 where f.feedId = :feedId")
    int incrementLike(@Param("feedId") Long feedId);

    @Query("select f.likeCount from Feed f where f.feedId = :feedId")
    int findLikeCount(@Param("feedId") Long feedId);

    @EntityGraph(attributePaths = {"authorProfile", "attachments"})
    List<Feed> findByDeletedFalseOrderByCreatedAtDesc();
}
