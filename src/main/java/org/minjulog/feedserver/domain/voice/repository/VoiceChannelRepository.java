package org.minjulog.feedserver.domain.voice.repository;

import org.minjulog.feedserver.domain.voice.model.VoiceChannel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VoiceChannelRepository extends JpaRepository<VoiceChannel, Long> {

    Optional<VoiceChannel> findByWorkspaceIdAndTitle(Long workspaceId, String title);
}
