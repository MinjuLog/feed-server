package org.minjulog.feedserver.presentation.rest.dto;

import org.minjulog.feedserver.domain.model.enumerate.EmojiType;

import java.util.List;
import java.util.Set;

public class ReactionDto {
    public record Response(
            String emojiKey,
            EmojiType emojiType,
            String objectKey,
            String unicode,
            Long emojiCount,
            boolean pressedByMe
    ) {
    }

    public record PressedUsersResponse(
            Set<String> usernames
    ) {
    }

    public record CustomEmojisResponse(
            List<CustomEmojiResponse> customEmojis
    ) {
    }

    public record CustomEmojiResponse(
            String emojiKey,
            String objectKey
    ) {
    }

    public record CreateCustomEmojiRequest(
            String objectKey,
            String emojiKey
    ) {
    }
}
