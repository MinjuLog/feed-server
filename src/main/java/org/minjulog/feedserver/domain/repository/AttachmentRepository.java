package org.minjulog.feedserver.domain.repository;

import org.minjulog.feedserver.domain.model.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AttachmentRepository extends JpaRepository<Attachment, Long> {

    @Query("""
        select a
        from Attachment a
        where a.feed.feedId in :feedIds
    """)
    List<Attachment> findByFeedIds(@Param("feedIds") List<Long> feedIds);

}
