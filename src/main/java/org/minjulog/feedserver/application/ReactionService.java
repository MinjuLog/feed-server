package org.minjulog.feedserver.application;

import lombok.RequiredArgsConstructor;
import org.minjulog.feedserver.domain.model.Feed;
import org.minjulog.feedserver.domain.model.Profile;
import org.minjulog.feedserver.domain.model.Reaction;
import org.minjulog.feedserver.domain.model.ReactionType;
import org.minjulog.feedserver.domain.repository.FeedRepository;
import org.minjulog.feedserver.domain.repository.ProfileRepository;
import org.minjulog.feedserver.domain.repository.ReactionCountRepository;
import org.minjulog.feedserver.domain.repository.ReactionRepository;
import org.minjulog.feedserver.presentation.websocket.dto.ReactionPayloadDto;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReactionService {

    private final ReactionTypeService reactionTypeService;
    private final FeedRepository feedRepository;
    private final ProfileRepository profileRepository;
    private final ReactionRepository reactionRepository;
    private final ReactionCountRepository reactionCountRepository;

    @Transactional
    public ReactionPayloadDto.Response applyReaction(Long actorId, ReactionPayloadDto.Request payload) {
        Long feedId = payload.feedId();
        String key = payload.key();
        String emoji = payload.emoji();

        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new IllegalArgumentException("feed not found"));

        if (feed.isDeleted()) {
            throw new IllegalStateException("deleted feed");
        }

        Profile actor = profileRepository.findProfileByUserId(actorId);

        ReactionType reactionType =
                reactionTypeService.getOrCreateDefaultEmoji(key, emoji);

        boolean pressedByMe;
        if (reactionRepository.existsByFeedIdAndProfileIdAndReactionTypeId(feedId, actor.getProfileId(), reactionType.getId())) {
            reactionRepository.deleteByFeedIdAndProfileIdAndReactionTypeId(feedId, actor.getProfileId(), reactionType.getId());
            reactionCountRepository.decrementOrDelete(feedId, reactionType.getId());
            pressedByMe = false;
        } else {
            try {
                reactionRepository.save(
                        Reaction.builder()
                                .feed(feed)
                                .profile(actor)
                                .type(reactionType)
                                .build()
                );
                reactionCountRepository.increment(feedId, reactionType.getId());
                pressedByMe = true;
            } catch (DataIntegrityViolationException e) {
                pressedByMe = true;
            }
        }

        long newCount = reactionCountRepository.findCount(feedId, reactionType.getId()).orElse(0L);

        return new ReactionPayloadDto.Response(
                actorId,
                feedId,
                reactionType.getReactionKey(),
                pressedByMe,
                newCount,
                reactionType.getEmojiType(),
                reactionType.getEmoji(),
                reactionType.getObjectKey()
        );
    }
}
