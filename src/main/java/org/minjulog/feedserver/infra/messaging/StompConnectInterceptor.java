package org.minjulog.feedserver.infra.messaging;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.minjulog.feedserver.domain.model.Profile;
import org.minjulog.feedserver.domain.repository.ProfileRepository;
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
