package org.minjulog.feedserver.domain.repository;

import org.minjulog.feedserver.domain.model.EmojiCount;
import org.minjulog.feedserver.domain.model.enumerate.EmojiType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EmojiCountRepository extends JpaRepository<EmojiCount, UUID> {

    @Query("""
                select ec.feed.id as feedId,
                       e.emojiType as emojiType,
                       e.emojiKey as emojiKey,
                       e.objectKey as objectKey,
                       e.unicode as unicode,
                       ec.emojiCount as emojiCount
                from EmojiCount ec
                join ec.emoji e
                where ec.feed.id in :feedIds
            """)
    List<EmojiCountRow> findReactionCountsByFeedIds(@Param("feedIds") List<UUID> feedIds);

    interface EmojiCountRow {
        UUID getFeedId();
        String getEmojiKey();
        EmojiType getEmojiType();
        String getObjectKey();
        String getUnicode();
        Long getEmojiCount();
    }

    @Modifying
    @Query(value = """
                        insert into reaction_count (id, feed_id, emoji_id, emoji_count)
                        values (UUID_TO_BIN(UUID()), :feedId, :emojiId, 1)
                        on duplicate key update emoji_count = emoji_count + 1
                    """, nativeQuery = true)
    void increment(@Param("feedId") UUID feedId, @Param("emojiId") UUID emojiId);

    @Modifying
    @Query(value = """
                        update reaction_count
                        set emoji_count = emoji_count - 1
                        where feed_id = :feedId
                          and emoji_id = :emojiId
                    """, nativeQuery = true)
    int decrement(@Param("feedId") UUID feedId, @Param("emojiId") UUID emojiId);

    @Modifying
    @Query(value = """
                        delete from reaction_count
                        where feed_id = :feedId
                          and emoji_id = :emojiId
                          and emoji_count <= 0
                    """, nativeQuery = true)
    int deleteIfZero(@Param("feedId") UUID feedId, @Param("emojiId") UUID emojiId);

    default void decrementOrDelete(UUID feedId, UUID emojiId) {
        decrement(feedId, emojiId);
        deleteIfZero(feedId, emojiId);
    }

    @Query(value = """
                        select emoji_count from reaction_count
                        where feed_id = :feedId
                          and emoji_id = :emojiId
                        limit 1
                    """, nativeQuery = true)
    Optional<Long> findCount(@Param("feedId") UUID feedId, @Param("emojiId") UUID emojiId);
}
