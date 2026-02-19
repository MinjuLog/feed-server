package org.minjulog.feedserver.presentation.rest;

import lombok.RequiredArgsConstructor;
import org.minjulog.feedserver.application.FeedService;
import org.minjulog.feedserver.infra.messaging.StompPrincipal;
import org.minjulog.feedserver.presentation.rest.dto.FeedDto;
import org.minjulog.feedserver.presentation.rest.dto.ReactionDto;
import org.minjulog.feedserver.presentation.websocket.dto.FeedPayloadDto;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class FeedRestController {

    private final FeedService feedService;
    private final SimpMessagingTemplate messagingTemplate;

    @PostMapping("/api/feeds")
    public ResponseEntity<FeedPayloadDto.Response> createFeed(
            @RequestHeader(value = "X-User-Id", required = false) Long authorId,
            @RequestBody FeedPayloadDto.Request req
    ) {
        FeedPayloadDto.Response saved = feedService.messagingFeed(authorId, req);

        messagingTemplate.convertAndSend("/topic/workspace." + saved.workspaceId(), saved);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/api/feeds")
    public ResponseEntity<List<FeedDto.Response>> findAllFeeds(
            @RequestHeader("X-User-Id") Long userId
    ) {
        return ResponseEntity.ok(feedService.findAllFeeds(userId));
    }

    @GetMapping("/api/feeds/{feedId}/reactions/{emojiKey}/users")
    public ResponseEntity<ReactionDto.PressedUsersResponse> sendReactionPressedUsers(
            @PathVariable("feedId") UUID feedId,
            @PathVariable("emojiKey") String emojiKey,
            @RequestHeader("X-User-Id") Long userId
    ) {
        return ResponseEntity.ok(
                new ReactionDto.PressedUsersResponse(
                        feedService.findReactionPressedUsers(feedId, userId, emojiKey)
                )
        );
    }

    @DeleteMapping("/api/feeds/{feedId}")
    public ResponseEntity<Boolean> deleteFeed(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable("feedId") UUID feedId
    ) {
        FeedService.DeleteFeedResult result = feedService.deleteFeed(userId, feedId);
        messagingTemplate.convertAndSend(
                "/topic/workspace." + result.workspaceId() + "/delete",
                new FeedDeleteEvent(result.feedId(), userId)
        );
        return ResponseEntity.ok(result.deleted());
    }

    @GetMapping("/api/online-users")
    public Set<String> findAllOnlineUsers() {
        return feedService.findAllOnlineUsers();
    }

    private record FeedDeleteEvent(
            UUID feedId,
            Long actorId
    ) {
    }
}
