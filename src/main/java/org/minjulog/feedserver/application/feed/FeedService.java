package org.minjulog.feedserver.application.feed;

import lombok.RequiredArgsConstructor;
import org.minjulog.feedserver.domain.model.Attachment;
import org.minjulog.feedserver.domain.model.Feed;
import org.minjulog.feedserver.domain.model.UserProfile;
import org.minjulog.feedserver.domain.model.Workspace;
import org.minjulog.feedserver.domain.repository.FeedReadRepository;
import org.minjulog.feedserver.infrastructure.cache.FeedPresenceStore;
import org.minjulog.feedserver.domain.repository.FeedRepository;
import org.minjulog.feedserver.domain.repository.ReactionRepository;
import org.minjulog.feedserver.domain.repository.UserProfileRepository;
import org.minjulog.feedserver.domain.repository.WorkspaceRepository;
import org.minjulog.feedserver.presentation.request.*;
import org.minjulog.feedserver.presentation.response.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class FeedService {

    private final FeedRepository feedRepository;
    private final FeedReadRepository feedReadRepository;
    private final ReactionRepository reactionRepository;
    private final UserProfileRepository userProfileRepository;
    private final WorkspaceRepository workspaceRepository;
    private final FeedPresenceStore feedPresenceStore;

    @Value("${env.REACTION.WORKSPACE_ID:1}")
    private Long defaultWorkspaceId;

    @Transactional
    public FeedResponse.Create messagingFeed(Long actorId, FeedRequest.Create payload) {
        UserProfile author = getOrCreateUserProfile(actorId);
        Workspace workspace = resolveWorkspace(payload.workspaceId());

        Feed feed = Feed.builder()
                .workspace(workspace)
                .authorUserProfile(author)
                .content(payload.content())
                .build();

        feedAttachmentsDtoToEntity(payload.attachments()).forEach(feed::addAttachment);

        Feed saved = feedRepository.save(feed);

        return new FeedResponse.Create(
                saved.getId(),
                saved.getWorkspace().getId(),
                saved.getAuthorId(),
                saved.getAuthorName(),
                saved.getContent(),
                saved.getCreatedAt().toString(),
                saved.getAttachments().stream()
                        .map(a -> new AttachmentResponse.Attach(
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
    public List<FeedResponse.Read> findAllFeeds(Long viewerId) {
        List<Feed> feeds = feedRepository.findByDeletedFalseOrderByCreatedAtDesc();
        if (feeds.isEmpty()) {
            return List.of();
        }

        List<UUID> feedIds = feeds.stream().map(Feed::getId).toList();

        Map<UUID, List<ReactionResponse.Read>> reactionsByFeedId =
                feedReadRepository.findReactionsByFeedIds(viewerId, feedIds);

        return feeds.stream()
                .map(f -> {
                    UUID feedId = f.getId();

                    List<AttachmentResponse.Read> attachmentDtos = f.getAttachments().stream()
                            .map(a -> new AttachmentResponse.Read(
                                    a.getObjectKey(),
                                    a.getOriginalName(),
                                    a.getContentType(),
                                    a.getSize()
                            ))
                            .toList();

                    return new FeedResponse.Read(
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

    private List<Attachment> feedAttachmentsDtoToEntity(List<AttachmentRequest.Attach> attachments) {
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
