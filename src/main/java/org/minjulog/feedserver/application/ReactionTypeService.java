package org.minjulog.feedserver.application;

import lombok.RequiredArgsConstructor;
import org.minjulog.feedserver.domain.feed.model.Emoji;
import org.minjulog.feedserver.domain.feed.model.Workspace;
import org.minjulog.feedserver.domain.feed.model.enumerate.EmojiType;
import org.minjulog.feedserver.domain.feed.repository.EmojiRepository;
import org.minjulog.feedserver.domain.feed.repository.WorkspaceRepository;
import org.minjulog.feedserver.presentation.rest.dto.ReactionDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReactionTypeService {

    private final EmojiRepository emojiRepository;
    private final WorkspaceRepository workspaceRepository;

    @Value("${env.REACTION.WORKSPACE_ID:1}")
    private Long defaultWorkspaceId;

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

    @Transactional
    public ReactionDto.CustomEmojiResponse createCustomEmoji(String emojiKey, String objectKey) {
        Workspace workspace = getOrCreateWorkspace(defaultWorkspaceId);

        Emoji emoji = Emoji.builder()
                .workspace(workspace)
                .emojiKey(emojiKey)
                .emojiType(EmojiType.CUSTOM)
                .objectKey(objectKey)
                .build();

        Emoji saved = emojiRepository.saveAndFlush(emoji);

        return new ReactionDto.CustomEmojiResponse(saved.getEmojiKey(), saved.getObjectKey());
    }

    @Transactional(readOnly = true)
    public ReactionDto.CustomEmojisResponse getCustomEmojis() {
        return new ReactionDto.CustomEmojisResponse(
                emojiRepository
                        .findByWorkspaceIdAndEmojiType(defaultWorkspaceId, EmojiType.CUSTOM)
                        .stream()
                        .map(a -> new ReactionDto.CustomEmojiResponse(a.getEmojiKey(), a.getObjectKey()))
                        .toList()
        );
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
