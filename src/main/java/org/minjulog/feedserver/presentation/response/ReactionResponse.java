package org.minjulog.feedserver.presentation.response;

import org.minjulog.feedserver.domain.model.enumeration.EmojiType;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public class ReactionResponse {

    public record Read(
            String emojiKey,
            EmojiType emojiType,
            String objectKey,
            String unicode,
            Long emojiCount,
            boolean pressedByMe
    ) {
    }

    public record FindPressedUsers(
            Set<String> usernames
    ) {
    }

    public record FindCustomEmojis(
            List<FindCustomEmoji> customEmojis
    ) {
    }

    public record FindCustomEmoji(
            String emojiKey,
            String objectKey
    ) {
    }

    public record Apply(
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
