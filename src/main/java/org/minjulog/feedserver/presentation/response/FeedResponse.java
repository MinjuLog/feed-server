package org.minjulog.feedserver.presentation.response;

import java.util.List;
import java.util.UUID;

public class FeedResponse {

    public record Read(
            UUID id,
            Long authorId,
            String authorName,
            String content,
            String timestamp,
            List<AttachmentResponse.Read> attachments,
            List<ReactionResponse.Read> reactions
    ) {
    }

    public record Create(
            UUID id,
            Long workspaceId,
            Long authorId,
            String authorName,
            String content,
            String timestamp,
            List<AttachmentResponse.Attach> attachments,
            List<ReactionResponse.Apply> reactions
    ) {
    }
}
