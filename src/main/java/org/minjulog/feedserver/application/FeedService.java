package org.minjulog.feedserver.application;

import lombok.RequiredArgsConstructor;
import org.minjulog.feedserver.domain.model.Attachment;
import org.minjulog.feedserver.domain.model.Feed;
import org.minjulog.feedserver.domain.repository.*;
import org.minjulog.feedserver.infra.cache.PresenceStore;
import org.minjulog.feedserver.presentation.rest.dto.AttachmentDto;
import org.minjulog.feedserver.presentation.rest.dto.FeedDto;
import org.minjulog.feedserver.presentation.rest.dto.ReactionDto;
import org.minjulog.feedserver.presentation.websocket.dto.AttachmentPayloadDto;
import org.minjulog.feedserver.presentation.websocket.dto.FeedPayloadDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FeedService {

    private final FeedRepository feedRepository;
    private final ReactionCountRepository reactionCountRepository;
    private final ReactionRepository reactionRepository;
    private final AttachmentRepository attachmentRepository;
    private final ProfileRepository profileRepository;
    private final PresenceStore presenceStore;

    @Transactional
    public FeedPayloadDto.Response messagingFeed(FeedPayloadDto.Request payload) {
        Feed feed = Feed.builder()
                .authorProfile(profileRepository.findProfileByUserId(payload.authorId()))
                .content(payload.content())
                .deleted(false)
                .createdAt(LocalDateTime.now())
                .build();
        feedAttachmentsDtoToEntity(payload.attachments()).forEach(feed::addAttachment);

        Feed saved = feedRepository.saveAndFlush(feed);

        return new FeedPayloadDto.Response(
                saved.getFeedId(),
                saved.getAuthorId(),
                saved.getAuthorName(),
                saved.getContent(),
                saved.getCreatedAt().toString(),
                saved.getAttachments().stream()
                        .map(a -> new AttachmentPayloadDto.Response(
                                a.getObjectKey(),
                                a.getOriginalName(),
                                a.getContentType(),
                                a.getSize()
                        ))
                        .toList(),
                new ArrayList<>()
        );
    }

    @Transactional(readOnly = true)
    public List<FeedDto.Response> findAllFeeds(Long viewerId) {
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

        Map<Long, List<ReactionDto.Response>> reactionsByFeedId =
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

                                            return new ReactionDto.Response(
                                                    row.getReactionKey(),
                                                    row.getEmojiType(),
                                                    row.getObjectKey(),
                                                    row.getEmoji(),
                                                    row.getCount(),
                                                    pressedByMe
                                            );
                                        },
                                        Collectors.toList()
                                )
                        ));

        // 3) 첨부 (feedId -> List<AttachmentResponse>)
        List<Attachment> attachments = attachmentRepository.findByFeedIds(feedIds);

        Map<Long, List<AttachmentDto.Response>> attachmentsByFeedId = attachments.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        a -> a.getFeed().getFeedId(),
                        java.util.stream.Collectors.mapping(a ->
                                        new AttachmentDto.Response(
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

                    return new FeedDto.Response(
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

    @Transactional(readOnly = true)
    public Set<String> findReactionPressedUsers(Long feedId, Long userId, String reactionKey) {
        // 1) reaction에서 profileId만 조회 (중복 제거)
        List<Long> profileIds = reactionRepository.findProfileIdsByFeedIdAndReactionKey(feedId, reactionKey);

        if (profileIds.isEmpty()) return Set.of();

        // 2) profileId들로 username 한 번에 조회
        // (정렬이 필요하면 아래 repository에서 ORDER BY를 걸거나, profileIds 순서대로 매핑)
        return profileRepository.findUsernamesByProfileIdIn(profileIds);
    }

    public Set<String> findAllOnlineUsers() {
        return presenceStore.getOnlineUsers();
    }

    private List<Attachment> feedAttachmentsDtoToEntity(
            List<AttachmentPayloadDto.Request> attachments
    ) {
        return attachments.stream()
                .map(attachment -> Attachment.builder()
                        .objectKey(attachment.objectKey())
                        .originalName(attachment.originalName())
                        .contentType(attachment.contentType())
                        .size(attachment.size())
                        .build()
                )
                .toList();
    }
}
