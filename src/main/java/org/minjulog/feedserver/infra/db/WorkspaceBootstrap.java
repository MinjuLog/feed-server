package org.minjulog.feedserver.infra.db;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.minjulog.feedserver.domain.feed.model.Workspace;
import org.minjulog.feedserver.domain.feed.repository.WorkspaceRepository;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class WorkspaceBootstrap {

    private final WorkspaceRepository workspaceRepository;

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
