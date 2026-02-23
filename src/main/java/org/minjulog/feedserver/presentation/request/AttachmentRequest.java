package org.minjulog.feedserver.presentation.request;

public class AttachmentRequest {

    public record Attach(String objectKey, String originalName, String contentType, Long size) {}

    public record IssuePreSignedUrl(
            AttachmentRequest.UploadType uploadType,
            String fileName
    ) {
    }

    public enum UploadType {
        PROFILE, FEED, CUSTOM_EMOJI
    }
}
