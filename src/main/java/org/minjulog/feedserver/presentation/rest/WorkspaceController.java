package org.minjulog.feedserver.presentation.rest;

import lombok.RequiredArgsConstructor;
import org.minjulog.feedserver.application.WorkspaceService;
import org.minjulog.feedserver.presentation.rest.dto.WorkspaceDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class WorkspaceController {

    private final WorkspaceService workspaceService;

    @PatchMapping("/workspaces/{workspaceId}/likes")
    public ResponseEntity<WorkspaceDto.IncrementLikeResponse> incrementLikeCount(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long workspaceId,
            @RequestBody WorkspaceDto.IncrementLikeRequest request
    ) {
        Long delta = request.delta();
        Long likeCount = workspaceService.incrementLike(workspaceId, delta);

        return ResponseEntity.ok(
                new WorkspaceDto.IncrementLikeResponse(workspaceId, likeCount)
        );
    }
}
