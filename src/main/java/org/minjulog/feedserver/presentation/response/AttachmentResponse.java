package org.minjulog.feedserver.presentation.response;

public class AttachmentResponse {

    public record Read(String objectKey, String originalName, String contentType, Long size) {
    }

    public record IssuePreSignedUrl(
            String objectKey,
            String uploadUrl
    ) {
    }

    public record Attach(
            String objectKey,
            String originalName,
            String contentType,
            Long size
    ) {
    }
}
