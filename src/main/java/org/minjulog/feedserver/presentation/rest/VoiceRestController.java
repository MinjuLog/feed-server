package org.minjulog.feedserver.presentation.rest;

import lombok.RequiredArgsConstructor;
import org.minjulog.feedserver.application.LiveKitTokenService;
import org.minjulog.feedserver.application.VoiceChatService;
import org.minjulog.feedserver.application.VoiceService;
import org.minjulog.feedserver.presentation.rest.dto.LiveKitTokenDto;
import org.minjulog.feedserver.presentation.rest.dto.VoiceRoomMessageDto;
import org.minjulog.feedserver.presentation.rest.dto.VoiceRoomDto;
import org.minjulog.feedserver.presentation.websocket.dto.VoiceRoomPresencePayload;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class VoiceRestController {

    private static final String VOICE_CHANNEL_PRESENCE_TOPIC_PREFIX = "/topic/voice.channel.";
    private static final String VOICE_ROOM_CHAT_TOPIC_PREFIX = "/topic/voice.room.";

    private final LiveKitTokenService liveKitTokenService;
    private final VoiceService voiceService;
    private final VoiceChatService voiceChatService;
    private final SimpMessagingTemplate messagingTemplate;

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
        Long channelId = voiceService.getRoom(roomId).getChannel().getId();
        voiceService.joinRoom(roomId, userId);
        List<VoiceRoomDto.UserResponse> onlineUsers = voiceService.getOnlineUsers(roomId);
        messagingTemplate.convertAndSend(
                VOICE_CHANNEL_PRESENCE_TOPIC_PREFIX + channelId,
                new VoiceRoomPresencePayload(
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
        List<VoiceRoomDto.UserResponse> onlineUsers = voiceService.getOnlineUsers(roomId);
        messagingTemplate.convertAndSend(
                VOICE_CHANNEL_PRESENCE_TOPIC_PREFIX + channelId,
                new VoiceRoomPresencePayload(
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
    public ResponseEntity<List<VoiceRoomMessageDto.Response>> findMessages(
            @PathVariable("roomId") Long roomId
    ) {
        return ResponseEntity.ok(voiceChatService.findMessages(roomId));
    }

    @PostMapping("/api/voice/rooms/{roomId}/messages")
    public ResponseEntity<VoiceRoomMessageDto.Response> createMessage(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable("roomId") Long roomId,
            @RequestBody VoiceRoomMessageDto.Request request
    ) {
        VoiceRoomMessageDto.Response response = voiceChatService.createMessage(roomId, userId, request);
        messagingTemplate.convertAndSend(
                VOICE_ROOM_CHAT_TOPIC_PREFIX + roomId + ".chat",
                response
        );
        return ResponseEntity.ok(response);
    }
}
