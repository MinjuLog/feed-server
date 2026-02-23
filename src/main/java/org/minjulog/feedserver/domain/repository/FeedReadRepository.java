package org.minjulog.feedserver.domain.repository;

import lombok.RequiredArgsConstructor;
import org.minjulog.feedserver.infrastructure.repository.JpaEmojiCountRepository;
import org.minjulog.feedserver.infrastructure.repository.JpaReactionRepository;
import org.minjulog.feedserver.presentation.response.ReactionResponse;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class FeedReadRepository {

    private final JpaReactionRepository reactionRepository;
    private final JpaEmojiCountRepository emojiCountRepository;

    public Map<UUID, List<ReactionResponse.Read>> findReactionsByFeedIds(Long viewerId, List<UUID> feedIds) {
        if (feedIds == null || feedIds.isEmpty()) {
            return Map.of();
        }

        List<JpaReactionRepository.MyReactionRow> myReactions =
                reactionRepository.findMyReactions(viewerId, feedIds);

        Map<UUID, Set<String>> myReactionsByFeedId =
                myReactions.stream()
                        .collect(Collectors.groupingBy(
                                JpaReactionRepository.MyReactionRow::getFeedId,
                                Collectors.mapping(
                                        JpaReactionRepository.MyReactionRow::getEmojiKey,
                                        Collectors.toSet()
                                )
                        ));

        List<JpaEmojiCountRepository.EmojiCountRow> reactionCountRows =
                emojiCountRepository.findReactionCountsByFeedIds(feedIds);

        return reactionCountRows.stream()
                .collect(Collectors.groupingBy(
                        JpaEmojiCountRepository.EmojiCountRow::getFeedId,
                        Collectors.mapping(row -> {
                                    Set<String> myKeys = myReactionsByFeedId.getOrDefault(row.getFeedId(), Set.of());
                                    boolean pressedByMe = myKeys.contains(row.getEmojiKey());

                                    return new ReactionResponse.Read(
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
    }
}
