package org.minjulog.feedserver.presentation.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.minjulog.feedserver.application.FeedService;
import org.minjulog.feedserver.application.ReactionService;
import org.minjulog.feedserver.infra.messaging.StompPrincipal;
import org.minjulog.feedserver.presentation.websocket.dto.FeedPayloadDto;
import org.minjulog.feedserver.presentation.websocket.dto.ReactionPayloadDto;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@Slf4j
public class FeedMessageController {

    private final FeedService feedService;
    private final ReactionService reactionService;

    @MessageMapping("/feed")
    @SendTo("/topic/room.1")
    public FeedPayloadDto.Response messageFeed(@Payload FeedPayloadDto.Request payload) {
        return feedService.messagingFeed(payload);
    }

    @MessageMapping("/feed/reaction")
    @SendTo("/topic/room.1/reaction")
    public ReactionPayloadDto.Response messageReaction(@Payload ReactionPayloadDto.Request req, Principal principal) {
        return reactionService.applyReaction(((StompPrincipal) principal).getUserId(), req);
    }
}
