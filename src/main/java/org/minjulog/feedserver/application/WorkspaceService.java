package org.minjulog.feedserver.application;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.minjulog.feedserver.domain.model.Workspace;
import org.minjulog.feedserver.domain.repository.WorkspaceRepository;
import org.minjulog.feedserver.presentation.rest.dto.WorkspaceDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WorkspaceService {

    private final WorkspaceRepository workspaceRepository;

    @Transactional(readOnly = true)
    public WorkspaceDto.WorkspaceResponse getWorkspace(Long workspaceId) {
        Workspace workspace =
                workspaceRepository.findById(workspaceId)
                        .orElseThrow(() -> new EntityNotFoundException("Workspace not found"));

        return new WorkspaceDto.WorkspaceResponse(workspace.getLikeCount());
    }

    @Transactional
    public WorkspaceDto.IncrementLikeResponse incrementLike(Long actorId, Long workspaceId, Long delta) {
        workspaceRepository.addLikeCount(workspaceId, delta);
        Workspace workspace =
                workspaceRepository.findById(workspaceId)
                        .orElseThrow(() -> new EntityNotFoundException("Workspace not found"));
        return new WorkspaceDto.IncrementLikeResponse(actorId, workspaceId, workspace.getLikeCount());
    }
}
