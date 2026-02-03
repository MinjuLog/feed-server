package org.minjulog.feedserver.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

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
    private boolean deleted = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder.Default
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "feed_id")
    private List<Attachment> attachments = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "feed", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReactionCount> reactionCounts = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
    }


    public Long getAuthorId() {
        return this.getAuthorProfile().getUserId();
    }
    public String getAuthorName() {
        return this.getAuthorProfile().getUsername();
    }

    public void addAttachment(Attachment attachment) {
        if (this.attachments == null) this.attachments = new ArrayList<>();
        attachments.add(attachment);
        attachment.setFeed(this);
    }

    public void delete() {
        this.deleted = true;
    }
}
