package org.minjulog.feedserver.presentation.websocket.payload;
import java.util.List;

public class FeedPayloadDto {

    public record Request(
            long authorId,
            String content,
            List<AttachmentPayloadDto.Request> attachments
    ) {}

    public record Response(
            Long id,
            Long authorId,
            String authorName,
            String content,
            String timestamp,
            List<AttachmentPayloadDto.Response> attachments,
            List<ReactionPayloadDto.Response> reactions
    ) {}

}
