package org.minjulog.feedserver.infra.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class CaffeineVoiceRoomPresenceStore implements VoiceRoomPresenceStore {

    private final Cache<Long, Set<Long>> roomToUsers = Caffeine.newBuilder()
            .expireAfterAccess(Duration.ofHours(2))
            .build();

    @Override
    public void addUser(Long roomId, Long userId) {
        if (roomId == null || userId == null) {
            return;
        }
        roomToUsers.asMap().compute(roomId, (key, value) -> {
            Set<Long> users = value;
            if (users == null) {
                users = ConcurrentHashMap.newKeySet();
            }
            users.add(userId);
            return users;
        });
    }

    @Override
    public void removeUser(Long roomId, Long userId) {
        if (roomId == null || userId == null) {
            return;
        }
        roomToUsers.asMap().computeIfPresent(roomId, (key, users) -> {
            users.remove(userId);
            return users.isEmpty() ? null : users;
        });
    }

    @Override
    public Set<Long> getOnlineUsers(Long roomId) {
        Set<Long> users = roomToUsers.getIfPresent(roomId);
        return users == null ? Set.of() : Set.copyOf(users);
    }
}
