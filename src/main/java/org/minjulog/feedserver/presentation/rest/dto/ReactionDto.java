package org.minjulog.feedserver.presentation.rest.dto;
import org.minjulog.feedserver.domain.model.EmojiType;

import java.util.List;
import java.util.Set;

public class ReactionDto {
    public record Response(
            String reactionKey,
            EmojiType emojiType,
            String objectKey,
            String emoji,
            Long count,
            boolean pressedByMe
    ) {}

    public record PressedUsersResponse(
            Set<String> usernames
    ) {}

    public record CustomEmojisResponse(
            List<CustomEmojiResponse> customEmojis
    ) {}

    public record CustomEmojiResponse(
            String reactionKey,
            String objectKey
    ) {}

    public record CreateCustomEmojiRequest(
            String objectKey,
            String reactionKey
//            Long workspaceId
    ) {}
}
