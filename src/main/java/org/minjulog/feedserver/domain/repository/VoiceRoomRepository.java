package org.minjulog.feedserver.domain.repository;

import lombok.RequiredArgsConstructor;
import org.minjulog.feedserver.domain.model.VoiceRoom;
import org.minjulog.feedserver.infrastructure.repository.JpaVoiceRoomRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class VoiceRoomRepository {
    private final JpaVoiceRoomRepository jpa;

    public boolean existsByChannelIdAndTitle(Long channelId, String title) { return jpa.existsByChannelIdAndTitle(channelId, title); }
    public List<VoiceRoom> findByChannelIdOrderByCreatedAtAsc(Long channelId) { return jpa.findByChannelIdOrderByCreatedAtAsc(channelId); }
    public Optional<VoiceRoom> findById(Long id) { return jpa.findById(id); }
    public VoiceRoom save(VoiceRoom room) { return jpa.save(room); }
}
