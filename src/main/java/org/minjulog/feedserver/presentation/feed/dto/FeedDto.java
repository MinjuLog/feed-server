package org.minjulog.feedserver.presentation.feed.dto;

import java.util.List;
import java.util.UUID;

public class FeedDto {

    public record Response(
            UUID id,
            Long authorId,
            String authorName,
            String content,
            String timestamp,
            List<AttachmentDto.Response> attachments,
            List<ReactionDto.Response> reactions
    ) {
    }
}
