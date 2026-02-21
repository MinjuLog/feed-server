package org.minjulog.feedserver.presentation.feed.dto;

public class AttachmentPayloadDto {

    public record Request(
            String objectKey,
            String originalName,
            String contentType,
            Long size
    ) {}

    public record Response(
            String objectKey,
            String originalName,
            String contentType,
            Long size
    ) {}
}
