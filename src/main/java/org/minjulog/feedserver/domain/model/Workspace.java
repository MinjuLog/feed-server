package org.minjulog.feedserver.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity
@NoArgsConstructor @AllArgsConstructor
@Builder @Getter @Setter
public class Workspace {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="like_count", nullable = false)
    private Long likeCount = 0L;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (this.likeCount == null) {
            this.likeCount = 0L;
        }
        this.createdAt = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
    }
}
