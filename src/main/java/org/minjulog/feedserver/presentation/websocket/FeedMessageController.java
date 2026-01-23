package org.minjulog.feedserver.presentation.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.minjulog.feedserver.application.feed.FeedService;
import org.minjulog.feedserver.application.feed.ReactionService;
import org.minjulog.feedserver.application.principal.StompPrincipal;
import org.minjulog.feedserver.domain.feed.Feed;
import org.minjulog.feedserver.presentation.websocket.payload.AttachmentPayloadDto;
import org.minjulog.feedserver.presentation.websocket.payload.FeedPayloadDto;
import org.minjulog.feedserver.presentation.websocket.payload.ReactionPayloadDto;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.ArrayList;

@Controller
@RequiredArgsConstructor
@Slf4j
public class FeedMessageController {

    private final FeedService feedService;
    private final ReactionService reactionService;

    @MessageMapping("/feed")
    @SendTo("/topic/room.1")
    public FeedPayloadDto.Response send(@Payload FeedPayloadDto.Request payload, Principal principal) {
        StompPrincipal stompPrincipal = (StompPrincipal) principal;
        Feed feed = feedService.saveFeed(
                stompPrincipal.getUserId(),
                payload.content(),
                payload.attachments()
        );
        return new FeedPayloadDto.Response(
                feed.getFeedId(),
                feed.getAuthorId(),
                feed.getAuthorName(),
                feed.getContent(),
                feed.getCreatedAt().toString(),
                feed.getAttachments().stream()
                        .map(a -> new AttachmentPayloadDto.Response(
                                a.getObjectKey(),
                                a.getOriginalName(),
                                a.getContentType(),
                                a.getSize()
                        ))
                        .toList(),
                new ArrayList<>()
        );
    }

    @MessageMapping("/feed/reaction")
    @SendTo("/topic/room.1/reaction")
    public ReactionPayloadDto.Response sendReaction(@Payload ReactionPayloadDto.Request req, Principal principal) {
        StompPrincipal stompPrincipal = (StompPrincipal) principal;
        Long feedId = req.feedId();
        String key = req.key();
        String emoji = req.emoji();
        Long userId = stompPrincipal.getUserId();

        return reactionService.applyReaction(userId, feedId, key, emoji);
    }
}
