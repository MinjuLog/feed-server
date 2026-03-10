package org.minjulog.feedserver.presentation.controller;

import lombok.RequiredArgsConstructor;
import org.minjulog.feedserver.application.feed.FeedService;
import org.minjulog.feedserver.presentation.request.*;
import org.minjulog.feedserver.presentation.response.*;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class FeedController {

    private final FeedService feedService;
    private final SimpMessagingTemplate messagingTemplate;

    @PostMapping("/api/feeds")
    public ResponseEntity<FeedResponse.Create> createFeed(
            @RequestHeader("X-User-Id") Long authorId,
            @RequestBody FeedRequest.Create req
    ) {
        FeedResponse.Create saved = feedService.messagingFeed(authorId, req);

        messagingTemplate.convertAndSend("/topic/workspace." + saved.workspaceId(), saved);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/api/feeds")
    public ResponseEntity<List<FeedResponse.Read>> findAllFeeds(
            @RequestHeader("X-User-Id") Long userId
    ) {
        return ResponseEntity.ok(feedService.findAllFeeds(userId));
    }

    @GetMapping("/api/feeds/{feedId}/reactions/{emojiKey}/users")
    public ResponseEntity<ReactionResponse.FindPressedUsers> sendReactionPressedUsers(
            @PathVariable("feedId") UUID feedId,
            @PathVariable("emojiKey") String emojiKey,
            @RequestHeader("X-User-Id") Long userId
    ) {
        return ResponseEntity.ok(
                new ReactionResponse.FindPressedUsers(
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
