package org.minjulog.feedserver.infrastructure.repository;

import org.minjulog.feedserver.domain.model.VoiceRoomMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface JpaVoiceRoomMessageRepository extends JpaRepository<VoiceRoomMessage, UUID> {
    List<VoiceRoomMessage> findByRoomIdOrderByCreatedAtAsc(Long roomId);
}
