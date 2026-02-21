package org.minjulog.feedserver.presentation.voice.dto;


public class VoiceRoomDto {

    public record UserResponse(
            Long userId,
            String username
    ) {
    }

    public record RoomResponse(
            Long id,
            String title,
            boolean active,
            String createdAt,
            java.util.List<UserResponse> onlineUsers
    ) {
    }
}
