package org.minjulog.feedserver.presentation.rest;

import lombok.RequiredArgsConstructor;
import org.minjulog.feedserver.application.WorkspaceService;
import org.minjulog.feedserver.presentation.rest.dto.WorkspaceDto;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class WorkspaceController {

    private final WorkspaceService workspaceService;
    private final SimpMessagingTemplate messagingTemplate;

    @GetMapping("/api/workspaces/{workspaceId}")
    public ResponseEntity<WorkspaceDto.WorkspaceResponse> getWorkspace(
            @PathVariable("workspaceId") Long workspaceId
    ) {
        return ResponseEntity.ok(workspaceService.getWorkspace(workspaceId));
    }

    @PatchMapping("/api/workspaces/{workspaceId}/like-count")
    public ResponseEntity<WorkspaceDto.IncrementLikeResponse> incrementLikeCount(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable("workspaceId") Long workspaceId,
            @RequestBody WorkspaceDto.IncrementLikeRequest request
    ) {
        WorkspaceDto.IncrementLikeResponse response =
                workspaceService.incrementLike(userId, workspaceId, request.delta());

        messagingTemplate.convertAndSend(
                "/topic/workspace." + workspaceId + "/like",
                response
        );

        return ResponseEntity.ok(response);
    }
}
