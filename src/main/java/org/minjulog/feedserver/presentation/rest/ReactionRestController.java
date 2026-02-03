package org.minjulog.feedserver.presentation.rest;

import lombok.RequiredArgsConstructor;
import org.minjulog.feedserver.application.ReactionTypeService;
import org.minjulog.feedserver.presentation.rest.dto.ReactionDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ReactionRestController {

    private final ReactionTypeService reactionTypeService;

    @GetMapping("/api/custom-emojis")
    public ReactionDto.CustomEmojisResponse sendCustomEmojiObjectKeys() {
        return reactionTypeService.getCustomEmojis();
    }

    @PostMapping("/api/custom-emojis")
    public ReactionDto.CustomEmojiResponse addCustomEmoji(@RequestBody ReactionDto.CreateCustomEmojiRequest req) {
        return reactionTypeService.createCustomEmoji(req.reactionKey(), req.objectKey());
    }
}
