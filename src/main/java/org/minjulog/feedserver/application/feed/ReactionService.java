package org.minjulog.feedserver.application.feed;

import lombok.RequiredArgsConstructor;
import org.minjulog.feedserver.domain.feed.model.Emoji;
import org.minjulog.feedserver.domain.feed.model.Feed;
import org.minjulog.feedserver.domain.feed.model.Reaction;
import org.minjulog.feedserver.domain.feed.model.UserProfile;
import org.minjulog.feedserver.domain.feed.repository.EmojiCountRepository;
import org.minjulog.feedserver.domain.feed.repository.FeedRepository;
import org.minjulog.feedserver.domain.feed.repository.ReactionRepository;
import org.minjulog.feedserver.domain.feed.repository.UserProfileRepository;
import org.minjulog.feedserver.presentation.feed.dto.ReactionPayloadDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReactionService {

    private final ReactionTypeService reactionTypeService;
    private final FeedRepository feedRepository;
    private final UserProfileRepository userProfileRepository;
    private final ReactionRepository reactionRepository;
    private final EmojiCountRepository emojiCountRepository;

    @Transactional
    public ReactionPayloadDto.Response applyReaction(Long actorId, ReactionPayloadDto.Request payload) {
        UUID feedId = payload.feedId();
        String emojiKey = payload.emojiKey();
        String unicode = payload.unicode();

        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new IllegalArgumentException("feed not found"));

        if (feed.isDeleted()) {
            throw new IllegalStateException("deleted feed");
        }

        UserProfile actor = userProfileRepository.findByUserId(actorId)
                .orElseGet(() -> userProfileRepository.saveAndFlush(new UserProfile(actorId)));

        Emoji emoji = reactionTypeService.getOrCreateDefaultEmoji(feed.getWorkspace().getId(), emojiKey, unicode);

        boolean pressedByMe;
        if (reactionRepository.existsByFeedIdAndUserProfileIdAndEmojiId(feedId, actor.getId(), emoji.getId())) {
            reactionRepository.deleteByFeedIdAndUserProfileIdAndEmojiId(feedId, actor.getId(), emoji.getId());
            emojiCountRepository.decrementOrDelete(feedId, emoji.getId());
            pressedByMe = false;
        } else {
            reactionRepository.save(
                    Reaction.builder()
                            .feed(feed)
                            .userProfile(actor)
                            .emoji(emoji)
                            .build()
            );
            emojiCountRepository.increment(feedId, emoji.getId());
            pressedByMe = true;
        }

        long newCount = emojiCountRepository.findCount(feedId, emoji.getId()).orElse(0L);

        return new ReactionPayloadDto.Response(
                actorId,
                feedId,
                emoji.getEmojiKey(),
                pressedByMe,
                newCount,
                emoji.getEmojiType(),
                emoji.getUnicode(),
                emoji.getObjectKey()
        );
    }
}
