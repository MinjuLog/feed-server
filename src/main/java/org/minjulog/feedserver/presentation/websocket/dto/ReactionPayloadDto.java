package org.minjulog.feedserver.presentation.websocket.dto;

import org.minjulog.feedserver.domain.model.EmojiType;

public class ReactionPayloadDto {

    public record Request(
            Long feedId,
            String key,
            String emoji
    ) {}

    public record Response(
            Long actorId,
            Long feedId,
            String key,
            boolean pressedByMe,
            long count,
            EmojiType emojiType,
            String emoji,
            String objectKey
    ) {}
}
