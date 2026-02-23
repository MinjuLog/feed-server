package org.minjulog.feedserver.presentation.controller;

import lombok.RequiredArgsConstructor;
import org.minjulog.feedserver.application.feed.ReactionService;
import org.minjulog.feedserver.presentation.request.*;
import org.minjulog.feedserver.presentation.response.*;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ReactionController {

    private static final String REACTION_TOPIC = "/topic/workspace.1/reaction";

        private final ReactionService reactionService;
    private final SimpMessagingTemplate messagingTemplate;

    @GetMapping("/api/custom-emojis")
    public ReactionResponse.FindCustomEmojis sendCustomEmojiObjectKeys() {
        return reactionService.getCustomEmojis();
    }

    @PostMapping("/api/custom-emojis")
    public ReactionResponse.FindCustomEmoji addCustomEmoji(@RequestBody ReactionRequest.CreateCustomEmoji req) {
        return reactionService.createCustomEmoji(req.emojiKey(), req.objectKey());
    }

    @PostMapping("/api/feeds/reactions")
    public ResponseEntity<ReactionResponse.Apply> applyReaction(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody ReactionRequest.Apply req
    ) {
        ReactionResponse.Apply result = reactionService.applyReaction(userId, req);
        messagingTemplate.convertAndSend(REACTION_TOPIC, result);
        return ResponseEntity.ok(result);
    }
}
