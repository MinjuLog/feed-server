package org.minjulog.feedserver.application.feed;

import lombok.RequiredArgsConstructor;
import org.minjulog.feedserver.domain.feed.reaction.type.ReactionRenderType;
import org.minjulog.feedserver.domain.feed.reaction.type.ReactionType;
import org.minjulog.feedserver.domain.feed.reaction.type.ReactionTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReactionTypeService {

    private final ReactionTypeRepository reactionTypeRepository;

    @Transactional
    public ReactionType createUnicodeReaction(String key, String unicode) {
        ReactionType reactionType = ReactionType.builder()
                .workspaceId(1L)
                .key(key)
                .renderType(ReactionRenderType.UNICODE)
                .unicode(unicode)
                .build();

        return reactionTypeRepository.saveAndFlush(reactionType);
    }

    @Transactional
    public ReactionType createImageReaction(String key, String imageUrl) {
        ReactionType reactionType = ReactionType.builder()
                .workspaceId(1L)
                .key(key)
                .renderType(ReactionRenderType.IMAGE)
                .imageUrl(imageUrl)
                .build();

        return reactionTypeRepository.saveAndFlush(reactionType);
    }
}
