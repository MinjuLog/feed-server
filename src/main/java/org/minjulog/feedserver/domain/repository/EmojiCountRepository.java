package org.minjulog.feedserver.domain.repository;

import lombok.RequiredArgsConstructor;
import org.minjulog.feedserver.infrastructure.repository.JpaEmojiCountRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class EmojiCountRepository {
    private final JpaEmojiCountRepository jpa;

    public List<JpaEmojiCountRepository.EmojiCountRow> findReactionCountsByFeedIds(List<UUID> feedIds) { return jpa.findReactionCountsByFeedIds(feedIds); }
    public void increment(UUID feedId, UUID emojiId) { jpa.increment(feedId, emojiId); }
    public void decrementOrDelete(UUID feedId, UUID emojiId) { jpa.decrementOrDelete(feedId, emojiId); }
    public Optional<Long> findCount(UUID feedId, UUID emojiId) { return jpa.findCount(feedId, emojiId); }
}
