package org.minjulog.feedserver.domain.repository;

import lombok.RequiredArgsConstructor;
import org.minjulog.feedserver.domain.model.Emoji;
import org.minjulog.feedserver.domain.model.enumeration.EmojiType;
import org.minjulog.feedserver.infrastructure.repository.JpaEmojiRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class EmojiRepository {
    private final JpaEmojiRepository jpa;

    public Optional<Emoji> findByWorkspaceIdAndEmojiKey(Long workspaceId, String emojiKey) { return jpa.findByWorkspaceIdAndEmojiKey(workspaceId, emojiKey); }
    public List<Emoji> findByWorkspaceIdAndEmojiType(Long workspaceId, EmojiType emojiType) { return jpa.findByWorkspaceIdAndEmojiType(workspaceId, emojiType); }
    public Emoji save(Emoji emoji) { return jpa.save(emoji); }
    public Emoji saveAndFlush(Emoji emoji) { return jpa.saveAndFlush(emoji); }
}
