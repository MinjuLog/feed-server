package org.minjulog.feedserver.infrastructure.repository;

import org.minjulog.feedserver.domain.model.Emoji;
import org.minjulog.feedserver.domain.model.enumeration.EmojiType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaEmojiRepository extends JpaRepository<Emoji, UUID> {
    Optional<Emoji> findByWorkspaceIdAndEmojiKey(Long workspaceId, String emojiKey);
    List<Emoji> findByWorkspaceIdAndEmojiType(Long workspaceId, EmojiType emojiType);
}
