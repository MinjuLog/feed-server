package org.minjulog.feedserver.domain.feed.reaction;

import jakarta.persistence.*;
import lombok.*;
import org.minjulog.feedserver.domain.feed.Feed;
import org.minjulog.feedserver.domain.feed.reaction.type.ReactionType;
import org.minjulog.feedserver.domain.profile.Profile;

@Entity
@Table(
        name = "reaction",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_feed_profile_type",
                        columnNames = {"feed_id", "profile_id", "reaction_type_id"}
                )
        },
        indexes = {
                @Index(name = "idx_feed", columnList = "feed_id"),
                @Index(name = "idx_feed_type", columnList = "feed_id,reaction_type_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Reaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "profile_id", nullable = false)
    private Profile profile;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "feed_id", nullable = false)
    private Feed feed;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reaction_type_id", nullable = false)
    private ReactionType type; // LIKE, LOVE, LAUGH ...
}
