package org.minjulog.feedserver.application.feed;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import org.minjulog.feedserver.application.presence.PresenceStore;
import org.minjulog.feedserver.domain.feed.Feed;
import org.minjulog.feedserver.domain.feed.attachment.Attachment;
import org.minjulog.feedserver.domain.feed.FeedRepository;
import org.minjulog.feedserver.domain.feed.attachment.AttachmentRepository;
import org.minjulog.feedserver.domain.feed.reaction.ReactionRepository;
import org.minjulog.feedserver.domain.feed.reaction.count.ReactionCountRepository;
import org.minjulog.feedserver.domain.profile.ProfileRepository;
import org.minjulog.feedserver.view.FeedController;
import org.minjulog.feedserver.view.FeedController.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class FeedService {

    private final FeedRepository feedRepository;
    private final ReactionCountRepository reactionCountRepository;
    private final ReactionRepository reactionRepository;
    private final AttachmentRepository attachmentRepository;

    private final ProfileRepository profileRepository;

    private final PresenceStore presenceStore;

    private List<Attachment> feedAttachmentsDtoToEntity(
            List<FeedAttachmentRequest> attachments
    ) {
        return attachments.stream()
                .map(attachment -> Attachment.builder()
                        .objectKey(attachment.objectKey())
                        .originalName(attachment.originalName())
                        .contentType(attachment.contentType())
                        .build()
                )
                .toList();
    }

    @Transactional
    public Feed saveFeed(long userId, String content, List<FeedAttachmentRequest> attachments) {
        Feed feed = Feed.builder()
                .authorProfile(profileRepository.findProfileByUserId(userId))
                .content(content)
                .deleted(false)
                .createdAt(LocalDateTime.now())
                .build();
        feedAttachmentsDtoToEntity(attachments).forEach(feed::addAttachment);

        return feedRepository.saveAndFlush(feed);
    }

    @Transactional(readOnly = true)
    public List<FeedController.FeedMessageResponse> findAllFeeds(Long viewerId) {
        List<Feed> feeds = feedRepository.findByDeletedFalseOrderByCreatedAtDesc();
        if (feeds.isEmpty()) return List.of();
        List<Long> feedIds = feeds.stream().map(Feed::getFeedId).toList();

        List<ReactionRepository.MyReactionRow> myReactions =
                reactionRepository.findMyReactions(viewerId, feedIds);

        Map<Long, Set<String>> myReactionsByFeedId =
                myReactions.stream()
                        .collect(Collectors.groupingBy(
                                ReactionRepository.MyReactionRow::getFeedId,
                                Collectors.mapping(
                                        ReactionRepository.MyReactionRow::getReactionKey,
                                        Collectors.toSet()
                                )
                        ));


        List<ReactionCountRepository.ReactionCountRow> reactionCountRows =
                reactionCountRepository.findReactionCountsByFeedIds(feedIds);

        Map<Long, List<FeedReactionResponse>> reactionsByFeedId =
                reactionCountRows.stream()
                        .collect(Collectors.groupingBy(
                                ReactionCountRepository.ReactionCountRow::getFeedId,
                                Collectors.mapping(row -> {
                                            Set<String> myKeys =
                                                    myReactionsByFeedId.getOrDefault(
                                                            row.getFeedId(),
                                                            Set.of()
                                                    );

                                            boolean pressedByMe =
                                                    myKeys.contains(row.getReactionKey());

                                            return new FeedReactionResponse(
                                                    row.getReactionKey(),
                                                    row.getReactionRenderType(),
                                                    row.getImageUrl(),
                                                    row.getUnicode(),
                                                    row.getCount(),
                                                    pressedByMe
                                            );
                                        },
                                        Collectors.toList()
                                )
                        ));

        // 3) 첨부 (feedId -> List<AttachmentResponse>)
        List<Attachment> attachments = attachmentRepository.findByFeedIds(feedIds);

        Map<Long, List<FeedAttachmentResponse>> attachmentsByFeedId = attachments.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        a -> a.getFeed().getFeedId(),
                        java.util.stream.Collectors.mapping(a ->
                                        new FeedController.FeedAttachmentResponse(
                                                a.getObjectKey(),
                                                a.getOriginalName(),
                                                a.getContentType(),
                                                a.getSize()
                                        ),
                                java.util.stream.Collectors.toList()
                        )
                ));

        // 4) 최종 DTO 조립
        return feeds.stream()
                .map(f -> {
                    long feedId = f.getFeedId();

                    return new FeedMessageResponse(
                            feedId,
                            f.getAuthorId(),
                            f.getAuthorProfile().getUsername(),
                            f.getContent(),
                            f.getCreatedAt().toString(),
                            attachmentsByFeedId.getOrDefault(feedId, List.of()),
                            reactionsByFeedId.getOrDefault(feedId, List.of())
                    );
                })
                .toList();
    }


    public Set<String> findAllOnlineUsers() {
        return presenceStore.getOnlineUsers();
    }
}
