package org.minjulog.feedserver.domain.repository;

import lombok.RequiredArgsConstructor;
import org.minjulog.feedserver.domain.model.Reaction;
import org.minjulog.feedserver.infrastructure.repository.JpaReactionRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ReactionRepository {
    private final JpaReactionRepository jpa;

    public List<JpaReactionRepository.MyReactionRow> findMyReactions(Long viewerId, List<UUID> feedIds) { return jpa.findMyReactions(viewerId, feedIds); }
    public boolean existsByFeedIdAndUserProfileIdAndEmojiId(UUID feedId, UUID userProfileId, UUID emojiId) { return jpa.existsByFeedIdAndUserProfileIdAndEmojiId(feedId, userProfileId, emojiId); }
    public long deleteByFeedIdAndUserProfileIdAndEmojiId(UUID feedId, UUID userProfileId, UUID emojiId) { return jpa.deleteByFeedIdAndUserProfileIdAndEmojiId(feedId, userProfileId, emojiId); }
    public List<UUID> findUserProfileIdsByFeedIdAndEmojiKey(UUID feedId, String emojiKey) { return jpa.findUserProfileIdsByFeedIdAndEmojiKey(feedId, emojiKey); }
    public Reaction save(Reaction reaction) { return jpa.save(reaction); }
}
