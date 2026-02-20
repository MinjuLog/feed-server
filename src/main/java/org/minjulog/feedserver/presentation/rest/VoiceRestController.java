package org.minjulog.feedserver.presentation.rest;

import lombok.RequiredArgsConstructor;
import org.minjulog.feedserver.application.LiveKitTokenService;
import org.minjulog.feedserver.application.VoiceService;
import org.minjulog.feedserver.presentation.rest.dto.LiveKitTokenDto;
import org.minjulog.feedserver.presentation.rest.dto.VoiceRoomDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class VoiceRestController {

    private final LiveKitTokenService liveKitTokenService;
    private final VoiceService voiceService;

    @PostMapping("/api/voice/livekit/token")
    public ResponseEntity<LiveKitTokenDto.Response> issueLiveKitToken(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody LiveKitTokenDto.Request request
    ) {
        return ResponseEntity.ok(liveKitTokenService.issueToken(userId, request));
    }

    @GetMapping("/api/voice/channels/{channelId}/rooms")
    public ResponseEntity<List<VoiceRoomDto.RoomResponse>> findRooms(
            @PathVariable("channelId") Long channelId
    ) {
        return ResponseEntity.ok(voiceService.findRooms(channelId));
    }

    @PostMapping("/api/voice/rooms/{roomId}/join")
    public ResponseEntity<Void> joinRoom(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable("roomId") Long roomId
    ) {
        voiceService.joinRoom(roomId, userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/api/voice/rooms/{roomId}/leave")
    public ResponseEntity<Void> leaveRoom(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable("roomId") Long roomId
    ) {
        voiceService.leaveRoom(roomId, userId);
        return ResponseEntity.ok().build();
    }
}
