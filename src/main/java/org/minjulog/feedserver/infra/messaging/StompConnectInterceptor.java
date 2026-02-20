package org.minjulog.feedserver.infra.messaging;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.minjulog.feedserver.domain.feed.model.UserProfile;
import org.minjulog.feedserver.domain.feed.repository.UserProfileRepository;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class StompConnectInterceptor implements ChannelInterceptor {

    private final UserProfileRepository userProfileRepository;

    @Override
    @Transactional
    public Message<?> preSend(Message<?> message, MessageChannel messageChannel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String rawUserId = Objects.requireNonNull(accessor.getFirstNativeHeader("userId"));
            long userId;
            try {
                userId = Long.parseLong(rawUserId);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("userId must be numeric userId", e);
            }

            UserProfile userProfile = userProfileRepository.findByUserId(userId)
                    .orElseGet(() -> userProfileRepository.saveAndFlush(new UserProfile(userId)));

            accessor.setUser(new StompPrincipal(userProfile.getUserId(), userProfile.getUsername()));
        }

        return message;
    }
}
