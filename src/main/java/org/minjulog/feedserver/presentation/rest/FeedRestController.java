package org.minjulog.feedserver.presentation.rest;

import lombok.RequiredArgsConstructor;
import org.minjulog.feedserver.application.FeedService;
import org.minjulog.feedserver.presentation.rest.dto.FeedDto;
import org.minjulog.feedserver.presentation.rest.dto.ReactionDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class FeedRestController {

    private final FeedService feedService;

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
        return ResponseEntity.ok(feedService.deleteFeed(userId, feedId));
    }

    @GetMapping("/api/online-users")
    public Set<String> findAllOnlineUsers() {
        return feedService.findAllOnlineUsers();
    }
}
