package org.minjulog.feedserver.presentation.feed.dto;

import org.minjulog.feedserver.domain.feed.model.enumerate.EmojiType;

import java.util.UUID;

public class ReactionPayloadDto {

    public record Request(
            UUID feedId,
            String emojiKey,
            String unicode
    ) {
    }

    public record Response(
            Long actorId,
            UUID feedId,
            String emojiKey,
            boolean pressedByMe,
            long emojiCount,
            EmojiType emojiType,
            String unicode,
            String objectKey
    ) {
    }
}
