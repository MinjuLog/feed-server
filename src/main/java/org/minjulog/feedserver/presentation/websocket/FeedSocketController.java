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
public class FeedSocketController {

    private final FeedService feedService;
    private final ReactionService reactionService;

    @MessageMapping("/feed")
    @SendTo("/topic/workspace.1")
    public FeedPayloadDto.Response publishFeed(@Payload FeedPayloadDto.Request payload, Principal principal) {
        Long actorId = extractActorId(payload.authorId(), principal);
        return feedService.messagingFeed(actorId, payload);
    }

    @MessageMapping("/feed/reaction")
    @SendTo("/topic/workspace.1/reaction")
    public ReactionPayloadDto.Response publishReaction(@Payload ReactionPayloadDto.Request req, Principal principal) {
        if (!(principal instanceof StompPrincipal stompPrincipal)) {
            throw new IllegalStateException("principal is required for reaction");
        }
        return reactionService.applyReaction(stompPrincipal.getUserId(), req);
    }

    private Long extractActorId(Long payloadAuthorId, Principal principal) {
        if (principal instanceof StompPrincipal stompPrincipal) {
            return stompPrincipal.getUserId();
        }
        if (payloadAuthorId != null) {
            return payloadAuthorId;
        }
        throw new IllegalStateException("authorId is required when principal is missing");
    }
}
