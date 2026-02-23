package org.minjulog.feedserver.presentation.controller;

import lombok.RequiredArgsConstructor;
import org.minjulog.feedserver.application.feed.WorkspaceService;
import org.minjulog.feedserver.presentation.request.*;
import org.minjulog.feedserver.presentation.response.*;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class WorkspaceController {

    private final WorkspaceService workspaceService;
    private final SimpMessagingTemplate messagingTemplate;

    @GetMapping("/api/workspaces/{workspaceId}")
    public ResponseEntity<WorkspaceResponse.Get> getWorkspace(
            @PathVariable("workspaceId") Long workspaceId
    ) {
        return ResponseEntity.ok(workspaceService.getWorkspace(workspaceId));
    }

    @PatchMapping("/api/workspaces/{workspaceId}/like-count")
    public ResponseEntity<WorkspaceResponse.IncrementLike> incrementLikeCount(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable("workspaceId") Long workspaceId,
            @RequestBody WorkspaceRequest.IncrementLike request
    ) {
        WorkspaceResponse.IncrementLike response =
                workspaceService.incrementLike(userId, workspaceId, request.delta());

        messagingTemplate.convertAndSend(
                "/topic/workspace." + workspaceId + "/like",
                response
        );

        return ResponseEntity.ok(response);
    }
}
