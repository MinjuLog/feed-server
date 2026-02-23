package org.minjulog.feedserver.infrastructure.repository;

import org.minjulog.feedserver.domain.model.VoiceChannel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JpaVoiceChannelRepository extends JpaRepository<VoiceChannel, Long> {

    Optional<VoiceChannel> findByWorkspaceIdAndTitle(Long workspaceId, String title);
}
