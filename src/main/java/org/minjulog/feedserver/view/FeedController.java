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
import org.minjulog.feedserver.application.feed.FeedService;
import org.minjulog.feedserver.application.principal.StompPrincipal;
import org.minjulog.feedserver.domain.feed.Feed;
import org.minjulog.feedserver.domain.feed.reaction.type.ReactionRenderType;
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

    @Value("${env.MINIO.EXTERNAL_BASE}")
    private String MINIO_EXTERNAL_BASE;

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
                feed.getCreatedAt().toString(),
                feed.getAttachments().stream()
                        .map(a -> new FeedAttachmentResponse(
                                a.getObjectKey(),
                                a.getOriginalName(),
                                a.getContentType(),
                                a.getSize()
                        ))
                        .toList(),
                new ArrayList<>()
        );
    }

    @MessageMapping("/feed/reaction")
    @SendTo("/topic/room.1/reaction")
    public ReactionResponse sendReaction(@Payload ReactionRequest req, Principal principal) {
        StompPrincipal stompPrincipal = (StompPrincipal) principal;
        Long feedId = req.feedId();
        Long userId = stompPrincipal.getUserId();
        String key = req.key();

        return feedService.applyReaction(userId, feedId, key);
    }

    @ResponseBody
    @GetMapping("/api/feeds")
    public List<FeedMessageResponse> findAllFeeds(
            @RequestHeader("X-User-Id") long userId
    ) {
        return feedService.findAllFeeds(userId);
    }

    @ResponseBody
    @GetMapping("/api/feeds/{feedId}/reactions/{reactionKey}/users")
    public ReactionPressedUsersResponse sendReactionPressedUsers(
            @PathVariable Long feedId,
            @PathVariable String reactionKey,
            @RequestHeader("X-User-Id") Long userId
    ) {
        return new ReactionPressedUsersResponse(feedService.findReactionPressedUsers(feedId, userId, reactionKey));
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

    enum UploadType {
        PROFILE, FEED
    }

    public record FeedMessageRequest(
            long authorId,
            String content,
            List<FeedAttachmentRequest> attachments
    ) {}

    public record FeedMessageResponse(
            Long id,
            Long authorId,
            String authorName,
            String content,
            String timestamp,
            List<FeedAttachmentResponse> attachments,
            List<FeedReactionResponse> reactions
    ) {}

    public record FeedAttachmentRequest(String objectKey, String originalName, String contentType, long size) {}
    public record FeedAttachmentResponse(String objectKey, String originalName, String contentType, long size) {}
    public record FeedReactionResponse(String key, ReactionRenderType renderType, String imageUrl, String unicode, Long count, boolean isPressed) {}

    public record ReactionPressedUsersResponse(
            Set<String> usernames
    ) {}
    public record ReactionRequest(Long feedId, String key) {}
    public record ReactionResponse(
            Long actorId,
            Long feedId,
            String key,
            boolean pressedByMe,
            int count,
            ReactionRenderType renderType,
            String unicode,
            String imageUrl
    ) {}

    public record PreSignedUrlRequest(UploadType uploadType, String fileName) {}
    public record PreSignedUrlResponse(String objectKey, String uploadUrl) {}

}