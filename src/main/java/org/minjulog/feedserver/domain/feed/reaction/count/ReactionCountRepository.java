package org.minjulog.feedserver.domain.feed.reaction.count;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReactionCountRepository extends JpaRepository<ReactionCount, Long> {

    @Query("""
                select rc.feed.feedId as feedId,
                       rt.key as reactionKey,
                       rt.imageUrl as imageUrl,
                       rt.unicode as unicode,
                       rc.count as count
                from ReactionCount rc
                join rc.reactionType rt
                where rc.feed.feedId in :feedIds
            """)
    List<ReactionCountRow> findReactionCountsByFeedIds(@Param("feedIds") List<Long> feedIds);

    public interface ReactionCountRow {
        Long getFeedId();
        String getReactionKey();
        String getImageUrl();
        String unicode();
        Integer getCount();
    }
}
