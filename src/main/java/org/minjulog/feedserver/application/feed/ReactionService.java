package org.minjulog.feedserver.application.feed;

import lombok.RequiredArgsConstructor;
import org.minjulog.feedserver.domain.model.Emoji;
import org.minjulog.feedserver.domain.model.Feed;
import org.minjulog.feedserver.domain.model.Reaction;
import org.minjulog.feedserver.domain.model.UserProfile;
import org.minjulog.feedserver.domain.model.Workspace;
import org.minjulog.feedserver.domain.model.enumeration.EmojiType;
import org.minjulog.feedserver.domain.repository.EmojiCountRepository;
import org.minjulog.feedserver.domain.repository.EmojiRepository;
import org.minjulog.feedserver.domain.repository.FeedRepository;
import org.minjulog.feedserver.domain.repository.ReactionRepository;
import org.minjulog.feedserver.domain.repository.UserProfileRepository;
import org.minjulog.feedserver.domain.repository.WorkspaceRepository;
import org.minjulog.feedserver.presentation.request.*;
import org.minjulog.feedserver.presentation.response.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReactionService {

    private final EmojiRepository emojiRepository;
    private final WorkspaceRepository workspaceRepository;
    private final FeedRepository feedRepository;
    private final UserProfileRepository userProfileRepository;
    private final ReactionRepository reactionRepository;
    private final EmojiCountRepository emojiCountRepository;

    @Value("${env.REACTION.WORKSPACE_ID:1}")
    private Long defaultWorkspaceId;

    @Transactional
    public ReactionResponse.FindCustomEmoji createCustomEmoji(String emojiKey, String objectKey) {
        Workspace workspace = getOrCreateWorkspace(defaultWorkspaceId);

        Emoji emoji = Emoji.builder()
                .workspace(workspace)
                .emojiKey(emojiKey)
                .emojiType(EmojiType.CUSTOM)
                .objectKey(objectKey)
                .build();

        Emoji saved = emojiRepository.saveAndFlush(emoji);
        return new ReactionResponse.FindCustomEmoji(saved.getEmojiKey(), saved.getObjectKey());
    }

    @Transactional(readOnly = true)
    public ReactionResponse.FindCustomEmojis getCustomEmojis() {
        return new ReactionResponse.FindCustomEmojis(
                emojiRepository
                        .findByWorkspaceIdAndEmojiType(defaultWorkspaceId, EmojiType.CUSTOM)
                        .stream()
                        .map(a -> new ReactionResponse.FindCustomEmoji(a.getEmojiKey(), a.getObjectKey()))
                        .toList()
        );
    }

    @Transactional
    public ReactionResponse.Apply applyReaction(Long actorId, ReactionRequest.Apply payload) {
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

        Emoji emoji = getOrCreateDefaultEmoji(feed.getWorkspace().getId(), emojiKey, unicode);

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

        return new ReactionResponse.Apply(
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

    @Transactional
    public Emoji getOrCreateDefaultEmoji(Long workspaceId, String emojiKey, String unicodeEmoji) {
        return emojiRepository.findByWorkspaceIdAndEmojiKey(workspaceId, emojiKey)
                .orElseGet(() -> emojiRepository.save(
                        Emoji.builder()
                                .workspace(getOrCreateWorkspace(workspaceId))
                                .emojiKey(emojiKey)
                                .emojiType(EmojiType.DEFAULT)
                                .unicode(unicodeEmoji)
                                .build()
                ));
    }

    private Workspace getOrCreateWorkspace(Long workspaceId) {
        return workspaceRepository.findById(workspaceId)
                .orElseGet(() -> workspaceRepository.saveAndFlush(
                        Workspace.builder()
                                .likeCount(0L)
                                .build()
                ));
    }
}
