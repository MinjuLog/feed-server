package org.minjulog.feedserver.application.voice;

import lombok.RequiredArgsConstructor;
import org.minjulog.feedserver.infra.cache.voice.VoiceRoomPresenceStore;
import org.minjulog.feedserver.infra.messaging.StompPrincipal;
import org.minjulog.feedserver.presentation.voice.dto.VoiceRoomPresencePayload;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;

@Component
@RequiredArgsConstructor
public class VoiceRoomPresenceEventHandler {

    private static final String VOICE_CHANNEL_PRESENCE_TOPIC_PREFIX = "/topic/voice.channel.";

    private final VoiceService voiceService;
    private final VoiceRoomPresenceStore voiceRoomPresenceStore;
    private final SimpMessagingTemplate messagingTemplate;

    @EventListener
    public void onConnected(SessionConnectedEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Long userId = resolveUserId(accessor.getUser());
        if (userId == null) {
            return;
        }
        voiceRoomPresenceStore.bindSession(accessor.getSessionId(), userId);
    }

    @Order(Ordered.HIGHEST_PRECEDENCE)
    @EventListener
    public void onDisconnect(SessionDisconnectEvent event) {
        String sessionId = event.getSessionId();
        Long userId = resolveUserId(event.getUser());
        if (userId == null) {
            userId = voiceRoomPresenceStore.findUserIdBySessionId(sessionId);
        }
        if (userId == null) {
            return;
        }
        final Long disconnectedUserId = userId;

        var affectedRoomIds = voiceRoomPresenceStore.removeUserFromAllRoomsBySessionId(sessionId);
        if (affectedRoomIds.isEmpty()) {
            return;
        }

        System.out.println(userId);

        String username = voiceService.getUsername(disconnectedUserId);
        voiceService.getDisconnectRoomPresences(affectedRoomIds)
                .forEach(presence -> messagingTemplate.convertAndSend(
                        VOICE_CHANNEL_PRESENCE_TOPIC_PREFIX + presence.channelId(),
                        new VoiceRoomPresencePayload(
                                "LEAVE",
                                presence.channelId(),
                                presence.roomId(),
                                disconnectedUserId,
                                username,
                                presence.onlineUsers()
                        )
                ));
    }

    private Long parseUserId(String userId) {
        if (userId == null) {
            return null;
        }
        try {
            return Long.parseLong(userId);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private Long resolveUserId(Principal principal) {
        if (principal == null) {
            return null;
        }
        if (principal instanceof StompPrincipal stompPrincipal) {
            return stompPrincipal.getUserId();
        }
        return parseUserId(principal.getName());
    }
}
