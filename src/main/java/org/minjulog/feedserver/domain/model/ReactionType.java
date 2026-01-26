package org.minjulog.feedserver.domain.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "reaction_type",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_workspace_key", columnNames = {"workspace_id", "reaction_key"})
        },
        indexes = {
                @Index(name = "idx_workspace_key", columnList = "workspace_id,reaction_key")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ReactionType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "workspace_id", nullable = false)
    private Long workspaceId;

    @Column(name = "reaction_key", nullable = false, length = 64)
    private String reactionKey; // 예: ":party_parrot:" 또는 "party_parrot"

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private EmojiType emojiType;

    @Column(length = 32)
    private String emoji;

    @Column(length = 512)
    private String objectKey;

}
