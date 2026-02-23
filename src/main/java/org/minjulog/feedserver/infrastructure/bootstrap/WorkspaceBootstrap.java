package org.minjulog.feedserver.infrastructure.bootstrap;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.minjulog.feedserver.domain.model.Workspace;
import org.minjulog.feedserver.infrastructure.repository.JpaWorkspaceRepository;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class WorkspaceBootstrap {

    private final JpaWorkspaceRepository workspaceRepository;

    @EventListener(ApplicationReadyEvent.class)
    public void ensureWorkspaceExists() {
        if (workspaceRepository.count() > 0) {
            return;
        }

        workspaceRepository.saveAndFlush(
                Workspace.builder()
                        .likeCount(0L)
                        .build()
        );
        log.info("No workspace found. Created initial workspace.");
    }
}
