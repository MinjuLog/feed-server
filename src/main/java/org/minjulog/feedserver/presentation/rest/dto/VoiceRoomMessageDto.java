package org.minjulog.feedserver.presentation.rest.dto;

import java.util.UUID;

public class VoiceRoomMessageDto {

    public record Request(
            String content
    ) {
    }

    public record Response(
            UUID id,
            Long roomId,
            Long senderId,
            String senderName,
            String content,
            String createdAt
    ) {
    }
}
