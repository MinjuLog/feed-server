package org.minjulog.feedserver.presentation.response;

import java.util.List;
import java.util.UUID;

public class VoiceResponse {

    public record IssueToken(
            String token,
            String roomName,
            String identity,
            String participantName
    ) {
    }

    public record ReadMessage(
            UUID id,
            Long roomId,
            Long senderId,
            String senderName,
            String content,
            String createdAt
    ) {
    }

    public record ReadUser(
            Long userId,
            String username
    ) {
    }

    public record ReadRoom(
            Long id,
            String title,
            boolean active,
            String createdAt,
            List<ReadUser> onlineUsers
    ) {
    }

    public record ReadPresence(
            String type,
            Long channelId,
            Long roomId,
            Long userId,
            String username,
            List<ReadUser> onlineUsers
    ) {
    }
}
