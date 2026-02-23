package org.minjulog.feedserver.presentation.request;

public class WorkspaceRequest {

    public record IncrementLike(
            Long delta
    ) {
    }
}
