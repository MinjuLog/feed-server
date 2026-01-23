package org.minjulog.feedserver.application;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.minjulog.feedserver.domain.model.Feed;
import org.minjulog.feedserver.domain.repository.FeedRepository;
import org.minjulog.feedserver.domain.model.Reaction;
import org.minjulog.feedserver.domain.repository.ReactionRepository;
import org.minjulog.feedserver.domain.repository.ReactionCountRepository;
import org.minjulog.feedserver.domain.model.ReactionType;
import org.minjulog.feedserver.domain.model.Profile;
import org.minjulog.feedserver.domain.repository.ProfileRepository;
import org.minjulog.feedserver.presentation.websocket.dto.ReactionPayloadDto;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReactionService {

    private final ReactionTypeService reactionTypeService;
    private final FeedRepository feedRepository;
    private final ProfileRepository profileRepository;
    private final ReactionRepository reactionRepository;
    private final ReactionCountRepository reactionCountRepository;

    @Transactional
    public ReactionPayloadDto.Response applyReaction(Long actorId, Long feedId, String key, String emoji) {
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new IllegalArgumentException("feed not found"));

        if (feed.isDeleted()) {
            throw new IllegalStateException("deleted feed");
        }

        Profile actor = profileRepository.findProfileByUserId(actorId);

        ReactionType reactionType =
                reactionTypeService.getOrCreateDefaultEmoji(key, emoji);


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

        return new ReactionPayloadDto.Response(
                actorId,
                feedId,
                reactionType.getKey(),
                pressedByMe,
                newCount,
                reactionType.getEmojiType(),
                reactionType.getEmoji(),
                reactionType.getImageUrl()
        );
    }


}

