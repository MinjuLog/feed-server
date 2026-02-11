package org.minjulog.feedserver.application;

import lombok.RequiredArgsConstructor;
import org.minjulog.feedserver.domain.model.EmojiType;
import org.minjulog.feedserver.domain.model.ReactionType;
import org.minjulog.feedserver.domain.repository.ReactionTypeRepository;
import org.minjulog.feedserver.presentation.rest.dto.ReactionDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReactionTypeService {

    private final ReactionTypeRepository reactionTypeRepository;

    @Value("${env.REACTION.WORKSPACE_ID:1}")
    private Long workspaceId;

    @Transactional
    public ReactionType getOrCreateDefaultEmoji(String reactionKey, String unicodeEmoji) {
        return reactionTypeRepository.findByReactionKey(reactionKey)
                .orElseGet(() -> {
                    try {
                        return reactionTypeRepository.save(
                                ReactionType.builder()
                                        .workspaceId(workspaceId)
                                        .reactionKey(reactionKey)
                                        .emojiType(EmojiType.DEFAULT)
                                        .emoji(unicodeEmoji)
                                        .build()
                        );
                    } catch (DataIntegrityViolationException e) {
                        return reactionTypeRepository.findByReactionKey(reactionKey)
                                .orElseThrow(() -> e);
                    }
                });
    }

    @Transactional
    public ReactionDto.CustomEmojiResponse createCustomEmoji(String reactionKey, String objectKey) {
        ReactionType reactionType = ReactionType.builder()
                .workspaceId(workspaceId)
                .reactionKey(reactionKey)
                .emojiType(EmojiType.CUSTOM)
                .objectKey(objectKey)
                .build();

        ReactionType saved = reactionTypeRepository.saveAndFlush(reactionType);

        return new ReactionDto.CustomEmojiResponse(saved.getReactionKey(), saved.getObjectKey());
    }

    @Transactional(readOnly = true)
    public ReactionDto.CustomEmojisResponse getCustomEmojis() {
        return new ReactionDto.CustomEmojisResponse(
                reactionTypeRepository
                        .findByEmojiType(EmojiType.CUSTOM)
                        .stream()
                        .map(
                                a -> new ReactionDto.CustomEmojiResponse(
                                        a.getReactionKey(),
                                        a.getObjectKey()
                                ))
                        .toList()
        );
    }
}
