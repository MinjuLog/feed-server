package org.minjulog.feedserver.application;

import io.livekit.server.AccessToken;
import io.livekit.server.RoomJoin;
import io.livekit.server.RoomName;
import lombok.RequiredArgsConstructor;
import org.minjulog.feedserver.infra.livekit.LiveKitProperties;
import org.minjulog.feedserver.presentation.rest.dto.LiveKitTokenDto;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class LiveKitTokenService {

    private final LiveKitProperties properties;

    public LiveKitTokenDto.Response issueToken(Long userId, LiveKitTokenDto.Request request) {
        if (userId == null) {
            throw new IllegalArgumentException("userId is required");
        }
        if (request == null || !StringUtils.hasText(request.roomName())) {
            throw new IllegalArgumentException("roomName is required");
        }
        if (!StringUtils.hasText(properties.getApiKey()) || !StringUtils.hasText(properties.getApiSecret())) {
            throw new IllegalStateException("LiveKit API key/secret is not configured");
        }

        String identity = userId.toString();
        String participantName = StringUtils.hasText(request.participantName())
                ? request.participantName()
                : "user-" + userId;

        AccessToken token = new AccessToken(properties.getApiKey(), properties.getApiSecret());
        token.setIdentity(identity);
        token.setName(participantName);
        token.addGrants(new RoomJoin(true), new RoomName(request.roomName()));

        return new LiveKitTokenDto.Response(
                token.toJwt(),
                request.roomName(),
                identity,
                participantName
        );
    }
}
