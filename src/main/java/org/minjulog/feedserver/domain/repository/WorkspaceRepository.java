package org.minjulog.feedserver.domain.repository;

import lombok.RequiredArgsConstructor;
import org.minjulog.feedserver.domain.model.Workspace;
import org.minjulog.feedserver.infrastructure.repository.JpaWorkspaceRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class WorkspaceRepository {
    private final JpaWorkspaceRepository jpa;

    public Optional<Workspace> findById(Long id) { return jpa.findById(id); }
    public Workspace saveAndFlush(Workspace workspace) { return jpa.saveAndFlush(workspace); }
    public Long addLikeCount(Long workspaceId, Long delta) { return jpa.addLikeCount(workspaceId, delta); }
}
