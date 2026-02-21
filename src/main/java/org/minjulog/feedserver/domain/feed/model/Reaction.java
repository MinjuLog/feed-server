package org.minjulog.feedserver.domain.feed.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(
        name = "reaction",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_feed_user_profile_emoji",
                        columnNames = {"feed_id", "user_profile_id", "emoji_id"}
                )
        },
        indexes = {
                @Index(name = "idx_reaction_feed", columnList = "feed_id"),
                @Index(name = "idx_reaction_feed_emoji", columnList = "feed_id,emoji_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Reaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_profile_id", nullable = false)
    private UserProfile userProfile;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "feed_id", nullable = false)
    private Feed feed;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "emoji_id", nullable = false)
    private Emoji emoji;
}
