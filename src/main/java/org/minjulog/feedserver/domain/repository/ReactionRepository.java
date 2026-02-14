package org.minjulog.feedserver.domain.repository;

import org.minjulog.feedserver.domain.model.Reaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ReactionRepository extends JpaRepository<Reaction, UUID> {

    @Query("""
                select r.feed.id as feedId,
                       e.emojiKey as emojiKey
                from Reaction r
                join r.emoji e
                where r.userProfile.userId = :viewerId
                  and r.feed.id in :feedIds
            """)
    List<MyReactionRow> findMyReactions(
            @Param("viewerId") Long viewerId,
            @Param("feedIds") List<UUID> feedIds
    );

    @Query("""
        select case when count(r) > 0 then true else false end
        from Reaction r
        where r.feed.id = :feedId
          and r.userProfile.id = :userProfileId
          and r.emoji.id = :emojiId
    """)
    boolean existsByFeedIdAndUserProfileIdAndEmojiId(
            @Param("feedId") UUID feedId,
            @Param("userProfileId") UUID userProfileId,
            @Param("emojiId") UUID emojiId
    );

    @Modifying
    @Query("""
        delete from Reaction r
        where r.feed.id = :feedId
          and r.userProfile.id = :userProfileId
          and r.emoji.id = :emojiId
    """)
    long deleteByFeedIdAndUserProfileIdAndEmojiId(
            @Param("feedId") UUID feedId,
            @Param("userProfileId") UUID userProfileId,
            @Param("emojiId") UUID emojiId
    );

    @Query("""
                select distinct r.userProfile.id
                from Reaction r
                where r.feed.id = :feedId
                  and r.emoji.emojiKey = :emojiKey
            """)
    List<UUID> findUserProfileIdsByFeedIdAndEmojiKey(
            @Param("feedId") UUID feedId,
            @Param("emojiKey") String emojiKey
    );

    interface MyReactionRow {
        UUID getFeedId();
        String getEmojiKey();
    }
}
