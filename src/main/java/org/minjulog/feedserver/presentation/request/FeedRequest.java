package org.minjulog.feedserver.presentation.request;

import java.util.List;

public class FeedRequest {

    public record Create(
            Long workspaceId,
            Long authorId,
            String content,
            List<AttachmentRequest.Attach> attachments
    ) {
    }
}
