package org.minjulog.feedserver.domain.voice.repository;

import org.minjulog.feedserver.domain.voice.model.VoiceRoomMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface VoiceRoomMessageRepository extends JpaRepository<VoiceRoomMessage, UUID> {

    List<VoiceRoomMessage> findByRoomIdOrderByCreatedAtAsc(Long roomId);
}
