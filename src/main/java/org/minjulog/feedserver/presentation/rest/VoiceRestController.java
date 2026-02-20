package org.minjulog.feedserver.presentation.rest;

import lombok.RequiredArgsConstructor;
import org.minjulog.feedserver.application.LiveKitTokenService;
import org.minjulog.feedserver.presentation.rest.dto.LiveKitTokenDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class VoiceRestController {

    private final LiveKitTokenService liveKitTokenService;

    @PostMapping("/api/voice/livekit/token")
    public ResponseEntity<LiveKitTokenDto.Response> issueLiveKitToken(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody LiveKitTokenDto.Request request
    ) {
        return ResponseEntity.ok(liveKitTokenService.issueToken(userId, request));
    }
}
