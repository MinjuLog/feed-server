package org.minjulog.feedserver.domain.feed.repository;

import org.minjulog.feedserver.domain.feed.model.Emoji;
import org.minjulog.feedserver.domain.feed.model.enumerate.EmojiType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EmojiRepository extends JpaRepository<Emoji, UUID> {

    Optional<Emoji> findByWorkspaceIdAndEmojiKey(Long workspaceId, String emojiKey);

    List<Emoji> findByWorkspaceIdAndEmojiType(Long workspaceId, EmojiType emojiType);
}
