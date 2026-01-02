package org.minjulog.feedserver.view;

import java.security.Principal;
import java.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.minjulog.feedserver.application.*;
import org.minjulog.feedserver.domain.feed.Feed;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.stereotype.Controller;

import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@Slf4j
public class FeedController {

    private final FeedService feedService;

    @MessageMapping("/feed")
    @SendTo("/topic/room.1")
    public FeedMessageResponse send(@Payload FeedMessageRequest payload, Principal principal) {
        StompPrincipal stompPrincipal = (StompPrincipal) principal;
        Feed feed = feedService.saveFeed(stompPrincipal.getUserId(), payload.content());
        return new FeedMessageResponse(
                feed.getFeedId(),
                feed.getAuthorId(),
                feed.getAuthorName(),
                feed.getContent(),
                feed.getLikeCount(),
                feed.getCreatedAt().toString()
        );
    }

    @MessageMapping("/feed/like")
    @SendTo("/topic/room.1/like")
    public LikeResponse pressLike(@Payload LikeRequest req, Principal principal) {
        StompPrincipal stompPrincipal = (StompPrincipal) principal;
        long feedId = req.feedId();
        long userId = stompPrincipal.getUserId();
        feedService.like(userId, feedId);

        return new LikeResponse(userId, feedId);
    }

    @ResponseBody
    @GetMapping("/api/feeds")
    public List<FeedMessageResponse> findAllFeeds() {
        List<Feed> feeds = feedService.findAllFeeds();
        return feeds.stream()
                .map(f -> new FeedMessageResponse(
                        f.getFeedId(),
                        f.getAuthorId(),
                        f.getAuthorProfile().getUsername(),
                        f.getContent(),
                        f.getLikeCount(),
                        f.getCreatedAt().toString()
                ))
                .toList();
    }

    @ResponseBody
    @GetMapping("/api/online-users")
    public Set<String> findAllOnlineUsers() {
        return feedService.findAllOnlineUsers();
    }

    public record FeedMessageRequest(long authorId, String content) {}
    public record FeedMessageResponse(long id, long authorId, String authorName, String content, int likes, String timestamp) {}
    public record LikeRequest(long feedId) {}
    public record LikeResponse(long actorId, long feedId) {}
}