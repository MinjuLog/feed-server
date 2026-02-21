package org.minjulog.feedserver.domain.feed.model;

import jakarta.persistence.*;
import lombok.*;
import org.minjulog.feedserver.domain.feed.model.enumerate.EmojiType;

import java.util.UUID;

@Entity
@Table(
        name = "emoji",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_workspace_emoji_key", columnNames = {"workspace_id", "emoji_key"})
        },
        indexes = {
                @Index(name = "idx_workspace_emoji_key", columnList = "workspace_id,emoji_key")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Emoji {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    @Column(name = "emoji_key", nullable = false, length = 64)
    private String emojiKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "emoji_type", nullable = false, length = 16)
    private EmojiType emojiType;

    @Column(name = "unicode", length = 32)
    private String unicode;

    @Column(name = "object_key", length = 512)
    private String objectKey;
}
