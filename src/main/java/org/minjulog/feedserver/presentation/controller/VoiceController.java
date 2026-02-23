package org.minjulog.feedserver.presentation.controller;

import lombok.RequiredArgsConstructor;
import org.minjulog.feedserver.application.voice.VoiceService;
import org.minjulog.feedserver.presentation.request.*;
import org.minjulog.feedserver.presentation.response.*;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class VoiceController {

    private static final String VOICE_CHANNEL_PRESENCE_TOPIC_PREFIX = "/topic/voice.channel.";
    private static final String VOICE_ROOM_CHAT_TOPIC_PREFIX = "/topic/voice.room.";

        private final VoiceService voiceService;
        private final SimpMessagingTemplate messagingTemplate;

    @PostMapping("/api/voice/livekit/token")
    public ResponseEntity<VoiceResponse.IssueToken> issueLiveKitToken(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody VoiceRequest.IssueToken request
    ) {
        return ResponseEntity.ok(voiceService.issueToken(userId, request));
    }

    @GetMapping("/api/voice/channels/{channelId}/rooms")
    public ResponseEntity<List<VoiceResponse.ReadRoom>> findRooms(
            @PathVariable("channelId") Long channelId
    ) {
        return ResponseEntity.ok(voiceService.findRooms(channelId));
    }

    @PostMapping("/api/voice/rooms/{roomId}/join")
    public ResponseEntity<Void> joinRoom(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable("roomId") Long roomId
    ) {
        Long channelId = voiceService.getRoom(roomId).getChannel().getId();
        voiceService.joinRoom(roomId, userId);
        List<VoiceResponse.ReadUser> onlineUsers = voiceService.getOnlineUsers(roomId);
        messagingTemplate.convertAndSend(
                VOICE_CHANNEL_PRESENCE_TOPIC_PREFIX + channelId,
                new VoiceResponse.ReadPresence(
                        "JOIN",
                        channelId,
                        roomId,
                        userId,
                        voiceService.getUsername(userId),
                        onlineUsers
                )
        );
        return ResponseEntity.ok().build();
    }

    @PostMapping("/api/voice/rooms/{roomId}/leave")
    public ResponseEntity<Void> leaveRoom(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable("roomId") Long roomId
    ) {
        Long channelId = voiceService.getRoom(roomId).getChannel().getId();
        voiceService.leaveRoom(roomId, userId);
        List<VoiceResponse.ReadUser> onlineUsers = voiceService.getOnlineUsers(roomId);
        messagingTemplate.convertAndSend(
                VOICE_CHANNEL_PRESENCE_TOPIC_PREFIX + channelId,
                new VoiceResponse.ReadPresence(
                        "LEAVE",
                        channelId,
                        roomId,
                        userId,
                        voiceService.getUsername(userId),
                        onlineUsers
                )
        );
        return ResponseEntity.ok().build();
    }

    @GetMapping("/api/voice/rooms/{roomId}/messages")
    public ResponseEntity<List<VoiceResponse.ReadMessage>> findMessages(
            @PathVariable("roomId") Long roomId
    ) {
        return ResponseEntity.ok(voiceService.findMessages(roomId));
    }

    @PostMapping("/api/voice/rooms/{roomId}/messages")
    public ResponseEntity<VoiceResponse.ReadMessage> createMessage(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable("roomId") Long roomId,
            @RequestBody VoiceRequest.CreateMessage request
    ) {
        VoiceResponse.ReadMessage response = voiceService.createMessage(roomId, userId, request);
        messagingTemplate.convertAndSend(
                VOICE_ROOM_CHAT_TOPIC_PREFIX + roomId + ".chat",
                response
        );
        return ResponseEntity.ok(response);
    }
}
