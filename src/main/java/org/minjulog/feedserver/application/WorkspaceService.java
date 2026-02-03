package org.minjulog.feedserver.application;

import lombok.RequiredArgsConstructor;
import org.minjulog.feedserver.domain.repository.WorkspaceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WorkspaceService {

    private final WorkspaceRepository workspaceRepository;

    @Transactional
    public Long incrementLike(Long workspaceId, Long delta) {
        return workspaceRepository.addLikeCount(workspaceId, delta);
    }
}
