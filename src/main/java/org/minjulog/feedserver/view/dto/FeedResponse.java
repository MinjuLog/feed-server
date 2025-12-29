package org.minjulog.feedserver.view.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.minjulog.feedserver.domain.feed.Feed;

@Data
@NoArgsConstructor @AllArgsConstructor
public class FeedResponse {
    private Long id;
    private Long authorId;
    private String authorName;
    private String timestamp;
    private String content;
    private int likes;

    public static FeedResponse feedToFeedResponse(Feed feed) {
        return new FeedResponse(
                feed.getFeedId(),
                feed.getAuthorId(),
                feed.getAuthorName(),
                feed.getCreatedAt().toString(),
                feed.getContent(),
                feed.getLikeCount()
        );
    }
}
