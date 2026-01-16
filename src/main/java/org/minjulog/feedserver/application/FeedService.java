package org.minjulog.feedserver.application;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.minjulog.feedserver.domain.feed.Feed;
import org.minjulog.feedserver.domain.feed.FeedAttachment;
import org.minjulog.feedserver.domain.feed.FeedRepository;
import org.minjulog.feedserver.domain.profile.ProfileRepository;
import org.minjulog.feedserver.view.FeedController.FeedAttachmentRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class FeedService {

    private final FeedRepository feedRepository;
    private final ProfileRepository profileRepository;
    private final PresenceStore presenceStore;

    private List<FeedAttachment> feedAttachmentsDtoToEntity(
            List<FeedAttachmentRequest> attachments
    ) {
        return attachments.stream()
                .map(attachment -> FeedAttachment.builder()
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
                .likeCount(0)
                .deleted(false)
                .createdAt(LocalDateTime.now())
                .build();
        feedAttachmentsDtoToEntity(attachments).forEach(feed::addAttachment);

        return feedRepository.saveAndFlush(feed);
    }

    @Transactional
    public int like(long userId, long feedId) {
        feedRepository.incrementLike(feedId);
        return feedRepository.findLikeCount(feedId);
    }

    @Transactional(readOnly = true)
    public List<Feed> findAllFeeds() {
        return feedRepository.findByDeletedFalseOrderByCreatedAtDesc();
    }

    public Set<String> findAllOnlineUsers() {
        return presenceStore.getOnlineUsers();
    }
}
