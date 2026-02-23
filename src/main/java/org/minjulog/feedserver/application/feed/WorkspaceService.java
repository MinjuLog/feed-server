package org.minjulog.feedserver.application.feed;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.minjulog.feedserver.domain.model.Workspace;
import org.minjulog.feedserver.domain.repository.WorkspaceRepository;
import org.minjulog.feedserver.presentation.response.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WorkspaceService {

    private final WorkspaceRepository workspaceRepository;

    @Transactional(readOnly = true)
    public WorkspaceResponse.Get getWorkspace(Long workspaceId) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new EntityNotFoundException("Workspace not found"));

        return new WorkspaceResponse.Get(workspace.getLikeCount());
    }

    @Transactional
    public WorkspaceResponse.IncrementLike incrementLike(Long actorId, Long workspaceId, Long delta) {
        workspaceRepository.addLikeCount(workspaceId, delta);
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new EntityNotFoundException("Workspace not found"));
        return new WorkspaceResponse.IncrementLike(actorId, workspaceId, workspace.getLikeCount());
    }
}
