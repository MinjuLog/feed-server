package org.minjulog.feedserver.presentation.response;

public class WorkspaceResponse {

    public record Get(
            Long likeCount
    ) {
    }

    public record IncrementLike(
            Long actorId,
            Long workspaceId,
            Long likeCount
    ) {
    }
}
