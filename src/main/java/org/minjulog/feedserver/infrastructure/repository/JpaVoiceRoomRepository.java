package org.minjulog.feedserver.infrastructure.repository;

import org.minjulog.feedserver.domain.model.VoiceRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaVoiceRoomRepository extends JpaRepository<VoiceRoom, Long> {
    boolean existsByChannelIdAndTitle(Long channelId, String title);
    List<VoiceRoom> findByChannelIdOrderByCreatedAtAsc(Long channelId);
}
