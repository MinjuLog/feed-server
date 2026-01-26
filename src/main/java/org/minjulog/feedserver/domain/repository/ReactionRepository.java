package org.minjulog.feedserver.domain.repository;

import org.minjulog.feedserver.domain.model.Reaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReactionRepository extends JpaRepository<Reaction, Long> {

    @Query("""
                select r.feed.feedId as feedId,
                       rt.reactionKey as reactionKey
                from Reaction r
                join r.type rt
                where r.profile.userId = :viewerId
                  and r.feed.feedId in :feedIds
            """)
    List<MyReactionRow> findMyReactions(
            @Param("viewerId") long viewerId,
            @Param("feedIds") List<Long> feedIds
    );

    @Query("""
        select case when count(r) > 0 then true else false end
        from Reaction r
        where r.feed.feedId = :feedId
          and r.profile.profileId = :profileId
          and r.type.id = :reactionTypeId
    """)
    boolean existsByFeedIdAndProfileIdAndReactionTypeId(
            @Param("feedId") Long feedId,
            @Param("profileId") Long profileId,
            @Param("reactionTypeId") Long reactionTypeId
    );

    @Modifying
    @Query("""
        delete from Reaction r
        where r.feed.feedId = :feedId
          and r.profile.profileId = :profileId
          and r.type.id = :reactionTypeId
    """)
    Long deleteByFeedIdAndProfileIdAndReactionTypeId(
            @Param("feedId") Long feedId,
            @Param("profileId") Long profileId,
            @Param("reactionTypeId") Long reactionTypeId
    );

    @Query("""
                select distinct r.profile.profileId
                from Reaction r
                where r.feed.feedId = :feedId
                  and r.type.reactionKey = :reactionKey
            """)
    List<Long> findProfileIdsByFeedIdAndReactionKey(
            @Param("feedId") Long feedId,
            @Param("reactionKey") String reactionKey
    );

    public interface MyReactionRow {
        Long getFeedId();
        String getReactionKey();
    }
}
