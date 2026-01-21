package org.minjulog.feedserver.application.feed;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.minjulog.feedserver.application.presence.PresenceStore;
import org.minjulog.feedserver.domain.feed.Feed;
import org.minjulog.feedserver.domain.feed.attachment.Attachment;
import org.minjulog.feedserver.domain.feed.FeedRepository;
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
    private final ProfileRepository profileRepository;
    private final ReactionCountRepository reactionCountRepository;

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
    public List<FeedController.FeedMessageResponse> findAllFeeds() {
        List<Feed> feeds = feedRepository.findByDeletedFalseOrderByCreatedAtDesc();
        List<Long> feedIds = feeds.stream().map(Feed::getFeedId).toList();

        List<ReactionCountRepository.ReactionCountRow> reactionCountRows =
                reactionCountRepository.findReactionCountsByFeedIds(feedIds);

    }

    public Set<String> findAllOnlineUsers() {
        return presenceStore.getOnlineUsers();
    }
}
