package org.minjulog.feedserver.infra.cache.voice;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
public class CaffeineVoiceRoomPresenceStore implements VoiceRoomPresenceStore {

    private final Cache<Long, Set<Long>> roomToUsers = Caffeine.newBuilder()
            .expireAfterAccess(Duration.ofHours(2))
            .build();
    private final ConcurrentMap<String, Long> sessionToUser = new ConcurrentHashMap<>();
    private final ConcurrentMap<Long, Set<String>> userToSessions = new ConcurrentHashMap<>();

    @Override
    public void bindSession(String sessionId, Long userId) {
        if (sessionId == null || userId == null) {
            return;
        }
        sessionToUser.put(sessionId, userId);
        userToSessions.computeIfAbsent(userId, key -> ConcurrentHashMap.newKeySet()).add(sessionId);
    }

    @Override
    public Long findUserIdBySessionId(String sessionId) {
        if (sessionId == null) {
            return null;
        }
        return sessionToUser.get(sessionId);
    }

    @Override
    public Set<Long> removeUserFromAllRoomsBySessionId(String sessionId) {
        if (sessionId == null) {
            return Set.of();
        }

        Long userId = sessionToUser.remove(sessionId);
        if (userId == null) {
            return Set.of();
        }

        Set<String> sessions = userToSessions.get(userId);
        if (sessions != null) {
            sessions.remove(sessionId);
            if (sessions.isEmpty()) {
                userToSessions.remove(userId);
            } else {
                return Set.of();
            }
        }

        return removeUserFromAllRooms(userId);
    }

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
    public Set<Long> removeUserFromAllRooms(Long userId) {
        if (userId == null) {
            return Set.of();
        }

        Set<Long> affectedRoomIds = new HashSet<>();
        roomToUsers.asMap().forEach((roomId, users) -> {
            if (users.remove(userId)) {
                affectedRoomIds.add(roomId);
            }
        });

        affectedRoomIds.forEach(roomId ->
                roomToUsers.asMap().computeIfPresent(roomId, (key, users) -> users.isEmpty() ? null : users)
        );
        return Set.copyOf(affectedRoomIds);
    }

    @Override
    public Set<Long> getOnlineUsers(Long roomId) {
        Set<Long> users = roomToUsers.getIfPresent(roomId);
        return users == null ? Set.of() : Set.copyOf(users);
    }
}
