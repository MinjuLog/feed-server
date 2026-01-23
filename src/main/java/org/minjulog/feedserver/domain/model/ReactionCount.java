package org.minjulog.feedserver.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "reaction_count",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_feed_reaction_type",
                        columnNames = {"feed_id", "reaction_type_id"}
                )
        },
        indexes = {
                @Index(name = "idx_feed", columnList = "feed_id")
        }
)
@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
public class ReactionCount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // MANY ReactionCount -> ONE Feed
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "feed_id", nullable = false)
    private Feed feed;

    // MANY ReactionCount -> ONE ReactionType
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reaction_type_id", nullable = false)
    private ReactionType reactionType;

    @Column(nullable = false)
    private int count;
}

