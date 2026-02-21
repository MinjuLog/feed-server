package org.minjulog.feedserver.infra.cache.voice;

import java.util.Set;

public interface VoiceRoomPresenceStore {

    void bindSession(String sessionId, Long userId);

    Long findUserIdBySessionId(String sessionId);

    Set<Long> removeUserFromAllRoomsBySessionId(String sessionId);

    void addUser(Long roomId, Long userId);

    void removeUser(Long roomId, Long userId);

    Set<Long> removeUserFromAllRooms(Long userId);

    Set<Long> getOnlineUsers(Long roomId);
}
