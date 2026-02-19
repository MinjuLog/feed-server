package org.minjulog.feedserver.presentation.rest;

import lombok.RequiredArgsConstructor;
import org.minjulog.feedserver.application.ReactionService;
import org.minjulog.feedserver.application.ReactionTypeService;
import org.minjulog.feedserver.presentation.rest.dto.ReactionDto;
import org.minjulog.feedserver.presentation.websocket.dto.ReactionPayloadDto;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ReactionRestController {

    private static final String REACTION_TOPIC = "/topic/workspace.1/reaction";

    private final ReactionTypeService reactionTypeService;
    private final ReactionService reactionService;
    private final SimpMessagingTemplate messagingTemplate;

    @GetMapping("/api/custom-emojis")
    public ReactionDto.CustomEmojisResponse sendCustomEmojiObjectKeys() {
        return reactionTypeService.getCustomEmojis();
    }

    @PostMapping("/api/custom-emojis")
    public ReactionDto.CustomEmojiResponse addCustomEmoji(@RequestBody ReactionDto.CreateCustomEmojiRequest req) {
        return reactionTypeService.createCustomEmoji(req.emojiKey(), req.objectKey());
    }

    @PostMapping("/api/feeds/reactions")
    public ResponseEntity<ReactionPayloadDto.Response> applyReaction(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody ReactionPayloadDto.Request req
    ) {
        ReactionPayloadDto.Response result = reactionService.applyReaction(userId, req);
        messagingTemplate.convertAndSend(REACTION_TOPIC, result);
        return ResponseEntity.ok(result);
    }
}
