package org.minjulog.feedserver.infra;

import jakarta.transaction.Transactional;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.minjulog.feedserver.application.StompPrincipal;
import org.minjulog.feedserver.domain.profile.*;
import org.springframework.messaging.*;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.messaging.support.*;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StompConnectInterceptor implements ChannelInterceptor {

    private final ProfileRepository profileRepository;

    @Override
    @Transactional
    public Message<?> preSend(Message<?> message, MessageChannel messageChannel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            long userId = Long.parseLong(Objects.requireNonNull(accessor.getFirstNativeHeader("userId")));

            Profile existingProfile = profileRepository.findProfileByUserId(userId);

            if (existingProfile == null) {
                Profile profile = new Profile(userId);
                profileRepository.saveAndFlush(profile);
                existingProfile = profile;
            }

            accessor.setUser(new StompPrincipal(userId, existingProfile.getUsername()));
        }

        return message;
    }
}
