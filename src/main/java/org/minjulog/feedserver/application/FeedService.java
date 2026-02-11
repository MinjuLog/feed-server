package org.minjulog.feedserver.application;

import lombok.RequiredArgsConstructor;
import org.minjulog.feedserver.domain.model.Attachment;
import org.minjulog.feedserver.domain.model.Feed;
import org.minjulog.feedserver.domain.repository.FeedRepository;
import org.minjulog.feedserver.domain.repository.ProfileRepository;
import org.minjulog.feedserver.domain.repository.ReactionCountRepository;
import org.minjulog.feedserver.domain.repository.ReactionRepository;
import org.minjulog.feedserver.infra.cache.PresenceStore;
import org.minjulog.feedserver.presentation.rest.dto.AttachmentDto;
import org.minjulog.feedserver.presentation.rest.dto.FeedDto;
import org.minjulog.feedserver.presentation.rest.dto.ReactionDto;
import org.minjulog.feedserver.presentation.websocket.dto.AttachmentPayloadDto;
import org.minjulog.feedserver.presentation.websocket.dto.FeedPayloadDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final ProfileRepository profileRepository;
    private final PresenceStore presenceStore;

    @Transactional
    public FeedPayloadDto.Response messagingFeed(FeedPayloadDto.Request payload) {
        Feed feed = Feed.builder()
                .authorProfile(profileRepository.findProfileByUserId(payload.authorId()))
                .content(payload.content())
                .build();
        feedAttachmentsDtoToEntity(payload.attachments()).forEach(feed::addAttachment);

        Feed saved = feedRepository.save(feed);

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

        return feeds.stream()
                .map(f -> {
                    long feedId = f.getFeedId();

                    List<AttachmentDto.Response> attachmentDtos = f.getAttachments().stream()
                            .map(a -> new AttachmentDto.Response(
                                    a.getObjectKey(),
                                    a.getOriginalName(),
                                    a.getContentType(),
                                    a.getSize()
                            ))
                            .toList();

                    return new FeedDto.Response(
                            feedId,
                            f.getAuthorId(),
                            f.getAuthorProfile().getUsername(),
                            f.getContent(),
                            f.getCreatedAt().toString(),
                            attachmentDtos,
                            reactionsByFeedId.getOrDefault(feedId, List.of())
                    );
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public Set<String> findReactionPressedUsers(Long feedId, Long userId, String reactionKey) {
        List<Long> profileIds = reactionRepository.findProfileIdsByFeedIdAndReactionKey(feedId, reactionKey);

        if (profileIds.isEmpty()) return Set.of();

        return profileRepository.findUsernamesByProfileIdIn(profileIds);
    }

    @Transactional
    public Boolean deleteFeed(Long userId, Long feedId) {
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new IllegalArgumentException("feed not found"));

        if (feed.isDeleted()) {
            return true;
        }

        if (!feed.getAuthorId().equals(userId)) {
            throw new IllegalStateException("not author");
        }

        feed.delete();
        return true;
    }

    public Set<String> findAllOnlineUsers() {
        return presenceStore.getOnlineUsers();
    }

    private List<Attachment> feedAttachmentsDtoToEntity(
            List<AttachmentPayloadDto.Request> attachments
    ) {
        if (attachments == null || attachments.isEmpty()) return List.of();
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
