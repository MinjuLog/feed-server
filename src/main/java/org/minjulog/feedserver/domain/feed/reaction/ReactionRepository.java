package org.minjulog.feedserver.domain.feed.reaction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReactionRepository extends JpaRepository<Reaction, Long> {

    @Query("""
                select r.feed.feedId as feedId,
                       rt.key as reactionKey
                from Reaction r
                join r.type rt
                where r.profile.userId = :viewerId
                  and r.feed.feedId in :feedIds
            """)
    List<MyReactionRow> findMyReactions(
            @Param("viewerId") long viewerId,
            @Param("feedIds") List<Long> feedIds
    );

    public interface MyReactionRow {
        Long getFeedId();
        String getReactionKey();
    }
}
