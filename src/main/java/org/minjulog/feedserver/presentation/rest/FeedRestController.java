package org.minjulog.feedserver.presentation.rest;

import io.minio.*;
import java.net.URI;
import java.util.*;
import java.util.concurrent.TimeUnit;

import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import org.minjulog.feedserver.application.FeedService;
import org.minjulog.feedserver.presentation.rest.dto.FeedDto;
import org.minjulog.feedserver.presentation.rest.dto.PreSignedDto;
import org.minjulog.feedserver.presentation.rest.dto.ReactionDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class FeedRestController {

    private final FeedService feedService;

    @GetMapping("/api/feeds")
    public List<FeedDto.Response> findAllFeeds(
            @RequestHeader("X-User-Id") Long userId
    ) {
        return feedService.findAllFeeds(userId);
    }

    @GetMapping("/api/feeds/{feedId}/reactions/{reactionKey}/users")
    public ReactionDto.PressedUsersResponse sendReactionPressedUsers(
            @PathVariable("feedId") Long feedId,
            @PathVariable("reactionKey") String reactionKey,
            @RequestHeader("X-User-Id") Long userId
    ) {
        return new ReactionDto.PressedUsersResponse(feedService.findReactionPressedUsers(feedId, userId, reactionKey));
    }


    @GetMapping("/api/online-users")
    public Set<String> findAllOnlineUsers() {
        return feedService.findAllOnlineUsers();
    }

}