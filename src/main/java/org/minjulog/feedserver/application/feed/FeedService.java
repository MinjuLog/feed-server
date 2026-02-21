package org.minjulog.feedserver.application.feed;

import lombok.RequiredArgsConstructor;
import org.minjulog.feedserver.domain.feed.model.Attachment;
import org.minjulog.feedserver.domain.feed.model.Feed;
import org.minjulog.feedserver.domain.feed.model.UserProfile;
import org.minjulog.feedserver.domain.feed.model.Workspace;
import org.minjulog.feedserver.domain.feed.repository.*;
import org.minjulog.feedserver.infra.cache.feed.FeedPresenceStore;
import org.minjulog.feedserver.presentation.feed.dto.AttachmentDto;
import org.minjulog.feedserver.presentation.feed.dto.FeedDto;
import org.minjulog.feedserver.presentation.feed.dto.ReactionDto;
import org.minjulog.feedserver.presentation.feed.dto.AttachmentPayloadDto;
import org.minjulog.feedserver.presentation.feed.dto.FeedPayloadDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FeedService {

    private final FeedRepository feedRepository;
    private final EmojiCountRepository emojiCountRepository;
    private final ReactionRepository reactionRepository;
    private final UserProfileRepository userProfileRepository;
    private final WorkspaceRepository workspaceRepository;
    private final FeedPresenceStore feedPresenceStore;

    @Value("${env.REACTION.WORKSPACE_ID:1}")
    private Long defaultWorkspaceId;

    @Transactional
    public FeedPayloadDto.Response messagingFeed(Long actorId, FeedPayloadDto.Request payload) {
        UserProfile author = getOrCreateUserProfile(actorId);
        Workspace workspace = resolveWorkspace(payload.workspaceId());

        Feed feed = Feed.builder()
                .workspace(workspace)
                .authorUserProfile(author)
                .content(payload.content())
                .build();

        feedAttachmentsDtoToEntity(payload.attachments()).forEach(feed::addAttachment);

        Feed saved = feedRepository.save(feed);

        return new FeedPayloadDto.Response(
                saved.getId(),
                saved.getWorkspace().getId(),
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
        if (feeds.isEmpty()) {
            return List.of();
        }

        List<UUID> feedIds = feeds.stream().map(Feed::getId).toList();

        List<ReactionRepository.MyReactionRow> myReactions =
                reactionRepository.findMyReactions(viewerId, feedIds);

        Map<UUID, Set<String>> myReactionsByFeedId =
                myReactions.stream()
                        .collect(Collectors.groupingBy(
                                ReactionRepository.MyReactionRow::getFeedId,
                                Collectors.mapping(
                                        ReactionRepository.MyReactionRow::getEmojiKey,
                                        Collectors.toSet()
                                )
                        ));

        List<EmojiCountRepository.EmojiCountRow> reactionCountRows =
                emojiCountRepository.findReactionCountsByFeedIds(feedIds);

        Map<UUID, List<ReactionDto.Response>> reactionsByFeedId =
                reactionCountRows.stream()
                        .collect(Collectors.groupingBy(
                                EmojiCountRepository.EmojiCountRow::getFeedId,
                                Collectors.mapping(row -> {
                                            Set<String> myKeys = myReactionsByFeedId.getOrDefault(row.getFeedId(), Set.of());
                                            boolean pressedByMe = myKeys.contains(row.getEmojiKey());

                                            return new ReactionDto.Response(
                                                    row.getEmojiKey(),
                                                    row.getEmojiType(),
                                                    row.getObjectKey(),
                                                    row.getUnicode(),
                                                    row.getEmojiCount(),
                                                    pressedByMe
                                            );
                                        },
                                        Collectors.toList()
                                )
                        ));

        return feeds.stream()
                .map(f -> {
                    UUID feedId = f.getId();

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
                            f.getAuthorUserProfile().getUsername(),
                            f.getContent(),
                            f.getCreatedAt().toString(),
                            attachmentDtos,
                            reactionsByFeedId.getOrDefault(feedId, List.of())
                    );
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public Set<String> findReactionPressedUsers(UUID feedId, Long userId, String emojiKey) {
        List<UUID> userProfileIds = reactionRepository.findUserProfileIdsByFeedIdAndEmojiKey(feedId, emojiKey);
        if (userProfileIds.isEmpty()) {
            return Set.of();
        }
        return userProfileRepository.findUsernamesByUserProfileIdIn(userProfileIds);
    }

    @Transactional
    public DeleteFeedResult deleteFeed(Long userId, UUID feedId) {
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new IllegalArgumentException("feed not found"));
        Long workspaceId = feed.getWorkspace().getId();

        if (feed.isDeleted()) {
            return new DeleteFeedResult(true, workspaceId, feedId);
        }

        if (!feed.getAuthorId().equals(userId)) {
            throw new IllegalStateException("not author");
        }

        feed.delete();
        return new DeleteFeedResult(true, workspaceId, feedId);
    }

    public Set<String> findAllOnlineUsers() {
        return feedPresenceStore.getOnlineUsers();
    }

    private UserProfile getOrCreateUserProfile(Long userId) {
        return userProfileRepository.findByUserId(userId)
                .orElseGet(() -> userProfileRepository.saveAndFlush(new UserProfile(userId)));
    }

    private Workspace resolveWorkspace(Long workspaceId) {
        if (workspaceId != null) {
            return workspaceRepository.findById(workspaceId)
                    .orElseThrow(() -> new IllegalArgumentException("workspace not found"));
        }

        return workspaceRepository.findById(defaultWorkspaceId)
                .orElseGet(() -> workspaceRepository.saveAndFlush(
                        Workspace.builder()
                                .likeCount(0L)
                                .build()
                ));
    }

    private List<Attachment> feedAttachmentsDtoToEntity(List<AttachmentPayloadDto.Request> attachments) {
        if (attachments == null || attachments.isEmpty()) {
            return List.of();
        }

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

    public record DeleteFeedResult(
            boolean deleted,
            Long workspaceId,
            UUID feedId
    ) {
    }
}
