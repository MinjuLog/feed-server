package org.minjulog.feedserver.presentation.rest.dto;

public class WorkspaceDto {

    public record IncrementLikeRequest(
            Long delta
    ) {}

    public record IncrementLikeResponse(
            Long workspaceId,
            Long likeCount
    ) {}
}
