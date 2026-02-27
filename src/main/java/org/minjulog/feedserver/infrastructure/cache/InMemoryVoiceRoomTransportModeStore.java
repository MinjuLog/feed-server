package org.minjulog.feedserver.infrastructure.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.minjulog.feedserver.application.voice.VoiceTransportMode;

import java.time.Duration;

public class InMemoryVoiceRoomTransportModeStore implements VoiceRoomTransportModeStore {

    private final Cache<Long, VoiceTransportMode> roomToMode = Caffeine.newBuilder()
            .expireAfterAccess(Duration.ofHours(2))
            .build();

    @Override
    public VoiceTransportMode getMode(Long roomId) {
        if (roomId == null) {
            return null;
        }
        return roomToMode.getIfPresent(roomId);
    }

    @Override
    public void putMode(Long roomId, VoiceTransportMode mode) {
        if (roomId == null || mode == null) {
            return;
        }
        roomToMode.put(roomId, mode);
    }

    @Override
    public void clear(Long roomId) {
        if (roomId == null) {
            return;
        }
        roomToMode.invalidate(roomId);
    }
}
