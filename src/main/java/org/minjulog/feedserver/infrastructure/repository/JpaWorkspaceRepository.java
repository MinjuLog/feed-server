package org.minjulog.feedserver.infrastructure.repository;

import org.minjulog.feedserver.domain.model.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JpaWorkspaceRepository extends JpaRepository<Workspace, Long> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            update workspace
            set like_count = like_count + :delta
            where id = :workspaceId
          """, nativeQuery = true)
    Long addLikeCount(@Param("workspaceId") Long workspaceId, @Param("delta") Long delta);
}
