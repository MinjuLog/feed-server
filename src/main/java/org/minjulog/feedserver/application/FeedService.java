package org.minjulog.feedserver.application;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.minjulog.feedserver.domain.feed.Feed;
import org.minjulog.feedserver.domain.feed.FeedRepository;
import org.minjulog.feedserver.domain.profile.ProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class FeedService {

    private final FeedRepository feedRepository;
    private final ProfileRepository profileRepository;
    private final PresenceStore presenceStore;

    @Transactional
    public Feed saveFeed(long userId, String content) {
        Feed feed = Feed.builder()
                .authorProfile(profileRepository.findProfileByUserId(userId))
                .content(content)
                .likeCount(0)
                .deleted(false)
                .createdAt(LocalDateTime.now())
                .build();

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
