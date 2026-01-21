package org.minjulog.feedserver.domain.feed;

import jakarta.persistence.*;
import lombok.*;
import org.minjulog.feedserver.domain.profile.Profile;

@Entity
@Table(
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"profile_id", "feed_id"})
        }
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedReaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "profile_id", nullable = false)
    private Profile profile;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "feed_id", nullable = false)
    private Feed feed;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReactionType type; // LIKE, LOVE, LAUGH ...
}

