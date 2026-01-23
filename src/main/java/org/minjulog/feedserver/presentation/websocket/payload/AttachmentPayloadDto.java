package org.minjulog.feedserver.presentation.websocket.payload;

public class AttachmentPayloadDto {

    public record Request(
            String objectKey,
            String originalName,
            String contentType,
            long size
    ) {}

    public record Response(
            String objectKey,
            String originalName,
            String contentType,
            long size
    ) {}
}
