package org.minjulog.feedserver.presentation.request;

import tools.jackson.databind.JsonNode;

public class VoiceRequest {

    public enum MeshSignalType {
        HELLO,
        OFFER,
        ANSWER,
        ICE,
        LEAVE
    }

    public record MeshSignalDto(
            String roomId,
            Object fromUserId,
            String fromName,
            Object toUserId,
            MeshSignalType type,
            JsonNode sdp,
            JsonNode candidate
    ) {
    }

    public record CreateMessage(
            String content
    ) {
    }

    public record IssueToken(
            String roomName,
            String participantName
    ) {
    }
}
