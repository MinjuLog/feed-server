package org.minjulog.feedserver.application.feed;

import lombok.RequiredArgsConstructor;
import org.minjulog.feedserver.infra.cache.feed.FeedPresenceStore;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

@Component
@RequiredArgsConstructor
public class FeedPresenceEventHandler {

    private static final String PRESENCE_TOPIC = "/topic/workspace.1/connect";

    private final FeedPresenceStore feedPresenceStore;
    private final SimpMessagingTemplate messagingTemplate;

    @EventListener
    public void onConnected(SessionConnectedEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        String userId = accessor.getUser() != null ? accessor.getUser().getName() : "anonymous";

        feedPresenceStore.onConnected(sessionId, userId);
        messagingTemplate.convertAndSend(PRESENCE_TOPIC, new FeedPresenceEventPayload("JOIN", userId));
    }

    @EventListener
    public void onSubscribe(SessionSubscribeEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        feedPresenceStore.onSubscribe(accessor.getSessionId(), accessor.getDestination());
    }

    @EventListener
    public void onDisconnect(SessionDisconnectEvent event) {
        String sessionId = event.getSessionId();
        String userId = event.getUser() != null ? event.getUser().getName()
                : feedPresenceStore.findUserIdBySessionId(sessionId).orElse("anonymous");

        if (!"anonymous".equals(userId)) {
            feedPresenceStore.removeUser(userId);
        } else {
            feedPresenceStore.onDisconnected(sessionId);
        }
        messagingTemplate.convertAndSend(PRESENCE_TOPIC, new FeedPresenceEventPayload("LEAVE", userId));
    }

    public record FeedPresenceEventPayload(String type, String userId) {
    }
}
