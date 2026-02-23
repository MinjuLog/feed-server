package org.minjulog.feedserver.presentation.request;

import java.util.UUID;

public class ReactionRequest {

    public record CreateCustomEmoji(
            String objectKey,
            String emojiKey
    ) {
    }

    public record Apply(
            UUID feedId,
            String emojiKey,
            String unicode
    ) {
    }
}
