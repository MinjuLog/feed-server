package org.minjulog.feedserver.application.feed;

import lombok.RequiredArgsConstructor;
import org.minjulog.feedserver.domain.feed.FeedRepository;
import org.minjulog.feedserver.domain.feed.reaction.ReactionRepository;
import org.minjulog.feedserver.domain.feed.reaction.count.ReactionCountRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FeedQueryService {
    private final FeedRepository feedRepository;
    private final ReactionRepository reactionRepository;
    private final ReactionCountRepository reactionCountRepository;
}
