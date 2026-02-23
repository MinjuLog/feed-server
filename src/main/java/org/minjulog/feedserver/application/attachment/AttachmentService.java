package org.minjulog.feedserver.application.attachment;

import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import org.minjulog.feedserver.presentation.request.*;
import org.minjulog.feedserver.presentation.response.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AttachmentService {

    private final MinioClient minioClient;

    @Value("${env.MINIO.BUCKET_NAME}")
    private String bucketName;

    @Value("${env.MINIO.EXTERNAL_BASE}")
    private String externalBase;

    public AttachmentResponse.IssuePreSignedUrl issuePreSignedUrl(AttachmentRequest.IssuePreSignedUrl request) throws Exception {
        String safeName = sanitize(request.fileName());
        String objectKey = request.uploadType().name().toLowerCase() + "/" + UUID.randomUUID() + "-" + safeName;

        String url = minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .method(Method.PUT)
                        .bucket(bucketName)
                        .object(objectKey)
                        .expiry(10, TimeUnit.MINUTES)
                        .build()
        );

        return new AttachmentResponse.IssuePreSignedUrl(objectKey, rewritePreSignedUrl(url));
    }

    private String rewritePreSignedUrl(String preSignedUrl) {
        URI u = URI.create(preSignedUrl);
        String path = u.getPath().replaceFirst("^/minjulog", "");
        return externalBase + path + (u.getQuery() != null ? "?" + u.getQuery() : "");
    }

    private String sanitize(String filename) {
        if (filename == null) return "file";
        return filename
                .replace('\u0000', '_')
                .replaceAll("[\\\\/\\r\\n\\t]", "_");
    }
}
