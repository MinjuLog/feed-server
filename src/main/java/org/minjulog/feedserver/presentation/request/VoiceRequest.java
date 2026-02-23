package org.minjulog.feedserver.presentation.request;

public class VoiceRequest {

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
