package org.minjulog.feedserver.presentation.websocket.dto;

import java.util.List;
import java.util.UUID;

public class FeedPayloadDto {

    public record Request(
            Long workspaceId,
            Long authorId,
            String content,
            List<AttachmentPayloadDto.Request> attachments
    ) {
    }

    public record Response(
            UUID id,
            Long workspaceId,
            Long authorId,
            String authorName,
            String content,
            String timestamp,
            List<AttachmentPayloadDto.Response> attachments,
            List<ReactionPayloadDto.Response> reactions
    ) {
    }
}
