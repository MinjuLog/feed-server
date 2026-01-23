package org.minjulog.feedserver.application;

import lombok.RequiredArgsConstructor;
import org.minjulog.feedserver.domain.model.EmojiType;
import org.minjulog.feedserver.domain.model.ReactionType;
import org.minjulog.feedserver.domain.repository.ReactionTypeRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReactionTypeService {

    private final ReactionTypeRepository reactionTypeRepository;

    @Transactional
    public ReactionType getOrCreateDefaultEmoji(String key, String unicodeEmoji) {
        return reactionTypeRepository.findByKey(key)
                .orElseGet(() -> {
                    try {
                        return reactionTypeRepository.save(
                                ReactionType.builder()
                                        .workspaceId(1L)
                                        .key(key)
                                        .emojiType(EmojiType.DEFAULT)
                                        .emoji(unicodeEmoji)
                                        .build()
                        );
                    } catch (DataIntegrityViolationException e) {
                        // 동시성으로 이미 생성된 경우 재조회
                        return reactionTypeRepository.findByKey(key)
                                .orElseThrow(() -> e);
                    }
                });
    }


    @Transactional
    public ReactionType createImageEmoji(String key, String imageUrl) {
        ReactionType reactionType = ReactionType.builder()
                .workspaceId(1L)
                .key(key)
                .emojiType(EmojiType.CUSTOM)
                .imageUrl(imageUrl)
                .build();

        return reactionTypeRepository.saveAndFlush(reactionType);
    }
}
