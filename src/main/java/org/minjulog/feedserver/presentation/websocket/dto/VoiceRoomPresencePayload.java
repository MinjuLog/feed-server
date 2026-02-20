package org.minjulog.feedserver.presentation.websocket.dto;

import org.minjulog.feedserver.presentation.rest.dto.VoiceRoomDto;

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
