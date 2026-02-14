package org.minjulog.feedserver.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(
        name = "reaction_count",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_feed_emoji",
                        columnNames = {"feed_id", "emoji_id"}
                )
        },
        indexes = {
                @Index(name = "idx_reaction_count_feed", columnList = "feed_id"),
                @Index(name = "idx_reaction_count_feed_emoji", columnList = "feed_id,emoji_id")
        }
)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EmojiCount {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "feed_id", nullable = false)
    private Feed feed;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "emoji_id", nullable = false)
    private Emoji emoji;

    @Column(name = "emoji_count", nullable = false)
    private Long emojiCount;
}
