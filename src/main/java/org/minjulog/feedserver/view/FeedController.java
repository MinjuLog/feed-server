package org.minjulog.feedserver.view;

import java.security.Principal;
import java.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.minjulog.feedserver.application.*;
import org.minjulog.feedserver.view.dto.*;
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
    public FeedResponse send(@Payload FeedMessage payload, Principal principal) {
        StompPrincipal stompPrincipal = (StompPrincipal) principal;
        return FeedResponse.feedToFeedResponse(
                feedService.saveFeed(
                        stompPrincipal.getUserId(),
                        payload.getContent()
                )
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
    public List<FeedResponse> findAllFeeds() {
        return feedService.findAllFeeds();
    }

    @ResponseBody
    @GetMapping("/api/online-users")
    public Set<String> findAllOnlineUsers() {
        return feedService.findAllOnlineUsers();
    }
}