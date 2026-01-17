package org.minjulog.feedserver.view;

import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.ServerException;
import io.minio.errors.XmlParserException;
import io.minio.http.Method;
import java.io.IOException;
import java.net.URI;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.util.*;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.minjulog.feedserver.application.*;
import org.minjulog.feedserver.domain.feed.Feed;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.stereotype.Controller;

import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@Slf4j
public class FeedController {

    private final FeedService feedService;
    private final MinioClient minioClient;

    @Value("${env.MINIO.BUCKET_NAME}")
    private String MINIO_BUCKET_NAME;

    @MessageMapping("/feed")
    @SendTo("/topic/room.1")
    public FeedMessageResponse send(@Payload FeedMessageRequest payload, Principal principal) {
        StompPrincipal stompPrincipal = (StompPrincipal) principal;
        Feed feed = feedService.saveFeed(
                stompPrincipal.getUserId(),
                payload.content(),
                payload.attachments()
        );
        return new FeedMessageResponse(
                feed.getFeedId(),
                feed.getAuthorId(),
                feed.getAuthorName(),
                feed.getContent(),
                feed.getLikeCount(),
                feed.getCreatedAt().toString(),
                feed.getAttachments().stream()
                        .map(a -> new FeedAttachmentResponse(
                                a.getObjectKey(),
                                a.getOriginalName(),
                                a.getContentType(),
                                a.getSize()
                        ))
                        .toList()
        );
    }

    @MessageMapping("/feed/like")
    @SendTo("/topic/room.1/like")
    public LikeResponse pressLike(@Payload LikeRequest req, Principal principal) {
        StompPrincipal stompPrincipal = (StompPrincipal) principal;
        long feedId = req.feedId();
        long userId = stompPrincipal.getUserId();
        feedService.like(userId, feedId);

        return new LikeResponse(userId, feedId);
    }

    @ResponseBody
    @GetMapping("/api/feeds")
    public List<FeedMessageResponse> findAllFeeds() {
        List<Feed> feeds = feedService.findAllFeeds();
        return feeds.stream()
                .map(f -> new FeedMessageResponse(
                        f.getFeedId(),
                        f.getAuthorId(),
                        f.getAuthorProfile().getUsername(),
                        f.getContent(),
                        f.getLikeCount(),
                        f.getCreatedAt().toString(),
                        f.getAttachments().stream()
                                .map(a -> new FeedAttachmentResponse(
                                        a.getObjectKey(),
                                        a.getOriginalName(),
                                        a.getContentType(),
                                        a.getSize()
                                ))
                                .toList()
                ))
                .toList();
    }

    @ResponseBody
    @GetMapping("/api/online-users")
    public Set<String> findAllOnlineUsers() {
        return feedService.findAllOnlineUsers();
    }

    @ResponseBody
    @GetMapping("/api/pre-signed-url")
        public PreSignedUrlResponse sendPreSignUrl(@ModelAttribute PreSignedUrlRequest req)
            throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
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

        return new PreSignedUrlResponse(objectKey, rewritePreSignedUrl(url));
    }

    // 임시로 처리 (추후 정적 리소스 업로드 URL 을 위한 서브 도메인 설정 필요)
    private String rewritePreSignedUrl(String preSignedUrl) {
        // 외부에서 접근 가능한 base (nginx)
        String externalBase = "https://perfume-palette-for-u.com:1107/minjulog-static";

        URI u = URI.create(preSignedUrl);

        // u.getPath(): /minjulog/feed/xxx.jpg
        String path = u.getPath().replaceFirst("^/minjulog", ""); // -> /feed/xxx.jpg

        return externalBase + path + (u.getQuery() != null ? "?" + u.getQuery() : "");
    }

    private String sanitize(String filename) {
        if (filename == null) return "file";
        return filename.replaceAll("[\\\\/\\r\\n\\t\0]", "_");
    }

    enum UploadType {
        PROFILE, FEED
    }

    public record FeedMessageRequest(
            long authorId,
            String content,
            List<FeedAttachmentRequest> attachments
    ) {}

    public record FeedMessageResponse(
            long id,
            long authorId,
            String authorName,
            String content,
            int likes,
            String timestamp,
            List<FeedAttachmentResponse> attachments
    ) {}

    public record FeedAttachmentRequest(String objectKey, String originalName, String contentType, long size) {}
    public record FeedAttachmentResponse(String objectKey, String originalName, String contentType, long size) {}
    public record LikeRequest(long feedId) {}
    public record LikeResponse(long actorId, long feedId) {}
    public record PreSignedUrlRequest(UploadType uploadType, String fileName) {}
    public record PreSignedUrlResponse(String objectKey, String uploadUrl) {}

}