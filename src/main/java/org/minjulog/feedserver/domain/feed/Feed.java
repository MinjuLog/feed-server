package org.minjulog.feedserver.domain.feed;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import lombok.*;
import org.minjulog.feedserver.domain.profile.Profile;

@Entity @Table(name = "feed")
@Getter @Builder
@NoArgsConstructor @AllArgsConstructor
public class Feed {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long feedId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_profile_id", nullable = false)
    private Profile authorProfile;

    @Column(nullable = false, length = 1000)
    private String content;

    @Column(nullable = false)
    private int likeCount;

    @Column(nullable = false)
    private boolean deleted = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
    }

    public void like() {
        this.likeCount++;
    }

    public void unlike() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }

    public long getAuthorId() {
        return this.getAuthorProfile().getUserId();
    }

    public String getAuthorName() {
        return this.getAuthorProfile().getUsername();
    }

    public void delete() {
        this.deleted = true;
    }
}
