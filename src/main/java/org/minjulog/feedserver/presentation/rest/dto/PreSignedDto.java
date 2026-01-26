package org.minjulog.feedserver.presentation.rest.dto;

public class PreSignedDto {

    public record Request(
            UploadType uploadType,
            String fileName
    ) {}
    public record Response(
            String objectKey,
            String uploadUrl
    ) {}

    public enum UploadType {
        PROFILE, FEED, CUSTOM_EMOJI
    }
}
