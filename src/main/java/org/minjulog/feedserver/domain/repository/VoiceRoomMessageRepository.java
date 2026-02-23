package org.minjulog.feedserver.domain.repository;

import lombok.RequiredArgsConstructor;
import org.minjulog.feedserver.domain.model.VoiceRoomMessage;
import org.minjulog.feedserver.infrastructure.repository.JpaVoiceRoomMessageRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class VoiceRoomMessageRepository {
    private final JpaVoiceRoomMessageRepository jpa;

    public VoiceRoomMessage save(VoiceRoomMessage message) { return jpa.save(message); }
    public List<VoiceRoomMessage> findByRoomIdOrderByCreatedAtAsc(Long roomId) { return jpa.findByRoomIdOrderByCreatedAtAsc(roomId); }
}
