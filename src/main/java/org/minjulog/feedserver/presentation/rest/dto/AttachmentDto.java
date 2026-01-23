package org.minjulog.feedserver.presentation.rest.dto;

public class AttachmentDto {

    public record Request(String objectKey, String originalName, String contentType, long size) {}
    public record Response(String objectKey, String originalName, String contentType, long size) {}
}
