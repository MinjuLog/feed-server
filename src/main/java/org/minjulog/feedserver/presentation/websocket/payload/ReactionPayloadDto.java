package org.minjulog.feedserver.presentation.websocket.payload;

import org.minjulog.feedserver.domain.feed.reaction.type.EmojiType;

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
            int count,
            EmojiType emojiType,
            String emoji,
            String imageUrl
    ) {}
}
