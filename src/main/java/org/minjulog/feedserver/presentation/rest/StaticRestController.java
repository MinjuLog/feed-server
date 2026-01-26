package org.minjulog.feedserver.presentation.rest;

import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import org.minjulog.feedserver.presentation.rest.dto.PreSignedDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
@RequiredArgsConstructor
public class StaticRestController {

    private final MinioClient minioClient;

    @Value("${env.MINIO.BUCKET_NAME}")
    private String MINIO_BUCKET_NAME;

    @Value("${env.MINIO.EXTERNAL_BASE}")
    private String MINIO_EXTERNAL_BASE;

    @GetMapping("/api/pre-signed-url")
    public PreSignedDto.Response sendPreSignedUrl(@ModelAttribute PreSignedDto.Request req) throws Exception {
        String safeName = sanitize(req.fileName());
        String objectKey = req.uploadType().name().toLowerCase() + "/" + UUID.randomUUID() + "-" + safeName;

        String url = minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .method(Method.PUT)
                        .bucket(MINIO_BUCKET_NAME)
                        .object(objectKey)
                        .expiry(10, TimeUnit.MINUTES)
                        .build()
        );

        return new PreSignedDto.Response(objectKey, rewritePreSignedUrl(url));
    }

    // 임시로 처리 (추후 정적 리소스 업로드 URL 을 위한 서브 도메인 설정 필요)
    private String rewritePreSignedUrl(String preSignedUrl) {
        URI u = URI.create(preSignedUrl);
        // u.getPath(): /minjulog/feed/xxx.jpg
        String path = u.getPath().replaceFirst("^/minjulog", ""); // -> /feed/xxx.jpg
        // 외부에서 접근 가능한 base (nginx)
        return MINIO_EXTERNAL_BASE + path + (u.getQuery() != null ? "?" + u.getQuery() : "");
    }

    private String sanitize(String filename) {
        if (filename == null) return "file";
        return filename.replaceAll("[\\\\/\\r\\n\\t\0]", "_");
    }
}
