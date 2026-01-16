package org.minjulog.feedserver.domain.feed;
import jakarta.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor @AllArgsConstructor
@Builder @Getter @Setter
public class FeedAttachment {

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
