package org.minjulog.feedserver.presentation.rest;

import lombok.RequiredArgsConstructor;
import org.minjulog.feedserver.application.FeedService;
import org.minjulog.feedserver.presentation.rest.dto.FeedDto;
import org.minjulog.feedserver.presentation.rest.dto.ReactionDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

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