package org.minjulog.feedserver.presentation.feed.dto;

public class WorkspaceDto {

    public record WorkspaceResponse(
            Long likeCount
    ) {
    }

    public record IncrementLikeRequest(
            Long delta
    ) {
    }

    public record IncrementLikeResponse(
            Long actorId,
            Long workspaceId,
            Long likeCount
    ) {
    }
}
