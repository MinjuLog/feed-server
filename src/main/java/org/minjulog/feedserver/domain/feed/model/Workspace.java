package org.minjulog.feedserver.domain.feed.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "workspace")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class Workspace {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Builder.Default
    @Column(name = "like_count", nullable = false)
    private Long likeCount = 0L;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder.Default
    @OneToMany(mappedBy = "workspace")
    private List<Feed> feeds = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "workspace")
    private List<Emoji> emojis = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (this.likeCount == null) {
            this.likeCount = 0L;
        }
        this.createdAt = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
    }
}
