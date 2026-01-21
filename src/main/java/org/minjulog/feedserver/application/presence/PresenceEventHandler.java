package org.minjulog.feedserver.application.presence;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.*;

@Component
@RequiredArgsConstructor
public class PresenceEventHandler {

    private final PresenceStore store;
    private final SimpMessagingTemplate messagingTemplate;
    private static final String PRESENCE_TOPIC = "/topic/room.1/connect";

    @EventListener
    public void onConnected(SessionConnectedEvent event) {
        StompHeaderAccessor acc = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = acc.getSessionId();
        String userId = acc.getUser() != null ? acc.getUser().getName() : "anonymous";
        store.onConnected(sessionId, userId);

        messagingTemplate.convertAndSend(PRESENCE_TOPIC, new PresenceMessage("JOIN", userId));
    }

    @EventListener
    public void onSubscribe(SessionSubscribeEvent event) {
        StompHeaderAccessor acc = StompHeaderAccessor.wrap(event.getMessage());
        store.onSubscribe(acc.getSessionId(), acc.getDestination());
    }

    @EventListener
    public void onDisconnect(SessionDisconnectEvent event) {
        String sessionId = event.getSessionId();
        String userId = store.findUserIdBySessionId(sessionId).orElse("anonymous");
        store.onDisconnected(sessionId);
        messagingTemplate.convertAndSend(PRESENCE_TOPIC,
                new PresenceMessage("LEAVE", userId));
    }

    public record PresenceMessage(String type, String userId) {}
}
