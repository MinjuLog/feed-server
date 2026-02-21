package org.minjulog.feedserver.presentation.voice.dto;

import java.util.List;

public record VoiceRoomPresencePayload(
        String type,
        Long channelId,
        Long roomId,
        Long userId,
        String username,
        List<VoiceRoomDto.UserResponse> onlineUsers
) {
}
