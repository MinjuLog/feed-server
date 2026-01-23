package org.minjulog.feedserver.presentation.dto;
import org.minjulog.feedserver.domain.feed.reaction.type.EmojiType;

import java.util.Set;

public class ReactionDto {
    public record Response(
            String reactionKey,
            EmojiType emojiType,
            String emojiUrl,
            String emoji,
            Long count,
            boolean pressedByMe
    ) {}

    public record PressedUsersResponse(
            Set<String> usernames
    ) {}
}
