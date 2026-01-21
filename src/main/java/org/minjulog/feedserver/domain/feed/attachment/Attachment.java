package org.minjulog.feedserver.domain.feed.attachment;
import jakarta.persistence.*;
import lombok.*;
import org.minjulog.feedserver.domain.feed.Feed;

@Entity
@NoArgsConstructor @AllArgsConstructor
@Builder @Getter @Setter
public class Attachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feed_id")
    private Feed feed;

    private String objectKey;
    private String originalName;
    private String contentType;
    private long size;
}
