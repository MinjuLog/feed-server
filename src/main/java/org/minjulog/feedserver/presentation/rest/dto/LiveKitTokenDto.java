package org.minjulog.feedserver.presentation.rest.dto;

public class LiveKitTokenDto {

    public record Request(
            String roomName,
            String participantName
    ) {
    }

    public record Response(
            String token,
            String roomName,
            String identity,
            String participantName
    ) {
    }
}
