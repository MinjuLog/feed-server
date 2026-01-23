package org.minjulog.feedserver.presentation.dto;
import java.util.List;

public class FeedDto {

    public record Response(
            Long id,
            Long authorId,
            String authorName,
            String content,
            String timestamp,
            List<AttachmentDto.Response> attachments,
            List<ReactionDto.Response> reactions
    ) {}
}
