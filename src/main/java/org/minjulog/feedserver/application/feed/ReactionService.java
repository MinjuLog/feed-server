package org.minjulog.feedserver.application.feed;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.minjulog.feedserver.domain.feed.Feed;
import org.minjulog.feedserver.domain.feed.FeedRepository;
import org.minjulog.feedserver.domain.feed.reaction.Reaction;
import org.minjulog.feedserver.domain.feed.reaction.ReactionRepository;
import org.minjulog.feedserver.domain.feed.reaction.count.ReactionCountRepository;
import org.minjulog.feedserver.domain.feed.reaction.type.ReactionType;
import org.minjulog.feedserver.domain.feed.reaction.type.ReactionTypeRepository;
import org.minjulog.feedserver.domain.profile.Profile;
import org.minjulog.feedserver.domain.profile.ProfileRepository;
import org.minjulog.feedserver.view.FeedController.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReactionService {

    private final FeedRepository feedRepository;
    private final ProfileRepository profileRepository;

    private final ReactionTypeRepository reactionTypeRepository;
    private final ReactionRepository reactionRepository;
    private final ReactionCountRepository reactionCountRepository;

    @Transactional
    public ReactionResponse applyReaction(Long actorId, Long feedId, String key) {
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new IllegalArgumentException("feed not found"));

        if (feed.isDeleted()) {
            throw new IllegalStateException("deleted feed");
        }

        Profile actor = profileRepository.findProfileByUserId(actorId);

        ReactionType reactionType = reactionTypeRepository
                .findByKey(key)
                .orElseThrow(() -> new IllegalArgumentException("reaction type not found"));

        // 토글
        boolean pressedByMe;
        if (reactionRepository.existsByFeedIdAndProfileIdAndReactionTypeId(feedId, actor.getProfileId(), reactionType.getId())) {
            reactionRepository.deleteByFeedIdAndProfileIdAndReactionTypeId(feedId, actor.getProfileId(), reactionType.getId());
            reactionCountRepository.decrementOrDelete(feedId, reactionType.getId());
            pressedByMe = false;
        } else {
            // 동시성에서 중복 insert는 unique로 막히고, 여기서 예외 처리로 흡수 가능
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
                // 거의 동시에 눌러서 이미 들어간 경우
                pressedByMe = true;
            }
        }

        int newCount = reactionCountRepository.findCount(feedId, reactionType.getId()).orElse(0);

        return new ReactionResponse(
                actorId,
                feedId,
                reactionType.getKey(),
                pressedByMe,
                newCount,
                reactionType.getRenderType(),
                reactionType.getUnicode(),
                reactionType.getImageUrl()
        );
    }
}

