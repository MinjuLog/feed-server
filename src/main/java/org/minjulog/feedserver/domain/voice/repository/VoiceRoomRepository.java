package org.minjulog.feedserver.domain.voice.repository;

import org.minjulog.feedserver.domain.voice.model.VoiceRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VoiceRoomRepository extends JpaRepository<VoiceRoom, Long> {

    boolean existsByChannelIdAndTitle(Long channelId, String title);

    List<VoiceRoom> findByChannelIdOrderByCreatedAtAsc(Long channelId);
}
