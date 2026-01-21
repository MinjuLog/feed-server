package org.minjulog.feedserver.domain.feed.reaction.type;

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
    private String key; // 예: ":party_parrot:" 또는 "party_parrot"

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private ReactionRenderType renderType; // UNICODE / IMAGE

    // UNICODE일 때 사용 (복합 이모지 포함)
    @Column(length = 32)
    private String unicode; // 예: "👍", "👨‍👩‍👧‍👦"

    // IMAGE일 때 사용 (MinIO/S3 URL 또는 objectKey)
    @Column(length = 512)
    private String imageUrl;

    public void validate() {
        if (renderType == ReactionRenderType.UNICODE && (unicode == null || unicode.isBlank())) {
            throw new IllegalStateException("UNICODE reaction must have unicode");
        }
        if (renderType == ReactionRenderType.IMAGE && (imageUrl == null || imageUrl.isBlank())) {
            throw new IllegalStateException("IMAGE reaction must have imageUrl");
        }
    }
}
