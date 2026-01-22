package org.minjulog.feedserver.domain.feed.reaction.count;

import org.minjulog.feedserver.domain.feed.reaction.type.EmojiType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReactionCountRepository extends JpaRepository<ReactionCount, Long> {

    @Query("""
                select rc.feed.feedId as feedId,
                       rt.key as reactionKey,
                       rt.imageUrl as imageUrl,
                       rt.emoji as emoji,
                       rc.count as count
                from ReactionCount rc
                join rc.reactionType rt
                where rc.feed.feedId in :feedIds
            """)
    List<ReactionCountRow> findReactionCountsByFeedIds(@Param("feedIds") List<Long> feedIds);

    public interface ReactionCountRow {
        Long getFeedId();
        String getReactionKey();
        EmojiType getEmojiType();
        String getImageUrl();
        String getEmoji();
        Long getCount();
    }

    @Modifying
    @Query(value = """
                        insert into reaction_count (feed_id, reaction_type_id, count)
                        values (:feedId, :reactionTypeId, 1)
                        on duplicate key update count = count + 1
                    """, nativeQuery = true)
    void increment(@Param("feedId") Long feedId, @Param("reactionTypeId") Long reactionTypeId);

    @Modifying
    @Query(value = """
                        update reaction_count
                        set count = count - 1
                        where feed_id = :feedId
                          and reaction_type_id = :reactionTypeId
                    """, nativeQuery = true)
    int decrement(@Param("feedId") Long feedId, @Param("reactionTypeId") Long reactionTypeId);

    @Modifying
    @Query(value = """
                        delete from reaction_count
                        where feed_id = :feedId
                          and reaction_type_id = :reactionTypeId
                          and count <= 0
                    """, nativeQuery = true)
    int deleteIfZero(@Param("feedId") Long feedId, @Param("reactionTypeId") Long reactionTypeId);

    default void decrementOrDelete(Long feedId, Long reactionTypeId) {
        decrement(feedId, reactionTypeId);
        deleteIfZero(feedId, reactionTypeId);
    }

    @Query(value = """
                        select count from reaction_count
                        where feed_id = :feedId
                          and reaction_type_id = :reactionTypeId
                        limit 1
                    """, nativeQuery = true)
    Optional<Integer> findCount(@Param("feedId") Long feedId, @Param("reactionTypeId") Long reactionTypeId);
}
