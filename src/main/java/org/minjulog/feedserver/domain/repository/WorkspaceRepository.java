package org.minjulog.feedserver.domain.repository;

import org.minjulog.feedserver.domain.model.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WorkspaceRepository extends JpaRepository<Workspace, Long> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            update worksapce
            set like_count = like_count + :delta
            where id = :workspaceId
          """, nativeQuery = true)
    Long addLikeCount(@Param("workspaceId") Long workspaceId, @Param("delta") Long delta);
 }
